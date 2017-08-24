package com.pictureair.photopass.http.rxhttp;

/**
 * 所有Api接口可以通过此类获得
 * Created by pengwu on 16/7/5.
 */
public enum ApiFactory {

    INSTANCE;
    private final PhotoPassAuthApi authApi;

    ApiFactory(){
        authApi = RetrofitClient.INSTANCE.getRetrofit().create(PhotoPassAuthApi.class);
    }

    public PhotoPassAuthApi getPhotoPassAuthApi(){
        return authApi;
    }
}
