package com.pictureair.photopassCopy.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pictureair.photopassCopy.R;
import com.pictureair.photopassCopy.entity.SendAddress;

import java.util.List;

/**
 * 收货地址
 * Created by jiaxing on 16/5/19.
 */
public class SendAddressAdapter extends BaseAdapter {
    List<SendAddress> list;
    Context mContext;
    private int curIndex = -1;
    private boolean modifying;//是否正在修改中，正在修改中不允许点击,这样防止频繁点击ib造成多次请求卡顿。
    public SendAddressAdapter(Context context, List<SendAddress> arrayList) {
        mContext = context;
        this.list = arrayList;
        setCurIndex();
    }

    @Override
    public int getCount() {
        return null!=list?list.size():0;
    }

    @Override
    public Object getItem(int position) {
        return null!=list?list.get(position):null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if(convertView==null){
            convertView=View.inflate(mContext, R.layout.item_add_list_address,null);
            holder=new ViewHolder();
            holder.ib= (ImageButton) convertView.findViewById(R.id.item_address_ib);
            holder.nameTv= (TextView) convertView.findViewById(R.id.item_address_tv_name);
            holder.phoneTv= (TextView) convertView.findViewById(R.id.item_address_tv_phone);
            holder.addressTv= (TextView) convertView.findViewById(R.id.item_address_tv);
            holder.btnModi= (Button) convertView.findViewById(R.id.item_address_btn_modi);
            convertView.setTag(holder);
        }else{
            holder= (ViewHolder) convertView.getTag();
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!modifying) {
                    if (null != itemListener && position != curIndex) {
                        refreshCurrentIndex(position);
                        modifying=true;
                        itemListener.clickItem(position, list.get(position));
                    }
                }
            }
        });
        if(list.get(position).isSelected()){
            holder.ib.setImageResource(R.drawable.invoice_press);
        }else{
            holder.ib.setImageResource(R.drawable.invoice_nor);
        }
        holder.btnModi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null!=itemListener)
                    itemListener.editItem(position);
            }
        });
        holder.nameTv.setText(list.get(position).getName());
        holder.phoneTv.setText(list.get(position).getMobilePhone());
        holder.addressTv.setText(getDisplayAddress(position));
        return convertView;
    }

    public String getDisplayAddress(int pos){
        return list.get(pos).getProvince()+list.get(pos).getCity()+list.get(pos).getCountry()+list.get(pos).getDetailAddress();
    }

    //把position设置为当前选中状态和非选中状态切换，并把其他的取消选中
    public void refreshCurrentIndex(int position){

        for(int i=0;i<getCount();i++) {
            if (i == position) {
                list.get(i).setSelected(true);
                curIndex=position;
            } else {
                list.get(i).setSelected(false);
            }
        }
        //notifyDataSetChanged();
    }

    //设置Position为选中，并把其他的取消选中
    public void setCurrentIndex(int position){
        for(int i=0;i<getCount();i++) {
            if (i == position) {
                list.get(i).setSelected(true);
                curIndex=position;
            } else {
                list.get(i).setSelected(false);
            }
        }
    }

    public void setModifying(boolean modify){
        modifying = modify;
    }

    public int getCurIndex(){
        return curIndex;
    }

    //设置当前选中项index
    public void setCurIndex(){
        if(null != list && list.size()>0){
            for(int i=0;i<list.size();i++){
                if(list.get(i).isSelected())
                {
                    curIndex=i;
                    break;
                }
            }
        }
    }

    //当被选择项删除时选择第一项
    public void selectDefaultIndex(int delPosition){
        if(null != list && list.size()>0){
            if(delPosition == curIndex) {
                curIndex = 0;
                list.get(0).setSelected(true);
            }
        }else
            curIndex=-1;
    }
    public interface AddressItemListener{
        void editItem(int position);
        void clickItem(int position, SendAddress address);
    }

    public AddressItemListener itemListener;

    public void setAddressItemListener(AddressItemListener listener){
        this.itemListener=listener;
    }
    public static class ViewHolder{
        public TextView nameTv;
        public ImageButton ib;
        public TextView phoneTv;
        public TextView addressTv;
        public Button btnModi;
    }
}
