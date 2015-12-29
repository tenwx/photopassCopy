package com.pictureair.photopass.entity;

/**
 * Created by bauer_bao on 15/12/28.
 */
public class ScanInfoEvent implements BaseBusEvent {
    private int errorType;
    private String result;
    private boolean hasBind;

    public ScanInfoEvent(int errorType, String result, boolean hasBind) {
        this.errorType = errorType;
        this.result = result;
        this.hasBind = hasBind;
    }

    public boolean isHasBind() {
        return hasBind;
    }

    public void setHasBind(boolean hasBind) {
        this.hasBind = hasBind;
    }

    public int getErrorType() {
        return errorType;
    }

    public void setErrorType(int errorType) {
        this.errorType = errorType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
