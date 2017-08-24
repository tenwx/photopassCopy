package com.pictureair.photopass.http.rxhttp;

/**
 * Created by pengwu on 16/11/24.
 *
 * 对于异常返回码的封装，这里传入的state是服务器返回的状态，比如：
 *   case 6035://Current certification has expired, please login again
 *   case 6079://Current certification has expired, please login again
 *   case 6080://token已经过期
 *   case 6074://get token error
 *   case 6075://set token error
 *   case 6151://query token error
 *   case 6153://未授权
 *   case 6034://please login
 *   case 5030://not login
 *   case 5011://not login
 */

public class ServerException extends Exception {

    private int state;

    public ServerException(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
