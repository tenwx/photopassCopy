package com.pictureair.photopass.entity;

import java.util.List;

/**
 * Created by milo on 15/12/13.
 */
public class GoodsInfoJson {
    List<GoodsInfo1> goods;

    public List<GoodsInfo1> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsInfo1> goods) {
        this.goods = goods;
    }

    public static String getString(){
        String testData = "{\n" +
                "  \"goods\": [\n" +
                "    {\n" +
                "      \"productType\": \"photoPassPlus\",\n" +
                "      \"goodsKey\": \"6234f4deaa464c3be9968f533ad75c4a\",\n" +
                "      \"nameAlias\": \"PhotoPass+\",\n" +
                "      \"promotionInfo\": {},\n" +
                "      \"storeId\": \"54b4a700155da2bf0e141bbe\",\n" +
                "      \"pictures\": [\n" +
                "        {\n" +
                "          \"no\": 0,\n" +
                "          \"url\": \"/productImage/gift-photopassplus.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee1a\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"no\": 1,\n" +
                "          \"url\": \"/productImage/gift-photopassplus2.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee19\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"descriptionLanguages\": \"PictureWorks 公司自营产品,用于合作的园区的photoPass升级\",\n" +
                "      \"price\": 369,\n" +
                "      \"description\": \"PictureWorks 公司自营产品,用于合作的园区的photoPass升级\",\n" +
                "      \"other\": {},\n" +
                "      \"productId\": \"565fcdcbab8fb0e96a83ee17\",\n" +
                "      \"name\": \"PhotoPass+\",\n" +
                "      \"nameAliasLanguages\": \"PhotoPass+\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"productType\": \"virtul file\",\n" +
                "      \"goodsKey\": \"3213d8de560c93e2994cf4f0460c8807\",\n" +
                "      \"nameAlias\": \"Single Digital\",\n" +
                "      \"promotionInfo\": {},\n" +
                "      \"storeId\": \"54b4a700155da2bf0e141bbe\",\n" +
                "      \"pictures\": [\n" +
                "        {\n" +
                "          \"no\": 0,\n" +
                "          \"url\": \"/productImage/gift-singleDigital.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee1f\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"no\": 1,\n" +
                "          \"url\": \"/productImage/gift-singleDigital2.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee1e\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"descriptionLanguages\": \"数码照片文件\",\n" +
                "      \"sequence\": 3,\n" +
                "      \"price\": 59,\n" +
                "      \"description\": \"数码照片文件\",\n" +
                "      \"other\": {},\n" +
                "      \"productId\": \"565fcdcbab8fb0e96a83ee1c\",\n" +
                "      \"name\": \"Single Digital\",\n" +
                "      \"nameAliasLanguages\": \"Single Digital\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"productType\": \"service\",\n" +
                "      \"goodsKey\": \"82864d5647f5ea28c19503fe2070e404\",\n" +
                "      \"nameAlias\": \"6R Print (6\\\"x 8\\\")\",\n" +
                "      \"promotionInfo\": {},\n" +
                "      \"storeId\": \"54b4a700155da2bf0e141bbe\",\n" +
                "      \"pictures\": [\n" +
                "        {\n" +
                "          \"no\": 0,\n" +
                "          \"url\": \"/productImage/gift-6r.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee24\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"no\": 1,\n" +
                "          \"url\": \"/productImage/gift-6r2.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee23\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"descriptionLanguages\": \"打印6R照片\",\n" +
                "      \"sequence\": 5,\n" +
                "      \"price\": 99,\n" +
                "      \"description\": \"打印6R照片\",\n" +
                "      \"other\": {},\n" +
                "      \"productId\": \"565fcdcbab8fb0e96a83ee21\",\n" +
                "      \"name\": \"6R Print\",\n" +
                "      \"nameAliasLanguages\": \"6R Print\"\n" +
                "    },\n" +
                "    {\n" +
                "      \"productType\": \"Duffy t-shirt\",\n" +
                "      \"goodsKey\": \"44d2a0320318f5a6a8ae7d30f90aaa56\",\n" +
                "      \"nameAlias\": \"Duffy T-shirt\",\n" +
                "      \"promotionInfo\": {},\n" +
                "      \"storeId\": \"54b4a700155da2bf0e141bbe\",\n" +
                "      \"pictures\": [\n" +
                "        {\n" +
                "          \"no\": 0,\n" +
                "          \"url\": \"/productImage/gift-tshirt.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee2c\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"no\": 1,\n" +
                "          \"url\": \"/productImage/gift-tshirt2.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee2b\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"descriptionLanguages\": \"It is a precious memory when you take a look the duffy T-shirt that had printed your personal happy photos with your family \",\n" +
                "      \"sequence\": 10,\n" +
                "      \"price\": 288,\n" +
                "      \"description\": \"It is a precious memory when you take a look the duffy T-shirt that had printed your personal happy photos with your family \",\n" +
                "      \"other\": {},\n" +
                "      \"productId\": \"565fcdcbab8fb0e96a83ee29\",\n" +
                "      \"name\": \"Duffy T-shirt\",\n" +
                "      \"nameAliasLanguages\": {\n" +
                "        \"en\": \"Duffy T-shirt\",\n" +
                "        \"zh\": \"Duffy 可爱人像T恤 \"\n" +
                "      }\n" +
                "    },\n" +
                "    {\n" +
                "      \"productType\": \"cookies\",\n" +
                "      \"goodsKey\": \"097e4bde6c7a5498f76fcdb2bf933371\",\n" +
                "      \"nameAlias\": \"Your personal cookies gift\",\n" +
                "      \"promotionInfo\": {},\n" +
                "      \"storeId\": \"54b4a700155da2bf0e141bbe\",\n" +
                "      \"pictures\": [\n" +
                "        {\n" +
                "          \"no\": 0,\n" +
                "          \"url\": \"/productImage/gift-cookies.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee31\"\n" +
                "        },\n" +
                "        {\n" +
                "          \"no\": 1,\n" +
                "          \"url\": \"/productImage/gift-cookies2.jpg\",\n" +
                "          \"_id\": \"565fcdcbab8fb0e96a83ee30\"\n" +
                "        }\n" +
                "      ],\n" +
                "      \"descriptionLanguages\": \"Create your personal cookies gift , just like beautiful memory is in your life .\",\n" +
                "      \"sequence\": 11,\n" +
                "      \"price\": 688,\n" +
                "      \"description\": \"Create your personal cookies gift , just like beautiful memory is in your life .\",\n" +
                "      \"other\": {},\n" +
                "      \"productId\": \"565fcdcbab8fb0e96a83ee2e\",\n" +
                "      \"name\": \"Your personal cookies gift\",\n" +
                "      \"nameAliasLanguages\": \"Your personal cookies gift\"\n" +
                "    }\n" +
                "  ]\n" +
                "}";
        return testData;
    }
}
