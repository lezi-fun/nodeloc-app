# nodeloc-app

NodeLoc([www.nodeloc.com](https://www.nodeloc.com))的 Android 原生客户端 —— 自由、平等、友好、开放、有趣的交流社区。

> 本项目为社区驱动的第三方客户端,与 NodeLoc 官方无关;内容版权归 NodeLoc 及各原作者。

## 下载

每次推送到 main 都会自动构建并在 [Releases](https://github.com/lezi-fun/nodeloc-app/releases) 发布:
- `nodeloc-universal-<短哈希>.apk` 通用包(推荐)
- `nodeloc-arm64-v8a-*` 现代设备 · `armeabi-v7a` 老设备 · `x86_64/x86` 模拟器

## 技术栈

Kotlin 2.0 · Jetpack Compose(Material 3) · OkHttp + kotlinx.serialization · Coil · Coroutines

## 构建

```bash
./gradlew assembleRelease   # 输出于 app/build/outputs/apk/release/
```
要求 JDK 17。Release 变体复用调试签名,便于直接安装体验。

## 设计参考

- [设计画布(在线)](https://lezi-fun.github.io/nodeloc-app/design/) · 三方向可切换原型
- [品牌规格](research/brand-spec.md) / [网页版布局样式实测笔记](research/ui-layout-notes.md) / [前端镜像](mirror/)
