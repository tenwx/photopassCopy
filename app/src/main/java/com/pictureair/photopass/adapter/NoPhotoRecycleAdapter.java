package com.pictureair.photopass.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PPinfo;

import java.util.ArrayList;

/**
 * Created by bauer_bao on 16/11/10.
 */

public class NoPhotoRecycleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private ArrayList<PPinfo> ppList;

    public NoPhotoRecycleAdapter(Context context, ArrayList<PPinfo> ppList) {
        this.context = context;
        this.ppList = ppList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.story_no_photo_item, parent, false);
        return new RecyclerItemViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (ppList.size() == 0 || position >= getItemCount()) {
            return;
        }

        if (viewHolder instanceof RecyclerItemViewHolder) {
            final RecyclerItemViewHolder recyclerViewHolder = (RecyclerItemViewHolder) viewHolder;
            String time = ppList.get(position).getShootDate();
            if (TextUtils.isEmpty(time)) {
                time = context.getString(R.string.today);
            }
            recyclerViewHolder.cardTimeTV.setText("(" + time + ")");
            recyclerViewHolder.cardNoTV.setText(String.format(context.getString(R.string.story_card), ppList.get(position).getPpCode()));

        }
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return ppList.size();
    }

    public class RecyclerItemViewHolder extends RecyclerView.ViewHolder {
        private TextView cardNoTV, cardTimeTV;

        public RecyclerItemViewHolder(View convertView) {
            super(convertView);
            cardNoTV = (TextView) convertView.findViewById(R.id.story_pp_card_no_tv);
            cardTimeTV = (TextView) convertView.findViewById(R.id.story_pp_card_time_tv);
        }
    }

}