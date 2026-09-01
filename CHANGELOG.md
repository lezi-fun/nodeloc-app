# NodeLoc App 更新日志

## v0.3.3 (2026-09-01)

本版以修复为主。相当一部分工作是回头对照 Discourse 官方源码（`discourse/discourse`
的 `frontend/discourse-markdown-it`、`app/lib/composer/toolbar.ts`、
`common/base/onebox.scss` 等）逐项核对，纠正之前凭猜实现、与官网不一致的地方。

### 🐛 问题修复

- **Markdown 预览彻底修好**
  - 根因：`POST /posts/preview.json` 在 Discourse 上并不存在，任何内容都返回 404
    （"找不到请求的 URL 或资源。"）。官网 composer 的预览本来就是浏览器里
    markdown-it 客户端渲染，没有服务端预览端点
  - 改为本地渲染：CommonMark + GFM 表格 / 删除线 / 自动链接扩展，产出的 HTML
    交给 `CookedText`，与帖子正文共用同一套排版
  - 创建话题、楼层回复、编辑帖子三处预览统一切到本地渲染
  - 渲染细节对齐 `discourse-markdown-it/src/setup.js`：单换行渲染成 `<br>`
    （官网 `breaks: !traditional_markdown_linebreaks`，本站未开该设置）、
    `html: true` 不转义正文 HTML、`:emoji:` 图片属性照 `features/emoji.js`
    与站点实际 cooked 对齐（`emoji_set` 是 `unicode` 而非 `twitter`）

- **第三方登录改为应用内完成**
  - 原实现走不通：用 CustomTabs 跳外部浏览器、靠 `nodeloc://auth` 回跳，但
    Discourse 的 `redirect_uri` 注册在提供商侧（实测 `/auth/github` 带的是
    站点自己的 `/auth/github/callback`），自定义 scheme 永远不触发；
    兑换调用打的 `/session/current_user_confirm_session` 实测 404
  - 新增应用内 WebView 授权：打开 `/auth/{provider}` → 用户在提供商页面授权 →
    检测到从提供商跳回站点域即取会话 cookie 并关闭 WebView →
    同步进 OkHttp 的 CookieJar，再经 `/session/current.json` 确认登录态
  - 判定以"离开过站点再跳回"为条件，不依赖 callback 路径拼写；站点域按主机名
    比对，避免 `www` / 尾斜杠差异。拿不到长期登录 cookie 时保持 WebView 打开，
    首次登录可在站内补完注册表单
  - 登录页正常显示第三方登录入口（Google、GitHub、X、Telegram）

- **onebox 链接卡片对齐官网的内 / 外链两种形态**
  - 外链卡片：标题链接改取 `h3 > a`（原先取第一个 `<a>`，实际拿到的是
    `header.source` 里的域名链接）；来源域名取 `header.source`；
    缩略图按 `.aspect-image` 的 `--aspect-ratio` 铺满宽度，无该包裹时还原成
    官网的左浮动小图（`max-width: 35%`、`max-height: 170px`）
  - 删掉误加的左侧强调色竖线 —— `onebox-shadow` mixin 是 `border: 0` 加
    1px 描边与 4px 浅色外圈，`border-left` 只在 onebox 被引用块包住时出现
  - 站内话题引用：新增独立渲染，24px 头像 + 话题链接 + 分类徽章 +
    blockquote 摘要，徽章颜色解析官网内联的 `--category-badge-color`；
    补上属于它的 `border-left`（5px 中性灰），标题与正文共用同一块底色

- **投票功能修正**：投票按钮替代点赞按钮而非并存；帖子本身算 1 票，初始分数为 1；
  顶 / 踩箭头改用官网 `arrow-big-up` / `arrow-big-down` 的原始 SVG 路径构造，
  不再用 Material Icons 的细箭头
- **表情**：工具栏表情按钮原用带头发的 `Icons.Filled.Face`，官网是
  `far-face-smile`（只有圆圈轮廓加眼睛和嘴），已更换；选择面板去掉 chip 的
  边框与填充背景，改成官网的裸图网格，单元格尺寸按 `emoji-picker.scss` 取值
  （图片 24dp、单元格 36dp）
- 修复多个帖子渲染问题：Markdown 图片语法 `![alt](url)` 的渲染、小程序改为
  可点击卡片并在独立 Activity 打开、`<details>` 标签导致的崩溃
- 修复点赞图标样式：改用 Material Icons 的中空 / 实心心形，与官网行为一致
- 修复点赞后回复按钮暂时消失：用 `key(post.id, canReply)` 稳定重组期间的按钮状态
- 修复话题界面登录提示闪烁：登录态在检查完成前为未知状态，不再从"登录后回复"
  跳变成回复框
- 修复搜索类型选择：补上用户与分类搜索结果的显示，排序选项只在话题 / 帖子搜索时出现

### ✨ 新增功能

- **编辑器工具栏对齐官网 composer 的菜单结构**
  - 分组顺序按官网 `fontStyles → insertions → extras` 排列
  - 标题与列表改为弹出菜单：标题 6 项（标题 1-4 / 正文 / 小号文字）、
    列表 3 项（无序 / 有序 / 任务清单）
  - 新增 `+` 菜单：代码块、插入表格、应用 wrap
  - 代码按官网 `if (!this.capabilities.touch)` 的做法从触屏主栏移出
- **Markdown / 预览切换改成官网的分段开关**：复刻 composer 工具栏最左侧的
  `ComposerToggleSwitch`，灰底滑轨上一块浅色滑块，左格是 Font Awesome 的
  markdown 标记、右格是字母 A；尺寸照 `composer-toggle-switch.scss` 取值
- **帖子投票**：顶 / 踩 API 与界面
- **发私信**：用户资料页新增入口与撰写对话框
- **复制 Markdown**：帖子更多菜单新增
- **退出登录**：设置界面新增
- 编辑帖子时自动预填充原内容
- 回复后局部插入新楼层，不再整页刷新，避免页面跳动

### 🔧 优化改进

- 新增依赖：CommonMark 及 GFM 表格 / 删除线 / 自动链接扩展（本地 Markdown 渲染）
- 清理已失效的代码：`previewPost` 与 `PostPreviewDto`、`nodeloc://auth` deeplink、
  `AuthCallbackHandler`、`loginWithPayload`
- 删除临时测试脚本与 README 中过时的功能列表

### ⚠️ 已知限制

- 本地 Markdown 渲染不认 Discourse 自有的 bbcode（`[quote]`、`[details]`、
  `[wrap]` 等）与 onebox，预览里这些会显示为原文；发布后由服务端正常渲染
- `:emoji:` 简码没有内置 emoji 名单，打错的简码在预览里会显示为裂图
  （官网未命中时保留原文）
- 第三方登录的收尾（跳回站点域后取 cookie）需真实账号授权才能走通，尚未实测

---

## v0.3.2 (2024-XX-XX)

### ✨ 新增功能
- **应用自动更新系统**
  - 启动时自动检查 GitHub Release 最新版本
  - Material 3 风格更新提示对话框，显示版本号和更新内容
  - 一键下载 universal APK 并自动触发安装
  - 支持版本号智能比较（x.y.z 格式）
  - 后台下载进度通知，完成后弹出系统安装界面

- **MessageBus 实时消息系统**
  - 实现 Discourse MessageBus 长轮询机制
  - 支持 15+ 个频道类型订阅（/latest、/topic/{id}、/categories 等）
  - 自动重连机制（指数退避策略）
  - 话题详情页动态订阅，收到更新时自动刷新

- **设备信息显示**
  - 发帖时根据 `post_source_level` 设置自动添加设备信息
  - 话题详情页显示发帖设备徽章（平台、品牌、型号）
  - 圆角背景徽章样式，与称号样式一致

### 🔧 优化改进
- 版本号统一管理，支持通过 `-PappVersion` 或环境变量指定
- 初始版本设为 v0.0.1
- User-Agent 完全模拟真实 Android Chrome
- 修正签到请求格式

### 📱 技术架构
- 添加 FileProvider 支持 Android 7.0+ APK 安装
- 实现 APK 下载管理器（DownloadManager + BroadcastReceiver）
- 新增 `REQUEST_INSTALL_PACKAGES` 权限

---

## v0.3.1 (2024-XX-XX)

### ✨ 新增功能
- **后台版本检查**：集成 GitHub Release API，静默检查新版本
- **站点自定义表情**：完整支持 NodeLoc 自定义表情，按分组显示友好名称

### 🐛 问题修复
- 修复表情选择器和 Markdown 预览功能
- 表情选择器按名称前缀正确分组
- 表情选择器只显示 NodeLoc 自定义表情，过滤系统表情
- 为无图片节点显示默认图标
- 完善侧边栏小程序列表显示
- 对齐侧边栏"查看更多内容"按钮
- 补全编辑器附件上传参数
- 改善小程序 WebView 加载体验
- 修复创建话题编辑器操作
- 对齐打赏能量图标颜色
- 修正抽奖长按与图片链接展示
- 对齐抽奖卡片交互体验
- 增加外部链接安全提示
- 改善帖子图片与链接渲染
- 修复抽奖购票数量校验

### 🔧 优化改进
- 完善帖子编辑器与社区交互
- 表情显示更友好的分组名称

### 🎨 用户体验
- 点击侧边栏用户头像可打开个人资料
- 禁用侧边栏滑动手势避免误触

---

## v0.3.0 (2024-XX-XX)

### ✨ 新增功能
- **管理员/版主专属功能**
  - 楼层操作菜单新增管理功能：锁定、设为 Wiki、重新渲染、取消隐藏、变更所有者、永久删除
  - 楼层操作（收藏、举报、删除、恢复、点赞）完全跟随服务端权限

- **动态配置系统**
  - 侧栏节点、标签、小程序改为跟随服务端配置，不再本地硬编码
  - 创建话题的节点选择器改为可搜索的底部弹层，无需滚动查找

- **阅读进度上报**
  - 支持 screen-track 阅读进度统计
  - 对齐官网 POST /topics/timings 触发规则

- **帖子编辑功能**
  - 支持编辑自己发过的帖子（PUT /posts/{id}）
  - 复用 Markdown 编辑器，保持一致体验

- **用户主页**
  - 完整的用户资料页面：头像、称号、简介、统计信息、最近帖子
  - 支持称号动效（discourse-custom-badge 渐变流光文字）

- **打赏能量系统**
  - 完整支持 discourse-reward 打赏能量
  - 话题和评论楼层均可打赏
  - 展示累计获赏能量

- **多样化反应**
  - 支持 discourse-reactions 帖子反应
  - 单击切换默认赞，长按弹出 7 种表情选择

- **创建话题**
  - 主页右下角新建话题按钮
  - 完整创建流程：选择节点 + 标题 + Markdown 正文

- **回复增强**
  - 回复栏支持插入 GIF（KLIPY 搜索，经站点后端代理）
  - Markdown 工具栏：粗体、斜体、标题、链接、引用、代码、列表
  - 按官网 D-Editor 行为规范实现

- **抽奖功能**
  - 完整支持 discourse-lottery 帖子内抽奖
  - 展示抽奖卡片、参与购票/随缘
  - 发起者可开奖与结束抽奖

- **小程序/小游戏**
  - 支持渲染帖子内嵌小程序/小游戏（discourse-apps）
  - 使用 WebView 加载官网自包含 webview 页面

- **内容本地化**
  - 显示帖子翻译提示条
  - 支持查看原文

### 🐛 问题修复
- 话题列表和详情页的节点标签改用官网上传的图标图片
- 抽屉用户区改用青铜/白银/黄金/钻石/王者会员称号
- 修复 TagDto 序列化器循环初始化导致主页加载崩溃
- 表单 POST 遇 403 时清空 CSRF 缓存并自动重试
- 兼容对象数组格式的话题标签
- 高级搜索筛选器改为不跨会话记忆
- 头像等站点图片显式持久缓存（内存 25% 堆 + 磁盘 64MB）

### 🔧 优化改进
- 去掉主页顶部主题筛选条，对齐官网
- 主页顶部显示签到图标+头像（已登录）或绿色登录按钮（未登录）
- 详情页底部回复完全可用：发送、失败提示、成功刷新
- 未登录时回复栏引导登录

### 🗑️ 清理
- 删除早期设计探索目录 design/（原型已不再使用）
- 忽略 Playwright MCP 调研留下的本地快照/日志目录

### 📦 CI/CD
- 支持 workflow_dispatch 手动填版本号触发部署
- APK/tag/Release 按版本号命名

---

## v0.2.0 (2024-XX-XX)

### ✨ 新增功能
- **登录系统**
  - 官网风格登录页面（两阶段 TOTP）
  - 抽屉用户区：登录入口、头像等级、登出确认
  - 会话基础设施：CookieJar 持久化 _t
  - CSRF/登录/登出/当前用户 API 与 SessionRepo

- **搜索功能**
  - 对齐官网全页搜索
  - 类型筛选、高级筛选器（分类、标签、状态、作者、时间等）
  - 排序选项、帖子模式
  - 右上角搜索接入全站搜索（防抖 + 匹配片段）

- **抽屉导航**
  - 添加原生字标加载动画
  - 分类改为真实节点列表并加载站点 logo
  - 支持 SVG 图标（coil-svg）
  - 节点改用站点上传的真实图标，缺失时回退分类色点

- **新话题提示**
  - 对齐官网新话题横幅
  - 定期检查并显示绿色横幅
  - 点击静默刷新回顶

- **动图支持**
  - 帖子流头像支持动图（官网 _2.gif 版）
  - 全局 Coil 挂载 GIF/SVG 解码

- **权限与访问控制**
  - 列表受限话题显示锁图标
  - 登录态变色：等级达标绿色 #4CAF50，未登录/不达标金色 #FF9800
  - 无权访问/不存在话题展示官网风格专用页面

### 🐛 问题修复
- 主页滑到底部自动加载下一页，失败后停止避免无限重试
- 详情页头部标题改用接口返回的最新话题名
- 加载动画按实际墨迹包围盒居中，消除水平偏移
- 自定义表情改行内 20px 不再遮挡文字
- 正文图片应用内缩放预览
- 等级限制帖渲染官网同款锁提示卡片
- 加载字标默认高度 64dp 调整为 44dp
- 列表锁图标改用官网 lc-lock 线框锁
- 顶栏 logo 按官网实测校准至 30dp/条高 52dp
- 修复分类色崩溃、身份徽章硬编码与协议相对 URL
- 修复 Compose 与 Jsoup 编译错误

### 🔧 优化改进
- 全局字号较 Material 默认下调一档，贴近官网移动端观感
- 列表标记对齐官网：置顶显示图钉，信任等级限制话题显示锁
- 对齐 Discourse 回复与主题分页
- 对齐嵌套回复与富文本渲染

---

## v0.1.0 (首个版本)

### ✨ 初始功能
- NodeLoc 话题列表浏览
- 话题详情页阅读
- 基础 Discourse API 集成
- Material 3 深色/浅色主题
- 基础 Markdown 渲染
- 图片预览功能
