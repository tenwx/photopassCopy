package com.pictureair.photopassCopy.widget.pullloadlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.util.PictureAirLog;

/**
 * Created by pengwu on 16/6/16.
 */
public class ReFreshLayout extends RelativeLayout {


    private float mStartY;
    private OnRefreshListener listener;
    private int mState = STATUS_REFRESH_FINISHED;
    private boolean mIsLoading;
    private View header;
    private ProgressBar progressBar;
    private ImageView arrow;
    private TextView description;
    private MarginLayoutParams headerLayoutParams;
    private int hideHeaderHeight;
    private MyListView listView;
    private int lastStatus = mState;
    private boolean loadOnce;
    /**
     * 下拉状态
     */
    public static final int STATUS_PULL_TO_REFRESH = 0;

    /**
     * 释放立即刷新状态
     */
    public static final int STATUS_RELEASE_TO_REFRESH = 1;

    /**
     * 正在刷新状态
     */
    public static final int STATUS_REFRESHING = 2;

    /**
     * 刷新完成或未刷新状态
     */
    public static final int STATUS_REFRESH_FINISHED = 3;
    /**
     * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
     */
    private boolean ableToPull;
    /**
     * 累计移动距离
     */
    private int allMove;
    /**
     * 表示已截取到move事件
     */
    private boolean isPull;
    float lastY;

    public ReFreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null);
        progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
        arrow = (ImageView) header.findViewById(R.id.arrow);
        description = (TextView) header.findViewById(R.id.description);
        addView(header, 0);
        mIsLoading = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        PictureAirLog.e("onLayout", "onLayout");
        if (!loadOnce && listView != null) {

            hideHeaderHeight = -header.getHeight();
            LayoutParams lp = (LayoutParams) (header.getLayoutParams());
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            header.setLayoutParams(lp);
            headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
            headerLayoutParams.bottomMargin = hideHeaderHeight;
            loadOnce = true;
        }
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    public void setListView(MyListView lv) {
        this.listView = lv;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (listView == null) {
            return super.dispatchTouchEvent(ev);
        }
        boolean handle = super.dispatchTouchEvent(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_UP) {
            handle |= onTouchEvent(ev);
        }
        return handle;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (listView == null) {
            return super.onInterceptTouchEvent(ev);
        } else {
            setIsAbleToPull(ev);
            PictureAirLog.v("RefreshLayout", "move");
            PictureAirLog.v("ableToPull", String.valueOf(ableToPull));
            PictureAirLog.v("mIsLoading", String.valueOf(mIsLoading));
            if (ableToPull && !mIsLoading && listView != null) {
                if (isPull) {
                    return true;
                }
                if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                    float tmpY = ev.getRawY();
                    float space = Math.round(lastY - tmpY + 0.5f);
                    if (lastY != tmpY) {
                        lastY = tmpY;
                    }
                    PictureAirLog.v("RefreshLayout", "space");
                    PictureAirLog.v("space", String.valueOf(space));
                    PictureAirLog.v("allmove", String.valueOf(allMove));
                    if (space > 5 && allMove >= 0) {
                        isPull = true;
                        return true;
                    }
                }
            } else if (mIsLoading) {
                if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                    return true;
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (listView == null) {
            return super.onTouchEvent(ev);
        }
        setIsAbleToPull(ev);
        if (ableToPull && !mIsLoading && listView != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartY = ev.getRawY();
                    lastY = mStartY;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float tmpY = ev.getRawY();
                    float mSpace = lastY - tmpY;
                    int res = Math.round(mSpace / 2 + 0.5f);
                    if (lastY != tmpY) {
                        lastY = tmpY;
                    }
                    if (headerLayoutParams.bottomMargin < hideHeaderHeight) {
                        return false;
                    }
                    if (mState != STATUS_REFRESHING) {
                        if (headerLayoutParams.bottomMargin > 0) {
                            mState = STATUS_RELEASE_TO_REFRESH;
                        } else {
                            mState = STATUS_PULL_TO_REFRESH;
                        }
                    }
                    PictureAirLog.v("before res", String.valueOf(res));
                    if (res + headerLayoutParams.bottomMargin < hideHeaderHeight) {
                        res = hideHeaderHeight - headerLayoutParams.bottomMargin;
                    }
                    PictureAirLog.v("bottomMargin", String.valueOf(headerLayoutParams.bottomMargin));
                    PictureAirLog.v("res", String.valueOf(res));
                    allMove += res;
                    PictureAirLog.v("ontouchevent allMove", String.valueOf(allMove));
                    PictureAirLog.v("ontouchevent", String.valueOf("ACTION_MOVE"));
                    listView.scrollBy(0, res);
                    headerLayoutParams.bottomMargin = res + headerLayoutParams.bottomMargin;
                    header.setLayoutParams(headerLayoutParams);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    PictureAirLog.v("ontouchevent", "ACTION_UP");
                    if (mState == STATUS_RELEASE_TO_REFRESH && !mIsLoading) {
                        // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                        listView.scrollBy(0, -headerLayoutParams.bottomMargin);
                        headerLayoutParams.bottomMargin = 0;
                        header.setLayoutParams(headerLayoutParams);
                        mState = STATUS_REFRESHING;
                        updateHeaderView();
                        if (listener != null) {
                            mIsLoading = true;
                            listener.onRefresh();
                        }
                        PictureAirLog.v("Ontouc", "STATUS_RELEASE_TO_REFRESH");
                    } else if (mState == STATUS_PULL_TO_REFRESH) {
                        // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
                        resetView();
                        PictureAirLog.v("ontouchevent", "STATUS_PULL_TO_REFRESH");
                    }
                    PictureAirLog.v("ontouchevent", "out");
                    break;
            }
            // 时刻记得更新下拉头中的信息
            if (mState == STATUS_PULL_TO_REFRESH
                    || mState == STATUS_RELEASE_TO_REFRESH) {
                updateHeaderView();
                // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
                listView.setPressed(false);
                listView.setFocusable(false);
                listView.setFocusableInTouchMode(false);
                lastStatus = mState;
                // 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
                return true;
            }
        }
        return false;
    }

    private void resetView() {
        headerLayoutParams.bottomMargin = hideHeaderHeight;
        header.setLayoutParams(headerLayoutParams);
        if (listView != null) {
            listView.scrollTo(0, 0);
        }
        allMove = 0;
        isPull = false;
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        mState = STATUS_REFRESH_FINISHED;
        resetView();
        mIsLoading = false;
    }

    private void updateHeaderView() {
        if (lastStatus != mState) {
            if (mState == STATUS_PULL_TO_REFRESH) {
                description.setText(getResources().getString(R.string.pull_to_refresh));
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rotateArrow();
            } else if (mState == STATUS_RELEASE_TO_REFRESH) {
                description.setText(getResources().getString(R.string.release_to_refresh));
                arrow.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                rotateArrow();
            } else if (mState == STATUS_REFRESHING) {
                description.setText(getResources().getString(R.string.refreshing));
                progressBar.setVisibility(View.VISIBLE);
                arrow.clearAnimation();
                arrow.setVisibility(View.GONE);
            }
        }
    }

    /**
     * 根据当前的状态来旋转箭头。
     */
    private void rotateArrow() {
        float pivotX = arrow.getWidth() / 2f;
        float pivotY = arrow.getHeight() / 2f;
        float fromDegrees = 0f;
        float toDegrees = 0f;
        if (mState == STATUS_PULL_TO_REFRESH) {
            fromDegrees = 180f;
            toDegrees = 360f;
        } else if (mState == STATUS_RELEASE_TO_REFRESH) {
            fromDegrees = 0f;
            toDegrees = 180f;
        }
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
        animation.setDuration(100);
        animation.setFillAfter(true);
        arrow.startAnimation(animation);
    }

    /**
     * 根据当前ListView的滚动状态来设定 {@link #ableToPull}
     * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
     *
     * @param event
     */
    private void setIsAbleToPull(MotionEvent event) {
        if (listView == null) {
            ableToPull = false;
            return;
        }
        if (isListViewReachBottomEdge(listView) || listView.getCount() == 1) {
            if (!ableToPull) {
                mStartY = (int) event.getRawY();
            }
            // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
            ableToPull = true;
        } else {
            if (headerLayoutParams.bottomMargin != hideHeaderHeight) {
                headerLayoutParams.bottomMargin = hideHeaderHeight;
                header.setLayoutParams(headerLayoutParams);
            }
            ableToPull = false;
        }
    }

    public boolean isListViewReachBottomEdge(final ListView listView) {
        boolean result = false;
        if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
            final View bottomChildView = listView.getChildAt(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition());
            PictureAirLog.v("listView.getHeight()", String.valueOf(listView.getHeight()));
            PictureAirLog.v("bottomChildView.getBottom()", String.valueOf(bottomChildView.getBottom()));
            result = (listView.getHeight() >= bottomChildView.getBottom());
        }
        return result;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == VISIBLE) {
            loadOnce = false;
        }
    }
}
