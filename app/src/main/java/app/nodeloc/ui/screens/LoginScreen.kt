package app.nodeloc.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.nodeloc.R
import app.nodeloc.data.ApiException
import app.nodeloc.data.SecondFactorRequiredException
import app.nodeloc.data.SessionRepo
import app.nodeloc.data.model.AuthProviderDto
import app.nodeloc.ui.components.LoadingMark
import app.nodeloc.ui.components.OAuthLoginDialog
import app.nodeloc.ui.theme.LocalNodelocColors
import kotlinx.coroutines.launch

/** 官网风格登录页:密码登录 + 两阶段 TOTP 两步验证;错误文案优先采用服务端返回 */
@Composable
fun LoginScreen(onBack: () -> Unit) {
    val nc = LocalNodelocColors.current
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var totp by rememberSaveable { mutableStateOf("") }
    var secondFactorToken by rememberSaveable { mutableStateOf<String?>(null) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    // 非空表示正在用该提供商走应用内 OAuth
    var oauthProvider by remember { mutableStateOf<AuthProviderDto?>(null) }

    // 第三方登录提供商（硬编码，因为 API 端点需要权限）
    val authProviders = remember {
        listOf(
            AuthProviderDto(name = "google_oauth2", prettyName = "Google"),
            AuthProviderDto(name = "github", prettyName = "GitHub"),
            AuthProviderDto(name = "twitter", prettyName = "X"),
            AuthProviderDto(name = "telegram", prettyName = "Telegram"),
        )
    }

    fun submit() {
        if (busy) return
        busy = true
        error = null
        scope.launch {
            try {
                SessionRepo.login(
                    login = username.trim(),
                    password = password,
                    secondFactorToken = secondFactorToken,
                    totp = totp.takeIf { it.isNotBlank() },
                )
                onBack()
                return@launch
            } catch (e: SecondFactorRequiredException) {
                secondFactorToken = e.secondFactorToken
                error = e.message
            } catch (e: ApiException) {
                error = e.message
            } catch (e: Throwable) {
                error = e.message ?: "网络错误"
            }
            busy = false
        }
    }

    Column(Modifier.fillMaxSize().background(nc.background)) {
        Row(
            Modifier.fillMaxWidth().height(56.dp).background(nc.headerBg).padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回", tint = nc.onBackground)
            }
            Text("登录", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = nc.onBackground)
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(40.dp))
            Image(
                painter = painterResource(R.drawable.nodeloc_logo),
                contentDescription = "NodeLoc",
                modifier = Modifier.height(30.dp),
            )
            Spacer(Modifier.height(32.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                singleLine = true,
                label = { Text("用户名或邮箱") },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(14.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                label = { Text("密码") },
                shape = RoundedCornerShape(14.dp),
                colors = fieldColors(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )
            // 两步验证:服务端返回 second_factor_required 后出现
            if (secondFactorToken != null) {
                Spacer(Modifier.height(14.dp))
                OutlinedTextField(
                    value = totp,
                    onValueChange = { v -> totp = v.filter(Char::isDigit).take(6) },
                    singleLine = true,
                    label = { Text("两步验证码") },
                    placeholder = { Text("6 位动态码", color = nc.onSurfaceVariant) },
                    shape = RoundedCornerShape(14.dp),
                    colors = fieldColors(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            error?.let { msg ->
                Spacer(Modifier.height(12.dp))
                Text(
                    msg,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = ::submit,
                enabled = !busy && username.isNotBlank() && password.isNotBlank() && (secondFactorToken == null || totp.length == 6),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = nc.primary, contentColor = nc.onPrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                if (busy) {
                    LoadingMark(height = 22.dp)
                } else {
                    Text("登录", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(24.dp))

            // 第三方登录分隔线
            if (authProviders.isNotEmpty()) {
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.weight(1f).height(1.dp).background(nc.outlineVariant))
                    Text(
                        "或使用第三方登录",
                        style = MaterialTheme.typography.bodySmall,
                        color = nc.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                    Box(Modifier.weight(1f).height(1.dp).background(nc.outlineVariant))
                }

                // 第三方登录按钮:在应用内 WebView 里完成授权,不跳外部浏览器
                authProviders.forEach { provider ->
                    OutlinedButton(
                        onClick = { oauthProvider = provider },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = nc.primary),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text(provider.prettyName ?: provider.title ?: provider.name)
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }

    oauthProvider?.let { provider ->
        OAuthLoginDialog(
            providerName = provider.name,
            providerLabel = provider.prettyName ?: provider.title ?: provider.name,
            onDismiss = { oauthProvider = null },
            onAuthenticated = { cookieHeader ->
                oauthProvider = null
                busy = true
                error = null
                scope.launch {
                    runCatching { SessionRepo.adoptWebViewSession(cookieHeader) }
                        .onSuccess { onBack() }
                        .onFailure { error = it.message ?: "第三方登录失败，请重试" }
                    busy = false
                }
            },
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = LocalNodelocColors.current.outlineVariant,
    focusedBorderColor = LocalNodelocColors.current.primary,
    cursorColor = LocalNodelocColors.current.primary,
    focusedLabelColor = LocalNodelocColors.current.primary,
)

