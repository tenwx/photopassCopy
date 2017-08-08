package com.pictureair.photopassCopy.entity;

/**
 * Created by pengwu on 16/8/8.
 */
public class BasicResult<T> {
    private int status = 404;//状态码，正常返回状态码为200，异常返回状态码为对应的错误码
    private String msg="";//提示信息，正常返回时提示信息为空，异常返回提示信息为对应错误信息
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

    @Override
    public String toString() {
        return "BasicResult{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", result=" + result +
                '}';
    }
}
