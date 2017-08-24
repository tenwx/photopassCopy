package com.pictureair.hkdlphotopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.entity.Address;

import java.util.List;

/**
 * 地址选择适配器
 * Created by milo on 15/12/19.
 */
public class AddressAdapter extends BaseAdapter {
    private Context context;
    private List<Address> addresses;
    private ViewHolder viewHolder;
    private doOnClickAddressListener doOnClickAddressListener;

    public AddressAdapter(Context context, List<Address> addresses, doOnClickAddressListener listener) {
        this.context = context;
        this.addresses = addresses;
        this.doOnClickAddressListener = listener;

    }

    public void refresh(List<Address> list) {
        this.addresses = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return addresses.size();
    }

    @Override
    public Object getItem(int position) {
        return addresses.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item_address_list_layout, parent, false);
            viewHolder.addressTv = (TextView) convertView.findViewById(R.id.address_tv);
            viewHolder.addressIv = (ImageView) convertView.findViewById(R.id.address_iv);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (addresses == null || addresses.size() <= 0) {
            return convertView;
        }
        Address address = addresses.get(position);
        viewHolder.addressTv.setText(address.getAddress());
        if (address.getIsSelect()) {
            viewHolder.addressIv.setImageResource(R.drawable.sele);
        } else {
            viewHolder.addressIv.setImageResource(R.drawable.nosele);
        }
        viewHolder.addressIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doOnClickAddressListener.addressClick(position);
            }
        });


        return convertView;
    }

    public class ViewHolder {
        TextView addressTv;
        ImageView addressIv;
    }

    public interface doOnClickAddressListener {
        void addressClick(int position);
    }

}
