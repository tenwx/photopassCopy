package com.pictureair.photopass.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by bauer_bao on 16/11/10.
 */

public class RecycleDividerItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public RecycleDividerItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;

        if (view.getPivotX() == 0)
            outRect.left = 0;

//
//
//        // Add top margin only for the first item to avoid double space between items
//        if(parent.getChildLayoutPosition(view) % 3 == 0)
//            outRect.left = 0;
    }
}
