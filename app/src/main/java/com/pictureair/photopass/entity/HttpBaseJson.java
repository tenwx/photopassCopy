package com.pictureair.photopass.entity;

import java.io.Serializable;

/**
 * Created by milo on 15/12/5.
 * 服务器返回的基类
 */
public class HttpBaseJson implements Serializable {
    private int status = 404;//状态码，正常返回状态码为200，异常返回状态码为对应的错误码
    private String msg = "";//提示信息，正常返回时提示信息为空，异常返回提示信息为对应错误信息
    private Object result = null;//对象类型，为空或json格式的数据

    public HttpBaseJson() {
    }

    public HttpBaseJson(int status, String msg, Object result) {
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

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
