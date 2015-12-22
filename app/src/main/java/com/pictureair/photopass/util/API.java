package com.pictureair.photopass.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/** 所有与后台的交互都封装到此类 */
public class API {
	public static final int MODIFY_PWD_SUCCESS = 391;
	public static final int MODIFY_PWD_FAILED = 390;

	/**
	 * 修改密码或者忘记密码接口
	 * @param context
	 * @param oldPwd 旧密码，修改的时候用到，如果是忘记密码的话，设为null
	 * @param newPwd 新密码
	 * @param type 判断是否是修改密码（null）还是忘记密码（forget）
	 * @param handler
	 */
	public static void modifyPwd(Context context, String oldPwd, String newPwd, String type, final Handler handler) {
		StringBuffer url = new StringBuffer();
		final SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		url.append(Common.BASE_URL).append(Common.MODIFYPWD);
		final RequestParams params = new RequestParams();
		params.put(Common.NEW_PASSWORD, AppUtil.md5(newPwd));
		params.put(Common.USERINFO_TOKENID, sp.getString(Common.USERINFO_TOKENID, null));
		if (type.equals("forget")) {//忘记密码，不需要填写oldpassword
			params.put(Common.MODIFY_OR_FORGET, type);
		}else {//修改密码操作，type不要填写
			params.put(Common.OLD_PASSWORD, AppUtil.md5(oldPwd));
		}
		System.out.println("sign tokenid = "+sp.getString(Common.USERINFO_TOKENID, null));
		HttpUtil.post(url.toString(), params, new JsonHttpResponseHandler() {
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("sign====="+response);
				if (response.has("error")) {
					try {
						Message message = handler.obtainMessage();
						message.what = MODIFY_PWD_FAILED;
						message.obj = response.getJSONObject("error").get("type");;
						handler.sendMessage(message);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (response.has("success")) {
					handler.sendEmptyMessage(MODIFY_PWD_SUCCESS);
				}

			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("modify failed------"+errorResponse);
				Message message = handler.obtainMessage();
				message.what = MODIFY_PWD_FAILED;
				message.obj = errorResponse;
				handler.sendMessage(message);
			}
		});
	}

}
