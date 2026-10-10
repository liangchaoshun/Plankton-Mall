package com.fuyouwentian.planktonmall.ui.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.UiEvent

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToRegister: () -> Unit = {},
    onLoginSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast
                        .makeText(
                            context, event.message,
                            Toast.LENGTH_SHORT
                        )
                        .show()
                }
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White // background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // 标题
            Text(
                text = "登录",
                fontSize = 36.sp,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "请登录以获得独家XX服务",
                fontSize = 14.sp,
                color = Color(0xFF999999)
            )
            Spacer(modifier = Modifier.height(48.dp))
            // 账号
            LoginTextField(
                value = uiState.account,
                onValueChange = viewModel::onAccountChange,
                placeholder = "账号",
                doLogin = viewModel::login
            )
            Spacer(modifier = Modifier.height(16.dp))
            // 密码
            var passwordVisible by remember { mutableStateOf(false) }
            LoginTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "密码",
                doLogin = viewModel::login,
                isPassword = true,
                isPasswordVisible = passwordVisible,
                onVisibilityChange = { passwordVisible = !passwordVisible }
            )
            Spacer(modifier = Modifier.height(16.dp))
            // 记住我
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = uiState.rememberMe,
                        onCheckedChange = viewModel::switchRememberMe,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF5E5CE6), // 勾选后的填充色
                            uncheckedColor = Color(0xFF5E5CE6), // 未勾选时的边框色
                            checkmarkColor = Color.White, // 勾选后中间那个对勾的颜色
                        )
                    )
                    Text(
                        text = "记住我",
                        fontSize = 14.sp,
                        color = Color(0xFF5E5CE6)
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "忘记密码",
                        fontSize = 14.sp,
                        color = Color(0xFF5E5CE6)
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = viewModel::login,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF5E5CE6)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !uiState.loading
            ) {
                if (uiState.loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "登录",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(modifier = Modifier.weight(0.7f))
            // 第三方登录
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
                Text(
                    text = "第三方账号登录",
                    modifier = Modifier.padding(horizontal = 16.dp),
                    fontSize = 16.sp,
                    color = Color(0xFF666666)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFEEEEEE))
            }

            Spacer(modifier = Modifier.weight(0.3f))

            // 第三方登录
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // QQ
                SocialLoginIcon(
                    color = Color(0xFF4D96FF),
                    iconRes = R.drawable.ic_qq
                )
                Spacer(modifier = Modifier.width(32.dp))
                // WeChat
                SocialLoginIcon(
                    color = Color(0xFF52C41A),
                    iconRes = R.drawable.ic_wechat
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // 注册入口
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "还没有账号？",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                TextButton(
                    onClick = onNavigateToRegister,
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = "注册",
                        fontSize = 14.sp,
                        color = Color(0xFF5E5CE6),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    doLogin: () -> Unit,
    isPassword: Boolean = false,
    isPasswordVisible: Boolean = false,
    onVisibilityChange: (() -> Unit)? = null,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp)),
        placeholder = {
            Text(text = placeholder, color = Color(0xFFAAAAAA))
        },
        colors = TextFieldDefaults.colors(
            // 背景色（浅灰）
            focusedContainerColor = Color(0xFFF5F7FA),
            unfocusedContainerColor = Color(0xFFF5F7FA),
            disabledContainerColor = Color(0xFFF5F7FA),
            // 底部指示线
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            // 光标颜色
            cursorColor = Color(0xFF5E5CE6),
            // 文字颜色
            focusedTextColor = Color(0xFF333333),
            unfocusedTextColor = Color(0xFF333333),
        ),
        shape = RoundedCornerShape(8.dp),
        singleLine = true,
        visualTransformation = if (isPassword && !isPasswordVisible)
            PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { doLogin() }
        ),
        trailingIcon = if (isPassword) {
            {
                IconButton(onClick = { onVisibilityChange?.invoke() }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle Password Visibility",
                        tint = Color(0xFF999999)
                    )
                }
            }
        } else null,
    )
}

@Composable
fun SocialLoginIcon(
    color: Color,
    iconRes: Int, // drawable 资源 id
    onClickIcon: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(color)
            .clickable { onClickIcon() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = Color.White, // 图标填充白色
            modifier = Modifier.size(30.dp)
        )
    }
}

