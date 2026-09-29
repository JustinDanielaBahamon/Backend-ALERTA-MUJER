import express from 'express';
import cors from 'cors';
import { createProxyMiddleware } from 'http-proxy-middleware';

const app = express();
const PORT = process.env.PORT || 8080;
const CORS_ORIGIN = process.env.CORS_ORIGIN || '*';

app.use(cors({ origin: CORS_ORIGIN === '*' ? true : CORS_ORIGIN.split(',') }));

const IDENTITY_URL = process.env.IDENTITY_SERVICE_URL || 'http://localhost:3001';
const ALERT_URL = process.env.ALERT_SERVICE_URL || 'http://localhost:3002';
const RESOURCE_URL = process.env.RESOURCE_SERVICE_URL || 'http://localhost:3003';
const ADMIN_URL = process.env.ADMIN_SERVICE_URL || 'http://localhost:3004';

app.use('/api/identity', createProxyMiddleware({ target: IDENTITY_URL, changeOrigin: true, pathRewrite: { '^/api/identity': '' } }));
app.use('/api/alerts', createProxyMiddleware({ target: ALERT_URL, changeOrigin: true, pathRewrite: { '^/api/alerts': '' } }));
app.use('/api/resources', createProxyMiddleware({ target: RESOURCE_URL, changeOrigin: true, pathRewrite: { '^/api/resources': '' } }));
app.use('/api/admin', createProxyMiddleware({ target: ADMIN_URL, changeOrigin: true, pathRewrite: { '^/api/admin': '' } }));

app.get('/health', (_req, res) => {
  res.json({ status: 'ok', service: 'gateway' });
});

app.listen(PORT, () => {
  console.log(`API Gateway corriendo en puerto ${PORT}`);
});
