package com.pictureAir;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.pictureAir.util.AppManager;

/**
 * 国家列表
 * @author bass
 *
 */
public class NationalListSelectionActivity extends BaseActivity {
	// 组件
	private ListView lv;
	private myAdapter adapter;
	private ImageView back;// 返回
	// 选中的
	private String countryCode = "";
	private String country = "";
	// 只有国家名称，的列表
	private String[] counryList;
	private String codelist[] = { "" };// 区号集合

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_national_list_selection);

		initView();
		getDate();

		adapter = new myAdapter(NationalListSelectionActivity.this,
				R.layout.national_list_selection_item, counryList);
		lv.setAdapter(adapter);

	}

	private void initView() {
		AppManager.getInstance().addActivity(this);// 添加到activity管理器

		back = (ImageView) findViewById(R.id.login_back);
		lv = (ListView) findViewById(R.id.listView_nationalList_Selection);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> v, View view, int position,
					long arg3) {
				country = v.getItemAtPosition(position).toString();// 得到国家名称
				countryCode = codelist[position];// 区号

				Intent intent = new Intent();
				intent.putExtra("country", country);
				intent.putExtra("countryCode", countryCode);
				setResult(111, intent);
				finish();
			}
		});

	}

	/** 读取数据，之后传给适配器 */
	private void getDate() {
		String nationalList[] = getResources().getStringArray(R.array.smssdk_country);
		counryList = new String[nationalList.length];
		codelist = new String[nationalList.length];

		for (int i = 0; i < nationalList.length; i++) {
			String bb[] = nationalList[i].split(",");
			System.out.println("===============" + bb[0]);
			System.out.println("===============" + bb[1]);
			counryList[i] = bb[0];
			codelist[i] = bb[1];
		}
		System.out.println("＝" + counryList + codelist);
	}


	private class myAdapter extends BaseAdapter {
		private int layout;
		private Context contextT;
		private String[] cc;

		public myAdapter(Context context, int resource, String[] list) {
			layout = resource;
			contextT = context;
			cc = list;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = LayoutInflater.from(contextT).inflate(layout, null);
			TextView tv = (TextView) convertView.findViewById(R.id.nationalListSelection_tV01);
			tv.setText(cc[position] + "");

			return convertView;
		}

		@Override
		public int getCount() {
			return cc.length;
		}

		@Override
		public Object getItem(int arg0) {
			return cc[arg0];
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}
	
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}
}
