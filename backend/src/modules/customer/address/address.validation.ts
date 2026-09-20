import { z } from 'zod';
import { AddressType } from '@prisma/client';

export const CreateAddressSchema = z.object({
  body: z.object({
    id: z.string().optional().nullable(),
    name: z.string().min(1, 'Recipient name is required'),
    phone: z.string().min(6, 'Valid phone number is required'),
    addressLine1: z.string().min(1, 'Address line 1 is required'),
    addressLine2: z.string().optional().nullable(),
    landmark: z.string().optional().nullable(),
    city: z.string().min(1, 'City is required'),
    state: z.string().min(1, 'State is required'),
    pincode: z.string().min(3, 'Valid pincode is required'),
    latitude: z.number().optional().nullable(),
    longitude: z.number().optional().nullable(),
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
