package app.nodeloc

import android.app.Application
import android.os.Build
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
        SessionRepo.init(this)
        CoroutineScope(Dispatchers.IO).launch { SessionRepo.refresh() }
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
            .build()
}
