package com.pictureair.photopass.eventbus;

/**
 * Created by bauer_bao on 15/12/28.
 */
public class ScanInfoEvent implements BaseBusEvent {
    private int errorType;
    private String result;
    private boolean hasBind;
    private String codeType;

    public ScanInfoEvent(int errorType, String result, boolean hasBind, String codeType) {
        this.errorType = errorType;
        this.result = result;
        this.hasBind = hasBind;
        this.codeType = codeType;
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

    public String getCodeType() {
        return codeType;
    }

    public void setCodeType(String codeType) {
        this.codeType = codeType;
    }
}
