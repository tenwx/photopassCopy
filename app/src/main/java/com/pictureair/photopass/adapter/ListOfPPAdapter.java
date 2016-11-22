package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.EditStoryAlbumActivity;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.PWPhotoPassPhotoView;
import com.pictureair.photopass.widget.PWToast;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * pp数据的适配器
 */
public class ListOfPPAdapter extends BaseAdapter {
    private ArrayList<PPinfo> arrayList;
    private Context mContext;
    private int screenWidth = 0;// 屏幕宽度
    private ViewHolder holder;
    private doDeletePhotoListener deleteListner;
    private LinearLayout.LayoutParams params;
    private PWToast myToast;

    private boolean isSelete;
    private boolean showCount;//显示数字
    private Handler mHandler;
    private OnItemChildClickListener childClickListener;
    private int useNumber = 0;//已经使用的个数
    private int choice = 0;//选中的个数
    private HashMap<Integer, Boolean> map;//统计被勾选的子项
    private PPPinfo pppInfo;
    private boolean isDeletePP;

    private String userPP;

    public ListOfPPAdapter(ArrayList<PPinfo> list, Context mContext, final doDeletePhotoListener deleteListner,
                           boolean isSelete, boolean isDeletePP, boolean showCount, Handler mHandler, PPPinfo pppInfo) {
        this.arrayList = list;
        this.mContext = mContext;
        this.deleteListner = deleteListner;
        this.isSelete = isSelete;
        this.showCount = showCount;
        this.mHandler = mHandler;
        this.isDeletePP = isDeletePP;
        myToast = new PWToast(mContext);
        screenWidth = ScreenUtil.getScreenWidth(mContext);// 获取屏幕宽度
        params = new LinearLayout.LayoutParams((screenWidth - 24) / 6, (screenWidth - 24) / 6);
        params.setMargins(2, 2, 2, 2);

        this.pppInfo = pppInfo;
        map = new HashMap<>();
        if (isSelete) {
            useNumber = pppInfo.bindInfo.size();
        }

        userPP = SPUtils.getString(mContext, Common.SHARED_PREFERENCE_USERINFO_NAME, Common.USERINFO_USER_PP, "");
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // 初始化view
        holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.my_pp_list, null);
            holder.ppCode = (TextView) convertView.findViewById(R.id.pp_code);
            holder.deleteMyPP = (ImageView) convertView.findViewById(R.id.delete_my_pp);
            holder.image1 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img1);
            holder.image2 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img2);
            holder.image3 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img3);
            holder.image4 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img4);
            holder.image5 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img5);
            holder.image6 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img6);
            holder.image7 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img7);
            holder.image8 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img8);
            holder.image9 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img9);
            holder.image10 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img10);
            holder.image11 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img11);
            holder.image12 = (PWPhotoPassPhotoView) convertView.findViewById(R.id.pp_img12);

            holder.ppImageLayout1 = (LinearLayout) convertView.findViewById(R.id.pp_image_layout1);
            holder.ppImageLayout2 = (LinearLayout) convertView.findViewById(R.id.pp_image_layout2);
            holder.dividerView = convertView.findViewById(R.id.divider_line);
            holder.conerImageView = (ImageView) convertView.findViewById(R.id.my_pp_miqi);
            holder.tvShootDate = (TextView) convertView.findViewById(R.id.tv_shoot_date);
            holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.pp_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        childClickListener = new OnItemChildClickListener(position, isSelete);
        holder.itemLayout.setOnClickListener(childClickListener);

        if (isSelete) {
            holder.deleteMyPP.setVisibility(View.GONE);
            holder.img_no_check = (ImageView) convertView.findViewById(R.id.img);
            holder.img_no_check.setVisibility(View.VISIBLE);
            holder.tvShootDate.setText(arrayList.get(position).getShootDate());
            holder.tvShootDate.setVisibility(View.VISIBLE);
            //初始化选中与否
            if (arrayList.get(position).isSelected == 0) {//未选中
                holder.img_no_check.setImageResource(R.drawable.nosele);
            } else if (arrayList.get(position).isSelected == 1) {//选中
                holder.img_no_check.setImageResource(R.drawable.sele);
            } else {//不可选
                holder.img_no_check.setImageResource(R.drawable.del1);
            }
        } else {
            //判断是否显示删除按钮
            if (isDeletePP && !arrayList.get(position).getPpCode().equals(userPP)) {
                holder.deleteMyPP.setVisibility(View.VISIBLE);
            } else {
                holder.deleteMyPP.setVisibility(View.GONE);
            }
            holder.deleteMyPP.setOnClickListener(new PhotoPassManagerOnClickListener(position, true));//删除图片
        }

        if (arrayList == null || arrayList.size() <= 0) {
            return convertView;
        }

        // 初始化pp码
        PPinfo ppInfo1 = arrayList.get(position);
        if (showCount) {
            holder.ppCode.setText(ppInfo1.getPpCode() + " (" + ppInfo1.getPhotoCount() + ")");

        } else {
            holder.ppCode.setText(ppInfo1.getPpCode());
        }

        int photoCount = ppInfo1.getVisiblePhotoCount();
        // 图片显示
        if (photoCount == 0) {
            holder.conerImageView.setImageResource(R.drawable.my_pp_miqi_no_photo);
        } else {
            holder.conerImageView.setImageResource(R.drawable.my_pp_miqi);
        }
        if (photoCount <= 6) {
            holder.ppImageLayout2.setVisibility(View.GONE);
            holder.dividerView.setVisibility(View.VISIBLE);

        } else if (photoCount > 6) {
            holder.dividerView.setVisibility(View.GONE);
            holder.ppImageLayout2.setVisibility(View.VISIBLE);

            holder.image7.setLayoutParams(params);
            holder.image8.setLayoutParams(params);
            holder.image9.setLayoutParams(params);
            holder.image10.setLayoutParams(params);
            holder.image11.setLayoutParams(params);
            holder.image12.setLayoutParams(params);

            holder.image7.initData(ppInfo1.getUrlList().get(6), 6, photoCount);
            holder.image8.initData(ppInfo1.getUrlList().get(7), 7, photoCount);
            holder.image9.initData(ppInfo1.getUrlList().get(8), 8, photoCount);
            holder.image10.initData(ppInfo1.getUrlList().get(9), 9, photoCount);
            holder.image11.initData(ppInfo1.getUrlList().get(10), 10, photoCount);
            holder.image12.initData(ppInfo1.getUrlList().get(11), 11, photoCount);
        }

        holder.image1.setLayoutParams(params);
        holder.image2.setLayoutParams(params);
        holder.image3.setLayoutParams(params);
        holder.image4.setLayoutParams(params);
        holder.image5.setLayoutParams(params);
        holder.image6.setLayoutParams(params);

        holder.image1.initData(ppInfo1.getUrlList().get(0), 0, photoCount);
        holder.image2.initData(ppInfo1.getUrlList().get(1), 1, photoCount);
        holder.image3.initData(ppInfo1.getUrlList().get(2), 2, photoCount);
        holder.image4.initData(ppInfo1.getUrlList().get(3), 3, photoCount);
        holder.image5.initData(ppInfo1.getUrlList().get(4), 4, photoCount);
        holder.image6.initData(ppInfo1.getUrlList().get(5), 5, photoCount);
        return convertView;
    }

    private class ViewHolder {
        TextView ppCode;// pp的时间，pp码，pp对应的照片的数量,
        ImageView deleteMyPP, img_no_check;//删除PP，选中按钮
        PWPhotoPassPhotoView image1, image2, image3, image4, image5, image6, image7,
                image8, image9, image10, image11, image12;
        LinearLayout ppImageLayout1, ppImageLayout2;
        ImageView conerImageView;
        LinearLayout itemLayout;
        TextView tvShootDate;
        View dividerView;
    }

    /**
     * 检测是否包含图片
     *
     * @param position
     * @return
     */
    public boolean checkUrl(int position) {
        if (arrayList == null || arrayList.size() <= 0 || arrayList.get(position).getUrlList() == null || arrayList.get(position).getUrlList().size() <= 0) {
            return false;
        }
        if (arrayList.get(position).getUrlList() == null || arrayList.get(position).getUrlList().size() < 1 || arrayList.get(position).getUrlList().get(0).equals("")) {
            return false;
        }
        return true;
    }

    /**
     * 刷新界面
     *
     * @param ppInfo1s 数据
     * @param isDeletePP
     */
    public void refresh(ArrayList<PPinfo> ppInfo1s, boolean isDeletePP) {
        this.arrayList = ppInfo1s;
        this.isDeletePP = isDeletePP;
        this.notifyDataSetChanged();
    }

    //删除图片监听
    public interface doDeletePhotoListener {
        void deletePhoto(int position);
    }

    class PhotoPassManagerOnClickListener implements OnClickListener {
        private int position;
        private boolean delete;

        public PhotoPassManagerOnClickListener(int position, boolean delete) {
            this.position = position;
            this.delete = delete;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            PictureAirLog.out("preivew photo on click");
            if (delete) {
                PictureAirLog.out("delete pp");
                //删除PP
                deleteListner.deletePhoto(position);
            }
        }

    }


    //点击一个item的事件
    private class OnItemChildClickListener implements OnClickListener {
        private int position;
        private boolean isSelect;

        public OnItemChildClickListener(int position, boolean isSelect) {
            this.position = position;
            this.isSelect = isSelect;
        }

        @Override
        public void onClick(View v) {
            if (isSelect) {
                if (arrayList.get(position) instanceof PPinfo) {
                    PPinfo ppInfo = (PPinfo) arrayList.get(position);
                    switch (v.getId()) {
                        case R.id.img:
                        case R.id.pp_item:
                            PictureAirLog.out("adapter---size=" + map.size());
                            if (null != mHandler) {
                                Message msg = mHandler.obtainMessage();
                                if (ppInfo.isSelected == 0) {
                                    if (useNumber >= pppInfo.capacity) {
                                        myToast.setTextAndShow(R.string.outofrange, Common.TOAST_SHORT_TIME);
                                        break;
                                    } else {
                                        ++choice;
                                        for (int j = 0; j < arrayList.size(); j++) {
                                        }
                                        ppInfo.isSelected = 1;
                                        ++useNumber;
                                        map.put(position, ppInfo.isSelected == 1);
                                    }
                                } else if (ppInfo.isSelected == 2) {

                                } else {
                                    --choice;
                                    if (choice == 0) {
                                        for (int j = 0; j < arrayList.size(); j++) {
                                            PPinfo pp = (PPinfo) arrayList.get(j);
                                            if (pp.isSelected == 2) {
                                                pp.isSelected = 0;
                                            }
                                        }
                                    }
                                    ppInfo.isSelected = 0;
                                    --useNumber;
                                    map.remove(position);
                                }
                                notifyDataSetChanged();
                                msg.arg1 = useNumber;
                                msg.what = 2;
                                mHandler.sendMessage(msg);
                            }
                            break;

                        default:
                            break;
                    }
                }
            } else {
                //选择PP 点击单张直接进入改PP的相册页面
                if (checkUrl(position)) {
                    if (arrayList.get(position).getVisiblePhotoCount() > 0) {
                        //进入相册
                        Intent i = new Intent(mContext, EditStoryAlbumActivity.class);
                        i.putExtra("ppCode", arrayList.get(position).getPpCode());
                        mContext.startActivity(i);
                    }
                }
            }
        }
    }

    public HashMap<Integer, Boolean> getMap() {
        return map;
    }

    public void setMap(HashMap<Integer, Boolean> map) {
        this.map = map;
    }


}
