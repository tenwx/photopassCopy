package com.pictureair.photopass.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ViewPhotoGridViewAdapter;
import com.pictureair.photopass.entity.GoodsInfo1;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DisneyVideoTool;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ReflectionUtil;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 预览照片，总共有4大类，1，全部的照片，2，pictureair网络获取的图片，3，本软件拍的照片，4，已经购买的照片
 * 只能选择照片或者拍摄新的照片，不能做其他操作
 *
 * @author bauer_bao
 */

public class SelectPhotoActivity1 extends BaseActivity implements OnClickListener {
    private static final String TAG = "SelectPhotoActivity1";
    //申明控件
    private ImageView rtLayout;
    private Button btnGoToSelectPhoto;
    private TextView okButton, tvHead;
    private GridView gridView;
    private ViewPhotoGridViewAdapter photoPassAdapter;
    private RelativeLayout noPhotoRelativeLayout;

    private MyToast newToast;
    private MyApplication myApplication;
    private ArrayList<PhotoInfo> photoPassArrayList;
    private String activity = null;

    private ArrayList<PhotoInfo> photoURLlist = new ArrayList<>();//选择的图片的list
    private int photocount = 1;//需要添加的图片数量，以后要改这个数值
    private int selectedCount = 0;//已经选择了的图片数量
    private GoodsInfo1 goodsInfo;//存放商品信息

    private boolean isBuy = false;//只显示已购买的照片
    private LinearLayout llNullPhoto;
    private PopupWindow popupWindow;
    private CustomProgressDialog customProgressDialog;
    private Context context;
    //底部view
    private LinearLayout llDisneyVideoFoot,llShopPhoto;
    private TextView tvSelectPhotoOk;

    private final Handler selectPhotoHandler = new SelectPhotoHandler(this);


    private static class SelectPhotoHandler extends Handler{
        private final WeakReference<SelectPhotoActivity1> mActivity;

        public SelectPhotoHandler(SelectPhotoActivity1 activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            case 111:
                okButton.setText(String.format(getString(R.string.hasselectedphoto), msg.arg1, photocount));//更新button
                break;

            case API1.UPLOAD_PHOTO_MAKE_VIDEO_SUCCESS:
                // 发送成功
                if (null != customProgressDialog && customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                initPopWindow();
                break;

            case API1.UPLOAD_PHOTO_MAKE_VIDEO_FAILED:
                // 处理失败，数据错误
//                    initPopWindow();
                if (null != customProgressDialog && customProgressDialog.isShowing()) {
                    customProgressDialog.dismiss();
                }
                newToast.setTextAndShow(getString(ReflectionUtil.getStringId(context, msg.arg1)), Common.TOAST_SHORT_TIME);
                PictureAirLog.e(TAG, "处理失败，数据错误" + getString(ReflectionUtil.getStringId(context, msg.arg1)));
                break;

            default:
                break;
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_photo_1);
        activity = getIntent().getStringExtra("activity");
        photocount = getIntent().getIntExtra("photoCount", 1);
        context = this;
        goodsInfo = (GoodsInfo1) getIntent().getSerializableExtra("goodsInfo");
        if (goodsInfo != null) {
            photocount = goodsInfo.getEmbedPhotosCount();
        }
        if (photocount == 0) {
            photocount = 1;
        }
        initview();
    }

    //初始化函数
    private void initview() {
        //初始化资源
        newToast = new MyToast(this);
        myApplication = (MyApplication) getApplication();
        //初始化控件
        okButton = (TextView) findViewById(R.id.button1);
        rtLayout = (ImageView) findViewById(R.id.rlrt);
        gridView = (GridView) findViewById(R.id.gridView_all);
        gridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
        //空照片介绍页面
        llNullPhoto = (LinearLayout) findViewById(R.id.ll_null_photo);
        btnGoToSelectPhoto = (Button) findViewById(R.id.btn_goto_select);
        btnGoToSelectPhoto.setTypeface(MyApplication.getInstance().getFontBold());
        tvHead = (TextView) findViewById(R.id.tv_head);
        noPhotoRelativeLayout = (RelativeLayout) findViewById(R.id.no_photo_relativelayout);
        initDisneySelectPhotoFootView();

        //绑定监听
        rtLayout.setOnClickListener(this);
        okButton.setOnClickListener(this);
        btnGoToSelectPhoto.setOnClickListener(this);

        /*
         * 更新标题
         * 迪士尼视频页面的选择照片底部有3个Icon
         */
        if (activity != null && activity.equals(DisneyVideoTool.DISNEY_VIDEO)) {
            tvHead.setText(getResources().getString(R.string.story_tab_bought));
            llDisneyVideoFoot.setVisibility(View.VISIBLE);
            tvSelectPhotoOk.setVisibility(View.VISIBLE);
            moveOkButtonLocation();
            isBuy = true;
        }

        //初始化数据列表
        photoPassArrayList = new ArrayList<>();
        photoPassArrayList = transferPhotoItemInfoToPhotoInfo(isBuy);
        PictureAirLog.v(TAG, "pp photo size: " + photoPassArrayList.size());

        //判断是否有照片
        if (isBuy) {
            if (photoPassArrayList != null && photoPassArrayList.size() > 0) {
                llNullPhoto.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
            } else {
                gridView.setVisibility(View.GONE);
                llNullPhoto.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.GONE);
                okButton.setEnabled(false);
                return;
            }
        } else {
            if (photoPassArrayList != null && photoPassArrayList.size() > 0) {
                noPhotoRelativeLayout.setVisibility(View.GONE);
                gridView.setVisibility(View.VISIBLE);
            } else {
                gridView.setVisibility(View.GONE);
                noPhotoRelativeLayout.setVisibility(View.VISIBLE);
                okButton.setVisibility(View.GONE);
                okButton.setEnabled(false);
                return;
            }
        }
        //设置默认没有选中
        photoPassAdapter = new ViewPhotoGridViewAdapter(this, photoPassArrayList);
        gridView.setAdapter(photoPassAdapter);
        gridView.setOnItemClickListener(new PhotoSelectedListener());

        //初始化数据
        if (myApplication.needScanPhoto()) {//如果刚进入这个页面，则onresume不需要重新扫描文件，所以直接将变量设为false，以免造成oncreate和onresume同时进行，造成collection数组越界
            myApplication.setneedScanPhoto(false);
        }
        //获取可选图片总数
        okButton.setVisibility(View.VISIBLE);
        okButton.setText(String.format(getString(R.string.hasselectedphoto), 0, photocount));
    }

    /**
     * 如果是迪士尼视频进入到本页面（那么okButton在底部显示）
     */
    private void moveOkButtonLocation() {
        int width = ScreenUtil.getScreenWidth(context);
        int height = ScreenUtil.getScreenHeight(context);
        okButton.layout(20,20,20,20);
    }

    /**
     * 初始化视频制作选择照片的底部view
     */
    private void initDisneySelectPhotoFootView() {
        llDisneyVideoFoot = (LinearLayout)findViewById(R.id.ll_disney_video_foot);
        llShopPhoto = (LinearLayout)findViewById(R.id.ll_shop_photo);
        tvSelectPhotoOk = (TextView)findViewById(R.id.tv_select_photo_ok);
        llShopPhoto.setOnClickListener(this);
        tvSelectPhotoOk.setVisibility(View.GONE);
//        tvSelectPhotoOk.setOnClickListener(this);
    }




    /**
     * 将photoItemInfo的列表转成photoInfo的列表
     */
    public ArrayList<PhotoInfo> transferPhotoItemInfoToPhotoInfo(boolean isBuy) {
        ArrayList<PhotoInfo> list = new ArrayList<>();
        PictureAirLog.v(TAG, "myApplication pp photo size: " + myApplication.photoPassPicList.size());
        for (PhotoInfo photoInfo : myApplication.photoPassPicList) {
            photoInfo.isChecked = 1;
            photoInfo.isSelected = 0;
            photoInfo.showMask = 0;
            if (isBuy) {
                if (photoInfo.isPayed == 1) {
                    list.add(photoInfo);
                }
            } else {
                if (goodsInfo == null) {
                    PictureAirLog.v(TAG, "goodsInfo == null");
                    return list;
                }
                //数码照片--是则获取未购买的图片 礼物--获取全部
                if (goodsInfo.getName().equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
                    if (photoInfo.isPayed == 0) {
                        list.add(photoInfo);
                    }
                } else {
                    list.add(photoInfo);
                }
            }
        }
        return list;
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (myApplication.needScanPhoto()) {//需要刷新
            PictureAirLog.v(TAG, "need scan photo--------------");
//            checkNewPhotos(Common.PHOTO_SAVE_PATH, magicArrayList, Common.ALBUM_MAGIC);
            myApplication.setneedScanPhoto(false);
        } else {
            PictureAirLog.v(TAG, "has new edit photo false");
        }
    }


    //gridview点击监听
    private class PhotoSelectedListener implements AdapterView.OnItemClickListener {
        private PhotoInfo info;
        private ViewPhotoGridViewAdapter viewPhotoGridViewAdapter;

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //获取对应相册中的SelectPhotoItemInfo对象
            info = photoPassArrayList.get(position);
            viewPhotoGridViewAdapter = photoPassAdapter;
            Message msg = selectPhotoHandler.obtainMessage();
            //获取已经选择的照片数量
            selectedCount = photoURLlist.size();
            //判断数量
            if (selectedCount >= 0) {
                PictureAirLog.v(TAG, selectedCount + "current:" + position);
                if (info.isSelected == 1) {//取消选中
                    if (photoURLlist.contains(info)) {//存在
                        photoURLlist.remove(info);
                    }
                    info.isSelected = 0;
                    info.showMask = 0;
                    selectedCount--;
                    PictureAirLog.v(TAG, "点过了，取消选中");
                    int visiblePos = gridView.getFirstVisiblePosition();

                    viewPhotoGridViewAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
                } else {
                    if (selectedCount < photocount) {
                        info.isSelected = 1;
                        info.showMask = 1;
                        PictureAirLog.v(TAG, "没点过，选中");
                        selectedCount++;
                        int visiblePos = gridView.getFirstVisiblePosition();
                        viewPhotoGridViewAdapter.refreshView(position, gridView.getChildAt(position - visiblePos), 1);
                        if (photoURLlist.contains(info)) {//说明已经存在
                            newToast.setTextAndShow(R.string.photo_selected, Common.TOAST_SHORT_TIME);
                        } else {
                            photoURLlist.add(info);
                        }
                    } else {
                        newToast.setTextAndShow(String.format(getString(R.string.limit_photos), photocount), Common.TOAST_SHORT_TIME);
                    }
                }
                msg.what = 111;
                msg.arg1 = selectedCount;
                selectPhotoHandler.sendMessage(msg);//通知主UI更改信息
            } else {
                newToast.setTextAndShow(String.format(getString(R.string.limit_photos), photocount), Common.TOAST_SHORT_TIME);
            }
            isEnabled();
        }
    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.view_mask:
            case R.id.btn_submit:
                popupWindow.dismiss();
                finish();
                break;

            case R.id.ll_shop_photo:
            case R.id.btn_goto_select:
                //删除所有aty，只剩下mainTab页面，
                //将mainTab切换到shop Tab
                AppManager.getInstance().killOtherActivity(MainTabActivity.class);
                myApplication.setChangeToShopTab(true);
                break;

            case R.id.rlrt://返回按钮
                finish();
                break;

            case R.id.button1://选择确定按钮
                if (AppUtil.getNetWorkType(context) == AppUtil.NETWORKTYPE_INVALID) {
                    newToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);
                    return;
                }
                if (photoURLlist.size() == 0) {
                    newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
                } else if (photoURLlist.size() < photocount) {
                    newToast.setTextAndShow(R.string.select_photos, Common.TOAST_SHORT_TIME);
                } else {
                    if (activity.equals("detailproductactivity") || activity.equals("previewproductactivity")) {
                        //从详细商品界面进入
                        PictureAirLog.v(TAG, "选择图片数量" + photoURLlist.size());
                        intent = new Intent(this, PreviewProductActivity.class);
                        intent.putExtra("goodsInfo", goodsInfo);
                        intent.putExtra("photopath", photoURLlist);
                        startActivity(intent);
                    } else if (activity.equals("makegiftactivity")) {
                        //从其他界面进来，返回新选择的照片
                        intent = new Intent();
                        intent.putExtra("photopath", photoURLlist);
                        setResult(20, intent);
                        finish();
                    } else if (activity.equals("submitorderactivity")) {
                        //从其他界面进来，返回新选择的照片
                        intent = new Intent();
                        intent.putExtra("photopath", photoURLlist);
                        setResult(20, intent);
                        finish();
                    } else if (activity.equals("cartactivity")) {
                        PictureAirLog.v(TAG, "提交按钮: " + photoURLlist.get(0).photoPathOrURL);
                        intent = new Intent();
                        intent.putExtra("photopath", photoURLlist);
                        setResult(20, intent);
                        finish();
                    } else if (activity.equals("disney_video")) {
                        customProgressDialog = CustomProgressDialog.show(context, context.getString(R.string.is_loading), false, null);
                        StringBuffer photos = new StringBuffer();
                        for (int i = 0; i < photoURLlist.size(); i++) {
                            String photoId = photoURLlist.get(i).photoId;
                            if (i == 0) {
                                photos.append(photoId);
                            } else {
                                photos.append("," + photoId);
                            }
                        }
                        PictureAirLog.i(TAG, "photos===>" + photos.toString());
                        API1.uploadPhotoMakeVideo(photos.toString(), selectPhotoHandler);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void isEnabled() {
        if (photoURLlist.size() == photocount) {
            okButton.setEnabled(true);
            okButton.setTextColor(getResources().getColor(R.color.white));
        } else {
            okButton.setEnabled(false);
            okButton.setTextColor(getResources().getColor(R.color.gray_light5));
        }
    }

    private void initPopWindow() {
        // 利用layoutInflater获得View
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popView = inflater.inflate(R.layout.popupwindow_disney_video_select_photo, null);
        popupWindow = new PopupWindow(popView,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(getResources().getColor(R.color.transparent));
        popupWindow.setBackgroundDrawable(dw);
        //设置popwindow出现和消失动画
        popupWindow.setAnimationStyle(R.style.from_center_anim);
        popupWindow.showAtLocation(okButton, Gravity.BOTTOM, 0, 0);
        popupWindow.setOutsideTouchable(false);
        TextView tv1 = (TextView) popView.findViewById(R.id.tv_video_popup1);
        tv1.setTypeface(MyApplication.getInstance().getFontBold());
        Button btnSubmit = (Button) popView.findViewById(R.id.btn_submit);
        btnSubmit.setTypeface(MyApplication.getInstance().getFontBold());
        View view = (View) popView.findViewById(R.id.view_mask);
        view.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
//        popupWindow.showAtLocation(gridView, Gravity.BOTTOM, 100, 100);
        popupWindow.update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        selectPhotoHandler.removeCallbacksAndMessages(null);
    }
}
