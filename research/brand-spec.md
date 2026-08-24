# NodeLoc 品牌规格（brand-spec.md）

> 来源:https://www.nodeloc.com 线上站点实测(2025-08 抓取),非猜测。
> 资产目录: research/assets/

## 一句话定位
自由、平等、友好、开放、有趣的互联网交流社区(Discourse 论坛,2025-05 自 Flarum 迁移)。

## 品牌资产清单
| 资产 | 路径 | 备注 |
|---|---|---|
| Logo(横版字标) | assets/nodeloc-logo.svg | 描边风格 960×280,stroke #009966+#FF9933,圆头笔画 |
| Favicon | assets/favicon-32.png | 32×32 |
| 首页快照 | home.html | 完整 DOM |
| 全量样式 | common.css (1MB) / colors-light.css / colors-dark.css / theme-custom-css.css |

## 核心色板(CSS 变量实测)
| Token | Light | Dark |
|---|---|---|
| tertiary(品牌主色) | **#009966** | #118a53 |
| quaternary(辅橙) | **#FF9933** | #c14924 |
| love(红心) | #fa6c8d | #fa6c8d |
| primary(正文) | #222222 | #d5d5d5 |
| secondary(页面底) | #ffffff | **#1e1a15**(暖棕黑) |
| header_background | #ffffff | #12100d(暖棕黑) |
| success | #009900 | #1ca551 |
| danger | #c80001 | #e45735 |
| highlight | #ffff4d | #a87137 |

暗色主题特征:**暖棕色深底**(非纯黑灰),与绿色主色形成"暖夜+翠绿"识别度。

## 分类色(site.json 实测)
互联网服务 #2cb2b5 · 科技与创作 #826026 · 数码与硬件 #3184c4 · 生活与兴趣 #549447 · 活动与互动 #F1592A

## 字体
- 正文+标题: **Montserrat**, Arial, sans-serif(含 @font-face 内嵌)
- 代码: JetBrains Mono, Consolas, Monaco, monospace
- theme-color: light #ffffff / dark #12100e

## 站点趣味细节(theme-custom-css.css)
- 管理员 James 头像金色光晕脉冲动画(#f5bf03/gold)
- 版主头像青绿光晕 #62fedf
- 用户签名默认 30% 透明度,hover 显现
- 排行榜领奖台位移、Rich 徽章光效 → 社区重视游戏化氛围

## 未找到/待确认
- 无独立方形 App 图标源文件(favicon 可作基底,或需基于 logo 重绘)
- 无官方移动端 App(本仓库即为其 Android 客户端)


## 补充(2025-08-24):官方提供 14 套用户可选色板
详见 ui-layout-notes.md 第五节。默认亮=scheme7(NodeLoc) 默认暗=scheme8(Classic);
标准黑=scheme1。全部完整色值内嵌于 site.json,客户端可做"色板驱动"主题引擎。
