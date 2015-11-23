package com.pictureAir;

import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.MyToast;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class ModifyPasswordActivity extends BaseActivity implements OnClickListener{
	private SharedPreferences sharedPreferences;
    private ImageView back;
    private EditText oldPassword,newPassword;
    private Button submit;
    private MyToast newToast;
    boolean isSele = true;
    ImageButton radio;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			
			case API.MODIFY_PWD_SUCCESS:
				newToast.setTextAndShow(R.string.modify_password_success, Common.TOAST_SHORT_TIME);
				ModifyPasswordActivity.this.finish();
				break;
			
			case API.MODIFY_PWD_FAILED:
				if (msg.obj.toString().equals("errOldPwd")) {
					newToast.setTextAndShow(R.string.errOldPwd, Common.TOAST_SHORT_TIME);
				}else if(msg.obj.toString().equals("shortPassword")){
					newToast.setTextAndShow(R.string.shortPassword, Common.TOAST_SHORT_TIME);
				}else if(msg.obj.toString().equals("userNotExist")){
					newToast.setTextAndShow(R.string.userNotExist, Common.TOAST_SHORT_TIME);
				}else{
					newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				}
				
				break;

			default:
				break;
			}

		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_modify_password);
		AppManager.getInstance().addActivity(this);
		initView();
	}
	
	private void initView(){
		newToast = new MyToast(this);
		sharedPreferences = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		radio = (ImageButton) findViewById(R.id.modify_password_radio);
		back = (ImageView) findViewById(R.id.back_modify);
		oldPassword = (EditText) findViewById(R.id.old_password);
		newPassword = (EditText) findViewById(R.id.new_password);
		submit = (Button) findViewById(R.id.submit);
		
		radio.setOnClickListener(this);
		back.setOnClickListener(this);
		submit.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back_modify:
			this.finish();
			break;
		case R.id.submit:
			if (oldPassword.getText().toString().trim().equals("") || newPassword.getText().toString().trim().equals("")) {
				newToast.setTextAndShow(R.string.modify_password_empty_hint, Common.TOAST_SHORT_TIME);
			}else{
				API.modifyPwd(this, oldPassword.getText().toString().trim(), newPassword.getText().toString().trim(), "", mHandler);
			}
			break;
		case R.id.modify_password_radio:
			
			 if(isSele){
	            	radio.setImageResource(R.drawable.modify_password_radio_nor);
	            	oldPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
	            	newPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
	            	isSele = false;
	            }else{
	            	radio.setImageResource(R.drawable.modify_password_radio_sele);
	            	oldPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
	            	newPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
	            	isSele = true;
	            }
			
			break;

		default:
			break;
		}
		
	}

	

}
