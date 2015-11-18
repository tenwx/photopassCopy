package com.pictureAir;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.pictureAir.adapter.MyAdapter;
import com.pictureAir.entity.Question;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * 答案界面 实现问题的回答
 * 
 */
public class AnswerActivity extends Activity {

	private final String TAG = "AnswerActivity ";
	// 申明控件
	private TextView answerName;
	private TextView answerContent;
	private ImageView back;
	private MyAdapter adapter;

	public final static int RESULT_CODE = 1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.answer);

		answerName = (TextView) findViewById(R.id.answerName);
		answerContent = (TextView) findViewById(R.id.answerContent);
		back = (ImageView) findViewById(R.id.back);

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});

		// 传递问题
		Intent intent = getIntent();
		Bundle bundle = intent.getExtras();
		Question question = bundle.getParcelable("question");
		answerName.setText(question.questionName);
		
		// 答案
		answerContent.setText(question.answer);
	}
}
