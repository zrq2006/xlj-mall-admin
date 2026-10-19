import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const OUT = path.join(ROOT, 'frontend', 'public', 'img', 'products');
fs.mkdirSync(OUT, { recursive: true });

// 用分类 emoji + 渐变底生成商品占位图，避免依赖外部图片资源
const items = [
  ['p1', '🎁', '#e8f0ff', '#c9dcff'],
  ['p2', '🥤', '#e6f7f2', '#c2ecdf'],
  ['p3', '🧹', '#f3ecff', '#dccbf7'],
  ['p4', '🍲', '#fff2e6', '#ffd9b8'],
  ['p5', '💄', '#ffeaf1', '#ffc9da'],
  ['p6', '🧴', '#eaf6ff', '#c6e6fb'],
  ['p7', '⌚', '#eef1f7', '#d3dae8'],
  ['p8', '👑', '#fff6e0', '#ffe2a8'],
  ['p9', '👟', '#eaf2ff', '#c8dcff'],
  ['p10', '🎧', '#e9edf5', '#cbd4e4'],
  ['p11', '👜', '#fdf0e6', '#f7d5b8'],
  ['p12', '🐶', '#f0f9ea', '#d4efc2'],
];

for (const [name, emoji, from, to] of items) {
  const svg = `<svg xmlns="http://www.w3.org/2000/svg" width="160" height="160" viewBox="0 0 160 160">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="${from}"/>
      <stop offset="100%" stop-color="${to}"/>
    </linearGradient>
  </defs>
  <rect width="160" height="160" rx="12" fill="url(#g)"/>
  <circle cx="80" cy="76" r="46" fill="#ffffff" opacity="0.55"/>
  <text x="80" y="96" font-size="56" text-anchor="middle">${emoji}</text>
</svg>
`;
  fs.writeFileSync(path.join(OUT, `${name}.svg`), svg, 'utf8');
}

console.log(`generated ${items.length} product images -> ${OUT}`);
