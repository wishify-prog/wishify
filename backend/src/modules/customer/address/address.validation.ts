import { z } from 'zod';
import { AddressType } from '@prisma/client';

export const CreateAddressSchema = z.object({
  body: z.object({
    name: z.string().min(2, 'Recipient name is required'),
    phone: z.string().min(10, 'Valid phone number is required'),
    addressLine1: z.string().min(5, 'Address line 1 is required'),
    addressLine2: z.string().optional(),
    landmark: z.string().optional(),
    city: z.string().min(2, 'City is required'),
    state: z.string().min(2, 'State is required'),
    pincode: z.string().length(6, 'Pincode must be 6 digits'),
    latitude: z.number().optional(),
    longitude: z.number().optional(),
    addressType: z.nativeEnum(AddressType).default(AddressType.HOME),
    isDefault: z.boolean().default(false),
  }),
});

export const UpdateAddressSchema = z.object({
  params: z.object({
    id: z.string().uuid('Invalid address ID'),
  }),
  body: CreateAddressSchema.shape.body.partial(),
});
