package com.pictureair.photopass.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.GoodsInfoJson;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.widget.PWToast;
import com.trello.rxlifecycle.components.RxActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 扫码的管理类
 * @author bauer_bao
 *
 */
public class DealCodeUtil {
	private Context context;
	private String cartId;
	private Handler handler;
	private String codeType;
	private PWToast myToast;
	private String dealWay;
	private boolean isInputAct;
	private GoodsInfo goodsInfo;
	private String[] photoUrls;

	/**
	 * code处理失败
	 */
	public static final int DEAL_CODE_FAILED = 2;
	/**
	 * code处理成功
	 */
	public static final int DEAL_CODE_SUCCESS = 3;

	/**
	 * 处理递推code成功
	 */
	public static final int DEAL_CHID_CODE_SUCCESS = 5;

	/**
	 * 需要返回信息，如果是coupon需要将couponInfo返回
	 */
	public static final int STATE_RETURN_MSG = 1;

	/**
	 * 绑定pp成功，不需要返回信息
	 */
	public static final int STATE_ADD_PP_TO_USER_NOT_RETURN_SUCCESS = 4;

	/**
	 * 正常流程进入，并且绑定ppp成功，不需要将结果返回
	 */
	public static final int STATE_ADD_PPP_TO_USER_NOT_RETURN_SUCCESS = 3;

	/**
	 * 绑定coupon成功，不需要返回信息
	 */
	public static final int STATE_ADD_COUPON_TO_USER_NOT_RETURN_SUCCESS = 2;

	private int id = 0;
	
	public DealCodeUtil(Context context, Intent intent, boolean isInputAct, Handler handler) {
		this.context = context;
		this.handler = handler;
		myToast = new PWToast(context);
		dealWay = intent.getStringExtra("type");
		this.isInputAct = isInputAct;
	}

	/**
	 * 检查code失败的情况
	 */
	private void checkCodeAvailableFailed(){
		if (dealWay != null && dealWay.equals("coupon")) {
			id = R.string.incorrect_coupon;
		} else {
			id = R.string.http_error_code_6136;
		}
		myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
		handler.sendEmptyMessage(DEAL_CODE_FAILED);
	}
	
	/**
	 * 处理正常的二维码
	 * @param code
	 */
	public void startDealCode(String code){
		checkCodeAvailable(code);
	}

	/**
	 * 处理正常的二维码
	 * @param code
	 */
	public void startDealCode(String code, boolean isInputAct){
		this.isInputAct = isInputAct;
		checkCodeAvailable(code);
	}

	/**
	 * 检查code是否可用
	 * @param code
     */
	private void checkCodeAvailable(final String code) {
		PictureAirLog.d("check code available");
		API2.checkCodeAvailable(code)
				.compose(((RxActivity) context).<JSONObject>bindToLifecycle())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new RxSubscribe<JSONObject>() {
					@Override
					public void _onNext(JSONObject jsonObject) {
						PictureAirLog.out("check code success--->" + jsonObject.toString());
						String result = jsonObject.getString("codeType");
						if (result == null || "invalid".equals(result.toString())) {
							checkCodeAvailableFailed();
							return;
						} else if ("photoPass".equals(result.toString())) {
							codeType = "pp";
						} else if ("photoPassPlus".equals(result.toString())) {
							codeType = "ppp";
						} else if ("coupon".equals(result.toString())) {
							codeType = "coupon";
						}

						if (TextUtils.isEmpty(codeType)) {//为空
							checkCodeAvailableFailed();
							return;
						}

						PictureAirLog.out("codetype--->" + codeType + " dealway--->" + dealWay);
						if (dealWay != null) {//如果从ppp、pp或者coupon页面进来，卡的类型不一致，直接返回，退出，一致，则添加
							if (!dealWay.equals(codeType)) {//类型不一致
								if (dealWay.equals("ppp") || dealWay.equals("pp")) {//ppp或者pp
									PictureAirLog.out("--------->need call back");
									Bundle bundle = new Bundle();
									bundle.putInt("status", STATE_RETURN_MSG);
									bundle.putString("result", "notSame");
									handler.obtainMessage(DEAL_CODE_SUCCESS, bundle).sendToTarget();
								} else {//coupon
									id = R.string.incorrect_coupon;
									myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
									handler.sendEmptyMessage(DEAL_CODE_FAILED);
									return;
								}
							} else {//卡类型一致
								getInfo(code, codeType);
							}
						} else {//故事页面
							getInfo(code, codeType);

						}
					}

					@Override
					public void _onError(int status) {
						netWorkError(status);
					}

					@Override
					public void onCompleted() {

					}
				});
	}

	/**
	 * 请求错误
	 * @param status
     */
	private void netWorkError(int status){
		PictureAirLog.out("check code failed: " + status);
		id = ReflectionUtil.getStringId(context, status);

		if (dealWay != null && (dealWay.equals("ppp") || dealWay.equals("pp")) && //如果从ppp页面或者pp页面过来，需要返回错误类型数据，并且需要跳转到对应的activity
				!isInputAct) {//如果不是手动输入页面
			handler.obtainMessage(DEAL_CODE_FAILED, id).sendToTarget();
		}else {//弹出错误提示
			if (dealWay != null && dealWay.equals("coupon")) {
				id = R.string.incorrect_coupon;
			}
			myToast.setTextAndShow(id, Common.TOAST_SHORT_TIME);
			handler.sendEmptyMessage(DEAL_CODE_FAILED);
		}
	}

	/**
	 * 处理递推二维码
	 * 1.获取商品数据
	 * 2.获取ppp数据
	 * 3.加入购物车
	 * 4.进入下单页面
	 * @param code
	 */
	public void startDealChidCode(String code){
		getGoods();
	}

	/**
	 * 初始化数据
	 */
	private void getGoods() {
		//从缓层中获取数据
		Observable.just(ACache.get(context).getAsString(Common.ALL_GOODS))
				.subscribeOn(Schedulers.io())
				.flatMap(new Func1<String, Observable<JSONObject>>() {
					@Override
					public Observable<JSONObject> call(String s) {
						if (!TextUtils.isEmpty(s)) {
							PictureAirLog.d("goods is not null");
							return Observable.just(JSONObject.parseObject(s));

						} else {
							PictureAirLog.d("goods is null");
							//从网络获取商品,先检查网络
							return API2.getGoods()
									.map(new Func1<JSONObject, JSONObject>() {
										@Override
										public JSONObject call(JSONObject jsonObject) {
											ACache.get(MyApplication.getInstance()).put(Common.ALL_GOODS, jsonObject.toString(), ACache.TIME_DAY);
											return jsonObject;
										}
									});
						}
					}
				})
				//解析json
				.map(new Func1<JSONObject, GoodsInfo>() {
					@Override
					public GoodsInfo call(JSONObject jsonObject) {
						PictureAirLog.d("parse goods json need add to cart");
						List<GoodsInfo> allGoodsList1 = new ArrayList<>();
						GoodsInfoJson goodsInfoJson = JsonTools.parseObject(jsonObject.toString(), GoodsInfoJson.class);//GoodsInfoJson.getString()
						if (goodsInfoJson != null && goodsInfoJson.getGoods().size() > 0) {
							allGoodsList1.addAll(goodsInfoJson.getGoods());
						}
						//获取PP+
						for (GoodsInfo goods : allGoodsList1) {
							if (goods.getName().equals(Common.GOOD_NAME_PPP)) {
								goodsInfo = goods;
								break;
							}
						}
						//封装购物车宣传图
						photoUrls = new String[goodsInfo.getPictures().size()];
						for (int i = 0; i < goodsInfo.getPictures().size(); i++) {
							photoUrls[i] = goodsInfo.getPictures().get(i).getUrl();
						}
						return goodsInfo;
					}
				})
				//加入购物车
				.flatMap(new Func1<GoodsInfo, Observable<JSONObject>>() {
					@Override
					public Observable<JSONObject> call(GoodsInfo goodsInfo) {
						PictureAirLog.d("start add to goods key:" + goodsInfo.getGoodsKey());
						//调用addToCart API1
						return API2.addToCart(goodsInfo.getGoodsKey(), 1, true, null);
					}
				})
				.observeOn(AndroidSchedulers.mainThread())
				.compose(((RxActivity) context).<JSONObject>bindToLifecycle())
				.subscribe(new RxSubscribe<JSONObject>() {
					@Override
					public void _onNext(JSONObject jsonObject) {
						PictureAirLog.d("add to cart success--> " + jsonObject);
						int currentCartCount = SPUtils.getInt(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, 0);
						SPUtils.put(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.CART_COUNT, currentCartCount + 1);
						cartId = jsonObject.getString("cartId");
						PictureAirLog.d("cartid--> " + cartId);

					}

					@Override
					public void _onError(int status) {
						myToast.setTextAndShow(R.string.http_error_code_401, Common.TOAST_SHORT_TIME);
						handler.sendEmptyMessage(DEAL_CODE_FAILED);
					}

					@Override
					public void onCompleted() {
						//生成订单
						ArrayList<CartItemInfo> orderinfoArrayList = new ArrayList<>();
						CartItemInfo cartItemInfo = new CartItemInfo();
						cartItemInfo.setCartId(cartId);
						cartItemInfo.setProductName(goodsInfo.getName());
						cartItemInfo.setProductNameAlias(goodsInfo.getNameAlias());
						cartItemInfo.setUnitPrice(goodsInfo.getPrice());
						cartItemInfo.setEmbedPhotos(new ArrayList<CartPhotosInfo>());
						cartItemInfo.setDescription(goodsInfo.getDescription());
						cartItemInfo.setQty(1);
						cartItemInfo.setStoreId(goodsInfo.getStoreId());
						cartItemInfo.setPictures(photoUrls);
						cartItemInfo.setPrice(goodsInfo.getPrice());
						cartItemInfo.setCartProductType(3);

						orderinfoArrayList.add(cartItemInfo);

						Bundle bundle = new Bundle();
						bundle.putSerializable("orderinfo", orderinfoArrayList);
						handler.obtainMessage(DEAL_CHID_CODE_SUCCESS, bundle).sendToTarget();
					}
				});
	}

	/**
	 * 根据code开始绑定到user
	 * @param code
	 * @param type
     */
	private void getInfo(final String code, final String type){
		Map<String,Object> params = new HashMap<>();
		PictureAirLog.out("scan result=" + code + ">>" + type);
		String  decryptStr = AESKeyHelper.decryptString(SPUtils.getString(context, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_TOKENID, ""), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0));
		if (decryptStr != null) {
			params.put(Common.USERINFO_TOKENID, decryptStr);
		}
		String urlString;
		if ("pp".equals(type)) {
			PictureAirLog.out("pp");
			if (code != null) {
				params.put(Common.CUSTOMERID, code);
			}
			urlString = Common.BASE_URL_TEST + Common.ADD_CODE_TO_USER;

		}else {
			PictureAirLog.out("ppp or coupon");
			if (code != null) {
				params.put(Common.PPPCode, code);
			}
			urlString = Common.BASE_URL_TEST+Common.BIND_PPP_TO_USER;

		}

		API2.addScanCodeToUser(urlString, params)
				.compose(((RxActivity) context).<JSONObject>bindToLifecycle())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(new RxSubscribe<JSONObject>() {
					@Override
					public void _onNext(JSONObject jsonObject) {
						PictureAirLog.out("add scan code success---->" + type);
						if ("pp".equals(type)) {
							//绑定pp成功
							Bundle bundle2 = new Bundle();
							if (dealWay != null && dealWay.equals("pp")) {//从pp进入
								bundle2.putInt("status", STATE_RETURN_MSG);
								bundle2.putString("result", "ppOK" + code);
								PictureAirLog.out("scan pp ok 2222");
								handler.obtainMessage(DEAL_CODE_SUCCESS, bundle2).sendToTarget();

							} else {
								bundle2.putInt("status", STATE_ADD_PP_TO_USER_NOT_RETURN_SUCCESS);
								PictureAirLog.out("scan pp ok 444");
								handler.obtainMessage(DEAL_CODE_SUCCESS, bundle2).sendToTarget();

							}
						} else {//绑定ppp或coupon成功
							Bundle bundle3 = new Bundle();
							PictureAirLog.out("add ppp code or coupon to user success--->" + dealWay);
							if ("ppp".equals(type)) {//ppp
								if (dealWay != null && dealWay.equals("ppp")) {//从ppp进入
									bundle3.putInt("status", STATE_RETURN_MSG);
									bundle3.putString("result", "pppOK");
									PictureAirLog.out("scan ppp ok 555");
									handler.obtainMessage(DEAL_CODE_SUCCESS, bundle3).sendToTarget();

								}else {
									bundle3.putInt("status", STATE_ADD_PPP_TO_USER_NOT_RETURN_SUCCESS);
									PictureAirLog.out("scan ppp ok 333");
									handler.obtainMessage(DEAL_CODE_SUCCESS, bundle3).sendToTarget();

								}
							} else {//coupon
								//1.从ppp页面进入扫码,不会出现
								//2.从主页进入扫码，直接绑定，也可以返回数据，scan页面也不会处理
								//3.从coupon进入扫码，直接绑定，并且返回数据
								if (dealWay == null) {//从主页进入
									bundle3.putInt("status", STATE_ADD_COUPON_TO_USER_NOT_RETURN_SUCCESS);
									PictureAirLog.out("scan coupon ok 5555");

								} else {//从coupon页面进入
									//解析json
									PictureAirLog.out("coupon---->" + jsonObject.toString());
									CouponInfo couponInfo = JsonUtil.getCouponInfo(jsonObject.getJSONObject("PPP"));
									//将对象放入bundle3中
									bundle3.putInt("status", STATE_RETURN_MSG);
									PictureAirLog.out("coupon---->" + couponInfo == null ? " null " : "not null");
									bundle3.putSerializable("coupon", couponInfo);
									PictureAirLog.out("scan coupon ok");
								}
								handler.obtainMessage(DEAL_CODE_SUCCESS, bundle3).sendToTarget();

							}
						}

					}

					@Override
					public void _onError(int status) {
						//绑定失败
						PictureAirLog.out("error code --->" + status);
						myToast.setTextAndShow(ReflectionUtil.getStringId(context, status), Common.TOAST_SHORT_TIME);
						handler.sendEmptyMessage(DEAL_CODE_FAILED);
					}

					@Override
					public void onCompleted() {

					}
				});
	}

}
