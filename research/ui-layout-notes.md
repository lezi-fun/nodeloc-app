# NodeLoc 网页版布局与样式实测笔记

> 采集方式:无头 Chrome 真渲染 + getComputedStyle 探针(非目测) · 2025-08-24
> 截图:research/ui-ref/{nl-desktop-light,dark,nl-mobile-light,nl-mobile-drawer,nl-mobile-dark-drawer}.png
> 数据:ui-styles-{light,dark}.json · ui-struct-desktop.json · ui-mobile-drawer.json

## 一、布局骨架
### 桌面 (1440×900)
```
.d-header-wrap (内容高 ~45.5px,透明底贴页面底色,无阴影)
 ├ 左:logo 字标(svg)
 └ 右:.btn-primary「推广」+ 语言切换 + 图标区
#main-outlet.wrap = [sidebar-wrapper | 内容列]
 sidebar:分区列表(见菜单) · 内容列:list-controls 筛选条 + table.topic-list
 topic-list 行: main-link(标题16px/400 + 摘要) | posters 头像叠放 | num 列
```
### 移动 (390×844)
```
顶栏:汉堡(aria-label=导航菜单) + 居中 logo + 图标
列表:单列行(标题/分类徽章/头像),无侧栏
菜单:「导航菜单」→ .menu-panel.revamped.slide-in 抽屉(左右滑入)
```

## 二、组件实测值(浅 vs 深)
| 组件 | Light | Dark |
|---|---|---|
| 页面底色 | #EDEAE3 暖纸色系(body 透明,继承) | 同源变量翻转为暖黑系 |
| header 底 | 透明/无边框阴影 | #1E1A15 |
| 话题标题 | 16px / 400 / #222222 | 同尺寸 #D5D5D5 |
| 分类徽章字 | 10.5px / #646464 | #A0A0A0 |
| 标签 pill | bg #F8F8F8 · 字 #919191 · r999 | bg #262626 · 字 #8A8A8A |
| 主按钮 | #009966 白字 r999 胶囊 | oklch(.42 .107 156)≈深绿 |
| header(暗色实拍) | — | rgb(30,26,21)=#1E1A15 |

> 关键确认:暗色 header 就是品牌规格的暖棕黑 #1E1A15,与 mirror CSS scheme_8 完全一致;
> 主按钮浅色即品牌绿 #009966,暗色用降明度绿(非同值)。

## 三、菜单清单(真实抓取)
### 侧栏/抽屉分区(data-section-name)
1. community:话题 / 更多
2. resources:关于 · 常见问题 · 服务条款 · 隐私政策 · 开放登录 · 能量星球 · 友情推荐 · 合作伙伴 · 广告投放
3. discourse-apps(站内小游戏):Tic-Tac-toe · 问道 · 宝可梦 · 符文珠阵 · 果刃 · 浏览更多
4. communities:浏览全部(第三方社区导航)
5. categories:互联网服务 · 科技与创作 · 数码与硬件 · 生活与兴趣 · 活动与互动 · 商业与金融…(带分类色)
6. tags:AI · AFF · 求助 · VPS · 已完成 · 所有标签

### 移动抽屉底部快捷条
更多 | 小程序 | 节点 | 标签 | 资源

## 四、对 Android App 的直接落点
1. 底色体系:App 浅色底用暖纸白(#EDEAE3 邻域)、暗底 #1E1A15 —— 不是纯白/纯黑;画布三方向中 B 最贴近官方气质
2. 标题排版:话题行标题 regular(400)/16sp 而非 semibold;分类徽章 10.5sp 灰字+色块
3. 按钮语言:全站胶囊形(r999);主操作绿底白字,暗色换深绿
4. 信息架构:App 抽屉复刻「社区/资源/游戏/分类/标签」五组 + 底部快捷条;游戏分区是 NodeLoc 特色入口
5. GIF 头像:详情页原样播放规则维持画布方案
