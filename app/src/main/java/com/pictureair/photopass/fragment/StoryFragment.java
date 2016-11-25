package com.pictureair.photopass.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.ADVideoDetailProductActivity;
import com.pictureair.photopass.activity.PreviewPhotoActivity;
import com.pictureair.photopass.adapter.StickyRecycleAdapter;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.StoryFragmentEvent;
import com.pictureair.photopass.eventbus.StoryRefreshEvent;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.CustomTextView;
import com.pictureair.photopass.widget.PWStickySectionRecyclerView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;


public class StoryFragment extends Fragment implements PWStickySectionRecyclerView.OnPullListener{
	private static final String TAG = "StoryFragment";
	private RelativeLayout noPhotoRelativeLayout;
	private PWStickySectionRecyclerView pwStickySectionRecyclerView;
	private TextView noPhotoTextView;
	private ArrayList<PhotoInfo> targetArrayList;
	private ArrayList<PhotoInfo> photoInfoArrayList;
	private int tab;
	private int oldCount;
	private View view;
	private SwipeRefreshLayout refreshLayout;
	private CustomTextView tvStickyHeaderView;
	private static Handler handler;

	private static final int REFRESH = 666;
	public static final int LOAD_MORE = 77777;
	private String[] tabName = {"all", "photopass", "local", "bought", "favourite"};
	
	public static StoryFragment getInstance(ArrayList<PhotoInfo> photoInfoArrayList, ArrayList<PhotoInfo> targetArrayList, int tab, Handler h){
		handler = h;
		StoryFragment storyFragment = new StoryFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList("photo", photoInfoArrayList);
		bundle.putParcelableArrayList("target", targetArrayList);
		bundle.putInt("tab", tab);
		storyFragment.setArguments(bundle);
		return storyFragment;
	}
	
	public StoryFragment() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onAttach(Context context) {
		// TODO Auto-generated method stub
		if (getArguments() != null) {
			photoInfoArrayList = getArguments().getParcelableArrayList("photo");
			targetArrayList = getArguments().getParcelableArrayList("target");
			tab = getArguments().getInt("tab");
			oldCount = photoInfoArrayList.size();
		}
		super.onAttach(context);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.story_pinned_list, container, false);
		}
		PictureAirLog.d("fragment init create");
		noPhotoRelativeLayout = (RelativeLayout) view.findViewById(R.id.no_photo_relativelayout);
		noPhotoTextView = (TextView) view.findViewById(R.id.no_photo_textView);
		tvStickyHeaderView = (CustomTextView) view.findViewById(R.id.section_time);
		pwStickySectionRecyclerView = (PWStickySectionRecyclerView) view.findViewById(R.id.pw_sticky_section_recyclerview);

		tvStickyHeaderView.setTypeface(MyApplication.getInstance().getFontBold());

		refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        refreshLayout.setEnabled(true);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				PictureAirLog.out("start refresh");
				if (photoInfoArrayList.size() != 0 && !pwStickySectionRecyclerView.isLoadMore()) {

					Message message = handler.obtainMessage();
					message.what = REFRESH;
					message.arg1 = tab;
					handler.sendMessage(message);
				}
			}
		});

		pwStickySectionRecyclerView.setOnRecyclerViewItemClickListener(new PhotoOnItemClickListener());
		pwStickySectionRecyclerView.setOnPullListener(this);

		ViewGroup parent = (ViewGroup) view.getParent();
		if (parent != null) {
			parent.removeView(view);
		}

		return view;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		/**
		 * airpass需要统计用户图片数量
		 */
		if (tab == 1) {
			int countLocal = SPUtils.getInt(getContext(), "Umeng", "count", 0);
			boolean isCount = SPUtils.getBoolean(getContext(), "Umeng", "isCount", false);
			if (!isCount) {
				UmengUtil.onEvent(getContext(), Common.EVENT_CONTAIN_PICTURE_PEOPLES);
				SPUtils.put(getContext(), "Umeng", "isCount", true);
			}
			
			if ( countLocal == photoInfoArrayList.size()) {
				//不记录。
			}else{
				if (photoInfoArrayList.size() > countLocal) {
					SPUtils.put(getContext(), "Umeng", "count", photoInfoArrayList.size());
					int i = Math.abs((photoInfoArrayList.size() - countLocal));
					for (int j = 0; j < i; j++) {
						UmengUtil.onEvent(getContext(), Common.EVENT_TOTAL_PICTURES);
					}
				}
			}
		}

		pwStickySectionRecyclerView.initDate(photoInfoArrayList);

		if (tab == 1) {//airpass
			noPhotoTextView.setText(R.string.no_photo_in_airpass);
		} else if (tab == 2) {//local
			
			noPhotoTextView.setText(R.string.no_photo_in_magiccam);
		} else if (tab == 3) {//bought
			noPhotoTextView.setText(R.string.no_photo_in_bought);
			
		} else if (tab == 4) {//favourite
			noPhotoTextView.setText(R.string.no_photo_in_favourite);
		}
		
		if (photoInfoArrayList.size() > 0) {
			pwStickySectionRecyclerView.setVisibility(View.VISIBLE);
			noPhotoRelativeLayout.setVisibility(View.GONE);

		} else {
			pwStickySectionRecyclerView.setVisibility(View.GONE);
			noPhotoRelativeLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		PictureAirLog.d("fragment init destroyview");
		pwStickySectionRecyclerView.removeItemDecoration();//因为多次进来，会叠加间距，因此在此移除
	}

	@Override
	public void loadMore() {
		if (!refreshLayout.isRefreshing()) {
			PictureAirLog.d("start load more---->");
			pwStickySectionRecyclerView.setIsLoadMore(true);
			pwStickySectionRecyclerView.setLoadMoreType(StickyRecycleAdapter.LOAD_MORE_LOADING);

			Message message = handler.obtainMessage();
			message.what = LOAD_MORE;
			message.arg1 = tab;
			handler.sendMessage(message);
		}
	}

	//照片点击的监听类
	private class PhotoOnItemClickListener implements StickyRecycleAdapter.OnRecyclerViewItemClickListener{
		@Override
		public void onItemClick(View view, int position) {
			if (photoInfoArrayList.size() == 0) {
				return;
			}
			PictureAirLog.out("click photo---> " + position);
			PhotoInfo photoInfo = null;

			if (position < 0) {
				position = 0;
			} else {
				photoInfo = photoInfoArrayList.get(position);
				position -= photoInfo.sectionId + 1;
			}

			if (photoInfo.isVideo == 1 && photoInfo.isPayed == 0) {//广告视频
				PictureAirLog.v(TAG, "点击了广告视频");

				Intent intent = new Intent(getContext(), ADVideoDetailProductActivity.class);
				intent.putExtra("videoInfo", photoInfo);
				Bundle bundle = new Bundle();
				bundle.putInt("position", position);
				bundle.putString("tab", tabName[tab]);
				intent.putExtra("bundle", bundle);
				startActivity(intent);

			} else {
				PictureAirLog.v(TAG,"点击了照片");
				Intent i = new Intent();
				i.setClass(getContext(), PreviewPhotoActivity.class);
				Bundle bundle = new Bundle();
				bundle.putInt("position", position);
				bundle.putString("photoId", photoInfo.photoId);
				bundle.putString("tab", tabName[tab]);
				i.putExtra("bundle", bundle);
				getContext().startActivity(i);
			}
		}

		@Override
		public void onLoadMoreClick(View view, int position) {
			PictureAirLog.d("failed---> click to load more data");
			loadMore();
		}
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (!EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().register(this);
		}
	}
	
	@Override
	public void onDetach() {
		// TODO Auto-generated method stub
		super.onDetach();
		if (EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().unregister(this);
		}
	}
	
	@Subscribe
	public void onUserEvent(BaseBusEvent baseBusEvent) {
		if (baseBusEvent instanceof StoryFragmentEvent) {//获取刷新数据更新页面
			PictureAirLog.out("get data from bus");
			StoryFragmentEvent storyFragmentEvent = (StoryFragmentEvent) baseBusEvent;
			PictureAirLog.out("tab = "+ tab + "------>" + storyFragmentEvent.getTab());
			if (storyFragmentEvent.getTab() == tab) {
				PictureAirLog.out("start update" + tab);
				PictureAirLog.out("storyFragmentEvent.getTab() = " + storyFragmentEvent.getTab());
				int newCount = storyFragmentEvent.getPhotoInfos().size();
				photoInfoArrayList.clear();
				targetArrayList.clear();
				photoInfoArrayList.addAll(storyFragmentEvent.getPhotoInfos());
				targetArrayList.addAll(storyFragmentEvent.getTargetInfos());
				PictureAirLog.out("photo size --" + photoInfoArrayList.size() + " new count" + (newCount - oldCount) + storyFragmentEvent.isRefresh());
				if (photoInfoArrayList.size() == 0) {
					pwStickySectionRecyclerView.setVisibility(View.GONE);
					noPhotoRelativeLayout.setVisibility(View.VISIBLE);
					if (tab == 1) {//airpass
						noPhotoTextView.setText(R.string.no_photo_in_airpass);
					} else if (tab == 2) {//local
						noPhotoTextView.setText(R.string.no_photo_in_magiccam);
					} else if (tab == 3) {//bought
						noPhotoTextView.setText(R.string.no_photo_in_bought);
					} else if (tab == 4) {//favourite
						noPhotoTextView.setText(R.string.no_photo_in_favourite);
					}
				} else {
					pwStickySectionRecyclerView.setVisibility(View.VISIBLE);
					noPhotoRelativeLayout.setVisibility(View.GONE);
					
				}
				if (!storyFragmentEvent.isRefresh()) {
					pwStickySectionRecyclerView.setIsLoadMore(false);
					pwStickySectionRecyclerView.scrollToPosition(oldCount);
					if (newCount - oldCount < Common.LOAD_PHOTO_COUNT) {
						pwStickySectionRecyclerView.setLoadMoreType(StickyRecycleAdapter.LOAD_MORE_NO_MORE);

					}
					oldCount = newCount;
				}
				pwStickySectionRecyclerView.notifyDataSetChanged();
				if (refreshLayout.isRefreshing()) {
					refreshLayout.setRefreshing(false);
				}
				EventBus.getDefault().removeStickyEvent(storyFragmentEvent);
			}
		} else if (baseBusEvent instanceof StoryRefreshEvent) {
			StoryRefreshEvent storyRefreshEvent = (StoryRefreshEvent) baseBusEvent;
			if (storyRefreshEvent.getTab() == tab &&
					storyRefreshEvent.getRefreshStatus() == StoryRefreshEvent.START_REFRESH) {//通知页面开始刷新
				PictureAirLog.out(tab + "------>start refresh from bus");

				if (!pwStickySectionRecyclerView.isLoadMore() && !refreshLayout.isRefreshing()) {//如果不在加载更多，并且不在刷新，开始刷新数据
					PictureAirLog.out(tab + "------>start refresh from tab----");
					refreshLayout.setRefreshing(true);
					Message message = handler.obtainMessage();
					message.what = REFRESH;
					message.arg1 = tab;
					handler.sendMessage(message);
				}

				EventBus.getDefault().removeStickyEvent(storyRefreshEvent);
			}

			if (storyRefreshEvent.getTab() == tab &&
					storyRefreshEvent.getRefreshStatus() == StoryRefreshEvent.STOP_REFRESH) {//开始关闭刷新
				PictureAirLog.out(tab + "------>stop refresh from bus");
				if (refreshLayout.isRefreshing()) {
					refreshLayout.setRefreshing(false);
				}
				EventBus.getDefault().removeStickyEvent(storyRefreshEvent);
			}

			if (storyRefreshEvent.getTab() == tab &&
					storyRefreshEvent.getRefreshStatus() == StoryRefreshEvent.STOP_LOAD_MORE) {//开始关闭加载更多，此处为加载更多失败的处理
				PictureAirLog.out(tab + "------>stop loading more from bus");
				pwStickySectionRecyclerView.setIsLoadMore(false);
				pwStickySectionRecyclerView.setLoadMoreType(StickyRecycleAdapter.LOAD_MORE_FAILED);
				EventBus.getDefault().removeStickyEvent(storyRefreshEvent);
			}
		}
	}

}