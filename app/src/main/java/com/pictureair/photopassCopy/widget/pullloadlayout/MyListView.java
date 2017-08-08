package com.pictureair.photopassCopy.widget.pullloadlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * Created by pengwu on 16/6/16.
 */

public class MyListView extends ListView{

    private Scroller mScroll;

    public MyListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroll = new Scroller(context);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroll.computeScrollOffset()){
            scrollTo(mScroll.getCurrX(),mScroll.getCurrY());
            invalidate();
        }
    }
}
