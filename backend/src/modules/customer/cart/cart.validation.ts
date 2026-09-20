import { z } from 'zod';

export const AddCartItemSchema = z.object({
  body: z.object({
    productId: z.string().uuid('Invalid product ID'),
    variantId: z.string().uuid('Invalid variant ID').optional(),
    quantity: z.number().int().min(1).default(1),
    personalizationText: z.string().max(100).optional(),
    deliveryDate: z.string().optional(),
    deliverySlotId: z.string().uuid('Invalid delivery slot ID').optional(),
    addOns: z
      .array(
        z.object({
          addOnId: z.string().uuid(),
          quantity: z.number().int().min(1).default(1),
        })
      )
      .optional(),
  }),
});

export const UpdateCartItemSchema = z.object({
  params: z.object({
    id: z.string().uuid('Invalid cart item ID'),
  }),
  body: z.object({
    quantity: z.number().int().min(1).optional(),
    personalizationText: z.string().max(100).optional(),
    deliveryDate: z.string().optional(),
    deliverySlotId: z.string().uuid().optional(),
    addOns: z
      .array(
        z.object({
          addOnId: z.string().uuid(),
          quantity: z.number().int().min(0),
        })
      )
      .optional(),
  }),
});
