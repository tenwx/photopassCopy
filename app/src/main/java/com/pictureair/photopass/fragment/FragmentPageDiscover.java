package com.pictureair.photopass.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.alibaba.fastjson.JSON;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.adapter.DiscoverLocationAdapter;
import com.pictureair.photopass.entity.DiscoverItemInfo;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * 发现页面，显示各个地点的与当前的距离，可以筛选各个地方，可支持导航
 *
 * @author bauer_bao
 */
public class FragmentPageDiscover extends BaseFragment {


    private ListView discoverListView;
    private NoNetWorkOrNoCountView noNetWorkOrNoCountView;
    private SwipeRefreshLayout refreshLayout;

    private DiscoverLocationAdapter adapter;
    private List<DiscoverItemInfo> itemInfoList;

    //申明变量
    private static final String TAG = "FragmentPageDiscover";

    private final Handler fragmentPageDiscoverHandler = new FragmentPageDiscoverHandler(this);

    private static class FragmentPageDiscoverHandler extends Handler{
        private final WeakReference<FragmentPageDiscover> mActivity;

        public FragmentPageDiscoverHandler(FragmentPageDiscover activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (mActivity.get() == null) {
                return;
            }
            mActivity.get().dealHandler(msg);
        }
    }

    /**
     * 处理Message
     * @param msg
     */
    private void dealHandler(Message msg) {
        switch (msg.what) {
            default:
                break;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_discover,null);
        discoverListView = (ListView) view.findViewById(R.id.discover_listView);
        noNetWorkOrNoCountView = (NoNetWorkOrNoCountView) view.findViewById(R.id.discoverNoNetWorkView);
        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.discover_refresh_layout);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        refreshLayout.setEnabled(true);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.setRefreshing(false);
            }
        });

        for (int i = 0;i<5;i++) {
            DiscoverItemInfo itemInfo = new DiscoverItemInfo();
            itemInfo.imageId = R.drawable.ic_discover_loading;
            itemInfo.jumpUrl = "";
            itemInfo.mainViewText = "欢饮光临发现功能";
            itemInfo.mainViewUrl = Common.PHOTO_URL+itemInfo.imageId + i;
            itemInfo.updateTime = "上午 9:30";
            itemInfo.itemList = new ArrayList<DiscoverItemInfo.DiscoverListItemInfo>();
            for (int j = 0;j<8;j++) {
                DiscoverItemInfo.DiscoverListItemInfo listItemInfo = new DiscoverItemInfo().new DiscoverListItemInfo();
                listItemInfo.itemImageId = R.drawable.has_ppp_icon;
                listItemInfo.itemImageUrl = Common.PHOTO_URL + listItemInfo.itemImageId + i;
                listItemInfo.itemText = "这是展示，仅供参考！";
                listItemInfo.itemJumpUrl = "";
                itemInfo.itemList.add(listItemInfo);
            }
            if (itemInfoList == null) {
                itemInfoList = new ArrayList<DiscoverItemInfo>();
            }
            itemInfoList.add(itemInfo);
        }

        adapter = new DiscoverLocationAdapter(getContext(), itemInfoList);
        discoverListView.setAdapter(adapter);

        String str = JSON.toJSONString(itemInfoList);



//        ACache.get(MyApplication.getInstance()).put("discover",itemInfoList);


        return view;
    }


}