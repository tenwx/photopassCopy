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
-dontskipnonpubliclibraryclassmembers
-dontskipnonpubliclibraryclasses
-dontpreverify
-verbose
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-keepattributes InnerClasses,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Signture

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

#微信支付避免混淆
-keep class com.tencent.mm.sdk.** { *;}

#避免混淆银联支付
-keep class com.unionpay.** {*;}
-keep class org.simalliance.openmobileapi.** {*;}
-keep class org.simalliance.openmobileapi.service.** {*;}

#短信验证和分享避免混淆
-keep class android.net.http.SslError
-keep class android.webkit.**{*;}
-keep class cn.sharesdk.**{*;}
-keep class m.framework.**{*;}
-keep class com.mob.**{*;}
-keep class com.sina.**{*;}
-keep class **.R$* {*;}
-keep class **.R{*;}
-dontwarn cn.sharesdk.**
-dontwarn com.mob.**
-dontwarn **.R$*

#保持 native 的方法不去混淆
-keepclasseswithmembernames class * {
    native <methods>;
}

#保持自定义控件类不被混淆，指定格式的构造方法不去混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

#保持指定规则的方法不被混淆（Android layout 布局文件中为控件配置的onClick方法不能混淆）
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

#保持自定义控件指定规则的方法不被混淆
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

#保持枚举 enum 不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#保持R文件不被混淆，否则，你的反射是获取不到资源id的
-keep class **.R$* {*;}

#-keep public class com.pictureAir.R$*{
#	public static final int *;
#}

#保持 Parcelable 不被混淆（aidl文件不能去混淆）
-keep class * implements android.os.Parcelable {
	public static final android.os.Parcelable$Creator *;
}

#需要序列化和反序列化的类不能被混淆（注：Java反射用到的类也不能被混淆）
-keepnames class * implements java.io.Serializable

#保护实现接口Serializable的类中，指定规则的类成员不被混淆
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
    public <fields>;
}

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,LocalVariable*Table,*Annotation*,Synthetic,EnclosingMethod

#友盟混淆
-keepclassmembers class * {
   	public <init>(org.json.JSONObject);
}

# glide 混淆
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
# 如果配置了GlideModule，需要加后面的代码  -keepresourcexmlelements manifest/application/meta-data@value=GlideModule

#不混淆org.apache.http.legacy.jar
-dontwarn android.net.compatibility.**
-dontwarn android.net.http.**
-dontwarn com.android.internal.http.multipart.**
-dontwarn org.apache.commons.**
-dontwarn org.apache.http.**
-keep class android.net.compatibility.**{*;}
-keep class android.net.http.**{*;}
-keep class com.android.internal.http.multipart.**{*;}
-keep class org.apache.commons.**{*;}
-keep class org.apache.http.**{*;}

 # fastjson 混淆
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
-keepclassmembers class * {
    public <methods>;
}

# OCR文字识别 混淆
-keep class com.pictureair.photopass.zxing.**{*;}
-keep class com.googlecode.**{*;}

### greenDAO 3
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties

# If you do not use SQLCipher:
#-dontwarn org.greenrobot.greendao.database.**
# If you do not use RxJava:
-dontwarn rx.**

# sqlcipher混淆
-keep class net.sqlcipher.database.* extends java.lang.Exception {
   *;
}
-keepclasseswithmembers class net.sqlcipher.** {
    native <methods>;
}
-keep class net.sqlcipher.database.SQLite* {
    int nHandle;
    int nStatement;
}
-keep class net.sqlcipher.CursorWindow {
    int nWindow;
}
-keep class net.sqlcipher.database.SQLiteDatabase {
    int mNativeHandle;
}

#定位
-keep class com.amap.api.maps2d.**{*;}
-keep class com.amap.api.mapcore2d.**{*;}

-keep class com.amap.api.location.**{*;}
-keep class com.amap.api.fence.**{*;}
-keep class com.autonavi.aps.amapapi.model.**{*;}

#百度统计
-keep class com.baidu.kirin.** { *; }
-keep class com.baidu.mobstat.** { *; }
-keep class com.baidu.bottom.** { *; }

# 听云统计 ProGuard configurations for NetworkBench Lens
-keep class com.networkbench.** { *; }
-dontwarn com.networkbench.**
# End NetworkBench Lens

#retrofit混淆
-dontwarn okhttp3.**
-keep class okhttp3.internal.**{*;}
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.**{ *; }

# RxJava RxAndroid
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}

# butterknife
-keep public class * implements butterknife.internal.ViewBinder { public <init>(); }
# Prevent obfuscation of types which use ButterKnife annotations since the simple name
# is used to reflectively look up the generated ViewBinder.
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }

#udesk
-keep class udesk.** {*;}
-keep class cn.udesk.**{*; }
#七牛
-keep class com.qiniu.android.dns.** {*; }
-keep class com.qiniu.android.** {*;}
#smack
-keep class org.jxmpp.** {*;}
-keep class de.measite.** {*;}
-keep class org.jivesoftware.** {*;}
-keep class org.xmlpull.** {*;}
#Android M 权限
-keep class rx.** {*;}
-keep class com.tbruyelle.rxpermissions.** {*;}

#其它
-keep class com.tencent.bugly.** {*; }
-keep class de.hdodenhof.circleimageview.** {*;}