package com.fuyouwentian.planktonmall.mock

import com.fuyouwentian.planktonmall.data.model.Category
import com.fuyouwentian.planktonmall.data.model.Series

object MockCategories {
    val data: List<Category> = listOf(
        Category(
            id = "613b1ee4ff64b12271bffdd3",
            name_zh = "电脑办公",
            name_en = "Computer office work",
            desc = "电脑办公类别 desc",
            no = 1,
            create_time = "2026-07-04T09:58:24.803Z",
            update_time = "2026-07-04T09:58:24.803Z",
            series_data = listOf(
                Series(
                    id = "613b34158cccf94dfbdca450",
                    name_zh = "笔记本",
                    name_en = "Laptop",
                    desc = "电脑办公 > 笔记本",
                    no = 1,
                    icon_url =
                        "https://liangchaoshun.site/api/static/files/1783394601327_d76e5ef7-f86a-4d54-b577-00f352ace84f@笔记本.png",
                    category_id = "613b1ee4ff64b12271bffdd3",
                    create_time = "2026-07-04T13:49:40.116Z",
                    update_time = "2026-07-04T13:49:45.963Z"
                ),
                Series(
                    id = "613b3da66c6f0a5d68c2bc7f",
                    name_zh = "显卡",
                    name_en = "Graphics card",
                    desc = "电脑办公 > 显卡",
                    no = 2,
                    icon_url =
                        "https://liangchaoshun.site/api/static/files/1783394601014_b05f8244-3e2d-4083-9681-8b10a7b2497f@显卡.jpg",
                    category_id = "613b1ee4ff64b12271bffdd3",
                    create_time = "2026-07-04T13:51:40.116Z",
                    update_time = "2026-07-04T13:51:45.963Z"
                ),
                Series(
                    id = "613b3dfa6c6f0a5d68c2bc83",
                    name_zh = "一体机",
                    name_en = "All-in-one computer",
                    desc = "电脑办公 > 一体机",
                    no = 3,
                    icon_url =
                        "https://liangchaoshun.site/api/static/files/1783394600490_fbc29384-4a81-49ea-85bb-c0a47b2abd59@一体机.jpg",
                    category_id = "613b1ee4ff64b12271bffdd3",
                    create_time = "2026-07-04T13:52:15.116Z",
                    update_time = "2026-07-04T13:52:20.963Z"
                )
            )
        ),
        Category(
            id = "613b1f67ff64b12271bffddc",
            name_zh = "家用电器",
            name_en = "Home Appliances",
            desc = "家用电器类别 desc",
            no = 4,
            create_time = "2026-07-04T10:01:35.593Z",
            update_time = "2026-07-04T10:01:48.424Z",
            series_data = listOf(
                Series(
                    id = "613b41cd6c6f0a5d68c2bca3",
                    name_zh = "豆浆机",
                    name_en = "Soybean milk mechine",
                    desc = "家用电器 > 豆浆机",
                    no = 1,
                    icon_url =
                        "https://liangchaoshun.site/api/static/files/1783394602612_2aa33ead-e433-45f6-932f-e03e48c9650f@豆浆机.jpg",
                    category_id = "613b1f67ff64b12271bffddc",
                    create_time = "2026-07-04T14:00:48.407Z",
                    update_time = "2026-07-04T14:00:48.488Z"
                ),
                Series(
                    id = "613b41f06c6f0a5d68c2bca8",
                    name_zh = "微波炉",
                    name_en = "Microwave oven",
                    desc = "家用电器 > 微波炉",
                    no = 2,
                    icon_url =
                        "https://liangchaoshun.site/api/static/files/1783394602259_80327c10-fc9c-42b5-9e15-9a4b5677a9ca@微波炉.jpg",
                    category_id = "613b1f67ff64b12271bffddc",
                    create_time = "2026-07-04T14:01:45.407Z",
                    update_time = "2026-07-04T14:01:45.488Z"
                )
            )
        ),
        Category(
            id = "613cdc8491532a76854afa03",
            name_zh = "手机数码",
            name_en = "Phones & Digital",
            desc = "手机数码类别 desc",
            no = 7,
            create_time = "2026-07-04T10:03:44.340Z",
            update_time = "2026-07-04T10:03:37.474Z",
            series_data = listOf(
                Series(
                    id = "613cdf63317e00030c89ed9f",
                    name_zh = "手机",
                    name_en = "Phone",
                    icon_url =
                        "https://liangchaoshun.site/api/static/files/1783394604119_309c3618-76d6-4399-8468-0723b0bf5bca@手机.jpg",
                    no = 1,
                    desc = "手机数码 > 手机",
                    category_id = "613cdc8491532a76854afa03",
                    create_time = "2026-07-04T14:11:15.459Z",
                    update_time = "2026-07-04T14:11:15.274Z"
                ),
                Series(
                    id = "6228c29035966be24727af6f",
                    name_zh = "无人机",
                    name_en = "Drone",
                    icon_url =
                        "https://liangchaoshun.site/api/static/files/1783394604290_8cd997d8-6e5b-415c-ae0a-70998bbceeaf@无人机.jpg",
                    no = 2,
                    desc = "手机数码 > 无人机",
                    category_id = "613cdc8491532a76854afa03",
                    create_time = "2026-07-04T14:12:06.459Z",
                    update_time = "2026-07-04T14:12:06.274Z"
                )
            )
        )
    )
    val total = 0
}