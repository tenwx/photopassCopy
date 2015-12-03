package com.pictureair.photopass.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.MyApplication;
import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.ListOfPPAdapter;
import com.pictureair.photopass.customDialog.CustomDialog;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.PPPinfo;
import com.pictureair.photopass.entity.PPinfo;
import com.pictureair.photopass.entity.PhotoInfo;
import com.pictureair.photopass.service.DownloadService;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;
import com.pictureair.photopass.widget.NoNetWorkOrNoCountView;
import com.pictureair.photopass.widget.XListViewHeader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/** 显示用户所有的PP或某张PP+可绑定的PP */
public class MyPPActivity extends BaseActivity implements OnClickListener {
	private ImageView back;
	private ListView listPP;
	private ImageView delete;
	private ListOfPPAdapter listPPAdapter;
	private ArrayList<PPinfo> showPPCodeList;// 需要显示的List

	private SharedPreferences sharedPreferences;
	private PictureAirDbManager pictureAirDbManager;

	private static final int UPDATE_UI = 10000;
	private static final int DELETE_PHOTO = 10001;
	public static boolean isDeletePhoto = false;//是否是编辑状态
	private MyToast myToast;
	
	private MyApplication myApplication;
	private int selectedCurrent = -1;
	private int selectedTag = -1;
	private String selectedPhotoId = null;//记录已经购买了的照片的photoId
	
	private NoNetWorkOrNoCountView netWorkOrNoCountView;
	private CustomProgressDialog customProgressDialog;
	
	boolean isSeletePP;
	private TextView tvTitle;
	
	//selectPP 中需要的。
	private TextView ok;
	private PPPinfo dppp;
	private ArrayList<PhotoInfo> tempPhotoLists; //保存选中的 pp。 （准备升级PP＋的pp）
	private CustomProgressDialog dialog;
	private CustomDialog customdialog; //  对话框
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case UPDATE_UI:
				showPPCodeList = pictureAirDbManager.getPPCodeInfo1ByPPCodeList(showPPCodeList,1);// 根据条码从数据库获取图片
				// 更新界面
				if (showPPCodeList != null && showPPCodeList.size() > 0) {
					if (!isDeletePhoto) {
						delete.setVisibility(View.VISIBLE);
					}
					listPPAdapter.refresh(showPPCodeList);
				}else {
					listPPAdapter.refresh(new ArrayList<PPinfo>());
					delete.setVisibility(View.GONE);
				}

				break;
				
			case API.HIDE_PP_SUCCESS:
				// 请求删除API成功
				JSONObject objectSuccess;
				try {
					Log.v("=========", "请求删除API" + msg.obj.toString());
					objectSuccess = new JSONObject(msg.obj.toString());
					boolean result = objectSuccess.getBoolean("success");
					if (result) {
						Log.v("=========", "删除成功。。。");
						//更新界面
						if (showPPCodeList != null && showPPCodeList.size() > 0) {
							API.getPPSByUserId(
									sharedPreferences.getString(Common.USERINFO_TOKENID, null),
									mHandler);
//							showPPCodeList.remove(deletePosition);
//							listPPAdapter.refresh(showPPCodeList);
						}
					} else {
						Log.v("=========", "删除失败。。。");
					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
				
			case API.HIDE_PP_FAILED:
				// 请求删除API失败
					Log.v("=========", "请求删除API" + msg.obj.toString());
					myToast.setTextAndShow(R.string.http_failed,Toast.LENGTH_LONG);

				break;
			case DELETE_PHOTO:
				//更新界面
				if (showPPCodeList != null && showPPCodeList.size() >= 0) {
					listPPAdapter.refresh(showPPCodeList);
				}
				break;

			case API.GET_PPS_SUCCESS:// 获取pp列表成功
				JSONObject ppsJsonObject = (JSONObject) msg.obj;
				System.out.println("pps===" + ppsJsonObject);
				if (ppsJsonObject.has("PPList")) {
					showPPCodeList.clear();
					try {
						JSONArray pplists = ppsJsonObject
								.getJSONArray("PPList");
						PPinfo ppCodeInfo = null;
						String ppcode = null;
						int isupgrade = 0;
						boolean createnew = false;
						// 遍历所有pplist，如果有重复的pp，isUpgrade属性取升级过的值，图片选最新的图片(如果图片数量为零，就不查找)
						for (int i = 0; i < pplists.length(); i++) {
							JSONObject pplist = pplists.getJSONObject(i);
							ppcode = pplist.getString("customerId");
							isupgrade = pplist.getInt("isUpgrade");
							createnew = false;
							// 查看是否有重复的ppcode，需要更新isupgrade和图片属性
							for (int j = 0; j < showPPCodeList.size(); j++) {
								if (ppcode.equals(showPPCodeList.get(j).getPpCode())) {
									createnew = true;
									ppCodeInfo = showPPCodeList.get(j);
									if (ppCodeInfo.getIsUpgrade() == 1) {

									} else {
										if (isupgrade == 1) {
											ppCodeInfo.setIsUpgrade(1);
											ppCodeInfo.setPhotoCount(ppCodeInfo.getPhotoCount()+pplist.getInt("photoCount"));
											System.out
													.println("changing------------");
										}
									}
									break;
								}
							}
							if (!createnew) {
								ppCodeInfo = new PPinfo();
								ppCodeInfo.setPpCode(pplist
										.getString("customerId"));
								ppCodeInfo.setPhotoCount(pplist.getInt("photoCount"));
								ppCodeInfo.setIsUpgrade(pplist.getInt("isUpgrade")); 
								ppCodeInfo.setShootDate(pplist
										.getString("shootDate"));
								ppCodeInfo.setIsHidden(pplist.getInt("isHidden"));
								showPPCodeList.add(ppCodeInfo);
							}
						}
//						System.out.println("ppcodelist size = "
//								+ pPCodeList.size());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					updateUI(UPDATE_UI);
					customProgressDialog.dismiss();
				} else {
					System.out.println("pp size == 0");
				}
				listPP.setVisibility(View.VISIBLE);
				netWorkOrNoCountView.setVisibility(View.GONE);
				break;
				
			case API.GET_PPS_FAILED:// 获取pp列表失败
//				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				customProgressDialog.dismiss();
				netWorkOrNoCountView.setVisibility(View.VISIBLE);
				netWorkOrNoCountView.setResult(R.string.no_network, R.string.click_button_reload, R.string.reload, R.drawable.no_network, mHandler, true);
				listPP.setVisibility(View.INVISIBLE);
				break;
			case NoNetWorkOrNoCountView.BUTTON_CLICK_WITH_RELOAD://noView的按钮响应重新加载点击事件
				customProgressDialog = CustomProgressDialog.show(MyPPActivity.this, getString(R.string.is_loading), false, null);
				API.getPPSByUserId(sharedPreferences.getString(Common.USERINFO_TOKENID, null),mHandler);
				break;
				
		    // seletePP的页面
			case 2222:
				listPPAdapter = new ListOfPPAdapter(showPPCodeList, MyPPActivity.this, null, null,true,mHandler,dppp);
				listPP.setAdapter(listPPAdapter);
				
				if (showPPCodeList.size() == 0) {
					ok.setEnabled(false);
				}
				break;
			case 2:
				ok.setText(formaStringPPP(msg.arg1 - dppp.bindInfo.size(), dppp.capacity - dppp.bindInfo.size()));
				break;
				
			case API.BIND_PP_FAILURE://网络获取失败
				if (msg.obj.toString().equals("PPHasUpgraded")) {//提示已经绑定
					myToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
				}else {//获取失败
					myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
					listPP.setVisibility(View.GONE);
				}
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				break;

			case API.FAILURE://连接失败
				if (msg.obj.toString().equals("PPHasUpgraded")) {//提示已经绑定
					myToast.setTextAndShow(R.string.select_pp_hasUpgraded, Common.TOAST_SHORT_TIME);
				}else {//获取失败
					myToast.setTextAndShow(R.string.select_bind_pp_faile, Common.TOAST_SHORT_TIME);
				}
				break;

			case API.SUCCESS://绑定成功
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(Common.NEED_FRESH, true);
				editor.commit();
				((MyApplication)getApplication()).setNeedRefreshPPPList(true);

				boolean notFirstGoBuyOnePhotoFlag = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_NOT_FIRST_BUY_ONE_PHOTO, sharedPreferences.getString(Common.USERINFO_ID, ""));  //不是第一次。
				boolean syncFlag = pictureAirDbManager.checkFirstBuyPhoto(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
				if (!notFirstGoBuyOnePhotoFlag) {
					//如果没有设置过。
					if (syncFlag) {
						// 下载。
						downloadPhotoList();
					}else{
						//弹框提示
						customdialog = new CustomDialog.Builder(MyPPActivity.this)
						.setMessage(getResources().getString(R.string.dialog_sync_message)) 
						.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								//结束第一次的状态。
								pictureAirDbManager.insertSettingStatus(Common.SETTING_NOT_FIRST_BUY_ONE_PHOTO, sharedPreferences.getString(Common.USERINFO_ID, ""));
								customdialog.dismiss();
								goIntent();
							}
						})
						.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								// TODO Auto-generated method stub
								//记录用户的设置
								pictureAirDbManager.insertSettingStatus(Common.SETTING_SYNC, sharedPreferences.getString(Common.USERINFO_ID, ""));
								//结束第一次的状态。
								pictureAirDbManager.insertSettingStatus(Common.SETTING_NOT_FIRST_BUY_ONE_PHOTO, sharedPreferences.getString(Common.USERINFO_ID, ""));
								//下载
								downloadPhotoList();
								customdialog.dismiss();
							}
						})
						.setCancelable(false)
						.create();
						customdialog.show();
					}

				}else{
					if (syncFlag) {
						// 下载。
						downloadPhotoList();
					}else{
						goIntent();
					}
				}

				//				Intent intent = new Intent(SelectPPActivity.this, MyPPPActivity.class);
				//				API.PPPlist.clear();
				//				if (dialog.isShowing()) {
				//					dialog.dismiss();
				//				}
				//				startActivity(intent);
				//				finish();
				break;

			default:
				break;
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_pp);
		
		initView_common();
		isSeletePP = getIntent().getBooleanExtra("isSeletePP", false);
		Log.e("cccc", "isSeletePP :"+isSeletePP);
		if (isSeletePP) {
			initView_selectPP();
			tvTitle.setText(R.string.selectionpp);
		}else{
			initView_notSelectPP();
			tvTitle.setText(R.string.mypage_pp);
		}
		
	}
	
	private void initView_common(){
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME,
				MODE_PRIVATE);
		AppManager.getInstance().addActivity(this);
		listPP = (ListView) findViewById(R.id.list_pp);
		tvTitle = (TextView) findViewById(R.id.mypp);
		back = (ImageView) findViewById(R.id.back);
		myToast = new MyToast(this);
		netWorkOrNoCountView = (NoNetWorkOrNoCountView) findViewById(R.id.nonetwork_view);
		
		pictureAirDbManager = new PictureAirDbManager(this);
		back.setOnClickListener(this);
	}
	
	private void initView_selectPP(){
		dppp = getIntent().getParcelableExtra("ppp");
		ok = (TextView) findViewById(R.id.ok);
		ok.setVisibility(View.VISIBLE);
		ok.setOnClickListener(this);
		ok.setText(formaStringPPP(0, dppp.capacity - dppp.bindInfo.size()));
		getPhotoUrlFromDatabase();
	}

	private void initView_notSelectPP() {
		delete = (ImageView) findViewById(R.id.cancel);
		myApplication = (MyApplication) getApplication();
		delete.setOnClickListener(this);
				// 获取PP信息
//		pPCodeList = new ArrayList<PPCodeInfo>();
		customProgressDialog = CustomProgressDialog.show(MyPPActivity.this, getString(R.string.is_loading), false, null);
		API.getPPSByUserId(
				sharedPreferences.getString(Common.USERINFO_TOKENID, null),
				mHandler);
		// pPCodeList = getIntent().getParcelableArrayListExtra("pPCodeList");
		showPPCodeList = new ArrayList<PPinfo>();
		listPPAdapter = new ListOfPPAdapter(showPPCodeList, MyPPActivity.this,
				new ListOfPPAdapter.doShowPhotoListener() {

					@Override
					public void previewPhoto(int position, int tag) {
						// TODO Auto-generated method stub
						// 进入图片详情
						showPhotoDetail(position, tag);
					}
				}, new ListOfPPAdapter.doDeletePhotoListener() {

					@Override
					public void deletePhoto(int position) {
						// TODO Auto-generated method stub
						deleteAPI(position);// 提交删除PP
					}
				},false,null,null);

		listPP.addHeaderView(new XListViewHeader(this));
		listPP.setAdapter(listPPAdapter);
		listPP.setHeaderDividersEnabled(true);
		listPP.setFooterDividersEnabled(false);

		if (showPPCodeList == null || showPPCodeList.size() <= 0) {
			return;
		}
		
		updateUI(UPDATE_UI);
	}

	// 更新界面
	public void updateUI(int what) {
		Message message = mHandler.obtainMessage(what);
		mHandler.sendMessage(message);
	}

	public void showPhotoDetail(int curInedx, int tag) {
		Log.v("============", "showPhotoDetail size : " + showPPCodeList.size());
		if (showPPCodeList == null || showPPCodeList.size() <= 0) {
			return;
		}
		PhotoInfo photoInfo = showPPCodeList.get(curInedx)
				.getSelectPhotoItemInfos().get(tag);
		if (photoInfo.photoPathOrURL.equals("")) {
			return;
		}
		selectedTag = tag;
		selectedCurrent = curInedx;
		selectedPhotoId = showPPCodeList.get(curInedx).getSelectPhotoItemInfos().get(tag).photoId;
		myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_MYPHOTOPASS);
		
		Log.v("============", "showPhotoDetail curIndex : " + curInedx + "url-->" + photoInfo.photoPathOrURL);
		Intent i = new Intent();
		
		ArrayList<PhotoInfo> photopassArrayList = new ArrayList<PhotoInfo>();
		//需要将picList中的图片数据全部转到成photopassArrayList
//		for (int j = 0; j < picList.size(); j++) {
			photopassArrayList.addAll(showPPCodeList.get(curInedx)
					.getSelectPhotoItemInfos());
//		}
			for (int j = 0; j < photopassArrayList.size(); j++) {
				photopassArrayList.get(j).onLine = 1;
			}
//			photoInfo.onLine = 1;
		i.setClass(this, PreviewPhotoActivity.class);
		i.putExtra("activity", "myPPActivity");
		i.putExtra("position", photopassArrayList.indexOf(photoInfo)+"");//在那个相册中的位置
		i.putExtra("photoId", photoInfo.photoId);
		i.putExtra("photos", photopassArrayList);//那个相册的全部图片路径
		i.putExtra("targetphotos", myApplication.magicPicList);
		
		startActivity(i);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:// 返回按钮
			if (isDeletePhoto) {
				isDeletePhoto = false;
				// 在删除状态下返回，不提交数据
				back.setImageResource(R.drawable.back_white);
				delete.setVisibility(View.VISIBLE);
				Log.v("===========", "取消删除......");
				updateUI(UPDATE_UI);//更新界面

			} else {
				finish();
			}

			break;

		case R.id.cancel:// 删除按钮
			Log.v("==============",
					"点击删除按钮 showPPCodeList" + showPPCodeList.size());
			if (isDeletePhoto) {
				isDeletePhoto = false;
				back.setImageResource(R.drawable.back_white);
				delete.setVisibility(View.VISIBLE);
				updateUI(DELETE_PHOTO);
			} else {
				if (showPPCodeList.size() == 0) {
					return;
				}
				isDeletePhoto = true;
				back.setImageResource(R.drawable.cancel_my_pp);
				delete.setVisibility(View.GONE);
				updateUI(DELETE_PHOTO);
			}
			break;
	    //  seletePP 界面的 点击事件
		case R.id.ok://确认绑定按钮
			HashMap<Integer, Boolean> map = listPPAdapter.getMap();
			if (map.size() == 0) {
				myToast.setTextAndShow(R.string.select_your_pp, Common.TOAST_SHORT_TIME);
				return;
			}
			for (int i = 0; i < map.size(); i++) {
				System.out.println("->"+map.get(i));
			}
			JSONArray pps = new JSONArray();
			tempPhotoLists = new ArrayList<PhotoInfo>();
			//			String binddate = null;
			System.out.println("size="+map.size());
			for (int j = 0; j < showPPCodeList.size(); j++) {
				JSONObject jsonObject = new JSONObject();
				if (null != map.get(j) && map.get(j)) {
					System.out.println(showPPCodeList.get(j).getPpCode());
					try {
						PhotoInfo photoInfo = new PhotoInfo();
						jsonObject.put("code", showPPCodeList.get(j).getPpCode());
						jsonObject.put("bindDate", showPPCodeList.get(j).getShootDate());
						photoInfo.photoId = showPPCodeList.get(j).getPpCode();
						photoInfo.shootTime = showPPCodeList.get(j).getPpCode();
						tempPhotoLists.add(photoInfo);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					pps.put(jsonObject);
				}
			}
			//			if (null == binddate) {
			//			}else {
			//			dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.is_loading), false, true);
			dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), true, null);
			API.bindPPsDateToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null),pps, dppp.PPPCode, mHandler);
			break;
		default:
			break;
		}
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isSeletePP) {
			
		}else{
//			System.out.println("MyPPActivity----->"+myApplication.getRefreshViewAfterBuyBlurPhoto());
			if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASSPAYED)) {
				System.out.println("deal data after bought photo");
				myApplication.setRefreshViewAfterBuyBlurPhoto("");
				//找到之前选择的图片的索引值，并且更新购买信息
				showPPCodeList.get(selectedCurrent).getSelectPhotoItemInfos().get(selectedTag).isPayed = 1;
				selectedCurrent = -1;
				selectedTag = -1;
				listPPAdapter.notifyDataSetChanged();
				//根据photoId，更新数据库中的字段
				pictureAirDbManager.updatePhotoBought(selectedPhotoId);
				selectedPhotoId = null;
			}
		}
	}
	
	//处理解析结果，并且从数据库中获取照片信息，新开线程，防止阻塞主线程
	private void getPhotoUrlFromDatabase() {
		new Thread(){
			public void run() {
				Log.e("API.PPlist.size()", "API.PPlist.size():"+API.PPlist.size());
				showPPCodeList = pictureAirDbManager.getPPCodeInfo1ByPPCodeList(API.PPlist, 2);
				Log.e("＝＝＝＝＝＝", "showPPCodeList："+showPPCodeList.size());
				mHandler.sendEmptyMessage(2222);
			};
		}.start();
	}
	// 请求删除API
	public boolean deleteAPI(int position) {
		RequestParams params = new RequestParams();
		String tokenId = sharedPreferences.getString(Common.USERINFO_TOKENID,
				"");
		params.put("tokenId", tokenId);
		if (showPPCodeList == null || showPPCodeList.size() <= 0
				|| tokenId.equals("")) {
			return false;
		}

		JSONArray pps = new JSONArray();
//		for (int j = 0; j < showPPCodeList.size(); j++) {
			JSONObject jsonObject = new JSONObject();
			try {
				jsonObject.put("code", showPPCodeList.get(position).getPpCode());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			pps.put(jsonObject);
//		}
		params.put("pps", pps);
		API.hidePPs(params, mHandler);
		return false;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}
	
	
	private String formaStringPPP(int count1, int count2) {
		return String.format(getString(R.string.pp_ok), count1, count2);
	}
	
	//下载照片
	private void downloadPhotoList(){
		Log.e("＝＝＝＝＝", "downloadPhotoList");
		if (tempPhotoLists.size()>0) {
			for (int i = 0; i < tempPhotoLists.size(); i++) {
				download(pictureAirDbManager.getPhotoUrlByPhotoIDAndShootOn(tempPhotoLists.get(i).photoId, tempPhotoLists.get(i).shootTime));
			}
		}
		goIntent();
	}

	private void download(ArrayList<PhotoInfo> arrayList){
		Log.e("=======", "arrayList.size()："+arrayList.size());
		if (arrayList.size()>0) {
			Intent intent = new Intent(MyPPActivity.this,
					DownloadService.class);
			Bundle bundle = new Bundle();
			bundle.putParcelableArrayList("photos", arrayList);
			intent.putExtras(bundle);
			startService(intent);
		}
	}

	private void goIntent(){
		Intent intent = new Intent(MyPPActivity.this, MyPPPActivity.class);
		API.PPPlist.clear();
		if (dialog.isShowing()) {
			dialog.dismiss();
		}
		startActivity(intent);
		finish();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	
	
	
}
