# To enable ProGuard in your project, edit project.properties
# to define the proguard.config property as described in that file.
#
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


# 指定代码的压缩级别
-optimizationpasses 5
-dontusemixedcaseclassnames
# 是否混淆第三方jar
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes InnerClasses,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature

#注意：studio里面的jar，工具自动添加混淆 不需要写，写上去会报重复申明
#将jar全部设置混淆
#-libraryjars libs/alipaysdk.jar
#-libraryjars libs/alipaysecsdk.jar
#-libraryjars libs/alipayutdid.jar
#-libraryjars libs/AMap_3DMap_V2.4.0.jar
#-libraryjars libs/Android_Location_V1.3.2.jar
#-libraryjars libs/android-async-http-1.4.9.jar
#-libraryjars libs/android-support-v4.jar
#-libraryjars libs/libammsdk.jar
#-libraryjars libs/MobLogCollector.jar
#-libraryjars libs/MobTools.jar
#-libraryjars libs/nineoldandroids-2.4.0.jar
#-libraryjars libs/PayPalAndroidSDK-2.8.7.jar
#-libraryjars libs/pinyin4j-2.5.0.jar
#-libraryjars libs/ShareSDK-Core-2.6.1.jar
#-libraryjars libs/ShareSDK-Facebook-2.6.1.jar
#-libraryjars libs/ShareSDK-QZone-2.6.1.jar
#-libraryjars libs/ShareSDK-SinaWeibo-2.6.1.jar
#-libraryjars libs/ShareSDK-Twitter-2.6.1.jar
#-libraryjars libs/ShareSDK-Wechat-2.6.1.jar
#-libraryjars libs/ShareSDK-Wechat-Core-2.6.1.jar
#-libraryjars libs/ShareSDK-Wechat-Favorite-2.6.1.jar
#-libraryjars libs/ShareSDK-Wechat-Moments-2.6.1.jar
#-libraryjars libs/socketio
#-libraryjars libs/umeng-analytics-v5.5.3.jar
#-libraryjars libs/universal-image-loader-1.9.2-with-sources.jar
#-libraryjars libs/zxing.jar

#将api自带的设置避免混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.View
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference

-ignorewarnings
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }
-keep public class * extends android.support.v4.**
-keep public class * extends android.app.Fragment

#支付宝避免混淆
-keep class com.alipay.android.app.IAlixPay{*;}
-keep class com.alipay.android.app.IAlixPay$Stub{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback{*;}
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub{*;}
-keep class com.alipay.sdk.app.PayTask{ public *;}
-keep class com.alipay.sdk.app.AuthTask{ public *;}
-keep class com.alipay.mobilesecuritysdk.*
-keep class com.ut.*

#短信验证和分享避免混淆
-keep class android.net.http.SslError
-keep class android.webkit.**{*;}
-keep class cn.sharesdk.**{*;}
-keep class m.framework.**{*;}
-keep class cn.smssdk.**{*;}
-keep class com.mob.**{*;}
-keep class com.sina.**{*;}
-keep class **.R{*;}
-dontwarn cn.sharesdk.**
-dontwarn **.R$*

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class **.R$* {
    *;
}

-keep class * implements android.os.Parcelable {
	public static final android.os.Parcelable$Creator *;
}

#友盟混淆
-keepclassmembers class * {
   	public <init>(org.json.JSONObject);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep public class com.pictureAir.R$*{
	public static final int *;
}

# universal-image-loader 混淆
-dontwarn com.nostra13.universalimageloader.**
-keep class com.nostra13.universalimageloader.** { *; }

##异步网络请求避免混淆
-dontwarn android-async-http-1.4.9.jar.**
-keep class android-async-http-1.4.9.jar.**{*;}

# fastjson 混淆
-dontwarn com.alibaba.fastjson.**
-dontskipnonpubliclibraryclassmembers
-dontskipnonpubliclibraryclasses
-keep class com.alibaba.fastjson.** { *; }
-keepclassmembers class * {
public <methods>;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
    public <fields>;
}
-keepattributes Signture