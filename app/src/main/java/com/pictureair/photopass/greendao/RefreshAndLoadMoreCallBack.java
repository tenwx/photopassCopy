package com.pictureair.photopass.greendao;

/**
 * 上拉下拉的临界点
 * Created by bauer_bao on 16/12/15.
 */
public interface RefreshAndLoadMoreCallBack {
    void getRefreshData(String refreshIds, String refreshTime);
    void getLoadMoreData(String loadMoreIds, String loadMoreTime);
    void getAllData(String refreshIds, String refreshTime, String loadMoreIds, String loadMoreTime);
}
