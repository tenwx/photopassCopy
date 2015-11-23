package com.pictureAir;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.MyToast;

public class AddPhoneActivity extends BaseActivity implements OnClickListener{
	private Button saveButton;
	private ImageButton returnButton;
	private EditText addphoneEditText;
	private MyToast newToast;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.addinformation);
		newToast = new MyToast(AddPhoneActivity.this);
		saveButton = (Button)findViewById(R.id.button1_save);
		saveButton.setOnClickListener(this);
		addphoneEditText = (EditText)findViewById(R.id.editText1);
		//获取电话号
		addphoneEditText.setText(getIntent().getStringExtra("phonenumber"));
		returnButton = (ImageButton)findViewById(R.id.imagebutton2_return);
		returnButton.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.button1_save://保存按钮
			String phoneString = addphoneEditText.getText().toString();
			if (phoneString.length()<11) {//判断手机号的长度
//				Toast.makeText(this, "please input right phone number", Toast.LENGTH_SHORT).show();
				newToast.setTextAndShow(R.string.input_right_phone_number, Common.TOAST_SHORT_TIME);
//				Toast.makeText(this, "please input right phone number", Toast.LENGTH_SHORT).show();
			}else {//将信息返回值上一activity
				Intent intent = new Intent();
				intent.putExtra("phone", phoneString);
				setResult(20, intent);
				finish();
			}
			break;
		case R.id.imagebutton2_return://返回按钮
			finish();
			break;
		default:
			break;
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		AppManager.getInstance().killActivity(this);
	}
}
