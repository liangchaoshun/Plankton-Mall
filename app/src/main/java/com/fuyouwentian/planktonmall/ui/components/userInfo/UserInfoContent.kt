package com.fuyouwentian.planktonmall.ui.components.userInfo

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.fuyouwentian.planktonmall.R
import com.fuyouwentian.planktonmall.domain.model.UserProfile

@Composable
fun UserInfoContent(
    modifier: Modifier = Modifier,
    userInfo: UserProfile? = null,
    viewModel: UserInfoContentViewModel = hiltViewModel(),
) {

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.White // background
    ) {
        if (userInfo == null) {
            // 请求成功但没有数据
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无数据",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dimensionResource(R.dimen.padding_small))
                    .background(Color.White)
            ) {
                UserFieldRow(label = "账号", content = userInfo.account)
                UserFieldRow(label = "昵称", content = userInfo.nickname)
                UserFieldRow(label = "手机号", content = userInfo.phone)
                UserFieldRow(label = "邮箱", content = userInfo.email)
                UserFieldRow(label = "个性签名", content = userInfo.bio)
                UserFieldRow(label = "出生日期", content = userInfo.birthday)
                UserFieldRow(label = "个性头像", content = "未处理")
                if (userInfo.receiverInfo?.isEmpty() == true) {
                    UserFieldRow(label = "收货信息", content = "")
                } else {
                    userInfo.receiverInfo?.forEachIndexed { index, info ->
                        UserFieldRow(
                            label = "收货信息${index + 1}",
                            content = "${info.name}, ${info.phone}, ${info.address}"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(dimensionResource(R.dimen.padding_medium)))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(dimensionResource(R.dimen.padding_small)),
                    enabled = true,
                    onClick = { Log.d("UserInfoContent", "编辑信息") }
                ) {
                    Text(
                        text = "编辑个人信息",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }

    }
}

@Composable
fun UserFieldRow(
    label: String,
    content: String? = ""
) {
    Row(
        modifier = Modifier
            .padding(vertical = dimensionResource(R.dimen.padding_small)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${label}：",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = content ?: "",
            color = Color(0xFF666666),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

