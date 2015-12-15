package com.pictureair.photopass.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.PreviewPhotoActivity;
import com.pictureair.photopass.adapter.StickyGridAdapter;
import com.pictureair.photopass.entity.BaseBusEvent;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.entity.StoryFragmentEvent;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.util.UmengUtil;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;


public class StoryFragment extends Fragment {
	private GridView gridView;
	private RelativeLayout noPhotoRelativeLayout;
	private TextView noPhotoTextView;
	private StickyGridAdapter stickyGridAdapter;
	private ArrayList<PhotoInfo> targetArrayList;
	private ArrayList<PhotoInfo> photoInfoArrayList;
	private int tab;
	private MyApplication application;
	private View view;
	private SharedPreferences countShare;
	private SwipeRefreshLayout refreshLayout;
	private static Handler handler;
	
	private static StoryFragment storyFragment;
	
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
			application = (MyApplication) getActivity().getApplication();
		}
		super.onAttach(context);
	}
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//		System.out.println("story fragment--------> oncreateView");
		if (view == null) {
			
			view = inflater.inflate(R.layout.story_pinned_list, container, false);
		}
		gridView = (GridView) view.findViewById(R.id.stickyGridHeadersGridView);
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
            		message.what = 666;
            		message.arg1 = tab;
            		handler.sendMessage(message);
				}
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        refreshLayout.setRefreshing(false);
//                    }
//                }, 1000);
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
			countShare = getContext().getSharedPreferences("Umeng", 0);
			int countLocal = countShare.getInt("count", 0);
			boolean isCount = countShare.getBoolean("isCount", false);
			if (!isCount) {
				UmengUtil.onEvent(getContext(), Common.HAVE_PHOTO_USERS_COUNT);
				Editor editor = countShare.edit();
				editor.putBoolean("isCount", true);
			    editor.commit();
			}
			
			if ( countLocal == photoInfoArrayList.size()) {
				//不记录。
			}else{
				if (photoInfoArrayList.size() > countLocal) {
					 Editor editor = countShare.edit();
					 editor.putInt("count", photoInfoArrayList.size());
					 editor.commit();
					 int i = Math.abs((photoInfoArrayList.size() - countLocal));
					 for (int j = 0; j < i; j++) {
						 UmengUtil.onEvent(getContext(), Common.ALL_PHOTO_COUNT);
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

//			Intent i = new Intent();
//			i.setClass(getContext(), PreviewPhotoActivity.class);
//			Bundle bundle = new Bundle();
//			bundle.putString("activity", "storyFragment");
//			bundle.putInt("position", position);//在那个相册中的位置
//			bundle.putString("photoId", photoInfoArrayList.get(position).photoId);
//			application.previewPhotoList.clear();
//			application.previewPhotoList.addAll(photoInfoArrayList);
//			bundle.putParcelableArrayList("targetphotos", targetArrayList);
//			i.putExtra("bundle", bundle);
//			getContext().startActivity(i);

			Intent i = new Intent();
			i.setClass(getContext(), PreviewPhotoActivity.class);
			i.putExtra("activity", "storyFragment");
			i.putExtra("position", position);//在那个相册中的位置
			i.putExtra("photoId", photoInfoArrayList.get(position).photoId);
			i.putExtra("targetphotos", targetArrayList);
			i.putExtra("photos", photoInfoArrayList);//那个相册的全部图片路径
			getContext().startActivity(i);
		}
	}
	
	@Override
	public void onResume() {
//		System.out.println("storyfragment---->onresume");
		// TODO Auto-generated method stub
		PictureAirLog.out(tab + "storyfragment------>"+ photoInfoArrayList.size());
		super.onResume();
		if (!EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().register(this);
		}
	}
	
	@Override
	public void onDetach() {
//		System.out.println("storyfragment---->ondetach");
		// TODO Auto-generated method stub
		super.onDetach();
		if (EventBus.getDefault().isRegistered(this)) {
			EventBus.getDefault().unregister(this);
		}
	}
	
	@Subscribe
	public void onUserEvent(BaseBusEvent baseBusEvent) {
		if (baseBusEvent instanceof StoryFragmentEvent) {
			StoryFragmentEvent storyFragmentEvent = (StoryFragmentEvent) baseBusEvent;
//			System.out.println("get data from bus");
			PictureAirLog.out("tab = "+ tab);
			if (storyFragmentEvent.getTab() == tab) {
//				System.out.println("start refresh"+ tab);
				PictureAirLog.out("storyFragmentEvent.getTab() = "+ storyFragmentEvent.getTab());
				photoInfoArrayList.clear();
				targetArrayList.clear();
				photoInfoArrayList.addAll(storyFragmentEvent.getPhotoInfos());
				targetArrayList.addAll(storyFragmentEvent.getTargetInfos());
//				System.out.println("photo size from eventBus--"+storyFragmentEvent.getPhotoInfos().size());
//				System.out.println("photo size from eventBus--"+storyFragmentEvent.getTargetInfos().size());
//				System.out.println("photo size from eventBus--"+storyFragmentEvent.getTab());
//				System.out.println("photo size --"+photoInfoArrayList.size());
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
		}
	}
}