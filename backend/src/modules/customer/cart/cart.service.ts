import { prisma } from '../../../services/prisma.service';
import { CONSTANTS } from '../../../config/constants';

export class CustomerCartService {
  static async getOrCreateCart(userId: string) {
    let cart = await prisma.cart.findUnique({
      where: { userId },
      include: {
        items: {
          include: {
            product: {
              include: {
                images: { where: { isPrimary: true }, take: 1 },
              },
            },
            variant: true,
            deliverySlot: true,
            addOns: {
              include: { addOn: true },
            },
          },
        },
      },
    });

    if (!cart) {
      cart = await prisma.cart.create({
        data: { userId },
        include: {
          items: {
            include: {
              product: {
                include: {
                  images: { where: { isPrimary: true }, take: 1 },
                },
              },
              variant: true,
              deliverySlot: true,
              addOns: {
                include: { addOn: true },
              },
            },
          },
        },
      });
    }

    // Calculate totals
    let subtotal = 0;
    let deliveryFee = 0;

    const formattedItems = cart.items.map((item) => {
      const unitPrice = item.variant ? item.variant.price : item.product.basePrice;
      const itemSubtotal = unitPrice * item.quantity;

      const addOnTotal = item.addOns.reduce((sum, a) => sum + a.addOn.price * a.quantity, 0);
      const totalItemPrice = itemSubtotal + addOnTotal;

      subtotal += totalItemPrice;

      if (item.deliverySlot && item.deliverySlot.fee > deliveryFee) {
        deliveryFee = item.deliverySlot.fee;
      }

      return {
        id: item.id,
        productId: item.productId,
        productTitle: item.product.title,
        productSlug: item.product.slug,
        productImage: item.product.images[0]?.imageUrl || null,
        variantId: item.variantId,
        variantTitle: item.variant?.title || null,
        unitPrice,
        quantity: item.quantity,
        personalizationText: item.personalizationText,
        deliveryDate: item.deliveryDate,
        deliverySlot: item.deliverySlot,
        addOns: item.addOns.map((a) => ({
          addOnId: a.addOnId,
          title: a.addOn.title,
          price: a.addOn.price,
          quantity: a.quantity,
          total: a.addOn.price * a.quantity,
        })),
        totalItemPrice,
      };
    });

    const taxAmount = Math.round(subtotal * CONSTANTS.DEFAULT_TAX_RATE);
    const totalAmount = subtotal + deliveryFee + taxAmount;

    return {
      cartId: cart.id,
      items: formattedItems,
      itemCount: formattedItems.reduce((acc, item) => acc + item.quantity, 0),
      subtotal,
      deliveryFee,
      taxAmount,
      totalAmount,
    };
  }

  static async addItem(userId: string, data: any) {
    const cart = await prisma.cart.findUnique({ where: { userId } });
    const cartId = cart ? cart.id : (await prisma.cart.create({ data: { userId } })).id;

    // Verify product & variant
    const product = await prisma.product.findUnique({
      where: { id: data.productId },
      include: { variants: true },
    });

    if (!product || !product.isActive) {
      throw new Error('PRODUCT_NOT_AVAILABLE');
    }

    const variantId = data.variantId || product.variants.find((v) => v.isDefault)?.id || product.variants[0]?.id;

    // Create cart item
    const cartItem = await prisma.cartItem.create({
      data: {
        cartId,
        productId: data.productId,
        variantId,
        quantity: data.quantity || 1,
        personalizationText: data.personalizationText || null,
        deliveryDate: data.deliveryDate ? new Date(data.deliveryDate) : null,
        deliverySlotId: data.deliverySlotId || null,
      },
    });

    // Add attached add-ons
    if (data.addOns && Array.isArray(data.addOns)) {
      for (const addon of data.addOns) {
        if (addon.quantity > 0) {
          await prisma.cartItemAddOn.create({
            data: {
              cartItemId: cartItem.id,
              addOnId: addon.addOnId,
              quantity: addon.quantity,
            },
          });
        }
      }
    }

    return this.getOrCreateCart(userId);
  }

  static async updateItem(userId: string, cartItemId: string, data: any) {
    const cart = await prisma.cart.findUnique({ where: { userId } });
    if (!cart) throw new Error('CART_NOT_FOUND');

    const item = await prisma.cartItem.findFirst({
      where: { id: cartItemId, cartId: cart.id },
    });

    if (!item) throw new Error('ITEM_NOT_FOUND_IN_CART');

    await prisma.cartItem.update({
      where: { id: cartItemId },
      data: {
        ...(data.quantity !== undefined && { quantity: data.quantity }),
        ...(data.personalizationText !== undefined && { personalizationText: data.personalizationText }),
        ...(data.deliveryDate !== undefined && { deliveryDate: new Date(data.deliveryDate) }),
        ...(data.deliverySlotId !== undefined && { deliverySlotId: data.deliverySlotId }),
      },
    });

    if (data.addOns && Array.isArray(data.addOns)) {
      for (const addon of data.addOns) {
        if (addon.quantity <= 0) {
          await prisma.cartItemAddOn.deleteMany({
            where: { cartItemId, addOnId: addon.addOnId },
          });
        } else {
          await prisma.cartItemAddOn.upsert({
            where: { cartItemId_addOnId: { cartItemId, addOnId: addon.addOnId } },
            update: { quantity: addon.quantity },
            create: { cartItemId, addOnId: addon.addOnId, quantity: addon.quantity },
          });
        }
      }
    }

    return this.getOrCreateCart(userId);
  }

  static async removeItem(userId: string, cartItemId: string) {
    const cart = await prisma.cart.findUnique({ where: { userId } });
    if (!cart) throw new Error('CART_NOT_FOUND');

    await prisma.cartItem.deleteMany({
      where: { id: cartItemId, cartId: cart.id },
    });

    return this.getOrCreateCart(userId);
  }
}
