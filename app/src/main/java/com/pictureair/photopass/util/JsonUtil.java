package com.pictureair.photopass.util;


import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.maps.model.LatLng;
import com.pictureair.photopass.entity.BindPPInfo;
import com.pictureair.photopass.entity.CartItemInfo;
import com.pictureair.photopass.entity.CartItemInfo1;
import com.pictureair.photopass.entity.CartPhotosInfo;
import com.pictureair.photopass.entity.CartPhotosInfo1;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.entity.HelpInfo;
import com.pictureair.photopass.entity.OrderInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;

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
        info.place = object.getString("location");
        info.placeUrl = (Common.PHOTO_URL + object.getString("defaultPhoto")).trim();
        if (object.containsKey("GPS")) {
            JSONObject obj = (JSONObject) object.get("GPS");
//			System.out.println("转换之前的坐标"+obj.toString());
//			LatLng latLng = AppUtil.converterFromGPS2BD(obj);//转换成百度坐标系
            LatLng latLng = AppUtil.converterFromGPS2AMAP(obj);//转换成高德坐标系
            info.latitude = latLng.latitude;
            info.longitude = latLng.longitude;
//			System.out.println("转换之后的坐标"+latLng.toString());
        }
        if (object.containsKey("description")) {
            info.placeDetailIntroduce = object.getString("description");
        }
        info.popularity = "popularity";
        info.islove = 0;
        info.showDetail = 0;
        return info;
    }

    /**
     * 照片信息解析 ，并且把数据插入到数据库中作为缓存数据
     */
    public static PhotoInfo getPhoto(SQLiteDatabase db, JSONObject object) throws JSONException {
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
            PictureAirLog.out("get transfer time----> "+ info.shootOn);
            PictureAirLog.out("shootTime----> "+ info.shootTime);
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
//		info.albumName = "";
//		info.isPayed = 0;
        return info;
    }

    /**
     * 用户信息解析
     */
    public static void getUserInfo(final Context context, JSONObject object, Handler handler) throws JSONException {
        SharedPreferences sp = context.getSharedPreferences(Common.USERINFO_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString(Common.USERINFO_TOKENID, object.getString("tokenId"));
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

        String headUrl = null;
        if (obj.containsKey("avatarUrl")) {
            System.out.println("");
            headUrl = obj.getString("avatarUrl");
            e.putString(Common.USERINFO_HEADPHOTO, headUrl);
        }
//        if (headUrl != null) {
        // 更新头像图片
//            headUrl = Common.PHOTO_URL + headUrl;
//            System.out.println("get head image");
//            HttpUtil1.asynDownloadBinaryData(headUrl, new HttpCallback() {
//                @Override
//                public void onSuccess(byte[] binaryData) {
//                    super.onSuccess(binaryData);
//                    try {
//                        System.out.println("get head image success--------");
//                        File userFile = new File(Common.USER_PATH);
//                        if (!userFile.exists()) {
//                            userFile.mkdirs();
//                        }
//                        File headPhoto = new File(Common.USER_PATH + Common.HEADPHOTO_PATH);
//                        headPhoto.createNewFile();
//                        FileOutputStream fos = new FileOutputStream(headPhoto);
//                        fos.write(binaryData);
//                        fos.close();
//                    } catch (FileNotFoundException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//
//            });
//        }
        String bgUrl = null;
        if (obj.containsKey("coverHeaderImage")) {
            bgUrl = obj.getString("coverHeaderImage");
            e.putString(Common.USERINFO_BGPHOTO, bgUrl);
        }
//        if (bgUrl != null) {
//            // 更新背景图片
//            bgUrl = Common.PHOTO_URL + bgUrl;
//
//            HttpUtil1.asynDownloadBinaryData(bgUrl, new HttpCallback() {
//                @Override
//                public void onSuccess(byte[] binaryData) {
//                    super.onSuccess(binaryData);
//                    try {
//                        File bgPhoto = new File(Common.USER_PATH + Common.BGPHOTO_PAHT);
//                        bgPhoto.createNewFile();
//                        FileOutputStream fos = new FileOutputStream(bgPhoto);
//                        fos.write(binaryData);
//                        fos.close();
//                    } catch (FileNotFoundException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//                }
//            });
//        }
        e.commit();
    }

    /**
     * 创建修改购物车参数的jsonobject对象
     *
     * @param photoArrayList 传入需要修改的图片参数，一个是photoId，一个是photoUrl
     * @param cartItem       每一项的item对象，里面会包含price参数
     * @return
     */
    public static JSONArray addAndModifyCartItemJsonArray(ArrayList<PhotoInfo> photoArrayList, CartItemInfo1 cartItem) {
        if (photoArrayList == null) {
            return null;
        }
        JSONArray photoIdArray = new JSONArray();//放入图片的json数组
        try {
            List<CartPhotosInfo1> photoslist;
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
     * 解析购物车信息
     *
     * @param itemObject
     * @return cartItemInfo
     */
    public static CartItemInfo getCartItemInfo(JSONObject itemObject) {
        CartItemInfo cartInfo = new CartItemInfo();
        int quantity;
        try {
            quantity = itemObject.getIntValue("qty");
            cartInfo.cart_quantity = quantity;
            cartInfo.cart_promotionPrice = itemObject.getDouble("price");
            cartInfo.cart_id = itemObject.getString("_id");
            JSONArray productsArray = itemObject.getJSONArray("products");
            JSONObject productJsonObject = productsArray.getJSONObject(0);//默认一个商品只有一个svg文件
            cartInfo.cart_originalPrice = productJsonObject.getDouble("price");
            cartInfo.cart_storeId = productJsonObject.getString("storeId");
            cartInfo.cart_productName = productJsonObject.getString("name");
            if (cartInfo.cart_productName.equals(Common.GOOD_NAME_PPP)) {//ppp
                cartInfo.cart_productType = 3;
            } else if (cartInfo.cart_productName.equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {//pp
                cartInfo.cart_productType = 2;
            } else {//other goods
                cartInfo.cart_productType = 1;
            }
            cartInfo.cart_productId = productJsonObject.getString("productId");
            if (productJsonObject.containsKey("productDescription")) {
                cartInfo.cart_productIntroduce = productJsonObject.getString("productDescription");
            } else {
                cartInfo.cart_productIntroduce = "Made by PictureAir";
            }
            cartInfo.cart_productImageUrl = productJsonObject.getString("productImage");
//			cartInfo.cart_embedPhotoCount = productJsonObject.getInt("embedPhotosCount");
            cartInfo.cart_embedPhotoCount = 2;//暂时写死，应该是1，但是后面空的购物车也会加个空白的项，所以为2
            JSONArray embedphotoArray = productJsonObject.getJSONArray("embedPhotos");
            ArrayList<CartPhotosInfo> gridviewphotolist = new ArrayList<CartPhotosInfo>();
            CartPhotosInfo cartPhotosInfo;
            /****临时添加*****/
//			if (cartInfo.cart_productType == 2) {//如果是pp商品
//				
//				if (0 == embedphotoArray.length()) {//应该没有添加图片的商品
//					
//				}else {//数码商品
//					cartInfo.cart_productType = 1;
//				}
//			}
            /****临时添加*****/
            if (0 == embedphotoArray.size()) {
                System.out.println("0000000000000");
                final int count = quantity;
                cartPhotosInfo = new CartPhotosInfo();
                cartPhotosInfo.cart_photoUrl = "";
                cartPhotosInfo.cart_photoId = "";
                cartPhotosInfo.cart_photoCount = count + "";
                gridviewphotolist.add(cartPhotosInfo);
                cartInfo.hasPhoto = false;
            } else {
                System.out.println("---------buwei000000000");
                JSONObject embedphotoObject = embedphotoArray.getJSONObject(0);//一般一个svg文件只有一个孔去添加图片
                JSONArray photosidJsonArray = embedphotoObject.getJSONArray("photosIds");
                for (int j = 0; j < photosidJsonArray.size(); j++) {
                    JSONObject photoidJsonObject = photosidJsonArray.getJSONObject(j);
                    final int count = quantity;
                    cartPhotosInfo = new CartPhotosInfo();
                    cartPhotosInfo.cart_photoUrl = photoidJsonObject.getString("photoUrl");
                    cartPhotosInfo.cart_photoId = photoidJsonObject.getString("photoId");
                    cartPhotosInfo.cart_photoCount = count + "";
                    gridviewphotolist.add(cartPhotosInfo);
                }
                cartInfo.hasPhoto = true;
            }
            cartInfo.cart_photoUrls = gridviewphotolist;
            if (cartInfo.cart_productType != 1) {//如果是虚拟商品，则不需要加图片
                cartInfo.isFullPhotos = true;
                cartInfo.hasPhoto = true;
                System.out.println("type!=1");
            } else if (gridviewphotolist.size() < cartInfo.cart_embedPhotoCount) {//如果是正常商品，判断已经加的图品数量和需要数量是否一致
                cartInfo.isFullPhotos = false;
                System.out.println("size < count");
            } else {
                cartInfo.isFullPhotos = true;
                cartInfo.hasPhoto = true;
                System.out.println("others");
            }
            cartInfo.isSelect = true;
            cartInfo.show_edit = 0;
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return cartInfo;
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
            orderInfo.orderId = orderJsonObject.getString("_id");//订单ID
            orderInfo.orderStatus = orderJsonObject.getIntValue("status");//订单状态

            JSONObject resumeJsonObject = orderJsonObject.getJSONObject("resume");
            orderInfo.orderTime = resumeJsonObject.getString("time");//订单时间
            orderInfo.orderPayMentMethod = resumeJsonObject.getIntValue("payType");//支付类型
            orderInfo.orderNumber = resumeJsonObject.getString("code");//订单号

            JSONObject deliveryJsonObject = orderJsonObject.getJSONObject("deliveryInfo");
            if (deliveryJsonObject.containsKey("deliveryType")) {//快递方式
                orderInfo.deliveryMethod = deliveryJsonObject.getIntValue("deliveryType");
            }

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
            if (deliveryAddressJsonObject.containsKey("detailedAddress")) {//街道
                orderInfo.deliveryAddress += deliveryAddressJsonObject.getString("detailedAddress");
            }
            if (orderInfo.deliveryAddress == null) {
                orderInfo.deliveryAddress = "";
            }

            JSONObject priceJsonObject = orderJsonObject.getJSONObject("priceInfo");
            orderInfo.deliveryShipping = priceJsonObject.getDouble("shipping");//运费
            orderInfo.productPrice = priceJsonObject.getDouble("productPrice");//商品价格
            orderInfo.orderTotalPrice = orderInfo.deliveryShipping + orderInfo.productPrice;//商品总价

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
        ArrayList<CartItemInfo> orderDetailsArrayList = new ArrayList<CartItemInfo>();
        CartItemInfo cartItemInfo = null;
        JSONObject productInfoJsonObject;
        try {
            productInfoJsonObject = orderJsonObject.getJSONObject("productInfo");
            JSONArray productsArray = productInfoJsonObject.getJSONArray("products");
            //获取订单下的所有商品
            CartPhotosInfo cartPhotosInfo = null;
            JSONArray usePhotosArray;
            JSONObject usePhotoObject;
            for (int i = 0; i < productsArray.size(); i++) {
                cartItemInfo = new CartItemInfo();
                JSONObject productJsonObject = productsArray.getJSONObject(i);
                cartItemInfo.cart_productName = productJsonObject.getString("productName");//商品名字
                cartItemInfo.cart_productImageUrl = productJsonObject.getString("productImage");//商品预览图URL
                cartItemInfo.cart_quantity = productJsonObject.getIntValue("qty");//商品数量
                cartItemInfo.cart_promotionPrice = productJsonObject.getDouble("unitPrice");//商品单价
                //获取添加照片的信息
                usePhotosArray = productJsonObject.getJSONArray("usePhotos");
                if (usePhotosArray.size() == 0) {//如果为0，不赋值

                } else {
                    ArrayList<CartPhotosInfo> photourlsArrayList = new ArrayList<CartPhotosInfo>();
                    for (int j = 0; j < usePhotosArray.size(); j++) {
                        usePhotoObject = usePhotosArray.getJSONObject(j);
                        cartPhotosInfo = new CartPhotosInfo();
                        cartPhotosInfo.cart_photoUrl = usePhotoObject.getString("photoUrl");//商品添加图片的URL
                        photourlsArrayList.add(cartPhotosInfo);
                    }
                    cartItemInfo.cart_photoUrls = photourlsArrayList;
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
                        frameInfo.frameThumbnailPath160 = x160JsonObject.getString("url");
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
                        frameInfo.frameThumbnailPath160 = x160JsonObject.getString("url");
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
                    ppPinfoArrayList.add(ppPinfo);
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }
        return ppPinfoArrayList;

    }

    public static ArrayList<HelpInfo> getHelpInfoList(JSONObject jsonObject){
        ArrayList<HelpInfo> helpInfos= new ArrayList<HelpInfo>();
        JSONArray array = jsonObject.getJSONArray("helpList");
        HelpInfo info = null;
        JSONObject obj = null;
        for(int i= 0; i<array.size();i++){
            info = new HelpInfo();
            obj = (JSONObject)array.get(i);
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
     * @param jsonObject
     * @return
     */
    public static ArrayList<PPinfo> getPPSByPPP(JSONObject jsonObject){
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
}
