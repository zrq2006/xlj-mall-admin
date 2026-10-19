import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(HERE, 'screenshots');
fs.mkdirSync(OUT, { recursive: true });

const EDGE = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const BASE = process.env.BASE_URL || 'http://127.0.0.1:8080';
const consoleErrors = [];

const browser = await puppeteer.launch({
  executablePath: EDGE,
  headless: 'new',
  args: ['--no-sandbox', '--disable-gpu', '--hide-scrollbars', '--force-device-scale-factor=1'],
  defaultViewport: { width: 1440, height: 900, deviceScaleFactor: 1 },
});
const page = await browser.newPage();
page.on('console', (m) => {
  if (m.type() === 'error') consoleErrors.push(m.text());
});
page.on('pageerror', (e) => consoleErrors.push(`pageerror: ${e.message}`));

const sleep = (ms) => new Promise((r) => setTimeout(r, ms));
const shot = async (name) => {
  await sleep(500);
  await page.screenshot({ path: path.join(OUT, name) });
  console.log('saved', name);
};

// 1. 登录
await page.goto(`${BASE}/login`, { waitUntil: 'networkidle0' });
await page.click('.login-button');
await page.waitForSelector('.stat-card');
await sleep(900);
await page.screenshot({ path: path.join(OUT, '01-workbench.png') });
console.log('saved 01-workbench.png');

// 2. 商品列表 + 查询
await page.goto(`${BASE}/products`, { waitUntil: 'networkidle0' });
await page.waitForSelector('.el-table__row');
await sleep(600);
await page.screenshot({ path: path.join(OUT, '02-products.png') });
console.log('saved 02-products.png');

// 2.1 搜索
await page.type('.filter-form input', '饮水机');
await page.click('.filter-form .el-button--primary');
await sleep(900);
const filteredRows = await page.$$eval('.el-table__body .el-table__row', (rows) => rows.length);
const filteredNames = await page.$$eval('.product-name', (els) => els.map((e) => e.textContent.trim()));
console.log('搜索「饮水机」行数:', filteredRows, filteredNames.slice(0, 3));

// 2.2 新增商品弹窗
await page.click('.toolbar .el-button--success');
await page.waitForSelector('.el-dialog', { visible: true });
await sleep(500);
await page.screenshot({ path: path.join(OUT, '05-product-dialog.png') });
console.log('saved 05-product-dialog.png');
await page.keyboard.press('Escape');
await sleep(500);

// 3. 分类管理
await page.goto(`${BASE}/categories`, { waitUntil: 'networkidle0' });
await page.waitForSelector('.el-table__row');
await sleep(600);
await page.screenshot({ path: path.join(OUT, '03-categories.png') });
console.log('saved 03-categories.png');

// 4. 品牌管理 + 新增品牌
await page.goto(`${BASE}/brands`, { waitUntil: 'networkidle0' });
await page.waitForSelector('.el-table__row');
await sleep(600);
await page.screenshot({ path: path.join(OUT, '04-brands.png') });
console.log('saved 04-brands.png');

// 4.1 品牌新增（真实写入）
await page.click('.toolbar .el-button--primary');
await page.waitForSelector('.el-dialog', { visible: true });
await sleep(400);
const dialogInputs = await page.$$('.el-dialog input');
await dialogInputs[0].type('UI 自动化测试品牌');
await dialogInputs[1].type('U');
await page.screenshot({ path: path.join(OUT, '06-brand-dialog.png') });
await page.click('.el-dialog__footer .el-button--primary');
await sleep(1200);
const hasToast = await page.$eval('.el-message', (el) => el.textContent.trim()).catch(() => null);
console.log('新增品牌提示:', hasToast);

// 4.2 校验第一页是否出现新品牌（排序 0 会排在首位）
const names = await page.$$eval('.el-table__body .el-table__row', (rows) =>
  rows.map((r) => r.querySelectorAll('td')[2]?.textContent.trim()),
);
console.log('品牌首列:', names.slice(0, 3));

// 4.3 删除刚创建的品牌
const firstRowButtons = await page.$$('.el-table__body .el-table__row:first-child .el-button');
for (const btn of firstRowButtons) {
  const text = await btn.evaluate((el) => el.textContent.trim());
  if (text === '删除') {
    await btn.click();
    await sleep(500);
    await page.click('.el-message-box__btns .el-button--primary');
    await sleep(1000);
    break;
  }
}
const afterDelete = await page.$$eval('.el-table__body .el-table__row', (rows) => rows.length);
console.log('删除后行数:', afterDelete);

// 5. 刷新资源路径校验（深层路由 SPA 回退）
const resp = await page.goto(`${BASE}/brands`, { waitUntil: 'networkidle0' });
console.log('深层路由 status:', resp.status());

console.log('console errors:', consoleErrors.length ? consoleErrors : 'none');
await browser.close();
