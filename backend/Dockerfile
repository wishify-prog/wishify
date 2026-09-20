FROM node:20-slim AS builder

WORKDIR /app

RUN apt-get update -y && apt-get install -y openssl

COPY package*.json ./
COPY prisma ./prisma/

RUN npm ci

COPY tsconfig.json ./
COPY src ./src

RUN npx prisma generate
RUN npm run build
RUN npx tsc prisma/seed.ts --outDir dist --target ES2022 --module commonjs --moduleResolution node --esModuleInterop --skipLibCheck || true

FROM node:20-slim AS runner

WORKDIR /app

RUN apt-get update -y && apt-get install -y openssl

ENV NODE_ENV=production

COPY package*.json ./
RUN npm ci --only=production

COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma
COPY --from=builder /app/node_modules/@prisma ./node_modules/@prisma
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/prisma ./prisma
COPY swagger.json ./

EXPOSE 4000

CMD ["sh", "-c", "npx prisma db push --accept-data-loss && (node dist/seed.js || true) && node dist/server.js"]
