package com.pictureair.photopass.widget;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.pictureair.photopass.R;

public class FontEditDialog extends Dialog implements android.view.View.OnClickListener{
	private ImageButton imgbtn_submit,imgbtn_cancel,imgbtn_clear;
	private EditText et_message;
	private Handler handler;
	private Context context;
	String text = "";

	public FontEditDialog(Context context,String text,Handler handler) {
		super(context);
		this.handler = handler;
		this.context = context;
		this.text = text;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		this.setContentView(R.layout.dialog_font_edittext);
		
		imgbtn_clear = (ImageButton) findViewById(R.id.imgbtn_clear);
		imgbtn_cancel = (ImageButton) findViewById(R.id.imgbtn_cancel);
		imgbtn_submit = (ImageButton) findViewById(R.id.imgbtn_submit);
		et_message = (EditText) findViewById(R.id.et_message);
		et_message.setText(text);
		et_message.setSelection(text.length());
		imgbtn_submit.setOnClickListener(this);
		imgbtn_cancel.setOnClickListener(this);
		imgbtn_clear.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Message msg;
		switch (v.getId()) {
		case R.id.imgbtn_submit:
			text = et_message.getText().toString().trim();
			msg = handler.obtainMessage();
			msg.obj = text;
			msg.what = 0000;
			handler.sendMessage(msg);
			break;
			
		case R.id.imgbtn_cancel:
			handler.sendEmptyMessage(3333);
			break;
		case R.id.imgbtn_clear:
			et_message.setText("");
			break;

		default:
			break;
		}
	}

}
