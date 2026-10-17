package com.fuyouwentian.planktonmall.mock

import com.fuyouwentian.planktonmall.domain.model.Category
import com.fuyouwentian.planktonmall.domain.model.Series

object MockCategories {
    val data: List<Category> = listOf(
        Category(
            id = "613b1ee4ff64b12271bffdd3",
            nameZh = "电脑办公",
            nameEn = "Computer office work",
            desc = "电脑办公类别 desc",
            no = 1,
            createTime = "2026-07-04T09:58:24.803Z",
            updateTime = "2026-07-04T09:58:24.803Z",
            seriesData = listOf(
                Series(
                    id = "613b34158cccf94dfbdca450",
                    nameZh = "笔记本",
                    nameEn = "Laptop",
                    desc = "电脑办公 > 笔记本",
                    no = 1,
                    iconUrl =
                        "https://liangchaoshun.site/api/static/files/1783394601327_d76e5ef7-f86a-4d54-b577-00f352ace84f@bijiben.png",
                    categoryId = "613b1ee4ff64b12271bffdd3",
                    createTime = "2026-07-04T13:49:40.116Z",
                    updateTime = "2026-07-04T13:49:45.963Z"
                ),
                Series(
                    id = "613b3da66c6f0a5d68c2bc7f",
                    nameZh = "显卡",
                    nameEn = "Graphics card",
                    desc = "电脑办公 > 显卡",
                    no = 2,
                    iconUrl =
                        "https://liangchaoshun.site/api/static/files/1783394601014_b05f8244-3e2d-4083-9681-8b10a7b2497f@xianka.jpg",
                    categoryId = "613b1ee4ff64b12271bffdd3",
                    createTime = "2026-07-04T13:51:40.116Z",
                    updateTime = "2026-07-04T13:51:45.963Z"
                ),
                Series(
                    id = "613b3dfa6c6f0a5d68c2bc83",
                    nameZh = "一体机",
                    nameEn = "All-in-one computer",
                    desc = "电脑办公 > 一体机",
                    no = 3,
                    iconUrl =
                        "https://liangchaoshun.site/api/static/files/1783394600490_fbc29384-4a81-49ea-85bb-c0a47b2abd59@yitiji.jpg",
                    categoryId = "613b1ee4ff64b12271bffdd3",
                    createTime = "2026-07-04T13:52:15.116Z",
                    updateTime = "2026-07-04T13:52:20.963Z"
                )
            )
        ),
        Category(
            id = "613b1f67ff64b12271bffddc",
            nameZh = "家用电器",
            nameEn = "Home Appliances",
            desc = "家用电器类别 desc",
            no = 4,
            createTime = "2026-07-04T10:01:35.593Z",
            updateTime = "2026-07-04T10:01:48.424Z",
            seriesData = listOf(
                Series(
                    id = "613b41cd6c6f0a5d68c2bca3",
                    nameZh = "豆浆机",
                    nameEn = "Soybean milk mechine",
                    desc = "家用电器 > 豆浆机",
                    no = 1,
                    iconUrl =
                        "https://liangchaoshun.site/api/static/files/1783394602612_2aa33ead-e433-45f6-932f-e03e48c9650f@doujiangji.jpg",
                    categoryId = "613b1f67ff64b12271bffddc",
                    createTime = "2026-07-04T14:00:48.407Z",
                    updateTime = "2026-07-04T14:00:48.488Z"
                ),
                Series(
                    id = "613b41f06c6f0a5d68c2bca8",
                    nameZh = "微波炉",
                    nameEn = "Microwave oven",
                    desc = "家用电器 > 微波炉",
                    no = 2,
                    iconUrl =
                        "https://liangchaoshun.site/api/static/files/1783394602259_80327c10-fc9c-42b5-9e15-9a4b5677a9ca@weibolu.jpg",
                    categoryId = "613b1f67ff64b12271bffddc",
                    createTime = "2026-07-04T14:01:45.407Z",
                    updateTime = "2026-07-04T14:01:45.488Z"
                )
            )
        ),
        Category(
            id = "613cdc8491532a76854afa03",
            nameZh = "手机数码",
            nameEn = "Phones & Digital",
            desc = "手机数码类别 desc",
            no = 7,
            createTime = "2026-07-04T10:03:44.340Z",
            updateTime = "2026-07-04T10:03:37.474Z",
            seriesData = listOf(
                Series(
                    id = "613cdf63317e00030c89ed9f",
                    nameZh = "手机",
                    nameEn = "Phone",
                    iconUrl =
                        "https://liangchaoshun.site/api/static/files/1783394604119_309c3618-76d6-4399-8468-0723b0bf5bca@shouji.jpg",
                    no = 1,
                    desc = "手机数码 > 手机",
                    categoryId = "613cdc8491532a76854afa03",
                    createTime = "2026-07-04T14:11:15.459Z",
                    updateTime = "2026-07-04T14:11:15.274Z"
                ),
                Series(
                    id = "6228c29035966be24727af6f",
                    nameZh = "无人机",
                    nameEn = "Drone",
                    iconUrl =
                        "https://liangchaoshun.site/api/static/files/1783394604290_8cd997d8-6e5b-415c-ae0a-70998bbceeaf@wurenji.jpg",
                    no = 2,
                    desc = "手机数码 > 无人机",
                    categoryId = "613cdc8491532a76854afa03",
                    createTime = "2026-07-04T14:12:06.459Z",
                    updateTime = "2026-07-04T14:12:06.274Z"
                )
            )
        )
    )
    val total = 3
}