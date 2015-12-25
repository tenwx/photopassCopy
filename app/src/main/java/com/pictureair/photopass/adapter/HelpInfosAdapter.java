package com.pictureair.photopass.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.entity.HelpInfo;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;

import java.util.ArrayList;

/**
 * Created by bass on 15/12/16.
 */
public class HelpInfosAdapter extends BaseAdapter {
    private ArrayList<HelpInfo> listInfos;
    private Context context;
    private LayoutInflater inflater;

    public HelpInfosAdapter(ArrayList<HelpInfo> listInfos, Context context) {
        this.listInfos = listInfos;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return listInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return listInfos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        HelpView helpView = null;
        if (null == convertView) {
            helpView = new HelpView();
            convertView = inflater.inflate(R.layout.help_item, null);
            helpView.answer = (TextView) convertView.findViewById(R.id.tv_answer);
            helpView.question = (TextView) convertView.findViewById(R.id.tv_question);
            helpView.question.setTypeface(MyApplication.getInstance().getFontBold());
            convertView.setTag(helpView);
        } else {
            helpView = (HelpView) convertView.getTag();
        }
        //init data
        if ( MyApplication.getInstance().getLanguageType().equals(Common.SIMPLE_CHINESE)) {
            helpView.answer.setText(listInfos.get(position).getHelpAnswerCN());
            helpView.question.setText(listInfos.get(position).getHelpQuestionCN());
        } else {
            helpView.answer.setText(listInfos.get(position).getHelpAnswerEN());
            helpView.question.setText(listInfos.get(position).getHelpQuestionEN());
        }
        return convertView;
    }

    private class HelpView {
        TextView question, answer;
    }

}
