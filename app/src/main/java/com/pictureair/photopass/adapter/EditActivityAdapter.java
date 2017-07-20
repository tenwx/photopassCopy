package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.PWDialog;
import com.pictureair.photopass.editPhoto.util.PWEditUtil;
import com.pictureair.photopass.editPhoto.util.PhotoCommon;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.greendao.PictureAirDbManager;
import com.pictureair.photopass.http.rxhttp.RxSubscribe;
import com.pictureair.photopass.util.API2;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.PWToast;
import com.trello.rxlifecycle.components.RxActivity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;

/**
 * 图片编辑 边框，滤镜，饰品的适配器
 */
public class EditActivityAdapter extends RecyclerView.Adapter<EditActivityAdapter.EditHolder> implements PWDialog.OnPWDialogClickListener {
    private Context mContext;
    private List<String> stickerPathList;
    private int editType = PhotoCommon.EditNone;
    private int[] filterText = { R.string.original, R.string.lomo, R.string.earlybird, R.string.vintage,
            R.string.hdr, R.string.whitening, R.string.natural };
    private ArrayList<FrameOrStikerInfo> frameInfos;
    private Handler handler;
    private boolean firstFileFailOrExist = false;
    private boolean secondFileFailOrExist = false;
    private static final int UPDATE_PROGRESS = 101;//更新进度条
    private static final int DOWNLOAD_DIALOG = 102;
    private long firstFileProgress = 0;//文件下载的进度
    private long secondFileProgress = 0;//文件下载进度
    private PWDialog pwDialog;
    private PWToast myToast;
    private EditHolder selectHolder;
    private int position;
    private int bmpWidth;
    private int bmpHeight;
    //图片路径
    private String mPhotoPath;
    private boolean mLocal;


    class EditHolder extends RecyclerView.ViewHolder {
        ImageView editImageview;//编辑的图片
        TextView editText;//文字
        ProgressBar progressBar;//进度条
        LinearLayout itemLayout;//布局
        ImageView maskImageView;//蒙版
        RelativeLayout itemRelativeLayout;
        TextView fileSizeTextView;//文件大小

        public EditHolder(View itemView) {
            super(itemView);
            editImageview = (ImageView) itemView.findViewById(R.id.editImageview);
            editText = (TextView) itemView.findViewById(R.id.editTextview);
            progressBar = (ProgressBar) itemView.findViewById(R.id.progressBar);
            itemLayout = (LinearLayout) itemView.findViewById(R.id.item);
            maskImageView = (ImageView) itemView.findViewById(R.id.frame_mask);
            itemRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.edit_item);
            fileSizeTextView = (TextView) itemView.findViewById(R.id.file_size);
        }
    }

    private Handler downloadHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    EditHolder holder = (EditHolder) msg.obj;
                    holder.progressBar.setProgress(msg.arg1);
                    holder.fileSizeTextView.setText(msg.arg1 + "%");

                    if (firstFileProgress + secondFileProgress == 100) {//下载完成的处理
                        holder.progressBar.setVisibility(View.GONE);
                        holder.maskImageView.setVisibility(View.GONE);
                        holder.fileSizeTextView.setVisibility(View.GONE);
                        frameInfos.get(position).setIsDownload(1);
                        firstFileFailOrExist = false;
                        secondFileFailOrExist = false;
                        firstFileProgress = 0;
                        secondFileProgress = 0;
                        PictureAirDbManager.updateFrameAndStickerDownloadStatus(frameInfos.get(position).getFrameName(), 1);
                    }
                    break;

                default:
                    break;
            }
            return false;
        }
    });

    public EditActivityAdapter(Context context, String photoPath, List<String> stickerPathList, int editType, ArrayList<FrameOrStikerInfo> frameInfos, int width, int height, boolean local,Handler handler) {
        this.mContext = context;
        this.editType = editType;
        mPhotoPath = photoPath;
        this.stickerPathList = stickerPathList;
        this.frameInfos = frameInfos;
        this.handler = handler;
        this.bmpWidth = width;
        this.bmpHeight = height;
        this.mLocal = local;
        myToast = new PWToast(context);
    }

    @Override
    public EditHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.edit_item, parent, false);
        return new EditHolder(view);
    }

    @Override
    public void onBindViewHolder(EditHolder holder, int position) {
        holder.editText.setVisibility(View.GONE);
        holder.maskImageView.setVisibility(View.GONE);
        holder.fileSizeTextView.setVisibility(View.INVISIBLE);
        holder.progressBar.setVisibility(View.INVISIBLE);
        if (editType == PhotoCommon.EditFilter) {//滤镜
            LayoutParams layoutParams = holder.itemRelativeLayout.getLayoutParams();
            layoutParams.height = ScreenUtil.dip2px(mContext, 50);
            layoutParams.width = ScreenUtil.dip2px(mContext, 50);

            LayoutParams layoutParams1 = holder.itemLayout.getLayoutParams();
            layoutParams1.width = layoutParams.width;

            holder.itemLayout.setLayoutParams(layoutParams1);
            holder.itemRelativeLayout.setLayoutParams(layoutParams);

            GlideUtil.load(mContext, stickerPathList.get(position), R.drawable.decoration_bg, R.drawable.ic_failed, holder.editImageview);
            holder.editText.setText(filterText[position]);
            holder.editText.setVisibility(View.VISIBLE);
            holder.itemRelativeLayout.setOnClickListener(new ItemOnClickListener(holder, position));
        }
        if (editType == PhotoCommon.EditSticker) {//饰品
            LayoutParams layoutParams = holder.itemRelativeLayout.getLayoutParams();
            layoutParams.height = ScreenUtil.dip2px(mContext, 60);
            layoutParams.width = ScreenUtil.dip2px(mContext, 60);
            holder.itemRelativeLayout.setLayoutParams(layoutParams);

            LayoutParams layoutParams1 = holder.editImageview.getLayoutParams();
            layoutParams1.width = ScreenUtil.dip2px(mContext, 50);
            layoutParams1.height = ScreenUtil.dip2px(mContext, 50);
            holder.editImageview.setLayoutParams(layoutParams1);

            LayoutParams layoutParams2 = holder.itemLayout.getLayoutParams();
            layoutParams2.width = layoutParams.width;
            holder.itemLayout.setLayoutParams(layoutParams2);

            if (frameInfos.get(position).getOnLine() == 1) {//网络图片
                GlideUtil.load(mContext, Common.PHOTO_URL + frameInfos.get(position).getOriginalPathPortrait(), R.drawable.decoration_bg, R.drawable.ic_failed, holder.editImageview);
            } else {//本地assets图片
                GlideUtil.load(mContext, frameInfos.get(position).getOriginalPathPortrait(), R.drawable.decoration_bg, R.drawable.ic_failed, holder.editImageview);
            }
            holder.itemRelativeLayout.setBackgroundResource(R.drawable.decoration_bg);
            holder.itemRelativeLayout.setOnClickListener(new ItemOnClickListener(holder, position));
        }

        if (editType == PhotoCommon.EditFrame) {//边框
            LayoutParams layoutParams = holder.itemRelativeLayout.getLayoutParams();
            LayoutParams layoutParam1 = holder.editImageview.getLayoutParams();
            LayoutParams layoutParams2 = holder.itemLayout.getLayoutParams();
            if (mLocal) {
                GlideUtil.loadTarget(mContext, mPhotoPath, bmpWidth, bmpHeight, new FrameListTarget(holder, position));
            } else {
                GlideUtil.load(mContext, mPhotoPath, new FrameListTarget(holder, position));
            }

            if (bmpWidth > bmpHeight) {
                layoutParams.height = LayoutParams.MATCH_PARENT;
                layoutParams.width = ScreenUtil.dip2px(mContext, 80);
                layoutParams2.width =  layoutParams.width + 10;

                layoutParam1.height = ScreenUtil.dip2px(mContext, 60);
                layoutParam1.width = ScreenUtil.dip2px(mContext, 80);
            } else {
                layoutParams.height = LayoutParams.MATCH_PARENT;
                layoutParams.width = ScreenUtil.dip2px(mContext, 65*3/4);
                layoutParams2.width = layoutParams.width + 10;

                layoutParam1.height = ScreenUtil.dip2px(mContext, 80);
                layoutParam1.width = ScreenUtil.dip2px(mContext, 60);
            }
            holder.itemLayout.setLayoutParams(layoutParams2);
            holder.itemRelativeLayout.setLayoutParams(layoutParams);
            holder.editImageview.setLayoutParams(layoutParam1);
            PWEditUtil.setMargins(holder.itemRelativeLayout, 5, 5, 5, 5);
            holder.itemRelativeLayout.setOnClickListener(new ItemOnClickListener(holder, position));
        }
    }

    @Override
    public int getItemCount() {
        if (editType == PhotoCommon.EditFrame || editType == PhotoCommon.EditSticker) {
            return frameInfos.size();
        } else {
            return stickerPathList.size();
        }
    }

    /**
     * item点击监听
     *
     * @author bauer_bao
     */
    private class ItemOnClickListener implements OnClickListener {
        private EditHolder itemClickHolder;
        private int position;

        public ItemOnClickListener(EditHolder holderView, int position) {
            this.itemClickHolder = holderView;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if(editType == PhotoCommon.EditFrame) {
                if (frameInfos.get(position).getOnLine() == 1 && frameInfos.get(position).getIsDownload() == 0) {//网络图片，并且未下载
                    itemClickHolder.progressBar.setVisibility(View.VISIBLE);//开始下载
                    itemClickHolder.fileSizeTextView.setVisibility(View.VISIBLE);
                    itemClickHolder.progressBar.setProgress(0);
                    downloadFrame(position, true, itemClickHolder);
                    downloadFrame(position, false, itemClickHolder);
                    if (AppUtil.getNetWorkType(mContext) == AppUtil.NETWORKTYPE_INVALID) {//无网络
                        myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);

                    } else if (AppUtil.getNetWorkType(mContext) == AppUtil.NETWORKTYPE_MOBILE) {//使用流量
                        showDownloadDialog(itemClickHolder, position);

                    } else {//wifi
                        prepareDownload(itemClickHolder, position);
                    }

                } else {//本地图片，或者网络图片已经下载
                    //开始加载图片
                    Message message = handler.obtainMessage();
                    message.what = PhotoCommon.OnclickFramePosition;
                    message.arg1 = position;
                    handler.sendMessage(message);
                }
            }else if(editType == PhotoCommon.EditSticker){
                Message message = handler.obtainMessage();
                message.what = PhotoCommon.OnclickStickerPosition;
                message.arg1 = position;
                handler.sendMessage(message);
            }else if(editType == PhotoCommon.EditFilter){
                Message message = handler.obtainMessage();
                message.what = PhotoCommon.OnclickFilterPosition;
                message.arg1 = position;
                handler.sendMessage(message);
            }
        }
    }

    /**
     * 创建下载对话框
     *
     * @param holder
     * @param position
     */
    private void showDownloadDialog(EditHolder holder, int position) {
        this.selectHolder = holder;
        this.position = position;

        if (pwDialog == null) {
            pwDialog = new PWDialog(mContext, DOWNLOAD_DIALOG)
                    .setPWDialogMessage(R.string.dialog_download_message)
                    .setPWDialogNegativeButton(R.string.dialog_cancel)
                    .setPWDialogPositiveButton(R.string.dialog_ok)
                    .setOnPWDialogClickListener(this)
                    .pwDialogCreate();
        }
        pwDialog.pwDilogShow();
    }

    /**
     * 对话框点击事件监听
     *
     * @author bauer_bao
     */
    @Override
    public void onPWDialogButtonClicked(int which, int dialogId) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (dialogId == DOWNLOAD_DIALOG) {
                    PictureAirLog.out("positive button");
                    prepareDownload(this.selectHolder, position);
                }
                break;
        }
    }

    /**
     * 准备下载
     *
     * @param holder
     * @param position
     */
    private void prepareDownload(EditHolder holder, int position) {
        holder.progressBar.setVisibility(View.VISIBLE);//开始下载
        holder.fileSizeTextView.setVisibility(View.VISIBLE);
        holder.progressBar.setProgress(0);
        downloadFrame(position, true, holder);
        downloadFrame(position, false, holder);
    }

    /**
     * 下载边框
     *
     * @param position
     * @param firstTime
     * @param holder
     */
    private void downloadFrame(final int position, final boolean firstTime, final EditHolder holder) {
        String downloadNameString = null;
        String url = null;
        if (firstTime) {
            url = frameInfos.get(position).getOriginalPathLandscape();
            if (url == null || url.equals("")) {//文件不存在
                firstFileFailOrExist = true;
                return;
            }
            downloadNameString = "frame_landscape_" + AppUtil.getReallyFileName(url,0);
        } else {
            url = frameInfos.get(position).getOriginalPathPortrait();
            if (url == null || url.equals("")) {//文件不存在
                secondFileFailOrExist = true;
                return;
            }
            downloadNameString = "frame_portrait_" + AppUtil.getReallyFileName(url,0);
        }

        final File file = new File(mContext.getFilesDir(), "frames");
        if (!file.exists()) {
            file.mkdirs();
        }
        final File downloadFile = new File(file.toString(), downloadNameString);
        PictureAirLog.e("adapeter","downloadFile:"+downloadFile.toString());
        if (downloadFile.exists()) {//文件存在
            PictureAirLog.out(downloadNameString + "file exist");
            if (firstTime) {
                firstFileFailOrExist = true;
            } else {
                secondFileFailOrExist = true;
            }
            return;
        }
        final String fileName = downloadNameString;

        API2.downloadHeadFile(Common.PHOTO_URL + url, new com.pictureair.photopass.http.rxhttp.HttpCallback() {
            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                if (firstTime) {
                    firstFileProgress = bytesWritten * 50 / totalSize;
                } else {
                    secondFileProgress = bytesWritten * 50 / totalSize;
                }
                Message message = downloadHandler.obtainMessage();
                message.what = UPDATE_PROGRESS;
                message.obj = holder;
                if (firstFileFailOrExist || secondFileFailOrExist) {//有文件存在，或者下载失败，需要在进度上加50
                    message.arg1 = (int) (50 + bytesWritten * 50 / totalSize);
                } else {//都可以正常下载，下载进度取两者平均值
                    message.arg1 = (int) (firstFileProgress + secondFileProgress);
                }
                downloadHandler.sendMessage(message);
            }
        })
                .map(new Func1<ResponseBody, String>() {
                    @Override
                    public String call(ResponseBody responseBody) {
                        try {
                            return AppUtil.writeFile(responseBody, file.toString(), fileName);
                        } catch (Exception e) {
                            throw Exceptions.propagate(e);
                        }
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .compose(((RxActivity)mContext).<String>bindToLifecycle())
                .subscribe(new RxSubscribe<String>() {
                    @Override
                    public void _onNext(String s) {
                    }

                    @Override
                    public void _onError(int status) {
                        downloadFile.delete();
                        if (firstTime) {
                            firstFileFailOrExist = true;
                        } else {
                            secondFileFailOrExist = true;
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private class FrameListTarget extends SimpleTarget<Bitmap> {
        private  EditHolder frameListHolder;
        private int position;
        public FrameListTarget(EditHolder holderView, int position) {
            this.frameListHolder = holderView;
            this.position = position;
        }

        @Override
        public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
            if (bitmap == null || bitmap.isRecycled()) return;
            frameListHolder.editImageview.setBackgroundDrawable(new BitmapDrawable(bitmap));
            frameListHolder.editImageview.setScaleType(ImageView.ScaleType.FIT_XY);
//			System.out.println(position + " ---->" + frameInfos.get(position).frameThumbnailPath160);
            if (frameInfos.get(position).getOnLine() == 1) {
                // 网络边框。 3.0版本
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    GlideUtil.load(mContext, Common.PHOTO_URL + frameInfos.get(position).getThumbnailPathH160(), R.drawable.decoration_bg, R.drawable.ic_failed, frameListHolder.editImageview);
                }else{
                    GlideUtil.load(mContext, Common.PHOTO_URL + frameInfos.get(position).getThumbnailPathV160(), R.drawable.decoration_bg, R.drawable.ic_failed, frameListHolder.editImageview);
                }

                if (frameInfos.get(position).getIsDownload() == 0) {
                    frameListHolder.maskImageView.setVisibility(View.VISIBLE);
                    frameListHolder.fileSizeTextView.setVisibility(View.VISIBLE);
                    frameListHolder.fileSizeTextView.setText(frameInfos.get(position).getFileSize() / 1024 / 1024 + "M");
                }else {
                    frameListHolder.maskImageView.setVisibility(View.GONE);
                    frameListHolder.fileSizeTextView.setVisibility(View.INVISIBLE);
                }
            } else {
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    GlideUtil.load(mContext, frameInfos.get(position).getThumbnailPathH160(), R.drawable.decoration_bg, R.drawable.ic_failed, frameListHolder.editImageview);
                } else {
                    GlideUtil.load(mContext, frameInfos.get(position).getThumbnailPathV160(), R.drawable.decoration_bg, R.drawable.ic_failed, frameListHolder.editImageview);
                }
            }
        }
    }

}
