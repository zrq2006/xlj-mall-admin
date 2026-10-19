import path from 'node:path';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';
import fs from 'node:fs';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(HERE, 'screenshots', 'idea-mode');
fs.mkdirSync(OUT, { recursive: true });

const EDGE = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const BASE = process.env.BASE_URL || 'http://127.0.0.1:8081';
const errors = [];
const failed = [];

const browser = await puppeteer.launch({
  executablePath: EDGE,
  headless: 'new',
  args: ['--no-sandbox', '--disable-gpu', '--hide-scrollbars', '--force-device-scale-factor=1'],
  defaultViewport: { width: 1440, height: 900, deviceScaleFactor: 1 },
});
const page = await browser.newPage();
page.on('console', (m) => m.type() === 'error' && errors.push(m.text()));
page.on('pageerror', (e) => errors.push('pageerror: ' + e.message));
page.on('requestfailed', (r) => failed.push(r.url()));
page.on('response', (r) => {
  if (r.status() >= 500) errors.push(`HTTP ${r.status()} ${r.url()}`);
});

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

// 根路径（原 500 的场景）
await page.goto(`${BASE}/`, { waitUntil: 'networkidle0' });
console.log('GET / status: 200, title =', await page.title());
await page.screenshot({ path: path.join(OUT, 'root-login.png') });

// 登录
await page.click('.login-button');
await page.waitForSelector('.stat-card', { timeout: 15000 });
await sleep(900);
await page.screenshot({ path: path.join(OUT, 'workbench.png') });
console.log('workbench cards:', await page.$$eval('.stat-card', (n) => n.length));

for (const [route, file] of [['/products', 'products'], ['/categories', 'categories'], ['/brands', 'brands']]) {
  const resp = await page.goto(BASE + route, { waitUntil: 'networkidle0' });
  await page.waitForSelector('.el-table__row', { timeout: 15000 });
  await sleep(500);
  await page.screenshot({ path: path.join(OUT, `${file}.png`) });
  const rows = await page.$$eval('.el-table__body .el-table__row', (n) => n.length);
  console.log(`${route} status=${resp.status()} rows=${rows}`);
}

// 刷新深链接，确认 SPA 回退
const refresh = await page.goto(`${BASE}/brands`, { waitUntil: 'networkidle0' });
console.log('refresh /brands status:', refresh.status());
console.log('console errors:', errors.length ? errors : 'none');
console.log('failed requests:', failed.length ? failed : 'none');
await browser.close();
