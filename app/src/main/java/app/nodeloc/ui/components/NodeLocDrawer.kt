package app.nodeloc.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.nodeloc.R
import app.nodeloc.data.DiscourseApi
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.ui.theme.LocalNodelocColors
import app.nodeloc.util.absoluteUrl
import app.nodeloc.util.hexColor
import coil.compose.AsyncImage

/** 抽屉展示的节点(分类),与官网侧栏主节点一致 */
private val drawerCategories = listOf("互联网服务", "科技与创作", "数码与硬件", "生活与兴趣", "活动与互动")

@Composable
fun NodeLocDrawer(onClose: () -> Unit) {
    val nc = LocalNodelocColors.current
    var cats by remember { mutableStateOf<Map<Int, CategoryDto>>(emptyMap()) }
    LaunchedEffect(Unit) {
        cats = runCatching { SiteRepo.categories() }.getOrDefault(emptyMap())
    }

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.86f),
        drawerContainerColor = nc.surface,
        drawerContentColor = nc.onSurface,
    ) {
        Column(
            Modifier.fillMaxHeight().verticalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(R.drawable.nodeloc_logo),
                    contentDescription = "NodeLoc",
                    modifier = Modifier.size(width = 96.dp, height = 28.dp),
                )
            }
            DrawerSectionTitle("社区")
            NavigationDrawerItem(
                label = { Text("话题", fontWeight = FontWeight.Bold) },
                selected = true,
                onClick = onClose,
                icon = { androidx.compose.material3.Icon(Icons.Filled.Home, contentDescription = null) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = nc.secondaryContainer,
                    selectedTextColor = nc.onSecondaryContainer,
                    selectedIconColor = nc.primary,
                ),
            )
            DrawerEntry("更多", Icons.Filled.MoreVert, onClose)
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = nc.outlineVariant)

            DrawerSectionTitle("资源")
            DrawerEntry("关于 NodeLoc", Icons.Filled.Info, onClose)
            DrawerEntry("常见问题", Icons.Filled.Info, onClose)
            DrawerEntry("开放登录", Icons.Filled.Info, onClose)

            DrawerSectionTitle("分类")
            drawerCategories.forEach { name ->
                val cat = cats.values.firstOrNull { it.name == name }
                CategoryEntry(
                    text = name,
                    color = cat?.let { hexColor(it.color) } ?: Color.Gray,
                    logoUrl = cat?.uploadedLogo?.url?.let { absoluteUrl(it, DiscourseApi.BASE) },
                    onClick = onClose,
                )
            }

            DrawerSectionTitle("标签")
            DrawerEntry("AI", Icons.Filled.Info, onClose)
            DrawerEntry("VPS", Icons.Filled.Info, onClose)
            DrawerEntry("所有标签", Icons.Filled.Info, onClose)
        }
    }
}

@Composable
private fun DrawerSectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = LocalNodelocColors.current.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 10.dp, bottom = 4.dp),
    )
}

@Composable
private fun DrawerEntry(text: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(text) },
        selected = false,
        onClick = onClick,
        icon = { androidx.compose.material3.Icon(icon, contentDescription = null) },
    )
}

/** 节点条目:优先显示站点上传的分类图标,缺失时回退到分类色点 */
@Composable
private fun CategoryEntry(text: String, color: Color, logoUrl: String?, onClick: () -> Unit) {
    val nc = LocalNodelocColors.current
    NavigationDrawerItem(
        label = { Text(text) },
        selected = false,
        onClick = onClick,
        icon = {
            if (logoUrl != null) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(26.dp),
                    contentScale = ContentScale.Fit,
                )
            } else {
                Box(Modifier.size(10.dp).background(color, CircleShape))
            }
        },
        colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = nc.onSurfaceVariant),
    )
}
