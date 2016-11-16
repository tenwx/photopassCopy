package com.pictureair.photopass.widget;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
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
        //获取当前的view在每一行中的第几个位置
        int index = ((GridLayoutManager.LayoutParams)view.getLayoutParams()).getSpanIndex();

        if (index != 0) {//如果不是第一个，则需要设置左边距
            outRect.left = space;
        }
    }
}
