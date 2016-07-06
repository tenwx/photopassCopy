package com.pictureair.photopass.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.BindPPInfo;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.entity.HelpInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.service.SocketUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * 数据解析
 */
public class JsonUtil {
    /**
     * 地点信息解析
     */
    public static DiscoverLocationItemInfo getLocation(JSONObject object) throws JSONException {
        DiscoverLocationItemInfo info = new DiscoverLocationItemInfo();
        info.locationId = object.getString("locationId");
        info.locationIds = object.getJSONArray("shootSpots").toString();
        info.isShow = object.getBooleanValue("visibility") ? 1 : 0;
        if (object.containsKey("location")) {
            info.placeCHName = object.getString("location");
        }
        if (object.containsKey("location_EN")) {
            info.placeENName = object.getString("location_EN");
        }
        info.placeUrl = (Common.PHOTO_URL + object.getString("defaultPhoto")).trim();
        if (object.containsKey("GPS")) {
            JSONObject obj = (JSONObject) object.get("GPS");
//			PictureAirLog.out("转换之前的坐标"+obj.toString());
//			LatLng latLng = AppUtil.converterFromGPS2BD(obj);//转换成百度坐标系
            if (obj != null) {
                if (!obj.containsKey("GPSLatitude") || !obj.containsKey("GPSLongitude")) {
                    info.latitude = 0;
                    info.longitude = 0;
                } else {
                    info.latitude = Double.valueOf(obj.getString("GPSLatitude"));
                    info.longitude = Double.valueOf(obj.getString("GPSLongitude"));
                }
            }

//			PictureAirLog.out("转换之后的坐标"+latLng.toString());
        }
        if (object.containsKey("description")) {
            info.placeDetailCHIntroduce = object.getString("description");
        }

        if (object.containsKey("description_EN")) {
            info.placeDetailENIntroduce = object.getString("description_EN");
        }
        info.popularity = "popularity";
        info.islove = 0;
        info.showDetail = 0;
        return info;
    }

    /**
     * 照片信息解析 ，并且把数据插入到数据库中作为缓存数据
     */
    public static PhotoInfo getPhoto(JSONObject object) throws JSONException {
        PhotoInfo info = new PhotoInfo();
        info.onLine = 1;
        //获取图片的ID
        if (object.containsKey("_id"))
            info.photoId = object.getString("_id");

        //获取图片的购买状态
        if (object.containsKey("isPaid") && "true".equals(object.getString("isPaid"))) {
            info.isPayed = 1;
        } else {
            info.isPayed = 0;
        }
        info.isVideo = 0;
        //获取图片的location信息
        if (object.containsKey("locationId"))
            info.locationId = object.getString("locationId");
        //获取图片的原始路径信息
        if (object.containsKey("originalInfo")) {
            JSONObject obj = (JSONObject) object.get("originalInfo");
            if (obj.containsKey("url")) {
                StringBuffer sb = new StringBuffer();
                sb.append(Common.PHOTO_URL).append(obj.getString("url"));
                info.photoPathOrURL = sb.toString().trim();
            }
        }
        //获取图片的缩略图路径
        if (object.containsKey("thumbnail")) {
            JSONObject obj = (JSONObject) object.get("thumbnail");
            if (obj.containsKey("x128")) {
                JSONObject x216 = (JSONObject) obj.get("x128");
                if (x216.containsKey("url")) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(Common.PHOTO_URL).append(x216.getString("url"));
                    info.photoThumbnail = sb.toString().trim();
                }
            }
            if (obj.containsKey("x512")) {
                JSONObject x512 = (JSONObject) obj.get("x512");
                if (x512.containsKey("url")) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(x512.getString("url"));
                    info.photoThumbnail_512 = sb.toString().trim();
                }
            }
            if (obj.containsKey("x1024")) {
                JSONObject x1024 = (JSONObject) obj.get("x1024");
                if (x1024.containsKey("url")) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(Common.PHOTO_URL).append(x1024.getString("url"));
                    info.photoThumbnail_1024 = sb.toString().trim();
                }
            }
        }
        //获取图片对应的pp码
        String ppCode = "";
        if (object.containsKey("customerIds")) {
            JSONArray customerIdsArray = object.getJSONArray("customerIds");
            JSONObject customerId;
            for (int i = 0; i < customerIdsArray.size(); i++) {
                customerId = customerIdsArray.getJSONObject(i);
                if (customerId.containsKey("code")) {
                    ppCode += customerId.getString("code") + ",";
                }
            }
            info.photoPassCode = ppCode;
        }
        //获取图片的拍摄日期
        if (object.containsKey("shootDate")) {
            String time = object.getString("shootDate");
            info.shootTime = time;
        }
        if (object.containsKey("strShootOn")) {
            info.shootOn = object.getString("strShootOn");
        }

        //是否添加过 模版
        if (object.containsKey("presetId")) {
            String presetId = object.getString("presetId");
            if (presetId.equals("000000000000000000000000")) {
                info.isHasPreset = 0;
            } else {
                info.isHasPreset = 1;
            }
        } else {
            info.isHasPreset = 0;
        }

        info.isChecked = 0;
        info.isSelected = 0;
        info.isLove = 0;
        info.isUploaded = 0;
        info.showMask = 0;
        info.lastModify = 0l;
        info.index = "";
//		info.albumName = "";
//		info.isPayed = 0;
        return info;
    }

    /**
     * 视频信息解析 ，并且把数据插入到数据库中作为缓存数据
     */
    public static PhotoInfo getVideoInfo(JSONObject object) throws JSONException {
        PhotoInfo info = new PhotoInfo();
        info.onLine = 1;
        //获取图片的ID
        if (object.containsKey("_id"))
            info.photoId = object.getString("_id");

        //获取图片的购买状态
        info.isPayed = 1;
        info.isVideo = 1;
        //获取图片的原始路径信息
        if (object.containsKey("url")) {
            info.photoPathOrURL = object.getString("url");
        }
        //获取图片对应的pp码
        info.photoPassCode = "";
        //获取视频的拍摄日期
        if (object.containsKey("createdOn")) {
            String time = object.getString("createdOn");
            info.shootOn = AppUtil.GTMToLocal(time);
            info.shootTime = info.shootOn.substring(0, 10);
//            PictureAirLog.out("get transfer time----> " + info.shootOn);
//            PictureAirLog.out("shootTime----> " + info.shootTime);
        }

        if (object.containsKey("fileSize")) {
            info.fileSize = object.getIntValue("fileSize");
        }

        if (object.containsKey("width")) {
            info.videoWidth = object.getIntValue("width");
        }

        if (object.containsKey("height")) {
            info.videoHeight = object.getIntValue("height");
        }

        info.isChecked = 0;
        info.isSelected = 0;
        info.isLove = 0;
        info.isUploaded = 0;
        info.showMask = 0;
        info.lastModify = 0l;
        info.index = "";
        info.photoThumbnail = "";
        info.photoThumbnail_512 = "";
        info.photoThumbnail_1024 = "";
        info.locationId = "";
        info.shareURL = "";
        info.locationCountry = "";
        info.locationName = "";
        return info;
    }

    /**
     * 用户信息解析
     */
    public static void getUserInfo(final Context context, JSONObject object, String account, Handler handler) throws JSONException {
        SharedPreferences sp = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString(Common.USERINFO_TOKENID, AESKeyHelper.encryptString(object.getString("tokenId"), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP)));
        PictureAirLog.out("get user info---->" + object.toString());
        JSONObject obj = object.getJSONObject("user");
        if (obj.containsKey("_id")) {

            e.putString(Common.USERINFO_ID, obj.getString("_id"));
        }
        if (obj.containsKey("name")) {

            e.putString(Common.USERINFO_NICKNAME, obj.getString("name"));
        }


        if (obj.containsKey("country")) {
            e.putString(Common.USERINFO_COUNTRY,
                    obj.getString("country"));
        }

        //如果 用户存在ppCode，存入 USERINFO_USER_PP 字段。
        if (obj.containsKey("userPP")) {
            // 存入 USERINFO_USER_PP 字段。
            e.putString(Common.USERINFO_USER_PP, obj.getString("userPP"));
        }

        if (obj.containsKey("gender")) {

            if ("1".equals(obj.getString("gender")) || "male".equals(obj.getString("gender"))) {
                e.putString(Common.USERINFO_GENDER, "male");
            } else if ("0".equals(obj.getString("gender")) || "female".equals(obj.getString("gender"))) {
                e.putString(Common.USERINFO_GENDER, "female");
            } else {
                e.putString(Common.USERINFO_GENDER, "male");
            }
        }
        if (obj.containsKey("birthday")) {

            e.putString(Common.USERINFO_BIRTHDAY, obj.getString("birthday").split("T")[0]);
        }

        e.putString(Common.USERINFO_ACCOUNT, account);

        String headUrl;
        if (obj.containsKey("avatarUrl")) {
            PictureAirLog.out("");
            headUrl = obj.getString("avatarUrl");
            e.putString(Common.USERINFO_HEADPHOTO, headUrl);
        }
        String bgUrl;
        if (obj.containsKey("coverHeaderImage")) {
            bgUrl = obj.getString("coverHeaderImage");
            e.putString(Common.USERINFO_BGPHOTO, bgUrl);
        }

        if (obj.containsKey("customerIds")) {
            e.putInt(Common.PP_COUNT, obj.getJSONArray("customerIds").size());
        }
        e.commit();
    }

    /**
     * 创建修改购物车参数的jsonobject对象
     *
     * @param photoArrayList 传入需要修改的图片参数，一个是photoId，一个是photoUrl
     * @param cartItem       每一项的item对象，里面会包含price参数
     * @return
     */
    public static JSONArray addAndModifyCartItemJsonArray(ArrayList<PhotoInfo> photoArrayList, CartItemInfo cartItem) {
        if (photoArrayList == null) {
            return null;
        }
        JSONArray photoIdArray = new JSONArray();//放入图片的json数组
        try {
            List<CartPhotosInfo> photoslist;
            if (cartItem.getEmbedPhotos() != null && cartItem.getEmbedPhotos().size() > 0) {
                photoslist = cartItem.getEmbedPhotos();//获取每一个item中的图片数组
            } else {
                photoslist = new ArrayList<>();
            }

            if (photoArrayList != null && photoArrayList.size() > 0) {//判断有没有照片，如果为null，说明不需要修改照片，这个时候应该使用原有的照片
                for (int i = 0; i < photoslist.size(); i++) {
                    JSONArray photoIds = new JSONArray();//放入图片的图片id数组
                    JSONObject photoId = new JSONObject();
                    photoId.put("photoId", photoArrayList.get(i).photoId);
                    photoIds.add(photoId);
                    photoIdArray.add(photoIds);
                }
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return photoIdArray;
    }


//    /**
//     * 创建修改购物车参数的jsonobject对象
//     *
//     * @param photoArrayList 传入需要修改的图片参数，一个是photoId，一个是photoUrl
//     * @param cartitem       每一项的item对象，里面会包含price参数
//     * @return
//     */
//    public static JSONObject CreateModifyCartItemJsonObject(ArrayList<PhotoInfo> photoArrayList, CartItemInfo1 cartitem, int count) {
//        // TODO Auto-generated method stub
//
//        JSONObject itemJsonObject = new JSONObject();
//        try {
//            JSONArray productsJsonArray = new JSONArray();//修改图片的jsonarray
//            JSONArray embedphotos = new JSONArray();//放入图片的json数组
//            List<CartPhotosInfo1> photoslist;
//            if (cartitem.getEmbedPhotos() != null && cartitem.getEmbedPhotos().size() > 0) {
//                photoslist = cartitem.getEmbedPhotos();//获取每一个item中的图片数组
//            } else {
//                photoslist = new ArrayList<>();
//            }
//
//            if (photoArrayList != null && photoArrayList.size() > 0) {//判断有没有照片，如果为null，说明不需要修改照片，这个时候应该使用原有的照片
//                for (int i = 0; i < photoslist.size(); i++) {
//                    JSONObject embedphoto = new JSONObject();
//                    JSONArray photoids = new JSONArray();//放入图片的图片id数组
//                    for (int j = 0; j < photoArrayList.size(); j++) {
//                        JSONObject photoid = new JSONObject();
//                        photoid.put("photoId", photoArrayList.get(j).photoId);
//                        photoid.put("photoUrl", photoArrayList.get(j).photoPathOrURL);
//                        photoids.add(photoid);
//                    }
//                    embedphoto.put("photosIds", photoids);
//                    embedphoto.put("svg", "svg info");
//                    embedphotos.add(embedphoto);
//                }
//            }
//            JSONObject productJsonObject = new JSONObject();
//            productJsonObject.put("storeId", cartitem.getCartId());
//            productJsonObject.put("embedPhotos", embedphotos);
//            productsJsonArray.add(productJsonObject);
//            //			}
//            itemJsonObject.put("_id", cartitem.getCartId());
//            itemJsonObject.put("qty", count);
//            itemJsonObject.put("products", productsJsonArray);
//        } catch (JSONException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//        return itemJsonObject;
//    }


    /**
     * 创建收货地址的json对象
     *
     * @param country
     * @param phone         座机
     * @param district
     * @param name
     * @param detailaddress
     * @param telephone     手机号
     * @param province
     * @param city
     * @return
     */
    public static JSONObject createAddressJsonObjectItem(String country, String phone, String district, String name,
                                                         String detailaddress, String telephone, String province, String city) {
        // TODO Auto-generated method stub
        JSONObject itemJsonObject = new JSONObject();
        try {
            itemJsonObject.put("defaultChose", true);
            itemJsonObject.put("zip", "");
            itemJsonObject.put("area", country);
            itemJsonObject.put("mobileNum", telephone);//手机
            itemJsonObject.put("county", district);
            itemJsonObject.put("consignee", name);
            itemJsonObject.put("detailedAddress", detailaddress);
            itemJsonObject.put("telephone", phone);//座机
            itemJsonObject.put("provinces", province);
            itemJsonObject.put("city", city);
        } catch (JSONException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        return itemJsonObject;
    }

    /**
     * 获取订单信息的Group信息的json解析方法
     *
     * @param orderJsonObject
     * @return
     */
    //还缺以下参数
//	public String orderIntroduce;//订单明细，支付宝下单的时候需要这个参数
    public static OrderInfo getOrderGroupInfo(JSONObject orderJsonObject) {
        OrderInfo orderInfo = new OrderInfo();
        try {

            //resume
            JSONObject resumeJsonObject = orderJsonObject.getJSONObject("resume");
            orderInfo.orderId = resumeJsonObject.getString("orderId");
            orderInfo.orderNumber = resumeJsonObject.getString("code");//订单号
            orderInfo.orderTime = resumeJsonObject.getString("time");//订单时间
            orderInfo.orderPayMentMethod = resumeJsonObject.getIntValue("payType");//支付类型
            orderInfo.orderStatus = resumeJsonObject.getIntValue("status");//订单状态
            //priceInfo
            JSONObject priceJsonObject = orderJsonObject.getJSONObject("priceInfo");
            orderInfo.deliveryShipping = priceJsonObject.getDouble("shipping");//运费
            orderInfo.productPrice = priceJsonObject.getDouble("productPrice");//商品价格
            orderInfo.orderTotalPrice = priceJsonObject.getDouble("totalPrice");//商品总价
            orderInfo.straightwayPreferentialPrice = priceJsonObject.getDouble("straightwayPreferentialPrice");//优惠折扣
            orderInfo.promotionPreferentialPrice = priceJsonObject.getDouble("promotionPreferentialPrice");//优惠立减
            orderInfo.preferentialPrice = priceJsonObject.getDouble("preferentialPrice");//优惠减免总费用
            orderInfo.resultPrice = priceJsonObject.getDouble("resultPrice");//初始总费用
            orderInfo.actualotalPrice = priceJsonObject.getDouble("totalPrice");//实际支付总价

            //deliveryInfo
            JSONObject deliveryJsonObject = orderJsonObject.getJSONObject("deliveryInfo");
            if (deliveryJsonObject.containsKey("deliveryType")) {
                //物流类型
                orderInfo.deliveryMethod = deliveryJsonObject.getIntValue("deliveryType");
            }

            //deliveryAddress
            JSONObject deliveryAddressJsonObject = deliveryJsonObject.getJSONObject("deliveryAddress");
            if (deliveryAddressJsonObject.containsKey("consignee")) {//收货人
                orderInfo.deliveryCustomer = deliveryAddressJsonObject.getString("consignee");
            } else {
                orderInfo.deliveryCustomer = "";
            }
            if (deliveryAddressJsonObject.containsKey("mobileNum")) {//手机
                orderInfo.deliveryPhoneNumber = deliveryAddressJsonObject.getString("mobileNum");
            } else {
                orderInfo.deliveryPhoneNumber = "";
            }
            if (deliveryAddressJsonObject.containsKey("telephone")) {//固电
                orderInfo.deliveryHomeNumber = deliveryAddressJsonObject.getString("telephone");
            } else {
                orderInfo.deliveryHomeNumber = "";
            }
            if (deliveryAddressJsonObject.containsKey("zip")) {//邮编
                orderInfo.deliveryPostNumber = deliveryAddressJsonObject.getString("zip");
            } else {
                orderInfo.deliveryPostNumber = "";
            }
            if (deliveryAddressJsonObject.containsKey("area")) {//国家
                orderInfo.deliveryAddress = deliveryAddressJsonObject.getString("area") + ",";
            }
            if (deliveryAddressJsonObject.containsKey("provinces")) {//省
                orderInfo.deliveryAddress += deliveryAddressJsonObject.getString("provinces") + ",";
            }
            if (deliveryAddressJsonObject.containsKey("city")) {//市
                orderInfo.deliveryAddress += deliveryAddressJsonObject.getString("city") + ",";
            }
            if (deliveryAddressJsonObject.containsKey("county")) {//区
                orderInfo.deliveryAddress += deliveryAddressJsonObject.getString("county") + ",";
            }
            if (deliveryAddressJsonObject.containsKey("detailedAddress")) {//详细地址
                orderInfo.deliveryAddress = deliveryAddressJsonObject.getString("detailedAddress");
            }
            if (orderInfo.deliveryAddress == null) {
                orderInfo.deliveryAddress = "";
            }


            //logisticsInfo
            JSONObject logisticJsonObject = deliveryJsonObject.getJSONObject("logisticsInfo");
            if (logisticJsonObject.containsKey("company")) {//快递公司
                orderInfo.deliveryCompany = logisticJsonObject.getString("company");
            } else {
                orderInfo.deliveryCompany = "";
            }
            if (logisticJsonObject.containsKey("code")) {//快递单号
                orderInfo.deliveryNumber = logisticJsonObject.getString("code");
            } else {
                orderInfo.deliveryNumber = "";
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return orderInfo;
    }

    /**
     * 获取订单信息中的child部分的json解析方法
     *
     * @param orderJsonObject
     * @return
     */
    public static ArrayList<CartItemInfo> getOrderChildInfo(JSONObject orderJsonObject) {
        ArrayList<CartItemInfo> orderDetailsArrayList = new ArrayList<>();
        CartItemInfo cartItemInfo;
        try {
            JSONArray productsArray = orderJsonObject.getJSONArray("productInfo");
            //获取订单下的所有商品
            CartPhotosInfo cartPhotosInfo = null;
            JSONArray usePhotosArray;
            for (int i = 0; i < productsArray.size(); i++) {
                cartItemInfo = new CartItemInfo();
                JSONObject productJsonObject = productsArray.getJSONObject(i);

                cartItemInfo.setProductName(productJsonObject.getString("productNameAilas"));//商品名字
                cartItemInfo.setCartProductImageUrl(productJsonObject.getString("productImage"));//商品预览图URL
                cartItemInfo.setQty(productJsonObject.getIntValue("qty"));//商品数量
                cartItemInfo.setUnitPrice(productJsonObject.getIntValue("unitPrice"));//商品单价
                cartItemInfo.setCartProductType(productJsonObject.getIntValue("productEntityType"));//商品虚拟／实体类型（0,1）


                //获取添加照片的信息
                usePhotosArray = productJsonObject.getJSONArray("usePhotos");//商品名字
                if (usePhotosArray.size() == 0) {//如果为0，不赋值

                } else {
                    ArrayList<CartPhotosInfo> photourlsArrayList = new ArrayList<>();
                    for (int j = 0; j < usePhotosArray.size(); j++) {
                        JSONObject usePhotoObject = usePhotosArray.getJSONObject(j);
                        cartPhotosInfo = new CartPhotosInfo();
                        cartPhotosInfo.setPhotoUrl(usePhotoObject.getString("photoUrl"));//商品添加图片的URL
                        photourlsArrayList.add(cartPhotosInfo);
                    }
                    cartItemInfo.setEmbedPhotos(photourlsArrayList);
                }
                orderDetailsArrayList.add(cartItemInfo);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return orderDetailsArrayList;
    }

    /**
     * 获取边框信息
     *
     * @param frameJsonObject
     * @return
     */
    public static FrameOrStikerInfo getFrameInfo(JSONObject frameJsonObject) {
        FrameOrStikerInfo frameInfo = new FrameOrStikerInfo();
        try {
            frameInfo.onLine = 1;
            frameInfo.isDownload = 0;
//			frameInfo.needShow = 0;
            if (frameJsonObject.containsKey("assetName")) {
                frameInfo.frameName = frameJsonObject.getString("assetName");
            }
            if (frameJsonObject.containsKey("imgUrl_H")) {
                frameInfo.frameOriginalPathLandscape = frameJsonObject.getString("imgUrl_H");
            }
            if (frameJsonObject.containsKey("imgUrl_V")) {
                frameInfo.frameOriginalPathPortrait = frameJsonObject.getString("imgUrl_V");
            }
            if (frameJsonObject.containsKey("locationId")) {//特定场馆
                frameInfo.locationId = frameJsonObject.getString("locationId");
            } else {
                frameInfo.locationId = "common";//通用边框
            }
            if (frameJsonObject.containsKey("active_H")) {
                frameInfo.isActive = frameJsonObject.getBoolean("active_H") ? 1 : 0;
            }
            if (frameJsonObject.containsKey("thumbnail_H")) {
                JSONObject thumbnailJsonObject = frameJsonObject.getJSONObject("thumbnail_H");
                if (thumbnailJsonObject.containsKey("x400")) {
                    JSONObject x400JsonObject = thumbnailJsonObject.getJSONObject("x400");
                    if (x400JsonObject.containsKey("url")) {
                        frameInfo.frameThumbnailPathLandscape400 = x400JsonObject.getString("url");
                    }
                }
                if (thumbnailJsonObject.containsKey("x160")) {
                    JSONObject x160JsonObject = thumbnailJsonObject
                            .getJSONObject("x160");
                    if (x160JsonObject.containsKey("url")) {
                        frameInfo.frameThumbnailPathH160 = x160JsonObject
                                .getString("url");
                    }
                }

            }
            if (frameJsonObject.containsKey("thumbnail_V")) {
                JSONObject thumbNailPortraritJsonObject = frameJsonObject.getJSONObject("thumbnail_V");
                if (thumbNailPortraritJsonObject.containsKey("x300")) {
                    JSONObject x400JsonObject = thumbNailPortraritJsonObject.getJSONObject("x300");
                    if (x400JsonObject.containsKey("url")) {
                        frameInfo.frameThumbnailPathPortrait400 = x400JsonObject.getString("url");
                    }
                }
                if (thumbNailPortraritJsonObject.containsKey("x120")) {
                    JSONObject x160JsonObject = thumbNailPortraritJsonObject.getJSONObject("x120");
                    if (x160JsonObject.containsKey("url")) {
                        frameInfo.frameThumbnailPathV160 = x160JsonObject.getString("url");
                    }
                }
            }
            if (frameJsonObject.containsKey("fileSize_V")) {
                frameInfo.fileSize = frameJsonObject.getIntValue("fileSize_V");
            } else {
                frameInfo.fileSize = 0;
            }
            if (frameJsonObject.containsKey("fileSize_H")) {
                frameInfo.fileSize += frameJsonObject.getIntValue("fileSize_H");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return frameInfo;
    }

    /**
     * 获取饰品信息
     *
     * @param stickerJsonObject
     * @return
     */
    public static FrameOrStikerInfo getStickerInfo(JSONObject stickerJsonObject) {
        FrameOrStikerInfo frameInfo = new FrameOrStikerInfo();
        try {
            frameInfo.onLine = 1;
            frameInfo.isDownload = 0;
            if (stickerJsonObject.containsKey("assetName")) {
                frameInfo.frameName = stickerJsonObject.getString("assetName");
            }
            if (stickerJsonObject.containsKey("imgUrl")) {
                frameInfo.frameOriginalPathPortrait = stickerJsonObject.getString("imgUrl");
            }
            if (stickerJsonObject.containsKey("locationId")) {//特定场馆
                frameInfo.locationId = stickerJsonObject.getString("locationId");
            } else {
                frameInfo.locationId = "common";//通用边框
            }
            if (stickerJsonObject.containsKey("active")) {
                frameInfo.isActive = stickerJsonObject.getBoolean("active") ? 1 : 0;
            }
            if (stickerJsonObject.containsKey("thumbnail")) {
                JSONObject thumbnailJsonObject = stickerJsonObject.getJSONObject("thumbnail");
                if (thumbnailJsonObject.containsKey("x160")) {
                    JSONObject x160JsonObject = thumbnailJsonObject.getJSONObject("x160");
                    if (x160JsonObject.containsKey("url")) {
                        frameInfo.frameThumbnailPathH160 = x160JsonObject.getString("url");//测试代码，需要修改。
                    }
                }
            }
            if (stickerJsonObject.containsKey("fileSize")) {
                frameInfo.fileSize = stickerJsonObject.getIntValue("fileSize");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return frameInfo;
    }


    public static ArrayList<PPPinfo> getPPPSByUserId(JSONObject jsonObject) {
        ArrayList<PPPinfo> ppPinfoArrayList = new ArrayList<>();
        if (jsonObject.containsKey("PPPList")) {
            try {
                JSONArray ppplists = jsonObject.getJSONArray("PPPList");
                for (int i = 0; i < ppplists.size(); i++) {
                    JSONObject ppplist = ppplists.getJSONObject(i);
                    PPPinfo ppPinfo = new PPPinfo();
                    ppPinfo.PPPCode = ppplist.getString("PPPCode");
                    ppPinfo.capacity = ppplist.getIntValue("capacity");
                    ppPinfo.days = ppplist.getIntValue("days");
                    ppPinfo.PPP_ID = ppplist.getString("_id");
                    ppPinfo.ownOn = AppUtil.GTMToLocal(ppplist.getString("ownOn")).substring(0, 10).toString();
                    if (ppplist.containsKey("PPPType")) {
                        if (ppplist.getString("PPPType").equals("5")) {
                            ppPinfo.expericePPP = 1;
                        } else {
                            ppPinfo.expericePPP = 0;
                        }
                    }

                    if (ppplist.containsKey("cardBg")) {
                        ppPinfo.pppCardBg = ppplist.getString("cardBg");
                    }

                    if (ppplist.containsKey("isExpired")) {
                        ppPinfo.expired = ppplist.getBooleanValue("isExpired") ? 1 : 0;
                    }
                    if (ppplist.containsKey("bindInfo")){

                    String str = ppplist.getString("bindInfo");
                    JSONArray bindInfos = JSON.parseArray(str);
                    for (int j = 0; j < bindInfos.size(); j++) {
                        BindPPInfo bindInfo = new BindPPInfo();
                        JSONObject bindInfoObj = (JSONObject) bindInfos.get(j);
                        bindInfo.customerId = bindInfoObj.getString("customerId");
                        bindInfo.userids = bindInfoObj.getJSONArray("userIds").toString();  //分割成数组，暂时没有数据
                        //暂时 为空。 没有字段
                        bindInfo.bindDate = bindInfoObj.getString("bindDate");
                        ppPinfo.bindInfo.add(bindInfo);
                    }

                    }
                    if (ppplist.containsKey("expiredOn")) { //如果存在有效日期，就取值，如果不存在，就为空
                        ppPinfo.expiredOn = ppplist.getString("expiredOn");
                    } else {
                        ppPinfo.expiredOn = "";
                    }
                    ppPinfoArrayList.add(ppPinfo);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return ppPinfoArrayList;

    }


    /**
     * 选择
     * @param jsonObject
     * @return
     */
    public static ArrayList<PPPinfo> getPPPSByUserIdNHavedPPP(JSONObject jsonObject) {
        ArrayList<PPPinfo> ppPinfoArrayList = new ArrayList<>();
        if (jsonObject.containsKey("PPPList")) {
            try {
                JSONArray ppplists = jsonObject.getJSONArray("PPPList");
                for (int i = 0; i < ppplists.size(); i++) {
                    JSONObject ppplist = ppplists.getJSONObject(i);
                    PPPinfo ppPinfo = new PPPinfo();
//                    ppPinfo.PPPCode = ppplist.getString("PPPCode");
//                    ppPinfo.capacity = ppplist.getIntValue("capacity");
//                    ppPinfo.days = ppplist.getIntValue("days");
//                    ppPinfo.PPP_ID = ppplist.getString("_id");
                    ppPinfo.ownOn = AppUtil.GTMToLocal(ppplist.getString("ownOn")).substring(0, 10).toString();
                    ppPinfo.PPPCode = ppplist.getString("PPPCode");
                    ppPinfo.pppCardBg = ppplist.getString("cardBg");
                    ppPinfo.expiredOn = ppplist.getString("expiredOn");
                    ppPinfo.expired = ppplist.getIntValue("expired");
                    ppPinfo.capacity = ppplist.getIntValue("capacity");
                    String str = ppplist.getString("PPList");
                    JSONArray PPList = JSON.parseArray(str);  // 解析可能会出问题
                    for (int j = 0; j < PPList.size(); j++){
                        BindPPInfo bindPPInfo = new BindPPInfo();
                        bindPPInfo.customerId = PPList.getString(j);
                        ppPinfo.bindInfo.add(bindPPInfo);
                    }
//                    if (ppplist.containsKey("PPPType")) {
//                        if (ppplist.getString("PPPType").equals("5")) {
//                            ppPinfo.expericePPP = 1;
//                        } else {
//                            ppPinfo.expericePPP = 0;
//                        }
//                    }
//
//                    if (ppplist.containsKey("cardBg")) {
//                        ppPinfo.pppCardBg = ppplist.getString("cardBg");
//                    }
//
//                    if (ppplist.containsKey("isExpired")) {
//                        ppPinfo.expired = ppplist.getBooleanValue("isExpired") ? 1 : 0;
//                    }
//                    if (ppplist.containsKey("bindInfo")){
//
//                        String str = ppplist.getString("bindInfo");
//                        JSONArray bindInfos = JSON.parseArray(str);
//                        for (int j = 0; j < bindInfos.size(); j++) {
//                            BindPPInfo bindInfo = new BindPPInfo();
//                            JSONObject bindInfoObj = (JSONObject) bindInfos.get(j);
//                            bindInfo.customerId = bindInfoObj.getString("customerId");
//                            bindInfo.userids = bindInfoObj.getJSONArray("userIds").toString();  //分割成数组，暂时没有数据
//                            //暂时 为空。 没有字段
//                            bindInfo.bindDate = bindInfoObj.getString("bindDate");
//                            ppPinfo.bindInfo.add(bindInfo);
//                        }
//
//                    }
//                    if (ppplist.containsKey("expiredOn")) { //如果存在有效日期，就取值，如果不存在，就为空
//                        ppPinfo.expiredOn = ppplist.getString("expiredOn");
//                    } else {
//                        ppPinfo.expiredOn = "";
//                    }
                    ppPinfoArrayList.add(ppPinfo);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return ppPinfoArrayList;

    }

    public static ArrayList<HelpInfo> getHelpInfoList(JSONObject jsonObject) {
        ArrayList<HelpInfo> helpInfos = new ArrayList<HelpInfo>();
        JSONArray array = jsonObject.getJSONArray("helpList");
        HelpInfo info = null;
        JSONObject obj = null;
        for (int i = 0; i < array.size(); i++) {
            info = new HelpInfo();
            obj = (JSONObject) array.get(i);
            info.setHelpId(obj.getString("_id"));
            info.setHelpQuestionEN(obj.getString("ENQuestion"));
            info.setHelpAnswerEN(obj.getString("ENAnswer"));
            info.setHelpQuestionCN(obj.getString("CNQuestion"));
            info.setHelpAnswerCN(obj.getString("CNAnswer"));
            helpInfos.add(info);
            obj = null;
            info = null;
        }
        return helpInfos;
    }


    /**
     * 解析选择PP数据。
     *
     * @param jsonObject
     * @return
     */
    public static ArrayList<PPinfo> getPPSByPPP(JSONObject jsonObject) {
        ArrayList<PPinfo> ppInfoArrayList = new ArrayList<>();
        if (jsonObject.containsKey("PPList")) {
            JSONArray pplists = jsonObject.getJSONArray("PPList");
            for (int i = 0; i < pplists.size(); i++) {
                JSONObject pplist = pplists.getJSONObject(i);
                PPinfo pPinfo = new PPinfo();
                pPinfo.setPpCode(pplist.getString("customerId"));
                pPinfo.setPhotoCount(pplist.getIntValue("photoCount"));
                pPinfo.setShootDate(pplist.getString("shootDate"));
                ppInfoArrayList.add(pPinfo);
            }
        }
        return ppInfoArrayList;
    }


    /**
     * 齐超的接口
     * 解析优惠卷的json
     */
    public static List<CouponInfo> getCouponListFromJson(JSONObject jsonObject) {
        PictureAirLog.v("getJsonToObj", "解析优惠卷的json" + jsonObject);

        int amount = jsonObject.getIntValue("amount");
        if (amount == 0) {
            return null;
        }
        List<CouponInfo> list = new ArrayList<>();
        JSONArray array = jsonObject.getJSONArray("data");
        for (int i = 0; i < array.size(); i++) {
            list.add(getCouponInfo(array.getJSONObject(i)));
        }
        return list;
    }

    /**
     * lisa接口的解析json
     * 解析成功即可
     */
    public static List<CouponInfo> getCouponListFromJson2(JSONObject jsonObject) {
        PictureAirLog.v("getJsonToObj2", "解析优惠卷的json" + jsonObject);

        JSONArray array = jsonObject.getJSONArray("couponList");
        if (null == array || array.size() == 0) {
            return null;
        }
        List<CouponInfo> list = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            list.add(getCouponInfo(array.getJSONObject(i)));
        }
        return list;
    }

    /**
     * 获取coupon对象
     *
     * @param jsonObject
     * @return
     */
    public static CouponInfo getCouponInfo(JSONObject jsonObject) throws JSONException {
        String effectiveTime;
        String failureTime;
        String cnDesc = "";//中文描述
        String enDesc = "";//英文描述
        String cnName = "";//中文name
        String enName = "";//英文name
        String status = "";//是否过期，或者是否已使用
        boolean isCn = MyApplication.getInstance().getLanguageType().equals("zh");
        CouponInfo couponInfo = new CouponInfo();

        status = jsonObject.getBoolean("isExpired") ? "failure" : "active";//已过期？
        status = jsonObject.getBoolean("isUsed") ? "used" : status;//已使用？
        couponInfo.setCpStatus(status);//优惠卷状态

        couponInfo.setCpCode(jsonObject.getString("PPPCode"));
        couponInfo.setCpNumber(0);//LISA接口暂时没有这个
        couponInfo.setCpType("full");//优惠卷类型（discount,full,subtract）折扣，满，减

        enDesc = jsonObject.getJSONObject("codeDesc").getString("EN");
        cnDesc = jsonObject.getJSONObject("codeDesc").getString("CN");
        couponInfo.setCpDescribe(isCn ? cnDesc : enDesc);//描述

        enName = jsonObject.getJSONObject("codeName").getString("EN");
        cnName = jsonObject.getJSONObject("codeName").getString("CN");
        couponInfo.setCpName(isCn ? cnName : enName);//优惠卷名称

        //有效期
//        effectiveTime = jsonObject.getString("effectiveOn");//有效开始时间
//        effectiveTime = effectiveTime.split("T")[0];

        failureTime = AppUtil.GTMToLocal(jsonObject.getString("expiredOn"));//有效结束时间
        failureTime = failureTime.substring(0, 10).replace("-", "/");

        couponInfo.setCpValidityPeriod(failureTime);//有效期时间间隔


        return couponInfo;
    }

    /**
     * 处理手动拉取推送消息
     *
     * @param context       上下文
     * @param jsonObjectStr 返回的字符串
     * @param isMainPage    是否是主页面拉取信息
     * @param orderId       当前提交的订单
     */
    public static boolean dealGetSocketData(Context context, String jsonObjectStr, boolean isMainPage, String orderId, SharedPreferences sharedPreferences) {
        PictureAirLog.v("dealGetSocketData: ", "jsonObjectStr: " + jsonObjectStr);
        boolean isdonePayOrder = false;
        SocketUtil socketUtil = new SocketUtil(context, null, sharedPreferences);
        try {
            org.json.JSONObject jsonObject = new org.json.JSONObject(jsonObjectStr);
            //支付完成的推送donePayOrders
            org.json.JSONArray donePayOrdersArray = jsonObject.optJSONArray("donePayOrders");
            if (donePayOrdersArray != null && !isMainPage) {
                for (int i = 0; i < donePayOrdersArray.length(); i++) {
                    org.json.JSONObject donePayOrdersObject = donePayOrdersArray.getJSONObject(i);
                    if (donePayOrdersObject.optString("orderId").equals(orderId) && donePayOrdersObject.optBoolean("payDone", false)) {
                        //存在当前提交的orderId 并且支付状态为已支付则表示改orderId支付成功
                        socketUtil.socketOn("doneOrderPay", donePayOrdersObject, false);
                        isdonePayOrder = true;
                        break;
                    }
                }
            }

            //购买照片、pp升级的推送
            org.json.JSONArray upgradedPhotosArray = jsonObject.optJSONArray("upgradedPhotos");
            if (upgradedPhotosArray != null) {
                for (int i = 0; i < upgradedPhotosArray.length(); i++) {
                    org.json.JSONObject upgradedPhotosObject = upgradedPhotosArray.getJSONObject(i);
                    socketUtil.socketOn("upgradedPhotos", upgradedPhotosObject, false);
                }
            }

            //删除图片，或者删除pp对应的推送
            org.json.JSONArray deletePhotosArray = jsonObject.optJSONArray("delPhotos");
            if (deletePhotosArray != null) {
                for (int i = 0; i < deletePhotosArray.length(); i++) {
                    org.json.JSONObject deletePhotosObject = deletePhotosArray.getJSONObject(i);
                    socketUtil.socketOn("delPhotos", deletePhotosObject, false);
                }
            }


        } catch (org.json.JSONException e) {
            PictureAirLog.v("dealGetSocketData: ", "JSONException e: " + e.toString());
            e.printStackTrace();
        }
        return isdonePayOrder;
    }

}
