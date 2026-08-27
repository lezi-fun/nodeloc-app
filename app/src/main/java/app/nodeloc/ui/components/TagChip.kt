package app.nodeloc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nodeloc.ui.theme.LocalNodelocColors

/**
 * 官网 .discourse-tag.box 样式的话题标签胶囊:
 * 浅灰底(暗色取略亮于底色的面板色)+ 灰字 + 全圆角。
 */
@Composable
fun TagChip(name: String) {
    val nc = LocalNodelocColors.current
    val light = nc.background.luminance() > 0.5f
    Box(
        Modifier
            .background(
                if (light) Color(0xFFF8F8F8) else nc.surfaceVariant,
                RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 7.dp, vertical = 1.5.dp),
    ) {
        Text(
            name,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
            fontWeight = FontWeight.Normal,
            color = if (light) Color(0xFF919191) else nc.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
