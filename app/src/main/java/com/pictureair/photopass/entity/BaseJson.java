package com.pictureair.photopass.entity;

import java.io.Serializable;

/**
 * Created by milo on 15/12/5.
 * 服务器返回的基类
 */
public class BaseJson implements Serializable {
    private int status;//状态码，正常返回状态码为200，异常返回状态码为对应的错误码
    private String msg;//提示信息，正常返回时提示信息为空，异常返回提示信息为对应错误信息
    private String result;//对象类型，为空或json格式的数据

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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
