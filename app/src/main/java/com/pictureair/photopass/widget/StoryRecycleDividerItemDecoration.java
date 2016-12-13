package com.pictureair.photopass.widget;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by bauer_bao on 16/12/13.
 */

public class StoryRecycleDividerItemDecoration extends RecyclerView.ItemDecoration {
    /**
     * 必须是2的倍数
     */
    private int space;

    public StoryRecycleDividerItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        GridLayoutManager.LayoutParams layoutParams = (GridLayoutManager.LayoutParams) view.getLayoutParams();
        //获取当前的view在每一行中的第几个位置
        int lineIndex = layoutParams.getSpanIndex();
        //获取当前view占用多少spans
        int spanSize = layoutParams.getSpanSize();
        //获取当前view的索引值
        int position = parent.getChildAdapterPosition(view);

        //设置均分
        switch (lineIndex) {
            case 0:
                //第一列，靠左
                if (spanSize == 2) {//header
                    outRect.left = 0;

                } else {//item
                    outRect.left = space * 4;
                }
                break;

            case 1://第二列
                outRect.left = space;
                break;
        }

        //如果是第0个，说明是最顶上的header，不需要设置上边距
        if (position != 0) {
            outRect.top = space * 2;
        }
    }
}
