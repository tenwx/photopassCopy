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
        String testData = "{\"goods\":[{\"productType\":\"photoPassPlus\",\"goodsKey\":\"6234f4deaa464c3be9968f533ad75c4a\",\"nameAlias\":\"PhotoPass+\",\"promotionInfo\":{},\"storeId\":\"54b4a700155da2bf0e141bbe\",\"prictures\":[{\"no\":0,\"url\":\"/productImage/gift-photopassplus.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee1a\"},{\"no\":1,\"url\":\"/productImage/gift-photopassplus2.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee19\"}],\"descriptionLanguages\":\"PictureWorks 公司自营产品,用于合作的园区的photoPass升级\",\"price\":369,\"description\":\"PictureWorks 公司自营产品,用于合作的园区的photoPass升级\",\"other\":{},\"productId\":\"565fcdcbab8fb0e96a83ee17\",\"name\":\"PhotoPass+\",\"nameAliasLanguages\":\"PhotoPass+\"},{\"productType\":\"virtul file\",\"goodsKey\":\"3213d8de560c93e2994cf4f0460c8807\",\"nameAlias\":\"single Digital\",\"promotionInfo\":{},\"storeId\":\"54b4a700155da2bf0e141bbe\",\"prictures\":[{\"no\":0,\"url\":\"/productImage/gift-singleDigital.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee1f\"},{\"no\":1,\"url\":\"/productImage/gift-singleDigital2.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee1e\"}],\"descriptionLanguages\":\"数码照片文件\",\"sequence\":3,\"price\":59,\"description\":\"数码照片文件\",\"other\":{},\"productId\":\"565fcdcbab8fb0e96a83ee1c\",\"name\":\"Single Digital\",\"nameAliasLanguages\":\"Single Digital\"},{\"productType\":\"service\",\"goodsKey\":\"82864d5647f5ea28c19503fe2070e404\",\"nameAlias\":\"6R Print (6\\\"x 8\\\")\",\"promotionInfo\":{},\"storeId\":\"54b4a700155da2bf0e141bbe\",\"prictures\":[{\"no\":0,\"url\":\"/productImage/gift-6r.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee24\"},{\"no\":1,\"url\":\"/productImage/gift-6r2.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee23\"}],\"descriptionLanguages\":\"打印6R照片\",\"sequence\":5,\"price\":99,\"description\":\"打印6R照片\",\"other\":{},\"productId\":\"565fcdcbab8fb0e96a83ee21\",\"name\":\"6R Print\",\"nameAliasLanguages\":\"6R Print\"},{\"productType\":\"Duffy t-shirt\",\"goodsKey\":\"44d2a0320318f5a6a8ae7d30f90aaa56\",\"nameAlias\":\"Duffy T-shirt\",\"promotionInfo\":{},\"storeId\":\"54b4a700155da2bf0e141bbe\",\"prictures\":[{\"no\":0,\"url\":\"/productImage/gift-tshirt.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee2c\"},{\"no\":1,\"url\":\"/productImage/gift-tshirt2.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee2b\"}],\"descriptionLanguages\":\"It is a precious memory when you take a look the duffy T-shirt that had printed your personal happy photos with your family \",\"sequence\":10,\"price\":288,\"description\":\"It is a precious memory when you take a look the duffy T-shirt that had printed your personal happy photos with your family \",\"other\":{},\"productId\":\"565fcdcbab8fb0e96a83ee29\",\"name\":\"Duffy T-shirt\",\"nameAliasLanguages\":{\"en\":\"Duffy T-shirt\",\"zh\":\"Duffy 可爱人像T恤 \"}},{\"productType\":\"cookies\",\"goodsKey\":\"097e4bde6c7a5498f76fcdb2bf933371\",\"nameAlias\":\"Your personal cookies gift\",\"promotionInfo\":{},\"storeId\":\"54b4a700155da2bf0e141bbe\",\"prictures\":[{\"no\":0,\"url\":\"/productImage/gift-cookies.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee31\"},{\"no\":1,\"url\":\"/productImage/gift-cookies2.jpg\",\"_id\":\"565fcdcbab8fb0e96a83ee30\"}],\"descriptionLanguages\":\"Create your personal cookies gift , just like beautiful memory is in your life .\",\"sequence\":11,\"price\":688,\"description\":\"Create your personal cookies gift , just like beautiful memory is in your life .\",\"other\":{},\"productId\":\"565fcdcbab8fb0e96a83ee2e\",\"name\":\"Your personal cookies gift\",\"nameAliasLanguages\":\"Your personal cookies gift\"}]}";
        return testData;
    }
}
