import { EventEmitter } from 'events';
import { Response } from 'express';

export type RealtimeEventType =
  | 'CATALOG_UPDATED'
  | 'BANNER_UPDATED'
  | 'PRODUCT_UPDATED'
  | 'ORDER_UPDATED'
  | 'COUPON_UPDATED'
  | 'SLOT_UPDATED'
  | 'PINCODE_UPDATED';

export interface RealtimeEvent {
  type: RealtimeEventType;
  data?: any;
  timestamp: string;
}

class EventBusService {
  private emitter = new EventEmitter();
  private clients: Set<Response> = new Set();

  constructor() {
    this.emitter.setMaxListeners(200);
  }

  public registerClient(res: Response) {
    this.clients.add(res);

    // Send initial connected event
    const initialPayload: RealtimeEvent = {
      type: 'CATALOG_UPDATED',
      data: { connected: true },
      timestamp: new Date().toISOString(),
    };
    res.write(`data: ${JSON.stringify(initialPayload)}\n\n`);

    res.on('close', () => {
      this.clients.delete(res);
    });
  }

  public broadcast(type: RealtimeEventType, data: any = {}) {
    const event: RealtimeEvent = {
      type,
      data,
      timestamp: new Date().toISOString(),
    };

    const payload = `data: ${JSON.stringify(event)}\n\n`;
    for (const client of this.clients) {
      try {
        client.write(payload);
      } catch (err) {
        this.clients.delete(client);
      }
    }
  }

  public getClientCount(): number {
    return this.clients.size;
  }
}

export const eventBus = new EventBusService();
