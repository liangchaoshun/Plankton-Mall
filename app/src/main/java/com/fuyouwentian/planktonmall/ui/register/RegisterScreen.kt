package com.fuyouwentian.planktonmall.ui.register

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
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
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: RegisterViewModel = hiltViewModel(),
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is UiEvent.RegisterSuccess -> {
                    Toast.makeText(context, "注册成功", Toast.LENGTH_SHORT).show()
                    onRegisterSuccess()
                }

                else -> {}
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 背景图
        Image(
            painter = painterResource(id = R.drawable.login_register_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 30.dp), // 模糊图片
            contentScale = ContentScale.Crop
        )

        // 半透明遮罩
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x33FFFFFF))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.Start
        ) {
            // 标题
            Text(
                text = "注册",
                color = Color(0xFF333333),
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(modifier = Modifier.height(80.dp))
            // 账号
            RegisterTextField(
                value = uiState.account,
                onValueChange = viewModel::onAccountChange,
                placeholder = "账号",
                doRegister = viewModel::register
            )
            Spacer(modifier = Modifier.height(16.dp))
            // 密码
            var passwordVisible by remember { mutableStateOf(false) }
            RegisterTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "密码",
                doRegister = viewModel::register,
                isPassword = true,
                isPasswordVisible = passwordVisible,
                onVisibilityChange = { passwordVisible = !passwordVisible }
            )
            Spacer(modifier = Modifier.height(48.dp))
            // 注册按钮
            Button(
                onClick = viewModel::register,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                enabled = !uiState.commiting
            ) {
                if (uiState.commiting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "注册",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 注册入口
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "已有账号？",
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                TextButton(
                    onClick = onNavigateToLogin,
                    contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                    modifier = Modifier.height(24.dp)
                ) {
                    Text(
                        text = "去登录",
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
fun RegisterTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    doRegister: () -> Unit,
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
            onDone = { doRegister() }
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
