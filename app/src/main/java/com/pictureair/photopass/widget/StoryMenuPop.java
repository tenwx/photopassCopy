package com.pictureair.photopass.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.entity.PhotoItemInfo;

/**
 * Story页面中menu下拉菜单的内容 
 * 1.编辑整个页面的图片
 * 2.筛选图片
 * 3.显示收藏图片
 */
public class StoryMenuPop extends PopupWindow implements OnClickListener{
	public static final int EDIT = 201;
	public static final int SELECTION = 202;
	public static final int COLLECT = 203;
	private static final String TAG = "DefaultPop";
	private Context context;
	private LayoutInflater inflater;
	public View defaultView;
	private PhotoItemInfo info;
	private Handler mHandler;
	private ImageView editImageView, selectImageView, collectImageView;
	private LinearLayout editLinearLayout, selectLinearLayout, collectLinearLayout;
	private TextView editTextView, selectTextView, collectTextView;
	public StoryMenuPop(Context context, Handler handler) {
		super(context);
		this.context = context;
		mHandler = handler;
		initPopupWindow();
	}

	public void initPopupWindow() {
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		defaultView = inflater.inflate(R.layout.pop, null);
		defaultView.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		setContentView(defaultView);
//		editLinearLayout = (LinearLayout) defaultView.findViewById(R.id.story_edit);
//		selectLinearLayout = (LinearLayout) defaultView.findViewById(R.id.story_select);
//		collectLinearLayout = (LinearLayout) defaultView.findViewById(R.id.story_collect);
//		
//		editImageView = (ImageView) defaultView.findViewById(R.id.storyEditImageView);
//		selectImageView = (ImageView) defaultView.findViewById(R.id.storySelectImageView);
//		collectImageView = (ImageView) defaultView.findViewById(R.id.storyCollectImageView);
		
		editTextView = (TextView) defaultView.findViewById(R.id.storyEditTextView);
		selectTextView = (TextView) defaultView.findViewById(R.id.storySelectTextView);
		collectTextView = (TextView) defaultView.findViewById(R.id.storyCollectTextView);
		
//		editImageView.setOnClickListener(this);
//		selectImageView.setOnClickListener(this);
//		collectImageView.setOnClickListener(this);
//		
//		editLinearLayout.setOnClickListener(this);
//		selectLinearLayout.setOnClickListener(this);
//		collectLinearLayout.setOnClickListener(this);
		
		editTextView.setOnClickListener(this);
		selectTextView.setOnClickListener(this);
		collectTextView.setOnClickListener(this);
		
		setWidth(LayoutParams.WRAP_CONTENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
		setFocusable(true);
		setOutsideTouchable(true);

	}

	@Override
	public void dismiss() {
		// TODO Auto-generated method stub
		super.dismiss();
	}

	/**
	 * 
	 * @return pop的View
	 */
	public View getDefaultView() {
		return defaultView;
	}


	public PhotoItemInfo getInfo() {
		return info;
	}

	public void setInfo(PhotoItemInfo info) {
		this.info = info;
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.storyEditTextView:
//			editImageView.performClick();
//			editTextView.performClick();
			Log.d(TAG, "edit on click");
			mHandler.sendEmptyMessage(EDIT);
			break;
			
		case R.id.storySelectTextView:
//			selectImageView.performClick();
//			selectTextView.performClick();
			mHandler.sendEmptyMessage(SELECTION);
			Log.d(TAG, "select on click");
			break;
			
		case R.id.storyCollectTextView:
//			collectImageView.performClick();
//			collectTextView.performClick();
			mHandler.sendEmptyMessage(COLLECT);
			Log.d(TAG, "collect on click");
			break;
			
		default:
			break;
		}
		if(isShowing()){
			dismiss();
		}
		
	}
}
