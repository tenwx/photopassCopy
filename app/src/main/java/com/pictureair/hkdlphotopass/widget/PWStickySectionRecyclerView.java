package com.pictureair.hkdlphotopass.widget;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.adapter.StickyRecycleAdapter;
import com.pictureair.hkdlphotopass.entity.PhotoInfo;
import com.pictureair.hkdlphotopass.util.PictureAirLog;
import com.pictureair.hkdlphotopass.util.ScreenUtil;

import java.util.ArrayList;

/**
 * 可上拉加载更多的section RecyclerView
 * 支持是否设置section header
 * Created by bauer_bao on 16/11/21.
 */
public class PWStickySectionRecyclerView extends FrameLayout {
    private Context context;
    private RecyclerView recyclerView;
    private RelativeLayout sectionHeaderRL;
    private TextView sectionHeaderLocationTV, sectionHeaderPhotoCountTV;
    private ImageView sectionHeaderIV;
    private GridLayoutManager gridLayoutManager;
    private StickyRecycleAdapter stickyRecycleAdapter;
    private RecycleDividerItemDecoration recycleDividerItemDecoration;

    private ArrayList<PhotoInfo> photoInfoArrayList;
    private StickyRecycleAdapter.OnRecyclerViewItemClickListener onRecyclerViewItemClickListener;
    private OnPullListener onPullListener;
    private int columnCount = COLUMN_COUNT;
    private boolean isLoadMore = false;
    private boolean editMode = false;
    /**
     * 是否需要header悬浮，默认悬浮
     */
    private boolean isStickySectionHeader = true;

    private static final int COLUMN_COUNT = 3;//列数

    public PWStickySectionRecyclerView(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public PWStickySectionRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        inflate(context, R.layout.pw_sticky_section_recycler_view, this);
        recyclerView = (RecyclerView) findViewById(R.id.sticky_section_recycler_view);
        sectionHeaderRL = (RelativeLayout) findViewById(R.id.story_pinned_section_ll);
        sectionHeaderLocationTV = (TextView) findViewById(R.id.section_location_tv);
        sectionHeaderPhotoCountTV = (TextView) findViewById(R.id.section_photo_count_tv);
        sectionHeaderIV = (ImageView) findViewById(R.id.section_photo_iv);
    }

    /**
     * 设置列数
     * @param column
     */
    public void setColumnCount(int column) {
        if (column > 0) {
            columnCount = column;
        }
    }

    /**
     * 设置是否需要悬浮
     */
    public void setIsSectionMode(boolean sectionMode){
        isStickySectionHeader = sectionMode;
    }

    /**
     * 设置点击监听
     * @param listener
     */
    public void setOnRecyclerViewItemClickListener(StickyRecycleAdapter.OnRecyclerViewItemClickListener listener) {
        onRecyclerViewItemClickListener = listener;
    }

    /**
     * 设置加载监听
     * @param pullListener
     */
    public void setOnPullListener(OnPullListener pullListener){
        onPullListener = pullListener;
    }

    /**
     * 移除间距，防止叠加
     */
    public void removeItemDecoration() {
        if (recyclerView != null && recycleDividerItemDecoration != null) {//因为多次进来，会叠加间距，因此在此移除
            recyclerView.removeItemDecoration(recycleDividerItemDecoration);
        }
    }

    /**
     * 初始化数据
     */
    public void initDate(ArrayList<PhotoInfo> list, boolean editMode) {
        sectionHeaderRL.setVisibility(isStickySectionHeader ? VISIBLE : GONE);

        photoInfoArrayList = list;
        gridLayoutManager = new GridLayoutManager(context, columnCount);
        stickyRecycleAdapter = new StickyRecycleAdapter(context, photoInfoArrayList, editMode);
        stickyRecycleAdapter.setOnItemClickListener(onRecyclerViewItemClickListener);

        recyclerView.setHasFixedSize(true);
        recycleDividerItemDecoration = new RecycleDividerItemDecoration(ScreenUtil.dip2px(getContext(), 6));
        recyclerView.addItemDecoration(recycleDividerItemDecoration);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(stickyRecycleAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                dealWithLoadMore(dy, recyclerView);
                dealWithStickyHeader(recyclerView);
            }
        });
    }

    /**
     * 设置加载状态
     * @param loadMore
     */
    public void setIsLoadMore(boolean loadMore) {
        isLoadMore = loadMore;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        PictureAirLog.d("start edit--->");
        this.editMode = editMode;
        for (int i = 0; i < photoInfoArrayList.size(); i++) {
            photoInfoArrayList.get(i).setIsChecked(editMode ? 1 : 0);
        }
        stickyRecycleAdapter.setEditMode(editMode);

        stickyRecycleAdapter.notifyDataSetChanged();
    }

    /**
     * 判断是否需要加载更多
     * @param dy
     * @param recyclerView
     */
    private void dealWithLoadMore(int dy, RecyclerView recyclerView) {
        if (dy > 0 && gridLayoutManager.getItemCount() <= (gridLayoutManager.findFirstVisibleItemPosition() + recyclerView.getChildCount())
                && !isLoadMore) {//如果滑动到了item的最下面了（除了加载更多项）就开始加载更多
//		if (dy > 0 && !recyclerView.canScrollVertically(1) && !refreshLayout.isRefreshing() && !isLoadMore) {//如果滑动到底了，才开始加载更多
            PictureAirLog.d("start load more---->");
            if (onPullListener != null) {
                onPullListener.loadMore();
            }
        }
    }

    /**
     * 处理header
     * @param recyclerView
     */
    private void dealWithStickyHeader(RecyclerView recyclerView) {
        if (!isStickySectionHeader) {
            return;
        }
        // Get the sticky information from the topmost view of the screen.
        View stickyInfoView = recyclerView.findChildViewUnder(30, 1);//获取 （x,y）坐标下的view

        if (stickyInfoView != null && stickyInfoView.getContentDescription() != null) {
            String[] headers = stickyInfoView.getContentDescription().toString().split(",");
            sectionHeaderLocationTV.setText(headers[0]);
            sectionHeaderPhotoCountTV.setText("(" + headers[1] + ")");
            sectionHeaderIV.setVisibility(VISIBLE);
        }

        // Get the sticky view's translationY by the first view below the sticky's height.
        View transInfoView = recyclerView.findChildViewUnder(30, sectionHeaderRL.getMeasuredHeight() + 1);

        if (transInfoView != null && transInfoView.getTag() != null) {
            int transViewStatus = (int) transInfoView.getTag();
            int dealtY = transInfoView.getTop() - sectionHeaderRL.getMeasuredHeight();
            if (transViewStatus == StickyRecycleAdapter.HAS_STICKY_VIEW) {
                // If the first view below the sticky's height scroll off the screen,
                // then recovery the sticky view's translationY.
                if (transInfoView.getTop() > 0) {
                    sectionHeaderRL.setTranslationY(dealtY);
                } else {
                    sectionHeaderRL.setTranslationY(0);
                }
            } else if (transViewStatus == StickyRecycleAdapter.NONE_STICKY_VIEW) {
                sectionHeaderRL.setTranslationY(0);
            }
        }
    }

    /**
     * 通知adapter更新数据
     */
    public void notifyDataSetChanged() {
        if (stickyRecycleAdapter != null) {
            stickyRecycleAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 更新header数据
     */
    public void refreshHeaderView() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                dealWithStickyHeader(recyclerView);
            }
        }, 50);
    }

    /**
     * 返回当前处理状态
     * @return
     */
    public boolean isLoadMore() {
        return isLoadMore;
    }

    /**
     * 设置adapter的状态，并且通知更新UI
     * @param type
     */
    public void setLoadMoreType(int type) {
        if (stickyRecycleAdapter != null) {
            stickyRecycleAdapter.setLoadMoreType(type);
            recyclerView.post(new Runnable() {//不能在measure或者layout的过程中notify
                @Override
                public void run() {
                    stickyRecycleAdapter.notifyItemChanged(stickyRecycleAdapter.getItemCount() - 1);
                }
            });
        }
    }

    /**
     * 滑动到指定位置
     * @param position
     */
    public void scrollToPosition(int position) {
        recyclerView.scrollToPosition(position);
    }

    /**
     * 加载监听
     */
    public interface OnPullListener{
        /**
         * 加载监听
         */
        void loadMore();
    }
}
