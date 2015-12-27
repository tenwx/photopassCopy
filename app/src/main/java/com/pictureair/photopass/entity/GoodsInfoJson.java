package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/13.
 */
public class GoodsInfoJson implements Serializable{
    List<GoodsInfo1> goods = null;

    public GoodsInfoJson() {
    }

    public GoodsInfoJson(List<GoodsInfo1> goods) {
        this.goods = goods;
    }

    public List<GoodsInfo1> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsInfo1> goods) {
        this.goods = goods;
    }
}
