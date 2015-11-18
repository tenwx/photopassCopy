package com.pictureAir;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.R.integer;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.pictureAir.adapter.ListOfPPAdapter1;
import com.pictureAir.blur.BlurActivity;
import com.pictureAir.db.PhotoInfoDBHelper;
import com.pictureAir.entity.PPCodeInfo;
import com.pictureAir.entity.PPCodeInfo1;
import com.pictureAir.entity.PhotoInfo;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.MyToast;
import com.pictureAir.widget.XListViewHeader;

/** 显示用户所有的PP或某张PP+可绑定的PP */
public class MyPPActivity extends Activity implements OnClickListener {
	private ImageView back;
	private ListView listPP;
	private ImageView delete;
	private ListOfPPAdapter1 listPPAdapter;
	private ArrayList<PPCodeInfo> pPCodeList;
	private ArrayList<PPCodeInfo1> showPPCodeList;// 需要显示的List
	// private ArrayList<PPCodeInfo1> showDeletePPCodeList;// 删除后的List
	// private ArrayList<PPCodeInfo1> saveDeletePPCodeList;// 存放删的List

	private SQLiteDatabase database;
	private PhotoInfoDBHelper dbHelper;
	private SharedPreferences sharedPreferences;

	private static final int UPDATE_UI = 10000;
	private static final int DELETE_PHOTO = 10001;
	public static final int REQUEST_DELETE_PHOTO_SUCCESS = 10002;
	public static final int REQUEST_DELETE_PHOTO_FAIL = 10005;
	private List<PhotoInfo> selectPhotoItemInfos;
	private List<String> urlList;// 存放图片路径
	public static boolean isDeletePhoto = false;//是否是编辑状态
	private MyToast myToast;
	
	private static  int deletePosition;
	private MyApplication myApplication;
	private int selectedCurrent = -1;
	private int selectedTag = -1;
	private String selectedPhotoId;//记录已经购买了的照片的photoId

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case UPDATE_UI:
				showPPCodeList = getPhotoUrlFromDatabase();// 根据条码从数据库获取图片
				// 更新界面
				if (showPPCodeList != null && showPPCodeList.size() > 0) {
					if (!isDeletePhoto) {
						delete.setVisibility(View.VISIBLE);
					}
					listPPAdapter.refresh(showPPCodeList);
				}else {
					listPPAdapter.refresh(new ArrayList<PPCodeInfo1>());
					delete.setVisibility(View.GONE);
				}

				break;
			case REQUEST_DELETE_PHOTO_SUCCESS:
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
				
			case REQUEST_DELETE_PHOTO_FAIL:
				// 请求删除API失败
				JSONObject objectFail;
				try {
					Log.v("=========", "请求删除API" + msg.obj.toString());
					objectFail = new JSONObject(msg.obj.toString());
					myToast.setTextAndShow(R.string.http_failed,Toast.LENGTH_LONG);
//					boolean result = objectFail.getBoolean("--------");
//					if (result) {
//						Log.v("=========", "删除成功。。。");
//					} else {
//						Log.v("=========", "删除失败。。。");
//					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

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
					pPCodeList.clear();
					try {
						JSONArray pplists = ppsJsonObject
								.getJSONArray("PPList");
						PPCodeInfo ppCodeInfo = null;
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
							for (int j = 0; j < pPCodeList.size(); j++) {
								if (ppcode.equals(pPCodeList.get(j).ppCode)) {
									createnew = true;
									ppCodeInfo = pPCodeList.get(j);
									if (ppCodeInfo.isUpgrade == 1) {

									} else {
										if (isupgrade == 1) {
											ppCodeInfo.isUpgrade = 1;
											ppCodeInfo.photoCount += pplist
													.getInt("photoCount");
											System.out
													.println("changing------------");
										}
									}
									break;
								}
							}
							if (!createnew) {
								ppCodeInfo = new PPCodeInfo();
								ppCodeInfo.ppCode = pplist
										.getString("customerId");
								ppCodeInfo.photoCount = pplist
										.getInt("photoCount");
								ppCodeInfo.isUpgrade = pplist
										.getInt("isUpgrade");
								ppCodeInfo.shootDate = pplist
										.getString("shootDate");
								ppCodeInfo.isHidden = pplist.getInt("isHidden");
								pPCodeList.add(ppCodeInfo);
							}
						}
						System.out.println("ppcodelist size = "
								+ pPCodeList.size());
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					updateUI(UPDATE_UI);
				} else {
					System.out.println("pp size == 0");
				}
				break;
			case API.GET_PPS_FAILED:// 获取pp列表失败
				myToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
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
		setContentView(R.layout.my_pp_1);
		initView();
	}

	private void initView() {
		listPP = (ListView) findViewById(R.id.list_pp);
		back = (ImageView) findViewById(R.id.back);
		delete = (ImageView) findViewById(R.id.cancel);
		myApplication = (MyApplication) getApplication();
		delete.setOnClickListener(this);
		back.setOnClickListener(this);
		myToast = new MyToast(this);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME,
				MODE_PRIVATE);
		dbHelper = new PhotoInfoDBHelper(this, Common.PHOTOPASS_INFO_NAME,
				Common.PHOTOPASS_INFO_VERSION);
		AppManager.getInstance().addActivity(this);
		// 获取PP信息
		pPCodeList = new ArrayList<PPCodeInfo>();
		API.getPPSByUserId(
				sharedPreferences.getString(Common.USERINFO_TOKENID, null),
				mHandler);
		// pPCodeList = getIntent().getParcelableArrayListExtra("pPCodeList");
		showPPCodeList = new ArrayList<PPCodeInfo1>();
		// showDeletePPCodeList = new ArrayList<PPCodeInfo1>();
		// saveDeletePPCodeList = new ArrayList<PPCodeInfo1>();
		urlList = new ArrayList<String>();
		selectPhotoItemInfos = new ArrayList<PhotoInfo>();
		listPPAdapter = new ListOfPPAdapter1(showPPCodeList, MyPPActivity.this,
				new ListOfPPAdapter1.doShowPhotoListener() {

					@Override
					public void doShowPhotoListener(int position, int tag) {
						// TODO Auto-generated method stub
						// 进入图片详情
						showPhotoDetail(position, tag);
					}
				}, new ListOfPPAdapter1.doDeletePhotoListener() {

					@Override
					public void doDeletePhotoListener(int position) {
						// TODO Auto-generated method stub
						// 删除图片
						// Log.v("=========",
						// "doDeletePhotoListener showDeletePPCodeList: "
						// + showDeletePPCodeList.size());
						// Log.v("=========", "doDeletePhotoListener position: "
						// + position);
						// if (showDeletePPCodeList != null
						// && showDeletePPCodeList.size() > 0) {
						// saveDeletePPCodeList.add(showDeletePPCodeList
						// .get(position));
						// showDeletePPCodeList.remove(position);
						// updateUI(DELETE_PHOTO);
						// }
						deletePosition = position;
						deleteAPI(position);// 提交删除PP
//						updateUI(DELETE_PHOTO);
					}
				});

		listPP.addHeaderView(new XListViewHeader(this));
		listPP.setAdapter(listPPAdapter);
		listPP.setHeaderDividersEnabled(true);
		listPP.setFooterDividersEnabled(false);

		if (pPCodeList == null || pPCodeList.size() <= 0) {
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
		Log.v("============", "showPhotoDetail curIndex : " + curInedx
				+ "url-->" + photoInfo.photoPathOrURL);
		Intent i = new Intent();
		ArrayList<PhotoInfo> photopassArrayList = new ArrayList<PhotoInfo>();
		// PhotoInfo photopassmap = new PhotoInfo();
		// photopassmap.photoPathOrURL = showPPCodeList.get(curInedx)
		// .getSelectPhotoItemInfos().get(tag).photoPathOrURL;
		// photopassmap.photoThumbnail = showPPCodeList.get(curInedx)
		// .getSelectPhotoItemInfos().get(tag).photoThumbnail;
		// photopassmap.isPayed = showPPCodeList.get(curInedx)
		// .getSelectPhotoItemInfos().get(tag).isPayed;
		// photopassmap.photoId = showPPCodeList.get(curInedx)
		// .getSelectPhotoItemInfos().get(tag).photoId;
		// photopassmap.photoThumbnail_512 = showPPCodeList.get(curInedx)
		// .getSelectPhotoItemInfos().get(tag).photoThumbnail_512;
		// photopassmap.photoThumbnail_1024 = showPPCodeList.get(curInedx)
		// .getSelectPhotoItemInfos().get(tag).photoThumbnail_1024;
		photoInfo.onLine = 1;
		photopassArrayList.add(photoInfo);

		if (photoInfo.isPayed == 1) {
			System.out.println("has bought");
			i.setClass(this, PreviewPhotoActivity.class);
			i.putExtra("activity", "fragmentpage1");
			i.putExtra("flag", 1);// 哪个相册的标记
			i.putExtra("position", tag + "");// 在那个相册中的位置
			i.putExtra("photoId", photoInfo.photoId);
			i.putExtra("photos", photopassArrayList);// 那个相册的全部图片路径
			i.putExtra("targetphotos", new ArrayList<PhotoInfo>());
		} else {
			i.setClass(this, BlurActivity.class);
			Bundle bundle = new Bundle();
			bundle.putParcelable("photo", photoInfo);
			i.putExtras(bundle);
			selectedCurrent = curInedx;
			selectedTag = tag;
			selectedPhotoId = photoInfo.photoId;
			myApplication.setRefreshViewAfterBuyBlurPhoto(Common.FROM_MYPHOTOPASS);
		}
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
		default:
			break;
		}
	}

	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (myApplication.getRefreshViewAfterBuyBlurPhoto().equals(Common.FROM_MYPHOTOPASSPAYED)) {
			System.out.println("deal data after bought photo");
			myApplication.setRefreshViewAfterBuyBlurPhoto("");
			//找到之前选择的图片的索引值，并且更新购买信息
			showPPCodeList.get(selectedCurrent).getSelectPhotoItemInfos().get(selectedTag).isPayed = 1;
			selectedCurrent = -1;
			selectedTag = -1;
			listPPAdapter.notifyDataSetChanged();
			//根据photoId，更新数据库中的字段
			database = dbHelper.getWritableDatabase();
			database.execSQL("update "+Common.PHOTOPASS_INFO_TABLE+" set isPay = 1 where photoId = ?", new String[]{selectedPhotoId});
			database.close();
			selectedPhotoId = null;
//			Editor editor = sharedPreferences.edit();
//			editor.putBoolean(Common.NEED_FRESH, false);
//			editor.commit();
		}
	}
	
	
	// 处理解析结果，并且从数据库中获取照片信息，新开线程，防止阻塞主线程
	private ArrayList<PPCodeInfo1> getPhotoUrlFromDatabase() {
		showPPCodeList = new ArrayList<PPCodeInfo1>();
		database = dbHelper.getWritableDatabase();
		// int skipCount = 0;//设置跳过的数量
		// 获取需要显示的PP(去掉重复、隐藏的)
		for (int j = 0; j < pPCodeList.size(); j++) {
			if (j + 1 < pPCodeList.size()
					&& pPCodeList.get(j).ppCode
							.equals(pPCodeList.get(j + 1).ppCode)) {
				pPCodeList.remove(j);
			}
		}
		Cursor cursor = null;
		for (int i = 0; i < pPCodeList.size(); i++) {
			if (pPCodeList.get(i).isHidden == 1) {
				// skipCount ++;
				continue;
			}
			urlList = new ArrayList<String>();
			selectPhotoItemInfos = new ArrayList<PhotoInfo>();
			PPCodeInfo ppInfo = pPCodeList.get(i);
			Log.v("=======", "PP卡:" + ppInfo.ppCode);
			cursor = database.rawQuery("select * from "
					+ Common.PHOTOPASS_INFO_TABLE
					+ " where photoCode like ? order by shootOn desc",
					new String[] { "%" + ppInfo.ppCode + "%" });
			if (cursor != null && cursor.moveToFirst()) {
				do {
					// 获取图片路径
					urlList.add(cursor.getString(cursor
							.getColumnIndex("previewUrl")));
					PhotoInfo sInfo = new PhotoInfo();
					sInfo.photoId = cursor.getString(cursor
							.getColumnIndex("photoId"));
					sInfo.photoPathOrURL = cursor.getString(cursor
							.getColumnIndex("originalUrl"));
					sInfo.photoThumbnail = cursor.getString(cursor
							.getColumnIndex("previewUrl"));
					sInfo.photoThumbnail_512 = cursor.getString(cursor
							.getColumnIndex("previewUrl_512"));
					sInfo.photoThumbnail_1024 = cursor.getString(cursor
							.getColumnIndex("previewUrl_1024"));
					sInfo.photoPassCode = cursor.getString(cursor
							.getColumnIndex("photoCode"));
					sInfo.isPayed = Integer.valueOf(cursor.getString(cursor
							.getColumnIndex("isPay")));

					selectPhotoItemInfos.add(sInfo);
				} while (cursor.moveToNext());

			}
			Log.v("=======", "图片数量" + urlList.size());
			PPCodeInfo1 ppInfo1 = new PPCodeInfo1();
			ppInfo1.setPpCode(ppInfo.ppCode);
			ppInfo1.setShootDate(ppInfo.shootDate);
			// ppInfo1.setLocation(cursor.getString(cursor.getColumnIndex("location")));
			ppInfo1.setUrlList(urlList);
			ppInfo1.setSelectPhotoItemInfos(selectPhotoItemInfos);
			showPPCodeList.add(ppInfo1);
			// showPPCodeList.add(i - skipCount, ppInfo1);
		}
		// 处理完了，通知处理之后的信息
		if (cursor != null) {
			cursor.close();
		}
		database.close();
		return showPPCodeList;
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
}
