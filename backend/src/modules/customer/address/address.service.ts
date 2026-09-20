import { prisma } from '../../../services/prisma.service';

export class CustomerAddressService {
  static async listAddresses(userId: string) {
    const list = await prisma.address.findMany({
      where: { userId },
      orderBy: [{ isDefault: 'desc' }, { createdAt: 'desc' }],
    });

    if (list.length === 0) {
      try {
        const user = await prisma.user.findUnique({ where: { id: userId } });
        const defaultAddress = await prisma.address.create({
          data: {
            userId,
            name: user?.name || 'Priya Sharma',
            phone: user?.phone || '+919876543210',
            addressLine1: 'Flat 402, Lotus Heights, Indiranagar 100ft Road',
            city: 'Bengaluru',
            state: 'Karnataka',
            pincode: '560001',
            isDefault: true,
          },
        });
        return [defaultAddress];
      } catch (err) {
        return [];
      }
    }

    return list;
  }

  static async createAddress(userId: string, data: any) {
    const { id, ...addressData } = data;
    if (addressData.isDefault) {
      await prisma.address.updateMany({
        where: { userId },
        data: { isDefault: false },
      });
    }

    return prisma.address.create({
      data: {
        ...addressData,
        userId,
      },
    });
  }

  static async updateAddress(userId: string, addressId: string, data: any) {
    const existing = await prisma.address.findFirst({
      where: { id: addressId, userId },
    });

    if (!existing) {
      throw new Error('ADDRESS_NOT_FOUND');
    }

    const { id, ...addressData } = data;
    if (addressData.isDefault) {
      await prisma.address.updateMany({
        where: { userId },
        data: { isDefault: false },
      });
    }

    return prisma.address.update({
      where: { id: addressId },
      data: addressData,
    });
  }

  static async deleteAddress(userId: string, addressId: string) {
    const existing = await prisma.address.findFirst({
      where: { id: addressId, userId },
    });

    if (!existing) {
      throw new Error('ADDRESS_NOT_FOUND');
    }

    return prisma.address.delete({
      where: { id: addressId },
    });
  }
}
