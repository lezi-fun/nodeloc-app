package app.nodeloc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.util.absoluteUrl
import app.nodeloc.util.hexColor
import coil.compose.AsyncImage

/**
 * 节点标识:官网多数节点都上传了图标(含 svg),优先展示图标图片,
 * 没有上传图标的节点(纯色圆点)才回退到分类色实心圆。
 * 抽屉里的 CategoryEntry 单独实现(带文字标签整行的 NavigationDrawerItem 结构不同),
 * 这里给列表行/详情页/编辑器节点选择器等"标签内嵌小图标"场景统一复用。
 */
@Composable
fun CategoryDot(category: CategoryDto?, size: Dp = 14.dp) {
    val logoUrl = category?.uploadedLogo?.url?.let { absoluteUrl(it, DiscourseApi.BASE) }
    if (logoUrl != null) {
        AsyncImage(
            model = logoUrl,
            contentDescription = null,
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit,
        )
    } else {
        androidx.compose.foundation.layout.Box(
            Modifier.size(size * 0.6f).background(hexColor(category?.color), CircleShape),
        )
    }
}
