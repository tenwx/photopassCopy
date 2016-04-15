package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.pictureair.photopass.R;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.editPhoto.EditPhotoUtil;
import com.pictureair.photopass.entity.FrameOrStikerInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.HttpCallback;
import com.pictureair.photopass.util.HttpUtil1;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.MyToast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class EditActivityAdapter extends BaseAdapter {
    private DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true).showImageOnLoading(R.drawable.decoration_bg).build();// 下载图片显示
    private Context mContext;
    private List<String> stickerPathList;
    private int editType = 0;
    private int[] filterText = { R.string.original, R.string.lomo, R.string.earlybird, R.string.natural,
            R.string.hdr, R.string.whitening, R.string.vintage };
    private ArrayList<FrameOrStikerInfo> frameInfos;
    private Handler handler;
    private boolean firstFileFailOrExist = false;
    private boolean secondFileFailOrExist = false;
    private static final int UPDATE_PROGRESS = 101;//更新进度条
    private long firstFileProgress = 0;//文件下载的进度
    private long secondFileProgress = 0;//文件下载进度
    private CustomDialog customDialog;
    private MyToast myToast;
    private PictureAirDbManager pictureAirDbManager;


    private Handler downloadHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_PROGRESS:
                    HolderView holderView = (HolderView) msg.obj;
                    holderView.progressBar.setProgress(msg.arg1);
                    holderView.fileSizeTextView.setText(msg.arg1 + "%");
                    break;

                default:
                    break;
            }
        }

        ;
    };

    //图片
    private Bitmap bitmap;

    public EditActivityAdapter(Context context, Bitmap bitmap, List<String> stickerPathList, int editType, ArrayList<FrameOrStikerInfo> frameInfos, Handler handler) {
        this.mContext = context;
        this.editType = editType;
        this.bitmap = bitmap;
        this.stickerPathList = stickerPathList;
        this.frameInfos = frameInfos;
        this.handler = handler;
        myToast = new MyToast(context);
        pictureAirDbManager = new PictureAirDbManager(context);
    }

    @Override
    public int getCount() {
        if (editType == 1 || editType == 3) {
            return frameInfos.size();
        } else {
            return stickerPathList.size();
        }
    }


    @Override
    public Object getItem(int position) {
        if (editType == 1 || editType == 3) {
            return frameInfos.get(position);
        } else {
            return stickerPathList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HolderView holderView = null;
        // 布局混乱：如果position等于0的时候，重新加载布局。
        if (position == 0) {
            convertView = null;
        }
        if (convertView == null) {
            holderView = new HolderView();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.edit_item, parent, false);
            holderView.editImageview = (ImageView) convertView.findViewById(R.id.editImageview);
            holderView.editText = (TextView) convertView.findViewById(R.id.editTextview);
            holderView.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBar);
            holderView.itemLayout = (LinearLayout) convertView.findViewById(R.id.item);
            holderView.maskImageView = (ImageView) convertView.findViewById(R.id.frame_mask);
            holderView.itemRelativeLayout = (RelativeLayout) convertView.findViewById(R.id.edit_item);
            holderView.fileSizeTextView = (TextView) convertView.findViewById(R.id.file_size);
            convertView.setTag(holderView);
        } else {
            holderView = (HolderView) convertView.getTag();
        }

        holderView.editText.setVisibility(View.GONE);
        holderView.maskImageView.setVisibility(View.GONE);
        holderView.fileSizeTextView.setVisibility(View.INVISIBLE);
        holderView.progressBar.setVisibility(View.INVISIBLE);
        if (editType == 2) {//滤镜
            LayoutParams layoutParams = holderView.itemRelativeLayout.getLayoutParams();
            layoutParams.height = ScreenUtil.dip2px(mContext, 50);
            layoutParams.width = ScreenUtil.dip2px(mContext, 50);
            holderView.itemRelativeLayout.setLayoutParams(layoutParams);
            ImageLoader.getInstance().displayImage("assets://" + stickerPathList.get(position), holderView.editImageview, options);

            holderView.editText.setText(filterText[position]);
            holderView.editText.setVisibility(View.VISIBLE);
        }
        if (editType == 3) {//饰品
            LayoutParams layoutParams = holderView.itemRelativeLayout.getLayoutParams();
            layoutParams.height = ScreenUtil.dip2px(mContext, 60);
            layoutParams.width = ScreenUtil.dip2px(mContext, 60);
            holderView.itemRelativeLayout.setLayoutParams(layoutParams);
            if (frameInfos.get(position).onLine == 1) {//网络图片

                ImageLoader.getInstance().displayImage(Common.PHOTO_URL + frameInfos.get(position).frameOriginalPathPortrait, holderView.editImageview, options);
            } else {//本地assets图片

                ImageLoader.getInstance().displayImage(frameInfos.get(position).frameOriginalPathPortrait, holderView.editImageview, options);
            }
            holderView.editImageview.setBackgroundResource(R.drawable.decoration_bg);

        }

        if (editType == 1) {//边框
//			LayoutParams layoutParams = holderView.itemRelativeLayout.getLayoutParams();
//			layoutParams.height = ScreenUtil.dip2px(mContext, 80);
//			layoutParams.width = ScreenUtil.dip2px(mContext, 60);
//
//			holderView.itemRelativeLayout.setLayoutParams(layoutParams);
//			EditPhotoUtil.setMargins(holderView.itemRelativeLayout , 5, 5, 5, 5);
//			holderView.editImageview.setBackgroundDrawable(new BitmapDrawable(bitmap));
//			holderView.editImageview.setScaleType(ImageView.ScaleType.FIT_XY);
////			System.out.println(position + " ---->" + frameInfos.get(position).frameThumbnailPath160);
//			if (frameInfos.get(position).onLine == 1) {
//				ImageLoader.getInstance().displayImage(Common.PHOTO_URL + frameInfos.get(position).frameThumbnailPath160, holderView.editImageview, options);
//				if (frameInfos.get(position).isDownload == 0) {
//					holderView.maskImageView.setVisibility(View.VISIBLE);
//					holderView.fileSizeTextView.setVisibility(View.VISIBLE);
//					holderView.fileSizeTextView.setText(frameInfos.get(position).fileSize / 1024 / 1024 + "M");
//				}else {
//					holderView.maskImageView.setVisibility(View.GONE);
//					holderView.fileSizeTextView.setVisibility(View.INVISIBLE);
//				}
//			}else {
//				ImageLoader.getInstance().displayImage(frameInfos.get(position).frameThumbnailPath160, holderView.editImageview, options);
//			}
//			holderView.itemRelativeLayout.setOnClickListener(new ItemOnClickListener(holderView, position));
            LayoutParams layoutParams = holderView.itemRelativeLayout.getLayoutParams();
            LayoutParams layoutParam1 = holderView.editImageview.getLayoutParams();
            if (bitmap.getWidth() > bitmap.getHeight()) {
                layoutParams.height = LayoutParams.MATCH_PARENT;
                layoutParams.width = ScreenUtil.dip2px(mContext, 80);

                layoutParam1.height = ScreenUtil.dip2px(mContext, 60);
                layoutParam1.width = ScreenUtil.dip2px(mContext, 80);
            } else {
                layoutParams.height = LayoutParams.MATCH_PARENT;
                layoutParams.width = ScreenUtil.dip2px(mContext, 60);

                layoutParam1.height = ScreenUtil.dip2px(mContext, 80);
                layoutParam1.width = ScreenUtil.dip2px(mContext, 60);
            }

            holderView.itemRelativeLayout.setLayoutParams(layoutParams);
            holderView.editImageview.setLayoutParams(layoutParam1);

            EditPhotoUtil.setMargins(holderView.itemRelativeLayout, 5, 5, 5, 5);
            holderView.editImageview.setBackgroundDrawable(new BitmapDrawable(bitmap));
            holderView.editImageview.setScaleType(ImageView.ScaleType.FIT_XY);
//			System.out.println(position + " ---->" + frameInfos.get(position).frameThumbnailPath160);
            if (frameInfos.get(position).onLine == 1) {
                // 网络边框。 3.0版本
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    ImageLoader.getInstance().displayImage(Common.PHOTO_URL + frameInfos.get(position).frameThumbnailPathH160, holderView.editImageview, options);
                }else{
                    ImageLoader.getInstance().displayImage(Common.PHOTO_URL + frameInfos.get(position).frameThumbnailPathV160, holderView.editImageview, options);
                }

                if (frameInfos.get(position).isDownload == 0) {
					holderView.maskImageView.setVisibility(View.VISIBLE);
					holderView.fileSizeTextView.setVisibility(View.VISIBLE);
					holderView.fileSizeTextView.setText(frameInfos.get(position).fileSize / 1024 / 1024 + "M");
				}else {
					holderView.maskImageView.setVisibility(View.GONE);
					holderView.fileSizeTextView.setVisibility(View.INVISIBLE);
				}
            } else {
                if (bitmap.getWidth() > bitmap.getHeight()) {
                    ImageLoader.getInstance().displayImage(frameInfos.get(position).frameThumbnailPathH160, holderView.editImageview, options);
                } else {
                    ImageLoader.getInstance().displayImage(frameInfos.get(position).frameThumbnailPathV160, holderView.editImageview, options);
                }

            }
            holderView.itemRelativeLayout.setOnClickListener(new ItemOnClickListener(holderView, position));
        }
        return convertView;
    }

    class HolderView {
        ImageView editImageview;//编辑的图片
        TextView editText;//文字
        ProgressBar progressBar;//进度条
        LinearLayout itemLayout;//布局
        ImageView maskImageView;//蒙版
        RelativeLayout itemRelativeLayout;
        TextView fileSizeTextView;//文件大小
    }


    /**
     * item点击监听
     *
     * @author bauer_bao
     */
    private class ItemOnClickListener implements OnClickListener {
        private HolderView holderView;
        private int position;

        public ItemOnClickListener(HolderView holderView, int position) {
            this.holderView = holderView;
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (frameInfos.get(position).onLine == 1 && frameInfos.get(position).isDownload == 0) {//网络图片，并且未下载
				holderView.progressBar.setVisibility(View.VISIBLE);//开始下载
				holderView.fileSizeTextView.setVisibility(View.VISIBLE);
				holderView.progressBar.setProgress(0);
				downloadFrame(position, true, holderView);
				downloadFrame(position, false, holderView);

                if (AppUtil.getNetWorkType(mContext) == AppUtil.NETWORKTYPE_INVALID) {//无网络
                    myToast.setTextAndShow(R.string.no_network, Common.TOAST_SHORT_TIME);

                } else if (AppUtil.getNetWorkType(mContext) == AppUtil.NETWORKTYPE_MOBILE) {//使用流量
                    showDownloadDialog(holderView, position);

                } else {//wifi
                    prepareDownload(holderView, position);
                }

            } else {//本地图片，或者网络图片已经下载
                //开始加载图片
                Message message = handler.obtainMessage();
                message.what = 1111;
                message.arg1 = position;
                handler.sendMessage(message);
            }
        }
    }

    /**
     * 创建下载对话框
     *
     * @param holderView
     * @param position
     */
    private void showDownloadDialog(HolderView holderView, int position) {
        customDialog = new CustomDialog.Builder(mContext)
                .setMessage(mContext.getResources().getString(R.string.dialog_download_message))
                .setNegativeButton(mContext.getResources().getString(R.string.dialog_cancel), new DownloadDialogOnClickListener(holderView, position))
                .setPositiveButton(mContext.getResources().getString(R.string.dialog_ok), new DownloadDialogOnClickListener(holderView, position))
                .setCancelable(false)
                .create();
        customDialog.show();
    }

    /**
     * 对话框点击事件监听
     *
     * @author bauer_bao
     */
    private class DownloadDialogOnClickListener implements android.content.DialogInterface.OnClickListener {
        private HolderView holderView;
        private int position;

        public DownloadDialogOnClickListener(HolderView holderView, int position) {
            this.holderView = holderView;
            this.position = position;
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case DialogInterface.BUTTON_NEGATIVE://取消按钮
                    PictureAirLog.out("negative button");
                    break;

                case DialogInterface.BUTTON_POSITIVE://确定按钮
                    PictureAirLog.out("positive button");
                    prepareDownload(holderView, position);
                    break;

                default:
                    break;
            }
            customDialog.dismiss();
        }

    }

    /**
     * 准备下载
     *
     * @param holderView
     * @param position
     */
    private void prepareDownload(HolderView holderView, int position) {
        holderView.progressBar.setVisibility(View.VISIBLE);//开始下载
        holderView.fileSizeTextView.setVisibility(View.VISIBLE);
        holderView.progressBar.setProgress(0);
        downloadFrame(position, true, holderView);
        downloadFrame(position, false, holderView);
    }

    /**
     * 下载边框
     *
     * @param position
     * @param firstTime
     * @param holderView
     */
    private void downloadFrame(final int position, final boolean firstTime, final HolderView holderView) {
        String downloadNameString = null;
        String url = null;
        if (firstTime) {
            url = frameInfos.get(position).frameOriginalPathLandscape;
            if (url == null || url.equals("")) {//文件不存在
                firstFileFailOrExist = true;
                return;
            }
            downloadNameString = "frame_landscape_" + ScreenUtil.getReallyFileName(url,0);
        } else {
            url = frameInfos.get(position).frameOriginalPathPortrait;
            if (url == null || url.equals("")) {//文件不存在
                secondFileFailOrExist = true;
                return;
            }
            downloadNameString = "frame_portrait_" + ScreenUtil.getReallyFileName(url,0);
        }

        File file = new File(mContext.getFilesDir(), "frames");
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

//        HttpUtil1.asyncGet(Common.PHOTO_URL + url, new HttpCallback() {
//            @Override
//            public void onSuccess(byte[] binaryData) {
//                super.onSuccess(binaryData);
//                BufferedOutputStream stream = null;
//                try {
//                    FileOutputStream fsStream = new FileOutputStream(downloadFile.toString());
//                    stream = new BufferedOutputStream(fsStream);
//                    stream.write(binaryData);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                } finally {
//                    try {
//                        if (stream != null) {
//                            stream.flush();
//                            stream.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(int status) {
//                super.onFailure(status);
//                downloadFile.delete();
//                if (firstTime) {
//                    firstFileFailOrExist = true;
//                } else {
//                    secondFileFailOrExist = true;
//                }
//            }
//
//            @Override
//            public void onProgress(long bytesWritten, long totalSize) {
//                super.onProgress(bytesWritten, totalSize);
//                if (firstTime) {
//                    firstFileProgress = bytesWritten * 50 / totalSize;
//                } else {
//                    secondFileProgress = bytesWritten * 50 / totalSize;
//                }
////				System.out.println("progress:"+ firstFileProgress + "-" + secondFileProgress);
//                Message message = downloadHandler.obtainMessage();
//                message.what = UPDATE_PROGRESS;
//                message.obj = holderView;
//                if (firstFileFailOrExist || secondFileFailOrExist) {//有文件存在，或者下载失败，需要在进度上加50
//                    message.arg1 = (int) (50 + bytesWritten * 50 / totalSize);
//                } else {//都可以正常下载，下载进度取两者平均值
//                    message.arg1 = (int) (firstFileProgress + secondFileProgress);
//                }
//                downloadHandler.sendMessage(message);
//                if (firstFileProgress + secondFileProgress == 100) {//下载完成的处理
//                    holderView.progressBar.setVisibility(View.GONE);
//                    holderView.maskImageView.setVisibility(View.GONE);
//                    holderView.fileSizeTextView.setVisibility(View.GONE);
//                    frameInfos.get(position).isDownload = 1;
//                    firstFileFailOrExist = false;
//                    secondFileFailOrExist = false;
//                    firstFileProgress = 0;
//                    secondFileProgress = 0;
//                    pictureAirDbManager.updateFrameAndStickerDownloadStatus(frameInfos.get(position).frameName, 1);
//                }
//            }
//        });

        // two
        HttpUtil1.asyncDownloadBinaryData(Common.PHOTO_URL + url,new HttpCallback(){
            @Override
            public void onSuccess(byte[] binaryData) {
                super.onSuccess(binaryData);
                BufferedOutputStream stream = null;
                try {
                    FileOutputStream fsStream = new FileOutputStream(downloadFile.toString());
                    stream = new BufferedOutputStream(fsStream);
                    stream.write(binaryData);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (stream != null) {
                            stream.flush();
                            stream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(int status) {
                super.onFailure(status);
                downloadFile.delete();
                if (firstTime) {
                    firstFileFailOrExist = true;
                } else {
                    secondFileFailOrExist = true;
                }
            }

            @Override
            public void onProgress(long bytesWritten, long totalSize) {
                super.onProgress(bytesWritten, totalSize);
                if (firstTime) {
                    firstFileProgress = bytesWritten * 50 / totalSize;
                } else {
                    secondFileProgress = bytesWritten * 50 / totalSize;
                }
//				System.out.println("progress:"+ firstFileProgress + "-" + secondFileProgress);
                Message message = downloadHandler.obtainMessage();
                message.what = UPDATE_PROGRESS;
                message.obj = holderView;
                if (firstFileFailOrExist || secondFileFailOrExist) {//有文件存在，或者下载失败，需要在进度上加50
                    message.arg1 = (int) (50 + bytesWritten * 50 / totalSize);
                } else {//都可以正常下载，下载进度取两者平均值
                    message.arg1 = (int) (firstFileProgress + secondFileProgress);
                }
                downloadHandler.sendMessage(message);
                if (firstFileProgress + secondFileProgress == 100) {//下载完成的处理
                    holderView.progressBar.setVisibility(View.GONE);
                    holderView.maskImageView.setVisibility(View.GONE);
                    holderView.fileSizeTextView.setVisibility(View.GONE);
                    frameInfos.get(position).isDownload = 1;
                    firstFileFailOrExist = false;
                    secondFileFailOrExist = false;
                    firstFileProgress = 0;
                    secondFileProgress = 0;
                    pictureAirDbManager.updateFrameAndStickerDownloadStatus(frameInfos.get(position).frameName, 1);
                }
            }
        });
    }

}
