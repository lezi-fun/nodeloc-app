# NodeLoc 前端镜像（mirror/）

> 抓取时间:2026-08-24 · 来源:https://www.nodeloc.com · 用途:**nodeloc-app 客户端 UI 还原参考**

## 结构(保留站点原始路径)
| 路径 | 内容 |
|---|---|
| www.nodeloc.com/index.html | 首页完整 DOM |
| www.nodeloc.com/t/topic/103946.html | 话题详情页样例(置顶公告) |
| www.nodeloc.com/stylesheets/ | 当前页面引用的完整样式表(含 light/dark、RTL、common、各插件与主题) |
| www.nodeloc.com/assets/js*/ | Discourse 核心 JS 及插件(88 个) |
| www.nodeloc.com/theme-javascripts/ | 站点自定义主题 JS(28 个) |
| www.nodeloc.com/latest.json 等 | 前端数据端点样例(latest/site/categories) |
| www.nodeloc.com/opensearch.xml | 站点搜索描述文件 |

## 设计速查
- 主色 #009966(暗 #118a53) 辅橙 #FF9933 · 暗底为暖棕黑 #1e1a15/#12100d
- 字体 Montserrat / JetBrains Mono · 详细见 research/brand-spec.md

> ⚠️ 镜像内所有内容版权归 NodeLoc 及原作者,仅限本客户端开发对照使用,勿分发。
