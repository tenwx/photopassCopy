package com.pictureair.jni.keygenerator;

/**
 * Created by bauer_bao on 16/3/31.
 */
public class PWJniUtil {

    static {
        System.loadLibrary("PWJniUtil");
    }

    public native static String getAESKey(String appType);

    public native static String getAPPKey(String appType);

    public native static String getAppSecret(String appType);

    public native static String getSMSSDKAppKey(String appType);

    public native static String getSMSSDKAppSecret(String appType);

    public native static String getSqlCipherKey(String appType);

    public native static String getOcrStr1();

    public native static String getOcrStr2();

}
