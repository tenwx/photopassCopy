package com.pictureair.photopass.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.DailyPPCardInfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.ArrayList;

/**
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
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (dailyPPCardInfoArrayList.size() == 0 || position >= getItemCount()) {
            return;
        }

        if (viewHolder instanceof RecyclerItemViewHolder) {
            final RecyclerItemViewHolder recyclerViewHolder = (RecyclerItemViewHolder) viewHolder;
            recyclerViewHolder.ppCodeTV.setText(String.format(context.getString(R.string.story_card), dailyPPCardInfoArrayList.get(position).getPpCode()));
            recyclerViewHolder.dateTV.setText(dailyPPCardInfoArrayList.get(position).getShootDate());

            recyclerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.itemClick(position);

                }
            });

            recyclerViewHolder.buyTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.buyClick(position);

                }
            });

            recyclerViewHolder.downloadTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.downloadClick(position);
                }
            });

            if (dailyPPCardInfoArrayList.get(position).getActivated() == 1) {//已升级PP
                recyclerViewHolder.expireTV.setText(context.getString(R.string.story_expire_time));
                recyclerViewHolder.downloadTV.setVisibility(View.VISIBLE);
                recyclerViewHolder.buyTV.setVisibility(View.INVISIBLE);

            } else {
                recyclerViewHolder.expireTV.setText(context.getString(R.string.story_blur_expire_time));
                recyclerViewHolder.downloadTV.setVisibility(View.INVISIBLE);
                recyclerViewHolder.buyTV.setVisibility(View.VISIBLE);
            }

            PhotoInfo leftPhoto = dailyPPCardInfoArrayList.get(position).getLeftPhoto();
            PhotoInfo rightPhoto = dailyPPCardInfoArrayList.get(position).getRightPhoto();
            if (leftPhoto != null) {
                String photoUrl;
                if (leftPhoto.getIsPaid() == 1) {
                    photoUrl = Common.PHOTO_URL + leftPhoto.getPhotoThumbnail_512();

                } else {
                    photoUrl = leftPhoto.getPhotoThumbnail_128();

                }
                GlideUtil.load(context, photoUrl, leftPhoto.getIsEnImage() == 1, recyclerViewHolder.leftPhotoIV);
                recyclerViewHolder.leftLocationTV.setText(leftPhoto.getLocationName());
                recyclerViewHolder.leftLocationTV.setVisibility(View.VISIBLE);

            }

            if (rightPhoto != null) {
                String photoUrl;
                if (rightPhoto.getIsPaid() == 1) {
                    photoUrl = Common.PHOTO_URL + rightPhoto.getPhotoThumbnail_512();

                } else {
                    photoUrl = rightPhoto.getPhotoThumbnail_128();

                }
                recyclerViewHolder.rightPhotoIV.setScaleType(ImageView.ScaleType.CENTER_CROP);
                GlideUtil.load(context, photoUrl, rightPhoto.getIsEnImage() == 1, recyclerViewHolder.rightPhotoIV);
                recyclerViewHolder.rightLocationTV.setText(rightPhoto.getLocationName());
                recyclerViewHolder.rightLocationTV.setVisibility(View.VISIBLE);

            } else {
                recyclerViewHolder.rightPhotoIV.setScaleType(ImageView.ScaleType.FIT_CENTER);
                GlideUtil.load(context, GlideUtil.getDrawableUrl(context, R.drawable.story_no_photo_placeholder), recyclerViewHolder.rightPhotoIV);
                recyclerViewHolder.rightLocationTV.setVisibility(View.GONE);

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
        private ImageView leftPhotoIV, rightPhotoIV;
        private TextView leftLocationTV, rightLocationTV;
        private TextView dateTV, ppCodeTV, buyTV, expireTV, downloadTV;

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

            ViewGroup.LayoutParams params1 = leftPhotoIV.getLayoutParams();
            params1.height = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 50)) / COLUMN_COUNT;

            ViewGroup.LayoutParams params2 = rightPhotoIV.getLayoutParams();
            params2.height = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 50)) / COLUMN_COUNT;
        }
    }

    public interface OnRecyclerViewItemClickListener {
        void downloadClick(int position);
        void buyClick(int position);
        void itemClick(int position);
    }
}