package com.pictureair.photopass.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.pictureair.photopass.R;
import com.pictureair.photopass.util.API1;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.util.DealCodeUtil;
import com.pictureair.photopass.util.PictureAirLog;
import com.pictureair.photopass.widget.CustomProgressDialog;
import com.pictureair.photopass.widget.MyToast;

/**
 * 手动输入条码的页面
 * */
public class InputCodeActivity extends BaseActivity implements OnClickListener{
	private Button ok;
	private EditText input;
	private SharedPreferences sp;
	private MyToast newToast;

	private DealCodeUtil dealCodeUtil;

	private CustomProgressDialog dialog;

	private Handler handler2 = new Handler(){
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
				case DealCodeUtil.DEAL_CODE_FAILED:
					if (dialog.isShowing()) {
						dialog.dismiss();
					}
//					if (msg.obj != null) {//从ppp页面过来，需要返回
//						Intent intent2 = new Intent();
//						intent2.putExtra("result", "failed");
//						intent2.putExtra("errorType", Integer.valueOf(msg.obj.toString()));
//						setResult(RESULT_OK, intent2);
//					}
//					finish();
					break;

				case DealCodeUtil.DEAL_CODE_SUCCESS:
					if (dialog.isShowing()) {
						dialog.dismiss();
					}

					if (msg.obj != null) {//从ppp过来
						Intent intent2 = new Intent();
						Bundle bundle = (Bundle) msg.obj;
						if (bundle.getInt("status") == 1) {
							intent2.putExtra("result", bundle.getString("result"));
							setResult(RESULT_OK, intent2);
						} else if (bundle.getInt("status") == 2) {//将pp码返回
							intent2.putExtra("result", bundle.getString("result"));
							intent2.putExtra("hasBind", bundle.getBoolean("hasBind"));
							setResult(RESULT_OK, intent2);
						} else if (bundle.getInt("status") == 3) {
							intent2.setClass(InputCodeActivity.this, MyPPPActivity.class);
							API1.PPPlist.clear();
							startActivity(intent2);
						} else if (bundle.getInt("status") == 4){
							Editor editor = sp.edit();
							editor.putBoolean(Common.NEED_FRESH, true);
							editor.putInt(Common.PP_COUNT, sp.getInt(Common.PP_COUNT, 0) + 1);
							editor.commit();
						} else if (bundle.getInt("status") == 5){
							intent2.putExtra("result", bundle.getString("result"));
							PictureAirLog.out("scan ppp success and start back");
							setResult(RESULT_OK, intent2);
						}
					}
					AppManager.getInstance().killActivity(MipCaptureActivity.class);
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
		setContentView(R.layout.activity_inputcode);
		newToast = new MyToast(this);
		initview();
	}

	private void initview(){
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		ok = (Button) findViewById(R.id.sure);
		input = (EditText) findViewById(R.id.input);
		ok.setOnClickListener(this);
		setTopLeftValueAndShow(R.drawable.back_white,true);
		setTopTitleShow(R.string.manual);
		input.setOnEditorActionListener(new OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				if (actionId == EditorInfo.IME_ACTION_GO) {

					hideInputMethodManager(v);
					System.out.println("done");
					ok.performClick();

					return true;
				}
				return false;
			}
		});

		dealCodeUtil = new DealCodeUtil(this, getIntent(), handler2);

	}

	private void hideInputMethodManager(View v) {
		// TODO Auto-generated method stub
		/*隐藏软键盘*/  
		InputMethodManager imm = (InputMethodManager) v  
				.getContext().getSystemService(
						INPUT_METHOD_SERVICE);
		if (imm.isActive()) {  
			imm.hideSoftInputFromWindow(  
					v.getApplicationWindowToken(), 0);  
		} 
	}


	@Override
	public void onClick(View  v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.sure:
			if ("".equals(input.getText().toString())) {
				//				Toast.makeText(InputCodeAct.this, R.string.nocontext, Common.TOAST_SHORT_TIME).show();
				newToast.setTextAndShow(R.string.nocontext, Common.TOAST_SHORT_TIME);
			}else if (input.getText().toString().trim().length() != 16) {//长度不一致

				newToast.setTextAndShow(R.string.wrong_length, Common.TOAST_SHORT_TIME);
			}else {
				//如果有键盘显示，把键盘取消掉
				hideInputMethodManager(v);
				dialog = CustomProgressDialog.show(this, getString(R.string.is_loading), false, null);
				PictureAirLog.out("code is --->" + input.getText().toString());
//				dealCodeUtil.startDealCode("DPPPRU6CC7M5J6MM");
				dealCodeUtil.startDealCode(input.getText().toString().trim().toUpperCase());
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void TopViewClick(View view) {
		super.TopViewClick(view);
		switch (view.getId()) {
			case R.id.topLeftView:
				finish();
				break;
			default:
				break;
		}
	}
}
