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

package com.pictureair.photopass.alipay;

//
// 请参考 Android平台安全支付服务(msp)应用开发接口(4.2 RSA算法签名)部分，并使用压缩包中的openssl RSA密钥生成工具，生成一套RSA公私钥。
// 这里签名时，只需要使用生成的RSA私钥。
// Note: 为安全起见，使用RSA私钥进行签名的操作过程，应该尽量放到商家服务器端去进行。
public final class Keys {

	//合作身份者id，以2088开头的16位纯数字
	public static final String DEFAULT_PARTNER = "2088611102827602";

	//收款支付宝账号
	public static final String DEFAULT_SELLER = "online.payment@pictureworks.biz";

	//商户私钥，自助生成
	public static final String PRIVATE = "MIICeAIBADANBgkqhkiG9w0BAQEFAASCAmIwggJeAgEAAoGBAOEKTVKgUdpqrp/+YFHv+zgZQjAr3UfQQhJsLzVh9Lb/OzAn2EXaaPG04W/blf5I9Fa+txasplMNLiDx"+
			"MS4t813uJsihYNxOGFTzpNAdE0iurNx4yYRirYlH8/lxv6MMqbUeerU5tJcVFqlgBNulHC2W7MAI8AyPMVHYZSxwZSgPAgMBAAECgYEAz44t5MBC3l04tA3sjoX1QTdi"+
			"qYU7cQmudsla/TRcfQ+IapDFUx/vXMbEieOQQtjMqtj5iqswODdWzUFHZ90vsD6+zyx2/G0zt1cpFmVPBc1CdXDBjmqlD032pwnnMM8m5oB5pnl89pgWz5z0/3Gz16Rg"+
			"Ih59/etgYYrpjR44fxkCQQDwb0nDAssrE8hG2zD+jAaNEtGD+XFQXz9yk7yaW0xvgVewi/7fcNxhD/MNXsV+fOUQZ7b8Tl8lX4q3ak0DoHhNAkEA75vhzqiAg8WFusqw"+
			"0z6zECoBL0Wj5SxETm/H8iulfFCDLTnAtiEvrBrFiXAMFb5VsSPWoEA7HFjNZ3veuh5PywJBALzeHuoyUphXJNcMLdN2+VpuMujh1HJXL8b4aIo0N7nLt692eVLaHDOe"+
			"zMVu8eKnh2S4pBRUGAeKorPrq+2ya20CQQC9oGZSgPREVNwC+kp6xBI+Zp/NY5XT1vawBLL+qyAqhCVsc1paSWVLFVzj6jMGOj3jFPPUm0CsrHLSxSt9rGOFAkBs9gJ5"+
			"UsfgAjgY7H5uyY3p139aU1MraBHRbyOP8wvX5XbcmwuyrPwflyF9BmXdHkMkrYeRX8d4slhj4cWlzsNY";

	//支付宝公钥
	public static final String PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQE" +
			"B/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

}
