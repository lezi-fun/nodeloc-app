package app.nodeloc.ui.screens

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.nodeloc.data.DiscourseApi
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()

    var postSourceLevel by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // 加载当前设置
    LaunchedEffect(Unit) {
        loading = true
        runCatching {
            DiscourseApi.getPostSourceLevel()
        }.onSuccess { level ->
            postSourceLevel = level
            loading = false
        }.onFailure { error ->
            errorMessage = error.message
            loading = false
        }
    }

    // 保存设置
    fun savePostSourceLevel(level: Int) {
        scope.launch {
            saving = true
            errorMessage = null
            runCatching {
                DiscourseApi.setPostSourceLevel(level)
            }.onSuccess {
                postSourceLevel = level
                saving = false
            }.onFailure { error ->
                errorMessage = error.message ?: "保存失败"
                saving = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = nc.surface,
                    titleContentColor = nc.onSurface,
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(nc.background)
                .verticalScroll(rememberScrollState())
        ) {
            if (loading) {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // 发帖来源设置
                SettingsSection(title = "发帖来源") {
                    Text(
                        "选择帖子显示设备信息的程度",
                        style = MaterialTheme.typography.bodySmall,
                        color = nc.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    val options = listOf(
                        0 to "不显示",
                        1 to "仅显示客户端（NodeLoc Android）",
                        2 to "显示客户端与平台（NodeLoc Android）",
                        3 to "显示到手机品牌（NodeLoc Android - ${Build.MANUFACTURER}）",
                        4 to "显示到手机型号（NodeLoc Android - ${Build.MANUFACTURER} ${Build.MODEL}）"
                    )

                    options.forEach { (level, description) ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !saving) { savePostSourceLevel(level) },
                            color = nc.surface
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = postSourceLevel == level,
                                    onClick = { if (!saving) savePostSourceLevel(level) },
                                    enabled = !saving
                                )
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = nc.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        HorizontalDivider(color = nc.outlineVariant)
                    }

                    Text(
                        "提示：来源信息可以由任何人自定义，仅作装饰使用，不代表任何权限或认证",
                        style = MaterialTheme.typography.bodySmall,
                        color = nc.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // 错误提示
                errorMessage?.let { error ->
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                // 保存中提示
                if (saving) {
                    Row(
                        Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("保存中...", style = MaterialTheme.typography.bodySmall)
                    }
                }

                // 账号设置
                SettingsSection(title = "账号") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLogoutDialog = true },
                        color = nc.surface
                    ) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ExitToApp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "退出登录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }

    // 退出登录确认对话框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            runCatching {
                                app.nodeloc.data.SessionStore.clear()
                            }
                            onLogout()
                        }
                    }
                ) {
                    Text("退出", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    val nc = LocalNodelocColors.current
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = nc.primary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Surface(
            color = nc.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(content = content)
        }
    }
}
