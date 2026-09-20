import { PrismaClient, Role, SlotType, DiscountType, AddressType } from '@prisma/client';
import * as bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  console.log('🌱 Starting Wishify database seeding...');

  // 1. Seed Users
  console.log('👤 Seeding users...');
  const salt = await bcrypt.genSalt(10);
  const adminPasswordHash = await bcrypt.hash('AdminPassword123!', salt);
  const customerPasswordHash = await bcrypt.hash('CustomerPass123!', salt);

  const superAdmin = await prisma.user.upsert({
    where: { email: 'admin@wishify.in' },
    update: {},
    create: {
      email: 'admin@wishify.in',
      phone: '+919999999999',
      name: 'Super Admin',
      role: Role.SUPER_ADMIN,
      passwordHash: adminPasswordHash,
      isPhoneVerified: true,
      isActive: true,
    },
  });

  const customer = await prisma.user.upsert({
    where: { phone: '+919876543210' },
    update: {},
    create: {
      email: 'customer@wishify.in',
      phone: '+919876543210',
      name: 'Priya Sharma',
      role: Role.CUSTOMER,
      passwordHash: customerPasswordHash,
      isPhoneVerified: true,
      isActive: true,
      walletBalance: 250.0,
    },
  });

  // 2. Seed Addresses
  const existingAddress = await prisma.address.findFirst({ where: { userId: customer.id } });
  if (!existingAddress) {
    await prisma.address.create({
      data: {
        userId: customer.id,
        name: 'Priya Sharma',
        phone: '+919876543210',
        addressLine1: 'Flat 402, Lotus Heights',
        addressLine2: 'Indiranagar 100ft Road',
        landmark: 'Near Metro Station',
        city: 'Bengaluru',
        state: 'Karnataka',
        pincode: '560001',
        latitude: 12.9716,
        longitude: 77.5946,
        addressType: AddressType.HOME,
        isDefault: true,
      },
    });
  }

  // 3. Seed Pincodes
  console.log('📍 Seeding pincodes...');
  const pincodeData = [
    { code: '560001', city: 'Bengaluru', state: 'Karnataka', isSameDayAvailable: true, isMidnightAvailable: true, standardDeliveryFee: 0, midnightDeliveryFee: 249 },
    { code: '110001', city: 'New Delhi', state: 'Delhi', isSameDayAvailable: true, isMidnightAvailable: true, standardDeliveryFee: 0, midnightDeliveryFee: 249 },
    { code: '400001', city: 'Mumbai', state: 'Maharashtra', isSameDayAvailable: true, isMidnightAvailable: true, standardDeliveryFee: 0, midnightDeliveryFee: 249 },
    { code: '500001', city: 'Hyderabad', state: 'Telangana', isSameDayAvailable: true, isMidnightAvailable: true, standardDeliveryFee: 0, midnightDeliveryFee: 249 },
    { code: '411001', city: 'Pune', state: 'Maharashtra', isSameDayAvailable: true, isMidnightAvailable: true, standardDeliveryFee: 0, midnightDeliveryFee: 249 },
    { code: '600001', city: 'Chennai', state: 'Tamil Nadu', isSameDayAvailable: true, isMidnightAvailable: false, standardDeliveryFee: 49, midnightDeliveryFee: 299 },
    { code: '700001', city: 'Kolkata', state: 'West Bengal', isSameDayAvailable: true, isMidnightAvailable: false, standardDeliveryFee: 49, midnightDeliveryFee: 299 },
    { code: '302001', city: 'Jaipur', state: 'Rajasthan', isSameDayAvailable: false, isMidnightAvailable: false, standardDeliveryFee: 99, midnightDeliveryFee: 349 },
  ];

  for (const pin of pincodeData) {
    await prisma.pincode.upsert({
      where: { code: pin.code },
      update: pin,
      create: pin,
    });
  }

  // 4. Seed Delivery Slots
  console.log('⏰ Seeding delivery slots...');
  const slotData = [
    { title: 'Early Morning (6 AM - 9 AM)', slotType: SlotType.EARLY_MORNING, startTime: '06:00', endTime: '09:00', fee: 199.0, cutoffHoursBefore: 12 },
    { title: 'Standard Morning (9 AM - 12 PM)', slotType: SlotType.STANDARD, startTime: '09:00', endTime: '12:00', fee: 0.0, cutoffHoursBefore: 2 },
    { title: 'Standard Afternoon (12 PM - 3 PM)', slotType: SlotType.STANDARD, startTime: '12:00', endTime: '15:00', fee: 0.0, cutoffHoursBefore: 2 },
    { title: 'Standard Evening (3 PM - 6 PM)', slotType: SlotType.STANDARD, startTime: '15:00', endTime: '18:00', fee: 0.0, cutoffHoursBefore: 2 },
    { title: 'Standard Night (6 PM - 9 PM)', slotType: SlotType.STANDARD, startTime: '18:00', endTime: '21:00', fee: 0.0, cutoffHoursBefore: 2 },
    { title: 'Fixed Time (11 AM - 12 PM)', slotType: SlotType.FIXED_TIME, startTime: '11:00', endTime: '12:00', fee: 149.0, cutoffHoursBefore: 3 },
    { title: 'Fixed Time (5 PM - 6 PM)', slotType: SlotType.FIXED_TIME, startTime: '17:00', endTime: '18:00', fee: 149.0, cutoffHoursBefore: 3 },
    { title: 'Midnight Delivery (11 PM - 12 AM)', slotType: SlotType.MIDNIGHT, startTime: '23:00', endTime: '23:59', fee: 249.0, cutoffHoursBefore: 4 },
  ];

  for (const slot of slotData) {
    const existing = await prisma.deliverySlot.findFirst({ where: { title: slot.title } });
    if (!existing) {
      await prisma.deliverySlot.create({ data: slot });
    }
  }

  // 5. Seed Coupons
  console.log('🎟️ Seeding coupons...');
  const couponData = [
    { code: 'FIRSTGIFT', description: '20% OFF on your first gift order', discountType: DiscountType.PERCENTAGE, discountValue: 20, minOrderValue: 499, maxDiscountAmount: 200, validTo: new Date('2027-12-31') },
    { code: 'WISH500', description: 'Flat ₹500 OFF on premium gifts above ₹2499', discountType: DiscountType.FLAT, discountValue: 500, minOrderValue: 2499, maxDiscountAmount: 500, validTo: new Date('2027-12-31') },
    { code: 'FESTIVE15', description: '15% OFF festive celebrations', discountType: DiscountType.PERCENTAGE, discountValue: 15, minOrderValue: 999, maxDiscountAmount: 300, validTo: new Date('2027-12-31') },
  ];

  for (const c of couponData) {
    await prisma.coupon.upsert({
      where: { code: c.code },
      update: c,
      create: c,
    });
  }

  // 6. Seed Categories
  console.log('📂 Seeding categories...');
  const categoryData = [
    { name: 'Flowers', slug: 'flowers', description: 'Fresh roses, orchids, lilies, and carnations', imageUrl: 'https://images.unsplash.com/photo-1561181286-d3fee7d55364?w=600', sortOrder: 1 },
    { name: 'Cakes', slug: 'cakes', description: 'Decadent chocolate, black forest, red velvet, and cheesecakes', imageUrl: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=600', sortOrder: 2 },
    { name: 'Plants', slug: 'plants', description: 'Air purifying, indoor bonsai, succulents and lucky bamboo', imageUrl: 'https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=600', sortOrder: 3 },
    { name: 'Chocolates', slug: 'chocolates', description: 'Handcrafted truffles, Ferrero Rocher, and luxury hampers', imageUrl: 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=600', sortOrder: 4 },
    { name: 'Personalised Gifts', slug: 'personalised-gifts', description: 'Custom photo mugs, LED frames, cushions and keychains', imageUrl: 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=600', sortOrder: 5 },
    { name: 'Combos', slug: 'combos', description: 'Flower & cake, flowers & chocolates, luxury celebration hampers', imageUrl: 'https://images.unsplash.com/photo-1513151233558-d860c5398176?w=600', sortOrder: 6 },
  ];

  const categoryMap = new Map<string, string>();
  for (const cat of categoryData) {
    const created = await prisma.category.upsert({
      where: { slug: cat.slug },
      update: cat,
      create: cat,
    });
    categoryMap.set(cat.slug, created.id);
  }

  // 7. Seed Occasions
  console.log('🎉 Seeding occasions...');
  const occasionData = [
    { name: 'Birthday', slug: 'birthday', description: 'Celebrate another fabulous year with sweet surprises', bannerUrl: 'https://images.unsplash.com/photo-1530103862676-de8c9debad1d?w=800', sortOrder: 1 },
    { name: 'Anniversary', slug: 'anniversary', description: 'Romantic flowers, cakes and heartfelt gifts', bannerUrl: 'https://images.unsplash.com/photo-1518199266791-5375a83190b7?w=800', sortOrder: 2 },
    { name: 'Love & Romance', slug: 'love-romance', description: 'Express your deepest feelings with passionate red roses', bannerUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800', sortOrder: 3 },
    { name: 'Congratulations', slug: 'congratulations', description: 'Salute milestone achievements and new beginnings', bannerUrl: 'https://images.unsplash.com/photo-1513151233558-d860c5398176?w=800', sortOrder: 4 },
    { name: 'Housewarming', slug: 'housewarming', description: 'Lush greenery and cozy home decor gifts', bannerUrl: 'https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800', sortOrder: 5 },
  ];

  const occasionMap = new Map<string, string>();
  for (const occ of occasionData) {
    const created = await prisma.occasion.upsert({
      where: { slug: occ.slug },
      update: occ,
      create: occ,
    });
    occasionMap.set(occ.slug, created.id);
  }

  // 8. Seed Banners
  console.log('🖼️ Seeding banners...');
  const bannerData = [
    { title: 'Celebrate Every Moment', subtitle: 'Same-day flower & cake delivery across 500+ cities', imageUrl: 'https://images.unsplash.com/photo-1526047932273-341f2a7631f9?w=1200', deepLink: 'wishify://category/flowers', position: 'HOME_TOP', sortOrder: 1 },
    { title: 'Midnight Magic Deliveries', subtitle: 'Surprise them at 12:00 AM with fresh cakes & roses', imageUrl: 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=1200', deepLink: 'wishify://category/cakes', position: 'HOME_TOP', sortOrder: 2 },
    { title: 'Under ₹999 Budget Gifting', subtitle: 'Thoughtful surprises that don’t pinch your pocket', imageUrl: 'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?w=1200', deepLink: 'wishify://products?maxPrice=999', position: 'HOME_MIDDLE', sortOrder: 3 },
  ];

  for (const banner of bannerData) {
    const existing = await prisma.banner.findFirst({ where: { title: banner.title } });
    if (!existing) {
      await prisma.banner.create({ data: banner });
    }
  }

  // 9. Seed Add-Ons
  console.log('🎁 Seeding add-ons...');
  const addOnData = [
    { title: 'Happy Birthday Greeting Card', description: 'Personalized printed note inside card', price: 99.0, imageUrl: 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=300' },
    { title: 'Anniversary Special Card', description: 'Gold embossed greeting card', price: 99.0, imageUrl: 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=300' },
    { title: 'Party Popper Confetti', description: 'Fun handheld party blaster', price: 149.0, imageUrl: 'https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=300' },
    { title: 'Box of 4 Scented Candles', description: 'Lavender and Vanilla aromatic candles', price: 199.0, imageUrl: 'https://images.unsplash.com/photo-1603006905003-be475563bc59?w=300' },
    { title: 'Cute Teddy Bear (6 inch)', description: 'Soft plush cuddly bear', price: 299.0, imageUrl: 'https://images.unsplash.com/photo-1559454403-b8fb88521f11?w=300' },
    { title: 'Ferrero Rocher Box (4 pcs)', description: 'Crisp hazelnut milk chocolates', price: 199.0, imageUrl: 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=300' },
  ];

  const addOnIds: string[] = [];
  for (const addOn of addOnData) {
    const existing = await prisma.addOn.findFirst({ where: { title: addOn.title } });
    if (existing) {
      addOnIds.push(existing.id);
    } else {
      const created = await prisma.addOn.create({ data: addOn });
      addOnIds.push(created.id);
    }
  }

  // 10. Seed 30+ Realistic Products
  console.log('🛍️ Seeding 30+ products with variants and images...');
  const products = [
    // FLOWERS (1-6)
    {
      title: 'Romantic Red Roses Bouquet',
      slug: 'romantic-red-roses-bouquet',
      description: 'A breathtaking bunch of handpicked fresh velvety red roses wrapped in premium matte paper with a satin ribbon bow.',
      categorySlug: 'flowers',
      occasionSlug: 'love-romance',
      basePrice: 599,
      compareAtPrice: 799,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '10 Red Roses', price: 599, compareAtPrice: 799, sku: 'FLW-ROSE-10', stockQuantity: 150, weightOrSize: '10 Stems', isDefault: true },
        { title: '20 Red Roses', price: 999, compareAtPrice: 1299, sku: 'FLW-ROSE-20', stockQuantity: 100, weightOrSize: '20 Stems', isDefault: false },
        { title: '50 Grand Roses', price: 2299, compareAtPrice: 2899, sku: 'FLW-ROSE-50', stockQuantity: 50, weightOrSize: '50 Stems', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1561181286-d3fee7d55364?w=800', isPrimary: true },
        { imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800', isPrimary: false },
      ],
    },
    {
      title: 'Elegance Purple Orchids Bunch',
      slug: 'elegance-purple-orchids-bunch',
      description: 'Stunning exotic purple dendrobium orchids arranged delicately with seasonal fillers in blue craft paper.',
      categorySlug: 'flowers',
      occasionSlug: 'congratulations',
      basePrice: 799,
      compareAtPrice: 999,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '6 Orchids Bunch', price: 799, compareAtPrice: 999, sku: 'FLW-ORC-06', stockQuantity: 80, weightOrSize: '6 Stems', isDefault: true },
        { title: '10 Orchids Luxury', price: 1199, compareAtPrice: 1499, sku: 'FLW-ORC-10', stockQuantity: 60, weightOrSize: '10 Stems', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1526047932273-341f2a7631f9?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Sunshine Yellow Carnations & Lilies',
      slug: 'sunshine-yellow-carnations-lilies',
      description: 'Bright cheerful yellow carnations paired with pristine white oriental lilies to brighten anyone’s day.',
      categorySlug: 'flowers',
      occasionSlug: 'birthday',
      basePrice: 899,
      compareAtPrice: 1199,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: 'Standard Bouquet', price: 899, compareAtPrice: 1199, sku: 'FLW-CAR-LIL-STD', stockQuantity: 90, weightOrSize: 'Standard', isDefault: true },
        { title: 'Deluxe Glass Vase', price: 1399, compareAtPrice: 1699, sku: 'FLW-CAR-LIL-VASE', stockQuantity: 40, weightOrSize: 'Vase Arrangement', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1490750967868-88aa4486c946?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Pastel Pink Gerberas & Roses Basket',
      slug: 'pastel-pink-gerberas-roses-basket',
      description: 'A charming woven wicker basket brimming with soft baby pink roses, sweet gerberas, and gypsophila.',
      categorySlug: 'flowers',
      occasionSlug: 'anniversary',
      basePrice: 999,
      compareAtPrice: 1299,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: 'Classic Basket', price: 999, compareAtPrice: 1299, sku: 'FLW-BSK-CLS', stockQuantity: 75, weightOrSize: 'Classic', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1508610048659-a06b669e3321?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Serene White Asiatic Lilies',
      slug: 'serene-white-asiatic-lilies',
      description: 'Fragrant, pure white Asiatic lilies wrapped in eco-friendly jute packaging.',
      categorySlug: 'flowers',
      occasionSlug: 'housewarming',
      basePrice: 1099,
      compareAtPrice: 1399,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: false,
      variants: [
        { title: '4 Stems (8+ Blooms)', price: 1099, compareAtPrice: 1399, sku: 'FLW-LIL-04', stockQuantity: 60, weightOrSize: '4 Stems', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1525310072745-f49212b5ac6d?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Heart-Shaped Red Roses Box',
      slug: 'heart-shaped-red-roses-box',
      description: 'Premium handcrafted velvet heart box filled with 30 Dutch red roses with dew drops.',
      categorySlug: 'flowers',
      occasionSlug: 'love-romance',
      basePrice: 1899,
      compareAtPrice: 2499,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: 'Heart Box (30 Roses)', price: 1899, compareAtPrice: 2499, sku: 'FLW-HRT-30', stockQuantity: 40, weightOrSize: '30 Roses Box', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800', isPrimary: true },
      ],
    },

    // CAKES (7-12)
    {
      title: 'Classic German Black Forest Cake',
      slug: 'classic-german-black-forest-cake',
      description: 'Layers of moist chocolate sponge, whipped vanilla cream, sweet cherries, and dark chocolate curls.',
      categorySlug: 'cakes',
      occasionSlug: 'birthday',
      basePrice: 549,
      compareAtPrice: 699,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on cake (max 25 characters)',
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '0.5 kg (Eggless)', price: 549, compareAtPrice: 699, sku: 'CAK-BF-500G', stockQuantity: 200, weightOrSize: '0.5 kg', isDefault: true },
        { title: '1.0 kg (Eggless)', price: 999, compareAtPrice: 1299, sku: 'CAK-BF-1KG', stockQuantity: 150, weightOrSize: '1.0 kg', isDefault: false },
        { title: '2.0 kg (Eggless)', price: 1899, compareAtPrice: 2399, sku: 'CAK-BF-2KG', stockQuantity: 50, weightOrSize: '2.0 kg', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1578985545062-69928b1d9587?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Velvety Red Velvet Cream Cheese Cake',
      slug: 'velvety-red-velvet-cream-cheese-cake',
      description: 'Rich cocoa red velvet sponge frosted with authentic Philadelphia cream cheese frosting.',
      categorySlug: 'cakes',
      occasionSlug: 'anniversary',
      basePrice: 699,
      compareAtPrice: 899,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on cake (max 25 characters)',
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '0.5 kg', price: 699, compareAtPrice: 899, sku: 'CAK-RV-500G', stockQuantity: 120, weightOrSize: '0.5 kg', isDefault: true },
        { title: '1.0 kg', price: 1299, compareAtPrice: 1599, sku: 'CAK-RV-1KG', stockQuantity: 80, weightOrSize: '1.0 kg', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Decadent Belgian Chocolate Truffle Cake',
      slug: 'decadent-belgian-chocolate-truffle-cake',
      description: 'Dense dark chocolate cake filled with smooth Belgian ganache and finished with a glossy glaze.',
      categorySlug: 'cakes',
      occasionSlug: 'birthday',
      basePrice: 649,
      compareAtPrice: 799,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on cake (max 25 characters)',
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '0.5 kg', price: 649, compareAtPrice: 799, sku: 'CAK-TRUF-500G', stockQuantity: 180, weightOrSize: '0.5 kg', isDefault: true },
        { title: '1.0 kg', price: 1199, compareAtPrice: 1499, sku: 'CAK-TRUF-1KG', stockQuantity: 100, weightOrSize: '1.0 kg', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1606313564200-e75d5e30476c?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Tropical Fresh Fruit Cake',
      slug: 'tropical-fresh-fruit-cake',
      description: 'Vanilla sponge infused with exotic fruit crush and topped with kiwi, dragonfruit, strawberries, and oranges.',
      categorySlug: 'cakes',
      occasionSlug: 'congratulations',
      basePrice: 649,
      compareAtPrice: 849,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on cake (max 25 characters)',
      isSameDayEligible: true,
      isMidnightEligible: false,
      variants: [
        { title: '0.5 kg', price: 649, compareAtPrice: 849, sku: 'CAK-FRT-500G', stockQuantity: 90, weightOrSize: '0.5 kg', isDefault: true },
        { title: '1.0 kg', price: 1199, compareAtPrice: 1499, sku: 'CAK-FRT-1KG', stockQuantity: 60, weightOrSize: '1.0 kg', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1565958011703-44f9829ba187?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Butterscotch Crunch Celebration Cake',
      slug: 'butterscotch-crunch-celebration-cake',
      description: 'Golden sponge layered with creamy butterscotch cream and praline nougat crunchies.',
      categorySlug: 'cakes',
      occasionSlug: 'birthday',
      basePrice: 499,
      compareAtPrice: 649,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on cake (max 25 characters)',
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '0.5 kg', price: 499, compareAtPrice: 649, sku: 'CAK-BS-500G', stockQuantity: 210, weightOrSize: '0.5 kg', isDefault: true },
        { title: '1.0 kg', price: 899, compareAtPrice: 1149, sku: 'CAK-BS-1KG', stockQuantity: 120, weightOrSize: '1.0 kg', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1535141192574-5d4897c13136?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Heart-Shaped Ferrero Rocher Cake',
      slug: 'heart-shaped-ferrero-rocher-cake',
      description: 'Artisanal heart cake topped with whole Ferrero Rocher chocolates, toasted hazelnuts and Nutella ganache.',
      categorySlug: 'cakes',
      occasionSlug: 'love-romance',
      basePrice: 899,
      compareAtPrice: 1199,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on cake (max 25 characters)',
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '0.5 kg', price: 899, compareAtPrice: 1199, sku: 'CAK-FR-500G', stockQuantity: 70, weightOrSize: '0.5 kg', isDefault: true },
        { title: '1.0 kg', price: 1599, compareAtPrice: 1999, sku: 'CAK-FR-1KG', stockQuantity: 45, weightOrSize: '1.0 kg', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=800', isPrimary: true },
      ],
    },

    // PLANTS (13-17)
    {
      title: 'Air Purifying Sansevieria Snake Plant',
      slug: 'air-purifying-sansevieria-snake-plant',
      description: 'Hardy oxygen-releasing indoor snake plant in a modern matte ceramic planter.',
      categorySlug: 'plants',
      occasionSlug: 'housewarming',
      basePrice: 499,
      compareAtPrice: 699,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: false,
      variants: [
        { title: 'White Ceramic Pot', price: 499, compareAtPrice: 699, sku: 'PLN-SNK-WHT', stockQuantity: 110, weightOrSize: 'Medium (10-12 inch)', isDefault: true },
        { title: 'Brass Finish Pot', price: 799, compareAtPrice: 999, sku: 'PLN-SNK-BRS', stockQuantity: 50, weightOrSize: 'Large (14-16 inch)', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1509423350716-97f9360b4e09?w=800', isPrimary: true },
      ],
    },
    {
      title: '2-Layer Lucky Bamboo Plant in Glass Bowl',
      slug: '2-layer-lucky-bamboo-plant-glass-bowl',
      description: 'Feng shui symbol of prosperity and good fortune with colorful jelly stones in a crystal glass bowl.',
      categorySlug: 'plants',
      occasionSlug: 'congratulations',
      basePrice: 399,
      compareAtPrice: 499,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: false,
      variants: [
        { title: '2-Layer Bamboo', price: 399, compareAtPrice: 499, sku: 'PLN-BAM-2L', stockQuantity: 250, weightOrSize: '6-8 inch', isDefault: true },
        { title: '3-Layer Bamboo', price: 599, compareAtPrice: 749, sku: 'PLN-BAM-3L', stockQuantity: 130, weightOrSize: '10-12 inch', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Ficus Bonsai in Ceramic Tray',
      slug: 'ficus-bonsai-in-ceramic-tray',
      description: 'Exquisite 5-year-old miniature Ficus bonsai tree representing patience, strength, and harmony.',
      categorySlug: 'plants',
      occasionSlug: 'housewarming',
      basePrice: 1299,
      compareAtPrice: 1699,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: false,
      variants: [
        { title: 'Standard Tray', price: 1299, compareAtPrice: 1699, sku: 'PLN-BON-5YR', stockQuantity: 40, weightOrSize: '12-14 inch', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1512428813834-c702c7702b78?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Peace Lily Plant in Self-Watering Pot',
      slug: 'peace-lily-plant-self-watering-pot',
      description: 'Glossy dark green leaves with graceful white spathes that purify indoor air toxins.',
      categorySlug: 'plants',
      occasionSlug: 'housewarming',
      basePrice: 599,
      compareAtPrice: 799,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: false,
      variants: [
        { title: 'Self-Watering Pot', price: 599, compareAtPrice: 799, sku: 'PLN-LIL-SWP', stockQuantity: 85, weightOrSize: 'Medium', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1459411552884-841db9b3cc2a?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Jade Good Luck Succulent in Marble Pot',
      slug: 'jade-good-luck-succulent-marble-pot',
      description: 'Crassula Ovata money plant renowned for attracting wealth and positive energy.',
      categorySlug: 'plants',
      occasionSlug: 'congratulations',
      basePrice: 449,
      compareAtPrice: 599,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: false,
      variants: [
        { title: 'Marble Print Pot', price: 449, compareAtPrice: 599, sku: 'PLN-JAD-MBL', stockQuantity: 140, weightOrSize: 'Compact', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1463936575829-25148e1db1b8?w=800', isPrimary: true },
      ],
    },

    // CHOCOLATES (18-22)
    {
      title: 'Ferrero Rocher Golden Diamond Gift Box',
      slug: 'ferrero-rocher-golden-diamond-gift-box',
      description: 'Grand assortment of whole crunchy hazelnut wrapped in rich milk chocolate and crispy wafer.',
      categorySlug: 'chocolates',
      occasionSlug: 'birthday',
      basePrice: 849,
      compareAtPrice: 999,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '16 Pieces Box', price: 849, compareAtPrice: 999, sku: 'CHOC-FR-16', stockQuantity: 180, weightOrSize: '200g (16 Pcs)', isDefault: true },
        { title: '24 Pieces Box', price: 1299, compareAtPrice: 1499, sku: 'CHOC-FR-24', stockQuantity: 100, weightOrSize: '300g (24 Pcs)', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Artisanal Dark Chocolate Truffles Hamper',
      slug: 'artisanal-dark-chocolate-truffles-hamper',
      description: 'Handcrafted luxury chocolate truffles infused with hazelnut praline, sea salt caramel, and raspberry ganache.',
      categorySlug: 'chocolates',
      occasionSlug: 'love-romance',
      basePrice: 699,
      compareAtPrice: 899,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '9 Gourmet Truffles', price: 699, compareAtPrice: 899, sku: 'CHOC-TRUF-09', stockQuantity: 95, weightOrSize: '9 Pcs', isDefault: true },
        { title: '16 Gourmet Truffles', price: 1149, compareAtPrice: 1399, sku: 'CHOC-TRUF-16', stockQuantity: 60, weightOrSize: '16 Pcs', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1548741487-18d363dc4469?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Cadbury Silk Indulgence Bouquet',
      slug: 'cadbury-silk-indulgence-bouquet',
      description: 'Creative floral-style arrangement of Dairy Milk Silk bars wrapped in elegant gold foil and ribbon.',
      categorySlug: 'chocolates',
      occasionSlug: 'birthday',
      basePrice: 799,
      compareAtPrice: 999,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '5 Silk Bars Bouquet', price: 799, compareAtPrice: 999, sku: 'CHOC-SLK-05', stockQuantity: 130, weightOrSize: '5 Bars', isDefault: true },
        { title: '8 Silk Bars Grand Bouquet', price: 1249, compareAtPrice: 1549, sku: 'CHOC-SLK-08', stockQuantity: 70, weightOrSize: '8 Bars', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1549007994-cb92caebd54b?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Belgian Chocolate Dipped Strawberries Box',
      slug: 'belgian-chocolate-dipped-strawberries-box',
      description: 'Fresh farm strawberries dipped in dark, milk, and white Belgian chocolate with nutty sprinkles.',
      categorySlug: 'chocolates',
      occasionSlug: 'anniversary',
      basePrice: 999,
      compareAtPrice: 1299,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '12 Dipped Berries', price: 999, compareAtPrice: 1299, sku: 'CHOC-STRW-12', stockQuantity: 50, weightOrSize: '12 Pcs', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1514517521153-1be72277b32f?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Assorted Dry Fruits & Dark Chocolate Wooden Box',
      slug: 'assorted-dry-fruits-dark-chocolate-wooden-box',
      description: 'Royal carved wooden chest featuring California almonds, cashews, raisins, and 70% cocoa chocolate bars.',
      categorySlug: 'chocolates',
      occasionSlug: 'housewarming',
      basePrice: 1499,
      compareAtPrice: 1899,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: false,
      isMidnightEligible: false,
      variants: [
        { title: '500g Royal Box', price: 1499, compareAtPrice: 1899, sku: 'CHOC-DF-500G', stockQuantity: 65, weightOrSize: '500g', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1509440159596-0249088772ff?w=800', isPrimary: true },
      ],
    },

    // PERSONALISED GIFTS (23-27)
    {
      title: 'Custom Photo Magic Mug (Heat Sensitive)',
      slug: 'custom-photo-magic-mug',
      description: 'Black ceramic mug that reveals your hidden personalized photo when filled with hot coffee or tea.',
      categorySlug: 'personalised-gifts',
      occasionSlug: 'birthday',
      basePrice: 449,
      compareAtPrice: 599,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Upload your photo / Enter name to print',
      isSameDayEligible: false,
      isMidnightEligible: false,
      variants: [
        { title: '325ml Magic Mug', price: 449, compareAtPrice: 599, sku: 'PER-MUG-MAG', stockQuantity: 300, weightOrSize: '325ml', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800', isPrimary: true },
      ],
    },
    {
      title: 'LED Illuminated Acrylic Spotify Plaque',
      slug: 'led-illuminated-acrylic-spotify-plaque',
      description: 'Custom scannable Spotify code with your favorite song and personalized photo on crystal acrylic with wooden LED base.',
      categorySlug: 'personalised-gifts',
      occasionSlug: 'love-romance',
      basePrice: 899,
      compareAtPrice: 1299,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Song title & Artist + Couple Name',
      isSameDayEligible: false,
      isMidnightEligible: false,
      variants: [
        { title: 'Warm White LED Base', price: 899, compareAtPrice: 1299, sku: 'PER-SPT-WHT', stockQuantity: 150, weightOrSize: '6x8 inch', isDefault: true },
        { title: 'RGB Multi-Color LED', price: 1099, compareAtPrice: 1499, sku: 'PER-SPT-RGB', stockQuantity: 100, weightOrSize: '6x8 inch', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Personalized Engraved Wooden Keepsake Clock',
      slug: 'personalized-engraved-wooden-keepsake-clock',
      description: 'Natural pinewood desk clock laser engraved with names, anniversary date, and sweet wishes.',
      categorySlug: 'personalised-gifts',
      occasionSlug: 'anniversary',
      basePrice: 999,
      compareAtPrice: 1399,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Names & Special Date',
      isSameDayEligible: false,
      isMidnightEligible: false,
      variants: [
        { title: 'Round Wood Clock', price: 999, compareAtPrice: 1399, sku: 'PER-CLK-RND', stockQuantity: 80, weightOrSize: '8 inch diameter', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1508057198894-247b23fe5ade?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Custom Sequin Magic Photo Cushion',
      slug: 'custom-sequin-magic-photo-cushion',
      description: 'Sparkling reversible sequin pillow that reveals your photo when brushed with your hand.',
      categorySlug: 'personalised-gifts',
      occasionSlug: 'birthday',
      basePrice: 599,
      compareAtPrice: 799,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Name or photo text for cushion',
      isSameDayEligible: false,
      isMidnightEligible: false,
      variants: [
        { title: 'Golden Sequin 16x16"', price: 599, compareAtPrice: 799, sku: 'PER-CSH-GLD', stockQuantity: 140, weightOrSize: '16x16 inch', isDefault: true },
        { title: 'Red Sequin 16x16"', price: 599, compareAtPrice: 799, sku: 'PER-CSH-RED', stockQuantity: 120, weightOrSize: '16x16 inch', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1584100936595-c0654b55a2e2?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Engraved Leather Wallet & Keychain Combo',
      slug: 'engraved-leather-wallet-keychain-combo',
      description: 'Top-grain cruelty-free leather bifold wallet with personalized name charm and metallic pen in a gift box.',
      categorySlug: 'personalised-gifts',
      occasionSlug: 'congratulations',
      basePrice: 799,
      compareAtPrice: 1099,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Name to be embossed (max 12 chars)',
      isSameDayEligible: false,
      isMidnightEligible: false,
      variants: [
        { title: 'Vintage Tan Brown', price: 799, compareAtPrice: 1099, sku: 'PER-WLT-TAN', stockQuantity: 160, weightOrSize: 'Combo Box', isDefault: true },
        { title: 'Classic Jet Black', price: 799, compareAtPrice: 1099, sku: 'PER-WLT-BLK', stockQuantity: 150, weightOrSize: 'Combo Box', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1627123424574-724758594e93?w=800', isPrimary: true },
      ],
    },

    // COMBOS (28-32)
    {
      title: 'Roses & Truffle Cake Celebration Combo',
      slug: 'roses-truffle-cake-celebration-combo',
      description: 'Best-selling gift combo: 10 fresh Dutch red roses bouquet paired with a 0.5 kg dark chocolate truffle cake.',
      categorySlug: 'combos',
      occasionSlug: 'birthday',
      basePrice: 1099,
      compareAtPrice: 1499,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on cake (max 25 characters)',
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: '10 Roses + 0.5kg Cake', price: 1099, compareAtPrice: 1499, sku: 'CMB-RS-TRF-STD', stockQuantity: 120, weightOrSize: 'Standard Combo', isDefault: true },
        { title: '20 Roses + 1.0kg Cake', price: 1899, compareAtPrice: 2499, sku: 'CMB-RS-TRF-DLX', stockQuantity: 70, weightOrSize: 'Deluxe Combo', isDefault: false },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1513151233558-d860c5398176?w=800', isPrimary: true },
        { imageUrl: 'https://images.unsplash.com/photo-1561181286-d3fee7d55364?w=800', isPrimary: false },
      ],
    },
    {
      title: 'Orchids, Black Forest & Teddy Grand Hamper',
      slug: 'orchids-black-forest-teddy-grand-hamper',
      description: 'Exquisite 6 purple orchids bunch, 0.5 kg eggless black forest cake, and a cute 6-inch plush teddy bear.',
      categorySlug: 'combos',
      occasionSlug: 'birthday',
      basePrice: 1499,
      compareAtPrice: 1999,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on cake (max 25 characters)',
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: 'Grand 3-in-1 Hamper', price: 1499, compareAtPrice: 1999, sku: 'CMB-ORC-BF-TED', stockQuantity: 80, weightOrSize: 'Grand Hamper', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1513151233558-d860c5398176?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Romantic Heart Red Roses & Ferrero Rocher Combo',
      slug: 'romantic-heart-red-roses-ferrero-rocher-combo',
      description: 'A heart-melting combination of 15 red roses bunch and a 16-piece box of Ferrero Rocher chocolates.',
      categorySlug: 'combos',
      occasionSlug: 'love-romance',
      basePrice: 1399,
      compareAtPrice: 1799,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: 'Roses + 16 Ferrero Box', price: 1399, compareAtPrice: 1799, sku: 'CMB-RS-FR-16', stockQuantity: 90, weightOrSize: 'Combo', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Lucky Bamboo & Cadbury Celebration Hamper',
      slug: 'lucky-bamboo-cadbury-celebration-hamper',
      description: 'Lush 2-layer lucky bamboo in glass bowl bundled with a Cadbury Celebrations gift box.',
      categorySlug: 'combos',
      occasionSlug: 'congratulations',
      basePrice: 699,
      compareAtPrice: 899,
      isVegetarian: true,
      isPersonalized: false,
      isSameDayEligible: true,
      isMidnightEligible: false,
      variants: [
        { title: 'Bamboo + Cadbury Box', price: 699, compareAtPrice: 899, sku: 'CMB-BAM-CAD', stockQuantity: 110, weightOrSize: 'Gift Set', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1485955900006-10f4d324d411?w=800', isPrimary: true },
      ],
    },
    {
      title: 'Midnight Surprise: Roses, Red Velvet & Photo Card',
      slug: 'midnight-surprise-roses-red-velvet-photo-card',
      description: 'The ultimate midnight surprise pack: 12 red roses, 0.5 kg red velvet cake, and a customized greeting card.',
      categorySlug: 'combos',
      occasionSlug: 'anniversary',
      basePrice: 1349,
      compareAtPrice: 1749,
      isVegetarian: true,
      isPersonalized: true,
      personalizationPrompt: 'Message on card & cake',
      isSameDayEligible: true,
      isMidnightEligible: true,
      variants: [
        { title: 'Midnight Surprise Pack', price: 1349, compareAtPrice: 1749, sku: 'CMB-MID-SPL', stockQuantity: 65, weightOrSize: 'Special Hamper', isDefault: true },
      ],
      images: [
        { imageUrl: 'https://images.unsplash.com/photo-1513151233558-d860c5398176?w=800', isPrimary: true },
      ],
    },
  ];

  for (const prod of products) {
    const categoryId = categoryMap.get(prod.categorySlug)!;
    const occasionId = prod.occasionSlug ? occasionMap.get(prod.occasionSlug) : null;

    const existingProduct = await prisma.product.findUnique({
      where: { slug: prod.slug },
    });

    if (!existingProduct) {
      const createdProduct = await prisma.product.create({
        data: {
          title: prod.title,
          slug: prod.slug,
          description: prod.description,
          categoryId: categoryId,
          occasionId: occasionId,
          basePrice: prod.basePrice,
          compareAtPrice: prod.compareAtPrice,
          isVegetarian: prod.isVegetarian,
          isPersonalized: prod.isPersonalized,
          personalizationPrompt: prod.personalizationPrompt || null,
          isSameDayEligible: prod.isSameDayEligible,
          isMidnightEligible: prod.isMidnightEligible,
          averageRating: 4.8,
          reviewCount: 24,
          variants: {
            create: prod.variants.map((v) => ({
              title: v.title,
              price: v.price,
              compareAtPrice: v.compareAtPrice,
              sku: v.sku,
              stockQuantity: v.stockQuantity,
              weightOrSize: v.weightOrSize,
              isDefault: v.isDefault,
            })),
          },
          images: {
            create: prod.images.map((img, idx) => ({
              imageUrl: img.imageUrl,
              isPrimary: img.isPrimary,
              sortOrder: idx,
            })),
          },
          addOns: {
            connect: addOnIds.map((id) => ({ id })),
          },
        },
      });

      // Add a couple of initial approved reviews
      await prisma.review.create({
        data: {
          productId: createdProduct.id,
          userId: customer.id,
          rating: 5,
          comment: 'Absolutely stunning quality! Arrived right on time and made the celebration extra special.',
          status: 'APPROVED',
        },
      });
    }
  }

  console.log('✅ Seeding completed successfully!');
}

main()
  .catch((e) => {
    console.error('❌ Seeding failed:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
