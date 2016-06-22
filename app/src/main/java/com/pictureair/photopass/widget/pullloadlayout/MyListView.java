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
import android.widget.TextView;

import com.pictureair.photopass.R;

/**
 * Created by pengwu on 16/6/16.
 */

public class MyListView extends ListView{

    boolean mIsParentMoving = false;
    private View mfoot;
    private ProgressBar progressBar;
    private ImageView arrow;
    private TextView description;
    private int touchSlop;
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
     * 下拉头部回滚的速度
     */
    public static final int SCROLL_SPEED = -20;
    /**
     * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
     */
    private boolean ableToPull;
    private int hideHeaderHeight;
    private int mState = STATUS_REFRESH_FINISHED;
    private int lastStatus = mState;
    private boolean loadOnce;
    private float mStartY;
    private OnRefreshListener listener;

    private boolean mIsLoading;
    private boolean mNotAllowRefresh;
    private int mBottomPadding;

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mfoot = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh,null);
        progressBar = (ProgressBar) mfoot.findViewById(R.id.progress_bar);
        arrow = (ImageView) mfoot.findViewById(R.id.arrow);
        description = (TextView) mfoot.findViewById(R.id.description);
        addFooterView(mfoot);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mfoot.setVisibility(GONE);
    }
    public void setIsParentMoving (boolean move){
        mIsParentMoving = move;
    }

    public boolean getIsParentMoving(){
        return mIsParentMoving;
    }

    public void setFootViewVisibility(int visibility) {
        mfoot.setVisibility(visibility);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce && mfoot.getVisibility() == VISIBLE) {
            mfoot.measure(0,0);
            hideHeaderHeight = -mfoot.getMeasuredHeight();
            loadOnce = true;
            mfoot.setPadding(0,0,0,hideHeaderHeight);

        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mfoot.getVisibility() != VISIBLE) {
            return super.dispatchTouchEvent(ev);
        }
        boolean handled = super.dispatchTouchEvent(ev);
        handled |= onTouchEvent(ev);
        return handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mfoot.getVisibility() != VISIBLE) {
            if (mIsParentMoving) {
                if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                    return false;
                }
            }
            return super.onTouchEvent(ev);
        }else {
            setIsAbleToPull(ev);
            if (ableToPull && !mIsLoading && !mNotAllowRefresh) {
                switch (ev.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mStartY = ev.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float tmpY = ev.getRawY();
                        float space = mStartY - tmpY;
                        if (space <= 0 && mfoot.getPaddingBottom() <= hideHeaderHeight) {
                            return false;
                        }
                        if (space < touchSlop) {
                            return false;
                        }
                        if (mState != STATUS_REFRESHING) {
                            if (mfoot.getPaddingBottom() > 0) {
                                mState = STATUS_RELEASE_TO_REFRESH;
                            } else {
                                mState = STATUS_PULL_TO_REFRESH;
                            }
                        }
                        int bottomPadding = mfoot.getPaddingBottom() + (int) (space / 2);
                        if (mfoot.getPaddingBottom() < 10) {
                            mfoot.setPadding(0, 0, 0, bottomPadding);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                    default:
                        if (mState == STATUS_RELEASE_TO_REFRESH) {
                            // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                            mBottomPadding = mfoot.getPaddingBottom();
                            new RefreshingTask().execute();
                        } else if (mState == STATUS_PULL_TO_REFRESH) {
                            // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
                            mBottomPadding = mfoot.getPaddingBottom();
                            new HideHeaderTask().execute();
                        }
                        break;
                }
                // 时刻记得更新下拉头中的信息
                if (mState == STATUS_PULL_TO_REFRESH
                        || mState == STATUS_RELEASE_TO_REFRESH) {
                    updateHeaderView();
                    Log.e("touch","updateView"+String.valueOf(mState));
                    lastStatus = mState;
                    // 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
                    return true;
                }
            }
        }
        return super.onTouchEvent(ev);
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

    public boolean ismNotAllowRefresh() {
        return mNotAllowRefresh;
    }

    public void setmNotAllowRefresh(boolean mNotAllowRefresh) {
        this.mNotAllowRefresh = mNotAllowRefresh;
    }

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
            int bottomPadding = mBottomPadding;
            while (true) {
                bottomPadding = bottomPadding + SCROLL_SPEED;
                if (bottomPadding <= 0) {
                    bottomPadding = 0;
                    break;
                }
                publishProgress(bottomPadding);
            }
            mState = STATUS_REFRESHING;
            publishProgress(0);
            if (listener != null) {
                mIsLoading = true;
                listener.onRefresh();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... bottomMargin) {
            updateHeaderView();
            Log.e("RefreshingTask","updateView"+String.valueOf(mState));
            mfoot.setPadding(0,0,0,bottomMargin[0]);
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
            int bottomPadding = mBottomPadding;
            while (true) {
                bottomPadding = bottomPadding + SCROLL_SPEED;
                if (bottomPadding <= hideHeaderHeight) {
                    bottomPadding = hideHeaderHeight;
                    break;
                }
                publishProgress(bottomPadding);
                sleep(10);
            }
            return bottomPadding;
        }

        @Override
        protected void onProgressUpdate(Integer... bottomMargin) {
            mfoot.setPadding(0,0,0,bottomMargin[0]);
        }

        @Override
        protected void onPostExecute(Integer bottomMargin) {
            mfoot.setPadding(0,0,0,bottomMargin);
            mState = STATUS_REFRESH_FINISHED;
            mIsLoading = false;
        }
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        mState = STATUS_REFRESH_FINISHED;
        mfoot.setPadding(0,0,0,hideHeaderHeight);
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
        if (isListViewReachBottomEdge(this)) {
            if (!ableToPull) {
                mStartY = (int)event.getRawY();
            }
            ableToPull = true;
        }else {
            ableToPull = false;
        }
    }

    public boolean isListViewReachBottomEdge(final ListView listView) {
        boolean result=false;
        if (listView.getLastVisiblePosition() == (listView.getCount() - 1)) {
            final View bottomChildView = listView.getChildAt(listView.getLastVisiblePosition() - listView.getFirstVisiblePosition());
            result= (listView.getHeight()>=bottomChildView.getBottom());
        };
        return  result;
    }
}
