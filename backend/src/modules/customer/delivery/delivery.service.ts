import { prisma } from '../../../services/prisma.service';

export class CustomerDeliveryService {
  static async checkPincode(code: string) {
    const pincode = await prisma.pincode.findUnique({
      where: { code },
    });

    if (!pincode || !pincode.isServiceable) {
      return {
        isServiceable: false,
        code,
        message: 'Delivery is currently not available for this pincode.',
      };
    }

    return {
      isServiceable: true,
      code: pincode.code,
      city: pincode.city,
      state: pincode.state,
      isSameDayAvailable: pincode.isSameDayAvailable,
      isMidnightAvailable: pincode.isMidnightAvailable,
      standardDeliveryFee: pincode.standardDeliveryFee,
      midnightDeliveryFee: pincode.midnightDeliveryFee,
      message: `Delivery available in ${pincode.city}, ${pincode.state}`,
    };
  }

  static async getAvailableSlots(pincodeCode?: string, dateStr?: string) {
    const slots = await prisma.deliverySlot.findMany({
      where: { isActive: true },
      orderBy: { startTime: 'asc' },
    });

    const targetDate = dateStr ? new Date(dateStr) : new Date();
    const today = new Date();
    const isToday =
      targetDate.getFullYear() === today.getFullYear() &&
      targetDate.getMonth() === today.getMonth() &&
      targetDate.getDate() === today.getDate();

    const currentHour = today.getHours();

    // Check pincode capabilities if provided
    let pincodeInfo = null;
    if (pincodeCode) {
      pincodeInfo = await prisma.pincode.findUnique({ where: { code: pincodeCode } });
    }

    const evaluatedSlots = slots.map((slot) => {
      let isAvailable = true;
      let reason: string | null = null;

      // Check slot type against pincode capabilities
      if (pincodeInfo) {
        if (slot.slotType === 'MIDNIGHT' && !pincodeInfo.isMidnightAvailable) {
          isAvailable = false;
          reason = 'Midnight delivery not available in this area';
        }
        if (isToday && !pincodeInfo.isSameDayAvailable) {
          isAvailable = false;
          reason = 'Same-day delivery not available in this area';
        }
      }

      // Check cut-off time for today
      if (isToday && isAvailable) {
        const [slotStartHour] = slot.startTime.split(':').map(Number);
        if (currentHour + slot.cutoffHoursBefore >= slotStartHour) {
          isAvailable = false;
          reason = 'Order cut-off time has passed for this slot';
        }
      }

      return {
        ...slot,
        isAvailable,
        reason,
      };
    });

    return evaluatedSlots;
  }
}
