/*
 * Copyright (C) 2010 The MobileSecurePay Project
 * All right reserved.
 * author: shiqun.shi@alipay.com
 * 
 *  提示：如何获取安全校验码和合作身份者id
 *  1.用您的签约支付宝账号登录支付宝网站(www.alipay.com)
 *  2.点击“商家服务”(https://b.alipay.com/order/myorder.htm)
 *  3.点击“查询合作者身份(pid)”、“查询安全校验码(key)”
 */

package com.pictureair.photopassCopy.alipay;

//
// 请参考 Android平台安全支付服务(msp)应用开发接口(4.2 RSA算法签名)部分，并使用压缩包中的openssl RSA密钥生成工具，生成一套RSA公私钥。
// 这里签名时，只需要使用生成的RSA私钥。
// Note: 为安全起见，使用RSA私钥进行签名的操作过程，应该尽量放到商家服务器端去进行。
public final class Keys {

	//pp参数
	//合作身份者id，以2088开头的16位纯数字
	public static final String DEFAULT_PARTNER = "2088121658521542";

	//收款支付宝账号
	public static final String DEFAULT_SELLER = "online.shdr@pictureworks.biz";

	//商户私钥，自助生成
	public static final String PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAM4aMbrHbIakLyu7" +
			"KD6lhco7uhYeEtbA+sTJg5j7NVgL/c7gs448lSeQQgmslAg8u5Unw2dGU9bDvigs" +
			"7pBSSGjVP8ffN/Fhbibru+o43GnPrmN/VaZMJeZ4L6Yq7DOPwefB1qquH0JmSkwA" +
			"jDLuXBCW7SAhWaxvAhQw0TA7onF/AgMBAAECgYBdaFW9dwBijSqrwSjeK5pVK3iM" +
			"6ZhUKCX+5e10efWVL+oZxpDaF+7pfWHQjJySlyPU166hsafxn8kg4CxG0GPr/r5l" +
			"TI/iTNXTl5kMs7pBkNSMfqwiaFw2y86+FQxA6vWAYct58h0TfJIbQxew3FNKm9L3" +
			"mkP39QabHS0IZZPLAQJBAPBN0ccA2U0nFejnxxPFb9X/ZigM2yz0mN2RSumxjeK8" +
			"j6zQ9q8YGiT5KN/SlsKKwmZjn3SGhOBG5szYRdR2sz8CQQDbkHsKz6jElfGBUJeh" +
			"w2D2X3Wz4YMLwpdmM72zxaL57skMUYAYr7Z88kLjDo8NkFPnKLd/i2iSAGLhr+Hh" +
			"2fHBAkEAwS/euRzelZ8Zhx9mtWdhzGnB5+rF/XM6vc3DqvJ6PdEXtHheCU/YHOdO" +
			"S/pSB15kgMoQdC58/o0hmYc5RGCVIwJAH6/AMhU58/T3v4PT/kJmEehA4k7fTKku" +
			"lE9PR0x0csFlPmPcqrNhELMduAr31itIEIotwThrRPbNTmjFnoSHgQJAbOZSeGCq" +
			"hlh24Mn+k6bgUFYKqiZZBhcSvIq8SfMFuBCFzDZ8XG9cRKnY54raYVxve9pLtR8x" +
			"TAsHocRTAzvJ2g==";

	//支付宝公钥
	public static final String PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQE" +
			"B/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

}
