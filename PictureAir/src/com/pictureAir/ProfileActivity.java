package com.pictureAir;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
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
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureAir.util.API;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.Common;
import com.pictureAir.widget.MyToast;

/** 个人信息页面 */
public class ProfileActivity extends Activity implements OnClickListener {
	private TextView tvNickName, tvGender, tvBirthday,tvQq;
	private RelativeLayout nn, g, bd,q, item_password;
	private Button save;
	private ImageView back;
	private SharedPreferences sp;
	private int year,month,day;
	private MyToast newToast;
	private String birthdayString;
//	private CustomDialog customdialog;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			// TODO Auto-generated method stub
			switch (msg.what) {
			case R.id.item_nickname:
				String nn = (String) msg.obj;
				tvNickName.setText(nn);
				tvNickName.invalidate();
				break;
			case R.id.item_gender:
//				int sex = (Integer) msg.obj;
				String sex = (String)msg.obj;
				if(sex.equals("male")||sex.equals("男")){
					tvGender.setText(R.string.male);
				}else{
					tvGender.setText(R.string.female);
				}
				
				tvGender.invalidate();
				API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), sp.getString(Common.USERINFO_BIRTHDAY, ""), sex.toLowerCase(),sp.getString(Common.USERINFO_QQ, ""), handler);
//				API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), sp.getString(Common.USERINFO_BIRTHDAY, ""), tvGender.getText().toString(),sp.getString(Common.USERINFO_QQ, ""), handler);
				break;
			case R.id.item_birth:
				String birthString = (String) msg.obj;
				tvBirthday.setText(birthString);
				tvBirthday.invalidate();
				API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""),birthString , sp.getString(Common.USERINFO_GENDER, "").toLowerCase(),sp.getString(Common.USERINFO_QQ, ""), handler);
				break;
//			case R.id.item_email:
//				String emailString = (String) msg.obj;
//				email.setText(emailString);
//				email.invalidate();
//				break;
			case API.UPDATE_PROFILE_FAILED:
				System.out.println("-------failed");
				if (msg.obj == null) {//网络连接错误
					newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				}else {//修改失败
					newToast.setTextAndShow(R.string.failed, Common.TOAST_SHORT_TIME);
				}
				break;
				
			case API.UPDATE_PROFILE_SUCCESS:
				Editor e = sp.edit();
				e.putString(Common.USERINFO_NICKNAME, tvNickName.getText().toString());
				e.putString(Common.USERINFO_BIRTHDAY, tvBirthday.getText().toString());
				e.putString(Common.USERINFO_GENDER, tvGender.getText().toString().toLowerCase());
				e.putString(Common.USERINFO_QQ, tvQq.getText().toString());
				e.commit();
				newToast.setTextAndShow(R.string.save_success, Common.TOAST_SHORT_TIME);
//				finish();
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
		setContentView(R.layout.personal_profile);
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		AppManager.getInstance().addActivity(this);
		newToast = new MyToast(this);
		initView();
		initData();
	}

	private void initView() {
		tvNickName = (TextView) findViewById(R.id.nick_name);
		tvGender = (TextView) findViewById(R.id.sex);
		tvBirthday = (TextView) findViewById(R.id.birthday);
		tvQq = (TextView) findViewById(R.id.qq);
		
//		email = (TextView) findViewById(R.id.email);
		nn = (RelativeLayout) findViewById(R.id.item_nickname);
		g = (RelativeLayout) findViewById(R.id.item_gender);
		bd = (RelativeLayout) findViewById(R.id.item_birth);
		q = (RelativeLayout) findViewById(R.id.item_qq);
		item_password = (RelativeLayout) findViewById(R.id.item_modify);
//		wechat = (RelativeLayout) findViewById(R.id.item_wechat);
//		twitter = (RelativeLayout) findViewById(R.id.item_twitter);
		back = (ImageView) findViewById(R.id.back);
		save = (Button) findViewById(R.id.save);
		
		item_password.setOnClickListener(this);
		nn.setOnClickListener(this);
		g.setOnClickListener(this);
		bd.setOnClickListener(this);
		q.setOnClickListener(this);
//		wechat.setOnClickListener(this);
//		twitter.setOnClickListener(this);
		back.setOnClickListener(this);
		save.setOnClickListener(this);
	}

	private void initData() {
		tvNickName.setText(sp.getString(Common.USERINFO_NICKNAME, "pictureAir"));
		String genderStr = sp.getString(Common.USERINFO_GENDER, "male").toLowerCase();
		if(genderStr.equals("male")||genderStr.equals("男")){
			tvGender.setText(R.string.male);
		}else{
			tvGender.setText(R.string.female);
		}
//		tvGender.setText(sp.getString(Common.USERINFO_GENDER, "male"));
		birthdayString = sp.getString(Common.USERINFO_BIRTHDAY, "1991-01-01");
		
		if ("".equals(birthdayString.trim())) {
			birthdayString = "1991-01-01";
		}
		tvBirthday.setText(birthdayString);
		
		tvQq.setText(sp.getString(Common.USERINFO_QQ, ""));
		
		System.out.println("birthday------>"+birthdayString);
		 
//		qqTextView.setText(sp.getString(Common.USERINFO_QQ, ""));
//		wechaTextView.setText(sp.getString(Common.USERINFO_WECHAT, ""));
//		twitterTextView.setText(sp.getString(Common.USERINFO_TWITTER, ""));
		
		String[] time = birthdayString.split("-");
		year = Integer.valueOf(time[0]);
		month = Integer.valueOf(time[1]) - 1;
		day = Integer.valueOf(time[2]);
//		email.setText(sp.getString(Common.USERINFO_EMAIL, ""));
	}

	@Override
	public void onClick(View v) {
		Intent intent = null;
		switch (v.getId()) {
		case R.id.item_nickname:
			intent = new Intent(ProfileActivity.this, UpdateUserinfoActivity.class);
			intent.putExtra(Common.USERINFOTYPE, Common.NICKNAMETYPE);
			startActivityForResult(intent, 0);
//			startActivity(intent);
//			Dialog(R.id.item_nickname);
			break;
		case R.id.item_gender:
			Dialog(R.id.item_gender);
			break;
		case R.id.item_birth:
			Dialog(R.id.item_birth);
			break;
		case R.id.item_qq:
			intent = new Intent(ProfileActivity.this, UpdateUserinfoActivity.class);
			intent.putExtra(Common.USERINFOTYPE, Common.QQTYPE);
			startActivityForResult(intent, 1);
			break;
		case R.id.back:
			finish();
			break;
			
		case R.id.item_modify:
			//跳转新页面。
			intent = new Intent(ProfileActivity.this, ModifyPasswordActivity.class);
			startActivity(intent);
			break;
		case R.id.save:
			API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), tvNickName.getText().toString(), tvBirthday.getText().toString(), tvGender.getText().toString().toLowerCase(),sp.getString(Common.USERINFO_QQ, ""), handler);
			
//			Toast.makeText(this, "个人信息保存成功", 0).show();
			
			break;
		default:
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.e("resultCode", " "+resultCode);
		switch (resultCode) {
		case 1://更改昵称的标识
			String name = data.getStringExtra("nickName");
			tvNickName.setText(name);
			tvNickName.invalidate();
			
			API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), name, sp.getString(Common.USERINFO_BIRTHDAY, ""), sp.getString(Common.USERINFO_GENDER, "").toLowerCase(),sp.getString(Common.USERINFO_QQ, ""), handler);
//			Toast.makeText(getApplicationContext(), " "+name, 1000).show();
//			String em = data.getStringExtra("email");
//			Message msg = handler.obtainMessage();
//			msg.what = R.id.item_email;
//			msg.obj = em;
//			handler.sendMessage(msg);
			break;
		case 2://更改QQ的标识
			String qq = data.getStringExtra("qq");
			tvQq.setText(qq);
			tvQq.invalidate();
			API.updateProfile(sp.getString(Common.USERINFO_TOKENID, ""), sp.getString(Common.USERINFO_NICKNAME, ""), sp.getString(Common.USERINFO_BIRTHDAY, ""), sp.getString(Common.USERINFO_GENDER, "").toLowerCase(),qq, handler);
			break;
		default:
			break;
		}
	}

	private void Dialog(int type) {
		switch (type) {
//		case R.id.item_nickname:
//			final EditText inputServer = new EditText(this);
//			AlertDialog.Builder builder = new AlertDialog.Builder(this);
//			builder.setTitle(R.string.nn).setIcon(android.R.drawable.ic_dialog_info).setView(inputServer).setNegativeButton(R.string.button_cancel, null);
//			builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//				public void onClick(DialogInterface dialog, int which) {
//					Message msg = handler.obtainMessage();
//					String result = inputServer.getText().toString();
//					msg.what = R.id.item_nickname;
//					msg.obj = result;
//					handler.sendMessage(msg);
//				}
//			});
//			builder.show();
//			break;
		case R.id.item_gender:
			//初始化dialog
//			customdialog = new CustomDialog.Builder(this)
//			.setMessage(getString(R.string.grender)) 
//			.setNegativeButton(getResources().getString(R.string.dialog_cancel), new DialogOnClickListener())
//			.setPositiveButton(getResources().getString(R.string.dialog_ok), new DialogOnClickListener())
//			.setCancelable(false)
//			.create();
//			customdialog.show();
			new AlertDialog.Builder(this).
			setTitle(R.string.grender).
			setIcon(android.R.drawable.ic_dialog_info).
			setSingleChoiceItems(new String[] { getString(R.string.male), getString(R.string.female) }, 0, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Message msg = handler.obtainMessage();
					// TODO Auto-generated method stub
					msg.what = R.id.item_gender;
					switch (which) {
					case 0:
						msg.obj = "male";
//						msg.obj = R.string.male;
						break;
					case 1:
						msg.obj = "female";
//						msg.obj = R.string.female;
					default:
						break;
					}
					dialog.dismiss();
					handler.sendMessage(msg);
				}
			}).show();
			break;
		case R.id.item_birth:
			DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
				@Override
				public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
					Message msg = handler.obtainMessage();
					ProfileActivity.this.year = year;
					ProfileActivity.this.month = month;
					day = dayOfMonth;
					msg.what = R.id.item_birth;
					msg.obj = year + "-" + (month + 1) + "-" + dayOfMonth;
					handler.sendMessage(msg);
				}
			};
			new DatePickerDialog(this, dateListener, year, month, day).show();
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
