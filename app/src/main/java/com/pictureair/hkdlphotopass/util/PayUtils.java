package com.pictureair.hkdlphotopass.util;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.alipay.sdk.app.PayTask;
import com.pictureair.hkdlphotopass.activity.PaymentOrderActivity;
import com.pictureair.hkdlphotopass.alipay.AESOperator;
import com.pictureair.hkdlphotopass.alipay.PayResult;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.Util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

/**
 * Created by milo on 16/3/3.
 * 支付封装类
 * 支付类型：支付宝、微信、银联 、VISA信用卡
 */
public class PayUtils {
    private static final String TAG = "PayUtils";
    public static final int SHOW_DIALOG = 111;
    public static final int DISMISS_DIALOG = 222;
    private Activity activity;
    private Handler handler;
    private String orderId;
    private String nameString;
    private String introductString;
    private String priceString;
    private String seed;
    private IWXAPI msgApi;

    public PayUtils(Activity activity, Handler handler, String orderId, String nameString, String introductString, String priceString, String seed) {
        this.activity = activity;
        this.handler = handler;
        this.orderId = orderId;
        this.nameString = nameString;
        this.introductString = introductString;
        this.priceString = priceString;
        this.seed = seed;
    }

    /**
     * 支付宝支付
     *
     * @throws UnsupportedEncodingException
     */
    public void aliPay(String signed, String unSigned) throws UnsupportedEncodingException {
        try {
            String origin = AESOperator.getInstance().decrypt(unSigned, "pictureairapidsn", "1233210988906757");
            PictureAirLog.i("origin", origin);
        } catch (Exception e) {
            e.printStackTrace();
        }
//        PictureAirLog.v(TAG, "start aliPay" + "id: " + orderId);
//        PictureAirLog.v(TAG, "id: " + orderId);
//        PictureAirLog.v(TAG, "name : " + nameString);
//        PictureAirLog.v(TAG, "introductString: " + introductString);
//
//        String info = AliPayUtil.getOrderInfo(orderId, nameString,
//                introductString, priceString);
//        PictureAirLog.v(TAG, "info:" + info);
//        // 对订单做RSA 签名
//        String sign = AliPayUtil.sign(info);
//        PictureAirLog.v(TAG, "sign:" + sign);
        // 仅需对sign 做URL编码
//       String sign = URLEncoder.encode(signed, "UTF-8");
        // 完整的符合支付宝参数规范的订单信息
        final String payInfo = unSigned + "&sign=\"" + signed + "\"&"
                + AliPayUtil.getSignType();
        PictureAirLog.d("ExternalPartner", "start pay");
        // start the pay.
        new Thread() {
            public void run() {
                PayTask alipay = new PayTask(activity);
                // 设置为沙箱模式，不设置默认为线上环境
                // alipay.setSandBox(true);
                // 调用支付接口，获取支付结果
                String result = alipay.pay(payInfo, true);
                // Result result = new
                // Result(alipay.pay(orderInfo).toString());
                PictureAirLog.v(TAG, "pay info=" + result);
                PayResult payResult = new PayResult(result);
                // String resultInfo = payResult.getResult();

                String resultStatus = payResult.getResultStatus();
                Message msg = new Message();
                if (TextUtils.equals(resultStatus, "9000")) {// 支付成功
                    msg.what = PaymentOrderActivity.RQF_SUCCESS;
                } else if (TextUtils.equals(resultStatus, "6001")) {// 用户中途取消
                    msg.what = PaymentOrderActivity.RQF_CANCEL;
                } else if (TextUtils.equals(resultStatus, "4000")) {// 订单支付失败
                    msg.what = PaymentOrderActivity.RQF_ERROR;
                } else if (TextUtils.equals(resultStatus, "8000")) {// 正在处理中
                    msg.what = PaymentOrderActivity.RQF_SUCCESS;
                } else if (TextUtils.equals(resultStatus, "6002")) {// 网络连接出错
                    msg.what = PaymentOrderActivity.RQF_ERROR;
                }
                handler.sendMessage(msg);
            }
        }.start();

    }

    /**
     * 检查微信是否已经安装
     *
     * @return
     */
    public boolean isWechatInstalled() {
        msgApi = WXAPIFactory.createWXAPI(activity, null);
        return msgApi.isWXAppInstalled();
    }

    /**
     * 微信支付
     */
    public void wxPay() {
        handler.sendEmptyMessage(SHOW_DIALOG);
        GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
        getPrepayId.execute();
    }

    public void wxDealResult(int resultCode) {
        switch (resultCode) {
            case 0:// 微信支付成功
                handler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
                break;

            case -1:// 微信支付取消
            case -3:
                handler.sendEmptyMessage(PaymentOrderActivity.RQF_CANCEL);
                break;

            case -2:// 微信支付失败
                handler.sendEmptyMessage(PaymentOrderActivity.RQF_UNSUCCESS);
                break;

            default:
                break;
        }
    }


    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {
        StringBuffer sb = new StringBuffer();
        Map<String, String> resultunifiedorder;
        WXPayUtil wxPayUtil = new WXPayUtil(seed);

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Map<String, String> result) {
            // 生成预付单的结果
            handler.sendEmptyMessage(DISMISS_DIALOG);
            sb.append("prepay_id\n").append(result.get("prepay_id")).append("\n\n");

            resultunifiedorder = result;
            PictureAirLog.d("===============", result.toString());
            if (result.toString().equals("{}")) {
                handler.sendEmptyMessage(PaymentOrderActivity.RQF_ERROR);
            } else {
                // 生成签名参数
                PayReq req = wxPayUtil.getPayReq(sb, resultunifiedorder);
                // 调用微信支付
                wxPayUtil.sendPayReq(msgApi, req);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String, String> doInBackground(Void... params) {

            String url = String
                    .format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            // 生成的支付订单
            String entity = wxPayUtil.genProductArgs(nameString, orderId, priceString);

            PictureAirLog.d("orion", entity);
            // 把生成的支付订单post生成预付单
            byte[] buf = Util.httpPost(url, entity);
            String content = "";
            if (buf != null) {
                content = new String(buf);
            }
            PictureAirLog.d("orion", content);
            return wxPayUtil.decodeXml(content);
        }

    }

//    /**
//     * 银联支付
//     */
//    //mMode参数解释： "00" - 启动银联正式环境 "01" - 连接银联测试环境
//    private final String mMode = "01";
//    //银联测试假数据
//    private String RESULT = "{\"sign\":\"ZnZY4nqFGu/ugcXNIhniJh6UDVriWANlHtIDRzV9w120E6tUgpL9Z7jIFzWrSV73hmrkk8BZMXMc/9b8u3Ex1ugnZn0OZtWfMZk2I979dxp2MmOB+1N+Zxf8iHr7KNhf9xb+VZdEydn3Wc/xX/B4jncg0AwDJO/0pezhSZqdhSivTEoxq7KQTq2KaHJmNotPzBatWI5Ta7Ka2l/fKUv8zr6DGu3/5UaPqHhnUq1IwgxEWOYxGWQgtyTMo/tDIRx0OlXOm4iOEcnA9DWGT5hXTT3nONkRFuOSyqS5Rzc26gQE6boD+wkdUZTy55ns8cDCdaPajMrnuEByZCs70yvSgA==\",\"data\":\"pay_result=success&tn=201512151321481233778\"}";
//
//    public void unDealResult() {
//        //银联测试假数据
//        Intent data = new Intent();
//        PictureAirLog.e(TAG,"data" + data);
////        data.putExtra("pay_result", "success");
////        data.putExtra("result_data", RESULT);
//        if (data == null) {
//            return;
//        }
//        //支付控件返回字符串:success、fail、cancel 分别代表支付成功，支付失败，支付取消
//        String str = data.getExtras().getString("pay_result");
//        PictureAirLog.e(TAG,"str" + str);
//        if (str.equalsIgnoreCase("success")) {
//            // 支付成功后，extra中如果存在result_data，取出校验
//            // result_data结构见c）result_data参数说明
//            if (data.hasExtra("result_data")) {
//                String result = data.getExtras().getString("result_data");
//                PictureAirLog.e(TAG,"result" + result);
//                JSONObject resultJson = JSONObject.parseObject(result);
//                PictureAirLog.e(TAG,"resultJson" + resultJson);
//                String sign = resultJson.getString("sign");
//                PictureAirLog.e(TAG,"sign" + sign);
//                String dataOrg = resultJson.getString("data");
//                // 验签证书同后台验签证书
//                // 此处的verify，商户需送去商户后台做验签
//                boolean ret = UnionpayRSAUtil.verify(dataOrg, sign, mMode);
//
//                //测试修改
////                ret = true;
//
//                if (ret) {
//                    // 验证通过后，显示支付结果
//                    handler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
//                } else {
//                    // 验证不通过后的处理
//                    // 建议通过商户后台查询支付结果
//                    handler.sendEmptyMessage(PaymentOrderActivity.RQF_ERROR);
//                }
//            } else {
//                // 未收到签名信息
//                // 建议通过商户后台查询支付结果
//                handler.sendEmptyMessage(PaymentOrderActivity.RQF_SUCCESS);
//            }
//        } else if (str.equalsIgnoreCase("fail")) {
//            handler.sendEmptyMessage(PaymentOrderActivity.RQF_ERROR);
//        } else if (str.equalsIgnoreCase("cancel")) {
//            handler.sendEmptyMessage(PaymentOrderActivity.RQF_CANCEL);
//        }

//    }


}
