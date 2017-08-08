package com.pictureair.photopassCopy.widget;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by bauer_bao on 16/11/10.
 */

public class RecycleDividerItemDecoration extends RecyclerView.ItemDecoration {
    /**
     * 必须是3的倍数
     */
    private int space;

    public RecycleDividerItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //获取当前的view在每一行中的第几个位置
        int index = ((GridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();

        //设置均分
        switch (index) {
            //view宽 = （屏宽 - space * 2）/ 3
            case 0://第一列，靠左，因为宽度已经固定，因此，右边距可以不设置，右边距 = 屏宽/3 - view宽 = space * 2 / 3
                outRect.left = 0;
                break;

            case 1://第二列，居中显示，因此左右边距都是 space / 3
                outRect.left = space / 3;
                break;

            case 2://第三列，靠右显示，因此左边距为  space * 2 / 3，右边距为0，因为宽度固定，因此可以不用设置
                outRect.left = space * 2 / 3;
                break;
        }
    }
}
