package com.pictureair.photopassCopy.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by bauer_bao on 16/11/10.
 */

public class RecycleTopDividerItemDecoration extends RecyclerView.ItemDecoration {
    /**
     * 上边距
     */
    private int topSpace;

    public RecycleTopDividerItemDecoration(int topSpace) {
        this.topSpace = topSpace;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //获取当前的view的position
        if (parent.getChildLayoutPosition(view) == 0) {
            outRect.top = topSpace;
        } else {
            outRect.top = 0;
        }
    }
}
