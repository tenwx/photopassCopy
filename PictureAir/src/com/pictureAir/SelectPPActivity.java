package com.pictureAir;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureAir.adapter.ListOfPPAdapter;
import com.pictureAir.db.PhotoInfoDBHelper;
import com.pictureAir.entity.DayOfPP;
import com.pictureAir.entity.DayOfPPP;
import com.pictureAir.entity.PPinfo;
import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.CustomProgressDialog;
import com.pictureAir.widget.MyToast;

/**显示用户所有的PP或某张PP+可绑定的PP*/
public class SelectPPActivity extends Activity implements OnClickListener {
	private ImageView back;
	private ListView listPP;
	private TextView ok;
	private ListOfPPAdapter listPPAdapter;
	private ArrayList<DayOfPP> list3;// 对应pp+可以绑定的pp
	private DayOfPPP dppp;
	private CustomProgressDialog dialog;
	private SharedPreferences sharedPreferences;
	private MyToast newToast;
	private SQLiteDatabase database;
	private PhotoInfoDBHelper dbHelper;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 2222:
				
				listPPAdapter = new ListOfPPAdapter(list3, SelectPPActivity.this, mHandler,dppp);
				listPP.setAdapter(listPPAdapter);
//				listPPAdapter.notifyDataSetChanged();
				if (list3.size() == 0) {
					ok.setEnabled(false);
				}
				break;
			case 2://选择或者不选择时候更改ok的值
				ok.setText(formaStringPPP(msg.arg1 - dppp.usedNumber, dppp.amount - dppp.usedNumber));
				break;

			case API.FAILURE://网络获取失败
				if (msg.obj.toString().equals("PPHasUpgraded")) {//提示已经绑定
					newToast.setTextAndShow("PP has upgraded", Common.TOAST_SHORT_TIME);
				}else {//获取失败
					newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
					listPP.setVisibility(View.GONE);
				}
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				break;

			case API.SUCCESS://绑定成功
				Editor editor = sharedPreferences.edit();
				editor.putBoolean(Common.NEED_FRESH, true);
				editor.commit();
				((MyApplication)getApplication()).setNeedRefreshPPPList(true);
				
				Intent intent = new Intent(SelectPPActivity.this, MyPPPActivity.class);
				API.PPPlist.clear();
				if (dialog.isShowing()) {
					dialog.dismiss();
				}
				startActivity(intent);
				finish();
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_pp);
		initView();
	}

	private void initView() {
		list3 = new ArrayList<DayOfPP>();
		dbHelper = new PhotoInfoDBHelper(this, Common.PHOTOPASS_INFO_NAME, Common.PHOTOPASS_INFO_VERSION);
		getPhotoUrlFromDatabase();
		
		listPP = (ListView) findViewById(R.id.list_pp);
		back = (ImageView) findViewById(R.id.back);
		ok = (TextView) findViewById(R.id.ok);
		ok.setOnClickListener(this);
		back.setOnClickListener(this);
		AppManager.getInstance().addActivity(this);
		newToast = new MyToast(this);
//		list3 
		//不需要接收 list。可以直接从API中获取到。
		dppp = getIntent().getParcelableExtra("ppp");
		
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		
		ok.setText(formaStringPPP(0, dppp.amount - dppp.usedNumber));
		
		//刷新数据
		
//		list3.clear();  
//        if(listPPAdapter!=null){  
//        	listPPAdapter.notifyDataSetChanged();  
//        }  
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back://返回按钮
			finish();
			break;

		case R.id.ok://确认绑定按钮
//			if (list3.size() == 0) {
//				newToast.setTextAndShow(R.string.select_your_pp, Common.TOAST_SHORT_TIME);
//				return;
//				
//			}
			HashMap<Integer, Boolean> map = listPPAdapter.getMap();
			if (map.size() == 0) {
				newToast.setTextAndShow(R.string.select_your_pp, Common.TOAST_SHORT_TIME);
				return;
			}
			for (int i = 0; i < map.size(); i++) {
				System.out.println("->"+map.get(i));
			}
			JSONArray pps = new JSONArray();
//			String binddate = null;
			System.out.println("size="+map.size());
			for (int j = 0; j < list3.size(); j++) {
				JSONObject jsonObject = new JSONObject();
				if (null != map.get(j) && map.get(j)) {
					System.out.println(list3.get(j).ppId);
					try {
						jsonObject.put("code", list3.get(j).ppId);
						jsonObject.put("bindDate", list3.get(j).time);
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
				API.bindPPsDateToPPP(sharedPreferences.getString(Common.USERINFO_TOKENID, null),pps, dppp.pppId, mHandler);
//			}
			break;
		default:
			break;
		}
	}

	private String formaStringPPP(int count1, int count2) {
		return String.format(getString(R.string.pp_ok), count1, count2);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}
	
	
	//处理解析结果，并且从数据库中获取照片信息，新开线程，防止阻塞主线程
		private void getPhotoUrlFromDatabase() {
			database = dbHelper.getWritableDatabase();
			new Thread(){
				public void run() {
					Cursor cursor = null;
					Log.e("API.PPlist.size()", "API.PPlist.size():"+API.PPlist.size());
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
								" where photoCode like ? and shootTime=? order by shootOn desc limit 12", new String[] {"%" + dayOfPP.ppId + "%", dayOfPP.time});
						if (cursor.moveToFirst()) {
							do {
								if (dayOfPP.pp_url1 == null || "".equals(dayOfPP.pp_url1)) {
									dayOfPP.pp_url1 = Common.PHOTO_URL + cursor.getString(6);//previewUrl_512
									Log.e("PhotoUrl", "url :"+dayOfPP.pp_url1);
								}else if (dayOfPP.pp_url2 == null || "".equals(dayOfPP.pp_url2)) {
									dayOfPP.pp_url2 = Common.PHOTO_URL + cursor.getString(6);//previewUrl_512
								}else if (dayOfPP.pp_url3 == null || "".equals(dayOfPP.pp_url3)) {
									dayOfPP.pp_url3 = Common.PHOTO_URL + cursor.getString(6);//previewUrl_512
								}else if(dayOfPP.pp_url4 == null || "".equals(dayOfPP.pp_url4)){
									dayOfPP.pp_url4 = Common.PHOTO_URL + cursor.getString(6);
								}else if(dayOfPP.pp_url5 == null || "".equals(dayOfPP.pp_url5)){
									dayOfPP.pp_url5 = Common.PHOTO_URL + cursor.getString(6);
								}else if(dayOfPP.pp_url6 == null || "".equals(dayOfPP.pp_url6)){
									dayOfPP.pp_url6 = Common.PHOTO_URL + cursor.getString(6);
								}else if(dayOfPP.pp_url7 == null || "".equals(dayOfPP.pp_url7)){
									dayOfPP.pp_url7 = Common.PHOTO_URL + cursor.getString(6);
								}else if(dayOfPP.pp_url8 == null || "".equals(dayOfPP.pp_url8)){
									dayOfPP.pp_url8 = Common.PHOTO_URL + cursor.getString(6);
								}else if(dayOfPP.pp_url9 == null || "".equals(dayOfPP.pp_url9)){
									dayOfPP.pp_url9 = Common.PHOTO_URL + cursor.getString(6);
								}else if(dayOfPP.pp_url10 == null || "".equals(dayOfPP.pp_url10)){
									dayOfPP.pp_url10 = Common.PHOTO_URL + cursor.getString(6);
								}else if(dayOfPP.pp_url11 == null || "".equals(dayOfPP.pp_url11)){
									dayOfPP.pp_url11 = Common.PHOTO_URL + cursor.getString(6);
								}else if(dayOfPP.pp_url12 == null || "".equals(dayOfPP.pp_url12)){
									dayOfPP.pp_url12 = Common.PHOTO_URL + cursor.getString(6);
								}

							} while (cursor.moveToNext());
						}
						list3.add(0,dayOfPP);
						
					}
//					Collections.sort(list3);
					//处理完了，通知处理之后的信息
					if (cursor != null) {
						cursor.close();
					}
					database.close();
					mHandler.sendEmptyMessage(2222);
				};
			}.start();
		}
}
