package com.pictureair.photopass.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.DailyPPCardInfo;
import com.pictureair.photopass.util.BlurUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.ScreenUtil;

import java.util.ArrayList;

/**
 * Created by bauer_bao on 16/11/10.
 */

public class DailyPPCardRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int LOAD_HEADER_VIEW_TYPE = 101;
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
        if (viewType == LOAD_HEADER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.daily_pp_header_view, parent, false);
            return new StickySectionHeaderViewHolder(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.daily_pp_item_view, parent, false);
            return new RecyclerItemViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (dailyPPCardInfoArrayList.size() == 0 || position >= getItemCount()) {
            return;
        }

        if (viewHolder instanceof RecyclerItemViewHolder) {
            final RecyclerItemViewHolder recyclerViewHolder = (RecyclerItemViewHolder) viewHolder;
            recyclerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (dailyPPCardInfoArrayList.get(position).getPpCode().equals("occupy")) {//通用图，需要传递前一个position
                        mOnItemClickListener.itemClick(position - 1);

                    } else {
                        mOnItemClickListener.itemClick(position);
                    }
                }
            });
            if (dailyPPCardInfoArrayList.get(position).getPpCode().equals("occupy")) {//占位图，通用图
                GlideUtil.load(context, GlideUtil.getDrawableUrl(context, R.drawable.story_no_photo_placeholder), recyclerViewHolder.photoIV);
                return;
            }
            recyclerViewHolder.timeTV.setText(dailyPPCardInfoArrayList.get(position).getLocationPhoto().getStrShootOn().substring(0, 16));
            recyclerViewHolder.locationTV.setText(dailyPPCardInfoArrayList.get(position).getLocationPhoto().getLocationName());
            String photoUrl;
            if (dailyPPCardInfoArrayList.get(position).getLocationPhoto().getIsPaid() == 1) {
                photoUrl = Common.PHOTO_URL + dailyPPCardInfoArrayList.get(position).getLocationPhoto().getPhotoThumbnail_512();
            } else {
                photoUrl = dailyPPCardInfoArrayList.get(position).getLocationPhoto().getPhotoThumbnail_128();
            }
            GlideUtil.load(context, photoUrl, dailyPPCardInfoArrayList.get(position).getLocationPhoto().getIsEnImage() == 1, recyclerViewHolder.photoIV);

        } else if (viewHolder instanceof StickySectionHeaderViewHolder) {
            final StickySectionHeaderViewHolder recyclerViewSectionViewHolder = (StickySectionHeaderViewHolder) viewHolder;
            recyclerViewSectionViewHolder.ppCodeTV.setText(String.format(context.getString(R.string.story_card), dailyPPCardInfoArrayList.get(position).getPpCode()));
            recyclerViewSectionViewHolder.dateTV.setText(dailyPPCardInfoArrayList.get(position).getShootDate());
            recyclerViewSectionViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.itemClick(position);

                }
            });
            recyclerViewSectionViewHolder.buyTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.buyClick(position);

                }
            });
            recyclerViewSectionViewHolder.previewTV.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.previewClick(position);
                }
            });
            recyclerViewSectionViewHolder.downloadRL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnItemClickListener.downloadClick(position);
                }
            });
            if (dailyPPCardInfoArrayList.get(position).getActivated() == 1) {//已升级PP
                recyclerViewSectionViewHolder.expireTV.setText(context.getString(R.string.story_expire_time));
                GlideUtil.load(context, Common.PHOTO_URL + dailyPPCardInfoArrayList.get(position).getAlbumCoverPhoto().getPhotoThumbnail_512(),
                        dailyPPCardInfoArrayList.get(position).getLocationPhoto().getIsEnImage() == 1, recyclerViewSectionViewHolder.bgIV);
                recyclerViewSectionViewHolder.downloadRL.setVisibility(View.VISIBLE);
                recyclerViewSectionViewHolder.buyTV.setVisibility(View.GONE);
                recyclerViewSectionViewHolder.previewTV.setVisibility(View.GONE);
                recyclerViewSectionViewHolder.albumIV.setVisibility(View.GONE);

            } else {
                GlideUtil.load(context, dailyPPCardInfoArrayList.get(position).getAlbumCoverPhoto().getPhotoThumbnail_128(),
                        dailyPPCardInfoArrayList.get(position).getLocationPhoto().getIsEnImage() == 1, new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                recyclerViewSectionViewHolder.bgIV.setImageBitmap(BlurUtil.blur(bitmap.copy(Bitmap.Config.ARGB_8888, true)));
                            }
                        });
                recyclerViewSectionViewHolder.expireTV.setText(context.getString(R.string.story_blur_expire_time));
                recyclerViewSectionViewHolder.downloadRL.setVisibility(View.GONE);
                recyclerViewSectionViewHolder.buyTV.setVisibility(View.VISIBLE);
                recyclerViewSectionViewHolder.previewTV.setVisibility(View.VISIBLE);
                recyclerViewSectionViewHolder.albumIV.setVisibility(View.VISIBLE);
                GlideUtil.load(context, dailyPPCardInfoArrayList.get(position).getAlbumCoverPhoto().getPhotoThumbnail_128(),
                        dailyPPCardInfoArrayList.get(position).getLocationPhoto().getIsEnImage() == 1, new SimpleTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                                if (bitmap != null) {
                                    ViewGroup.LayoutParams params = recyclerViewSectionViewHolder.albumIV.getLayoutParams();
                                    if (bitmap.getWidth() > bitmap.getHeight()) {//横图
                                        params.width = ScreenUtil.getScreenWidth(context) / 3;
                                        params.height = params.width * 3 / 4;
                                    } else {//竖图
                                        params.height = ScreenUtil.getScreenWidth(context) / 3;
                                        params.width = params.height * 3 / 4;

                                    }
                                    recyclerViewSectionViewHolder.albumIV.setImageBitmap(bitmap);
                                }
                            }
                        });
            }


            if (dailyPPCardInfoArrayList.get(position).getSectionId() == dailyPPCardInfoArrayList.get(dailyPPCardInfoArrayList.size() - 1).getSectionId()) {//最后一个header
                recyclerViewSectionViewHolder.titleTV.setText(context.getString(R.string.story_first_photo_in_disney));
            } else {
                recyclerViewSectionViewHolder.titleTV.setText(context.getString(R.string.story_with_disney));
            }

        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0 || (dailyPPCardInfoArrayList.get(position).getSectionId() != dailyPPCardInfoArrayList.get(position - 1).getSectionId())) {
            return LOAD_HEADER_VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return dailyPPCardInfoArrayList.size();
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public class StickySectionHeaderViewHolder extends RecyclerView.ViewHolder {
        private ImageView bgIV, albumIV;
        private TextView dateTV, ppCodeTV, buyTV, previewTV, expireTV, titleTV;
        private RelativeLayout downloadRL;

        public StickySectionHeaderViewHolder(View itemView) {
            super(itemView);
            bgIV = (ImageView) itemView.findViewById(R.id.daily_pp_header_bg_iv);
            dateTV = (TextView) itemView.findViewById(R.id.daily_pp_header_date_tv);
            ppCodeTV = (TextView) itemView.findViewById(R.id.daily_pp_header_card_no_tv);
            buyTV = (TextView) itemView.findViewById(R.id.daily_pp_header_buy_tv);
            previewTV = (TextView) itemView.findViewById(R.id.daily_pp_header_preview_tv);
            downloadRL = (RelativeLayout) itemView.findViewById(R.id.daily_pp_header_download_rl);
            expireTV = (TextView) itemView.findViewById(R.id.daily_pp_header_bottom_tip_tv);
            titleTV = (TextView) itemView.findViewById(R.id.daily_pp_header_title_tv);
            albumIV = (ImageView) itemView.findViewById(R.id.daily_pp_header_album_iv);
            ViewGroup.LayoutParams params = itemView.getLayoutParams();
            params.width = ScreenUtil.getScreenWidth(context);
            params.height = params.width * 3 / 5;
            itemView.setLayoutParams(params);
        }
    }

    public class RecyclerItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView photoIV;
        private TextView locationTV;
        private TextView timeTV;

        public RecyclerItemViewHolder(View convertView) {
            super(convertView);
            photoIV = (ImageView) convertView.findViewById(R.id.daily_pp_item_photo_iv);
            locationTV = (TextView) convertView.findViewById(R.id.daily_pp_item_location_tv);
            timeTV = (TextView) convertView.findViewById(R.id.daily_pp_item_time_tv);
            ViewGroup.LayoutParams params = convertView.getLayoutParams();
            params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 4 * 5)) / COLUMN_COUNT;
            params.height = params.width;
            convertView.setLayoutParams(params);
        }
    }

    public interface OnRecyclerViewItemClickListener {
        void downloadClick(int position);
        void buyClick(int position);
        void previewClick(int position);
        void itemClick(int position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {//如果是gridlayoutmanager要实现header，需要继承此方法
        super.onAttachedToRecyclerView(recyclerView);
        final RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    //设置view的类型
                    int viewType = getItemViewType(position);
                    if (viewType == LOAD_HEADER_VIEW_TYPE) {//如果是header，获取当前gridlayoutmanager的列数
                        return gridLayoutManager.getSpanCount();//获取列数
                    } else {//如果是item，则返回1
                        return 1;
                    }
                }
            });
            //重新设置列数
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }
    }
}