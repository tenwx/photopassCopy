package com.pictureair.photopass.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;


public class StoryFragment extends Fragment {
	private static final String TAG = "StoryFragment";
	private RelativeLayout noPhotoRelativeLayout;
	private RecyclerView recyclerView;
	private TextView noPhotoTextView;
	private ArrayList<PhotoInfo> targetArrayList;
	private ArrayList<PhotoInfo> photoInfoArrayList;
	private int tab;
	private View view;
	private SwipeRefreshLayout refreshLayout;
	private GridLayoutManager gridLayoutManager;
	private StickyRecycleAdapter stickyRecycleAdapter;
	private TextView tvStickyHeaderView;
	private LinearLayout stickyHeaderLL;
	private static Handler handler;
	
	private static final int REFRESH = 666;
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
		}
		super.onAttach(context);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (view == null) {
			view = inflater.inflate(R.layout.story_pinned_list, container, false);
		}
		recyclerView = (RecyclerView) view.findViewById(R.id.stickyGridHeadersGridView);
		noPhotoRelativeLayout = (RelativeLayout) view.findViewById(R.id.no_photo_relativelayout);
		noPhotoTextView = (TextView) view.findViewById(R.id.no_photo_textView);
		tvStickyHeaderView = (TextView) view.findViewById(R.id.section_time);
		stickyHeaderLL = (LinearLayout) view.findViewById(R.id.story_pinned_section_ll);

		refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_layout);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        refreshLayout.setEnabled(true);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				PictureAirLog.out("start refresh");
				if (photoInfoArrayList.size() != 0) {

					Message message = handler.obtainMessage();
					message.what = REFRESH;
					message.arg1 = tab;
					handler.sendMessage(message);
				}
			}
		});

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

		gridLayoutManager = new GridLayoutManager(getContext(), 3);
		gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
			@Override
			public int getSpanSize(int position) {
				if (position >= photoInfoArrayList.size()) {
					return 3;
				}
				if (position == 0) {
					return 3;

				} else if (position > 0 && photoInfoArrayList.get(position).sectionId != photoInfoArrayList.get(position - 1).sectionId) {
					return 3;
				} else {
					return 1;
				}
			}
		});

		stickyRecycleAdapter = new StickyRecycleAdapter(getContext(), photoInfoArrayList);
		stickyRecycleAdapter.setOnItemClickListener(new PhotoOnItemClickListener());
//		recyclerView.addItemDecoration(new RecycleDividerItemDecoration(ScreenUtil.dip2px(getContext(), 5)));
		recyclerView.setLayoutManager(gridLayoutManager);
		recyclerView.setAdapter(stickyRecycleAdapter);
		recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
				super.onScrolled(recyclerView, dx, dy);
				dealWithStickyHeader(recyclerView);
			}
		});


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
			recyclerView.setVisibility(View.VISIBLE);
			noPhotoRelativeLayout.setVisibility(View.GONE);

		} else {
			recyclerView.setVisibility(View.GONE);
			noPhotoRelativeLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	private void dealWithStickyHeader(RecyclerView recyclerView) {
		// Get the sticky information from the topmost view of the screen.
		View stickyInfoView = recyclerView.findChildViewUnder(5, 1);

		if (stickyInfoView != null && stickyInfoView.getContentDescription() != null) {
			tvStickyHeaderView.setText(String.valueOf(stickyInfoView.getContentDescription()));
		}

		// Get the sticky view's translationY by the first view below the sticky's height.
		View transInfoView = recyclerView.findChildViewUnder(5, stickyHeaderLL.getMeasuredHeight() + 1);

		if (transInfoView != null && transInfoView.getTag() != null) {
			int transViewStatus = (int) transInfoView.getTag();
			int dealtY = transInfoView.getTop() - stickyHeaderLL.getMeasuredHeight();
			if (transViewStatus == StickyRecycleAdapter.HAS_STICKY_VIEW) {
				// If the first view below the sticky's height scroll off the screen,
				// then recovery the sticky view's translationY.
				if (transInfoView.getTop() > 0) {
					stickyHeaderLL.setTranslationY(dealtY);
				} else {
					stickyHeaderLL.setTranslationY(0);
				}
			} else if (transViewStatus == StickyRecycleAdapter.NONE_STICKY_VIEW) {
				stickyHeaderLL.setTranslationY(0);
			}
		}
	}

	//照片点击的监听类
	private class PhotoOnItemClickListener implements StickyRecycleAdapter.OnRecyclerViewItemClickListener{
		@Override
		public void onItemClick(View view, int position) {
			if (photoInfoArrayList.size() == 0) {
				return;
			}

			if (position < 0) {
				position = 0;
			} else {
				position -= photoInfoArrayList.get(position).sectionId + 1;
			}

			if (photoInfoArrayList.get(position).isVideo == 1 && photoInfoArrayList.get(position).isPayed == 0) {//广告视频
				PictureAirLog.v(TAG, "点击了广告视频");
				PhotoInfo info = photoInfoArrayList.get(position);
				PictureAirLog.out("未购买的视频");

				Intent intent = new Intent(getContext(), ADVideoDetailProductActivity.class);
				intent.putExtra("videoInfo", info);
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
				bundle.putString("tab", tabName[tab]);
				i.putExtra("bundle", bundle);
				getContext().startActivity(i);
			}
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
				photoInfoArrayList.clear();
				targetArrayList.clear();
				photoInfoArrayList.addAll(storyFragmentEvent.getPhotoInfos());
				targetArrayList.addAll(storyFragmentEvent.getTargetInfos());
//				PictureAirLog.out("photo size from eventBus--" + storyFragmentEvent.getPhotoInfos().size());
//				PictureAirLog.out("photo size from eventBus--" + storyFragmentEvent.getTargetInfos().size());
//				PictureAirLog.out("photo size from eventBus--" + storyFragmentEvent.getTab());
				PictureAirLog.out("photo size --" + photoInfoArrayList.size());
				if (photoInfoArrayList.size() == 0) {
					recyclerView.setVisibility(View.GONE);
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
					recyclerView.setVisibility(View.VISIBLE);
					noPhotoRelativeLayout.setVisibility(View.GONE);
					
				}
				if (stickyRecycleAdapter != null) {
					stickyRecycleAdapter.notifyDataSetChanged();
				}
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
				if (!refreshLayout.isRefreshing()) {
					refreshLayout.setRefreshing(true);
				}
				Message message = handler.obtainMessage();
				message.what = REFRESH;
				message.arg1 = tab;
				handler.sendMessage(message);

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
		}
	}

}