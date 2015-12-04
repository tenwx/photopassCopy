package com.pictureair.photopass.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.ScreenUtil;
/**
 * 显示商品合成图的类
 * @author bauer_bao
 *
 */
public class CompositeImageProductView extends RelativeLayout{
	private LayoutInflater layoutInflater;
	private ImageView goodsImageView, photoImageView, maskImageView, onlySelectedImageView;
	private RelativeLayout relativeLayout;
	private ImageLoader imageLoader;
	private Context context;
	
	private final int LOAD_SELECTED_IMAGE = 1;
	private final int LOAD_SELECTED_IMAGE_FOR_IPHONE_CASE = 2;//手机壳的处理
	private final int LOAD_SELECTED_IMAGE_FOR_MUG_CASE = 3;//杯子的处理
	private final int LOAD_SELECTED_IMAGE_FOR_KEYCHAIN_CASE = 4;//钥匙串的处理
	private final int LOAD_SELECTED_IMAGE_FOR_NO_PRODUCT_IMAGE = 5;//其他只显示图片的商品
	
	private int goodWidth, goodHeight, marginLeft, marginTop, photoWidth, photoHeight, maskBottom, maskTop, viewWidth, viewHeight;
	private float degree;
	private String goodName;
	
	
	public CompositeImageProductView(Context context) {
		super(context);
		imageLoader = ImageLoader.getInstance();
	}
	
	/**
	 * 自定义控件，先显示商品图片，然后显示添加的照片
	 * @param context
	 * @param goodURL 显示商品的图片
	 * @param photoURL 自己选择的图片
	 * @param goodWidth 商品原图的宽
	 * @param goodHeight 商品原图的高
	 * @param marginLeft 放置图片左边的留白
	 * @param marginTop 放置图片上边的留白
	 * @param photoWidth 放置图片的宽
	 * @param photoHeight 放置图片的高
	 * @param degree 放置图片的偏角，只有钥匙扣才有这个属性
	 * @param maskBottom 底层的蒙版
	 * @param maskTop 上层的遮罩
	 * @param goodName 商品名字
	 */
	public CompositeImageProductView(Context context, String goodURL, final int viewWidth, final int viewHeight, final String photoURL, 
			final int goodWidth, final int goodHeight, final int marginLeft, final int marginTop,
			final int photoWidth, final int photoHeight, final float degree, final int maskBottom, final int maskTop, final String goodName) {
		this(context);
		// TODO Auto-generated constructor stub
		layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layoutInflater.inflate(R.layout.compositeimageview, this);
		relativeLayout = (RelativeLayout)findViewById(R.id.compositeImageRelativeLayout);
		goodsImageView = (ImageView)findViewById(R.id.goodImageView);
		onlySelectedImageView = (ImageView)findViewById(R.id.selectedImageView);
		photoImageView = new ImageView(context);
		
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.goodWidth = goodWidth;
		this.goodHeight = goodHeight;
		this.marginLeft = marginLeft;
		this.marginTop = marginTop;
		this.photoWidth = photoWidth;
		this.photoHeight = photoHeight;
		this.degree = degree;
		this.maskBottom = maskBottom;
		this.maskTop = maskTop;
		this.goodName = goodName;
		this.context = context;
		System.out.println("------------googURL--->"+goodURL);
		//如果以下商品需要加载商品图片
		if (goodName.equals("canvas")||goodName.equals("iphone5Case")||goodName.equals("keyChain")||goodName.equals("mug")) {
			onlySelectedImageView.setVisibility(View.INVISIBLE);
			imageLoader.loadImage(Common.BASE_URL + goodURL, new SimpleImageLoadingListener(){
				@Override
				public void onLoadingComplete(String imageUri, View view,
						Bitmap loadedImage) {
					super.onLoadingComplete(imageUri, view, loadedImage);
					
					/**********************************************************************************/
					//判断网络获取的图片的大小，需要进行缩放操作
					//loadedImage的大小和viewWidth、viewHeight做比较
					Matrix matrix = new Matrix();
					System.out.println("wh"+loadedImage.getWidth()+"___"+loadedImage.getHeight());
					System.out.println("wh"+viewWidth+"___"+viewHeight);
//					if (loadedImage.getWidth() > viewWidth || loadedImage.getHeight() > viewHeight) {//大于预览框
//						if (loadedImage.getWidth() / (float) viewWidth < loadedImage.getHeight() / (float) viewHeight) {//按照高度缩放比例
//							scale = loadedImage.getHeight() / (float) viewHeight;
//						}else {//根据宽度缩放比例
//							scale = loadedImage.getWidth() / (float) viewWidth;
//						}
//						matrix.setScale(scale, scale);
//						loadedImage = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), matrix, true);
//					}
					
//					if (loadedImage.getWidth() <= viewWidth && loadedImage.getHeight() <= viewHeight) {//小于预览框
						if (loadedImage.getWidth() / (float) viewWidth < loadedImage.getHeight() / (float) viewHeight) {//按照高度缩放比例
							float scale =  viewHeight / (float) loadedImage.getHeight();
							matrix.setScale(scale, scale);
							System.out.println("scale-1>"+scale);
						}else {//根据宽度缩放比例
							float scale =  viewWidth / (float) loadedImage.getWidth();
							System.out.println("scale-2>"+scale);
							matrix.setScale(scale, scale);
						}
//						System.out.println("scale->"+scale);
						loadedImage = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), matrix, true);
					
//					}
					
					/**********************************************************************************/
					
					
					//加载完商品图片之后，加载选择的照片
					goodsImageView.setImageBitmap(loadedImage);
					System.out.println("jjjooooooooj="+goodsImageView.getWidth()+"_"+goodsImageView.getHeight());
					Message message = handler.obtainMessage();
					message.what = LOAD_SELECTED_IMAGE;
					message.obj = photoURL;
					handler.sendMessageDelayed(message, 500);
					
				}
			});
		}else {//其他商品，直接显示选择的照片
			onlySelectedImageView.setVisibility(View.VISIBLE);
			Message message = handler.obtainMessage();
			message.what = LOAD_SELECTED_IMAGE_FOR_NO_PRODUCT_IMAGE;
			message.obj = photoURL;
			handler.sendMessage(message);
		}
		
	}

	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case LOAD_SELECTED_IMAGE_FOR_NO_PRODUCT_IMAGE://只显示照片的处理方式
				if (goodName.equals(Common.GOOD_NAME_SINGLE_DIGITAL)) {
					android.view.ViewGroup.LayoutParams params = onlySelectedImageView.getLayoutParams();
					params.width = viewWidth;
					params.height = viewHeight;
					onlySelectedImageView.setLayoutParams(params);
					imageLoader.displayImage(msg.obj.toString(), onlySelectedImageView);
				}else {
					imageLoader.loadImage(msg.obj.toString(), new SimpleImageLoadingListener(){
						public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
							System.out.println("load success");
							//对于获取成功的图片，进行截取
							int clipStartX = 0;
							int clipStartY = 0;
							int clipWidth = 0;
							int clipHeight = 0;
							int bitmapWidht = loadedImage.getWidth();
							int bitmapHeight = loadedImage.getHeight();
							if (goodName.equals("4R Print")) {//3：2的截图
								if (bitmapWidht * 2 == bitmapHeight * 3 || bitmapWidht * 3 == bitmapHeight * 2) {//如果本来就是3：2的，直接显示
									//直接显示
									
								}else {//需要截取
									System.out.println("w&h"+bitmapWidht+"_"+bitmapHeight);
									if (bitmapHeight > bitmapWidht) {//竖着
										if (bitmapHeight / (float) bitmapWidht > 1.5) {//截取多余的高度
											
											clipStartX = 0;
											clipWidth = bitmapWidht;
											clipHeight = bitmapWidht * 3 / 2;
											clipStartY = (bitmapHeight - clipHeight) / 2;
										}else {//截取多余的宽度
											System.out.println("-----lanscape");
											clipStartY = 0;
											clipHeight = bitmapHeight;
											clipWidth = bitmapHeight * 2 / 3;
											clipStartX = (bitmapWidht - clipWidth) / 2;
										}
										
									}else {//横着
										if (bitmapWidht / (float) bitmapHeight < 1.5) {//截取多余的高度
											
											clipStartX = 0;
											clipWidth = bitmapWidht;
											clipHeight = bitmapWidht * 2 / 3;
											clipStartY = (bitmapHeight - clipHeight) / 2;
										}else {//截取多余的宽度
											System.out.println("-----lanscape");
											clipStartY = 0;
											clipHeight = bitmapHeight;
											clipWidth = bitmapHeight * 3 / 2;
											clipStartX = (bitmapWidht - clipWidth) / 2;
										}
										
									}
									System.out.println("x:"+clipStartX+"y:"+clipStartY+"w:"+clipWidth+"h:"+clipHeight);
									loadedImage = Bitmap.createBitmap(loadedImage, clipStartX, clipStartY, clipWidth, clipHeight);
								}
							}else if (goodName.equals(Common.GOOD_NAME_6R)||goodName.equals(Common.GOOD_NAME_COOK)||goodName.equals(Common.GOOD_NAME_TSHIRT)) {//4：3的截图
								if (bitmapWidht * 3 == bitmapHeight * 4 || bitmapWidht * 4 == bitmapHeight * 3) {//如果本来就是4：3的，直接显示
									//直接显示
									
								}else {//需要截取
									System.out.println("w&h"+bitmapWidht+"_"+bitmapHeight);
									if (bitmapHeight > bitmapWidht) {//竖着
										if (bitmapHeight / (float) bitmapWidht > 4 / 3.0) {//截取多余的高度
											
											clipStartX = 0;
											clipWidth = bitmapWidht;
											clipHeight = bitmapWidht * 4 / 3;
											clipStartY = (bitmapHeight - clipHeight) / 2;
										}else {//截取多余的宽度
											System.out.println("-----lanscape");
											clipStartY = 0;
											clipHeight = bitmapHeight;
											clipWidth = bitmapHeight * 3 / 4;
											clipStartX = (bitmapWidht - clipWidth) / 2;
										}
										
									}else {//横着
										if (bitmapWidht / (float) bitmapHeight < 4 / 3.0) {//截取多余的高度
											
											clipStartX = 0;
											clipWidth = bitmapWidht;
											clipHeight = bitmapWidht * 3 / 4;
											clipStartY = (bitmapHeight - clipHeight) / 2;
										}else {//截取多余的宽度
											System.out.println("-----lanscape");
											clipStartY = 0;
											clipHeight = bitmapHeight;
											clipWidth = bitmapHeight * 4 / 3;
											clipStartX = (bitmapWidht - clipWidth) / 2;
										}
										
									}
									System.out.println("x:"+clipStartX+"y:"+clipStartY+"w:"+clipWidth+"h:"+clipHeight);
									loadedImage = Bitmap.createBitmap(loadedImage, clipStartX, clipStartY, clipWidth, clipHeight);
								}
							}
							
							onlySelectedImageView.setImageBitmap(loadedImage);
						};
					});
				}
				break;
			
			case LOAD_SELECTED_IMAGE://加载选择的图片，普通商品
				/*********************确定选择的照片显示的区域*************************************/
				//获取goodsImageView的宽和高
				int goodW = goodsImageView.getWidth();
				int goodH = goodsImageView.getHeight();
				System.out.println("jjjj="+goodW+"_"+goodH);
				//获取goodsimageview在屏幕上的坐标
				int [] location = new int[2];
				goodsImageView.getLocationOnScreen(location);
				//全部换算成手机上预览的尺寸
				//先确定预览图片的宽和高
				int previewPhotoW = photoWidth * goodW / goodWidth;
				int previewPhotoH = photoHeight * goodH / goodHeight;
				//预览图片左边留白和上边留白
				int marginL = marginLeft * goodW / goodWidth;
				int marginT = marginTop * goodH / goodHeight;
				
				//获取顶部状态栏和标题栏的高度
				int [] toplocation = new int[2];
				relativeLayout.getLocationInWindow(toplocation);
				//留白的地方需要加上背景图片的留白
				marginL += location[0] - ScreenUtil.dip2px(context, 20);//因为photoImageView是new出来的，所以需要减去布局中的marginLeft的20dp，也可以通过获取viewpager的x坐标，并且减去这个坐标（和20dp转换为px是一样的）
				marginT += location[1] - toplocation[1];//因为margintop是本控件和父控件的上边边距，所以要减去标题栏和状态栏的高度
				
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(previewPhotoW, previewPhotoH);
				//设置左边距和上边距
				params.leftMargin = marginL;
				params.topMargin = marginT;
				photoImageView.setLayoutParams(params);
				
				addView(photoImageView);
				System.out.println("------------->"+msg.obj.toString());
				if (goodName.equals("mug")) {//直接显示，同时处理mask层
					imageLoader.displayImage(msg.obj.toString(), photoImageView);
					//添加mask层
					handler.sendEmptyMessage(LOAD_SELECTED_IMAGE_FOR_MUG_CASE);
					
				}else if (goodName.equals("iphone5Case")) {//获取选择的图片，进行合成
					Message m = handler.obtainMessage();
					m.what = LOAD_SELECTED_IMAGE_FOR_IPHONE_CASE;
					m.obj = msg.obj;
					handler.sendMessage(m);
					
				}else if (goodName.equals("keyChain")) {
					Message m = handler.obtainMessage();
					m.what = LOAD_SELECTED_IMAGE_FOR_KEYCHAIN_CASE;
					m.obj = msg.obj;
					handler.sendMessage(m);
				}else {//直接显示
					imageLoader.displayImage(msg.obj.toString(), photoImageView);
					
				}
				break;
				
			case LOAD_SELECTED_IMAGE_FOR_IPHONE_CASE://手机壳
				
				/*************************对添加的图片进行处理********************************/
				imageLoader.loadImage(msg.obj.toString(), new SimpleImageLoadingListener(){
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						System.out.println("load success");
						//获取遮罩层图片
						Bitmap mask = BitmapFactory.decodeResource(getResources(), maskBottom);
						Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
						System.out.println("mask size = "+result.getWidth()+"_"+result.getHeight());
						//将遮罩层的图片放到画布中
						Canvas mCanvas = new Canvas(result);
						Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
						paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));//参数的意思是，重叠的部分显示下面的
						System.out.println("selected image width and height:"+loadedImage.getWidth()+"_"+loadedImage.getHeight());
						//对loadedimage进行缩放，使得高度和商品mask的高度一致
						Matrix matrix = new Matrix();
						float scale = (float)mask.getHeight()/loadedImage.getHeight();
						matrix.setScale(scale, scale);
						loadedImage = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), matrix, true);
						System.out.println("selected image width and height:"+loadedImage.getWidth()+"_"+loadedImage.getHeight());
						//需要截取图片的中间部分
						mCanvas.drawBitmap(loadedImage, (mask.getWidth() - loadedImage.getWidth())/2, 0, null);//最先画，所以在最下层
						mCanvas.drawBitmap(mask, 0, 0, paint);//之后画的，所以在上层，所以重叠的部分显示original部分。
						//绘画最上层
						paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
						Bitmap topBitmap = BitmapFactory.decodeResource(getResources(), maskTop);
						mCanvas.drawBitmap(topBitmap, 0, 0, paint);
						
						paint.setXfermode(null);
						//回收资源
						mask.recycle();
						topBitmap.recycle();
						loadedImage.recycle();
						photoImageView.setImageBitmap(result);
					};
				});
				break;
				
			case LOAD_SELECTED_IMAGE_FOR_MUG_CASE://杯子的mask层
				/*************************对添加的图片进行处理********************************/
				//获取goodsImageView的宽和高
				int goodW2 = goodsImageView.getWidth();
				int goodH2 = goodsImageView.getHeight();
				//获取goodsimageview在屏幕上的坐标
				int [] location2 = new int[2];
				goodsImageView.getLocationInWindow(location2);
				//全部换算成手机上预览的尺寸
				//先确定预览图片的宽和高
				int previewPhotoW2 = photoWidth * goodW2 / goodWidth;
				int previewPhotoH2 = goodH2;
				//预览图片左边留白和上边留白
				int marginL2 = marginLeft * goodW2 / goodWidth;
				int marginT2 = 0;
				
				//获取顶部状态栏和标题栏的高度
				int [] toplocation2 = new int[2];
				relativeLayout.getLocationInWindow(toplocation2);
				//留白的地方需要加上背景图片的留白
				marginL2 += location2[0] - ScreenUtil.dip2px(context, 20);
				marginT2 += location2[1] - toplocation2[1];//因为margintop是本控件和父控件的上边边距，所以要减去标题栏和状态栏的高度
				
				maskImageView = new ImageView(context);
				RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(previewPhotoW2, previewPhotoH2);
				//设置左边距和上边距
				params2.leftMargin = marginL2;
				params2.topMargin = marginT2;
				
				maskImageView.setLayoutParams(params2);
				
				addView(maskImageView);
				Bitmap topBitmap2 = BitmapFactory.decodeResource(getResources(), maskTop);
				maskImageView.setImageBitmap(topBitmap2);
					
				break;

			case LOAD_SELECTED_IMAGE_FOR_KEYCHAIN_CASE://钥匙圈
				/*************************对添加的图片进行处理********************************/
				imageLoader.loadImage(msg.obj.toString(), new SimpleImageLoadingListener(){
					public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
						Matrix matrix = new Matrix();
						matrix.setSkew(degree, 0);
						loadedImage = Bitmap.createBitmap(loadedImage, 0, 0, loadedImage.getWidth(), loadedImage.getHeight(), matrix, true);
						photoImageView.setImageBitmap(loadedImage);
					};
				});
				break;
				
			default:
				break;
			}
		};
	};
}
