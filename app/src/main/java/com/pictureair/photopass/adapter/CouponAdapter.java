package com.pictureair.photopass.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.util.CouponTool;

import java.util.List;

/**
 * Created by bass on 16/3/11.
 */
public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.MyViewHolder> {
    private LayoutInflater mInflater;
    private Context mContext;
    private List<CouponInfo> mDatas;
    private String whatPage="";

    public CouponAdapter(Context context, List<CouponInfo> datas) {
        this.mContext = context;
        this.mDatas = datas;
        mInflater = LayoutInflater.from(context);
    }

    /**
     * 设置是从什么界面进来的
     * @param whatPage
     */
    public void setPage(String whatPage) {
        this.whatPage = whatPage;
    }

    /**
     * 设置数据
     * @param datas
     */
    public void setDatas(List<CouponInfo> datas){
        this.mDatas = datas;
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    /**
     * 绑定ViewHolder
     */
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        if (whatPage.equals(CouponTool.ACTIVITY_ORDER)){//如果是订单页面进来的，显示选择的框
            holder.ivSelect.setVisibility(View.VISIBLE);
        }else{
            holder.ivSelect.setVisibility(View.INVISIBLE);
        }
        holder.tvName.setText(mDatas.get(position).getCpName());
        holder.tvNumber.setText(mDatas.get(position).getCpNumber()+"");//这里是价格或者是折扣需要单位
        holder.tvDescribe.setText(mDatas.get(position).getCpDescribe());
        holder.tvValiditPeriod.setText(mDatas.get(position).getCpValidityPeriod());
        holder.tvStatus.setText(mDatas.get(position).getCpStatus()+"");
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.coupon_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvNumber;
        TextView tvDescribe;
        TextView tvValiditPeriod;
        TextView tvStatus;
        ImageView ivSelect;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvNumber = (TextView) itemView.findViewById(R.id.tv_number);
            tvDescribe = (TextView) itemView.findViewById(R.id.tv_describe);
            tvValiditPeriod = (TextView) itemView.findViewById(R.id.tv_validity_period);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            ivSelect = (ImageView)itemView.findViewById(R.id.iv_select);
        }
    }
}
