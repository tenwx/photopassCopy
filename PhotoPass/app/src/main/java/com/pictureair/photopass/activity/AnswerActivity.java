package com.pictureair.photopass.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.QuestionAdapter;
import com.pictureair.photopass.entity.QuestionInfo;

/**
 * 答案界面 实现问题的回答
 * 
 */
public class AnswerActivity extends BaseActivity {

	private final String TAG = "AnswerActivity ";
	// 申明控件
	private TextView answerName;
	private TextView answerContent;
	private ImageView back;
	private QuestionAdapter adapter;

	public final static int RESULT_CODE = 1;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_answer);

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
		QuestionInfo question = bundle.getParcelable("question");
		answerName.setText(question.questionName);
		
		// 答案
		answerContent.setText(question.answer);
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
