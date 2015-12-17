package com.pictureair.photopass.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.entity.BindPPInfo;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.widget.CheckUpdateManager;
import com.pictureair.photopass.widget.CustomProgressBarPop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/** 所有与后台的交互都封装到此类 */
public class API {
	private static final String TAG = "API";
	public static final int SUCCESS = 111;
	public static final int FAILURE = 222;//失败需分情况判断，是网络未打开还是IP地址无法连接亦或是没有授予网络权限
	public static final int GET_ORDER_NO_SUCCESS = 999;
	public static final int DELETE_ADDRESS_SUCCESS = 11;
	public static final int ADD_ADDRESS_SUCCESS = 12;
	public static final int MODIFY_ADDRESS_SUCCESS = 13;
	public static final int GET_ORDER_NO_FAILED = 17;
	public static final int SIGN_FAILED = 5220;
	public static final int DELETE_ORDER_SUCCESS = 14;
	public static final int BIND_PP_FAILURE = 6666;


	public static final int GET_LOCATION_SUCCESS = 301;
	public static final int GET_LOCATION_FAILED = 300;

	public static final int GET_PHOTOS_SUCCESS = 311;
	public static final int GET_PHOTOS_FAILED = 310;

	public static final int GET_REFRESH_PHOTOS_SUCCESS = 321;
	public static final int GET_REFRESH_PHOTOS_FAILED = 320;

	public static final int LOGOUT_SUCCESS = 331;
	public static final int LOGOUT_FAILED = 330;

	public static final int CHECK_CODE_SUCCESS = 341;
	public static final int CHECK_CODE_FAILED = 340;

	public static final int GET_PPS_SUCCESS = 351;
	public static final int GET_PPS_FAILED = 350;

	public static final int GET_CART_COUNT_SUCCESS = 361;
	public static final int GET_CART_COUNT_FAILED = 360;

	public static final int GET_PPP_SUCCESS = 371;
	public static final int GET_PPP_FAILED = 370;

	public static final int GET_STOREID_SUCCESS = 381;
	public static final int GET_STOREID_FAILED = 380;

	public static final int MODIFY_PWD_SUCCESS = 391;
	public static final int MODIFY_PWD_FAILED = 390;

	public static final int GET_DETAIL_GOOD_SUCCESS = 401;
	public static final int GET_DETAIL_GOOD_FAILED = 400;

	public static final int GET_ORDER_SUCCESS = 411;
	public static final int GET_ORDER_FAILED = 410;

	public static final int GET_ADDRESS_SUCCESS = 421;
	public static final int GET_ADDRESS_FAILED = 420;

	public static final int UPDATE_PROFILE_SUCCESS = 431;
	public static final int UPDATE_PROFILE_FAILED = 430;

	public static final int GET_ALL_GOODS_SUCCESS = 441;
	public static final int GET_ALL_GOODS_FAILED = 440;

	public static final int ADD_TO_CART_SUCCESS = 451;
	public static final int ADD_TO_CART_FAILED = 450;

	public static final int GET_PHOTOPASSPLUS_SUCCESS = 461;
	public static final int GET_PHOTOPASSPLUS_FAILED = 460; 

	public static final int GET_FAVORITE_LOCATION_SUCCESS = 471;
	public static final int GET_FAVORITE_LOCATION_FAILED = 470;

	public static final int EDIT_FAVORITE_LOCATION_SUCCESS = 481;
	public static final int EDIT_FAVORITE_LOCATION_FAILED = 480;

	public static final int GET_CART_SUCCESS = 491;
	public static final int GET_CART_FAILED = 490;

	public static final int DELETE_CART_SUCCESS = 501;
	public static final int DELETE_CART_FAILED = 500;

	public static final int UPLOAD_PHOTO_SUCCESS = 511;
	public static final int UPLOAD_PHOTO_FAILED = 510;

	public static final int GET_PP_SUCCESS = 521;
	public static final int GET_PP_FAILED = 520;

	public static final int HIDE_PP_SUCCESS = 531;
	public static final int HIDE_PP_FAILED = 530;

	public static final int GET_LAST_CONTENT_SUCCESS = 541;
	public static final int GET_LAST_CONTENT_FAILED = 540;
	
    public static final int APK_NEED_UPDATE = 551;
	public static final int APK_NEED_NOT_UPDATE = 550;

	public static final int DOWNLOAD_APK_SUCCESS = 561;
	public static final int DOWNLOAD_APK_FAILED = 560;


	/** 退出 */
	public static void Logout(final Context context, final Handler handler) {
		final SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		RequestParams params = new RequestParams();
		StringBuffer sb = new StringBuffer();
		sb.append(Common.BASE_URL);
		sb.append(Common.LOGOUT);
		String tokenId = sp.getString(Common.USERINFO_TOKENID, null);
		System.out.println("login _ tokenid = "+tokenId);
		params.put(Common.USERINFO_TOKENID, tokenId);
		HttpUtil.post(sb.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				if (statusCode == 200) {
					System.out.println("result="+response);
					if (response.has("error")) {
						handler.sendEmptyMessage(LOGOUT_FAILED);
					}else if (response.has("success")) {
						handler.sendEmptyMessage(LOGOUT_SUCCESS);
					}
				}
			}

			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				System.out.println("start logout------");
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				handler.sendEmptyMessage(LOGOUT_FAILED);
			}
		});
	}
	/** 注册 */
	public static void Sign(final Context context, final String userName, final String password , final Handler handler) {
		StringBuffer url = new StringBuffer();
		final SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
		url.append(Common.BASE_URL).append(Common.REGISTER);
		final RequestParams params = new RequestParams();
		params.put(Common.USERINFO_USERNAME, userName);
		params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
		System.out.println("sign tokenid = "+sp.getString(Common.USERINFO_TOKENID, null));
		params.put(Common.USERINFO_TOKENID, sp.getString(Common.USERINFO_TOKENID, null));
		HttpUtil.post(url.toString(), params, new JsonHttpResponseHandler() {
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("sign====="+response);
				if (statusCode == 200) {
					if (response.has("error")) {
						try {
							JSONObject errorJsonObject = response.getJSONObject("error");
							Message msg = handler.obtainMessage();
							//把需要传递的信息放到msg中
							msg.obj = errorJsonObject;
							msg.what = SIGN_FAILED;
							handler.sendMessage(msg);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (response.has("success")) {
						// 注册成功直接跳转到登录页面自动登录
						StringBuffer sb = new StringBuffer();
						sb.append(Common.BASE_URL).append(Common.LOGIN);
						params.put(Common.USERINFO_TOKENID, sp.getString(Common.USERINFO_TOKENID, null));
						params.put(Common.USERINFO_USERNAME, userName);
						params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));
						HttpUtil.post(sb.toString(), params, new JsonHttpResponseHandler() {
							public void onStart() {
								System.out.println("login after sign start======");
							};
							public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

								if (statusCode == 200) {
									try {
										JsonUtil.getUserInfo(context, com.alibaba.fastjson.JSONObject.parseObject(response.toString()) , handler);
									} catch (com.alibaba.fastjson.JSONException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									handler.sendEmptyMessage(SUCCESS);
								}
							};

							public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
								Message msg = handler.obtainMessage();
								//把需要传递的信息放到msg中
								msg.what = SIGN_FAILED;
								msg.obj = responseString;
								handler.sendMessage(msg);
							};
						});
					}

				}
			}
		});
	}

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

	/** 上传个人图片信息，头像或背景图 */
	/**
	 * 
	 * @param url
	 * @param params
	 * @param handler
	 * @param position 修改图片的时候需要这个参数来定位
	 * @throws FileNotFoundException    
	 */
	public static void SetPhoto(String url , RequestParams params,final Handler handler,final int position, final CustomProgressBarPop diaBarPop) throws FileNotFoundException {
		// 需要更新服务器中用户背景图片信息

		HttpUtil.post(url, params, new JsonHttpResponseHandler() {
			Message msg = handler.obtainMessage();
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("uploadphoto=="+response);
				try {
					if(response.has("error")){
						msg.what = UPLOAD_PHOTO_FAILED;
						JSONObject obj = response.getJSONObject("error");
						msg.obj = obj.getString("message");
					}else{
						msg.what  = UPLOAD_PHOTO_SUCCESS;
						msg.arg1 = position;
						msg.obj = response;
					}
					handler.sendMessage(msg);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onProgress(long bytesWritten, long totalSize) {
				// TODO Auto-generated method stub
				super.onProgress(bytesWritten, totalSize);
				//				msg.what = UPLOADING_PHOTO;
				//				msg.arg1 = bytesWritten * 100 / totalSize;
				diaBarPop.setProgress(bytesWritten, totalSize);
				//				handler.sendMessage(msg);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				msg.what = UPLOAD_PHOTO_FAILED;
				msg.obj = throwable.toString();
				handler.sendMessage(msg);
			}
		});

	}

	/** 获取个人图片信息，头像或背景图 */
	public static void getPhoto(String url, String tokenId, final String path , Handler handler) {
		RequestParams params = new RequestParams();
		params.put(Common.USERINFO_TOKENID, tokenId);
		HttpUtil.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				try {
					byte[] binaryData = response.getString("photo").getBytes();
					Bitmap bmp = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
					File file = new File(path);
					// 压缩格式
					CompressFormat format = Bitmap.CompressFormat.JPEG;
					// 压缩比例
					int quality = 100;
					// 若存在则删除
					if (file.exists())
						file.delete();
					// 创建文件
					file.createNewFile();

					OutputStream stream = new FileOutputStream(file);
					// 压缩输出
					bmp.compress(format, quality, stream);
					// 关闭
					stream.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
			}
		});
	}


	/** 获取购物车信息 */
	public static void getcart(final Context context,String url,String userid, final Handler handler) {
		RequestParams params = new RequestParams();
		//		params.put(Common.USER_ID, "54780d447eac676ae8cdcd44");
		params.put(Common.USER_ID, userid);
		HttpUtil.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				Message msg = handler.obtainMessage();
				msg.what = GET_CART_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("get cart failed-------");
				Message msg = handler.obtainMessage();
				msg.what = GET_CART_FAILED;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}



	/**
	 * 加入购物车
	 * @param userid
	 * @param storeId
	 * @param productId
	 * @param qty 加入的数量
	 * @param productName 商品名字
	 * @param photoId 加入的图片的id,仅仅是购买照片的时候需要
	 * @param price 商品价格
	 * @param photoImage 商品预览图
	 * @param productDescription 商品描述
	 * @param promotionId 套餐
	 * @param sourcePrice 商品原价
	 * @param handler
	 */
	public static void addtocart(String userid,String storeId,String productId, int qty, String productName, 
			String photoId, Double price, String photoImage, String productDescription, String promotionId, Double sourcePrice,final Handler handler,JSONArray embedphotos, boolean buynow) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.ADD_TO_CART);

		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userid);
		params.put(Common.IS_JUST_BUY, buynow);
		params.put(Common.STORE_ID, storeId);
		params.put(Common.PRODUCT_ID, productId);
		params.put(Common.QTY, qty);
		params.put(Common.PRODUCT_NAME, productName);
		params.put(Common.PHOTO_ID, photoId);
		params.put(Common.PRICE, price);
		params.put(Common.PHOTO_IMAGE, photoImage);
		System.out.println("photoimage = "+photoImage);
		params.put(Common.PRODUCT_DESCRIPTION, productDescription);
		params.put(Common.PROMOTION_ID, promotionId);
		params.put(Common.SOURCE_PRICE, sourcePrice);
		params.put(Common.EMBEDPHOTOS, embedphotos);
		System.out.println("photoid=="+photoId+"storeid=="+storeId+"userid=="+userid);
		//System.out.println("params == "+params.toString());
		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				if (response.has("result")) {
					System.out.println("result===="+response);
					Message msg = handler.obtainMessage();
					msg.what = ADD_TO_CART_SUCCESS;
					try {
						msg.obj = response.getJSONObject("result");
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					handler.sendMessage(msg);
				}


			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("faile"+errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = ADD_TO_CART_FAILED;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}

		});
	}

	/** 删除购物车信息 */
	public static void deletecart(final Context context,String url,String userid, com.alibaba.fastjson.JSONArray cartItemId, final Handler handler) {
		System.out.println("delete cart info");
		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userid);
		params.put(Common.CART_ITEM_IDS, cartItemId);
		HttpUtil.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println(response);
				Message msg = handler.obtainMessage();
				msg.what = DELETE_CART_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = DELETE_CART_FAILED;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}

	/**
	 * 修改购物车信息
	 * @param url
	 * @param userid
	 * @param cartItem jsonobject对象
	 * @param handler
	 * @param postion 选中的position
	 * @param type 添加还是减少
	 */
	public static void modifycart(String url,String userid, JSONObject cartItem, final Handler handler, final int postion, String type) {
		System.out.println("modify cart info");
		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userid);
		params.put(Common.ITEM, cartItem);
		System.out.println("传递的参数的jsonobject对象="+cartItem.toString());
		HttpUtil.post(url, params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("modify cart=="+response);
				//				Message msg = handler.obtainMessage();
				//				msg.what = DELETE_CART_SUCCESS;
				//				msg.arg1 = postion;
				//				msg.obj = response;
				//				handler.sendMessage(msg);

			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}


	/** 获取地点信息 */
	public static void getLocationInfo(final Context context, final Handler handler) {
		StringBuffer sb2 = new StringBuffer();
		sb2.append(Common.BASE_URL).append(Common.GET_ALL_LOCATIONS_OF_ALBUM_GROUP);
		final Message message = handler.obtainMessage();
		HttpUtil.get(sb2.toString(),null, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				ACache.get(context).put(Common.LOCATION_INFO, response.toString());
				message.what = GET_LOCATION_SUCCESS;
				message.obj = response;
				handler.sendMessage(message);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				message.what = GET_LOCATION_FAILED;
				handler.sendMessage(message);
			}

		});
	}

	/**
	 * 获取全部商品
	 * @param handler
	 * @param storeId
	 * @param language
	 */
	public static void getAllGoods(final Handler handler,String storeId, String language) {
		RequestParams params = new RequestParams();
		params.put(Common.STORE_ID, storeId);
		params.put(Common.LANGUAGE_NAME, language);
		StringBuffer sb = new StringBuffer();
//		sb.append(Common.BASE_URL).append(Common.GET_ALL_GOODS);
		HttpUtil.post(sb.toString(), params, new JsonHttpResponseHandler() {

			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				System.out.println("duixiang"+response);
				Message msgMessage = handler.obtainMessage();
				msgMessage.what = GET_ALL_GOODS_SUCCESS;
				msgMessage.obj = response;
				handler.sendMessage(msgMessage);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println(errorResponse+"3");
				Message msgMessage = handler.obtainMessage();
				msgMessage.what = GET_ALL_GOODS_FAILED;
				handler.sendMessage(msgMessage);
			}
		});
	}

	/**
	 * 获取PPP商品
	 * @param context
	 * @param ipString
	 * @param handler
	 * @param language
	 */
	public static void getPPP(final Context context, String ipString ,final Handler handler, String language) {
		RequestParams params = new RequestParams();
		StringBuffer sb = new StringBuffer();
		sb.append(Common.BASE_URL);
		sb.append(Common.GET_PHOTO_PASS_PLUS);
		params.put(Common.IP, ipString);
		params.put(Common.LANGUAGE_NAME, language);
		HttpUtil.post(sb.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				Message msg = handler.obtainMessage();
				msg.what = GET_PHOTOPASSPLUS_SUCCESS;
				msg.obj = response;
				ACache.get(context).put(Common.PPP_GOOD, response.toString());
				handler.sendMessage(msg);
				System.out.println("gkhgkhgk"+response);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = GET_PHOTOPASSPLUS_FAILED;
				handler.sendMessage(msg);
			}

		});
	}

	/**
	 * 获取banner商品详情
	 * @param context
	 * @param targetURL 请求的URL
	 * @param handler
	 * @param language
	 */
	public static void getDetailGood(final Context context, String targetURL ,final Handler handler, String language) {
		RequestParams params = new RequestParams();
		params.put(Common.LANGUAGE_NAME, language);
		StringBuffer sb = new StringBuffer();
		sb.append(Common.BASE_URL);
		sb.append(targetURL);
		HttpUtil.get(sb.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("banner----get detail success-----"+response);
				Message message = handler.obtainMessage();
				message.what = GET_DETAIL_GOOD_SUCCESS;
				message.obj = response;
				handler.sendMessage(message);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("failed---------"+errorResponse);
				handler.sendEmptyMessage(GET_DETAIL_GOOD_FAILED);
			}

		});
	}

	/**
	 * 获取storeid
	 * @param ipString ip地址
	 * @param handler
	 */
	public static void getStoreIdbyIP(String ipString ,final Handler handler) {
		RequestParams params = new RequestParams();
		StringBuffer sb = new StringBuffer();
		sb.append(Common.BASE_URL);
		sb.append(Common.GET_STORE_BY_IP);
		params.put(Common.IP, ipString);
		HttpUtil.post(sb.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				Message msg = handler.obtainMessage();
				msg.what = GET_STOREID_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("get storeid failed");
				handler.sendEmptyMessage(GET_STOREID_FAILED);
			}
		});
	}

	/**
	 * 获取购物车的数量
	 * @param userId
	 * @param handler
	 */
	public static void getcartcount(final Context context,String userId, final Handler handler) {
		RequestParams params = new RequestParams();
		StringBuffer sb = new StringBuffer();
		sb.append(Common.BASE_URL);
		sb.append(Common.GET_CART_COUNT);
		params.put(Common.USER_ID, userId);

		HttpUtil.post(sb.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.err.println("get caount "+ response);
				if (response.has("totalCount")) {
					SharedPreferences sharedPreferences = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
					Editor ed = sharedPreferences.edit();
					try {
						ed.putInt(Common.CART_COUNT, response.getInt("totalCount"));
						ed.commit();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(GET_CART_COUNT_SUCCESS);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("get cartcount failed");
				handler.sendEmptyMessage(GET_CART_COUNT_FAILED);
			}
		});
	}

	/**
	 * 提交订单，并且返回订单号，支付的时候需要这个订单号
	 * @param context
	 * @param handler
	 * @param userId
	 * @param addressId
	 * @param payType  支付类型  0 支付宝 1 银联  2 VISA信用卡 3 代付 4 分期 5自提 
	 * @param storeAddress  商店地址
	 * @param cartitemids  购物车id
	 * @param deliveryType  快递方式   物流(0)、自提(1)、直送(2),虚拟类商品无须快递(3)
	 */
	public static void addOnOrder(final Context context, final Handler handler, String userId, String addressId, int payType, String storeAddress, JSONArray cartitemids, int deliveryType){
		RequestParams params = new RequestParams();
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Common.BASE_URL);
		stringBuffer.append(Common.ADD_ON_ORDER);
		params.put(Common.USER_ID, userId);
		params.put(Common.ADDRESS_ID, addressId);
		params.put(Common.PAY_TYPE, payType);
		params.put(Common.STORE_ADDRESS, storeAddress);
		params.put(Common.TERMINAL, "android");
		params.put(Common.CART_ITEM_IDS, cartitemids);
		params.put(Common.DELIVERY_TYPE, deliveryType);

		System.out.println("-------->"+params.toString());
		System.out.println("addressid:"+addressId+";\npaytype:"+payType+";\nstoreaddress:"+storeAddress+";\ncartitemids:"+cartitemids.toString());

		HttpUtil.post(stringBuffer.toString(), params, new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				if (response.has("result")) {
					try {
						JSONObject resultJsonObject = response.getJSONObject("result");
						if (resultJsonObject.has("orderId")) {

							System.out.println("true");
							Message message = handler.obtainMessage();
							message.what = GET_ORDER_NO_SUCCESS;
							message.obj = resultJsonObject.getString("orderId");
							handler.sendMessage(message);
						}else {
							System.out.println("failed");
							Message message = handler.obtainMessage();
							message.what = GET_ORDER_NO_FAILED;
							message.obj = resultJsonObject.getString("message");
							handler.sendMessage(message);
						}
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				//				else if (response.has("err")) {
				//					try {
				//						JSONObject resultJsonObject = response.getJSONObject("err");
				//						if (resultJsonObject.has("message")) {
				//							
				//							System.out.println("false");
				//							Message message = handler.obtainMessage();
				//							message.what = GET_ORDER_NO_FAILED;
				//								message.obj = resultJsonObject.getString("message");
				//							handler.sendMessage(message);
				//						}
				//					} catch (JSONException e1) {
				//						// TODO Auto-generated catch block
				//						e1.printStackTrace();
				//					}
				//					
				//					System.out.println("false");
				//				}
				System.out.println("add on order========"+response);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("add on order ==========failed======"+ errorResponse);
				Message message = handler.obtainMessage();
				message.what = GET_ORDER_NO_FAILED;
				message.obj = "Failed";
				handler.sendMessage(message);
			}
		});
	}

	/**
	 * 获取收货地址信息
	 * @param userid
	 * @param handler
	 */
	public static void getAddress(String userid, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.GET_ADDRESS);

		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userid);

		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("result===="+response);
				Message msg = handler.obtainMessage();
				msg.what = GET_ADDRESS_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = GET_ADDRESS_FAILED;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}


	/**
	 * 添加收货地址信息
	 * @param userid
	 * @param addressinfo 地址封装后的json对象
	 * @param handler
	 */
	public static void addAddress(String userid, JSONObject addressinfo, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.ADD_ADDRESS);

		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userid);
		params.put(Common.ADDRESS_INFO, addressinfo);

		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("result===="+response);
				Message msg = handler.obtainMessage();
				msg.what = ADD_ADDRESS_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}
	/**
	 * 修改地址
	 * @param userid
	 * @param addressinfo 新的地址json对象
	 * @param handler
	 */
	public static void modifyAddress(String userid, JSONObject addressinfo, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.MODIFY_ADDRESS);

		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userid);
		params.put(Common.NEW_ADDRESS_INFO, addressinfo);
		System.out.println(addressinfo+"======jiekouzhongde ");

		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("result===="+response);
				Message msg = handler.obtainMessage();
				msg.what = MODIFY_ADDRESS_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}
	/**
	 * 删除收货地址
	 * @param userid
	 * @param addressId 地址ID
	 * @param handler
	 * @param positon
	 */
	public static void deleteAddress(String userid, String addressId, final Handler handler, final int positon) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.DELETE_ADDRESS);

		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userid);
		params.put(Common.ADDRESS_ID, addressId);

		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("result===="+response);
				Message msg = handler.obtainMessage();
				msg.what = DELETE_ADDRESS_SUCCESS;
				msg.arg1 = positon;
				msg.obj = response;
				handler.sendMessage(msg);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}
	/**
	 * 获取账号下有没有ppp
	 * @param tokenId
	 * @param handler
	 */
	public static final ArrayList<PPPinfo> PPPlist = new ArrayList<PPPinfo>();
	public static void getPPPSByUserId(String tokenId, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.GET_PPPS_BY_USERID);

		RequestParams params = new RequestParams();
		params.put(Common.USERINFO_TOKENID, tokenId);
		System.out.println("get ppp tokenid ======= "+tokenId);
		HttpUtil.get(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				System.out.println("get ppp start====================");
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("get ppp result============"+response);
				if (response.has("PPPList")) {
					PPPlist.clear();
					try {
						JSONArray ppplists = response.getJSONArray("PPPList");
						for (int i = 0; i < ppplists.length(); i++) {
							JSONObject ppplist = ppplists.getJSONObject(i);
							PPPinfo ppPinfo = new PPPinfo();
							//							ppPinfo.PPlist = ppplist.getString("PPList");
							ppPinfo.PPPCode = ppplist.getString("PPPCode");
							ppPinfo.capacity = ppplist.getInt("capacity");
							ppPinfo.days = ppplist.getInt("days");
							ppPinfo.PPP_ID = ppplist.getString("_id");
							ppPinfo.ownOn = AppUtil.GTMToLocal(ppplist.getString("ownOn")).substring(0, 10).toString();
							Log.d(TAG, "after translate = "+ ppPinfo.ownOn);
							String str = ppplist.getString("bindInfo");
							JSONArray bindInfos = new JSONArray(str);

							//bindInfos.length()  等于 3
							for (int j = 0; j < bindInfos.length(); j++) {
								BindPPInfo bindInfo = new BindPPInfo();
								JSONObject bindInfoObj = (JSONObject) bindInfos.get(j);
								bindInfo.customerId = bindInfoObj.getString("customerId");
								bindInfo.userids = bindInfoObj.getJSONArray("userIds").toString();  //分割成数组，暂时没有数据
								//暂时 为空。 没有字段
								bindInfo.bindDate = bindInfoObj.getString("bindDate");
								//								bindInfo.bindDate = "写死了";
								ppPinfo.bindInfo.add(bindInfo);
							}
							Log.e("==========","ppPinfo.bindInfo.size():  " +ppPinfo.bindInfo.size());
							PPPlist.add(ppPinfo);
							//							Log.e("＝＝＝＝＝＝＝＝", "bindInfo size :"+PPPlist.get(0).bindInfo.get(0).customerId);
						}
						Log.e("PPPlist", "PPPlist size:"+PPPlist.size());

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				Message msg = handler.obtainMessage();
				msg.what = GET_PPP_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("err================="+errorResponse);
				handler.sendEmptyMessage(GET_PPP_FAILED);
			}
		});

	}	
	/**
	 * 找到用户名下所有的PP码
	 * @param tokenId
	 * @param handler
	 */
	public static void getPPSByUserId(String tokenId, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.GET_PPS_BY_USERID);
		RequestParams params = new RequestParams();
		params.put(Common.USERINFO_TOKENID, tokenId);
		Log.d(TAG, "getPPSByUserId get ppcode tokenid ======= "+tokenId);
		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				Log.d(TAG, "getPPSByUserId get pp result============"+response);
				Message msg = handler.obtainMessage();
				msg.what = GET_PPS_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Log.d(TAG, "getPPSByUserId err============"+errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = GET_PPS_FAILED;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}

	public static final ArrayList<PPinfo> PPlist = new ArrayList<PPinfo>();
	public static void getPPsByPPPAndDate(String tokenId, String pppcode, String binddate, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.GET_PPS_BY_PPP_AND_DATE);

		RequestParams params = new RequestParams();
		params.put(Common.USERINFO_TOKENID, tokenId);
		params.put(Common.PPPCode, pppcode);
		params.put(Common.bindDate, binddate);
		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				System.out.println("get pp by ppp and date start====================");
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("get pp result============"+response);
				if (response.has("PPList")) {
					PPlist.clear();
					try {
						JSONArray pplists = response.getJSONArray("PPList");
						for (int i = 0; i < pplists.length(); i++) {
							JSONObject pplist = pplists.getJSONObject(i);
							PPinfo pPinfo = new PPinfo();
							pPinfo.setPpCode(pplist.getString("customerId"));
							pPinfo.setPhotoCount(pplist.getInt("photoCount"));
							pPinfo.setShootDate(pplist.getString("shootDate"));
							PPlist.add(pPinfo);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Message msg = handler.obtainMessage();
				msg.what = GET_PP_SUCCESS;
				msg.obj = response;
				handler.sendMessage(msg);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("err================="+errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});

	}

	/**
	 * 将pp绑定到ppp
	 * @param tokenid
	 * @param pps
	 * @param binddate
	 * @param ppp
	 * @param handler
	 */
	public static void bindPPsToPPP(String tokenid, JSONArray pps, String binddate, String ppp, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.BIND_PPS_TO_PPP);

		RequestParams params = new RequestParams();
		params.put(Common.USERINFO_TOKENID, tokenid);
		params.put(Common.PPS, pps);
		params.put(Common.bindDate, binddate);
		params.put(Common.ppp1, ppp);
		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				System.out.println("get pp by ppp and date start====================");
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("get pp result============"+response);
				Message message = handler.obtainMessage();

				try {
					if (response.has("error")) {
						message.what = FAILURE;
						message.obj = response.getJSONObject("error").get("type");
					}else {
						message.what = SUCCESS;
					}
					handler.sendMessage(message);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("err================="+errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);
				System.out.println("failed ========"+responseString);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONArray errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("failed array==============="+errorResponse);
			}
		});

	}
	/**
	 * 通过photoid将pp绑定到ppp，此方法只通过购买图片的时候调用
	 * @param tokenid
	 * @param photoid
	 * @param pppcode
	 * @param handler
	 */
	public static void bindPPToPPByPhotoId(String tokenid, String photoid, String pppcode, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.BIND_PP_TO_PPP_BY_PHOTOID);

		RequestParams params = new RequestParams();
		params.put(Common.USERINFO_TOKENID, tokenid);
		params.put(Common.PHOTO_ID, photoid);
		params.put(Common.PPPCode, pppcode);
		System.out.println("info:"+tokenid+"_"+photoid+"_"+pppcode);
		HttpUtil.get(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				System.out.println("start====================");
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("result============"+response);
				Message message = handler.obtainMessage();
				//				
				try {
					if (response.has("error")) {
						message.what = FAILURE;
						message.obj = response.getJSONObject("error").getString("message");
					}else {
						message.what = SUCCESS;
					}
					handler.sendMessage(message);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("err================="+errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);
				System.out.println("failed ========"+responseString);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONArray errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("failed array==============="+errorResponse);
			}
		});

	}

	/**
	 * 检查扫描的结果是否正确，并且返回是否已经被使用
	 * @param code
	 * @param handler
	 */
	public static void checkCodeAvailable(String code, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.CHECK_CODE_AVAILABLE);

		RequestParams params = new RequestParams();
		params.put(Common.CODE, code);
		HttpUtil.get(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("result============"+response);
				Message message = handler.obtainMessage();
				try {
					if (response.has("error")) {
						System.out.println("---------error");
						message.what = CHECK_CODE_FAILED;
						message.obj = response.getJSONObject("error");
					}else {
						System.out.println("----------->success");
						message.what = CHECK_CODE_SUCCESS;
						message.obj = response;
					}

					handler.sendMessage(message);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("check code fialed==========="+errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});
	}


	/**
	 * 获取订单信息
	 * @param userId
	 * @param handler
	 */
	public static void getOrderInfo(String userId, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.GET_ALL_ORDERS);

		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userId);
		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("result============"+response);
				Message message = handler.obtainMessage();
				message.what = GET_ORDER_SUCCESS;
				message.obj = response;
				handler.sendMessage(message);
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("check code fialed==========="+errorResponse);
				Message message = handler.obtainMessage();
				message.what = GET_ORDER_FAILED;
				message.obj = errorResponse;
				handler.sendMessage(message);
			}
		});
	}

	/**
	 * 删除订单信息
	 * @param handler
	 */
	public static void deleteOrder(String userId, String orderId, final OrderInfo groupInfo,
			final ArrayList<CartItemInfo> childInfo, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.DELETE_ORDER);
		RequestParams params = new RequestParams();
		params.put(Common.USER_ID, userId);
		params.put(Common.ORDER_ID, orderId);
		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {


			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				Log.e("statusCode", "statusCode:"+statusCode);
				super.onSuccess(statusCode, headers, response);
				System.out.println("result===="+response);
				Log.e("response", "response:"+response);
				//				Message msg = handler.obtainMessage();
				//				msg.what = DELETE_ADDRESS_SUCCESS;
				//				msg.arg1 = positon;
				//				msg.obj = response;
				//				handler.sendMessage(msg);
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers,String responseString) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, responseString);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);
				if(statusCode == 200){
					Message msg = handler.obtainMessage();
					Bundle b = new Bundle();
					b.putParcelable("group", groupInfo);
					b.putParcelableArrayList("child", childInfo);
					msg.what = DELETE_ORDER_SUCCESS;
					msg.setData(b);
					handler.sendMessage(msg);
				}
				Log.e("statusCode ", "statusCode:"+statusCode);
				Log.e("statusCode", " onFailure string : "+responseString);
			}
		});
	}


	/**
	 * 根据时间查找 PP
	 * @param tokenId
	 * @param bindDate
	 */
	public static void getPPByDate(String tokenId,String bindDate,final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.GET_PP_BY_DATE);
		RequestParams params = new RequestParams();
		params.put(Common.USERINFO_TOKENID, tokenId);
		params.put(Common.bindDate, bindDate);
		HttpUtil.get(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("getPPByDate:"+response);
				if (response.has("PPList")) {
					PPlist.clear();
					try {
						JSONArray pplists = response.getJSONArray("PPList");
						for (int i = 0; i < pplists.length(); i++) {
							JSONObject pplist = pplists.getJSONObject(i);
							PPinfo pPinfo = new PPinfo();
							pPinfo.setPpCode(pplist.getString("customerId"));
							pPinfo.setPhotoCount(pplist.getInt("photoCount"));
							pPinfo.setShootDate(pplist.getString("shootDate"));
							PPlist.add(pPinfo);
						}
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Message msg = handler.obtainMessage();
					msg.what = GET_PP_SUCCESS;
					msg.obj = response;
					handler.sendMessage(msg);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				handler.sendEmptyMessage(GET_PP_FAILED);
			}



		});
	}




	/**
	 * 将pp绑定到ppp
	 * @param tokenid
	 * @param pps
	 * @param ppp
	 * @param handler
	 */
	public static void bindPPsDateToPPP(String tokenid, JSONArray pps, String ppp, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.BIND_PPS_DATE_TO_PPP);

		RequestParams params = new RequestParams();
		params.put(Common.USERINFO_TOKENID, tokenid);
		params.put(Common.PPS, pps);
		params.put(Common.ppp1, ppp);
		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onStart() {
				// TODO Auto-generated method stub
				super.onStart();
				System.out.println("get pp by ppp and date start====================");
			}
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				System.out.println("get pp result============"+response);
				Message message = handler.obtainMessage();

				try {
					if (response.has("error")) {
						message.what = FAILURE;
						message.obj = response.getJSONObject("error").get("type");
					}else {
						message.what = SUCCESS;
					}
					handler.sendMessage(message);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				System.out.println("err================="+errorResponse);
				Message msg = handler.obtainMessage();
				msg.what = BIND_PP_FAILURE;
				msg.obj = errorResponse;
				handler.sendMessage(msg);
			}
		});

	}

	/**
	 * 隐藏PP
	 * @param params
	 * @param handler
	 */
	public static void hidePPs(RequestParams params, final Handler handler) {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.HIDE_PPS);

		HttpUtil.post(sBuffer.toString(), params, new JsonHttpResponseHandler() {


			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				Log.v("========statusCode", "statusCode:"+statusCode);
				Message message = handler.obtainMessage(HIDE_PP_SUCCESS);
				message.obj = response;
				handler.sendMessage(message);
				Log.v("=========response", "response:"+response);
			}			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				Log.v("========statusCode", "statusCode:"+statusCode);
				Message message = handler.obtainMessage(HIDE_PP_FAILED);
				message.obj = errorResponse;
				handler.sendMessage(message);
				Log.v("=========response", "response:"+errorResponse);
			}

		});
	}
	
	/**
	 * 获取最新的边框以及饰品信息
	 * @param lastUpdateTime 上次更新时间
	 * @param handler
	 */
	public static void getLastContent(String lastUpdateTime, final Handler handler){
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append(Common.BASE_URL);
		sBuffer.append(Common.GET_LASTEST_CONTENT);

		RequestParams params = new RequestParams();
		params.put(Common.LAST_UPDATE_TIME, lastUpdateTime);
		HttpUtil.get(sBuffer.toString(), params, new JsonHttpResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, headers, response);
				Message message = handler.obtainMessage();
				message.what = GET_LAST_CONTENT_SUCCESS;
				message.obj = response;
				handler.sendMessage(message);
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, throwable, errorResponse);
				handler.sendEmptyMessage(GET_LAST_CONTENT_FAILED);
			}
		});
	}
	
	
	/**
	 * socket链接后处理方法
	 * @param tokenId
	 */

//	public static void noticeSocketConnect(String tokenId){
//		StringBuffer sBuffer = new StringBuffer();
//		sBuffer.append(Common.BASE_URL);
//		sBuffer.append(Common.APNS_CONNECT);
//		RequestParams params = new RequestParams();
//		params.put(Common.USERINFO_TOKENID, tokenId);
//
//
//		HttpUtil.get(sBuffer.toString(), params, new JsonHttpResponseHandler() {
//			@Override
//			public void onSuccess(int statusCode, Header[] headers,
//					JSONObject response) {
//				// TODO Auto-generated method stub
//				super.onSuccess(statusCode, headers, response);
//				Log.e("＝链接上 访问成功＝＝＝", "＝＝＝");
//				Log.e("response ", "response :"+response);
//			}
//
//			@Override
//			public void onFailure(int statusCode, Header[] headers,
//					Throwable throwable, JSONObject errorResponse) {
//				// TODO Auto-generated method stub
//				super.onFailure(statusCode, headers, throwable, errorResponse);
//				Log.e("＝  链接上 访问失败＝＝＝＝＝＝", "＝＝＝");
//			}
//		});
//
//	}
	
	/**
	 * 手机端退出登录前调用
	 * @param tokenId
	 */
//	public static void noticeSocketDisConnect(String tokenId){
//		StringBuffer sBuffer = new StringBuffer();
//		sBuffer.append(Common.BASE_URL);
//		sBuffer.append(Common.APNS_DISCONNECT);
//		RequestParams params = new RequestParams();
//		params.put(Common.USERINFO_TOKENID, tokenId);
//
//		HttpUtil.get(sBuffer.toString(), params, new JsonHttpResponseHandler() {
//			@Override
//			public void onSuccess(int statusCode, Header[] headers,
//					JSONObject response) {
//				// TODO Auto-generated method stub
//				super.onSuccess(statusCode, headers, response);
//				Log.e("＝退出应用访问成功＝＝＝", "＝＝＝");
//			}
//
//			@Override
//			public void onFailure(int statusCode, Header[] headers,
//					Throwable throwable, JSONObject errorResponse) {
//				// TODO Auto-generated method stub
//				super.onFailure(statusCode, headers, throwable, errorResponse);
//				Log.e("＝退出应用访问失败＝＝＝＝＝＝", "＝＝＝");
//			}
//		});
//	}
	
	
	/**
	 * 手机端接收到推送后，调用清空推送数据
	 * @param tokenId
	 */
//	public static void clearSocketCachePhotoCount(String tokenId,String clearType){
//		StringBuffer sBuffer = new StringBuffer();
//		sBuffer.append(Common.BASE_URL);
//		sBuffer.append(Common.CLEAR_PHOTO_COUNT);
//		RequestParams params = new RequestParams();
//		params.put(Common.USERINFO_TOKENID, tokenId);
//		params.put(Common.CLEAR_TYPE, clearType);
//
//		HttpUtil.get(sBuffer.toString(), params, new JsonHttpResponseHandler() {
//			@Override
//			public void onSuccess(int statusCode, Header[] headers,
//					JSONObject response) {
//				// TODO Auto-generated method stub
//				super.onSuccess(statusCode, headers, response);
//				Log.e("＝收到推送 访问成功＝＝＝", "＝＝＝");
//			}
//
//			@Override
//			public void onFailure(int statusCode, Header[] headers,
//					Throwable throwable, JSONObject errorResponse) {
//				// TODO Auto-generated method stub
//				super.onFailure(statusCode, headers, throwable, errorResponse);
//				Log.e("＝收到推送 访问失败＝＝＝＝＝＝", "＝＝＝");
//			}
//		});
//	}
	
	public final static String checkUpdateTestingString = "{'version': {'_id': '560245482cd4db6c0a3a21e3','appName': 'pictureAir',"
			+ "'version': '2.1.2', 'createdOn': '2015-09-23T06:06:17.371Z', "
			+ " 'mandatory': 'false',  '__v': 0, "
			+ " 'content': '1、新增修改密码功能；\n2、优化注册功能；\n3、调整部分界面UI；\n1、新增修改密码功能；\n2、优化注册功能；\n3、调整部分界面UI；',"
			+ " 'content_EN': '1、Add password modification ;\n2、Improve register function ;\n3、Beautify UI design ;' ,'content_EN':'1、Addpasswordmodification;\n2、Improveregisterfunction;\n3、BeautifyUIdesign;',"
			+ "'downloadChannel':[ {'channel':'360',"
			+ "'downloadUrl':'http://gdown.baidu.com/data/wisegame/1f10e30a23693de1/baidushoujizhushou_16786079.apk'},"
			+ " { 'channel':'tencent',"
			+ "'downloadUrl':'http://mmgr.myapp.com/myapp/gjbig/packmanage/24/2/3/102027/tencentmobilemanager5.7.0_android_build3146_102027.apk'}]}}";

	/**
	 * 检查更新
	 * 
	 * @param context
	 * @param handler
	 * @param thisVerName
	 */
	public static void checkUpdate(final Context context,
			final Handler handler, final String thisVerName,
			final String language) {
		RequestParams params = new RequestParams();
		StringBuffer sb = new StringBuffer();
		sb.append(Common.BASE_URL2);
		sb.append(Common.CHECK_VERSION);
		params.put(Common.APP_NAME, Common.APPLICATION_NAME);
		HttpUtil.get(sb.toString(), params, new JsonHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode,
					Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				if (statusCode == 200) {
					try {
						// *******测试**********、
						response = new JSONObject(checkUpdateTestingString);
						 PictureAirLog.out("RESPONSE---->"+response);
						// *******测试**********、
						if (null != response.optJSONObject("version")) {
							JSONObject jsonObject = response
									.optJSONObject("version");
							String versionName = jsonObject
									.optString("version");
							String mandatory = jsonObject
									.optString("mandatory");
							String content_EN = jsonObject
									.optString("content_EN");
							String content = jsonObject.optString("content");
							String channel = "";
							String downloadUrl = "";
							JSONArray array = jsonObject
									.optJSONArray("downloadChannel");
							for (int i = 0; i < array.length(); i++) {
								channel = array.optJSONObject(i).optString(
										"channel");
								if (Common.UMENG_CHANNEL.equals(channel)) {
									downloadUrl = array.optJSONObject(i)
											.optString("downloadUrl");
									break;
								}
							}

							boolean flag = false;// 为false则不更新
							int[] number = CheckUpdateManager
									.verNameChangeInt(thisVerName);
							int[] newNumber = CheckUpdateManager
									.verNameChangeInt(versionName);
							for (int i = 0; i < number.length; i++) {
								if (number[i] < newNumber[i]) {
									// 需要更新
									flag = true;
									break;
								}
							}
							if (flag) {
								// 更新
								String[] objsStrings = new String[4];
								objsStrings[0] = versionName;
								objsStrings[1] = mandatory;

								objsStrings[3] = downloadUrl;

								if (null != language && language.equals("en")) {
									objsStrings[2] = content_EN;
								} else {
									objsStrings[2] = content;
								}
								Message message = new Message();
								message.what = APK_NEED_UPDATE;
								message.obj = objsStrings;
								handler.sendMessage(message);
							} else {
								handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
							}
						} else {
							handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
						}

					} catch (Exception e) {
						handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
					}
				} else {
					// 访问服务器失败,不更新
					handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
				 System.out.println("-------statusCode=" + statusCode);
				// 访问服务器失败,不更新
				handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				super.onFailure(statusCode, headers, throwable, errorResponse);
				// System.out.println("-------statusCode=" + statusCode);
				// 访问服务器失败,不更新
				handler.sendEmptyMessage(APK_NEED_NOT_UPDATE);
			}
		});
	}

	/**
	 * 下载apk文件
	 * 
	 * @param downloadURL
	 * @param handler
	 */
	public static void downloadAPK(String downloadURL,
			final CustomProgressBarPop customProgressBarPop,
			final String version, final Handler handler) {
		String[] allowedContentTypes = new String[] { "application/vnd.android.package-archive" };
		HttpUtil.get(downloadURL, new BinaryHttpResponseHandler(
				allowedContentTypes) {
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3) {
				// for (Header header : arg1)
				// {
				// Log.i(TAG, header.getName()+" / "+header.getValue());
				// }
				handler.sendEmptyMessage(DOWNLOAD_APK_FAILED);
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2) {
				Message message = handler.obtainMessage();
				message.what = DOWNLOAD_APK_SUCCESS;
				message.obj = arg2;
				handler.sendMessage(message);
				// File downloadAPKFile = new File(Common.DOWNLOAD_APK_PATH);
				// if (!downloadAPKFile.exists()) {
				// downloadAPKFile.mkdirs();
				// }
				// File downloadFile = new File(Common.DOWNLOAD_APK_PATH +
				// "pictureAir_"+ version +".apk");
				// try {
				// downloadFile.createNewFile();
				//
				// FileOutputStream fos = new FileOutputStream(downloadFile);
				// fos.write(arg2);
				// fos.close();
				// handler.sendEmptyMessage(DOWNLOAD_APK_SUCCESS);
				// } catch (IOException e) {
				// // TODO Auto-generated catch block
				// e.printStackTrace();
				// downloadFile.delete();
				// handler.sendEmptyMessage(DOWNLOAD_APK_FAILED);
				// }
			}

			@Override
			public void onProgress(long bytesWritten, long totalSize) {
				customProgressBarPop.setProgress(bytesWritten, totalSize);
			}
		});
	}
}
