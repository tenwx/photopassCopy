package com.pictureair.photopass.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.util.AppUtil;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.GlideUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.ScreenUtil;
import com.pictureair.photopass.widget.CustomTextView;

import java.text.ParseException;
import java.util.ArrayList;

/**
 * Created by bauer_bao on 16/11/10.
 */

public class StickyRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int HAS_STICKY_VIEW = 2;
    public static final int NONE_STICKY_VIEW = 3;

    public static final int LOAD_MORE_VIEW_TYPE = 100;
    public static final int LOAD_HEADER_VIEW_TYPE = 101;

    public static final int LOAD_MORE_LOADING = 6;//加载中的UI
    public static final int LOAD_MORE_NO_MORE = 7;//没有更多数据的UI
    public static final int LOAD_MORE_FAILED = 8;//加载失败的处理，可以点击，重新请求更多数据
    public static final int LOAD_MORE_CLICK_LOAD = 9;//如果数据少于1页，并且数据还可以加载，就可以点击，请求更多数据

    private static final int COLUMN_COUNT = 3;

    private Context context;
    private ArrayList<PhotoInfo> photoList;

    private int loadMoreType = LOAD_MORE_NO_MORE;

    private OnRecyclerViewItemClickListener mOnItemClickListener = null;


    public StickyRecycleAdapter(Context context, ArrayList<PhotoInfo> photoList) {
        this.context = context;
        this.photoList = photoList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOAD_MORE_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.story_pinned_listview_load_more, parent, false);
            return new LoadMoreViewHolder(view);

        } else if (viewType == LOAD_HEADER_VIEW_TYPE) {
            View view = LayoutInflater.from(context).inflate(R.layout.story_pinned_listview_section, parent, false);
            return new StickySectionHeaderViewHolder(view);

        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.sticky_grid_view, parent, false);
            return new RecyclerItemViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (photoList.size() == 0 || position >= getItemCount()) {
            return;
        }

        if (viewHolder instanceof RecyclerItemViewHolder) {
            final RecyclerItemViewHolder recyclerViewHolder = (RecyclerItemViewHolder) viewHolder;
            String photoUrl;
            if (photoList.size() == 0 || position >= photoList.size()) {
                return;
            }
            if (photoList.get(position).onLine == 1) {
                if (photoList.get(position).isPayed == 1) {
                    photoUrl = Common.PHOTO_URL + photoList.get(position).photoThumbnail_512;
                } else {
                    photoUrl = photoList.get(position).photoThumbnail;
                }
                if (photoList.get(position).isVideo == 1) {
                    recyclerViewHolder.videoImageView.setVisibility(View.VISIBLE);
                    ViewGroup.LayoutParams params2 = recyclerViewHolder.videoImageView.getLayoutParams();
                    params2.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 6 * 2)) / (4 * COLUMN_COUNT);
                    params2.height = params2.width;
                    recyclerViewHolder.videoImageView.setLayoutParams(params2);
                } else {
                    recyclerViewHolder.videoImageView.setVisibility(View.GONE);
                }
            } else {
                photoUrl = GlideUtil.getFileUrl(photoList.get(position).photoPathOrURL);
                recyclerViewHolder.videoImageView.setVisibility(View.GONE);
            }

            if (recyclerViewHolder.mImageView.getTag(R.id.glide_image_tag) == null || !recyclerViewHolder.mImageView.getTag(R.id.glide_image_tag).equals(photoUrl)) {//加载图片
                GlideUtil.load(context, photoUrl, AppUtil.isEncrypted(photoList.get(position).isEncrypted), recyclerViewHolder.mImageView);
                recyclerViewHolder.mImageView.setTag(R.id.glide_image_tag, photoUrl);
            }

            recyclerViewHolder.itemView.setTag(NONE_STICKY_VIEW);
            String headerTime = AppUtil.getHeaderTime(photoList, position);
            try {
                recyclerViewHolder.itemView.setContentDescription(AppUtil.dateToSmartDate(headerTime, context));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            recyclerViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PictureAirLog.d("click---->" + recyclerViewHolder.getLayoutPosition() + (mOnItemClickListener != null));
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(recyclerViewHolder.itemView, recyclerViewHolder.getLayoutPosition());
                    }
                }
            });


        } else if (viewHolder instanceof LoadMoreViewHolder) {
            final LoadMoreViewHolder loadMoreViewHolder = (LoadMoreViewHolder) viewHolder;

            PictureAirLog.d(" load more ---> " + loadMoreType);
            switch (loadMoreType) {
                case LOAD_MORE_LOADING:
                    loadMoreViewHolder.pbLoading.setVisibility(View.VISIBLE);
                    loadMoreViewHolder.tvLoadStatus.setVisibility(View.VISIBLE);
                    loadMoreViewHolder.tvLoadStatus.setText(context.getResources().getString(R.string.is_loading));
                    break;

                case LOAD_MORE_NO_MORE:
                    loadMoreViewHolder.pbLoading.setVisibility(View.GONE);
                    loadMoreViewHolder.tvLoadStatus.setText(context.getResources().getString(R.string.nomore));
                    break;

                case LOAD_MORE_FAILED:
                    loadMoreViewHolder.pbLoading.setVisibility(View.GONE);
                    loadMoreViewHolder.tvLoadStatus.setText(context.getResources().getString(R.string.failed_click_load));
                    loadMoreViewHolder.tvLoadStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnItemClickListener != null) {
                                mOnItemClickListener.onLoadMoreClick(loadMoreViewHolder.tvLoadStatus, loadMoreViewHolder.getLayoutPosition());
                            }
                        }
                    });
                    break;

                case LOAD_MORE_CLICK_LOAD:
                    loadMoreViewHolder.pbLoading.setVisibility(View.GONE);
                    loadMoreViewHolder.tvLoadStatus.setText(context.getResources().getString(R.string.click_load_more));
                    loadMoreViewHolder.tvLoadStatus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mOnItemClickListener != null) {
                                mOnItemClickListener.onLoadMoreClick(loadMoreViewHolder.tvLoadStatus, loadMoreViewHolder.getLayoutPosition());
                            }
                        }
                    });
                    break;
            }
        } else if (viewHolder instanceof StickySectionHeaderViewHolder) {
            StickySectionHeaderViewHolder recyclerViewSectionViewHolder = (StickySectionHeaderViewHolder) viewHolder;

            String headerTime = AppUtil.getHeaderTime(photoList, position);
            try {
                recyclerViewSectionViewHolder.storyTimeTextView.setText(AppUtil.dateToSmartDate(headerTime, context));
                recyclerViewSectionViewHolder.itemView.setContentDescription(AppUtil.dateToSmartDate(headerTime, context));
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            recyclerViewSectionViewHolder.itemView.setTag(HAS_STICKY_VIEW);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1) {
            return LOAD_MORE_VIEW_TYPE;
        } else if (position == 0 || (photoList.get(position).sectionId != photoList.get(position - 1).sectionId)){
            return LOAD_HEADER_VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return photoList == null ? 1 : photoList.size() + 1;
    }

    public void setLoadMoreType(int loadMoreType) {
        this.loadMoreType = loadMoreType;
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public class LoadMoreViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar pbLoading;
        public TextView tvLoadStatus;

        public LoadMoreViewHolder(View itemView) {
            super(itemView);

            pbLoading = (ProgressBar) itemView.findViewById(R.id.pb_loading);
            tvLoadStatus = (TextView) itemView.findViewById(R.id.tv_load_status);
        }
    }
    public class StickySectionHeaderViewHolder extends RecyclerView.ViewHolder {
        private CustomTextView storyTimeTextView;

        public StickySectionHeaderViewHolder(View itemView) {
            super(itemView);
            storyTimeTextView = (CustomTextView) itemView.findViewById(R.id.section_time);
            storyTimeTextView.setTypeface(MyApplication.getInstance().getFontBold());
        }
    }

    public class RecyclerItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;
        public ImageView videoImageView;
        public RelativeLayout gridItemRL;

        public RecyclerItemViewHolder(View convertView) {
            super(convertView);
            mImageView = (ImageView) convertView.findViewById(R.id.sticky_imageView);
            videoImageView = (ImageView) convertView.findViewById(R.id.play_video_iv);
            gridItemRL = (RelativeLayout) convertView.findViewById(R.id.sticky_grid_view_rl);
            ViewGroup.LayoutParams params = gridItemRL.getLayoutParams();
            //宽度不用设置，会平均分配，大小也正好等于高度的数值
            params.width = (ScreenUtil.getScreenWidth(context) - ScreenUtil.dip2px(context, 6 * 2)) / COLUMN_COUNT;
            params.height = params.width;
            gridItemRL.setLayoutParams(params);
        }
    }

    //define interface
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , int position);
        void onLoadMoreClick(View view , int position);
    }
}