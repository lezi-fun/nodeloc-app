package app.nodeloc.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import app.nodeloc.data.SessionRepo
import app.nodeloc.data.SiteRepo
import app.nodeloc.data.model.CategoryDto
import app.nodeloc.data.model.RecentAppDto
import app.nodeloc.data.model.TagDto
import app.nodeloc.ui.theme.LocalNodelocColors
import app.nodeloc.util.absoluteUrl
import app.nodeloc.util.hexColor
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

private val topicIdInUrl = Regex("""/t/[^/]+/(\d+)""")

/**
 * 抽屉内容跟随服务端配置:节点/标签取自当前用户的 sidebar_category_ids / sidebar_tags
 * (与官网侧栏一致,登录用户在官网侧栏设置里选的是哪些,这里就显示哪些),未登录或用户
 * 没有自定义标签时回退到站点默认(有图标的节点 / 站点热门标签)。
 */
@Composable
fun NodeLocDrawer(onClose: () -> Unit, onOpenLogin: () -> Unit = {}, onOpenTopicId: (Long) -> Unit = {}, onOpenProfile: (String) -> Unit = {}) {
    val nc = LocalNodelocColors.current
    // svg 解码由全局 ImageLoader(NodelocApp)提供
    var nodes by remember { mutableStateOf<List<CategoryDto>>(emptyList()) }
    var tags by remember { mutableStateOf<List<TagDto>>(emptyList()) }
    var apps by remember { mutableStateOf<List<RecentAppDto>>(emptyList()) }
    val me by SessionRepo.currentUser.collectAsState()
    var showLogout by remember { mutableStateOf(false) }
    var showAllApps by remember { mutableStateOf(false) }
    var showAllNodes by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(me?.id) {
        val allCategories = runCatching { SiteRepo.categories().values.toList() }.getOrDefault(emptyList())
        val sidebarIds = me?.sidebarCategoryIds.orEmpty()
        nodes = if (sidebarIds.isNotEmpty()) {
            allCategories.filter { it.id in sidebarIds }.sortedBy { it.position }
        } else {
            allCategories.filter { !it.uploadedLogo?.url.isNullOrBlank() }.sortedBy { it.position }.take(14)
        }
        val sidebarTagNames = me?.sidebarTags.orEmpty()
        tags = if (sidebarTagNames.isNotEmpty()) {
            sidebarTagNames.map { TagDto(name = it, slug = it) }
        } else {
            runCatching { SiteRepo.topTags() }.getOrDefault(emptyList()).take(5)
        }
        apps = if (me?.recentApps.orEmpty().isNotEmpty()) me?.recentApps.orEmpty() else runCatching { SiteRepo.popularApps() }.getOrDefault(emptyList())
    }

    fun openApp(app: RecentAppDto) {
        topicIdInUrl.find(app.url)?.groupValues?.get(1)?.toLongOrNull()?.let { id ->
            onClose()
            onOpenTopicId(id)
        }
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
            // 用户区:未登录显示登录入口,已登录显示头像/用户名/信任等级
            val user = me
            if (user == null) {
                NavigationDrawerItem(
                    label = { Text("登录", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = {
                        scope.launch { onClose() }
                        onOpenLogin()
                    },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedContainerColor = nc.secondaryContainer,
                        unselectedTextColor = nc.primary,
                        unselectedIconColor = nc.primary,
                    ),
                )
            } else {
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text(user.username, fontWeight = FontWeight.Bold)
                            Text(
                                app.nodeloc.util.TrustLevelNames.displayName(user.trustLevel) +
                                    if (user.admin) " · 管理员" else if (user.moderator) " · 版主" else "",
                                style = MaterialTheme.typography.labelSmall,
                                color = nc.onSurfaceVariant,
                            )
                        }
                    },
                    selected = false,
                    onClick = {
                        scope.launch { onClose() }
                        onOpenProfile(user.username)
                    },
                    icon = {
                        Avatar(user.username, SiteRepo.avatarUrl(user.avatarTemplate), size = 36.dp)
                    },
                )
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = nc.outlineVariant)
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

            if (apps.isNotEmpty()) {
                DrawerSectionTitle("小程序")
                apps.take(if (showAllApps) apps.size else 5).forEach { app ->
                    CategoryEntry(
                        text = app.name,
                        color = nc.primary,
                        logoUrl = app.logoUrl?.let { absoluteUrl(it, DiscourseApi.BASE) },
                        onClick = { openApp(app) },
                    )
                }
                if (apps.size > 5) {
                    DrawerEntry(if (showAllApps) "收起" else "查看更多", Icons.Filled.MoreVert) { showAllApps = !showAllApps }
                }
            }

            DrawerSectionTitle("节点")
            val visibleNodes = nodes.take(if (showAllNodes) nodes.size else 14)
            visibleNodes.forEach { cat ->
                CategoryEntry(
                    text = cat.name,
                    color = hexColor(cat.color),
                    logoUrl = cat.uploadedLogo?.url?.let { absoluteUrl(it, DiscourseApi.BASE) },
                    onClick = onClose,
                )
            }
            if (nodes.size > 14) {
                DrawerEntry(if (showAllNodes) "收起" else "查看更多", Icons.Filled.MoreVert) { showAllNodes = !showAllNodes }
            }

            if (tags.isNotEmpty()) {
                DrawerSectionTitle("标签")
                tags.forEach { tag ->
                    DrawerEntry(tag.name, Icons.Filled.Info, onClose)
                }
                DrawerEntry("所有标签", Icons.Filled.Info, onClose)
            }
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogout = false
                        scope.launch {
                            SessionRepo.logout()
                            onClose()
                        }
                    },
                ) { Text("退出", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showLogout = false }) { Text("取消") }
            },
        )
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

/** 节点条目:显示站点上传的分类图标(含 svg),缺失时回退到分类色点 */
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
                Icon(Icons.Filled.Forum, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
        },
        colors = NavigationDrawerItemDefaults.colors(unselectedIconColor = nc.onSurfaceVariant),
    )
}
