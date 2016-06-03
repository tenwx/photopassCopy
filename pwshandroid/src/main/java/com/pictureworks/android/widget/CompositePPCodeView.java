package com.pictureworks.android.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.zxing.WriterException;
import com.pictureworks.android.R;
import com.pictureworks.android.util.AppUtil;
import com.pictureworks.android.util.PictureAirLog;

/**
 * 显示pp二维码生成图的类
 * @author bauer_bao
 *
 */
public class CompositePPCodeView extends RelativeLayout{
	private LayoutInflater layoutInflater;
	private ImageView goodsImageView, photoImageView;
	private RelativeLayout relativeLayout;

	private final int LOAD_SELECTED_IMAGE = 1;

	private int goodWidth, goodHeight, marginLeft, marginTop, photoWidth, photoHeight;


	public CompositePPCodeView(Context context) {
		super(context);
	}

	/**
	 * 自定义控件，先显示商品图片，然后显示添加的照片
	 * @param context
	 * @param goodURL 显示pp码的背景图片
	 * @param photoURL pp码，用来生成二维码
	 * @param goodWidth 背景原图的宽
	 * @param goodHeight 背景原图的高
	 * @param marginLeft 放置图片左边的留白
	 * @param marginTop 放置图片上边的留白
	 * @param photoWidth 放置图片的宽
	 * @param photoHeight 放置图片的高
	 */
	public CompositePPCodeView(Context context, int goodURL, final String photoURL, 
			final int goodWidth, final int goodHeight, final int marginLeft, final int marginTop,
			final int photoWidth, final int photoHeight, final int index) {
		this(context);
		// TODO Auto-generated constructor stub
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.compositeimageview, this);
		relativeLayout = (RelativeLayout)findViewById(R.id.compositeImageRelativeLayout);
		goodsImageView = (ImageView)findViewById(R.id.goodImageView);
		//		photoImageView = new ImageView(context);
		photoImageView = (ImageView)findViewById(R.id.selectedImageView);
		photoImageView.setVisibility(View.VISIBLE);

		this.goodWidth = goodWidth;
		this.goodHeight = goodHeight;
		this.marginLeft = marginLeft;
		this.marginTop = marginTop;
		this.photoWidth = photoWidth;
		this.photoHeight = photoHeight;

		goodsImageView.setImageResource(goodURL);
		if (index==0) {//第一次创建的时候，因为是异步，所以第一次的时候设置完ppcode没法显示出来，所以加个判断标记
			ViewTreeObserver viewTreeObserver = goodsImageView.getViewTreeObserver();
			viewTreeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

				@Override
				public void onGlobalLayout() {
					goodsImageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
					Message message = handler.obtainMessage();
					message.what = LOAD_SELECTED_IMAGE;
					message.obj = photoURL;
					handler.sendMessage(message);
				}
			});
		}

	}
	//之后都是通过这个方法显示ppcode
	public void setPPCodeImage(String photoURL) {
		// TODO Auto-generated method stub
		Message message = handler.obtainMessage();
		message.what = LOAD_SELECTED_IMAGE;
		message.obj = photoURL;
		handler.sendMessage(message);
	}

	private Handler handler = new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LOAD_SELECTED_IMAGE://加载选择的图片
				//获取goodsImageView的宽和高
				int goodW = goodsImageView.getWidth();
				int goodH = goodsImageView.getHeight();
				//获取goodsimageview在屏幕上的坐标
				int [] location = new int[2];
				goodsImageView.getLocationInWindow(location);
				//全部换算成手机上预览的尺寸
				//先确定预览图片的宽和高
				int previewPhotoW = photoWidth * goodW / goodWidth;
				int previewPhotoH = photoHeight * goodH / goodHeight;
				PictureAirLog.out("size="+previewPhotoW+"___"+previewPhotoH);
				//预览图片左边留白和上边留白
				int marginL = marginLeft * goodW / goodWidth;
				int marginT = marginTop * goodH / goodHeight;

				//获取顶部状态栏和标题栏的高度
				int [] toplocation = new int[2];
				relativeLayout.getLocationInWindow(toplocation);
				//留白的地方需要加上背景图片的留白
				marginL += location[0];
				marginT += location[1] - toplocation[1];//因为margintop是本控件和父控件的上边边距，所以要减去标题栏和状态栏的高度
				LayoutParams params = new LayoutParams(previewPhotoW, previewPhotoH);
				//设置左边距和上边距
				params.leftMargin = marginL;
				params.topMargin = marginT;
				photoImageView.setLayoutParams(params);
				//				addView(photoImageView);
				//生成二维码，并且显示出来
				try {
					photoImageView.setImageBitmap(AppUtil.Create2DCode(msg.obj.toString()));
				} catch (WriterException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				PictureAirLog.out("fsdljk"+photoImageView.getWidth()+"___"+photoImageView.getHeight());
				break;

			default:
				break;
			}
		};
	};
}
