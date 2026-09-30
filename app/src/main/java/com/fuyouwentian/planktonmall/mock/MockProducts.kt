package com.fuyouwentian.planktonmall.mock

import com.fuyouwentian.planktonmall.domain.model.Product
import com.fuyouwentian.planktonmall.domain.model.ProductLite

object MockProducts {
    val allProducts = listOf(
        ProductLite(
            id = "6aab47cb2f18a7cf3d684206",
            name_zh = "浪琴（LONGINES）瑞士手表 名匠系列月相腕表 男士皮带机械表L29194783",
            name_en = "English name",
            icon_url = "https://liangchaoshun.site/api/static/files/1783394606367_b873295d-df72-4b68-99e5-f5ffd49dac63@icon.webp",
            series_id = "6a490daea4ccc46400d099b5",
            category_id = "6228a9bc35966be24727af36",
            price = "22200.00",
            cover = "https://liangchaoshun.site/api/static/files/1783394606324_b3069490-2978-4f26-81c4-515cf0a024bc@1.webp",
        ),
        ProductLite(
            id = "6aab47c92f18a7cf3d6841fa",
            name_zh = "程序员的自我修养 链接、装载与库",
            name_en = "English name",
            icon_url = "https://liangchaoshun.site/api/static/files/1783394605105_fe1a9d50-555f-420b-8ea6-0cbd9bbb9c31@icon.webp",
            series_id = "6295a4e065ded7dc3d78dba2",
            category_id = "6228a9bc35966be24727af34",
            price = "54.09",
            cover = "https://liangchaoshun.site/api/static/files/1783394605098_17a449ea-e645-4cae-b8f4-38b14be10808@1.webp",
        )
    )

    val ProductDetail = Product(
        id = "6aab47c02f18a7cf3d6841c4",
        name_zh = "影驰GeForce RTX 5080 金属大师 白 OC 16G GDDR7 DLSS 4.5 游戏设计OpenClaw部署NVIDIA英伟达显卡",
        name_en = "English name",
        price = "10367.90",
        home_banner = false,
        home_display = true,
        icon_url = "http://localhost:8058/api/static/files/1789609920387_15c2196a-5e50-4f2b-adf3-892649c653f4@icon.webp",
        banner_video_url = "http://localhost:8058/api/static/files/1789609920279_de19743d-3f89-4c3e-8dc5-c6bcb71e9d63@1.mp4",
        banner_model_url = "",
        series_id = "613b3da66c6f0a5d68c2bc7f",
        category_id = "613b1ee4ff64b12271bffdd3",
        desc_url = listOf(
            "http://localhost:8058/api/static/files/1789609920338_2a0c0853-45a5-4828-852e-0c27a7e371b4@1.jpg",
            "http://localhost:8058/api/static/files/1789609920342_16cc8f98-e4da-454e-b4d3-873f014c7fa2@2.jpg",
            "http://localhost:8058/api/static/files/1789609920360_e0543fe8-961f-494b-a994-70369c2b3041@3.jpg",
            "http://localhost:8058/api/static/files/1789609920340_12f3112f-e020-4534-abf2-2cf04188e7ee@4.jpg",
            "http://localhost:8058/api/static/files/1789609920345_0646e6c8-376d-482c-ad6c-8a2e5447a969@5.jpg",
            "http://localhost:8058/api/static/files/1789609920344_442c5992-ded5-4bb3-b82c-9a8d4643c262@6.jpg",
            "http://localhost:8058/api/static/files/1789609920361_72682c14-ee82-40c9-9f23-21c7b737fcc7@7.jpg",
            "http://localhost:8058/api/static/files/1789609920341_bfc713b9-079d-4ef9-a367-4c0ecdb4104a@8.jpg",
            "http://localhost:8058/api/static/files/1789609920340_b0875be4-0d36-44b0-b4b1-768f215bf77e@9.jpg",
            "http://localhost:8058/api/static/files/1789609920339_d7f8ece3-3d0b-4162-aedc-ee9a2fa70d5b@10.jpg",
            "http://localhost:8058/api/static/files/1789609920332_397eb80c-5683-488a-b8da-9513717a931b@11.jpg",
            "http://localhost:8058/api/static/files/1789609920343_bc5950a8-f84a-46d6-a20c-d47cd82667c6@12.jpg"
        ),
        banner_url = listOf(
            "http://localhost:8058/api/static/files/1789609920200_964109fa-59b7-4733-8678-5c05cc0836dc@1.webp",
            "http://localhost:8058/api/static/files/1789609920192_9ceea86e-2e64-47fd-ad0a-9c313d15a87e@2.webp",
            "http://localhost:8058/api/static/files/1789609920199_c446d354-5b1c-4a07-a7b9-92e933dfb738@3.webp",
            "http://localhost:8058/api/static/files/1789609920198_96a52d67-47cc-431e-9252-188caf80cfaf@4.webp",
            "http://localhost:8058/api/static/files/1789609920200_84cd0b77-f877-4d03-9943-84b8f12e1ae2@5.webp",
            "http://localhost:8058/api/static/files/1789609920199_518dfdbc-2141-46b6-a998-019c68322aa7@6.webp"
        ),
        create_time = "2026-09-17T01:52:00.401Z",
        update_time = "2026-09-17T01:52:00.401Z"
    )
}
