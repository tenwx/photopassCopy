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
public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.MyViewHolder> implements View.OnClickListener {
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

        /**
         * 如果选中，显示打勾
         */
        if (mDatas.get(position).getCpIsSelect()){
            holder.ivSelect.setVisibility(View.VISIBLE);
        }else{
            holder.ivSelect.setVisibility(View.INVISIBLE);
        }

        holder.tvName.setText(mDatas.get(position).getCpName());
        holder.tvNumber.setText(mDatas.get(position).getCpNumber()+"");//这里是价格或者是折扣需要单位
        holder.tvDescribe.setText(mDatas.get(position).getCpDescribe());
        holder.tvValiditPeriod.setText(mDatas.get(position).getCpValidityPeriod());
        holder.tvStatus.setText(mDatas.get(position).getCpStatus() + "");
        //将数据保存在itemView的Tag中，以便点击时进行获取
        holder.itemView.setTag(mDatas.get(position));
    }

    /**
     * 创建ViewHolder
     */
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.coupon_item, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        //将创建的View注册点击事件
        view.setOnClickListener(this);
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

    /**
     * 设置监听
     */
    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view , CouponInfo data);
    }
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;
    @Override
    public void onClick(View v) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(v,(CouponInfo)v.getTag());
        }
    }
    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

}
