/**
 * 生成 db/02-seed.sql 演示数据（可重复执行，结果确定）
 * 运行：node tools/gen-seed.mjs
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const SRC = path.dirname(fileURLToPath(import.meta.url));

// 固定种子的伪随机数，保证每次生成结果一致
let seed = 20260928;
const rnd = () => {
  seed = (seed * 1103515245 + 12345) % 2147483648;
  return seed / 2147483648;
};
const pick = (arr) => arr[Math.floor(rnd() * arr.length)];
const int = (min, max) => min + Math.floor(rnd() * (max - min + 1));
const money = (min, max) => (Math.round((min + rnd() * (max - min)) * 10) / 10).toFixed(1);
const esc = (v) => (v === null || v === undefined ? 'NULL' : `'${String(v).replace(/'/g, "''")}'`);

/* ------------------------------------------------------------------ 分类 */
const categories = [
  { id: 16, name: '日用百货', icon: '🎁' },
  { id: 12, name: '美妆护肤', icon: '💄' },
  { id: 2, name: '彩妆', icon: '🎨' },
  { id: 3, name: '手表', icon: '⌚' },
  { id: 4, name: '首饰', icon: '👑' },
  { id: 5, name: '男士', icon: '👖' },
  { id: 6, name: '个护', icon: '🧴' },
  { id: 7, name: '箱包', icon: '👜' },
  { id: 8, name: '女装', icon: '👗' },
  { id: 9, name: '鞋靴', icon: '👟' },
  { id: 10, name: '童装', icon: '🧸' },
  { id: 11, name: '食品饮料', icon: '🍵' },
  { id: 13, name: '家居', icon: '🛋️' },
  { id: 14, name: '数码', icon: '🎧' },
  { id: 15, name: '运动户外', icon: '🏕️' },
  { id: 1, name: '宠物', icon: '🐶' },
];

/* ------------------------------------------------------------------ 品牌 */
const luxury = [
  ['阿蒂仙之香', 'A', "阿蒂仙之香（L'Artisan Parfumeur）是法国独立香氛品牌，1976 年创立于巴黎，坚持手工调香与天然原料，代表作品有无花果、冥府之路等，以克制而写意的香气著称。"],
  ['蒂普提克', 'T', '法国巴黎的艺术香氛品牌，它因独特的调香理念和手工灌装工艺而闻名，Diptyque 以香氛蜡烛与淡香水见长，作品灵感来自旅行、植物与旧日记忆。'],
  ['古驰', 'G', '意大利奢侈品牌，涵盖时装、皮具、鞋履与配饰，以双 G 标志和复古浪漫的设计语言享誉全球。'],
  ['普拉达', 'P', '意大利奢侈品牌，以皮具和时装闻名，尼龙材质与极简先锋的设计是其标志。'],
  ['赛琳', 'C', '法国奢侈品牌，主营皮具、成衣、鞋履，以简洁线条与法式优雅著称。'],
  ['葆蝶家', 'B', '意大利奢侈品牌，以精湛皮革工艺著称，标志性的编织皮革（Intrecciato）辨识度极高。'],
  ['博柏利', 'B', '英国奢侈品牌，经典风衣和格纹图案，兼具英伦传统与户外功能。'],
  ['蔻驰', 'C', '美国轻奢品牌，主营皮具和配饰，设计年轻、价格亲和。'],
  ['迪奥', 'D', '法国奢侈品牌，高级时装与美妆，New Look 廓形影响深远。'],
  ['SK-II', 'S', '日本高端护肤品牌，核心成分 PITERA™，主打精华水与修护系列。'],
  ['香奈儿', 'X', '法国奢侈品牌，时装、香水与美妆并举，斜纹软呢与 5 号香水最为经典。'],
  ['爱马仕', 'A', '法国奢侈品牌，以马具起家，皮具、丝巾与腕表工艺精湛。'],
  ['路易威登', 'L', '法国奢侈品牌，旅行箱包起家，Monogram 帆布与成衣系列广受欢迎。'],
  ['圣罗兰', 'S', '法国奢侈品牌，摇滚与高定风格并存的时装与美妆品牌。'],
  ['芬迪', 'F', '意大利奢侈品牌，皮草与手袋工艺出众，法棍包为经典单品。'],
  ['罗意威', 'L', '西班牙奢侈品牌，以皮具工艺与 Puzzle 手袋闻名。'],
  ['纪梵希', 'J', '法国奢侈品牌，优雅高定与美妆香氛并重。'],
  ['阿玛尼', 'A', '意大利奢侈品牌，西装剪裁与极简美学兼备，美妆线同样知名。'],
  ['华伦天奴', 'H', '意大利奢侈品牌，铆钉元素与高定礼服是其标签。'],
  ['范思哲', 'F', '意大利奢侈品牌，美杜莎标志与巴洛克印花风格浓烈。'],
  ['巴黎世家', 'B', '西班牙/法国奢侈品牌，街头化设计与老爹鞋引领潮流。'],
  ['蒂芙尼', 'D', '美国珠宝品牌，蓝色礼盒与六爪钻戒经典。'],
  ['卡地亚', 'K', '法国珠宝与腕表品牌，Love、Juste un Clou 系列知名。'],
  ['宝格丽', 'B', '意大利珠宝品牌，彩色宝石与蛇形元素设计独特。'],
  ['梵克雅宝', 'F', '法国高级珠宝品牌，四叶草 Alhambra 系列广受喜爱。'],
  ['施华洛世奇', 'S', '奥地利水晶品牌，切割水晶饰品与摆件闻名。'],
  ['潘多拉', 'P', '丹麦珠宝品牌，串饰 DIY 玩法深受年轻消费者欢迎。'],
  ['浪琴', 'L', '瑞士腕表品牌，名匠与康卡斯系列兼具优雅与运动。'],
  ['天梭', 'T', '瑞士腕表品牌，力洛克系列入门机械表口碑良好。'],
  ['卡西欧', 'K', '日本腕表品牌，G-SHOCK 系列以抗震耐用著称。'],
  ['西铁城', 'X', '日本腕表品牌，光动能机芯与走时精度是其优势。'],
  ['精工', 'J', '日本腕表品牌，机械机芯与 Spring Drive 技术并重。'],
  ['雷达', 'L', '瑞士腕表品牌，陶瓷材质应用领先。'],
  ['美度', 'M', '瑞士腕表品牌，建筑灵感设计与性价比兼顾。'],
];

const beauty = [
  ['花西子', 'H', '国货彩妆品牌，以东方美学与雕花工艺为特色。'],
  ['完美日记', 'W', '国货彩妆品牌，动物眼影盘与高性价比著称。'],
  ['薇诺娜', 'W', '国货功效护肤品牌，专注敏感肌修护。'],
  ['珀莱雅', 'P', '国货护肤品牌，早期抗老与精华品类表现突出。'],
  ['自然堂', 'Z', '国货护肤品牌，雪域精粹系列广为人知。'],
  ['百雀羚', 'B', '国货经典护肤品牌，草本配方温和亲肤。'],
  ['丸美', 'W', '国货眼霜品牌，专注眼部抗老护理。'],
  ['野兽派', 'Y', '生活方式品牌，鲜花、香氛与家居礼品并重。'],
  ['观夏', 'G', '东方香氛品牌，以中国植物与意象调香。'],
  ['三顿半', 'S', '精品速溶咖啡品牌，冷萃超即溶产品走红。'],
];

const home = [
  ['小米', 'X', '科技家电品牌，智能家居生态链完善。'],
  ['华为', 'H', '科技品牌，手机、穿戴与智慧屏产品线齐全。'],
  ['荣耀', 'R', '科技品牌，手机与笔记本主打性能与轻薄。'],
  ['OPPO', 'O', '手机品牌，影像与快充技术见长。'],
  ['vivo', 'V', '手机品牌，人像摄影与设计感突出。'],
  ['一加', 'Y', '手机品牌，旗舰性能与流畅系统体验。'],
  ['漫步者', 'M', '音频品牌，耳机与音箱性价比高。'],
  ['安克', 'A', '智能配件品牌，充电与储能产品出口全球。'],
  ['罗技', 'L', '外设品牌，键鼠与电竞设备口碑稳定。'],
  ['雷蛇', 'L', '电竞外设品牌，灯效与人体工学设计鲜明。'],
  ['大疆', 'D', '无人机与影像品牌，稳定器与航拍设备领先。'],
  ['极米', 'J', '智能投影品牌，家用投影与激光电视。'],
  ['戴森', 'D', '英国家电品牌，吹风机、吸尘器与空气净化。'],
  ['飞利浦', 'F', '荷兰家电品牌，个护与照明产品线广泛。'],
  ['松下', 'S', '日本家电品牌，小家电与个护电器品质稳定。'],
  ['博朗', 'B', '德国家电品牌，剃须刀与厨房小电著称。'],
  ['象印', 'X', '日本保温品牌，保温杯与电饭煲。'],
  ['虎牌', 'H', '日本保温品牌，儿童与户外保温产品丰富。'],
  ['膳魔师', 'S', '德国保温品牌，真空保温技术。'],
  ['乐扣乐扣', 'L', '韩国保鲜容器品牌，密封与材质安全。'],
  ['苏泊尔', 'S', '厨房小家电品牌，锅具与电饭煲产品线全。'],
  ['九阳', 'J', '厨房小家电品牌，豆浆机与破壁机知名。'],
  ['美的', 'M', '综合家电品牌，白电与小家电覆盖广。'],
  ['格力', 'G', '空调品牌，核心技术与渠道能力强。'],
  ['海尔', 'H', '综合家电品牌，冰洗产品线完整。'],
  ['小天鹅', 'X', '洗衣机品牌，滚筒与洗烘一体机。'],
  ['恒洁', 'H', '卫浴品牌，智能马桶与浴室柜。'],
  ['林氏家居', 'L', '家居品牌，全屋家具与软装搭配。'],
  ['源氏木语', 'Y', '实木家具品牌，原木风格与环保用材。'],
  ['全棉时代', 'Q', '棉品生活方式品牌，母婴与个护棉品。'],
  ['无印良品', 'W', '日式生活方式品牌，简约耐用的家居杂货。'],
  ['名创优品', 'M', '快时尚百货品牌，IP 联名与小件日用品。'],
  ['三只松鼠', 'S', '休闲零食品牌，坚果与果干品类丰富。'],
  ['良品铺子', 'L', '休闲零食品牌，高端零食与礼盒。'],
  ['百草味', 'B', '休闲零食品牌，果干与肉类零食。'],
  ['来伊份', 'L', '休闲零食品牌，门店与线上并重。'],
  ['元气森林', 'Y', '饮料品牌，无糖气泡水与茶饮。'],
  ['农夫山泉', 'N', '饮用水品牌，天然水与茶 π 系列。'],
  ['伊利', 'Y', '乳制品品牌，液态奶与奶粉。'],
  ['蒙牛', 'M', '乳制品品牌，常温奶与低温鲜奶。'],
  ['雀巢', 'Q', '食品饮料集团，咖啡与冲调饮品。'],
  ['星巴克', 'X', '咖啡连锁品牌，挂耳与胶囊咖啡延伸。'],
  ['瑞幸', 'R', '咖啡连锁品牌，即溶与冻干咖啡。'],
  ['喜茶', 'X', '茶饮品牌，瓶装茶与周边。'],
  ['奈雪的茶', 'N', '茶饮品牌，鲜果茶与烘焙。'],
  ['耐克', 'N', '运动品牌，跑鞋与篮球装备。'],
  ['阿迪达斯', 'A', '运动品牌，三条纹经典产品线。'],
  ['彪马', 'B', '运动品牌，复古跑鞋与足球装备。'],
  ['新百伦', 'X', '运动品牌，复古慢跑鞋与总统跑鞋。'],
  ['亚瑟士', 'Y', '日本运动品牌，专业跑鞋与缓震科技。'],
  ['安德玛', 'A', '运动品牌，训练装备与紧身衣。'],
  ['李宁', 'L', '国货运动品牌，䨻科技与国潮设计。'],
  ['安踏', 'A', '国货运动品牌，氮科技跑鞋与冠军系列。'],
  ['特步', 'T', '国货运动品牌，跑鞋与大众运动。'],
  ['迪卡侬', 'D', '运动零售品牌，全品类平价运动装备。'],
  ['骆驼', 'L', '户外品牌，露营与徒步装备。'],
  ['探路者', 'T', '户外品牌，冲锋衣与登山装备。'],
  ['波司登', 'B', '羽绒服品牌，高端化与保暖科技。'],
  ['优衣库', 'Y', '服装品牌，基础款与摇粒绒、Heattech。'],
  ['太平鸟', 'T', '服装品牌，年轻化设计与联名。'],
  ['森马', 'S', '休闲服装品牌，大众基础款。'],
  ['海澜之家', 'H', '男装品牌，衬衫与西裤。'],
  ['韩都衣舍', 'H', '女装品牌，快反供应链与韩系风格。'],
  ['茵曼', 'Y', '棉麻女装品牌，自然文艺风格。'],
  ['内外', 'N', '内衣品牌，无尺码与舒适科技。'],
  ['蕉内', 'J', '内衣品牌，无标签与体感科技。'],
];

// 让品牌 id 34 / 38 分别对应截图中的阿蒂仙之香 / 蒂普提克，其余保持列表顺序（id 1=古驰，2=普拉达…）
const brandList = [...luxury, ...beauty, ...home].map(([name, letter, description]) => ({ name, letter, description }));
const takeOut = (name) => {
  const i = brandList.findIndex((b) => b.name === name);
  return brandList.splice(i, 1)[0];
};
const atelier = takeOut('阿蒂仙之香');
const diptyque = takeOut('蒂普提克');
brandList.splice(33, 0, atelier);
brandList.splice(37, 0, diptyque);
brandList.length = 105;
const brands = brandList.map((b, i) => ({ ...b, id: i + 1 }));
brands.forEach((b, i) => {
  b.sort = i === 33 ? 1 : i === 37 ? 2 : (i + 1) * 10;
  b.createdAt = i < 2 ? 'DATE_SUB(NOW(), INTERVAL 19 DAY)' : 'DATE_SUB(NOW(), INTERVAL 50 DAY)';
});
brands[33].description = "阿蒂仙之香（L'Artisan Parfumeur）是法国独立香氛品牌，1976 年创立于巴黎，坚持手工调香与天然原料，代表作品有无花果、冥府之路等，以克制而写意的香气著称，适合追求个性化的香氛爱好者。";
brands[37].description = '法国巴黎的艺术香氛品牌，它因独特的调香理念和手工灌装工艺而闻名，Diptyque 以香氛蜡烛与淡香水见长，作品灵感来自旅行、植物与旧日记忆，是家居香氛领域的代表品牌。';

/* ------------------------------------------------------------------ 商品 */
const productBases = [
  ['饮水机小型台式', 16, 168, 158], ['自集尘吸尘器', 16, 719.1, 699], ['多功能电煮锅', 16, 149, 119],
  ['空气炸锅家用大容量', 16, 259, 229], ['即热式饮水机', 16, 399, 359], ['折叠收纳箱三件套', 16, 45.9, 39.9],
  ['抽绳垃圾袋加厚', 16, 12.9, 9.9], ['一次性洗脸巾', 16, 29.9, 24.9],
  ['流金水爽肤精华', 12, 359, 329], ['氨基酸洁面乳', 12, 89, 79], ['烟酰胺身体乳', 12, 69, 59],
  ['视黄醇晚霜', 12, 229, 199], ['积雪草修护面膜', 12, 99, 85],
  ['丝绒雾面唇釉', 2, 119, 99], ['十二色眼影盘', 2, 149, 129], ['持妆粉底液', 2, 199, 179],
  ['纤细眼线笔', 2, 59, 49], ['浮雕腮红', 2, 89, 75],
  ['经典自动机械表', 3, 1299, 1180], ['石英简约腕表', 3, 699, 620], ['防水运动电子表', 3, 359, 299],
  ['轻奢锁骨链', 4, 259, 230], ['淡水珍珠耳钉', 4, 189, 165], ['999 足银手镯', 4, 399, 360],
  ['休闲商务衬衫', 5, 259, 229], ['纯棉圆领短袖', 5, 89, 75], ['修身直筒休闲裤', 5, 199, 179],
  ['声波电动牙刷', 6, 229, 199], ['负离子吹风机', 6, 299, 269], ['控油洗发水套装', 6, 129, 109],
  ['真皮通勤单肩包', 7, 899, 820], ['大容量旅行背包', 7, 359, 320], ['迷你链条斜挎包', 7, 469, 429],
  ['法式碎花连衣裙', 8, 329, 299], ['宽松针织开衫', 8, 259, 229],
  ['复古厚底老爹鞋', 9, 299, 269], ['轻便跑步鞋', 9, 419, 379],
  ['儿童纯棉家居服', 10, 89, 75], ['儿童防晒外套', 10, 149, 129],
  ['每日坚果礼盒', 11, 89, 79], ['冷萃即溶咖啡', 11, 69.9, 59.9], ['原味气泡水整箱', 11, 49.9, 42.9],
  ['实木床头柜', 13, 359, 320], ['乳胶护颈枕', 13, 199, 169], ['香薰蜡烛礼盒', 13, 129, 110],
  ['主动降噪蓝牙耳机', 14, 599, 549], ['机械键盘 87 键', 14, 359, 319], ['便携充电宝 20000mAh', 14, 149, 129],
  ['轻量化登山背包', 15, 299, 269], ['户外折叠露营椅', 15, 129, 109],
  ['猫粮全价冻干双拼', 1, 189, 165], ['宠物自动喂食器', 1, 269, 239],
];

const suffixes = ['', '【升级款】', ' 家用款', ' 2代', ' 礼盒装', ' 大容量', ' 2026 新款', ' 便携款', ' 旗舰版', ' 经典款'];
const featurePools = {
  16: '家用小电器，体积紧凑、操作简单，宿舍与办公室都能轻松收纳，日常使用省心。',
  12: '温和配方，肤感清爽不黏腻，早晚护肤搭配使用，适合长期囤货。',
  2: '粉质细腻易晕染，显色自然，日常通勤与出街妆容都能驾驭。',
  3: '走时稳定，表盘清晰易读，通勤与休闲场景都合适，附赠精致表盒。',
  4: '925 银/合金材质，做工细腻不易褪色，单戴叠戴都有气质。',
  5: '版型利落，面料挺括亲肤，四季可穿，通勤与休闲两相宜。',
  6: '温和清洁不刺激，日常护理一步到位，全家都能使用。',
  7: '容量分区合理，五金与走线扎实，通勤出差都能装下必备物品。',
  8: '版型友好不挑身材，面料垂顺，日常搭配一件就很有型。',
  9: '鞋底缓震回弹，楦型宽松不挤脚，久走久站也舒适。',
  10: '亲肤棉质面料，做工细致无多余线头，孩子穿着自在。',
  11: '甄选原料，独立小包装锁鲜，办公室与居家常备。',
  13: '简约耐看的家居好物，用料环保，装点空间提升生活质感。',
  14: '性能与便携兼顾，做工扎实，日常使用体验流畅稳定。',
  15: '轻量耐磨，收纳设计合理，周末露营与轻徒步都能胜任。',
  1: '配方温和，适口性好，毛孩子吃得安心，铲屎官更省心。',
};

const products = [];
let pid = 1;
for (let round = 0; products.length < 242; round++) {
  for (const [name, categoryId, price, market] of productBases) {
    if (products.length >= 242) break;
    const suffix = round === 0 ? '' : suffixes[round % suffixes.length];
    const brand = brands[int(0, brands.length - 1)];
    const p = Number(price) + round * 3;
    const m = Number(market) + round * 3;
    products.push({
      id: pid++,
      name: name + suffix,
      description: pick([featurePools[categoryId]]) + ' ' + pick(['现货速发', '顺丰包邮', '支持七天无理由', '假一赔十', '赠运费险']),
      categoryId,
      brandId: brand.id,
      price: p.toFixed(2),
      marketPrice: m.toFixed(2),
      stock: int(30, 999),
      sales: int(0, 260),
      image: `/img/products/p${(pid % 12) + 1}.svg`,
      status: rnd() > 0.12 ? 1 : 0,
      createdAt: `DATE_SUB(NOW(), INTERVAL ${int(1, 900)} HOUR)`,
    });
  }
}
// 让最新的三件商品与截图一致（id 242/241/240）
const last = products.slice(-3);
const exact = [
  {
    name: '饮水机小型台式',
    description: '饮水机小型台式家用制冷热两用立式冰温热迷你桌面办公室宿舍饮水器 樱花粉【常温+制热】制热型 旗舰冷热两用+防尘门【20升内大小桶+晒图返10】',
    price: '168.00', marketPrice: '158.00', stock: 136, sales: 0, image: '/img/products/p2.svg',
  },
  {
    name: '自集尘吸尘器',
    description: '返100元E卡到手619元】自集尘吸尘器 G30Max（pro）绿光家用吸尘器 30KPa吸力 70min续航 开荒',
    price: '719.10', marketPrice: '699.00', stock: 136, sales: 0, image: '/img/products/p3.svg',
  },
  {
    name: '多功能电煮锅',
    description: '多功能电煮锅1.5L 蒸煮焖炖涮 5挡火力调节 锅碗一体 安全防干烧 支持预约电蒸锅电煮锅',
    price: '149.00', marketPrice: '119.00', stock: 136, sales: 0, image: '/img/products/p4.svg',
  },
];
last.forEach((p, i) => {
  Object.assign(p, exact[2 - i]);
  p.categoryId = 16;
  p.brandId = null;
});
// 截图最上方是 id 242，故倒序写入，保证 id 与排序一致
products.sort((a, b) => a.id - b.id);

/* ------------------------------------------------------------------ 会员/商家/订单 */
const members = [];
for (let i = 1; i <= 320; i++) {
  members.push({
    nickname: `${pick(['小蓝', '海盐', '柠檬', '柚子', '橘子', '奶糖', '栗子', '云朵'])}${int(1000, 9999)}`,
    phone: `1${pick(['3', '5', '7', '8', '9'])}${String(int(10000000, 99999999))}`,
    createdAt: `DATE_SUB(NOW(), INTERVAL ${int(1, 400)} DAY)`,
  });
}

const merchants = [
  ['广州鲜果供应链有限公司', 0], ['杭州织造服饰有限公司', 1], ['上海橙意食品有限公司', 0],
  ['深圳蓝鲸数码科技有限公司', 1], ['义乌小商品贸易商行', 2], ['成都川味食品厂', 1],
  ['苏州家居用品制造厂', 0], ['青岛海产直供合作社', 1],
];

const orders = [];
const statuses = ['PENDING_PAY', 'PENDING_SHIP', 'SHIPPED', 'COMPLETED', 'CANCELLED'];
for (let i = 0; i < 180; i++) {
  const status = i < 4 ? 'PENDING_PAY' : i < 9 ? 'PENDING_SHIP' : pick(statuses);
  const amount = (int(4900, 129900) / 100).toFixed(2);
  const today = i < 7;
  orders.push({
    orderNo: `XLJ${2026101700000 + i}`,
    memberId: int(1, 320),
    total: amount,
    pay: amount,
    status,
    createdAt: today
      ? `DATE_SUB(NOW(), INTERVAL ${int(10, 600)} MINUTE)`
      : `DATE_SUB(NOW(), INTERVAL ${int(2, 60)} DAY)`,
    paidAt: ['PENDING_PAY', 'CANCELLED'].includes(status)
      ? 'NULL'
      : today
        ? `DATE_SUB(NOW(), INTERVAL ${int(5, 300)} MINUTE)`
        : `DATE_SUB(NOW(), INTERVAL ${int(2, 58)} DAY)`,
  });
}

/* ------------------------------------------------------------------ 输出 */
const out = [];
out.push('-- =============================================================');
out.push('-- 小蓝鲸优选 · 管理后台  演示数据（由 tools/gen-seed.mjs 生成，请勿手工编辑）');
out.push('-- mysql -u xlj_mall -pXljMall_2026 -h 127.0.0.1 xlj_mall < 02-seed.sql');
out.push('-- =============================================================');
out.push('SET NAMES utf8mb4;');
out.push('USE `xlj_mall`;');
out.push('');
out.push('DELETE FROM `product`;');
out.push('DELETE FROM `brand`;');
out.push('DELETE FROM `category`;');
out.push('DELETE FROM `mall_order`;');
out.push('DELETE FROM `merchant`;');
out.push('DELETE FROM `member`;');
out.push('');

out.push('-- 分类 ------------------------------------------------------------------');
for (const c of categories) {
  out.push(
    `INSERT INTO \`category\` (id, name, icon, image, sort, status, created_by) VALUES (${c.id}, ${esc(c.name)}, ${esc(c.icon)}, NULL, ${categories.indexOf(c)}, 1, 'system');`,
  );
}

out.push('');
out.push('-- 品牌 ------------------------------------------------------------------');
const brandRows = brands.map((b) => {
  const logo = [34, 38].includes(b.id) ? esc(b.id === 34 ? '🫙' : '🧴') : 'NULL';
  return `(${b.id}, ${esc(b.name)}, ${logo}, ${esc(b.letter)}, ${esc(b.description)}, ${b.sort}, 1, 'system', ${b.createdAt})`;
});
for (let i = 0; i < brandRows.length; i += 20) {
  out.push(
    'INSERT INTO `brand` (id, name, logo, first_letter, description, sort, status, created_by, created_at) VALUES\n' +
      brandRows.slice(i, i + 20).join(',\n') +
      ';',
  );
}

out.push('');
out.push('-- 商品 ------------------------------------------------------------------');
const productRows = products.map(
  (p) =>
    `(${p.id}, ${esc(p.name)}, ${esc(p.description)}, ${p.categoryId}, ${p.brandId}, ${p.price}, ${p.marketPrice}, ${p.stock}, ${p.sales}, ${esc(p.image)}, ${p.status}, 'system', ${p.createdAt})`,
);
for (let i = 0; i < productRows.length; i += 20) {
  out.push(
    'INSERT INTO `product` (id, name, description, category_id, brand_id, price, market_price, stock, sales, image, status, created_by, created_at) VALUES\n' +
      productRows.slice(i, i + 20).join(',\n') +
      ';',
  );
}

out.push('');
out.push('-- 会员 / 商家 / 订单（工作台统计） --------------------------------------');
const memberRows = members.map(
  (m) => `(${esc(m.nickname)}, ${esc(m.phone)}, ${m.createdAt})`,
);
out.push('INSERT INTO `member` (nickname, phone, created_at) VALUES\n' + memberRows.join(',\n') + ';');
out.push(
  'INSERT INTO `merchant` (name, audit_status, created_at) VALUES\n' +
    merchants.map(([name, s]) => `(${esc(name)}, ${s}, DATE_SUB(NOW(), INTERVAL ${int(1, 20)} DAY))`).join(',\n') +
    ';',
);
out.push(
  'INSERT INTO `mall_order` (order_no, member_id, total_amount, pay_amount, status, created_at, paid_at) VALUES\n' +
    orders
      .map((o) => `(${esc(o.orderNo)}, ${o.memberId}, ${o.total}, ${o.pay}, ${esc(o.status)}, ${o.createdAt}, ${o.paidAt})`)
      .join(',\n') +
    ';',
);
out.push('');

fs.mkdirSync(path.join(SRC, '..', 'db'), { recursive: true });
fs.writeFileSync(path.join(SRC, '..', 'db', '02-seed.sql'), out.join('\n'), 'utf8');
console.log(`seed written: ${categories.length} categories, ${brands.length} brands, ${products.length} products, ${members.length} members, ${merchants.length} merchants, ${orders.length} orders`);
