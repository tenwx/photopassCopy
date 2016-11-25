package com.pictureair.photopass.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

/**
 * 图片封装类
 *
 * @author bauer_bao
 */
@Entity
public class PhotoInfo2 {
    /**
     * shootDate : 2016-10-20
     * presetId : 000000000000000000000000
     * strShootOn : 2016-10-20 10:06:57
     * enImage : false
     * customerIds : [{"code":"SHDRC22AMWK5H4NB"}]
     * _id : 580827e8a482ecfc510000f2
     * receivedOn : 2016-10-20T02:06:57.000Z
     * mimeType : jpg
     * locationId : lk2
     * modifiedOn : 2016-10-20T06:46:42.558Z
     * shootOn : 2016-10-20T02:06:57.000Z
     * isPaid : true
     */

    @Id
    private long id;//自增长id
    private String photoId;//网络图片的话就是photoId，本地图片的话，就是原始路径
    private String photoPassCode;//网络图片的PPCode
    private String shootDate;// 拍摄时间 年月日，用于页面显示
    private String photoThumbnail_128;//缩略图128尺寸路径
    private String photoThumbnail_512;//缩略图512尺寸的路径
    private String photoThumbnail_1024;//缩略图1024尺寸的路径
    private String photoOriginalURL;//图片的原始路径
    private String locationId;//照片拍摄地点
    private String strShootOn;//拍摄时间 年月日时分秒，用来数据库的排序
    private boolean isPaid;//网络图片是否已经购买属性，1已付，0，未支付
    private String shareURL;//网络图片分享的URL
    private int isVideo;//1是视频，0是图片
    private int fileSize;//文件大小
    private int videoWidth;//视频文件宽
    private int videoHeight;//视频文件高
    private int hasPreset; // 照片是否有模版，0，代表没有模板，1，代表有模版
    private boolean enImage;//是否是加密的图片 0：未加密；1：已加密
    private String adURL;//广告链接
    private int onLine;//1线上图片，0，本地图片

    @Transient
    private String locationName;//每张图片的地点名称
    @Transient
    private String receivedOn;//每张图片到服务器的时间，用于刷新加载操作
    @Transient
    private int isRefreshInfo;//0：不需要刷新旧数据，1：需要刷新旧数据列表，只针对刷新的旧图片，其他均用不到
    @Transient
    private int sectionId;//悬浮的id
    @Transient
    private String failedTime;
    @Transient
    private int isChecked;//图片是否被检索到，用在ViewPhotoActivity中的全选操作，1，true，0，false
    @Transient
    private int isSelected;//图片是否被选中，1，选中，0，未选中

}
