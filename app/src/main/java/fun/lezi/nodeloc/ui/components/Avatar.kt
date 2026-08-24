package fun.lezi.nodeloc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlin.math.abs

private val Palette = listOf(
    Color(0xFF0088CC), Color(0xFFF1592A), Color(0xFF549447), Color(0xFF921665),
    Color(0xFFE45735), Color(0xFF652D90), Color(0xFF2CB2B5), Color(0xFFFF9933),
)

@Composable
fun Avatar(name: String, url: String?, size: Dp = 44.dp, modifier: Modifier = Modifier) {
    val c = Palette[abs(name.hashCode()) % Palette.size]
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(c.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = c,
        )
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = name,
                modifier = Modifier.size(size),
                contentScale = ContentScale.Crop,
            )
        }
    }
}