package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageAware;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.EditStoryAlbumActivity;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.PWToast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * pp数据的适配器
 */
public class ListOfPPAdapter extends BaseAdapter implements OnClickListener {
    private ArrayList<PPinfo> arrayList;
    private Context mContext;
    private int screenWidth = 0;// 屏幕宽度
    private ViewHolder holder;
    private doDeletePhotoListener deleteListner;
    private LinearLayout.LayoutParams params;
    private RelativeLayout.LayoutParams params2;
    private PWToast myToast;
    private SimpleDateFormat sdf;

    private boolean isSelete;
    private Handler mHandler;
    private OnItemChildClickListener childClickListener;
    private int useNumber = 0;//已经使用的个数
    private int choice = 0;//选中的个数
    private HashMap<Integer, Boolean> map;//统计被勾选的子项
    private PPPinfo pppInfo;
    private boolean isDeletePP;

    private SharedPreferences sharedPreferences;

    private String userPP;

    public ListOfPPAdapter(ArrayList<PPinfo> list, Context mContext, final doDeletePhotoListener deleteListner,
                           boolean isSelete, boolean isDeletePP, Handler mHandler, PPPinfo pppInfo) {
        this.arrayList = list;
        this.mContext = mContext;
        this.deleteListner = deleteListner;
        this.isSelete = isSelete;
        this.mHandler = mHandler;
        this.isDeletePP = isDeletePP;
        myToast = new PWToast(mContext);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        screenWidth = ScreenUtil.getScreenWidth(mContext);// 获取屏幕宽度
        params = new LinearLayout.LayoutParams(
                (screenWidth - 24) / 6, (screenWidth - 24) / 6);
        params.setMargins(2, 2, 2, 2);

        params2 = new RelativeLayout.LayoutParams(
                (screenWidth - 24) / 6, (screenWidth - 24) / 6);

        this.pppInfo = pppInfo;
        map = new HashMap<>();
        if (isSelete) {
            useNumber = pppInfo.bindInfo.size();
        }

        sharedPreferences = mContext.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME, Context.MODE_PRIVATE);
        userPP = sharedPreferences.getString(Common.USERINFO_USER_PP, "");

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
            holder.image1 = (ImageView) convertView.findViewById(R.id.pp_img1);
            holder.image2 = (ImageView) convertView.findViewById(R.id.pp_img2);
            holder.image3 = (ImageView) convertView.findViewById(R.id.pp_img3);
            holder.image4 = (ImageView) convertView.findViewById(R.id.pp_img4);
            holder.image5 = (ImageView) convertView.findViewById(R.id.pp_img5);
            holder.image6 = (ImageView) convertView.findViewById(R.id.pp_img6);
            holder.image7 = (ImageView) convertView.findViewById(R.id.pp_img7);
            holder.image8 = (ImageView) convertView.findViewById(R.id.pp_img8);
            holder.image9 = (ImageView) convertView.findViewById(R.id.pp_img9);
            holder.image10 = (ImageView) convertView.findViewById(R.id.pp_img10);
            holder.image11 = (ImageView) convertView.findViewById(R.id.pp_img11);
            holder.image12 = (ImageView) convertView.findViewById(R.id.pp_img12);

            holder.ppImageLayout1 = (LinearLayout) convertView.findViewById(R.id.pp_image_layout1);
            holder.ppImageLayout2 = (LinearLayout) convertView.findViewById(R.id.pp_image_layout2);
            holder.showCconutLayout = (RelativeLayout) convertView.findViewById(R.id.show_conut_layout);
            holder.photoCount = (TextView) convertView.findViewById(R.id.photo_count);
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


        // 初始化数据
        // 初始化pp码
        PPinfo ppInfo1 = new PPinfo();
        ppInfo1 = arrayList.get(position);
        ArrayList<String> urlList = new ArrayList<String>();
        holder.ppCode.setText(ppInfo1.getPpCode());
        if (ppInfo1.getUrlList() == null || ppInfo1.getUrlList().size() <= 0) {
            ppInfo1.setUrlList(new ArrayList<String>());
        }

        // 图片显示
        int photoCount = ppInfo1.getUrlList().size();
        urlList.removeAll(urlList);
        urlList.addAll(ppInfo1.getUrlList());
        if (photoCount == 0) {
            holder.conerImageView.setImageResource(R.drawable.my_pp_miqi_no_photo);
        } else {
            holder.conerImageView.setImageResource(R.drawable.my_pp_miqi);
        }
        if (photoCount <= 6) {
            holder.ppImageLayout2.setVisibility(View.GONE);
            holder.photoCount.setVisibility(View.GONE);
            for (int i = 0; i < 6; i++) {
                int num = 6 - photoCount;
                if (num > 0 && i > photoCount - 1) {
                    // 显示默认图片
                    String defaultUrl = "";
                    urlList.add(i, defaultUrl);
                }
            }
        } else if (photoCount > 6) {

            holder.ppImageLayout2.setVisibility(View.VISIBLE);
            if (photoCount <= 12) {
                holder.photoCount.setVisibility(View.GONE);
                for (int i = 0; i < 12; i++) {
                    int num = 12 - photoCount;
                    if (num > 0 && i > photoCount - 1) {
                        // 显示默认图片
                        String defaultUrl = "";
                        urlList.add(i, defaultUrl);
                    }
                }
            } else {
                holder.photoCount.setVisibility(View.VISIBLE);
                holder.photoCount.setText(urlList.size() + "");
            }

            holder.image7.setLayoutParams(params);
            holder.image8.setLayoutParams(params);
            holder.image9.setLayoutParams(params);
            holder.image10.setLayoutParams(params);
            holder.image11.setLayoutParams(params);
            holder.showCconutLayout.setLayoutParams(params);
            holder.image12.setLayoutParams(params2);

            ImageAware imageAware7 = new ImageViewAware(holder.image7, false);
            ImageAware imageAware8 = new ImageViewAware(holder.image8, false);
            ImageAware imageAware9 = new ImageViewAware(holder.image9, false);
            ImageAware imageAware10 = new ImageViewAware(holder.image10, false);
            ImageAware imageAware11 = new ImageViewAware(holder.image11, false);
            ImageAware imageAware12 = new ImageViewAware(holder.image12, false);

            String url7, url8, url9, url10, url11, url12;

            if (urlList.get(6) == null || urlList.get(6).equals("")) {
                url7 = "drawable://" + R.drawable.default_pp;
            } else {
                url7 = urlList.get(6);
            }

            if (urlList.get(7) == null || urlList.get(7).equals("")) {
                url8 = "drawable://" + R.drawable.default_pp;
            } else {
                url8 = urlList.get(7);
            }

            if (urlList.get(8) == null || urlList.get(8).equals("")) {
                url9 = "drawable://" + R.drawable.default_pp;
            } else {
                url9 = urlList.get(8);
            }

            if (urlList.get(9) == null || urlList.get(9).equals("")) {
                url10 = "drawable://" + R.drawable.default_pp;
            } else {
                url10 = urlList.get(9);
            }

            if (urlList.get(10) == null || urlList.get(10).equals("")) {
                url11 = "drawable://" + R.drawable.default_pp;
            } else {
                url11 = urlList.get(10);
            }

            if (urlList.get(11) == null || urlList.get(11).equals("")) {
                url12 = "drawable://" + R.drawable.default_pp;
            } else {
                url12 = urlList.get(11);
            }

            if (holder.image7.getTag() == null || !holder.image7.getTag().equals(url7)) {
                ImageLoader.getInstance().displayImage(url7, imageAware7);
                holder.image7.setTag(url7);
            }

            if (holder.image8.getTag() == null || !holder.image8.getTag().equals(url8)) {
                ImageLoader.getInstance().displayImage(url8, imageAware8);
                holder.image8.setTag(url8);
            }

            if (holder.image9.getTag() == null || !holder.image9.getTag().equals(url9)) {
                ImageLoader.getInstance().displayImage(url9, imageAware9);
                holder.image9.setTag(url9);
            }

            if (holder.image10.getTag() == null || !holder.image10.getTag().equals(url10)) {
                ImageLoader.getInstance().displayImage(url10, imageAware10);
                holder.image10.setTag(url10);
            }

            if (holder.image11.getTag() == null || !holder.image11.getTag().equals(url11)) {
                ImageLoader.getInstance().displayImage(url11, imageAware11);
                holder.image11.setTag(url11);
            }

            if (holder.image12.getTag() == null || !holder.image12.getTag().equals(url12)) {
                holder.image12.setTag(url12);
                ImageLoader.getInstance().displayImage(url12, imageAware12);
            }
        }

        holder.image1.setLayoutParams(params);
        holder.image2.setLayoutParams(params);
        holder.image3.setLayoutParams(params);
        holder.image4.setLayoutParams(params);
        holder.image5.setLayoutParams(params);
        holder.image6.setLayoutParams(params);

        ImageAware imageAware1 = new ImageViewAware(holder.image1, false);
        ImageAware imageAware2 = new ImageViewAware(holder.image2, false);
        ImageAware imageAware3 = new ImageViewAware(holder.image3, false);
        ImageAware imageAware4 = new ImageViewAware(holder.image4, false);
        ImageAware imageAware5 = new ImageViewAware(holder.image5, false);
        ImageAware imageAware6 = new ImageViewAware(holder.image6, false);

        String url1, url2, url3, url4, url5, url6;

        if (urlList.get(0) == null || urlList.get(0).equals("")) {
            url1 = "drawable://" + R.drawable.default_pp;
        } else {
            url1 = urlList.get(0);
        }

        if (urlList.get(1) == null || urlList.get(1).equals("")) {
            url2 = "drawable://" + R.drawable.default_pp;
        } else {
            url2 = urlList.get(1);
        }

        if (urlList.get(2) == null || urlList.get(2).equals("")) {
            url3 = "drawable://" + R.drawable.default_pp;
        } else {
            url3 = urlList.get(2);
        }

        if (urlList.get(3) == null || urlList.get(3).equals("")) {
            url4 = "drawable://" + R.drawable.default_pp;
        } else {
            url4 = urlList.get(3);
        }

        if (urlList.get(4) == null || urlList.get(4).equals("")) {
            url5 = "drawable://" + R.drawable.default_pp;
        } else {
            url5 = urlList.get(4);
        }

        if (urlList.get(5) == null || urlList.get(5).equals("")) {
            url6 = "drawable://" + R.drawable.default_pp;
        } else {
            url6 = urlList.get(5);
        }

        if (holder.image1.getTag() == null || !holder.image1.getTag().equals(url1)) {
            ImageLoader.getInstance().displayImage(url1, imageAware1);
            holder.image1.setTag(url1);
        }

        if (holder.image2.getTag() == null || !holder.image2.getTag().equals(url2)) {
            ImageLoader.getInstance().displayImage(url2, imageAware2);
            holder.image2.setTag(url2);
        }

        if (holder.image3.getTag() == null || !holder.image3.getTag().equals(url3)) {
            ImageLoader.getInstance().displayImage(url3, imageAware3);
            holder.image3.setTag(url3);
        }

        if (holder.image4.getTag() == null || !holder.image4.getTag().equals(url4)) {
            ImageLoader.getInstance().displayImage(url4, imageAware4);
            holder.image4.setTag(url4);
        }

        if (holder.image5.getTag() == null || !holder.image5.getTag().equals(url5)) {
            ImageLoader.getInstance().displayImage(url5, imageAware5);
            holder.image5.setTag(url5);
        }

        if (holder.image6.getTag() == null || !holder.image6.getTag().equals(url6)) {
            holder.image6.setTag(url6);
            ImageLoader.getInstance().displayImage(url6, imageAware6);
        }

        return convertView;
    }

    private class ViewHolder {
        TextView ppCode;// pp的时间，pp码，pp对应的照片的数量,
        ImageView deleteMyPP;//删除PP
        ImageView image1, image2, image3, image4, image5, image6, image7,
                image8, image9, image10, image11, image12, img_no_check;
        LinearLayout ppImageLayout1, ppImageLayout2;
        RelativeLayout showCconutLayout;
        TextView photoCount;
        ImageView conerImageView;
        LinearLayout itemLayout;
        TextView tvShootDate;
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


    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.pp_code:

                break;
            case R.id.delete_my_pp:

                break;
            default:
                break;
        }
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
                if (isDeletePP) {
                    return;
                }
                PictureAirLog.out("preview photo");
                //选择PP 点击单张直接进入改PP的相册页面
                if (checkUrl(position)) {
                    if (arrayList.get(position).getUrlList().size() > 0) {
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
