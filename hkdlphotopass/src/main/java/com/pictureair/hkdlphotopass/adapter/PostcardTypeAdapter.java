package com.pictureair.hkdlphotopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.R;


/**
 * Created by Eric on 16/4/18.
 */

public class PostcardTypeAdapter extends BaseAdapter {

    private Context mContext;
    private ViewHold viewHold;


    public PostcardTypeAdapter(Context context) {
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {

            viewHold = new ViewHold();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.postcard_type_item, null);
            viewHold.tvPostcardType = (TextView) convertView.findViewById(R.id.postcard_type_item);
            convertView.setTag(viewHold);

        } else {
            viewHold = (ViewHold) convertView.getTag();
        }

        return convertView;
    }

    public class ViewHold {

        TextView tvPostcardType;
    }
}
