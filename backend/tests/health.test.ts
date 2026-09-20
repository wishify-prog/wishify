import request from 'supertest';
import { createApp } from '../src/app';

const app = createApp();

describe('Health & Documentation Endpoints', () => {
  it('GET /health should return 200 with status UP', async () => {
    const res = await request(app).get('/health');
    expect(res.status).toBe(200);
    expect(res.body.status).toBe('UP');
    expect(res.body.service).toBe('wishify-backend');
  });

  it('GET /docs should serve Swagger documentation', async () => {
    const res = await request(app).get('/docs/');
    expect([200, 301, 302]).toContain(res.status);
  });
});
