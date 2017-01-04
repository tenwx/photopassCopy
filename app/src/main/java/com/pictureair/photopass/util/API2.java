package com.pictureair.photopass.util;

import android.content.Context;
import android.os.Handler;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.DownloadFileStatus;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.SendAddress;
import com.pictureair.photopass.http.retrofit_progress.ProgressListener;
import com.pictureair.photopass.http.rxhttp.ApiFactory;
import com.pictureair.photopass.http.rxhttp.HttpCallback;
import com.pictureair.photopass.http.rxhttp.PhotoPassAuthApi;
import com.pictureair.photopass.http.rxhttp.RxHelper;
import com.pictureair.photopass.widget.PWProgressBarDialog;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static com.pictureair.photopass.util.API1.GET_NEW_PHOTOS;
import static com.pictureair.photopass.util.API1.GET_OLD_PHOTOS;

/**
 * Created by pengwu on 16/11/9.
 *
 * HttpCallBack中重写 onProgress(数据传输进度) doOnSubscribe(代替Observable 的 doOnSubscribe)
 * .subscribe方法中需要new一个RXsubScribe,RxSubScribe对返回失败进行了处理
 *
 * 示例
 * API2.getSouvenirPhotos(MyApplication.getTokenId(), userPPCode, new HttpCallback() {
 *         @Override
 *       public void onProgress() {
 *          super.onProgress();
 *       }
 *
 *       @Override
 *       public void doOnSubscribe() {
 *          super.doOnSubscribe();
 *          showPWProgressDialog();
 *       }
 *   }).observeOn(AndroidSchedulers.mainThread())
 *       .compose(this.<JSONObject>bindUntilEvent(ActivityEvent.DESTROY))
 *       .subscribe(new RxSubscribe<JSONObject>() {
 *           @Override
 *           public void _onNext(JSONObject jsonObject) {
 *
 *           }
 *
 *           @Override
 *           public void _onError(int status) {
 *
 *           }
 *
 *           @Override
 *           public void onCompleted() {
 *
 *           }
 *   });
 *
 */

public class API2 {
    private static final String TAG = "API";

    public static final int GET_CART_SUCCESS = 4021;

    public static final int UPDATE_PROFILE_NAME = 5012;
    public static final int UPDATE_PROFILE_GENDER = 5013;
    public static final int UPDATE_PROFILE_BIRTHDAY = 5014;
    public static final int UPDATE_PROFILE_COUNTRY = 5015;

    public static final int DOWNLOAD_APK_FAILED = 6010;

    /**
     * 发送设备ID获取tokenId
     *
     * @param context
     */
    public static Observable<JSONObject> getTokenId(final Context context) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.TERMINAL, "android");
        String id = Installation.id(context);
        if (id != null) {
            params.put(Common.UUID, id);
        }
        params.put(Common.APP_ID, AppUtil.md5(PWJniUtil.getAPPKey(Common.APP_TYPE_SHDRPP) + PWJniUtil.getAppSecret(Common.APP_TYPE_SHDRPP)));

        return get(Common.BASE_URL_TEST + Common.GET_TOKENID, params, null);
    }


    /**
     * 登录
     *
     * @param userName
     * @param password
     */
    public static Observable<JSONObject> Login(final String userName, String password, String loginType, String verificationCode) {
        Map<String, Object> params = new HashMap<>();
        PictureAirLog.v("MyApplication.getTokenId()", MyApplication.getTokenId());
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());

        if (userName != null) {
            params.put(Common.USERINFO_USERNAME, userName);
        }
        if (loginType != null) {
            params.put(Common.LOGINTYPE, "verificationCodeLogin");
            params.put(Common.VERIFICATIONCODE, verificationCode);
        }
        if (password != null) {
            params.put(Common.USERINFO_PASSWORD, AppUtil.md5(AppUtil.md5(password) + PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)));
        }

        return post(Common.BASE_URL_TEST + Common.LOGIN, params, null);
    }

    /**
     * 登出账号
     */
    public static Observable<JSONObject> Logout() {
        Map<String, Object> params = new HashMap<>();
        PictureAirLog.v("MyApplication.getTokenId()", MyApplication.getTokenId());
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());

        return post(Common.BASE_URL_TEST + Common.LOGOUT, params, null);
    }

    /**
     * 下载头像或者背景文件
     *
     * @param downloadUrl
     * @param folderPath
     * @param fileName
     */
    public static Observable<InputStream> downloadHeadFile(String downloadUrl, final String folderPath, final String fileName, final HttpCallback callback) {

        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<InputStream> observable = request.download(downloadUrl, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (callback != null) callback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<ResponseBody, Observable<InputStream>>() {
                    @Override
                    public Observable<InputStream> call(ResponseBody responseBody) {
                        return RxHelper.createData(responseBody.byteStream());
                    }
                });
        return observable;

//        BinaryCallBack task = HttpUtil1.asyncDownloadBinaryData(downloadUrl, new HttpCallback() {
//            @Override
//            public void onSuccess(byte[] binaryData) {
//                super.onSuccess(binaryData);
//                try {
//                    File folder = new File(folderPath);
//                    if (!folder.exists()) {
//                        folder.mkdirs();
//                    }
//                    File file = new File(folderPath + fileName);
//                    file.createNewFile();
//                    FileOutputStream fos = new FileOutputStream(file);
//                    fos.write(binaryData);
//                    fos.close();
//                } catch (FileNotFoundException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
//                handler.obtainMessage(DOWNLOAD_FILE_SUCCESS, folderPath + fileName).sendToTarget();
//            }
//
//            @Override
//            public void onFailure(int status) {
//                super.onFailure(status);
//                handler.obtainMessage(DOWNLOAD_FILE_FAILED, status, 0).sendToTarget();
//            }
//
//            @Override
//            public void onProgress(long bytesWritten, long totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//                handler.obtainMessage(DOWNLOAD_FILE_PROGRESS, (int)bytesWritten, (int)totalSize).sendToTarget();
//            }
//        });
//
//        return task;
    }

    /**
     * 注册
     *
     * @param userName name
     * @param password pwd
     */
    public static Observable<JSONObject> Register(final String userName, final String password) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (userName != null) {
            params.put(Common.USERINFO_USERNAME, userName);
        }
        params.put(Common.USERINFO_PASSWORD, AppUtil.md5(password));

        return post(Common.BASE_URL_TEST + Common.REGISTER, params, null);
    }

    /**
     * 发送验证码
     * SEND_SMS_VALIDATE_CODE
     * <p/>
     * note:手机号格式 “＋8615717737873”
     * appID:string，必填，appID
     * phone:string，必填，手机号，
     * language:string,选填,语言，默认为CN，可填写值：CN或EN，
     * msgType:string,选填，默认为register，可选值（forgotPassword,register）
     */
    public static Observable<JSONObject> sendSMSValidateCode(final String tokenId, String phone, String language, boolean isRegister) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PHONE, "+" + phone);
        params.put(Common.LANGUAGE, language.equals(Common.SIMPLE_CHINESE) ? "CN" : "EN");
        params.put(Common.MSG_TYPE, isRegister ? "register" : "forgotPassword");

        PictureAirLog.v(TAG, "sendSMSValidateCode params：" + params.toString());

        return post(Common.BASE_URL_TEST + Common.SEND_SMS_VALIDATE_CODE, params, null);
    }

    /**
     * 判断验证信息是否有效 验证码
     * validateCode
     * <p>
     * tokenId:string，必填，tokenId
     * validateCode:string,必填，验证信息
     * sendTo:string,选填，email或mobile
     * msgType:string,选填，可选值（register,forgotPassword）
     */
    public static Observable<JSONObject> validateCode(final String tokenId, String validateCode, String phoneOremail, boolean isRegister) {

        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (validateCode != null) {
            params.put(Common.VALIDATE_CODE, validateCode);
        }
        params.put(Common.SEND_TO, "+" + phoneOremail);
        params.put(Common.MSG_TYPE, isRegister ? "register" : "forgotPassword");
        PictureAirLog.v(TAG, "validateCode params：" + params.toString());

        return post(Common.BASE_URL_TEST + Common.VALIDATE_CODE_URL, params, null);
    }


    /**
     * 获取所有的地址信息
     */
    public static Observable<JSONObject> getLocationInfo(String tokenId) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        return get(Common.BASE_URL_TEST + Common.GET_ALL_LOCATIONS_OF_ALBUM_GROUP, params, null);
    }

    /**
     * 获取用户照片
     *
     * @param tokenId 加了 shootDate
     */
    public static Observable<JSONObject> getPhotosByConditions(String tokenId, int type, String receiveOn, String repeatIds, String ppCode, String shootDate, int limit) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (type == GET_NEW_PHOTOS) {
            params.put(Common.GTE_RECEIVE_ON, receiveOn);
            params.put(Common.REPEAT_ID, repeatIds);

        } else if (type == GET_OLD_PHOTOS) {
            params.put(Common.LTE_RECEIVE_ON, receiveOn);
            params.put(Common.REPEAT_ID, repeatIds);

        }
        if (!TextUtils.isEmpty(ppCode)) {
            params.put(Common.CUSTOMERID, ppCode);
        }

        if (!TextUtils.isEmpty(shootDate)) {
            params.put(Common.SHOOTDATE, shootDate);
        }

        params.put(Common.LIMIT, limit);
        PictureAirLog.out("the time of start get photos = " + receiveOn + ",repeatids-->" + repeatIds);
        return get(Common.BASE_URL_TEST + Common.GET_PHOTOS_BY_CONDITIONS, params, null);
    }

    /**
     * 获取照片的最新数据
     *
     * @param tokenId
     */
    public static Observable<JSONObject> getNewPhotosInfo(String tokenId, String photoId) {//, final int id
        Map<String, Object> params = new HashMap<>();
        JSONArray ids = new JSONArray();
        ids.add(photoId);
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (ids != null) {
            params.put(Common.EPPP_IDS, ids.toJSONString());
        }

        return get(Common.BASE_URL_TEST + Common.GET_PHOTOS_BY_CONDITIONS, params, null);
    }

    /**
     * 获取首页轮播照片
     *
     * @param tokenId
     */
    public static Observable<JSONObject> getBannerPhotos(String tokenId) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        return get(Common.BASE_URL_TEST + Common.GET_BANNER_PHOTOS, params, null);
    }

    /**
     * 获取一卡一天的数据
     *
     * @param tokenId
     * @return
     */
    public static Observable<JSONObject> getLocationPhoto(String tokenId) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        return get(Common.BASE_URL_TEST + Common.GET_LOCATION_PHOTOS, params, null);
    }

    /**
     * 检查扫描的结果是否正确，并且返回是否已经被使用
     *
     * @param code
     */
    public static Observable<JSONObject> checkCodeAvailable(String code) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (code != null) {
            params.put(Common.CODE, code);
        }

        return get(Common.BASE_URL_TEST + Common.CHECK_CODE_AVAILABLE, params, null);
    }

    /**
     * 绑定扫描码到用户
     *
     * @param url
     * @param params
     */
    public static Observable<JSONObject> addScanCodeToUser(String url, Map params) {//final String type,

        return post(url, params, null);
    }

    /**
     * 删除网络图片
     *
     * @param tokenId
     */
    public static Observable<JSONObject> removePhotosFromPP(String tokenId, JSONArray ids, String ppCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (ids != null) {
            params.put(Common.SHARE_PHOTO_ID, ids.toJSONString());
        }
        if (ppCode != null) {
            params.put(Common.PP, ppCode);
        }
        PictureAirLog.out("param---->" + params.toString());

        return post(Common.BASE_URL_TEST + Common.REMOVE_PHOTOS_FROME_PP, params, null);
    }

    /**
     * 获取已收藏的地点信息
     *
     * @param tokenId
     */
    public static Observable<JSONObject> getFavoriteLocations(String tokenId) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);

        return get(Common.BASE_URL_TEST + Common.GET_FAVORITE_LOCATIONS, params, null);
    }

    /**
     * 收藏或者取消收藏地址获取已收藏的地点信息
     *
     * @param tokenId    必填，token
     * @param locationId locationId:string，必填，location的locationId
     * @param action     必填，操作（可选值：add，remove），收藏或取消收藏
     */
    public static Observable<JSONObject> editFavoriteLocations(String tokenId, String locationId, String action) {//final int position,
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (locationId != null) {
            params.put(Common.LOCATION_ID, locationId);
        }
        if (action != null) {
            params.put(Common.ACTION, action);
        }

        return post(Common.BASE_URL_TEST + Common.EDIT_FAVORITE_LOCATION, params, null);
    }


    /**
     * 获取最新的边框以及饰品信息
     *
     * @param lastUpdateTime 上次更新时间
     */
    public static Observable<JSONObject> getLastContent(String lastUpdateTime) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append(Common.BASE_URL_TEST);
        sBuffer.append(Common.GET_LASTEST_CONTENT);

        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (lastUpdateTime != null) {
            params.put(Common.LAST_UPDATE_TIME, lastUpdateTime);
        }

        return get(sBuffer.toString(), params, null);
    }

    /**
     * 获取有广告的地点
     */
    public static Observable<JSONObject> getADLocations() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());

        return get(Common.BASE_URL_TEST + Common.GET_AD_LOCATIONS, params, null);
    }

    /***************************************我的模块 start**************************************/


    /**
     * 更新用户头像或头部背景图
     *
     * @param params
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static Observable<JSONObject> updateUserImage(Map<String, RequestBody> params, final int position, final HttpCallback callback) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息

        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<JSONObject> observable = request.upload(Common.BASE_URL_TEST + Common.UPDATE_USER_IMAGE, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (callback != null) callback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (callback != null) callback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(RxHelper.<JSONObject>handleResult());

        return observable;
    }


    /**
     * 上传个人图片信息，头像或背景图
     *
     * @param params
     * @param position 修改图片的时候需要这个参数来定位
     * @throws FileNotFoundException
     */
    public static Observable<JSONObject> SetPhoto(Map<String, RequestBody> params, final int position, final HttpCallback callback) throws FileNotFoundException {
        // 需要更新服务器中用户背景图片信息

        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<JSONObject> observable = request.upload(Common.BASE_URL_TEST + Common.UPLOAD_PHOTOS, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (callback != null) callback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (callback != null) callback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(RxHelper.<JSONObject>handleResult());

        return observable;
    }

    /**
     * 更新用户信息
     *
     * @param tokenId  tokenId
     * @param name     名字
     * @param birthday 生日
     * @param gender   性别
     * @param QQ       qq
     */
    public static Observable<JSONObject> updateProfile(String tokenId, String name, String birthday, String gender, String country, String QQ) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (name != null) {
            params.put(Common.USERINFO_NICKNAME, name);
        }
        if (country != null) {
            params.put(Common.USERINFO_COUNTRY, country);
        }
        if (QQ != null) {
            params.put(Common.USERINFO_QQ, QQ);
        }
        if (birthday != null) {
            params.put(Common.USERINFO_BIRTHDAY, birthday);
        }
        if (gender != null) {
            params.put(Common.USERINFO_GENDER, gender);
        }

        return post(Common.BASE_URL_TEST + Common.UPDATE_PROFILE, params, null);
    }

    /**
     * 获取所有的PP
     * <p>
     * 不一致，缺少final boolean showErrorView
     */
    public static Observable<JSONObject> getPPSByUserId() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());

        return get(Common.BASE_URL_TEST + Common.GET_PPS_BY_USERID, params, null);
    }

    /**
     * 获取账号下所有ppp
     *
     * @param tokenId tokenId
     */
    public static ArrayList<PPPinfo> PPPlist = new ArrayList<>();

    public static Observable<JSONObject> getPPPSByUserId(String tokenId) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);

        return get(Common.BASE_URL_TEST + Common.GET_PPPS_BY_USERID, params, null);
    }

    /**
     * 隐藏PP
     *
     * @param params 参数
     */
    public static Observable<JSONObject> hidePPs(Map params) {

        return post(Common.BASE_URL_TEST + Common.HIDE_PPS, params, null);
    }

    /**
     * 将pp绑定到ppp
     *
     * @param tokenId  token
     * @param pps      pps
     * @param bindDate bind
     * @param ppp      ppp
     */
    public static Observable<JSONObject> bindPPsToPPP(String tokenId, JSONArray pps, String bindDate, String ppp) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.PPS, pps);
        if (bindDate != null) {
            params.put(Common.bindDate, bindDate);
        }
        if (ppp != null) {
            params.put(Common.ppp1, ppp);
        }

        return post(Common.BASE_URL_TEST + Common.BIND_PPS_TO_PPP, params, null);
    }

    /**
     * 绑定PP卡到用户
     */
    public static Observable<JSONObject> addCodeToUser(String ppCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (ppCode != null) {
            params.put(Common.CUSTOMERID, ppCode);
        }

        return get(Common.BASE_URL_TEST + Common.ADD_CODE_TO_USER, params, null);
    }


    /**
     * 扫描PPP并绑定用户
     *
     * @param params params
     */
    public static Observable<JSONObject> bindPPPToUser(Map params) {

        return post(Common.BASE_URL_TEST + Common.BIND_PPP_TO_USER, params, null);
    }


    /**
     * 帮助
     */
    public static Observable<JSONObject> getHelp() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());

        return get(Common.BASE_URL_TEST + Common.ME_HELP, params, null);
    }

    /**
     * 修改密码或者忘记密码接口
     *
     * @param oldPwd 旧密码，修改的时候用到，如果是忘记密码的话，设为null
     * @param newPwd 新密码
     */
    public static Observable<JSONObject> modifyPwd(String oldPwd, String newPwd) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.NEW_PASSWORD, AppUtil.md5(newPwd));
        params.put(Common.OLD_PASSWORD, AppUtil.md5(oldPwd));

        return post(Common.BASE_URL_TEST + Common.MODIFYPWD, params, null);
    }

    /**
     * 使用体验卡绑定未购买的图片
     *
     * @param pppCode  体验卡卡号
     * @param photoIds 绑定的图片
     */
    public static Observable<JSONObject> useExperiencePPP(String pppCode, JSONArray photoIds) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (pppCode != null) {
            params.put(Common.EPPP, pppCode);
        }
        if (photoIds != null) {
            params.put(Common.EPPP_IDS, photoIds.toJSONString());
        }
        PictureAirLog.out("photo ids --->" + photoIds);
        PictureAirLog.out("params--->" + params.toString());

        return post(Common.BASE_URL_TEST + Common.USE_EXPERIENCE_PPP, params, null);
    }

    /**
     * 从用户中移除pp
     *
     * @param ppCode pp码
     */
    public static Observable<JSONObject> removePPFromUser(String ppCode, final HttpCallback callback) {//final int position,
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (ppCode != null) {
            params.put(Common.CUSTOMERID, ppCode);
        }

        return post(Common.BASE_URL_TEST + Common.REMOVE_PP_FROM_USER, params, callback);
    }

    /***************************************我的模块 end**************************************/


    /***************************************Shop模块 start**************************************/


    /**
     * 获取全部商品
     */
    public static Observable<JSONObject> getGoods() {
        PictureAirLog.v(TAG, "getGoods");
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());

        return get(Common.BASE_URL_TEST + Common.GET_GOODS, params, null);
    }


    /**
     * 获取用户购物车信息
     *
     * @param cartIdsArray
     */
    public static Observable<JSONObject> getCarts(JSONArray cartIdsArray) {
        PictureAirLog.out("getCarts---》" + MyApplication.getTokenId());
        final int flag;//表示请求类型： 初始化/选中取消选中
        Map<String, Object> params = new HashMap<>();
//        if (cartIdsArray == null) {
//            flag = -1;
//        } else {
//              flag = GET_CART_SUCCESS;
//        }
        if (cartIdsArray != null && cartIdsArray.size() > 0) {
            params.put("cartItemIds", cartIdsArray.toString());
        }

        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());

        return get(Common.BASE_URL_TEST + Common.GET_CART, params, null);
    }


    /**
     * 添加购物车
     *
     * @param goodsKey    商品项key（必须）
     * @param qty         商品数量(可选)
     * @param isJustBuy   是否立即购买(可选)
     * @param embedPhotos 商品项对应配备的照片id与ppcode映射数组数据(可选)
     */
    public static Observable<JSONObject> addToCart(String goodsKey, int qty, Boolean isJustBuy, JSONArray embedPhotos) {
        PictureAirLog.v(TAG, "addToCart");
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (goodsKey != null) {
            params.put(Common.GOODS_KEY, goodsKey);
        }
        params.put(Common.IS_JUST_BUY, isJustBuy);
        params.put(Common.QTY, qty);
        if (embedPhotos != null) {
            params.put(Common.EMBEDPHOTOS, embedPhotos.toString());
        }

        return post(Common.BASE_URL_TEST + Common.ADD_TO_CART, params, null);
    }

    /**
     * 批量加入购物车
     *
     * @param tokenId
     * @param goods
     */
    public static Observable<JSONObject> batchAddToCarts(String tokenId, String goods) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (goods != null) {
            params.put(Common.GOODS, goods);
        }

        return post(Common.BASE_URL_TEST + Common.BATCH_ADD_TO_CART, params, null);
    }

    /**
     * 修改购物车
     *
     * @param cartId      购物车项id参数(可选,不填时为移除全部)
     * @param goodsKey    商品项key（可选）
     * @param qty         商品数量(可选)
     * @param embedPhotos 商品项对应配备的照片id与ppcode映射数组数据(可选)
     */
    public static Observable<JSONObject> modifyCart(String cartId, String goodsKey, int qty, JSONArray embedPhotos, final PWProgressBarDialog diaBarPop, final HttpCallback callback) {
        PictureAirLog.v(TAG, "modifyCart");
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (goodsKey != null) {
            params.put(Common.GOODS_KEY, goodsKey);
        }
        if (embedPhotos != null) {
            params.put(Common.EMBEDPHOTOS, embedPhotos.toString());
        }
        params.put(Common.QTY, qty);
        String url = Common.BASE_URL_TEST + Common.MODIFY_TO_CART + "/" + cartId;

        return put(url, params, callback);
    }


    /**
     * 移除用户购物车信息
     *
     * @param cartIdsArray 购物车项id参数(可选,不填时为移除全部)
     */
    public static Observable<JSONObject> removeCartItems(JSONArray cartIdsArray) {
        String url = Common.BASE_URL_TEST + Common.DELETE_TO_CART;
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (cartIdsArray != null && cartIdsArray.size() > 0) {
            params.put("cartIdsArray", cartIdsArray.toString());
        }
        PictureAirLog.v(TAG, "params" + params.toString());

        return delete(url, params, null);
    }

    /**
     * 提交订单
     *
     * @param cartItemIds  JSONArray
     * @param deliveryType 物流方式(必须，送货方式,物流(0)、自提(1)、直送(2),虚拟类商品无须快递(3))
     * @param outletId     门店编号门店主键(与addressId互斥,但不能都存在,若物流方式为3则无此条约束)
     * @param addressId    string用户地址id(与outletId互斥,但不能都存在)
     */
    public static Observable<JSONObject> addOrder(JSONArray cartItemIds, int deliveryType, String outletId, String addressId,
                                                  JSONArray couponCodes, JSONObject invoice,
                                                  String channelId, String uid) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (cartItemIds != null) {
            params.put("cartItemIds", cartItemIds.toString());
        }
        params.put("deliveryType", deliveryType);
        params.put("coupons", couponCodes == null ? "" : couponCodes.toString());
        if (deliveryType == 0) {
            //物流
            if (addressId != null) {
                params.put("addressId", addressId);
            }
        } else if (deliveryType == 1) {
            //自提
            if (outletId != null) {
                params.put("outletId", outletId);
            }
        }
        if (null != invoice)
            params.put("invoiceInfo", invoice);

        if (!TextUtils.isEmpty(channelId)) {
            params.put("channelId", channelId);
            params.put("uId", uid);
        }
        PictureAirLog.out("addorder params ------------>" + params.toString());

        return post(Common.BASE_URL_TEST + Common.ADD_ORDER, params, null);
    }


    /**
     * 获取订单信息
     */
    public static Observable<JSONObject> getOrderInfo() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());

        return post(Common.BASE_URL_TEST + Common.GET_ALL_ORDERS, params, null);
    }

    /**
     * 删除订单信息
     */
    public static Observable<JSONObject> removeOrder(String orderId) {//, final OrderInfo groupInfo, final OrderProductInfo childInfo
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (orderId != null) {
            params.put(Common.ORDER_ID, orderId);
        }
        PictureAirLog.v(TAG, "removeOrder params：" + params);

        return post(Common.BASE_URL_TEST + Common.DELETE_ORDER, params, null);
    }

    /**
     * 获得发票的所有地址列表
     */
    public static Observable<JSONObject> getInvoiceAddressList() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());

        return get(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, null);
    }

    /**
     * 获取用户购物车带发票的信息
     *
     * @param cartIdsArray
     */
    public static Observable<JSONObject> getCartsWithInvoice(JSONArray cartIdsArray, boolean isNeedInvoice, JSONArray couponCodes) {
        PictureAirLog.out("getCartsInvoice---》" + MyApplication.getTokenId());
        Map<String, Object> params = new HashMap<>();
        if (cartIdsArray != null && cartIdsArray.size() > 0) {
            params.put("cartItemIds", cartIdsArray.toString());
        }
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());
        params.put("isNeedInvoice", isNeedInvoice);
        params.put("couponCodes", couponCodes.toString());

        return get(Common.BASE_URL_TEST + Common.GET_CART, params, null);
    }

    /**
     * 添加发票的地址
     */
    public static Observable<JSONObject> addInvoiceAddress(SendAddress address) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (address.getName() != null) {
            params.put("consignee", address.getName());
        }
        if (address.getDetailAddress() != null) {
            params.put("detailedAddress", address.getDetailAddress());
        }
        if (address.getArea() != null) {
            params.put("area", address.getArea());
        }
        if (address.getProvince() != null) {
            params.put("provinces", address.getProvince());
        }
        if (address.getCity() != null) {
            params.put("city", address.getCity());
        }
        if (address.getCountry() != null) {
            params.put("county", address.getCountry());
        }
        if (address.getZip() != null) {
            params.put("zip", address.getZip());
        }
        if (address.getMobilePhone() != null) {
            params.put("mobileNum", address.getMobilePhone());
        }
        if (address.getTelePhone() != null) {
            params.put("telephone", address.getTelePhone());
        }
        params.put("defaultChose", address.isSelected());

        return post(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, null);
    }

    /**
     * 修改发票的地址
     */
    public static Observable<JSONObject> modifyInvoiceAddress(SendAddress address) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (address.getAddressId() != null) {
            params.put("addressId", address.getAddressId());
        }
        if (address.getName() != null) {
            params.put("consignee", address.getName());
        }
        if (address.getDetailAddress() != null) {
            params.put("detailedAddress", address.getDetailAddress());
        }
        if (address.getArea() != null) {
            params.put("area", address.getArea());
        }
        if (address.getProvince() != null) {
            params.put("provinces", address.getProvince());
        }
        if (address.getCity() != null) {
            params.put("city", address.getCity());
        }
        if (address.getCountry() != null) {
            params.put("county", address.getCountry());
        }
        if (address.getZip() != null) {
            params.put("zip", address.getZip());
        }
        if (address.getMobilePhone() != null) {
            params.put("mobileNum", address.getMobilePhone());
        }
        if (address.getTelePhone() != null) {
            params.put("telephone", address.getTelePhone());
        }
        params.put("defaultChose", address.isSelected());
        PictureAirLog.out("modify address ------>" + params.toString());

        return put(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, null);
    }

    /**
     * 删除发票的地址
     */
    public static Observable<JSONObject> deleteInvoiceAddress(String[] ids) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put("addressesIds", ids);

        return delete(Common.BASE_URL_TEST + Common.ADDRESS_LIST, params, null);
    }

    /**
     * 购买单张照片
     * 一键放入数码商品至购物车信息
     *
     * @param photoId photoId
     */
    public static Observable<JSONObject> buyPhoto(String photoId) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (photoId != null) {
            params.put(Common.PHOTO_ID, photoId);
        }
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());

        return post(Common.BASE_URL_TEST + Common.BUY_PHOTO, params, null);
    }

    /**
     * 获取门店地址信息
     */
    public static Observable<JSONObject> getOutlets() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, MyApplication.getInstance().getLanguageType());

        return post(Common.BASE_URL_TEST + Common.GET_OUTLET_ID, params, null);
    }

    /**
     * 获取抢单活动信息
     *
     * @param tokenId
     * @param language
     * @return
     */
    public static Observable<JSONObject> getDealingGoods(String tokenId, String language) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.LANGUAGE, language);

        return get(Common.BASE_URL_TEST + Common.GET_ALL_DEALINGS, params, null);
    }

    /***************************************
     * Shop模块 end
     **************************************/

    public final static String checkUpdateTestingString = "{'version': {'_id': '560245482cd4db6c0a3a21e3','appName': 'pictureAir',"
            + "'version': '2.1.4', 'createdOn': '2015-09-23T06:06:17.371Z', "
            + " 'mandatory': 'true',  '__v': 0, "
            + " 'versionOS': ['android'], "
            + " 'content': '1、新增修改密码功能；\n2、优化注册功能；\n3、调整部分界面UI；\n1、新增修改密码功能；\n2、优化注册功能；\n3、调整部分界面UI；',"
            + " 'content_EN': '1、Add password modification ;\n2、Improve register function ;\n3、Beautify UI design ;' ,'content_EN':'1、Addpasswordmodification;\n2、Improveregisterfunction;\n3、BeautifyUIdesign;',"
            + "'downloadChannel':[ {'channel':'website',"
            + "'downloadUrl':'http://www.disneyphotopass.com.cn/downloads/android/photopass/PhotoPassV1.1.0-website.apk'},"
            + " { 'channel':'tencent',"
            + "'downloadUrl':'http://dd.myapp.com/16891/2FA495F1283F48658CEACFF53DB6F856.apk?fsname=com.pictureair.photopass_1.1.1_4.apk'}]}}";


    /**
     * 获取最新的版本信息
     */
    public static Observable<JSONObject> checkUpdate(Context context) {
        final Context c = context;
        String verson = c.getSharedPreferences(Common.SHARED_PREFERENCE_APP, Context.MODE_PRIVATE).getString(Common.APP_VERSION_NAME, "");
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        params.put(Common.VERSION, verson);

        return get(Common.BASE_URL_TEST + Common.CHECK_VERSION, params, null);
    }

//    /**
//     * 下载apk文件
//     *
//     * @param downloadURL 下載路徑
//     * @param handler
//     */
//    public static void downloadAPK(final Context content,String downloadURL, final CustomProgressBarPop customProgressBarPop, final String version, final Handler handler) {
//
//        HttpUtil1.asyncDownloadBinaryData(downloadURL, new HttpCallback() {
//            @Override
//            public void onSuccess(byte[] binaryData) {
//                super.onSuccess(binaryData);
//                handler.obtainMessage(DOWNLOAD_APK_SUCCESS, binaryData).sendToTarget();
//            }
//
//            @Override
//            public void onFailure(int status) {
//                super.onFailure(status);
//                handler.sendEmptyMessage(DOWNLOAD_APK_FAILED);
//            }
//
//            @Override
//            public void onProgress(long bytesWritten, long totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//                customProgressBarPop.setProgress(bytesWritten,totalSize);
//            }
//        });
//    }

//    /**
//     * 下载apk文件
//     * @param content
//     * @param threadInfo
//     * @param handler
//     */
//    public static void downloadAPK2(final Context content,final ThreadInfo threadInfo, final Handler handler) {
//        final Intent intent = new Intent(BreakpointDownloadService.ACTION_UPDATE);
//
//        HttpUtil1.asyncDownloadBinaryData(threadInfo.getUrl(), new HttpCallback() {
//            @Override
//            public void onSuccess(byte[] binaryData) {
//                super.onSuccess(binaryData);
//                handler.obtainMessage(DOWNLOAD_APK_SUCCESS, binaryData).sendToTarget();
//            }
//
//            @Override
//            public void onFailure(int status) {
//                super.onFailure(status);
//                handler.sendEmptyMessage(DOWNLOAD_APK_FAILED);
//            }
//
//            @Override
//            public void onProgress(long bytesWritten, long totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//
//                intent.putExtra("bytesWritten", bytesWritten);
//                intent.putExtra("totalSize", totalSize);
//                content.sendBroadcast(intent);
//            }
//        });
//    }


    /***************************************推送 Start**************************************/
    /**
     * socket链接后处理方法
     */
    public static Observable<JSONObject> noticeSocketConnect() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);

        return get(Common.BASE_URL_TEST + Common.APNS_CONNECT, params, null);
    }


    /**
     * 手机端退出登录前调用
     */
    public static Observable<JSONObject> noticeSocketDisConnect() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);

        return get(Common.BASE_URL_TEST + Common.APNS_DISCONNECT, params, null);
    }


    /**
     * 手机端接收到推送后，调用清空推送数据
     *
     * @param clearType
     */
    public static Observable<JSONObject> clearSocketCachePhotoCount(String clearType) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (clearType != null) {
            params.put(Common.CLEAR_TYPE, clearType);
        }

        return get(Common.BASE_URL_TEST + Common.CLEAR_PHOTO_COUNT, params, null);
    }


    /**
     * 返回用户未接收到的推送消息
     */
    public static Observable<JSONObject> getSocketData() {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.APP_NAME, Common.APPLICATION_NAME);
        return get(Common.BASE_URL_TEST + Common.GET_SOCKET_DATA, params, null);
    }

    /***************************************
     * 推送 End
     **************************************/

    public static ArrayList<PPinfo> PPlist = new ArrayList<PPinfo>();

    /**
     * 根据PP+选择PP界面。  曾经根据日期选择，现在不需要日期。
     *
     * @param pppCode
     */
    public static Observable<JSONObject> getPPsByPPPAndDate(String pppCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (pppCode != null) {
            params.put(Common.PPPCode, pppCode);
        }
        String url = Common.BASE_URL_TEST + Common.GET_PPS_BY_PPP_AND_DATE;

        return get(url, params, null);
    }


    /**
     * 选择PP+ 绑定PP。现在的逻辑： 一张PP+卡只能绑定一张PP卡的某一天。
     *
     * @param pps
     * @param pppCode
     */
    public static Observable<JSONObject> bindPPsDateToPPP(JSONArray pps, String pppCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (pps != null) {
            params.put(Common.PPS, pps.toString());
        }
        if (pppCode != null) {
            params.put(Common.ppp1, pppCode);
        }
        String url = Common.BASE_URL_TEST + Common.BIND_PPS_DATE_TO_PPP;

        return post(url, params, null);
    }

    /**
     * 获取分享的URL
     *
     * @param photoID   id
     * @param shareType 视频还是照片
     */
    public static Observable<JSONObject> getShareUrl(String photoID, String shareType) {//final int id,
        Map<String, Object> params = new HashMap<>();
        JSONObject orgJSONObject = new JSONObject();
        try {
            orgJSONObject.put(Common.SHARE_MODE, shareType);
            orgJSONObject.put(Common.SHARE_PHOTO_ID, photoID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.SHARE_CONTENT, orgJSONObject.toString());
        params.put(Common.IS_USE_SHORT_URL, false);
        //BASE_URL_TEST2 测试成功
        PictureAirLog.out("get share url----------------" + params.toString());

        return post(Common.BASE_URL_TEST + Common.GET_SHARE_URL, params, null);
    }

    /**
     * 获取分享的URL
     *
     * @param longURL
     */
    public static Observable<JSONObject> getShortUrl(String longURL) {//, final int id
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LONG_URL, longURL);
        PictureAirLog.out("get share url----------------" + params.toString());

        return post(Common.BASE_URL_TEST + Common.GET_SHORT_URL, params, null);
    }

    /**
     * <<<<<<< HEAD
     * =======
     * 分享成功的回调，通知服务器已经成功分享
     *
     * @param shareId
     * @param platform
     */
    public static Observable<JSONObject> shareCallBack(String shareId, String platform) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (shareId != null) {
            params.put(Common.SHARE_ID, shareId);
        }
        if (platform != null) {
            params.put(Common.SHARE_PLATFORM, platform);
        }

        PictureAirLog.e("----shareCallBack:", "" + params.toString());

        return post(Common.BASE_URL_TEST + Common.SHARE_CALL_BACK, params, null);
    }

    /**
     * >>>>>>> feeec6a01b02967d5108228ed709ba5a5d821487
     * 获取照片的最新数据,并后台统计图片的下载数量
     *
     * @param tokenId
     * @param hasOriginalUrl 是否有原图
     */
    public static Observable<JSONObject> getPhotosInfo(String tokenId, final boolean hasOriginalUrl, final DownloadFileStatus fileStatus) {
        Map<String, Object> params = new HashMap<>();

        params.put(Common.USERINFO_TOKENID, tokenId);
        params.put(Common.ISDOWNLOAD, true);
        //有原图是字符串downloadPhotoIds，没有原图是传ids ，jsonArray
        if (hasOriginalUrl) {
            params.put(Common.DOWNLOAD_PHOTO_IDS, fileStatus.getPhotoId());
        } else {
            JSONArray ids = new JSONArray();
            ids.add(fileStatus.getPhotoId());
            params.put(Common.EPPP_IDS, ids.toString());

        }

        return get(Common.BASE_URL_TEST + Common.GET_PHOTOS_BY_CONDITIONS, params, null);
    }

    /**************************************下载图片 Start**************************************/
    /**
     * 下载图片的接口。
     *
     * @param fileStatus
     */
    public static Observable<InputStream> downLoadPhotosWithUrl(final DownloadFileStatus fileStatus, final Handler adapterHandler, final HttpCallback callback) {
        PictureAirLog.out("downloadurl photo--->" + fileStatus.getNewUrl());

        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<InputStream> observable = request.download(fileStatus.getNewUrl(), new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (callback != null) callback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (callback != null) callback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .flatMap(new Func1<ResponseBody, Observable<InputStream>>() {
                    @Override
                    public Observable<InputStream> call(final ResponseBody responseBody) {
                        return RxHelper.createData(responseBody.byteStream());
                    }
                });

        return observable;


//        BinaryCallBack task = HttpUtil1.asyncDownloadBinaryData(fileStatus.getNewUrl(), new HttpCallback() {
//            long startTime = System.currentTimeMillis();
//            long lastTime = startTime;
//            @Override
//            public void onSuccess(byte[] binaryData) {
//                super.onSuccess(binaryData);
//                PictureAirLog.e(TAG, "调用下载照片API成功");
//                Message msg =  handler.obtainMessage();
//                msg.what = DOWNLOAD_PHOTO_SUCCESS;
//                Bundle bundle = new Bundle();
//                fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FINISH;
//                bundle.putParcelable("url",fileStatus);
//                bundle.putByteArray("binaryData",binaryData);
//                msg.setData(bundle);
//                handler.sendMessage(msg);
//            }
//
//            @Override
//            public void onFailure(int status) {
//                super.onFailure(status);
//                PictureAirLog.e(TAG, "调用下载照片API失败：错误代码：" + status);
//                Message msg =  handler.obtainMessage();
//                msg.what = DOWNLOAD_PHOTO_FAILED;
//                Bundle bundle = new Bundle();
//                if (status != 404) {
//                    fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
//                } else {
//                    if (fileStatus.isVideo() == 0) {
//                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_UPLOADING;
//                    } else {
//                        fileStatus.status = DownloadFileStatus.DOWNLOAD_STATE_FAILURE;
//                    }
//                }
//                bundle.putParcelable("url",fileStatus);
//                bundle.putInt("status",status);
//                msg.setData(bundle);
//                handler.sendMessage(msg);
//            }
//
//            @Override
//            public void onProgress(long bytesWritten, long totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//                double currentSize = bytesWritten/1000d/1000d;
//                double total = totalSize/1000d/1000d;
//                String c = AppUtil.formatData(currentSize);
//                String t = AppUtil.formatData(total);
//                fileStatus.setCurrentSize(c);
//                fileStatus.setTotalSize(t);
//                long currentTime = System.currentTimeMillis();
//                float usedTime = (currentTime-lastTime)/1000f;
//                float keepTime = (currentTime-startTime)/1000f;
//                if (usedTime > 0.2) {
//                    lastTime = currentTime;
//                    double downSpeed = (bytesWritten / 1000d) / keepTime;
//                    String ds = AppUtil.formatData(downSpeed);
//                    fileStatus.setLoadSpeed(ds);
//                    if (adapterHandler != null) {
//                        adapterHandler.sendEmptyMessage(DownLoadingFragment.PHOTO_STATUS_UPDATE);
//                    }
//                }
//            }
//        });
//        return task;
    }
    /**************************************下载图片 End**************************************/

    /**
     * 忘记密码
     *
     * @param pwd
     * @param mobile
     */
    public static Observable<JSONObject> findPwd(String pwd, String mobile) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.NEW_PASSWORD, AppUtil.md5(pwd));
        if (mobile != null) {
            params.put(Common.USERINFO_USERNAME, mobile);
        }

        return post(Common.BASE_URL_TEST + Common.FORGET_PWD, params, null);
    }

    /**
     * 忘记密码
     *
     * @param email
     * @param language
     */
    public static Observable<JSONObject> findPwdEmail(String email, String language, String tokenId) {
        Map<String, Object> params = new HashMap<>();
        if (null != language) {
            params.put(Common.LANGUAGE, language.equals(Common.SIMPLE_CHINESE) ? "CN" : "EN");
        }
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (email != null) {
            params.put(Common.USERINFO_EMAIL, email);
        }

        return post(Common.BASE_URL_TEST + Common.FORGET_PWD_EMAIL, params, null);
    }


    /**
     * 获取unionpay的tn
     */
    public static Observable<JSONObject> getUnionPayTN(String orderCode) {
        PictureAirLog.e(TAG, MyApplication.getTokenId());
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (orderCode != null) {
            params.put(Common.ORDER_CODE, orderCode);
        }
        PictureAirLog.e(TAG, MyApplication.getTokenId());

        return get(Common.BASE_URL_TEST + Common.GET_UNIONPAY_TN, params, null);
    }


    /**
     * 根据商品查询所有可以使用的优惠卷
     * 1. tokenId
     * 2. cartItemIds:array<string>,用户选中的购物项(可选)
     */
    public static Observable<JSONObject> getCartItemCoupons(JSONArray cartItemIds) {
        Map<String, Object> params = new HashMap<>();
        if (null != cartItemIds) {//订单页面发来的请求
            params.put(Common.CART_ITEM_IDS, cartItemIds);
        }
        if (null != MyApplication.getTokenId()) {
            params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        }

        return get(Common.BASE_URL_TEST + Common.GET_COUPONS, params, null);
    }


    /**
     * 添加优惠卷
     * * 两个业务处理AB
     * A在me中进入的添加优惠卷
     * 1. tokenId
     * 2. 优惠code
     * B在订单页面进入的添加优惠卷
     * 1. tokenId
     * 2. 优惠code
     * 3. cartItemIds:array<string>,用户选中的购物项(可选)
     */
    public static Observable<JSONObject> addCoupons(String couponsCode, JSONArray cartItemIds) {
        Map<String, Object> params = new HashMap<>();
        if (null != cartItemIds) {//订单页面发来的请求
            params.put(Common.CART_ITEM_IDS, cartItemIds);
        }
        if (couponsCode != null) {
            params.put(Common.couponCode, couponsCode);
        }
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        PictureAirLog.e(TAG, MyApplication.getTokenId());

        return post(Common.BASE_URL_TEST + Common.ADD_COUPONS, params, null);
    }

    /**
     * 用户使用优惠码预览费用
     *
     * @param couponCodes  优惠码
     * @param cartItemsIds 用户选中的购物项
     */
    public static Observable<JSONObject> previewCoupon(JSONArray couponCodes, boolean needInvoice, JSONArray cartItemsIds) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (couponCodes != null) {
            params.put("couponCodes", couponCodes.toString());
        }
        if (cartItemsIds != null) {
            params.put("cartItemIds", cartItemsIds.toString());
        }
        params.put("isNeedInvoice", needInvoice);
        PictureAirLog.v(TAG, "previewCoupon params：" + params);

        return post(Common.BASE_URL_TEST + Common.PREVIEW_COUPONS, params, null);
    }

    /**
     * 从me中进入查询抵用劵
     */
    public static Observable<JSONObject> getCoupons() {
        Map<String, Object> params = new HashMap<>();

        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        PictureAirLog.e(TAG, "===========" + MyApplication.getTokenId());

        return get(Common.BASE_URL_TEST + Common.GET_ME_COUPONS, params, null);
    }

    /**
     * 根据照片的拍摄时间获取PP+卡列表
     * 用于预览图片页面，“使用已有的迪斯尼乐拍通一卡通”
     *
     * @param shootDate
     */
    public static Observable<JSONObject> getPPPsByShootDate(String shootDate) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (shootDate != null) {
            params.put(Common.SHOOTDATE, shootDate);
        }

        return get(Common.BASE_URL_TEST + Common.GET_PPPS_BY_SHOOTDATE, params, null);
    }

    /**
     * @param orderCode
     * @param dealingKey
     * @return
     */
    public static Observable<JSONObject> updateDealingOrder(String orderCode, String dealingKey) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.ORDER_CODE, orderCode);
        params.put(Common.DEALINGKEY, dealingKey);

        return post(Common.BASE_URL_TEST + Common.UPDATE_DEALING_ORDER, params, null);
    }


    /**
     * 提交订单
     *
     * @param goods        JSONArray
     * @param deliveryType 物流方式(必须，送货方式,物流(0)、自提(1)、直送(2),虚拟类商品无须快递(3))
     * @param outletId     门店编号门店主键(与addressId互斥,但不能都存在,若物流方式为3则无此条约束)
     * @param addressId    string用户地址id(与outletId互斥,但不能都存在)
     */
    public static Observable<JSONObject> addBook(JSONArray goods, JSONArray couponCodes, int deliveryType, String outletId, String addressId,
                                                 int payType, String channelId, String dealingKey) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        if (goods != null) {
            params.put(Common.GOODS, goods.toString());
        }
        if (couponCodes != null) {
            params.put("coupons", couponCodes.toString());
        }
        params.put(Common.DELIVERY_TYPE, deliveryType);

        if (deliveryType == 0) {
            //物流
            params.put(Common.ADDRESS_ID, addressId);
        } else if (deliveryType == 1) {
            //自提
            params.put("outletId", outletId);
        }

        params.put(Common.PAY_TYPE, payType);
        if (!TextUtils.isEmpty(channelId)) {
            params.put("channelId", channelId);
        }

        if (!TextUtils.isEmpty(dealingKey)) {
            params.put(Common.DEALINGKEY, dealingKey);
        }
        PictureAirLog.out("addorder params ------------>" + params.toString());

        return post(Common.BASE_URL_TEST + Common.ADD_BOOKING, params, null);
    }

    public static Observable<JSONObject> getSingleGoods(String dealingUrl, String language) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, MyApplication.getTokenId());
        params.put(Common.LANGUAGE, language);

        return get(Common.BASE_URL_TEST + dealingUrl, params, null);
    }

    /**
     * 获取纪念照
     *
     * @param tokenId
     */
    public static Observable<JSONObject> getSouvenirPhotos(final String tokenId, final String ppCode) {
        Map<String, Object> params = new HashMap<>();
        params.put(Common.USERINFO_TOKENID, tokenId);
        if (!TextUtils.isEmpty(ppCode)) {
            params.put(Common.CUSTOMERID, ppCode);
        }

        return get(Common.BASE_URL_TEST + Common.GET_PHOTOS_BY_CONDITIONS, params, null);
    }

    /**
     * 公共请求方法(无参)get
     *
     * @param requestUrl
     * @param progressCallback
     * @return
     */
    private static Observable<JSONObject> get(String requestUrl, final HttpCallback progressCallback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<JSONObject> observable = request.get(requestUrl, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (progressCallback != null) progressCallback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (progressCallback != null) progressCallback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(RxHelper.<JSONObject>handleResult());

        return observable;
    }

    /**
     * 无参数post
     *
     * @param url
     * @param progressCallback
     * @return
     */
    private static Observable<JSONObject> post(String url, final HttpCallback progressCallback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<JSONObject> observable = request.post(url, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (progressCallback != null) progressCallback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (progressCallback != null) progressCallback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(RxHelper.<JSONObject>handleResult());

        return observable;
    }

    /**
     * 公共请求方法(有参get
     *
     * @param params
     * @param requestUrl
     * @param progressCallback
     * @return
     */
    private static Observable<JSONObject> get(String requestUrl, Map<String, Object> params, final HttpCallback progressCallback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<JSONObject> observable = request.get(requestUrl, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (progressCallback != null) progressCallback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (progressCallback != null) progressCallback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(RxHelper.<JSONObject>handleResult());

        return observable;
    }

    /**
     * 有参数post
     *
     * @param url
     * @param params
     * @param progressCallback
     * @return
     */
    private static Observable<JSONObject> post(String url, Map<String, Object> params, final HttpCallback progressCallback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<JSONObject> observable = request.post(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (progressCallback != null) progressCallback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (progressCallback != null) progressCallback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(RxHelper.<JSONObject>handleResult());
        return observable;
    }

    /**
     * 公共请求方法 delete
     *
     * @param params
     * @param url
     * @param progressCallback
     * @return
     */
    private static Observable<JSONObject> delete(String url, Map<String, Object> params, final HttpCallback progressCallback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<JSONObject> observable = request.delete(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (progressCallback != null) progressCallback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (progressCallback != null) progressCallback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(RxHelper.<JSONObject>handleResult());

        return observable;
    }

    /**
     * put
     *
     * @param params
     * @param url
     * @param progressCallback
     * @return
     */
    private static Observable<JSONObject> put(String url, Map<String, Object> params, final HttpCallback progressCallback) {
        PhotoPassAuthApi request = ApiFactory.INSTANCE.getPhotoPassAuthApi();
        Observable<JSONObject> observable = request.put(url, params, new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength) {
                if (progressCallback != null) progressCallback.onProgress(bytesRead, contentLength);
            }
        })
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        if (progressCallback != null) progressCallback.doOnSubscribe();
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .compose(RxHelper.<JSONObject>handleResult());

        return observable;
    }
}
