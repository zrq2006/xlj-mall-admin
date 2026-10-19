import path from 'node:path';
import { fileURLToPath } from 'node:url';
import puppeteer from 'puppeteer-core';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const OUT = path.join(HERE, 'screenshots');
const EDGE = 'C:\\Program Files (x86)\\Microsoft\\Edge\\Application\\msedge.exe';
const BASE = process.env.BASE_URL || 'http://127.0.0.1:8080';

const browser = await puppeteer.launch({
  executablePath: EDGE,
  headless: 'new',
  args: ['--no-sandbox', '--disable-gpu', '--hide-scrollbars', '--force-device-scale-factor=1'],
  defaultViewport: { width: 1440, height: 900, deviceScaleFactor: 1 },
});
const page = await browser.newPage();
const errors = [];
page.on('pageerror', (e) => errors.push(e.message));
page.on('console', (m) => m.type() === 'error' && errors.push(m.text()));
const sleep = (ms) => new Promise((r) => setTimeout(r, ms));

await page.goto(`${BASE}/login`, { waitUntil: 'networkidle0' });
await page.click('.login-button');
await page.waitForSelector('.stat-card');

// 商品编辑
await page.goto(`${BASE}/products`, { waitUntil: 'networkidle0' });
await page.waitForSelector('.el-table__row');
await sleep(500);
const firstRowPrice = () => page.$eval('.el-table__body .el-table__row:first-child td:nth-child(3)', (el) => el.textContent.trim());
console.log('编辑前售价:', await firstRowPrice());
await page.click('.el-table__body .el-table__row:first-child .el-button');
await page.waitForSelector('.el-dialog', { visible: true });
await sleep(400);
const inputs = await page.$$('.el-dialog .el-input-number input');
console.log('数字输入框数量:', inputs.length);
await inputs[0].click({ clickCount: 3 });
await inputs[0].type('188');
await page.click('.el-dialog__footer .el-button--primary');
await sleep(1200);
const toast = await page.$eval('.el-message', (el) => el.textContent.trim()).catch(() => null);
console.log('保存提示:', toast);
console.log('编辑后售价:', await firstRowPrice());
await page.screenshot({ path: path.join(OUT, '07-product-edited.png') });

// 恢复原价
await page.click('.el-table__body .el-table__row:first-child .el-button');
await page.waitForSelector('.el-dialog', { visible: true });
await sleep(400);
const inputs2 = await page.$$('.el-dialog .el-input-number input');
await inputs2[0].click({ clickCount: 3 });
await inputs2[0].type('149');
await page.click('.el-dialog__footer .el-button--primary');
await sleep(1000);
console.log('恢复后售价:', await firstRowPrice());

// 分页
await page.click('.el-pager li:nth-child(2)');
await sleep(900);
const firstIdPage2 = await page.$eval('.el-table__body .el-table__row:first-child td:first-child', (el) => el.textContent.trim());
console.log('第二页首行 ID:', firstIdPage2);

// 分类新增 -> 删除（走 UI）
await page.goto(`${BASE}/categories`, { waitUntil: 'networkidle0' });
await page.waitForSelector('.el-table__row');
await page.click('.toolbar .el-button--primary');
await page.waitForSelector('.el-dialog', { visible: true });
await sleep(300);
await page.type('.el-dialog input', 'UI测试分类');
await page.click('.el-dialog__footer .el-button--primary');
await sleep(1000);
const catToast = await page.$eval('.el-message', (el) => el.textContent.trim()).catch(() => null);
console.log('新增分类提示:', catToast);

await page.goto(`${BASE}/categories?page=1`, { waitUntil: 'networkidle0' });
await page.waitForSelector('.el-table__row');
await sleep(600);
await page.click('.el-pager li:last-child');
await sleep(800);
const lastPageNames = await page.$$eval('.el-table__body .el-table__row', (rows) =>
  rows.map((r) => r.querySelectorAll('td')[3]?.textContent.trim()),
);
console.log('分类最后一页:', lastPageNames);

// 找到并删除测试分类
let removed = false;
for (let p = 0; p < 3 && !removed; p++) {
  const found = await page.$$eval('.el-table__body .el-table__row', (rows) =>
    rows.map((r) => r.textContent.includes('UI测试分类')),
  );
  const idx = found.indexOf(true);
  if (idx >= 0) {
    const btns = await page.$$('.el-table__body .el-table__row');
    const del = await btns[idx].$$('.el-button');
    for (const b of del) {
      const t = await b.evaluate((el) => el.textContent.trim());
      if (t === '删除') {
        await b.click();
        await sleep(500);
        await page.click('.el-message-box__btns .el-button--primary');
        await sleep(1000);
        removed = true;
        console.log('已删除测试分类');
        break;
      }
    }
  } else {
    await page.click('.el-pager li:nth-child(2)');
    await sleep(700);
  }
}

console.log('console errors:', errors.length ? errors : 'none');
await browser.close();
