package com.pictureair.photopass.widget;

import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;

import net.xpece.material.navigationdrawer.descriptors.AbsNavigationItemDescriptor;
import net.xpece.material.navigationdrawer.internal.ViewHolder;

/**
 * Created by pengwu on 16/11/29.
 */

public class PPDescriptor extends AbsNavigationItemDescriptor {

    @StringRes
    private int txt_dateId = 0;
    private String txt_date;

    @StringRes
    private int txt_numId = 0;
    private String txt_num;

    @StringRes
    private int txt_countId = 0;
    private String txt_count;

    @DrawableRes
    private int checked_drawable_id;
    @DrawableRes
    private int unCheck_drawable_id;

    private boolean mChecked = false;

    private DescriptorClickListener mListener;

    public PPDescriptor(int id, DescriptorClickListener listener) {
        super(id);
        this.mListener = listener;
    }

    @Override
    public int getLayoutId() {
        return R.layout.slide_list_item;
    }

    public PPDescriptor checked(boolean checked){
        this.mChecked = checked;
        return this;
    }

    public boolean ismChecked() {
        return mChecked;
    }

    public PPDescriptor date(String date) {
        this.txt_date = date;
        this.txt_dateId = 0;
        return this;
    }

    public PPDescriptor date(@StringRes int id) {
        this.txt_date = null;
        this.txt_dateId = id;
        return this;
    }

    public String getTxt_date(Context context) {
        if (txt_dateId != 0) {
            return context.getString(txt_dateId);
        } else {
            return txt_date;
        }
    }

    public PPDescriptor count(String count) {
        this.txt_count = count;
        this.txt_countId = 0;
        return this;
    }

    public PPDescriptor count(@StringRes int id) {
        this.txt_count = null;
        this.txt_countId = id;
        return this;
    }

    public String getTxt_count(Context context) {
        if (txt_countId != 0) {
            return context.getString(txt_countId);
        } else {
            return txt_count;
        }
    }

    public PPDescriptor num(String num) {
        this.txt_num = num;
        this.txt_numId = 0;
        return this;
    }

    public PPDescriptor num(@StringRes int id) {
        this.txt_num = null;
        this.txt_numId = id;
        return this;
    }

    public String getTxt_num(Context context) {
        if (txt_numId != 0) {
            return context.getString(txt_numId);
        } else {
            return txt_num;
        }
    }

    public PPDescriptor checkedDrawable(@DrawableRes int id) {
        this.checked_drawable_id = id;
        return this;
    }

    public PPDescriptor unCheckedDrawable(@DrawableRes int id) {
        this.unCheck_drawable_id = id;
        return this;
    }

    @Override
    public void bindView(View view, boolean selected) {
        super.bindView(view, false);
        setup(view);
    }

    private void setup(final View view) {
        Context context = view.getContext();
        ImageView img = ViewHolder.get(view, R.id.slide_list_item_select);
        img.setImageResource(mChecked ? checked_drawable_id : unCheck_drawable_id);
        TextView tv_date = ViewHolder.get(view, R.id.slide_list_item_date);
        tv_date.setText(getTxt_date(context));
        TextView tv_count = ViewHolder.get(view, R.id.slide_list_item_count);
        tv_count.setText(getTxt_count(context));
        TextView tv_Num = ViewHolder.get(view, R.id.slide_list_item_num);
        tv_Num.setText(getTxt_num(context));
    }

    @Override
    public boolean onClick(View view) {

        mChecked = !mChecked;
        ImageView img = ViewHolder.get(view, R.id.slide_list_item_select);
        img.setImageResource(mChecked ? checked_drawable_id : unCheck_drawable_id);
        if (mListener != null) {
            mListener.onItemClick(getId());
        }
        return true;
    }

    public interface DescriptorClickListener{

        public void onItemClick(int position);
    }
}
