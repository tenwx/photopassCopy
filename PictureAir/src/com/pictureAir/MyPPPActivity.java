package com.pictureAir;

import java.util.ArrayList;
import java.util.Collections;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActionBar.LayoutParams;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.pictureAir.adapter.ListOfPPPAdapter;
import com.pictureAir.customDialog.CustomDialog;
import com.pictureAir.db.PhotoInfoDBHelper;
import com.pictureAir.entity.DayOfPP;
import com.pictureAir.entity.DayOfPPP;
import com.pictureAir.entity.PPPinfo;
import com.pictureAir.entity.PPinfo;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.util.HttpsUtil;
import com.pictureAir.widget.BannerView_PPPIntroduce;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.PPPPop;
import com.pictureAir.widget.calendar.CalendarActivity;
/**显示用户所有的PP+或是对应某个PP而言可使用的PP+*/
public class MyPPPActivity extends BaseActivity implements OnClickListener {
	private final static int PPP_CODE = 1;
	private final static int PP_CODE = 2;
	private final static int OPTION_TYPE_CHOOSE_PP_TO_BIND = 3;
	private final static int OPTION_TYPE_SCAN_PP = 4;
	private final static int OPTION_TYPE_USE_OTHER_PPP = 5;
	private final static int OPTION_TYPE_BUY_PPP = 6;

	private ImageView setting, ppp_info_imageView;
	private ListView listPPP;
	private ImageView back;
	private View view;
	private LinearLayout scanOrBuyPPP, pppPopLinearLayout;
	private ImageView scanOrBuyImageView, optionImageView;
	private TextView  ppp_info, scanOrBuyTextView, optoinTextView,text_instruction;

	private BannerView_PPPIntroduce nopppLayout;

	private PopupWindow pop;
	private CustomProgressDialog dialog;
	private MyToast newToast;
	private CustomDialog customdialog;
	private MyApplication application;

	private ListOfPPPAdapter listPPPAdapter;
	private ArrayList<DayOfPPP> list1;// 绑定了pp的pp+
	private ArrayList<DayOfPP> list3;// 对应pp+可以绑定的pp
	private SharedPreferences sharedPreferences;
	private SQLiteDatabase database;
	private SQLiteOpenHelper dbHelper;

	private final static int DEAL_PP_JSON_SUCCESS = 4;
	private boolean hasOtherAvailablePPP = false;//判断是否还有其他可用的ppp
	private int currentPosition = 0;//记录选中的项的索引值

	private String errorMessage = "";
	private int optionType = 0;//记录弹框类型
    PPPPop pppPop;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Intent intent;
			switch (msg.what) {
			case 999://购买PP
				intent= new Intent(MyPPPActivity.this,PPPDetailProductActivity.class);
				startActivity(intent);
				if (pppPop.isShowing()) {
					pppPop.dismiss();
				}
				break;
			case 888://扫描
				intent = new Intent(MyPPPActivity.this, MipCaptureActivity.class);
				intent.putExtra("type", "ppp");//只扫描ppp
				startActivityForResult(intent, PPP_CODE);
				if (pppPop.isShowing()) {
					pppPop.dismiss();
				}
				break;
			case 1:
				dialog.dismiss();
				listPPP.setVisibility(View.VISIBLE);
				System.out.println("list========="+list1.size());
				listPPPAdapter = new ListOfPPPAdapter(list1, MyPPPActivity.this);
				listPPP.setAdapter(listPPPAdapter);
				break;

			case API.GET_PPP_SUCCESS://成功获取ppp信息
				Log.e("＝＝＝", "成功获取PPP信息");
				if (API.PPPlist.size()==0) {
					listPPP.setVisibility(View.GONE);
					nopppLayout.setVisibility(View.VISIBLE);
					text_instruction.setVisibility(View.VISIBLE);

				}else {
					Editor editor = sharedPreferences.edit();
					editor.putInt(Common.PPP_COUNT, API.PPPlist.size());
					editor.commit();
					database = dbHelper.getWritableDatabase(Common.SQLCIPHER_KEY);
					Cursor cursor = null;
//					int count = 0;
					for (int i = 0; i < API.PPPlist.size(); i++) {
						DayOfPPP dayOfPPP = new DayOfPPP();
						PPPinfo ppPinfo = API.PPPlist.get(i);
						// String bindddateString =
						// ppPinfo.bindInfo.get(0).bindDate;
						dayOfPPP.time = ppPinfo.ownOn;
						dayOfPPP.pppId = ppPinfo.PPPCode;
						dayOfPPP.amount = ppPinfo.capacity;
//						if(ppPinfo.bindInfo.size()>0){
//						for (int j = 0; j < ppPinfo.bindInfo.size(); j++) {
//							if (j == 0) {
//								if (null != ppPinfo.bindInfo.get(j).customerId
//										&& !"".equals(ppPinfo.bindInfo.get(j).customerId)) {
//									dayOfPPP.pp1 = ppPinfo.bindInfo.get(j).customerId;
//									count++;
//									// 只取第一张pp对应的第一张照片
//									cursor = database
//											.rawQuery(
//													"select * from "
//															+ Common.PHOTOPASS_INFO_TABLE
//															+ " where photoCode like ? and shootTime=? order by shootOn desc limit 1",
//													new String[] {
//															"%" + dayOfPPP.pp1
//																	+ "%",
//															dayOfPPP.time });
//									if (cursor.moveToFirst()) {
//										do {
//											dayOfPPP.pppBgUrl = Common.PHOTO_URL
//													+ cursor.getString(6);// previewUrl_512
//										} while (cursor.moveToNext());
//									}
//								}
//							} else if (j == 1) {
//
//							} else if (j == 2) {
//
//							}
//						}
//						}
						//判断是否有可用的ppp
						dayOfPPP.usedNumber = ppPinfo.bindInfo.size();
						if (!hasOtherAvailablePPP) {
							if (dayOfPPP.usedNumber < dayOfPPP.amount) {// 有空位的ppp
								hasOtherAvailablePPP = true;
							}
						}
						// 需要对ppp进行排序
						list1.add(dayOfPPP);
						System.out.println("the ppp code ====>"
								+ dayOfPPP.pppId);
						System.out
								.println("the ppp background photo url is ====>"
										+ dayOfPPP.pppBgUrl);
					}
					//for循环外面，else 里面
					//处理完了，通知处理之后的信息
					if (cursor != null) {
						cursor.close();
					}
					database.close();
					Collections.sort(list1);
					System.out.println("list-=--="+list1.size());
					listPPP.setVisibility(View.VISIBLE);
					nopppLayout.setVisibility(View.GONE);
					text_instruction.setVisibility(View.GONE);
					listPPPAdapter = new ListOfPPPAdapter(list1, MyPPPActivity.this);
					listPPP.setAdapter(listPPPAdapter);
				}
				application.setNeedRefreshPPPList(false);
				dialog.dismiss();
				break;

			case API.FAILURE://获取失败
			case API.GET_PPP_FAILED:
				if (msg.obj != null && msg.obj.toString().equals("PPHasUpgraded")) {
					System.out.println("PP has upgraded");
					newToast.setTextAndShow("PP has upgraded", Common.TOAST_SHORT_TIME);
				}else {
					newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				}
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				break;

			case API.GET_PP_SUCCESS://获取
				list3.clear();
				getPhotoUrlFromDatabase();
				break;

				//处理完pp信息之后
			case DEAL_PP_JSON_SUCCESS:
				//更新pop的内容
				switch (list1.get(currentPosition).usedNumber) {
				case 0://全新的
					if (list3.size()==0) {//没有符合的pp
						ppp_info.setText(getString(R.string.ppp_info2_2));
						optionImageView.setImageResource(R.drawable.scan_box6);
						optoinTextView.setText(getString(R.string.scan_pp));
						optionType = OPTION_TYPE_SCAN_PP;
						scanOrBuyPPP.setVisibility(View.INVISIBLE);
					}else {//有符合的pp
						ppp_info.setText(getString(R.string.ppp_info3));
						optionImageView.setImageResource(R.drawable.choose_pp);
						optoinTextView.setText(getString(R.string.choose_three_pp));
						optionType = OPTION_TYPE_CHOOSE_PP_TO_BIND;
						scanOrBuyPPP.setVisibility(View.VISIBLE);
						scanOrBuyImageView.setImageResource(R.drawable.scan_box3);
						scanOrBuyTextView.setText(getString(R.string.also_scan_pp));
					}
					break;

				case 1://未用完的
				case 2:
					if (list3.size()==0) {//没有符合的pp
						ppp_info.setText(getString(R.string.ppp_info2));
						optionImageView.setImageResource(R.drawable.scan_box6);
						optoinTextView.setText(getString(R.string.scan_pp));
						optionType = OPTION_TYPE_SCAN_PP;
						scanOrBuyPPP.setVisibility(View.INVISIBLE);
					}else {//有符合的pp
						ppp_info.setText(getString(R.string.ppp_info1));
						optionImageView.setImageResource(R.drawable.choose_pp);
						optoinTextView.setText(getString(R.string.choose_one_pp));
						optionType = OPTION_TYPE_CHOOSE_PP_TO_BIND;
						scanOrBuyPPP.setVisibility(View.VISIBLE);
						scanOrBuyImageView.setImageResource(R.drawable.scan_box3);
						scanOrBuyTextView.setText(getString(R.string.also_scan_pp));
					}
					break;

				case 3://全部用完的
					break;

				default:
					break;
				}

				dialog.dismiss();
				pop.showAtLocation(findViewById(R.id.myppp), Gravity.CENTER, 0, 0);
				backgroundAlpha(0.3f);
				break;

			case API.SUCCESS://绑定成功
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(Common.NEED_FRESH, true);
				editor.commit();
				list1.clear();
				hasOtherAvailablePPP = false;
				API.PPPlist.clear();
				getData();
				break;

			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_ppp);
		initView();
	}

	private void initView() {
		pppPop = new PPPPop(this, mHandler);
		//初始化
		AppManager.getInstance().addActivity(this);
		newToast = new MyToast(this);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		dbHelper = new PhotoInfoDBHelper(this, Common.PHOTOPASS_INFO_NAME, Common.PHOTOPASS_INFO_VERSION);
		application = (MyApplication)getApplication();
		//找控件
		text_instruction = (TextView) findViewById(R.id.text_instruction);
		back = (ImageView) findViewById(R.id.back);
		setting = (ImageView) findViewById(R.id.ppp_setting);
		nopppLayout = (BannerView_PPPIntroduce)findViewById(R.id.nopppinfo);
		listPPP = (ListView) findViewById(R.id.list_ppp);
		//添加浮窗的布局
		LayoutInflater inflater = LayoutInflater.from(this);
		view = inflater.inflate(R.layout.ppp_pop, null);
		pop = new PopupWindow(view, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, false);
		pop.setBackgroundDrawable(new ColorDrawable(android.R.color.transparent));
		pop.setFocusable(true);
		pop.setOutsideTouchable(true);
		pop.setAnimationStyle(R.style.from_center_anim);

		pppPopLinearLayout = (LinearLayout) view.findViewById(R.id.ppp_pop);
		ppp_info_imageView = (ImageView) view.findViewById(R.id.ppp_info_imageView);
		ppp_info = (TextView) view.findViewById(R.id.ppp_info);
		optionImageView = (ImageView) view.findViewById(R.id.option_imageView);
		optoinTextView = (TextView) view.findViewById(R.id.option_textView);
		scanOrBuyImageView = (ImageView) view.findViewById(R.id.bind_or_other_imageView);
		scanOrBuyTextView = (TextView) view.findViewById(R.id.bind_or_other_textView);
		scanOrBuyPPP = (LinearLayout) view.findViewById(R.id.scan_or_buy);

		list3 = new ArrayList<DayOfPP>();
		list1 = new ArrayList<DayOfPPP>();

		application.setNeedRefreshPPPList(true);

		//设置监听
		pop.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				// TODO Auto-generated method stub
				backgroundAlpha(1.0f);
			}
		});
		
		nopppLayout.setVisibility(View.INVISIBLE);
		listPPP.setVisibility(View.GONE);
		scanOrBuyPPP.setOnClickListener(this);
		optionImageView.setOnClickListener(this);
		optoinTextView.setOnClickListener(this);
		back.setOnClickListener(this);
		setting.setOnClickListener(this);
		listPPP.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				
				if (list1.get(position).usedNumber < list1.get(position).amount) {
					Log.e("ppp", "size :"+list1.get(position).pppId);
					//是没用完的ppp  跳转到选择日期的界面。
					Intent intent =  new Intent(MyPPPActivity.this,CalendarActivity.class);
					Bundle bundle = new Bundle();
					bundle.putParcelableArrayList("pp", list3);
					Log.e("＝＝ ", "size:"+list3.size());
					bundle.putParcelable("ppp", list1.get(position));
					intent.putExtras(bundle);
					startActivity(intent);
				}else{
				    //用完了的PPP  弹出窗口提示
					customdialog = new CustomDialog.Builder(MyPPPActivity.this).setMessage(getResources().getString(R.string.buy_ppp_tips))
							.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									customdialog.dismiss();
								}
							})
							.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface arg0, int arg1) {
									// TODO Auto-generated method stub
									Intent intent= new Intent(MyPPPActivity.this,PPPDetailProductActivity.class);
									startActivity(intent);
									customdialog.dismiss();
								}
							})
							.setCancelable(false)
							.create();
							customdialog.show();
				}
				
				
//				System.out.println("onItemClick---------");
//				currentPosition = position;
//
//				if (list1.get(position).usedNumber < list1.get(position).amount) {//是没用完的ppp
//					//请求api，检查是否有可用的pp
//					dialog = ProgressDialog.show(MyPPPActivity.this, getString(R.string.loading___), getString(R.string.is_loading), false, false);
//					API.getPPsByPPPAndDate(sharedPreferences.getString(Common.USERINFO_TOKENID, null), list1.get(position).pppId, list1.get(position).time, mHandler);
//
//				}else {//用完了的ppp
//					//判断是否还有其他可用的
//					if (hasOtherAvailablePPP) {//有可用的ppp
//						ppp_info.setText(getString(R.string.ppp_info4));
//						//更改optionImageview和optionTextView的内容
//						optionImageView.setImageResource(R.drawable.use_ppp);
//						optoinTextView.setText(getString(R.string.use_other_ppp));
//						optionType = OPTION_TYPE_USE_OTHER_PPP;
//						//设置scanorbuy是否显示
//						scanOrBuyPPP.setVisibility(View.VISIBLE);
//						scanOrBuyTextView.setText(getString(R.string.also_buy_ppp));
//						scanOrBuyImageView.setImageResource(R.drawable.cart_buy_ppp);
//					}else {//没有其他可用的ppp
//						ppp_info.setText(getString(R.string.ppp_info5));
//						//更改optionImageview和optionTextView的内容
//						optionImageView.setImageResource(R.drawable.buy_ppp);
//						optoinTextView.setText(getString(R.string.buy_another_ppp));
//						optionType = OPTION_TYPE_BUY_PPP;
//						scanOrBuyPPP.setVisibility(View.INVISIBLE);
//					}
//					pop.showAtLocation(findViewById(R.id.myppp), Gravity.CENTER, 0, 0);
//					backgroundAlpha(0.3f);
//				}
//
			}

		});
		pppPopLinearLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				pop.dismiss();
				return false;
			}
		});

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		System.out.println("ppp----->resume");
		if (application.getNeedRefreshPPPList()) {
			GetPPPList();
		}
	}


	//获取ppp数据
	private void GetPPPList() {
		// TODO Auto-generated method stub
//		dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.is_loading), false, false);
		dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
		list1.clear();
		hasOtherAvailablePPP = false;
		API.PPPlist.clear();
		getData();
	}

	//处理解析结果，并且从数据库中获取照片信息，新开线程，防止阻塞主线程
	private void getPhotoUrlFromDatabase() {
		database = dbHelper.getWritableDatabase(Common.SQLCIPHER_KEY);
		new Thread(){
			public void run() {
				Cursor cursor = null;
				for (int i = 0; i < API.PPlist.size(); i++) {
					DayOfPP dayOfPP = new DayOfPP();
					PPinfo pPinfo = API.PPlist.get(i);
					dayOfPP.number = pPinfo.photocount;
					dayOfPP.time = pPinfo.shootdate;
					dayOfPP.ppId = pPinfo.customerId;
					dayOfPP.isSelected = 0;
					System.out.println("isselect="+dayOfPP.isSelected);
					//之查找photoCode类似，并且shootTime一致的，降序之后的前3个值
					cursor = database.rawQuery("select * from " + Common.PHOTOPASS_INFO_TABLE + 
							" where photoCode like ? and shootTime=? order by shootOn desc limit 3", new String[] {"%" + dayOfPP.ppId + "%", dayOfPP.time});
					if (cursor.moveToFirst()) {
						do {
							if (dayOfPP.pp_url1 == null || "".equals(dayOfPP.pp_url1)) {
								dayOfPP.pp_url1 = Common.PHOTO_URL + cursor.getString(6);//previewUrl_512

							}else if (dayOfPP.pp_url2 == null || "".equals(dayOfPP.pp_url2)) {
								dayOfPP.pp_url2 = Common.PHOTO_URL + cursor.getString(6);//previewUrl_512

							}else if (dayOfPP.pp_url3 == null || "".equals(dayOfPP.pp_url3)) {
								dayOfPP.pp_url3 = Common.PHOTO_URL + cursor.getString(6);//previewUrl_512
							}

						} while (cursor.moveToNext());
					}
					list3.add(0,dayOfPP);
//					Collections.sort(list3);
				}
				//处理完了，通知处理之后的信息
				if (cursor != null) {
					cursor.close();
				}
				database.close();
				mHandler.sendEmptyMessage(DEAL_PP_JSON_SUCCESS);
			};
		}.start();
	}

	/**
	 * 设置window的透明度
	 * @param bgAlpha 0-1   透明---不透明
	 */
	public void backgroundAlpha(float bgAlpha)  
	{  
		WindowManager.LayoutParams lp = getWindow().getAttributes();  
		lp.alpha = bgAlpha; //0.0-1.0  
		getWindow().setAttributes(lp);  
	} 

	private void getData() {
		if (API.PPPlist.size() ==0) {//没有数据，需要重新获取
			System.out.println("ppp = 0");
			API.getPPPSByUserId(sharedPreferences.getString(Common.USERINFO_TOKENID, null), mHandler);
		}else {//有数据
			System.out.println("ppp != 0");
			for (int i = 0; i < API.PPPlist.size(); i++) {
				System.out.println("load==========");
				DayOfPPP dayOfPPP = new DayOfPPP();
				PPPinfo ppPinfo = API.PPPlist.get(i);
				String bindddateString = ppPinfo.bindInfo.get(0).bindDate;
				System.out.println(bindddateString);
				bindddateString = bindddateString.replace("[", "").replace("]", "").replaceAll("\"", "").trim();
				System.out.println(bindddateString);
				String [] timeStrings = bindddateString.split(",");
				if (timeStrings.length>0) {
					dayOfPPP.time = timeStrings[0];

				}else {
					dayOfPPP.time = "";
				}
				dayOfPPP.pppId = ppPinfo.PPPCode;
				dayOfPPP.amount = ppPinfo.capacity;
//				String pplistString = ppPinfo.PPlist;
//				System.out.println(pplistString);
//				pplistString = pplistString.replace("[", "").replace("]", "").replaceAll("\"", "").trim();
				String pplistString = ppPinfo.bindInfo.get(0).customerId;

				System.out.println(pplistString);
				String[] ppStrings = pplistString.split(",");

				int count = 0;
				for (int j = 0; j < ppStrings.length; j++) {
					if (j==0) {
						System.out.println(j+ppStrings[0]);
						if (null!=ppStrings[0]&&!"".equals(ppStrings[0])) {

							dayOfPPP.pp1 = ppStrings[0];
							count++;
						}
					}else if (j==1) {
						if (null!=ppStrings[1]&&!"".equals(ppStrings[1])) {

							System.out.println(j+ppStrings[1]);
							dayOfPPP.pp2 = ppStrings[1];
							count++;
						}
					}else if (j==2) {
						if (null!=ppStrings[2]&&!"".equals(ppStrings[2])) {

							System.out.println(j+ppStrings[2]);
							dayOfPPP.pp3 = ppStrings[2];
							count++;
						}
					}
				}
				dayOfPPP.usedNumber = count;
				list1.add(dayOfPPP);
			}
			Collections.sort(list1);
			Message message = mHandler.obtainMessage();
			message.what = 1;
			mHandler.sendMessage(message);
			application.setNeedRefreshPPPList(false);
		}
	}

	//退出app进行的判断，判断是否是栈中的唯一一个app，如果是，启动主页
	private void doBack() {
		// TODO Auto-generated method stub
		if (AppManager.getInstance().getActivityCount() == 1) {//一个activity的时候
			Intent intent = new Intent(this, MainTabActivity.class);
			startActivity(intent);
		}
		finish();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			doBack();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent i;
		switch (v.getId()) {
		case R.id.back://后退按钮
			doBack();
			break;

		case R.id.ppp_setting://设置按钮   + 按钮
			
		  
		    int[] location = new int[2];  
		    setting.getLocationOnScreen(location);
//		    System.out.println(" location x"+location[0]+"_location:"+location[1]);
//		    System.out.println(" width:"+setting.getWidth()+"_height:"+setting.getHeight());
//		    System.out.println("screen width:"+ScreenUtil.getScreenWidth(getApplicationContext()));
//		    System.out.println("pop width:"+pppPop.getWidth());
//		    pppPop.showAtLocation(setting, Gravity.NO_GRAVITY, location[0], location[1]+setting.getHeight()-10);
		    
//		    pppPop.showAtLocation(setting, Gravity.NO_GRAVITY, location[0],
//		    		location[1]+setting.getHeight()-10);
		    pppPop.showAsDropDown(setting);
			break;



		case R.id.option_imageView://有多个操作，1.bind，2.scan，3.use other，4.buy
		case R.id.option_textView:
			if (pop.isShowing()) {
				pop.dismiss();
			}
			switch (optionType) {
			case OPTION_TYPE_BUY_PPP://买ppp
				i = new Intent(this,PPPDetailProductActivity.class);
				startActivity(i);
				break;

			case OPTION_TYPE_CHOOSE_PP_TO_BIND://绑定pp
				Bundle bundle = new Bundle();
				bundle.putParcelableArrayList("pp", list3);
				bundle.putParcelable("ppp", list1.get(currentPosition));
				i = new Intent(this, SelectPPActivity.class);
				i.putExtras(bundle);
				startActivity(i);
				break;

			case OPTION_TYPE_SCAN_PP://扫描新的pp
				i = new Intent(this, MipCaptureActivity.class);
				i.putExtra("type", "pp");//只扫描pp
				startActivityForResult(i, PP_CODE);
				break;

			case OPTION_TYPE_USE_OTHER_PPP://使用其他ppp
				//list回滚到最顶端
				listPPP.setSelection(0);
				break;

			default:
				break;
			}
			break;

		case R.id.scan_or_buy://扫描pp或者购买ppp
			System.out.println("onclick");
			if (pop.isShowing()) {
				pop.dismiss();
			}
			if (list1.get(currentPosition).usedNumber == list1.get(currentPosition).amount) {//已经用完的，按钮的作用是购买
				i = new Intent(this,PPPDetailProductActivity.class);
				startActivity(i);
			}else {//扫描pp
				i = new Intent(this, MipCaptureActivity.class);
				i.putExtra("type", "pp");//只扫描pp
				startActivityForResult(i, PP_CODE);
			}
			break;

		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		// 判断扫描返回的结果
		if (resultCode != RESULT_OK) {
			return;
		}
		if (data.getStringExtra("result").equals("pppOK")) {//ppp绑定成功，需要重新获取ppp信息
			GetPPPList();
			//			dialog = ProgressDialog.show(this, "Loading ... ", "Loading, please wait...", false, false);
			//			list1.clear();
			//			hasOtherAvailablePPP = false;
			//			API.PPPlist.clear();
			//			getData();
		}else if (data.getStringExtra("result").equals("failed")) {//扫描失败
			System.out.println("------>"+data.getStringExtra("errorType"));
			if (data.getStringExtra("errorType").equals("incomplete")) {
				errorMessage = "incomplete";
			}else if (data.getStringExtra("errorType").equals("invalidCode")) {
				errorMessage = getString(R.string.invalidecode);
			}else if (data.getStringExtra("errorType").equals("errQueryUser")) {
				errorMessage = "errQueryUser";
			}else if (data.getStringExtra("errorType").equals("PPHasBind")) {
				if (requestCode == PPP_CODE) {
					errorMessage = getString(R.string.not_ppp_card);
				}else {
					errorMessage = "PPHasBind";
				}
			}else if (data.getStringExtra("errorType").equals("errQueryPPP")) {
				errorMessage = "errQueryPPP";
			}else if (data.getStringExtra("errorType").equals("noPaidForPPP")) {
				errorMessage = "noPaidForPPP";
			}else if (data.getStringExtra("errorType").equals("invalidPPP")) {
				errorMessage = "invalidPPP";
			}else if (data.getStringExtra("errorType").equals("errQueryPhoto")) {
				errorMessage = "errQueryPhoto";
			}else if (data.getStringExtra("errorType").equals("PPPHasBind")) {
				if (requestCode == PP_CODE) {
					errorMessage = getString(R.string.not_pp_card);
				}else {
					errorMessage = "PPPHasBind";
				}
			}
			customdialog = new CustomDialog.Builder(MyPPPActivity.this)
			.setMessage(errorMessage) 
			.setNegativeButton(null, new DialogOnClickListener(false, null, false))
			.setPositiveButton(getResources().getString(R.string.dialog_ok1), new DialogOnClickListener(false, null, false))
			.setCancelable(false)
			.create();
			customdialog.show();
		}else if (data.getStringExtra("result").equals("notSame")) {//卡片类型不一致
			//初始化dialog
			customdialog = new CustomDialog.Builder(MyPPPActivity.this)
			.setMessage((requestCode == PP_CODE)?getString(R.string.not_pp_card):getString(R.string.not_ppp_card)) 
			.setNegativeButton(null, new DialogOnClickListener(false, null, false))
			.setPositiveButton(getResources().getString(R.string.dialog_ok1), new DialogOnClickListener(false, null, false))
			.setCancelable(false)
			.create();
			customdialog.show();
		}else {//返回pp码，弹框，询问是否绑定
			
			customdialog = new CustomDialog.Builder(MyPPPActivity.this)
			.setMessage(getString(R.string.bind_pp_now)) 
			.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogOnClickListener(true, data.getStringExtra("result"), data.getBooleanExtra("hasBind", false)))
			.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogOnClickListener(true, data.getStringExtra("result"), data.getBooleanExtra("hasBind", false)))
			.setCancelable(false)
			.create();
			customdialog.show();
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}

	//对话框监听类
	private class DialogOnClickListener implements DialogInterface.OnClickListener{

		private boolean needBind;
		private String PPCode;
		private boolean needBindToUser;
		public DialogOnClickListener(boolean needBind, String ppCode, boolean needBindToUser) {
			this.needBind = needBind;
			this.PPCode = ppCode;
			this.needBindToUser = needBindToUser;
		}

		@Override
		public void onClick(DialogInterface dialog1, int which) {
			// TODO Auto-generated method stub
			switch (which) {
			case DialogInterface.BUTTON_POSITIVE:
				System.out.println("ok");
				//如果点击ok，则自动绑定，首先要绑定要user上，然后再绑定到ppp上
				if (needBind) {
//					dialog = ProgressDialog.show(MyPPPActivity.this, getString(R.string.loading___), getString(R.string.is_loading), false, false);
					dialog = CustomProgressDialog.show(MyPPPActivity.this, getString(R.string.is_loading), false, null);
					if (needBindToUser) {//是否已经绑定，如果已经绑定，则直接绑定到ppp，如果没有绑定，先绑定到user，在绑定到ppp
						//已经被绑定了，所以直接绑定ppp
						JSONArray pps = new JSONArray();
						pps.put(PPCode);
						API.bindPPsToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null), pps, "", list1.get(currentPosition).pppId, mHandler);
					}else {
						//没有被绑定，则先绑到user，再绑到ppp
						RequestParams params = new RequestParams();
						params.put(Common.USERINFO_TOKENID, sharedPreferences.getString(Common.USERINFO_TOKENID, ""));
						params.put(Common.CUSTOMERID, PPCode);
						HttpsUtil.get(Common.BASE_URL+Common.ADD_CODE_TO_USER, params, new JsonHttpResponseHandler(){
							@Override
							public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
								// TODO Auto-generated method stub
								super.onSuccess(statusCode, headers, response);
								if(statusCode == 200){
									//绑定成功
									JSONArray pps = new JSONArray();
									pps.put(PPCode);
									API.bindPPsToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null), pps, "", list1.get(currentPosition).pppId, mHandler);
								}
							}
							@Override
							public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
								// TODO Auto-generated method stub
								super.onFailure(statusCode, headers, throwable, errorResponse);
								//绑定失败
								dialog.dismiss();
								newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
							}
						});
					}
				}
				break;

			case DialogInterface.BUTTON_NEGATIVE:
				System.out.println("no");
				break;

			default:
				break;
			}
			dialog1.dismiss();
		}

	}
}
