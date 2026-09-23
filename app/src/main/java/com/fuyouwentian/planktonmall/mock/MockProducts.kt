package com.fuyouwentian.planktonmall.mock

import com.fuyouwentian.planktonmall.domain.model.ProductLite

object MockProducts {
    val allProducts = listOf(
        ProductLite(
            id = "6a4c712e248d023644abb907",
            name_zh = "浪琴（LONGINES）瑞士手表 名匠系列月相腕表 男士皮带机械表L29194783",
            name_en = "English name",
            icon_url = "https://liangchaoshun.site/api/static/files/1783394606367_b873295d-df72-4b68-99e5-f5ffd49dac63@icon.webp",
            series_id = "6a490daea4ccc46400d099b5",
            category_id = "6228a9bc35966be24727af36",
            cover = "https://liangchaoshun.site/api/static/files/1783394606324_b3069490-2978-4f26-81c4-515cf0a024bc@1.webp",
        ),
        ProductLite(
            id = "6a4c712d248d023644abb8f5",
            name_zh = "程序员的自我修养 链接、装载与库",
            name_en = "English name",
            icon_url = "https://liangchaoshun.site/api/static/files/1783394605105_fe1a9d50-555f-420b-8ea6-0cbd9bbb9c31@icon.webp",
            series_id = "6295a4e065ded7dc3d78dba2",
            category_id = "6228a9bc35966be24727af34",
            cover = "https://liangchaoshun.site/api/static/files/1783394605098_17a449ea-e645-4cae-b8f4-38b14be10808@1.webp",
        )
    )
}
