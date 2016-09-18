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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.ADVideoDetailProductActivity;
import com.pictureair.photopass.activity.PreviewPhotoActivity;
import com.pictureair.photopass.adapter.StickyGridAdapter;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.eventbus.BaseBusEvent;
import com.pictureair.photopass.eventbus.StoryFragmentEvent;
import com.pictureair.photopass.eventbus.StoryRefreshEvent;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.SPUtils;
import com.pictureair.photopass.util.UmengUtil;
import com.pictureair.photopass.widget.stickygridheaders.StickyGridHeadersGridView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;


public class StoryFragment extends Fragment {
	private static final String TAG = "StoryFragment";
	private StickyGridHeadersGridView gridView;
	private RelativeLayout noPhotoRelativeLayout;
	private TextView noPhotoTextView;
	private StickyGridAdapter stickyGridAdapter;
	private ArrayList<PhotoInfo> targetArrayList;
	private ArrayList<PhotoInfo> photoInfoArrayList;
	private int tab;
	private View view;
	private SwipeRefreshLayout refreshLayout;
	private static Handler handler;
	
	private static StoryFragment storyFragment;

	private static final int REFRESH = 666;
	private String[] tabName = {"all", "photopass", "local", "bought", "favourite"};
	
	public static StoryFragment getInstance(ArrayList<PhotoInfo> photoInfoArrayList, ArrayList<PhotoInfo> targetArrayList, int tab, Handler h){
//		System.out.println("storyfragment----->getinstance");
		handler = h;
		storyFragment = new StoryFragment();
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
//		System.out.println("storyfragemnt---->onattach");
		// TODO Auto-generated method stub
		if (getArguments() != null) {
			photoInfoArrayList = getArguments().getParcelableArrayList("photo");
			targetArrayList = getArguments().getParcelableArrayList("target");
			tab = getArguments().getInt("tab");
//			application = (MyApplication) getActivity().getApplication();
		}
		super.onAttach(context);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		System.out.println("story fragment--------> oncreateView");
		if (view == null) {
			
			view = inflater.inflate(R.layout.story_pinned_list, container, false);
		}
		gridView = (StickyGridHeadersGridView) view.findViewById(R.id.stickyGridHeadersGridView);
		noPhotoRelativeLayout = (RelativeLayout) view.findViewById(R.id.no_photo_relativelayout);
		noPhotoTextView = (TextView) view.findViewById(R.id.no_photo_textView);
		
		
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
//		System.out.println("story fragment--------> on activity created");
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
		
		
		stickyGridAdapter = new StickyGridAdapter(getContext(), photoInfoArrayList);
		gridView.setAdapter(stickyGridAdapter);
		gridView.setOnItemClickListener(new PhotoOnItemClickListener());
		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {
			
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				return true;
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
			gridView.setVisibility(View.VISIBLE);
			noPhotoRelativeLayout.setVisibility(View.GONE);

		} else {
			gridView.setVisibility(View.GONE);
			noPhotoRelativeLayout.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}

	//照片点击的监听类
	private class PhotoOnItemClickListener implements OnItemClickListener{
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			if (position < 0) {
				position = 0;
			}
			if (photoInfoArrayList.get(position).isVideo == 1 && photoInfoArrayList.get(position).isPayed == 0) {
				PictureAirLog.v(TAG, "点击了视频");

				PhotoInfo info = photoInfoArrayList.get(position);
				PictureAirLog.out("未购买的视频");
				/**
				 * 1.获取最新的视频信息
				 * 2.是否是已经购买
				 * 3.储存最新信息
				 * 4.跳转或者弹框提示
				 */
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
					gridView.setVisibility(View.GONE);
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
					gridView.setVisibility(View.VISIBLE);
					noPhotoRelativeLayout.setVisibility(View.GONE);
					
				}
				if (stickyGridAdapter != null) {
					stickyGridAdapter.notifyDataSetChanged();
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