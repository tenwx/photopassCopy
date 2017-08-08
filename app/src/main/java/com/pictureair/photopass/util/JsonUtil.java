package com.pictureair.photopass.util;


import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.pictureair.jni.ciphermanager.PWJniUtil;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.ADLocationInfo;
import com.pictureair.photopass.entity.BindPPInfo;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.entity.DailyPPCardInfo;
import com.pictureair.photopass.entity.DealingInfo;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.entity.GoodsInfo;
import com.pictureair.photopass.entity.JsonInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.SendAddress;
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
        info.setIsOnLine(1);
        //获取图片的ID
        if (object.containsKey("_id"))
            info.setPhotoId(object.getString("_id"));

        //获取图片的购买状态
        if (object.containsKey("isPaid") && "true".equals(object.getString("isPaid"))) {
            info.setIsPaid(1);
        } else {
            info.setIsPaid(0);
        }

        //是否是视频
        if (object.containsKey("mimeType") && object.getString("mimeType").toLowerCase().contains("mp4")) {
            info.setIsVideo(1);
        } else {
            info.setIsVideo(0);
        }

        //获取图片的location信息
        if (object.containsKey("locationId"))
            info.setLocationId(object.getString("locationId"));
        //获取图片的原始路径信息
        if (object.containsKey("originalInfo")) {
            JSONObject obj = (JSONObject) object.get("originalInfo");
            if (obj.containsKey("url")) {
                StringBuilder sb = new StringBuilder();
                sb.append(Common.PHOTO_URL).append(obj.getString("url"));
                info.setPhotoOriginalURL(sb.toString().trim());
            } else {
                info.setPhotoOriginalURL("");
            }
        } else {
            info.setPhotoOriginalURL("");
        }
        //获取图片的缩略图路径
        if (object.containsKey("thumbnail")) {
            JSONObject obj = (JSONObject) object.get("thumbnail");
            if (obj.containsKey("x128")) {
                JSONObject x216 = (JSONObject) obj.get("x128");
                if (x216.containsKey("url")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Common.PHOTO_URL).append(x216.getString("url"));
                    info.setPhotoThumbnail_128(sb.toString().trim());
                }
            }
            if (obj.containsKey("x512")) {
                JSONObject x512 = (JSONObject) obj.get("x512");
                if (x512.containsKey("url")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(x512.getString("url"));
                    info.setPhotoThumbnail_512(sb.toString().trim());
                }
            }
            if (obj.containsKey("x1024")) {
                JSONObject x1024 = (JSONObject) obj.get("x1024");
                if (x1024.containsKey("url")) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(Common.PHOTO_URL).append(x1024.getString("url"));
                    info.setPhotoThumbnail_1024(sb.toString().trim());
                }

                if (info.getIsVideo() == 1 && info.getIsPaid() == 1) {
                    if (x1024.containsKey("width")) {
                        info.setVideoWidth(x1024.getIntValue("width"));
                    }

                    if (x1024.containsKey("height")) {
                        info.setVideoHeight(x1024.getIntValue("height"));
                    }
                } else if (info.getIsVideo() == 1 && info.getIsPaid() == 0) {
                    if (object.containsKey("adInfo")) {
                        JSONObject adObj = object.getJSONObject("adInfo");
                        if (adObj.containsKey("url")) {
                            StringBuilder sb = new StringBuilder();
                            sb.append(Common.PHOTO_URL).append(adObj.getString("url"));
                            info.setAdURL(sb.toString().trim());
                        }

                        if (adObj.containsKey("width")) {
                            info.setVideoWidth(adObj.getIntValue("width"));
                        }

                        if (adObj.containsKey("height")) {
                            info.setVideoHeight(adObj.getIntValue("height"));
                        }
                    }
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
            info.setPhotoPassCode(ppCode);
        }
        //获取图片的拍摄日期
        if (object.containsKey("shootDate")) {
            String time = object.getString("shootDate");
            info.setShootDate(time);
        }
        if (object.containsKey("strShootOn")) {
            info.setStrShootOn(object.getString("strShootOn"));
        }

        //是否添加过 模版
        if (object.containsKey("presetId")) {
            String presetId = object.getString("presetId");
            if (presetId.equals("000000000000000000000000")) {
                info.setIsPreset(0);
            } else {
                info.setIsPreset(1);
            }
        } else {
            info.setIsPreset(0);
        }

        //是否加密
        if (object.containsKey("enImage")) {
//            PictureAirLog.out("has enimage info----->" + object.getBooleanValue("enImage"));
            info.setIsEnImage((object.getBooleanValue("enImage")) ? 1 : 0);

        } else {
            info.setIsEnImage(0);

        }

        if (object.containsKey("receivedOn")) {
            info.setReceivedOn(object.getString("receivedOn"));
        }

        if (object.containsKey("expireDate")) {
            info.setExipreDate(object.getString("expireDate"));
        }

        return info;
    }

    /**
     * 获取一卡一天的数据列表
     * @param jsonObject
     * @return
     */
    public static ArrayList<DailyPPCardInfo> getDailyPPCardInfoList(JSONObject jsonObject, ArrayList<DiscoverLocationItemInfo> locationList, String language) {
        ArrayList<DailyPPCardInfo> list = new ArrayList<>();
        if (jsonObject.containsKey("locationP")) {
            JSONArray locations = jsonObject.getJSONArray("locationP");
            for (int i = 0; i < locations.size(); i++) {
                list.add(parseLocationPhotoJson(locations.getJSONObject(i), i, locationList, language));
            }
        }
        return list;
    }

    /**
     * 获取一卡一天的数据列表
     * @return
     */
    public static ArrayList<DailyPPCardInfo> getDailyPPCardInfoList(ArrayList<JsonInfo> jsonInfos, ArrayList<DiscoverLocationItemInfo> locationList, String language) {
        ArrayList<DailyPPCardInfo> list = new ArrayList<>();
        for (int i = 0; i < jsonInfos.size(); i++) {
            list.add(parseLocationPhotoJson(JSONObject.parseObject(jsonInfos.get(i).getJsonString()), i, locationList, language));
        }
        return list;
    }

    /**
     * 解析首页数据
     * @param location
     * @param index
     * @param locationList
     * @param language
     * @return
     */
    private static DailyPPCardInfo parseLocationPhotoJson(JSONObject location, int index, ArrayList<DiscoverLocationItemInfo> locationList, String language) {
        DailyPPCardInfo dailyPPCardInfo = new DailyPPCardInfo();
        String ppCode = null;
        String shootDate = null;
        int isActivated = 0;
        int photoCount = 0;
        String shootOn = null;
        if (location.containsKey("PPCode")) {
            ppCode = location.getString("PPCode");
            dailyPPCardInfo.setPpCode(ppCode);
        }

        if (location.containsKey("shootOnDate")) {
            shootDate = location.getString("shootOnDate");
            dailyPPCardInfo.setShootDate(shootDate);
        }

        if (location.containsKey("ifActive")) {
            isActivated = location.getIntValue("ifActive");
            dailyPPCardInfo.setActivated(isActivated);
        }

        if (location.containsKey("shootOn")) {
            shootOn = location.getString("shootOn");
            dailyPPCardInfo.setShootOn(shootOn);
        }

        if (location.containsKey("pCount")) {
            photoCount = location.getIntValue("pCount");
            dailyPPCardInfo.setPhotoCount(photoCount);
        }

        if (location.containsKey("cardBg")) {
            dailyPPCardInfo.setLogoUrl(location.getString("cardBg"));
        }

        if (location.containsKey("bgColor")) {
            String color = location.getString("bgColor");
            String[] colors = color.split(",");
            if (colors.length == 3) {
                dailyPPCardInfo.setColorR(Integer.valueOf(colors[0]));
                dailyPPCardInfo.setColorG(Integer.valueOf(colors[1]));
                dailyPPCardInfo.setColorB(Integer.valueOf(colors[2]));
            } else {
                dailyPPCardInfo.setColorR(255);
                dailyPPCardInfo.setColorG(255);
                dailyPPCardInfo.setColorB(255);
            }
        } else {
            dailyPPCardInfo.setColorR(255);
            dailyPPCardInfo.setColorG(255);
            dailyPPCardInfo.setColorB(255);
        }

        if (location.containsKey("expiredDays")) {
            dailyPPCardInfo.setExpiredDays(location.getIntValue("expiredDays"));
        }

        dailyPPCardInfo.setSectionId(index);

        if (location.containsKey("loc")) {
            JSONArray photos = location.getJSONArray("loc");
            PhotoInfo photoInfo;
            for (int j = 0; j < photos.size(); j++) {
                if (j >= 2) {//只取第一和第二个点
                    break;
                }

                JSONObject photo = photos.getJSONObject(j);
                if (photo.containsKey("photoInfos")) {
                    photoInfo = getLocationPhoto(photo.getJSONObject("photoInfos"));
                    if (photo.containsKey("locationId")) {
                        photoInfo.setLocationId(photo.getString("locationId"));

                        //设置地点名称
                        int resultPosition = AppUtil.findPositionInLocationList(photoInfo, locationList);
                        if (resultPosition == -1) {//如果没有找到，说明是其他地点的照片
                            resultPosition = locationList.size() - 1;
                            photoInfo.setLocationId("others");
                        }
                        if (resultPosition < 0 ) {
                            resultPosition = 0;
                        }
                        if (language.equals(Common.SIMPLE_CHINESE)) {
                            photoInfo.setLocationName(locationList.get(resultPosition).placeCHName);
                        } else {
                            photoInfo.setLocationName(locationList.get(resultPosition).placeENName);
                        }
                    }

                    if (j == 0) {//第一个，为header数据
                        dailyPPCardInfo.setLeftPhoto(photoInfo);

                    } else {
                        dailyPPCardInfo.setRightPhoto(photoInfo);

                    }
                }
            }
        }
        return dailyPPCardInfo;
    }

    /**
     * 获取一卡一天内的photoInfo
     * @param photoInfoJO
     * @return
     */
    private static PhotoInfo getLocationPhoto(JSONObject photoInfoJO) {
        PhotoInfo photoInfo = new PhotoInfo();
        if (photoInfoJO.containsKey("_id")) {
            photoInfo.setPhotoId(photoInfoJO.getString("_id"));
        }
        if (photoInfoJO.containsKey("presetId")) {
            photoInfo.setIsPreset(photoInfoJO.getString("presetId").equals("000000000000000000000000") ? 0 : 1);
        }
        if (photoInfoJO.containsKey("strShootOn")) {
            photoInfo.setStrShootOn(photoInfoJO.getString("strShootOn"));
        }
        if (photoInfoJO.containsKey("x128")) {
            photoInfo.setPhotoThumbnail_128(Common.PHOTO_URL + photoInfoJO.getString("x128"));
        }
        if (photoInfoJO.containsKey("x512")) {
            photoInfo.setPhotoThumbnail_512(photoInfoJO.getString("x512"));
        }
        if (photoInfoJO.containsKey("x1024")) {
            photoInfo.setPhotoThumbnail_1024(photoInfoJO.getString("x1024"));
        }
        photoInfo.setIsVideo(0);//固定设为0
        if (photoInfoJO.containsKey("isPaid") && "true".equals(photoInfoJO.getString("isPaid"))) {
            photoInfo.setIsPaid(1);
        } else {
            photoInfo.setIsPaid(0);
        }
        if (photoInfoJO.containsKey("enImage")) {
            photoInfo.setIsEnImage((photoInfoJO.getBooleanValue("enImage")) ? 1 : 0);
        } else {
            photoInfo.setIsEnImage(0);
        }
        return photoInfo;
    }

    /**
     * 用户信息解析
     */
    public static void getUserInfo(final Context context, JSONObject object, String account, String password) throws JSONException {
        //此处不建议使用SPUtil类
        SharedPreferences sp = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString(Common.USERINFO_TOKENID, AESKeyHelper.encryptString(object.getString("tokenId"), PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)));
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

        String realAccount = AppUtil.getCorrectAccount(account);
        e.putString(Common.USERINFO_ACCOUNT, realAccount);
        e.putBoolean(Common.USERINFO_ISLOGIN, true);

        //在app sharedPreference中加入用户名，用于登录的时候获取
        SharedPreferences asp = context.getSharedPreferences(Common.SHARED_PREFERENCE_APP, Context.MODE_PRIVATE);
        SharedPreferences.Editor ae = asp.edit();
        ae.putString(Common.USERINFO_ACCOUNT, account);
        ae.putString(Common.USERINFO_PASSWORD,  AESKeyHelper.encryptString(password, PWJniUtil.getAESKey(Common.APP_TYPE_SHDRPP, 0)));
        ae.apply();

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
        e.apply();
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
                    photoId.put("photoId", photoArrayList.get(i).getPhotoId());
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

            //invoiceInfo
            if(orderJsonObject.containsKey("invoiceInfo") && orderInfo.invoiceInfo!=null){
               JSONObject jsonObject = orderJsonObject.getJSONObject("invoiceInfo");
                if(jsonObject ==null)
                    return orderInfo;
                if(jsonObject.containsKey("invoiceType")){
                    orderInfo.invoiceInfo.setType(jsonObject.getInteger("invoiceType"));
                }
                if(jsonObject.containsKey("invoiceTitle")){
                    orderInfo.invoiceInfo.setTitle(jsonObject.getInteger("invoiceTitle"));
                }
                if(orderInfo.invoiceInfo.getTitle() == 1 && jsonObject.containsKey("invoiceCompanyName")){
                    orderInfo.invoiceInfo.setCompanyName(jsonObject.getString("invoiceCompanyName"));
                }
                if(jsonObject.containsKey("invoiceContent")){
                    orderInfo.invoiceInfo.setContent(jsonObject.getInteger("invoiceContent"));
                }
                if(jsonObject.containsKey("invoiceAddress")){
                    JSONObject object = jsonObject.getJSONObject("invoiceAddress");
                    if(object.containsKey("area")){
                        orderInfo.invoiceInfo.getAddress().setArea(object.getString("area"));
                    }
                    if(object.containsKey("provinces")){
                        orderInfo.invoiceInfo.getAddress().setProvince(object.getString("provinces"));
                    }
                    if(object.containsKey("city")){
                        orderInfo.invoiceInfo.getAddress().setCity(object.getString("city"));
                    }
                    if(object.containsKey("county")){
                        orderInfo.invoiceInfo.getAddress().setCountry(object.getString("county"));
                    }
                    if(object.containsKey("detailedAddress")){
                        orderInfo.invoiceInfo.getAddress().setDetailAddress(object.getString("detailedAddress"));
                    }
                    if(object.containsKey("zip")){
                        orderInfo.invoiceInfo.getAddress().setZip(object.getString("zip"));
                    }
                    if(object.containsKey("consignee")){
                        orderInfo.invoiceInfo.getAddress().setName(object.getString("consignee"));
                    }
                    if(object.containsKey("mobileNum")){
                        orderInfo.invoiceInfo.getAddress().setMobilePhone(object.getString("mobileNum"));
                    }
                    if(object.containsKey("telephone")){
                        orderInfo.invoiceInfo.getAddress().setTelePhone(object.getString("telephone"));
                    }
                }
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
     * 获取发票地址列表
     *
     * @param addressJsonObject
     * @return
     */
    public static ArrayList<SendAddress> getAddressList(JSONObject addressJsonObject) {
        ArrayList<SendAddress> addressList = new ArrayList<>();
        SendAddress address;
        try {
            if(addressJsonObject==null || !addressJsonObject.containsKey("addresses"))
                return addressList;
            JSONArray allAddressArray = addressJsonObject.getJSONArray("addresses");//得到所有的地址
            if(allAddressArray==null || allAddressArray.size()<=0)
                return addressList;

            for(int i = 0;i<allAddressArray.size();i++){
                JSONObject json = allAddressArray.getJSONObject(i);
                address=new SendAddress();
                if(json.containsKey("addressId"))
                    address.setAddressId(json.getString("addressId"));
                if(json.containsKey("area"))
                    address.setArea(json.getString("area"));
                if(json.containsKey("provinces"))
                    address.setProvince(json.getString("provinces"));
                if(json.containsKey("city"))
                    address.setCity(json.getString("city"));
                if(json.containsKey("county"))
                    address.setCountry(json.getString("county"));
                if(json.containsKey("detailedAddress"))
                    address.setDetailAddress(json.getString("detailedAddress"));
                if(json.containsKey("zip"))
                    address.setZip(json.getString("zip"));
                if(json.containsKey("consignee"))
                    address.setName(json.getString("consignee"));
                if(json.containsKey("mobileNum"))
                    address.setMobilePhone(json.getString("mobileNum"));
                if(json.containsKey("telephone"))
                    address.setTelePhone(json.getString("telephone"));
                if(json.containsKey("defaultChose"))
                    address.setSelected(json.getBoolean("defaultChose"));

                addressList.add(address);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return addressList;
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
            frameInfo.setOnLine(1);
            frameInfo.setIsDownload(0);
            if (frameJsonObject.containsKey("assetName")) {
                frameInfo.setFrameName(frameJsonObject.getString("assetName"));
            }
            if (frameJsonObject.containsKey("imgUrl_H")) {
                frameInfo.setOriginalPathLandscape(frameJsonObject.getString("imgUrl_H"));
            }
            if (frameJsonObject.containsKey("imgUrl_V")) {
                frameInfo.setOriginalPathPortrait(frameJsonObject.getString("imgUrl_V"));
            }
            if (frameJsonObject.containsKey("locationId")) {//特定场馆
                frameInfo.setLocationId(frameJsonObject.getString("locationId"));
            } else {
                frameInfo.setLocationId("common");//通用边框
            }
            if (frameJsonObject.containsKey("active_H")) {
                frameInfo.setIsActive(frameJsonObject.getBoolean("active_H") ? 1 : 0);
            }
            if (frameJsonObject.containsKey("thumbnail_H")) {
                JSONObject thumbnailJsonObject = frameJsonObject.getJSONObject("thumbnail_H");
                if (thumbnailJsonObject.containsKey("x400")) {
                    JSONObject x400JsonObject = thumbnailJsonObject.getJSONObject("x400");
                    if (x400JsonObject.containsKey("url")) {
                        frameInfo.setThumbnailPathLandscape400(x400JsonObject.getString("url"));
                    }
                }
                if (thumbnailJsonObject.containsKey("x160")) {
                    JSONObject x160JsonObject = thumbnailJsonObject.getJSONObject("x160");
                    if (x160JsonObject.containsKey("url")) {
                        frameInfo.setThumbnailPathH160(x160JsonObject.getString("url"));
                    }
                }

            }
            if (frameJsonObject.containsKey("thumbnail_V")) {
                JSONObject thumbNailPortraritJsonObject = frameJsonObject.getJSONObject("thumbnail_V");
                if (thumbNailPortraritJsonObject.containsKey("x300")) {
                    JSONObject x400JsonObject = thumbNailPortraritJsonObject.getJSONObject("x300");
                    if (x400JsonObject.containsKey("url")) {
                        frameInfo.setThumbnailPathPortrait400(x400JsonObject.getString("url"));
                    }
                }
                if (thumbNailPortraritJsonObject.containsKey("x120")) {
                    JSONObject x160JsonObject = thumbNailPortraritJsonObject.getJSONObject("x120");
                    if (x160JsonObject.containsKey("url")) {
                        frameInfo.setThumbnailPathV160(x160JsonObject.getString("url"));
                    }
                }
            }
            if (frameJsonObject.containsKey("fileSize_V")) {
                frameInfo.setFileSize(frameJsonObject.getIntValue("fileSize_V"));
            } else {
                frameInfo.setFileSize(0);
            }
            if (frameJsonObject.containsKey("fileSize_H")) {
                frameInfo.setFileSize(frameInfo.getFileSize() + frameJsonObject.getIntValue("fileSize_H"));
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
            frameInfo.setOnLine(1);
            frameInfo.setIsDownload(0);
            if (stickerJsonObject.containsKey("assetName")) {
                frameInfo.setFrameName(stickerJsonObject.getString("assetName"));
            }
            if (stickerJsonObject.containsKey("imgUrl")) {
                frameInfo.setOriginalPathPortrait(stickerJsonObject.getString("imgUrl"));
            }
            if (stickerJsonObject.containsKey("locationId")) {//特定场馆
                frameInfo.setLocationId(stickerJsonObject.getString("locationId"));
            } else {
                frameInfo.setLocationId("common");//通用边框
            }
            if (stickerJsonObject.containsKey("active")) {
                frameInfo.setIsActive(stickerJsonObject.getBoolean("active") ? 1 : 0);
            }
            if (stickerJsonObject.containsKey("thumbnail")) {
                JSONObject thumbnailJsonObject = stickerJsonObject.getJSONObject("thumbnail");
                if (thumbnailJsonObject.containsKey("x160")) {
                    JSONObject x160JsonObject = thumbnailJsonObject.getJSONObject("x160");
                    if (x160JsonObject.containsKey("url")) {
                        frameInfo.setThumbnailPathH160(x160JsonObject.getString("url"));//测试代码，需要修改。
                    }
                }
            }
            if (stickerJsonObject.containsKey("fileSize")) {
                frameInfo.setFileSize(stickerJsonObject.getIntValue("fileSize"));
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
                    ppPinfo.ownOn = AppUtil.GTMToLocal(ppplist.getString("ownOn")).substring(0, 10);
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
                    ppPinfo.ownOn = AppUtil.GTMToLocal(ppplist.getString("ownOn")).substring(0, 10);
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
                if (pplist.containsKey("photoInfos")) {
                    pPinfo.setAlbumCoverPhotoInfo(getLocationPhoto(pplist.getJSONObject("photoInfos")));
                }
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
    public static boolean dealGetSocketData(Context context, String jsonObjectStr, boolean isMainPage, String orderId) {
        PictureAirLog.v("dealGetSocketData: ", "jsonObjectStr: " + jsonObjectStr);
        boolean isdonePayOrder = false;
        SocketUtil socketUtil = new SocketUtil(context, null);
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

    /**
     * 获取活动对象
     * @param jsonObject
     * @return
     */
    public static DealingInfo getDealingInfo(JSONObject jsonObject) {
        DealingInfo dealingInfo = null;
        if (jsonObject.containsKey("dealings")) {
            JSONArray jsonArray = jsonObject.getJSONArray("dealings");
            if (jsonArray.size() > 0) {
                dealingInfo = JsonTools.parseObject(jsonArray.getJSONObject(0).toJSONString(), DealingInfo.class);
                if (jsonArray.getJSONObject(0).containsKey("isPossible")) {
                    dealingInfo.setPossible(jsonArray.getJSONObject(0).getBoolean("isPossible"));
                }

                if (jsonArray.getJSONObject(0).containsKey("participated")) {
                    dealingInfo.setParticipated(jsonArray.getJSONObject(0).getBoolean("participated"));
                }

                return dealingInfo;
            }
        }
        return null;
    }

    public static GoodsInfo getGoodsInfo(JSONObject jsonObject) {

        GoodsInfo goodsInfo = JsonTools.parseObject(jsonObject.toString(), GoodsInfo.class);
        JSONObject json = null;
        if (jsonObject.containsKey("dealing")) {
            json = jsonObject.getJSONObject("dealing");
            if (json != null) {
                if (json.containsKey("isPossible")) {
                    goodsInfo.getDealing().setPossible(json.getBoolean("isPossible"));
                }
                if (json.containsKey("participated")) {
                    goodsInfo.getDealing().setParticipated(json.getBoolean("participated"));
                }
            }
        }
        return goodsInfo;
    }

    /**
     * 解析广告json
     * @param jsonObject
     * @return
     */
    public static ADLocationInfo getAdLocationInfo(JSONObject jsonObject) {
        ADLocationInfo adLocationInfo = new ADLocationInfo();
        adLocationInfo.setLocationId(jsonObject.getString("locationId"));
        adLocationInfo.setDescriptionCH(jsonObject.getJSONObject("adWords").getString("CN"));
        adLocationInfo.setDescriptionEN(jsonObject.getJSONObject("adWords").getString("EN"));
        return adLocationInfo;
    }

    /**
     * 获取pp列表信息
     * @return
     */
    public static ArrayList<PPinfo> getPPList(JSONObject jsonObject) {
        ArrayList<PPinfo> pPinfoArrayList = new ArrayList<>();
        PictureAirLog.json(jsonObject.toString());
        if (jsonObject.containsKey("PPList")) {
            try {
                JSONArray pplists = jsonObject.getJSONArray("PPList");
                PPinfo ppCodeInfo;
                String ppcode;
                boolean contains;
                // 遍历所有pplist，去除重复的pp
                for (int i = 0; i < pplists.size(); i++) {
                    JSONObject pplist = pplists.getJSONObject(i);
                    ppcode = pplist.getString("customerId");
                    contains = false;
                    // 查看是否有重复的ppcode，需要更新时间和数量
                    for (int j = 0; j < pPinfoArrayList.size(); j++) {
                        if (ppcode.equals(pPinfoArrayList.get(j).getPpCode())) {
                            contains = true;
                            ppCodeInfo = pPinfoArrayList.get(j);
                            ppCodeInfo.setShootDate(pplist.getString("shootDate")); //new add 取最新的时间，解决PP排序问题。
                            ppCodeInfo.setPhotoCount(ppCodeInfo.getPhotoCount() + pplist.getIntValue("photoCount"));
                            break;
                        }
                    }
                    if (!contains) {
                        ppCodeInfo = new PPinfo();
                        ppCodeInfo.setPpCode(pplist.getString("customerId"));
                        ppCodeInfo.setPhotoCount(pplist.getIntValue("photoCount"));
                        ppCodeInfo.setShootDate(pplist.getString("shootDate"));
                        pPinfoArrayList.add(ppCodeInfo);
                    }
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return pPinfoArrayList;
    }

}
