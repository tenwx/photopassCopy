package com.pictureAir.weChatPay;

public class Constants {
	// APP_ID 替换为你的应用从官方网站申请到的合法appId
    public static final String APP_ID = "wxd930ea5d5a258f4f";
    
    /** 商家向财付通申请的商家id */
    public static final String PARTNER_ID = "1900000109";
    
    public static final String PARTNER_KEY = "8934e7d15453e97507ef794cf7b0519d";
    
    /**
	 * 微信开放平台和商户约定的密钥
	 * 
	 * 注意：不能hardcode在客户端，建议genSign这个过程由服务器端完成
	 */
    public static final String APP_SECRET = "db426a9829e4b49a0dcac7b4162da6b6"; // wxd930ea5d5a258f4f 对应的密钥

	/**
	 * 微信开放平台和商户约定的支付密钥
	 * 
	 * 注意：不能hardcode在客户端，建议genSign这个过程由服务器端完成
	 */
    public static final String APP_KEY = "L8LrMqqeGRxST5reouB0K66CaYAWpqhAVsq7ggKkxHCOastWksvuX1uvmvQclxaHoYd3ElNBrNO2DHnnzgfVG9Qs473M3DTOZug5er46FhuGofumV8H2FVR9qkjSlC5K"; // wxd930ea5d5a258f4f 对应的支付密钥
    
    public static class ShowMsgActivity {
		public static final String STitle = "showmsg_title";
		public static final String SMessage = "showmsg_message";
		public static final String BAThumbData = "showmsg_thumb_data";
	}
}
