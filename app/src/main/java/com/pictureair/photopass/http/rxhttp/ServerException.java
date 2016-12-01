package com.pictureair.photopass.http.rxhttp;

/**
 * Created by pengwu on 16/11/24.
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
