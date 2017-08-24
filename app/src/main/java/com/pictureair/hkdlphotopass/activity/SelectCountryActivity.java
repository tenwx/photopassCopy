package com.pictureair.hkdlphotopass.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.util.AppManager;
import com.pictureair.hkdlphotopass.widget.country.CharacterParser;
import com.pictureair.hkdlphotopass.widget.country.PinyinComparator;
import com.pictureair.hkdlphotopass.widget.country.SideBar;
import com.pictureair.hkdlphotopass.widget.country.SortAdapter;
import com.pictureair.hkdlphotopass.widget.country.SortModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 选择国家页面
 * bass
 */
public class SelectCountryActivity extends BaseActivity {
    private TextView tvHead;
    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;
    private SortAdapter adapter;
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;
    private PinyinComparator pinyinComparator;

    /**
     * 请求此类的activity
     */
    public static final int requestCountry = 1000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_country);
        AppManager.getInstance().addActivity(this);
        initViews();
    }

    private void initViews() {
        //实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);
        tvHead = (TextView) findViewById(R.id.filter_tv);
        sortListView = (ListView) findViewById(R.id.country_lvcountry);

        LinearLayout topLeftView = (LinearLayout) this.findViewById(R.id.topLeftView);
        ImageView topLeftIv = (ImageView) this.findViewById(R.id.topLeft_iv);
        topLeftIv.setImageResource(R.drawable.back_blue);
        topLeftIv.setVisibility(View.VISIBLE);
        ((TextView) this.findViewById(R.id.topTitle)).setText(R.string.select_a_country);
        topLeftView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sideBarOntouch();
        listviewOnItemClick();
        listviewOnScroll();

        SourceDateList = filledData(getResources().getStringArray(R.array.smssdk_country));

        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new SortAdapter(this, SourceDateList);
        sortListView.setAdapter(adapter);
    }

    private void listviewOnItemClick() {
        sortListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                String[] strs = getCountryStr(((SortModel) adapter.getItem(position)).getName(), SelectCountryActivity.this);
                //"国家名称：" + strs[0] + "\n" + "国家区号：" + strs[1] + "\n" + "国家简码：" + strs[4]
                Intent intent = new Intent();
                intent.putExtra("country", strs);
                setResult(requestCountry, intent);
                finish();

            }
        });
    }

    private void listviewOnScroll() {
        sortListView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem != 0) {
                    if (null != adapter) {
                        tvHead.setVisibility(View.VISIBLE);
                        //根据position获取分类的首字母的char ascii值
                        int section = adapter.getSectionForPosition(firstVisibleItem);
                        //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
                        String aa = adapter.getPositionForChat(section);
                        tvHead.setText(aa);
                    }
                }

            }
        });
    }

    private void sideBarOntouch() {
        //设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {

            public void onTouchingLetterChanged(String s) {
                //该字母首次出现的位置
                int position = adapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    tvHead.setText(s.charAt(0) + "");
                    sortListView.setSelection(position);
                }

            }
        });
    }

    /**
     * 为ListView填充数据
     *
     * @param date
     * @return
     */
    private List<SortModel> filledData(String[] date) {
        List<SortModel> mSortList = new ArrayList<>();

        for (int i = 0; i < date.length; i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(date[i]);
            //汉子转换拼音
            String pinyin = characterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
//				sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * 国家名称 ［0］
     * 电话区号 ［1］
     * 国家简码  [4]
     */
    public String[] getCountryStr(String str, Context context) {
        String[] codeStrings;
        codeStrings = str.split(",");
        return codeStrings;
    }

    @Override
    protected void onDestroy() {
        AppManager.getInstance().killActivity(this);
        setResult(0);
        super.onDestroy();
    }

    //	@Override
//	public void TopViewClick(View view) {
//		super.TopViewClick(view);
//		switch (view.getId()) {
//			case R.id.topLeftView:
//				finish();
//				break;
//			default:
//				break;
//		}
//	}

}
