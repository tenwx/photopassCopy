package com.pictureair.photopass.http.rxhttp;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.util.AppExitUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.SPUtils;


/**
 * Created by bauer_bao on 16/11/11.
 */

public class APIException extends RuntimeException {


    public APIException(int resultCode) {
        this(getApiExceptionMessage(resultCode));
    }

    public APIException(String detailMessage) {
        super(detailMessage);
    }

    /**
     * @param code
     * @return
     */
    private static String getApiExceptionMessage(int code) {
        switch (code) {
            case 6035://Current certification has expired, please login again
            case 6079://Current certification has expired, please login again
            case 6080://token已经过期
            case 6074://get token error
            case 6075://set token error
            case 6151://query token error
            case 6153://未授权
            case 6034://please login
            case 5030://not login
            case 5011://not login
                boolean isLogin = SPUtils.getBoolean(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ISLOGIN, false);
                if (isLogin) {//没有登录
                    SPUtils.remove(MyApplication.getInstance(), Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_ISLOGIN);
                    AppExitUtil.getInstance().AppReLogin();
                }
                break;

            default:
                break;
        }
        return code + "";
    }
}