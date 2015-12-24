package com.pictureair.photopass.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.EditStoryAlbumActivity;
import com.pictureair.photopass.activity.MyPPActivity;
import com.pictureair.photopass.entity.DiscoverLocationItemInfo;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.PhotoItemInfo;
import com.pictureair.photopass.util.ACache;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.JsonUtil;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.util.UniversalImageLoadTool;
import com.pictureair.photopass.widget.MyToast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

/**
 * pp数据的适配器
 */
public class ListOfPPAdapter extends BaseAdapter implements OnClickListener {
    private ArrayList<PPinfo> arrayList;
    private Context mContext;
    private int screenWidth = 0;// 屏幕宽度
    private ViewHolder holder;
    private doShowPhotoListener listener;
    private doDeletePhotoListener deleteListner;
    private LinearLayout.LayoutParams params;
    private RelativeLayout.LayoutParams params2;
    private MyToast myToast;
    private SimpleDateFormat sdf;

    private boolean isSelete;
    private Handler mHandler;
    private OnItemChildClickListener childClickListener;
    private int useNumber = 0;//已经使用的个数
    private int choice = 0;//选中的个数
    private HashMap<Integer, Boolean> map;//统计被勾选的子项
    private PPPinfo pppInfo;

    public ListOfPPAdapter(ArrayList<PPinfo> list, Context mContext, final doShowPhotoListener listener, final doDeletePhotoListener deleteListner, boolean isSelete, Handler mHandler, PPPinfo pppInfo) {
        this.arrayList = list;
        this.mContext = mContext;
        this.listener = listener;
        this.deleteListner = deleteListner;
        this.isSelete = isSelete;
        this.mHandler = mHandler;
        myToast = new MyToast(mContext);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        screenWidth = ScreenUtil.getScreenWidth(mContext);// 获取屏幕宽度
        params = new LinearLayout.LayoutParams(
                (screenWidth - 24) / 6, (screenWidth - 24) / 6);
        params.setMargins(2, 2, 2, 2);

        params2 = new RelativeLayout.LayoutParams(
                (screenWidth - 24) / 6, (screenWidth - 24) / 6);

        this.pppInfo = pppInfo;
        map = new HashMap<Integer, Boolean>();
        if (isSelete) {
            useNumber = pppInfo.bindInfo.size();
        }


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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        if (isSelete) {
            holder.deleteMyPP.setVisibility(View.GONE);
            childClickListener = new OnItemChildClickListener(position);
            holder.img_no_check = (ImageView) convertView.findViewById(R.id.img);
            holder.itemLayout = (LinearLayout) convertView.findViewById(R.id.pp_item);
            //初始化选中与否
            if (arrayList.get(position).isSelected == 0) {//未选中
                holder.img_no_check.setImageResource(R.drawable.nosele);
            } else if (arrayList.get(position).isSelected == 1) {//选中
                holder.img_no_check.setImageResource(R.drawable.sele);
            } else {//不可选
                holder.img_no_check.setImageResource(R.drawable.del1);
            }
            holder.img_no_check.setOnClickListener(childClickListener);
            holder.itemLayout.setOnClickListener(childClickListener);
        } else {
            //判断是否显示删除按钮
            if (MyPPActivity.isDeletePhoto) {
                holder.deleteMyPP.setVisibility(View.VISIBLE);
            } else {
                holder.deleteMyPP.setVisibility(View.GONE);
            }


            holder.deleteMyPP.setOnClickListener(new PhotoPassManagerOnClickListener(position, 0, true));//删除图片
            holder.image1.setOnClickListener(new PhotoPassManagerOnClickListener(position, 0, false));
            holder.image2.setOnClickListener(new PhotoPassManagerOnClickListener(position, 1, false));
            holder.image3.setOnClickListener(new PhotoPassManagerOnClickListener(position, 2, false));
            holder.image4.setOnClickListener(new PhotoPassManagerOnClickListener(position, 3, false));
            holder.image5.setOnClickListener(new PhotoPassManagerOnClickListener(position, 4, false));
            holder.image6.setOnClickListener(new PhotoPassManagerOnClickListener(position, 5, false));
            holder.image7.setOnClickListener(new PhotoPassManagerOnClickListener(position, 6, false));
            holder.image8.setOnClickListener(new PhotoPassManagerOnClickListener(position, 7, false));
            holder.image9.setOnClickListener(new PhotoPassManagerOnClickListener(position, 8, false));
            holder.image10.setOnClickListener(new PhotoPassManagerOnClickListener(position, 9, false));
            holder.image11.setOnClickListener(new PhotoPassManagerOnClickListener(position, 10, false));
            holder.showCconutLayout.setOnClickListener(new PhotoPassManagerOnClickListener(position, 11, false));
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
//		Log.v("===================================开始=====================================",
//				"");
//		Log.v("===========", "PP卡: PPCode:" + ppInfo1.getPpCode());
//		Log.v("===========", "图片显示 photoCount:" + photoCount);
        urlList.removeAll(urlList);
        urlList.addAll(ppInfo1.getUrlList());
//		Log.v("============", "urlList.size()" + urlList.size());
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

            if (urlList.get(6) == null || urlList.get(6).equals("")) {
                holder.image7.setImageResource(R.drawable.default_pp);
                holder.image7.setTag("null");
            } else {
                UniversalImageLoadTool.loadImage(urlList.get(6), holder.image7);
            }
            if (urlList.get(7) == null || urlList.get(7).equals("")) {
                holder.image8.setImageResource(R.drawable.default_pp);
                holder.image8.setTag("null");
            } else {
                UniversalImageLoadTool.loadImage(urlList.get(7), holder.image8);
            }
            if (urlList.get(8) == null || urlList.get(8).equals("")) {
                holder.image9.setImageResource(R.drawable.default_pp);
                holder.image9.setTag("null");
            } else {
                UniversalImageLoadTool.loadImage(urlList.get(8), holder.image9);
            }
            if (urlList.get(9) == null || urlList.get(9).equals("")) {
                holder.image10.setImageResource(R.drawable.default_pp);
                holder.image10.setTag("null");
            } else {
                UniversalImageLoadTool.loadImage(urlList.get(9), holder.image10);
            }
            if (urlList.get(10) == null || urlList.get(10).equals("")) {
                holder.image11.setImageResource(R.drawable.default_pp);
                holder.image11.setTag("null");
            } else {
                UniversalImageLoadTool.loadImage(urlList.get(10), holder.image11);
            }
            if (urlList.get(11) == null || urlList.get(11).equals("")) {
                holder.image12.setImageResource(R.drawable.default_pp);
                holder.image12.setTag("null");
            } else {
                UniversalImageLoadTool.loadImage(urlList.get(11), holder.image12);
            }

        }

        holder.image1.setLayoutParams(params);
        holder.image2.setLayoutParams(params);
        holder.image3.setLayoutParams(params);
        holder.image4.setLayoutParams(params);
        holder.image5.setLayoutParams(params);
        holder.image6.setLayoutParams(params);

        if (urlList.get(0) == null || urlList.get(0).equals("")) {
            holder.image1.setImageResource(R.drawable.default_pp);
            holder.image1.setTag("null");
        } else {
            UniversalImageLoadTool.loadImage(urlList.get(0), holder.image1);
        }

        if (urlList.get(1) == null || urlList.get(1).equals("")) {
            holder.image2.setImageResource(R.drawable.default_pp);
            holder.image2.setTag("null");
        } else {
            UniversalImageLoadTool.loadImage(urlList.get(1), holder.image2);
        }

        if (urlList.get(2) == null || urlList.get(2).equals("")) {
            holder.image3.setImageResource(R.drawable.default_pp);
            holder.image3.setTag("null");
        } else {
            UniversalImageLoadTool.loadImage(urlList.get(2), holder.image3);
        }

        if (urlList.get(3) == null || urlList.get(3).equals("")) {
            holder.image4.setImageResource(R.drawable.default_pp);
            holder.image4.setTag("null");
        } else {
            UniversalImageLoadTool.loadImage(urlList.get(3), holder.image4);
        }

        if (urlList.get(4) == null || urlList.get(4).equals("")) {
            holder.image5.setImageResource(R.drawable.default_pp);
            holder.image5.setTag("null");
        } else {
            UniversalImageLoadTool.loadImage(urlList.get(4), holder.image5);
        }

        if (urlList.get(5) == null || urlList.get(5).equals("")) {
            holder.image6.setImageResource(R.drawable.default_pp);
            holder.image6.setTag("null");
        } else {
            UniversalImageLoadTool.loadImage(urlList.get(5), holder.image6);
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
    }

    /**
     * 检测是否包含图片
     *
     * @param position
     * @param n
     * @return
     */
    public boolean checkUrl(int position, int n) {
        if (arrayList == null || arrayList.size() <= 0 || arrayList.get(position).getUrlList() == null || arrayList.get(position).getUrlList().size() <= 0) {
            return false;
        }
        if (arrayList.get(position).getUrlList() == null || arrayList.get(position).getUrlList().size() < n + 1 || arrayList.get(position).getUrlList().get(n).equals("")) {
            return false;
        }
        return true;
    }

    //获取地点信息
    private ArrayList<DiscoverLocationItemInfo> createLocationList() {
        ArrayList<DiscoverLocationItemInfo> resultArrayList = new ArrayList<DiscoverLocationItemInfo>();
        if (ACache.get(mContext).getAsString(Common.LOCATION_INFO) == null) {
            myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
        } else {
            try {
                com.alibaba.fastjson.JSONObject response =  com.alibaba.fastjson.JSONObject.parseObject(ACache.get(mContext).getAsString(Common.LOCATION_INFO));
                com.alibaba.fastjson.JSONArray resultArray = response.getJSONArray("locations");
                for (int i = 0; i < resultArray.size(); i++) {
                    DiscoverLocationItemInfo locationInfo = new DiscoverLocationItemInfo();
                    com.alibaba.fastjson.JSONObject object = resultArray.getJSONObject(i);
                    locationInfo = JsonUtil.getLocation(object);
                    resultArrayList.add(locationInfo);
                }
            } catch (com.alibaba.fastjson.JSONException e) {
                e.printStackTrace();
            }
        }
        return resultArrayList;
    }

    /**
     * 生成photoItemInfo的列表
     *
     * @param index
     * @return
     */
    private ArrayList<PhotoItemInfo> createPhotoItemInfoList(int index) {
        ArrayList<PhotoItemInfo> resultArrayList = new ArrayList<PhotoItemInfo>();
        ArrayList<PhotoInfo> photoInfos = new ArrayList<PhotoInfo>();
        photoInfos.addAll(arrayList.get(index).getSelectPhotoItemInfos());

        ArrayList<DiscoverLocationItemInfo> locationList = createLocationList();

        PhotoItemInfo photoItemInfo = new PhotoItemInfo();

        //遍历所有photopass信息
        boolean clone_contains = false;
        Date date1;
        Date date2;
        for (int l = 0; l < photoInfos.size(); l++) {
            PhotoInfo info = photoInfos.get(l);
//			Log.d(TAG, "scan photo list:"+l);
            //先挑选出相同的locationid信息
            for (int i = 0; i < locationList.size(); i++) {
//				Log.d(TAG, "scan location:"+i);
                if (info.locationId.equals(locationList.get(i).locationId) || locationList.get(i).locationIds.contains(info.locationId)) {
//					Log.d(TAG, "find the location");
                    //如果locationid一样，需要判断是否已经存在此item，如果有，在按照时间分类，没有，新建一个item
                    try {
                        for (int j = 0; j < resultArrayList.size(); j++) {
                            //						Log.d(TAG, "weather already exists:"+j);
                            if (info.shootTime.equals(resultArrayList.get(j).shootTime)
                                    && (info.locationId.equals(resultArrayList.get(j).locationId) || resultArrayList.get(j).locationIds.contains(info.locationId))) {
                                info.locationName = resultArrayList.get(j).place;
                                resultArrayList.get(j).list.add(info);
                                date1 = sdf.parse(info.shootOn);
                                date2 = sdf.parse(resultArrayList.get(j).shootOn);
                                if (date1.after(date2)) {
                                    resultArrayList.get(j).shootOn = info.shootOn;
                                }
                                clone_contains = true;
                                break;
                            }
                        }
                    } catch (ParseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (!clone_contains) {
                        //初始化item的信息
                        photoItemInfo = new PhotoItemInfo();
                        photoItemInfo.locationId = locationList.get(i).locationId;
                        photoItemInfo.locationIds = locationList.get(i).locationIds.toString();
                        photoItemInfo.shootTime = info.shootTime;
                        photoItemInfo.place = locationList.get(i).place;
                        info.locationName = locationList.get(i).place;
                        photoItemInfo.list.add(info);
                        photoItemInfo.placeUrl = locationList.get(i).placeUrl;
                        photoItemInfo.latitude = locationList.get(i).latitude;
                        photoItemInfo.longitude = locationList.get(i).longitude;
                        photoItemInfo.islove = 0;
                        photoItemInfo.shootOn = info.shootOn;
                        resultArrayList.add(photoItemInfo);
                    } else {
                        clone_contains = false;
                    }
                    break;
                }
            }
        }

        Collections.sort(resultArrayList);//对all进行排序

        return resultArrayList;
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
     */
    public void refresh(ArrayList<PPinfo> ppInfo1s) {
        this.arrayList = ppInfo1s;
        this.notifyDataSetChanged();
    }

    //删除图片监听
    public interface doDeletePhotoListener {
        public void deletePhoto(int position);
    }

    //显示图片监听
    public interface doShowPhotoListener {
        public void previewPhoto(int position, int tag);
    }

    class PhotoPassManagerOnClickListener implements OnClickListener {
        private int position;
        private int tag;
        private boolean delete;

        public PhotoPassManagerOnClickListener(int position, int tag, boolean delete) {
            this.position = position;
            this.tag = tag;
            this.delete = delete;
        }

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (delete) {
                //删除PP
                deleteListner.deletePhoto(position);
            } else {
                //选择PP 点击单张直接进入改PP的相册页面
                if (checkUrl(position, tag)) {
                    if (arrayList.get(position).getUrlList().size() > 0) {
                        //进入相册
                        ArrayList<PhotoItemInfo> allPhotoItemInfos = createPhotoItemInfoList(position);
                        Intent i = new Intent(mContext, EditStoryAlbumActivity.class);
                        Bundle b = new Bundle();
                        b.putParcelableArrayList("photos", AppUtil.startSortForPinnedListView(allPhotoItemInfos));
                        i.putExtra("photos", b);
                        i.putExtra("mode", "noedit");
                        mContext.startActivity(i);
                    }
//                    else {
//                        //显示单张图片
//                        listener.previewPhoto(position, tag);
//                    }
                }
            }

        }

    }


    //点击一个item的事件

    private class OnItemChildClickListener implements OnClickListener {
        private int position;

        public OnItemChildClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (arrayList.get(position) instanceof PPinfo) {
                PPinfo ppInfo = (PPinfo) arrayList.get(position);
                switch (v.getId()) {
                    case R.id.img:
                    case R.id.pp_item:
                        System.out.println("adapter---size=" + map.size());
                        if (null != mHandler) {
                            Message msg = mHandler.obtainMessage();
                            if (ppInfo.isSelected == 0) {
                                if (useNumber >= pppInfo.capacity) {
                                    myToast.setTextAndShow(R.string.outofrange, Common.TOAST_SHORT_TIME);
                                    break;
                                } else {
                                    ++choice;
                                    for (int j = 0; j < arrayList.size(); j++) {
//                                        PPinfo pp = (PPinfo) arrayList.get(j);
//                                        if (!pp.getShootDate().equals(ppInfo.getShootDate())) {// && !pp.time.equals("")
//                                            pp.isSelected = 2;
//                                        }
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
//					map.put(position, ppInfo.isSelected == 1);
                        break;

                    default:
                        break;
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
