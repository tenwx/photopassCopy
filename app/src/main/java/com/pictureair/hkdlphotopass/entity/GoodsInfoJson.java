package com.pictureair.hkdlphotopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/13.
 */
public class GoodsInfoJson implements Serializable{
    List<GoodsInfo> goods = null;

    public GoodsInfoJson() {
    }

    public GoodsInfoJson(List<GoodsInfo> goods) {
        this.goods = goods;
    }

    public List<GoodsInfo> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsInfo> goods) {
        this.goods = goods;
    }
}
