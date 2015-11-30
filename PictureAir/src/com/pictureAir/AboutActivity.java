package com.pictureAir;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class AboutActivity extends BaseActivity implements OnClickListener{
    private TextView logo_text;
    private TextView tv_versionCode;
    private String versionCode;
	private String versionName;
	private ImageView rl_back;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_about);
		logo_text = (TextView) findViewById(R.id.logo_text);
		tv_versionCode = (TextView) findViewById(R.id.versionCode);
		rl_back = (ImageView) findViewById(R.id.back1);
		rl_back.setOnClickListener(this);
		getVersionCode();
		tv_versionCode.setText(versionName);
		logo_text.setText((Html.fromHtml("我"+ "<font color='#ffa300'><big>"+ "(迪)"+ "</big></font>" + "故事里有您更精"+ "<font color='#ffa300'><big>"+ "(彩)"+ "</big></font>")));
	}
	
	/**
	 * 获取应用的版本号
	 * @param context
	 */
	private void getVersionCode() {
		// TODO Auto-generated method stub
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			versionCode = info.versionCode + "";
			versionName = info.versionName;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back1:
			finish();
			break;

		default:
			break;
		}
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
