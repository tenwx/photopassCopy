package com.pictureair.hkdlphotopass.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.entity.DailyPPCardInfo;
import com.pictureair.hkdlphotopass.entity.PhotoInfo;
import com.pictureair.hkdlphotopass.util.Common;
import com.pictureair.hkdlphotopass.util.GlideUtil;
import com.pictureair.hkdlphotopass.util.ScreenUtil;

import java.util.ArrayList;

/**
 * 首页一卡一天的adapter
 * Created by bauer_bao on 16/11/10.
 */

public class DailyPPCardRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int COLUMN_COUNT = 2;

    private Context context;
    private ArrayList<DailyPPCardInfo> dailyPPCardInfoArrayList;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;


    public DailyPPCardRecycleAdapter(Context context, ArrayList<DailyPPCardInfo> dailyPPCardInfoArrayList) {
        this.context = context;
        this.dailyPPCardInfoArrayList = dailyPPCardInfoArrayList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.daily_pp_item_view, parent, false);
        return new RecyclerItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (dailyPPCardInfoArrayList.size() == 0 || position >= getItemCount()) {
            return;
        }

        if (viewHolder instanceof RecyclerItemViewHolder) {
            final int pos = viewHolder.getAdapterPosition();
            final RecyclerItemViewHolder recyclerViewHolder = (RecyclerItemViewHolder) viewHolder;
            recyclerViewHolder.ppCodeTV.setText(dailyPPCardInfoArrayList.get(position).getPpCode());
            recyclerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.itemClick(pos);

                }
            });

            recyclerViewHolder.buyTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.buyClick(pos);

                }
            });

            recyclerViewHolder.downloadTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.downloadClick(pos);
                }
            });

            if (dailyPPCardInfoArrayList.get(position).getPhotoCount() == 0) {//空卡
                recyclerViewHolder.dateTV.setVisibility(View.GONE);
//
                recyclerViewHolder.expireTV.setVisibility(View.GONE);
                recyclerViewHolder.downloadTV.setVisibility(View.GONE);
                recyclerViewHolder.buyTV.setVisibility(View.GONE);

//                recyclerViewHolder.leftPhotoIV.setVisibility(View.GONE);
                recyclerViewHolder.leftLocationTV.setVisibility(View.GONE);
                recyclerViewHolder.rightLocationTV.setVisibility(View.GONE);
//                recyclerViewHolder.rightPhotoIV.setVisibility(View.GONE);

            } else {
                recyclerViewHolder.dateTV.setVisibility(View.VISIBLE);
                recyclerViewHolder.dateTV.setText(dailyPPCardInfoArrayList.get(position).getShootDate().replace("-", "."));

                recyclerViewHolder.expireTV.setVisibility(View.VISIBLE);
                if (dailyPPCardInfoArrayList.get(position).getActivated() == 1) {//已升级PP
                    recyclerViewHolder.expireTV.setText(context.getString(R.string.story_expire_time));
//                    recyclerViewHolder.expireTV.setText(String.format(context.getString(R.string.story_expire_time),
//                            dailyPPCardInfoArrayList.get(position).getExpiredDays() == 0 ? 60 : dailyPPCardInfoArrayList.get(position).getExpiredDays()));
                    recyclerViewHolder.downloadTV.setVisibility(View.VISIBLE);
                    recyclerViewHolder.buyTV.setVisibility(View.INVISIBLE);

                } else {
                    recyclerViewHolder.expireTV.setText(context.getString(R.string.story_blur_expire_time));
                    recyclerViewHolder.downloadTV.setVisibility(View.INVISIBLE);
                    recyclerViewHolder.buyTV.setVisibility(View.VISIBLE);
                }
            }

            PhotoInfo leftPhoto = dailyPPCardInfoArrayList.get(position).getLeftPhoto();
            PhotoInfo rightPhoto = dailyPPCardInfoArrayList.get(position).getRightPhoto();
            if (leftPhoto != null) {
                String photoUrl;
                if (dailyPPCardInfoArrayList.get(position).getActivated() == 1) {
                    photoUrl = Common.PHOTO_URL + leftPhoto.getPhotoThumbnail_512();

                } else {
                    photoUrl = leftPhoto.getPhotoThumbnail_128();

                }
                recyclerViewHolder.leftPhotoIV.setVisibility(View.VISIBLE);
                if (recyclerViewHolder.leftPhotoIV.getTag(R.id.glide_image_tag) == null || !recyclerViewHolder.leftPhotoIV.getTag(R.id.glide_image_tag).equals(photoUrl)) {
                    GlideUtil.load(context, photoUrl, leftPhoto.getIsEnImage() == 1, recyclerViewHolder.leftPhotoIV);
                    recyclerViewHolder.leftPhotoIV.setTag(R.id.glide_image_tag, photoUrl);
                }
                recyclerViewHolder.leftLocationTV.setText(leftPhoto.getLocationName());
                recyclerViewHolder.leftLocationTV.setVisibility(View.VISIBLE);

            } else {
                recyclerViewHolder.leftPhotoIV.setVisibility(View.VISIBLE);
                recyclerViewHolder.leftPhotoIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                GlideUtil.load(context, GlideUtil.getDrawableUrl(context, R.drawable.story_no_photo_pholder), recyclerViewHolder.leftPhotoIV);
                recyclerViewHolder.leftPhotoIV.setTag(R.id.glide_image_tag, null);
                recyclerViewHolder.rightLocationTV.setVisibility(View.GONE);
            }

            if (rightPhoto != null) {
                String photoUrl;
                if (dailyPPCardInfoArrayList.get(position).getActivated() == 1) {
                    photoUrl = Common.PHOTO_URL + rightPhoto.getPhotoThumbnail_512();

                } else {
                    photoUrl = rightPhoto.getPhotoThumbnail_128();

                }
                recyclerViewHolder.rightPhotoIV.setVisibility(View.VISIBLE);
                recyclerViewHolder.rightPhotoIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                if (recyclerViewHolder.rightPhotoIV.getTag(R.id.glide_image_tag) == null || !recyclerViewHolder.rightPhotoIV.getTag(R.id.glide_image_tag).equals(photoUrl)) {
                    GlideUtil.load(context, photoUrl, rightPhoto.getIsEnImage() == 1, recyclerViewHolder.rightPhotoIV);
                    recyclerViewHolder.rightPhotoIV.setTag(R.id.glide_image_tag, photoUrl);
                }
                recyclerViewHolder.rightLocationTV.setText(rightPhoto.getLocationName());
                recyclerViewHolder.rightLocationTV.setVisibility(View.VISIBLE);

            } else {
                recyclerViewHolder.rightPhotoIV.setVisibility(View.VISIBLE);
                recyclerViewHolder.rightPhotoIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                GlideUtil.load(context, GlideUtil.getDrawableUrl(context, R.drawable.story_no_photo_pholder), recyclerViewHolder.rightPhotoIV);
                recyclerViewHolder.rightPhotoIV.setTag(R.id.glide_image_tag, null);
                recyclerViewHolder.rightLocationTV.setVisibility(View.GONE);
            }
            //设置卡片颜色
            recyclerViewHolder.cardView.setCardBackgroundColor(Color.rgb(dailyPPCardInfoArrayList.get(position).getColorR(),
                    dailyPPCardInfoArrayList.get(position).getColorG(), dailyPPCardInfoArrayList.get(position).getColorB()));

            //设置卡片样式
            String logoUrl;
            if (TextUtils.isEmpty(dailyPPCardInfoArrayList.get(position).getLogoUrl())) {
                logoUrl = GlideUtil.getDrawableUrl(context, R.drawable.photopass_logo_default);
            } else {
                logoUrl = Common.PHOTO_URL + dailyPPCardInfoArrayList.get(position).getLogoUrl();
//                logoUrl = GlideUtil.getDrawableUrl(context, R.drawable.hk_item_back);
            }
            if (recyclerViewHolder.logoIV.getTag(R.id.glide_image_tag) == null || !recyclerViewHolder.logoIV.getTag(R.id.glide_image_tag).equals(logoUrl)) {
                GlideUtil.load(context, logoUrl, recyclerViewHolder.logoIV);
                recyclerViewHolder.logoIV.setTag(R.id.glide_image_tag, logoUrl);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return dailyPPCardInfoArrayList.size();
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public class RecyclerItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView leftPhotoIV, rightPhotoIV, logoIV;
        private TextView leftLocationTV, rightLocationTV;
        private TextView dateTV, ppCodeTV, buyTV, expireTV, downloadTV;
        private CardView cardView;

        public RecyclerItemViewHolder(View convertView) {
            super(convertView);
            dateTV = (TextView) itemView.findViewById(R.id.daily_pp_header_date_tv);
            ppCodeTV = (TextView) itemView.findViewById(R.id.daily_pp_header_card_no_tv);
            expireTV = (TextView) itemView.findViewById(R.id.daily_pp_header_bottom_tip_tv);
            buyTV = (TextView) itemView.findViewById(R.id.daily_pp_header_buy_tv);
            downloadTV = (TextView) itemView.findViewById(R.id.daily_pp_header_download_tv);
            leftPhotoIV = (ImageView) itemView.findViewById(R.id.daily_pp_header_left_iv);
            leftLocationTV = (TextView) itemView.findViewById(R.id.daily_pp_header_left_tv);
            rightPhotoIV = (ImageView) itemView.findViewById(R.id.daily_pp_header_right_iv);
            rightLocationTV = (TextView) itemView.findViewById(R.id.daily_pp_header_right_tv);
            cardView = (CardView) itemView.findViewById(R.id.daily_pp_item_cardv);
            logoIV = (ImageView) itemView.findViewById(R.id.daily_pp_header_logo_iv);

            ViewGroup.LayoutParams params1 = leftPhotoIV.getLayoutParams();
            params1.height = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 54)) / COLUMN_COUNT;

            ViewGroup.LayoutParams params2 = rightPhotoIV.getLayoutParams();
            params2.height = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 54)) / COLUMN_COUNT;
        }
    }

    public interface OnRecyclerViewItemClickListener {
        void downloadClick(int position);

        void buyClick(int position);

        void itemClick(int position);
    }
}