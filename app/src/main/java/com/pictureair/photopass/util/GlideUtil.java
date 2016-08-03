package com.pictureair.photopass.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.signature.StringSignature;
import com.pictureair.photopass.R;

import java.util.concurrent.ExecutionException;

/**
 * Created by bauer_bao on 16/8/2.
 * 图片加载类, 统一适配(方便换库,方便管理)
 * 需要什么方法, 就添加什么方法
 */
public class GlideUtil {

    /**
     * 加载字节数组
     * @param context
     * @param bytes
     * @param imageView
     */
    public static void load(Context context, byte[] bytes, ImageView imageView) {
        Glide.with(context)
                .load(bytes)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)//565的图片，有些图片被过度压缩，导致图片泛绿，要么改成8888，要么修改缓存模式，缓存未压缩图片
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_failed)
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 加载url
     * @param context
     * @param url
     * @param imageView
     */
    public static void load(Context context, String url, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_failed)
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 加载url，定制占位图
     * @param context
     * @param url
     * @param placehoderId
     * @param errorId
     * @param imageView
     */
    public static void load(Context context, String url, int placehoderId, int errorId, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placehoderId)
                .error(errorId)
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 加载url，有解密
     * @param context
     * @param url
     * @param isEncrypted
     * @param imageView
     */
    public static void load(Context context, String url, boolean isEncrypted, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_failed)
                .isEncrypted(isEncrypted)
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 加载url，可重新加载图片
     * @param context
     * @param url
     * @param signature
     * @param imageView
     */
    public static void load(Context context, String url, String signature, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_failed)
                .signature(new StringSignature(signature))
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 加载url，可重新加载图片，可定制占位图
     * @param context
     * @param url
     * @param placehoderId
     * @param errorId
     * @param signature
     * @param imageView
     */
    public static void load(Context context, String url, int placehoderId, int errorId, String signature, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placehoderId)
                .error(errorId)
                .signature(new StringSignature(signature))
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 加载url，有解密，可重新加载图片
     * @param context
     * @param url
     * @param isEncrypted
     * @param signature
     * @param imageView
     */
    public static void load(Context context, String url, boolean isEncrypted, String signature, ImageView imageView) {
        Glide.with(context)
                .load(url)
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_failed)
                .isEncrypted(isEncrypted)
                .signature(new StringSignature(signature))
                .dontAnimate()
                .into(imageView);
    }

    /**
     * 加载url，获取bitmap，设置监听
     * @param context
     * @param url
     * @param simpleTarget
     */
    public static void load(Context context, String url, SimpleTarget simpleTarget) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_failed)
                .dontAnimate()
                .into(simpleTarget);
    }

    /**
     * 加载url，获取bitmap，定制占位图，设置监听
     * @param context
     * @param url
     * @param placeholderId
     * @param errorId
     * @param simpleTarget
     */
    public static void load(Context context, String url, int placeholderId, int errorId, SimpleTarget simpleTarget) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(placeholderId)
                .error(errorId)
                .dontAnimate()
                .into(simpleTarget);
    }

    /**
     * 加载url，获取bitmap，有解密，可设置监听
     * @param context
     * @param url
     * @param isEncrypted
     * @param simpleTarget
     */
    public static void load(Context context, String url, boolean isEncrypted, SimpleTarget simpleTarget) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .placeholder(R.drawable.ic_loading)
                .error(R.drawable.ic_failed)
                .isEncrypted(isEncrypted)
                .dontAnimate()
                .into(simpleTarget);
    }

    /**
     * 加载url，获取bitmap，设置监听
     * @param context
     * @param url
     * @param simpleTarget
     */
    public static void loadWithNoPlaceHolder(Context context, String url, SimpleTarget simpleTarget) {
        Glide.with(context)
                .load(url)
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .dontAnimate()
                .into(simpleTarget);
    }

    /**
     * 加载url，根据宽高获取bitmap
     * @param context
     * @param url
     * @param width
     * @param height
     * @return
     */
    public static Bitmap load(Context context, String url, int width, int height) {
        try {
            return  Glide.with(context)
                    .load(url)
                    .asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(width, height)
                    .get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取文件url
     * @param path
     * @return
     */
    public static String getFileUrl(String path) {
        return "file://" + path;
    }

    /**
     * 获取asset路径
     * @param assetFileName
     * @return
     */
    public static String getAssetUrl(String assetFileName) {
        return "file:///android_asset/" + assetFileName;
    }

    /**
     * 获取raw路径
     * @param context
     * @param rawId
     * @return
     */
    public static String getRawUrl(Context context, int rawId){
        return "android.resource://" + context.getPackageName() + "/raw/" + rawId;
    }

    /**
     * 获取drawable路径
     * @param context
     * @param drawbleId
     * @return
     */
    public static String getDrawableUrl(Context context, int drawbleId) {
        return "android.resource://" + context.getPackageName() + "/drawable/" + drawbleId;
    }
}
