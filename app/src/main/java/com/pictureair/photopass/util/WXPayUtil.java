package com.pictureair.photopass.util;

import android.util.Xml;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;

import net.sourceforge.simcpux.Constants;
import net.sourceforge.simcpux.MD5;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by milo on 16/3/9.
 * 微信支付工具类
 */
public class WXPayUtil {
    StringBuffer sb;
    private String seed;

    public WXPayUtil(String seed) {
        this.seed = seed;
    }

    public PayReq getPayReq(StringBuffer sb, Map<String, String> resultunifiedorder) {
        // 获取wechat的实例对象
        this.sb = sb;
        PayReq req = new PayReq();
        req.appId = Constants.APP_ID;
        req.partnerId = Constants.MCH_ID;
        req.prepayId = resultunifiedorder.get("prepay_id");// 预支付交易会话标:微信生成的预支付回话标识，用于后续接口调用中使用，该值有效期为2小时
        req.packageValue = "prepay_id=" + resultunifiedorder.get("prepay_id");
        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());// 时间戳

        List<NameValuePair> signParams = new LinkedList<>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

        req.sign = genAppSign(signParams);
        sb.append("sign\n").append(req.sign).append("\n\n");
        PictureAirLog.d("orion", signParams.toString());
        return req;
    }

    // 调用微信支付
    public void sendPayReq(IWXAPI msgApi, PayReq req) {
        msgApi.registerApp(Constants.APP_ID);
        msgApi.sendReq(req);

    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    private String genNonceStr() {
        SecureRandom secureRandom = null;
        try {
            secureRandom = SecureRandom.getInstance("SHA1PRNG");
            secureRandom.setSeed(seed.getBytes());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return MD5.getMessageDigest(String.valueOf(AESKeyHelper.secureIntRandom(10000)).getBytes());
    }

    // 生成支付订单
    public String genProductArgs(String nameString, String orderId, String priceString) {

        try {
            String nonceStr = genNonceStr();
            PictureAirLog.out("name--->" + nameString);
            PictureAirLog.out("name  utf--->" + URLDecoder.decode(nameString, "UTF-8"));
            List<NameValuePair> packageParams = new LinkedList<>();
            packageParams
                    .add(new BasicNameValuePair("appid", Constants.APP_ID));// 公众账号ID
            packageParams.add(new BasicNameValuePair("body", new String(nameString.getBytes("utf-8"), "utf-8")));// 商品描述
//            packageParams.add(new BasicNameValuePair("input_charset", "UTF-8"));
            packageParams
                    .add(new BasicNameValuePair("mch_id", Constants.MCH_ID));// 商户号
            packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));// 随机字符串
            packageParams.add(new BasicNameValuePair("notify_url",
                    Common.WECHAT_NOTIFY));// 通知地址:接收微信支付异步通知回调地址
            packageParams.add(new BasicNameValuePair("out_trade_no", orderId));// 商户订单号:商户系统内部的订单号,32个字符内、可包含字母
            packageParams.add(new BasicNameValuePair("spbill_create_ip",
                    "127.0.0.1"));// 终端IP:APP和网页支付提交用户端ip，Native支付填调用微信支付API的机器IP

            PictureAirLog.out("price------>" + priceString);
            double price = Double.valueOf(priceString);
            PictureAirLog.out("price------>" + (int) price);

            packageParams.add(new BasicNameValuePair("total_fee", ((int) price) * 100 + ""));// 总金额只能为整数,单位是分
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));// 交易类型:取值如下：JSAPI，NATIVE，APP，WAP

            String sign = genPackageSign(packageParams);
            packageParams.add(new BasicNameValuePair("sign", sign));// 签名

            return toXml(packageParams);

        } catch (Exception e) {
            PictureAirLog.out("genProductArgs fail, ex = " + e.getMessage());
            return null;
        }

    }


    private String genAppSign(List<NameValuePair> params) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            stringBuilder.append(params.get(i).getName());
            stringBuilder.append('=');
            stringBuilder.append(params.get(i).getValue());
            stringBuilder.append('&');
        }
        stringBuilder.append("key=");
        stringBuilder.append(Constants.API_KEY);

        sb.append("sign str\n").append(stringBuilder.toString()).append("\n\n");
        String appSign = MD5.getMessageDigest(stringBuilder.toString().getBytes()).toUpperCase();
        PictureAirLog.d("orion", appSign);
        return appSign;
    }

    // 生成签名参数
    private String genPackageSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.API_KEY);

        String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        PictureAirLog.d("orion", packageSign);
        return packageSign;
    }

    private String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<").append(params.get(i).getName()).append(">");

            sb.append(params.get(i).getValue());
            sb.append("</").append(params.get(i).getName()).append(">");
        }
        sb.append("</xml>");

        PictureAirLog.d("orion", sb.toString());
        return sb.toString();
    }

    public Map<String, String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName = parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if (!"xml".equals(nodeName)) {
                            xml.put(nodeName, parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            PictureAirLog.d("orion---exception", e.toString());
        }
        return null;

    }

}
