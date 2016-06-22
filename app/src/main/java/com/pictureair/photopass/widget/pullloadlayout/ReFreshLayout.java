package com.pictureair.photopass.widget.pullloadlayout;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;
import com.pictureair.photopass.R;

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
    private int touchSlop;
    private MyListView listView;
    private int lastStatus = mState;
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

    public ReFreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        header = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh,null);
        progressBar = (ProgressBar) header.findViewById(R.id.progress_bar);
        arrow = (ImageView) header.findViewById(R.id.arrow);
        description = (TextView) header.findViewById(R.id.description);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        addView(header,0);
        mIsLoading = false;
        header.setVisibility(GONE);
    }
    private boolean loadOnce;
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce && listView != null) {
            hideHeaderHeight = -header.getHeight();
            LayoutParams lp = (LayoutParams)(header.getLayoutParams());
            lp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            header.setLayoutParams(lp);
            headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
            headerLayoutParams.bottomMargin = hideHeaderHeight;
            loadOnce = true;
        }

    }

    public void setFootViewVisibility(int visibility) {
        header.setVisibility(visibility);
    }

    public void setOnRefreshListener(OnRefreshListener listener) {
        this.listener = listener;
    }

    public void removeRefreshListener() {
        this.listener = null;
    }

    public OnRefreshListener getOnRefreshListener(){
        return this.listener;
    }

    public void setListView (MyListView lv) {
        this.listView = lv;
    }

    private void translationY(View child,float distance){
        if (child != null) {
            ViewHelper.setTranslationY(child, distance);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (listView == null) {
            return  super.dispatchTouchEvent(ev);
        }
        boolean handle = super.dispatchTouchEvent(ev);
        handle |= onTouchEvent(ev);
        return handle;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        setIsAbleToPull(ev);
        if (ableToPull && !mIsLoading && listView != null) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mStartY = ev.getRawY();
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float tmpY = ev.getRawY();
                    float space = mStartY - tmpY;
                    if (space <= 0 && headerLayoutParams.bottomMargin <= hideHeaderHeight) {
                        return false;
                    }
                    if (space < touchSlop) {
                        return false;
                    }
                    if (mState != STATUS_REFRESHING) {
                        if (headerLayoutParams.bottomMargin > 0) {
                            mState = STATUS_RELEASE_TO_REFRESH;
                        }else{
                            mState = STATUS_PULL_TO_REFRESH;
                        }
                    }
                    if (!listView.getIsParentMoving()) {
                        listView.setIsParentMoving(true);
                    }
                    if (headerLayoutParams.bottomMargin < 10) {
                        translationY(listView, -space / 2);
                        headerLayoutParams.bottomMargin = (int) ((space / 2) + hideHeaderHeight);
                        header.setLayoutParams(headerLayoutParams);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (mState == STATUS_RELEASE_TO_REFRESH) {
                        // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                        new RefreshingTask().execute();
                        Log.e("Ontouc","STATUS_RELEASE_TO_REFRESH");
                    } else if (mState == STATUS_PULL_TO_REFRESH) {
                        // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
                        new HideHeaderTask().execute();
                    }
                    listView.setIsParentMoving(false);
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
    /**
     * 下拉头部回滚的速度
     */
    public static final int SCROLL_SPEED = -20;

    /**
     * 正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器。
     *
     * @author guolin
     */
    class RefreshingTask extends AsyncTask<Void, Integer, Void> {
        boolean moved = false;
        private void sleep(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected Void doInBackground(Void... params) {
            int bottomMargin = headerLayoutParams.bottomMargin;
            int transY = 0;
            while (true) {
                bottomMargin = bottomMargin + SCROLL_SPEED;
                if (bottomMargin <= 0) {
                    transY = SCROLL_SPEED - bottomMargin;
                    bottomMargin = 0;
                    break;
                }
                publishProgress(bottomMargin,SCROLL_SPEED);
            }
            mState = STATUS_REFRESHING;
            publishProgress(0,transY);
            if (listener != null) {
                mIsLoading = true;
                listener.onRefresh();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... bottomMargin) {
            updateHeaderView();
            headerLayoutParams.bottomMargin = bottomMargin[0];
            header.setLayoutParams(headerLayoutParams);
            if (bottomMargin[0] != 0) {
                translationY(listView, SCROLL_SPEED);
            } else {
                translationY(listView, bottomMargin[1]);
            }
        }
    }

    /**
     * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
     *
     * @author guolin
     */
    class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {
        private void sleep(int time) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        @Override
        protected Integer doInBackground(Void... params) {
            int bottomMargin = headerLayoutParams.bottomMargin;
            while (true) {
                bottomMargin = bottomMargin + SCROLL_SPEED;
                if (bottomMargin <= hideHeaderHeight) {
                    bottomMargin = hideHeaderHeight;
                    break;
                }
                publishProgress(bottomMargin,SCROLL_SPEED);
                sleep(10);
            }
            return bottomMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... bottomMargin) {
            headerLayoutParams.bottomMargin = bottomMargin[0];
            header.setLayoutParams(headerLayoutParams);
            if (bottomMargin[0] != hideHeaderHeight) {
                translationY(listView,-SCROLL_SPEED);
            }else{
                translationY(listView,0);
            }
        }

        @Override
        protected void onPostExecute(Integer bottomMargin) {
            headerLayoutParams.bottomMargin = bottomMargin;
            header.setLayoutParams(headerLayoutParams);
            mState = STATUS_REFRESH_FINISHED;
            mIsLoading = false;
        }
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        mState = STATUS_REFRESH_FINISHED;
        translationY(listView,0);
        headerLayoutParams.bottomMargin = hideHeaderHeight;
        header.setLayoutParams(headerLayoutParams);
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
                mStartY = (int)event.getRawY();
            }
            // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
            ableToPull = true;
        }else {
            if (headerLayoutParams.bottomMargin != hideHeaderHeight) {
                headerLayoutParams.bottomMargin = hideHeaderHeight;
                header.setLayoutParams(headerLayoutParams);
            }
            ableToPull = false;
        }
    }

    public boolean isListViewReachBottomEdge(final ListView listView) {
        boolean result=false;
        if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
            final View bottomChildView = listView.getChildAt(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition());
            result= (listView.getHeight()>=bottomChildView.getBottom());
        }
        return  result;
    }
}
