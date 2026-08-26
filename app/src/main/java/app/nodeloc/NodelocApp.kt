package app.nodeloc

import android.app.Application
import android.os.Build
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder

/**
 * 全局 Coil 配置:头像/节点 logo 需要动画 GIF 与 SVG 解码。
 * 官网头像 URL 以 .png 结尾但实际返回 image/gif(动图头像),GIF 解码按响应类型识别。
 */
class NodelocApp : Application(), ImageLoaderFactory {
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
