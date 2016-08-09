package com.pictureair.photopass.entity;

/**
 * Created by pengwu on 16/8/8.
 */
public class BasicResult<T> {
    private int status;
    private String msg;
    private T result;

    public BasicResult(){

    }
    public BasicResult(int status, String msg, T result) {
        this.status = status;
        this.msg = msg;
        this.result = result;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }
}
