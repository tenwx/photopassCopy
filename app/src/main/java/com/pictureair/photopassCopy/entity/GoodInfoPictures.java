package com.pictureair.photopassCopy.entity;

import java.io.Serializable;

/**
 * Created by milo on 15/12/13.
 */
public class GoodInfoPictures implements Serializable {
    private String url = ""; //string 商品图地址
    private int no = 0; //int 商品图序号

    public GoodInfoPictures(String url, int no) {
        this.url = url;
        this.no = no;
    }

    public GoodInfoPictures() {
    }

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
