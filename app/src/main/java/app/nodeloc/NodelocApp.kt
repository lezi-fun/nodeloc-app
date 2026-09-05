package app.nodeloc

import android.app.Application
import android.os.Build
import app.nodeloc.data.GithubReleaseChecker
import app.nodeloc.data.NotificationSync
import app.nodeloc.data.SessionRepo
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 全局初始化:Coil 挂动画 GIF 与 SVG 解码(官网动图头像 URL 以 .png 结尾但内容为 image/gif);
 * 会话层恢复持久化登录态并异步校验。
 */
class NodelocApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        NotificationSync.createChannel(this)
        SessionRepo.init(this)
        NotificationSync.schedulePeriodic(this)
        CoroutineScope(Dispatchers.IO).launch {
            SessionRepo.refresh()
            NotificationSync.check(this@NodelocApp)
        }
        CoroutineScope(Dispatchers.IO).launch { GithubReleaseChecker.check() }
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            // 头像等站点图片持久缓存:内存 25% 可用堆 + 磁盘 64MB,冷启动直接命中
            .memoryCache {
                coil.memory.MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .build()
}
