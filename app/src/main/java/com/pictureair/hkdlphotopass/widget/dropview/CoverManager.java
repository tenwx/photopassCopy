package com.pictureair.hkdlphotopass.widget.dropview;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * 连接WaterDrop和DropCover的桥梁
 * @author bauer_bao
 *
 */
public class CoverManager {
    private static CoverManager mCoverManager;
    private DropCover mDropCover;
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams params;

    private CoverManager() {

    }

    public WindowManager getWindowManager() {
        return mWindowManager;
    }

    public static CoverManager getInstance() {
        if (mCoverManager == null) {
            mCoverManager = new CoverManager();
        }
        return mCoverManager;
    }

    /**
     * 初始化
     * @param activity
     */
    public void init(Activity activity) {
        if (mDropCover == null) {
            mDropCover = new DropCover(activity);
        }
        params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= 23) {
            params.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        }
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
    }

    /**
     * 开始拖动
     * @param target
     * @param x
     * @param y
     * @param onDragCompeteListener
     */
    public void start(View target, float x, float y, DropCover.OnDragCompeteListener onDragCompeteListener) {
        if (mDropCover != null && mDropCover.getParent() == null) {
            mDropCover.setOnDragCompeteListener(onDragCompeteListener);
        } else {
            return;
        }

        int[] locations = new int[2];
        target.getLocationInWindow(locations);
        if (mWindowManager == null) {
    		mWindowManager = (WindowManager) target.getContext().getSystemService(Context.WINDOW_SERVICE);
		}
        if (mDropCover == null) {
            mDropCover = new DropCover(target.getContext());
        }
        mWindowManager.addView(mDropCover, params);
        mDropCover.setBackgroundColor(Color.TRANSPARENT);
        mDropCover.start(target.getWidth(), target.getHeight(), locations[0], locations[1]);
        
    }

    /**
     * 更新移动
     * @param x
     * @param y
     */
    public void update(float x, float y) {
        mDropCover.update(x, y);
    }

    /**
     * 结束移动
     * @param x
     * @param y
     */
    public void finish(float x, float y) {
        mDropCover.finish(x, y);
    }
    
    /**
     * 销毁windowMagager
     */
    public void destroy(){
    	if (mWindowManager != null) {
			mWindowManager = null;
		}
    	if (mDropCover != null) {
    		mDropCover = null;
    	}
    }
}
