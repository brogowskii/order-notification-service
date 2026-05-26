import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const requestRate = Number(__ENV.REQUEST_RATE || '20');
const duration = __ENV.DURATION || '1m';

export const errorRate = new Rate('order_intake_errors');

export const options = {
  scenarios: {
    order_intake: {
      executor: 'constant-arrival-rate',
      rate: requestRate,
      timeUnit: '1s',
      duration,
      preAllocatedVUs: Math.max(10, requestRate),
      maxVUs: Math.max(50, requestRate * 2),
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
    order_intake_errors: ['rate<0.01'],
  },
};

export default function () {
  const uniqueId = `${__VU}-${__ITER}-${Date.now()}`;
  const payload = JSON.stringify({
    shipmentNumber: `PL${uniqueId}`,
    recipientEmail: `recipient-${uniqueId}@example.com`,
    recipientCountryCode: 'PL',
    senderCountryCode: 'DE',
    statusCode: 42,
  });

  const response = http.post(`${baseUrl}/api/v1/orders`, payload, {
    headers: {
      'Content-Type': 'application/json',
    },
  });

  const accepted = check(response, {
    'order accepted': (res) => res.status === 202,
    'request id returned': (res) => Boolean(res.json('requestId')),
  });

  errorRate.add(!accepted);
  sleep(0.1);
}
