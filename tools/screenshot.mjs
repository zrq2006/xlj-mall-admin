import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(HERE, 'screenshots');
fs.mkdirSync(OUT, { recursive: true });

const EDGE = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const BASE = process.env.BASE_URL || 'http://127.0.0.1:8080';

const browser = await puppeteer.launch({
  executablePath: EDGE,
  headless: 'new',
  args: ['--no-sandbox', '--disable-gpu', '--hide-scrollbars', '--force-device-scale-factor=1'],
  defaultViewport: { width: 1440, height: 900, deviceScaleFactor: 1 },
});

const page = await browser.newPage();
const failed = [];
page.on('console', (msg) => {
  if (msg.type() === 'error') console.log('[console.error]', msg.text());
});
page.on('pageerror', (err) => console.log('[pageerror]', err.message));
page.on('requestfailed', (req) => failed.push(`${req.url()} -> ${req.failure()?.errorText}`));

async function shot(url, file, waitFor) {
  await page.goto(BASE + url, { waitUntil: 'networkidle0', timeout: 30000 });
  if (waitFor) await page.waitForSelector(waitFor, { timeout: 15000 });
  await new Promise((r) => setTimeout(r, 700));
  await page.screenshot({ path: path.join(OUT, file), fullPage: false });
  console.log('saved', file);
}

await shot('/login', '00-login.png', '.login-card');
await page.click('.login-button');
await page.waitForSelector('.stat-card', { timeout: 15000 });
await new Promise((r) => setTimeout(r, 3600));
await page.screenshot({ path: path.join(OUT, '01-workbench.png') });
console.log('saved 01-workbench.png');

await shot('/products', '02-products.png', '.el-table__row');
await shot('/categories', '03-categories.png', '.el-table__row');
await shot('/brands', '04-brands.png', '.el-table__row');

console.log('failed requests:', failed.length ? failed : 'none');
await browser.close();
