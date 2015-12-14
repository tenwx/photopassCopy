package com.pictureair.photopass.entity;

import java.io.Serializable;

/**
 * Created by milo on 15/12/13.
 */
public class GoodInfoPrictures implements Serializable {
    private String url; //string 商品图地址
    private int no; //int 商品图序号

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }
}
