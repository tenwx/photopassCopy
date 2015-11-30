package com.pictureAir;

import com.pictureAir.util.Common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateUserinfoActivity extends BaseActivity implements OnClickListener {
	private ImageView back;  // back btn
	private ImageButton ibSave;   // save btn
	
	private TextView tvHeader;
	private EditText etUserInfo;
	private int type;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_update_userinfo);

		init();
	}

	private void init() {
		tvHeader = (TextView) findViewById(R.id.tvHeader);
		etUserInfo = (EditText) findViewById(R.id.et_userinfo_text);
		
		//判断是从哪个页面跳转过来的。
		Intent intent=getIntent();
		type = intent.getIntExtra(Common.USERINFOTYPE, 0);
		switch (type) {
		case Common.NICKNAMETYPE:
			tvHeader.setText(R.string.title_update_nickname);
			etUserInfo.setHint(R.string.hint_text_nickname);
			break;
        case Common.EMAILTYPE:
        	tvHeader.setText(R.string.title_update_email);
        	etUserInfo.setHint(R.string.hint_text_email);
			break;
        case Common.QQTYPE:
        	tvHeader.setText(R.string.title_update_qq);
        	etUserInfo.setHint(R.string.hint_text_qq);
			break;

		default:
			break;
		}
		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(this);
		ibSave = (ImageButton) findViewById(R.id.ib_save);
		ibSave.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			this.finish();
			break;
		case R.id.ib_save:
			Intent mIntent = null;
			String userInfo = etUserInfo.getText().toString().trim();
			switch (type) {
			case Common.NICKNAMETYPE:
				mIntent = new Intent();
		        mIntent.putExtra("nickName", userInfo); 
				this.setResult(1, mIntent);
				break;
			case Common.QQTYPE:
				mIntent = new Intent();  
		        mIntent.putExtra("qq", userInfo); 
				this.setResult(2, mIntent);
				break;

			default:
				break;
			}
			this.finish();
			break;

		default:
			break;
		}
	}

}
