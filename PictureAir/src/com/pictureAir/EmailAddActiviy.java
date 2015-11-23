package com.pictureAir;

import com.pictureAir.util.Common;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
/**邮箱地址添加页面*/
public class EmailAddActiviy extends BaseActivity {
	private EditText head, end;
	private Button confirm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.input_email);
		initView();
		initData();
	}

	private void initView() {
		head = (EditText) findViewById(R.id.head);
		end = (EditText) findViewById(R.id.end);
		confirm = (Button) findViewById(R.id.confirm);
		confirm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.putExtra(Common.USERINFO_EMAIL, head.getText().toString() + "@" + end.getText().toString());
				setResult(RESULT_OK, intent);
				finish();
			}
		});
	}

	private void initData() {
		SharedPreferences sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		String email = sp.getString(Common.USERINFO_EMAIL, "");
		if (!email.equals("")) {
			String[] emails = email.split("@");
			head.setText(emails[0]);
			end.setText(emails[1]);
		}
	}
}
