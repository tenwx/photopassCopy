package com.pictureair.photopass.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.R;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.Installation;
import com.pictureair.photopass.widget.MyToast;

/**
 * 注册和修改密码的页面，前提都是通过了手机号码验证才会来到这个页面， 根据intent传递过来的type值判断是注册还是修改密码 type == 0
 * 为注册，type == 1为修改密码
 * PS：暂时因为注册和修改密码界面几乎一样，所以放在一个activity中完成，但后续如果改变注册页面布局的话最好和修改密码分开处理，使业务逻辑清晰化
 * */
public class SignOrForgetActivity extends BaseActivity implements OnClickListener {
	private EditText username, pwd1, pwd2;
	private Button sure;
	private ImageView back;
	private String phone;
	private TextView title;
	private SharedPreferences sp;
	private MyToast newToast;
	private int type;// 判断跳转来自注册还是密码修改 0：注册；1：修改密码；


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sign);
		initView();
	}
	
	private void initView() {

		phone = getIntent().getStringExtra("phone");//获取手机号
		type = getIntent().getIntExtra("type", 0);//注册
		System.out.println("phone------->"+phone+",type---->"+type);
		newToast = new MyToast(this);
		title = (TextView) findViewById(R.id.title);//获取标题名字
		sure = (Button) findViewById(R.id.sure);//
		if (type == 0){//sign
			title.setText(R.string.sign);
			sure.setText(R.string.sign);
		}
		else if (type == 1){//forget
			title.setText(R.string.forget_pwd);
			sure.setText(R.string.confirm);
		}
		username = (EditText) findViewById(R.id.username);
		username.setText(phone);
		pwd1 = (EditText) findViewById(R.id.pwd);
		pwd2 = (EditText) findViewById(R.id.pwd_again);
		back = (ImageView) findViewById(R.id.sign_back);
		back.setOnClickListener(this);
		sure.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		switch (v.getId()) {
		case R.id.sign_back:
			finish();
			break;
		case R.id.sure:
			String p1 = pwd1.getText().toString().trim();
			final String p2 = pwd2.getText().toString().trim();
			//显示不能为空密码
			if (p1.isEmpty()) {
				newToast.setTextAndShow(R.string.pw_null, Common.TOAST_SHORT_TIME);
				break;
			}
			//显示密码不一致
			if (p2.isEmpty() || !p2.equals(p1)) {
				newToast.setTextAndShow(R.string.pw_is_inconsistency, Common.TOAST_SHORT_TIME);
				break;
			}
			// 向服务器发送请求
			if (type == 0) {
				// 注册请求
				if (null == sp.getString(Common.USERINFO_TOKENID, null)) {//需要重新获取一次tokenid
					System.out.println("no tokenid, need to obtain one");
					final StringBuffer sb = new StringBuffer();
					sb.append(Common.BASE_URL_TEST).append(Common.GET_TOKENID);//获取地址
					
					RequestParams params = new RequestParams();
					params.put(Common.TERMINAL, "android");
					params.put(Common.UUID, Installation.id(this));
					
				}else {
				}
			} else if (type == 1) {
				// 修改密码请求
				// 注册请求
				newToast.setTextAndShow(R.string.not_open, Common.TOAST_SHORT_TIME);

			}
			break;
		default:
			break;
		}
	}
}
