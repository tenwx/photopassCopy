package com.pictureAir.adapter;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshPinnedSectionListView;
import com.pictureAir.R;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.entity.PhotoItemInfo;
import com.pictureAir.fragment.FragmentPageStory;
/**
 * 故事页面的ViewPager适配器
 * @author bauer_bao
 *
 */
public class StoryViewPagerAdapter extends PagerAdapter{
	private List<View> listViews;
	private PullToRefreshPinnedSectionListView pullToRefreshPinnedSectionAllListView;
	private PullToRefreshPinnedSectionListView pullToRefreshPinnedSectionPhotoPassListView;
	private PullToRefreshPinnedSectionListView pullToRefreshPinnedSectionMagicListView;
	private PullToRefreshPinnedSectionListView pullToRefreshPinnedSectionBoughtListView;
	private StoryPinnedListViewAdapter storyPinnedAllListViewAdapter;
	private StoryPinnedListViewAdapter storyPinnedPhotoPassListViewAdapter;
	private StoryPinnedListViewAdapter storyPinnedMagicListViewAdapter;
	private StoryPinnedListViewAdapter storyPinnedBoughtListViewAdapter;

	private Context context;
	private ArrayList<PhotoItemInfo> allArrayList;
	private ArrayList<PhotoItemInfo> photoPassArrayList;
	private ArrayList<PhotoItemInfo> magicArrayList;
	private ArrayList<PhotoItemInfo> boughtArrayList;
	private ArrayList<PhotoInfo> magicPhotoList;
	private final static String TAG = "StoryViewPagerAdapter";
	private Handler handler;
	
//	private Handler handler = new Handler(){
//		public void handleMessage(android.os.Message msg) {
//			Log.d(TAG, "refresh finish---------------->");
//
//			pullToRefreshPinnedSectionAllListView.onRefreshComplete();	
//		};
//	};
	
	public StoryViewPagerAdapter(Context context, List<View> list, ArrayList<PhotoItemInfo> allList, ArrayList<PhotoItemInfo> photoPassList, 
			ArrayList<PhotoItemInfo> magicList, ArrayList<PhotoItemInfo> boughtList, ArrayList<PhotoInfo> magicPhotoList, Handler handler) {
		listViews = list;
		this.context = context;
		allArrayList = allList;
		photoPassArrayList = photoPassList;
		magicArrayList = magicList;
		boughtArrayList = boughtList;
		this.magicPhotoList = magicPhotoList;
		this.handler = handler;
		Log.d(TAG, "arraylist size---->"+list.size());
		
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView(listViews.get(position));
	}

	@Override
	public int getCount() {
		return listViews.size();
	}
	
	//初始化页面
	@Override
	public Object instantiateItem(View container, int position) {
		Log.d(TAG, "instantiateItem----->" + position);
		switch (position) {
		case 0:
			pullToRefreshPinnedSectionAllListView = (PullToRefreshPinnedSectionListView)listViews.get(position).findViewById(R.id.pullToRefreshPinnedSectionListView);
			storyPinnedAllListViewAdapter = new StoryPinnedListViewAdapter(context, allArrayList, magicPhotoList);
//			pullToRefreshPinnedSectionAllListView.getRefreshableView().setFastScrollEnabled(true);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//				pullToRefreshPinnedSectionAllListView.getRefreshableView().setFastScrollAlwaysVisible(true);
//			}
			pullToRefreshPinnedSectionAllListView.setAdapter(storyPinnedAllListViewAdapter);
			pullToRefreshPinnedSectionAllListView.setOnRefreshListener(new PullToRefreshListener(position));
			break;
			
		case 1:
			pullToRefreshPinnedSectionPhotoPassListView = (PullToRefreshPinnedSectionListView)listViews.get(position).findViewById(R.id.pullToRefreshPinnedSectionListView);
			storyPinnedPhotoPassListViewAdapter = new StoryPinnedListViewAdapter(context, photoPassArrayList, magicPhotoList);
//			pullToRefreshPinnedSectionPhotoPassListView.getRefreshableView().setFastScrollEnabled(true);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//				pullToRefreshPinnedSectionPhotoPassListView.getRefreshableView().setFastScrollAlwaysVisible(true);
//			}
			pullToRefreshPinnedSectionPhotoPassListView.setAdapter(storyPinnedPhotoPassListViewAdapter);
			pullToRefreshPinnedSectionPhotoPassListView.setOnRefreshListener(new PullToRefreshListener(position));
			break;
			
		case 2:
			pullToRefreshPinnedSectionMagicListView = (PullToRefreshPinnedSectionListView)listViews.get(position).findViewById(R.id.pullToRefreshPinnedSectionListView);
			storyPinnedMagicListViewAdapter = new StoryPinnedListViewAdapter(context, magicArrayList, magicPhotoList);
//			pullToRefreshPinnedSectionMagicListView.getRefreshableView().setFastScrollEnabled(true);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//				pullToRefreshPinnedSectionMagicListView.getRefreshableView().setFastScrollAlwaysVisible(true);
//			}
			pullToRefreshPinnedSectionMagicListView.setAdapter(storyPinnedMagicListViewAdapter);
			pullToRefreshPinnedSectionMagicListView.setOnRefreshListener(new PullToRefreshListener(position));
			break;
			
		case 3:
			pullToRefreshPinnedSectionBoughtListView = (PullToRefreshPinnedSectionListView)listViews.get(position).findViewById(R.id.pullToRefreshPinnedSectionListView);
			storyPinnedBoughtListViewAdapter = new StoryPinnedListViewAdapter(context, boughtArrayList, magicPhotoList);
//			pullToRefreshPinnedSectionBoughtListView.getRefreshableView().setFastScrollEnabled(true);
//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
//				pullToRefreshPinnedSectionBoughtListView.getRefreshableView().setFastScrollAlwaysVisible(true);
//			}
			pullToRefreshPinnedSectionBoughtListView.setAdapter(storyPinnedBoughtListViewAdapter);
			pullToRefreshPinnedSectionBoughtListView.setOnRefreshListener(new PullToRefreshListener(position));
			break;

		default:
			break;
		}
//		listViews.get(position).setTag(position);
		((ViewPager) container).addView(listViews.get(position));
		return listViews.get(position);
	}

	@Override
	public int getItemPosition(Object object) {
//		View view = (View) object;
//		if ((Integer) view.getTag() == FragmentPageStory.currentIndex) {
//			
//		}
		return POSITION_NONE;
	}
	
	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}
	
	public void stopRefresh(int currentRefreshTab){
		switch (currentRefreshTab) {
		case 0:
			Log.d(TAG, "refresh finish---------------->" + currentRefreshTab);
			pullToRefreshPinnedSectionAllListView.onRefreshComplete();
			break;
			
		case 1:
			Log.d(TAG, "refresh finish---------------->" + currentRefreshTab);
			pullToRefreshPinnedSectionPhotoPassListView.onRefreshComplete();
			break;
			
		case 2:
			Log.d(TAG, "refresh finish---------------->" + currentRefreshTab);
			pullToRefreshPinnedSectionMagicListView.onRefreshComplete();
			break;
			
		case 3:
			Log.d(TAG, "refresh finish---------------->" + currentRefreshTab);
			pullToRefreshPinnedSectionBoughtListView.onRefreshComplete();
			break;

		default:
			break;
		}
		notifyDataSetChanged();
	}
	
	public void startRefresh(){
		
		switch (FragmentPageStory.getCurrentViewPager()) {
		case 0:
			Log.d(TAG, "refresh start---------------->" + 0);
			pullToRefreshPinnedSectionAllListView.setRefreshing();
			break;
			
		case 1:
			Log.d(TAG, "refresh start---------------->" + 1);
			pullToRefreshPinnedSectionPhotoPassListView.setRefreshing();
			break;
			
		case 2:
			Log.d(TAG, "refresh start---------------->" + 2);
			pullToRefreshPinnedSectionMagicListView.setRefreshing();
			break;
			
		case 3:
			Log.d(TAG, "refresh start---------------->" + 3);
			pullToRefreshPinnedSectionBoughtListView.setRefreshing();
			break;

		default:
			break;
		}
	}
	
	private class PullToRefreshListener implements OnRefreshListener<ListView>{
		private int index = 0;
		public PullToRefreshListener(int index) {
			this.index = index;
		}
		
		@Override
		public void onRefresh(PullToRefreshBase<ListView> refreshView) {
			// TODO Auto-generated method stub
			Log.d(TAG, "refresh ---------------->");
			Message message = handler.obtainMessage();
			message.what = 666;
			message.arg1 = index;
			handler.sendMessage(message);
		}
	}
}
