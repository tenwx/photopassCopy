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

package com.pictureair.hkdlphotopass.alipay;

//
// 请参考 Android平台安全支付服务(msp)应用开发接口(4.2 RSA算法签名)部分，并使用压缩包中的openssl RSA密钥生成工具，生成一套RSA公私钥。
// 这里签名时，只需要使用生成的RSA私钥。
// Note: 为安全起见，使用RSA私钥进行签名的操作过程，应该尽量放到商家服务器端去进行。
public final class Keys {

	//pp参数
	//合作身份者id，以2088开头的16位纯数字
	public static final String DEFAULT_PARTNER = "2088621237128219";

	//收款支付宝账号
	public static final String DEFAULT_SELLER = "HKDL.Cash.Payment03@disney.com";

	//商户私钥，自助生成
	public static final String PRIVATE = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALCtq2rXoqoaFRgqpjcdT5jZYkS3g5eIMLm05tDq7W6IjKxWwCvrNEzkBuW6wblhTMdQZ7W7+x2vN/xvDoR0RvPRZlJJ4SyDU93WKrNz8a8tzipb0xkyvgklG3XgAWAhPCKeTXw3903/2rD6fqNLu+NxmVzCdE8W08e0BTHBB79/AgMBAAECgYEAm14eADbgtPV612WqBY2hEakY4ZUAA6GgN679y3rMkQdDrcob9INB2twrtJonAf6uphfvG8HTcqdDdJm1CoW2XfyZsDKvTr6dbb0qKEYzvvTJTrbO0NvA/9VnujWZBczSPd8ubRg5GhUp6FBKs0tebIgRMrisVt9+lxJlUmfciQECQQDgBvPXTRrEFHyc441CNl7Y+FEaCcxc0/LWxaidaJeTUvEtPmwX2jnwqCCxe8yulcBJBQ5wmeQ2BIn5Zlk/gXPlAkEAyeTI25/cYanKSqfITpq9daI8MCvp22DwQRUOqG2XoY/hMgjlc/NZXKXaXhsg/7e6Xr742QkdEr+ls9BKTdw3kwJBAK0n0otEVn7eEt805rcZzT/I1u7YSfQqAlXM2yIneJjvj+hXijb5frUrTnHAbZk3uDL4TIvt9r1lzrtNR4hVZ50CQFdn8kGLKykpDghWLVVCjuWfsDkWue8dP8adbDQlBjGPDerQiS4y26xNrYVyb03maAxe13N3C5wzVnTLp2+f+HsCQBv8YZnZYgeewyRH+evCGA4MEqDrIF9xU1EF/MxM4fJiWdK0eaP0SWWg0D0Ed7OsnnVpUCJCBzfDVFK45XLM9vU=";

	//支付宝公钥
	public static final String PUBLIC = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCnxj/9qwVfgoUh/y2W89L6BkRAFljhNhgPdyPuBV64bfQNN1PjbCzkIM6qRdKBoLPXmKKMiFYnkd6rAoprih3/PrQE" +
			"B/VsW8OoM8fxn67UDYuyBTqA23MML9q1+ilIZwBC2AQ2UBVOrFXfFl75p6/B5KsiNG9zpgmLCUYuLkxpLQIDAQAB";

}
