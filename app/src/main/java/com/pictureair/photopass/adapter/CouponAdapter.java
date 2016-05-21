package com.pictureair.photopass.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.CouponInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.CouponTool;

import java.util.List;

/**
 * Created by bass on 16/3/11.
 */
public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.MyViewHolder> implements View.OnClickListener {
    private LayoutInflater mInflater;
    private Context mContext;
    private List<CouponInfo> mDatas;
    private String whatPage = "";
    private String currency = "";

    public CouponAdapter(Context context, List<CouponInfo> datas) {
        this.mContext = context;
        this.mDatas = datas;
        mInflater = LayoutInflater.from(context);
        currency = context.getSharedPreferences(Common.SHARED_PREFERENCE_USERINFO_NAME,Context.MODE_PRIVATE).getString("currency","");
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
        if (whatPage.equals(CouponTool.ACTIVITY_ORDER)){//订单页面进来的，显示选择的框.active可使用
            if (mDatas.get(position).getCpStatus().equals("active")){
                holder.ivSelect.setVisibility(View.VISIBLE);
                /**
                 * 如果选中，显示打勾
                 */
                if (mDatas.get(position).getCpIsSelect()){
                    holder.ivSelect.setImageResource(R.drawable.sele);
                }else{
                    holder.ivSelect.setImageResource(R.drawable.nosele);
                }
            }else{//不可使用或者已经过期
                holder.ivSelect.setVisibility(View.INVISIBLE);
            }
        }else{//me 界面进来的
            holder.ivSelect.setVisibility(View.INVISIBLE);
        }

        /**
         * 使用情况
         */
        if (mDatas.get(position).getCpStatus().equals("active")){//可使用
            setMyTextViewColor2(holder);
            holder.tvStatus.setText("");
            holder.rlRight.setBackgroundResource(R.drawable.coupon_2);
        }else if (mDatas.get(position).getCpStatus().equals("used")){//已使用
            setMyTextViewColor(holder);
            holder.tvStatus.setText(R.string.has_been_used);
            holder.rlRight.setBackgroundResource(R.drawable.coupon_3);
        }else if (mDatas.get(position).getCpStatus().equals("failure")){//已过期
            setMyTextViewColor(holder);
            holder.tvStatus.setText(R.string.expired);
            holder.rlRight.setBackgroundResource(R.drawable.coupon_3);
        }else{

        }

        /**
         * 显示价格 还是折扣
         */
        if (mDatas.get(position).getCpType().equals("discount")){//折扣
//            holder.tvNumber.setText((int)mDatas.get(position).getCpNumber()+ mContext.getResources().getString(R.string.discount));//这里是价格或者是折扣需要单位
        }else if (mDatas.get(position).getCpType().equals("full")){//满
            //目前不知如何显示
//            holder.tvNumber.setText((int)mDatas.get(position).getCpNumber()+ mContext.getResources().getString(R.string.discount));//这里是价格或者是折扣需要单位
        }else if (mDatas.get(position).getCpType().equals("subtract")){//减
            holder.tvNumber.setText(currency+" -"+(int)mDatas.get(position).getCpNumber());//这里是价格或者是折扣需要单位
        }
        holder.tvName.setText(mDatas.get(position).getCpName());
        holder.tvDescribe.setText(mDatas.get(position).getCpDescribe());
        holder.tvValiditPeriod.setText(String.format(mContext.getResources().getString(R.string.coupon_expireto), mDatas.get(position).getCpValidityPeriod()));

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

    /**
     * 设置颜色
     * @param holder
     */
    private void setMyTextViewColor(MyViewHolder holder){
        holder.tvName.setTextColor(mContext.getResources().getColor(R.color.pp_dark_blue));
        holder.tvNumber.setTextColor(mContext.getResources().getColor(R.color.pp_dark_blue));
        holder.tvDescribe.setTextColor(mContext.getResources().getColor(R.color.pp_dark_blue));
        holder.tvValiditPeriod.setTextColor(mContext.getResources().getColor(R.color.pp_dark_blue));
        holder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.pp_light_gray_normal));
    }

    /**
     * 设置可用优惠卷的字体颜色
     * @param holder
     */
    private void setMyTextViewColor2(MyViewHolder holder){
        holder.tvName.setTextColor(mContext.getResources().getColor(R.color.pp_dark_blue));
        holder.tvNumber.setTextColor(mContext.getResources().getColor(R.color.gray));
        holder.tvDescribe.setTextColor(mContext.getResources().getColor(R.color.pp_dark_blue));
        holder.tvValiditPeriod.setTextColor(mContext.getResources().getColor(R.color.pp_gray));
        holder.tvStatus.setTextColor(mContext.getResources().getColor(R.color.white));
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;
        TextView tvNumber;
        TextView tvDescribe;
        TextView tvValiditPeriod;
        TextView tvStatus;
        ImageView ivSelect;
        RelativeLayout rlRight;

        public MyViewHolder(View itemView) {
            super(itemView);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            tvNumber = (TextView) itemView.findViewById(R.id.tv_number);
            tvDescribe = (TextView) itemView.findViewById(R.id.tv_describe);
            tvValiditPeriod = (TextView) itemView.findViewById(R.id.tv_validity_period);
            tvStatus = (TextView) itemView.findViewById(R.id.tv_status);
            ivSelect = (ImageView)itemView.findViewById(R.id.iv_select);
            tvStatus.setRotation(30);
            tvNumber.setTypeface(MyApplication.getInstance().getFontBold());
            rlRight = (RelativeLayout)itemView.findViewById(R.id.rl_right);
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
