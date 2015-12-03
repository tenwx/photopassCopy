package com.pictureair.photopass.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.pictureair.photopass.R;
import com.pictureair.photopass.adapter.QuestionAdapter;
import com.pictureair.photopass.db.PictureAirDbManager;
import com.pictureair.photopass.entity.QuestionInfo;
import com.pictureair.photopass.util.AppManager;
import com.pictureair.photopass.util.PinYin;
import com.pictureair.photopass.widget.EditTextWithClear;

import java.util.ArrayList;


/**
 * 搜索界面 实现问题的搜索功能 支持关键字搜索
 * 
 */
public class HelpActivity extends BaseActivity implements OnClickListener{

	private final String TAG = "MainActivity ";
	// 申明控件
	private EditTextWithClear editText;
	private ListView mListView;
	private ArrayList<QuestionInfo> testArray = new ArrayList<QuestionInfo>();
	private QuestionAdapter adapter;
	private ImageView back;
	private QuestionInfo question;

	private PictureAirDbManager pictureAirDbManager;
	
	//feedback
		private EditText eTFeedback;
		private TextView tVsend;
		private TextView tVCancel;
		private AlertDialog myFeedbackDialog;
		private RelativeLayout relativeLayout1;

	/**
	 * 数组
	 */
	String[] items = { "How to use PhotoPass?", "How do I make purchases?", "Where can I collect my purchased items?", "What if I lose my park ticket/PhotoPass card?" };
	String[] answer = { "How to use PhotoPass?", "How do I make purchases?", "Where can I collect my purchased items?", "What if I lose my park ticket/PhotoPass card?" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		initView();
		pictureAirDbManager = new PictureAirDbManager(this);

		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < items.length; i++) {
					System.out.println("------>" + i);
					question = new QuestionInfo();
					question.questionName = items[i];
					question.answer = answer[i];
					testArray.add(question);
				}
				// 向数据库中插入指定数据
				pictureAirDbManager.insertIntoQuestionTable(testArray);
				handler.sendEmptyMessage(111);
			}
		}).start();	
	}
	
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 111:
				adapter.notifyDataSetChanged();
				break;

			default:
				break;
			}
		};
	};

	/**
	 * 初始化视图
	 */
	private void initView() {
		AppManager.getInstance().addActivity(this);
		editText = (EditTextWithClear) findViewById(R.id.input_edit);
		mListView = (ListView) findViewById(R.id.auto_list);
		back = (ImageView) findViewById(R.id.back);
		relativeLayout1 = (RelativeLayout) findViewById(R.id.relativeLayout1);
		relativeLayout1.setOnClickListener(this);
		adapter = new QuestionAdapter(this, testArray);
		mListView.setAdapter(adapter);

		back.setOnClickListener(this);
		// listView点击事件
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "test step 1------>");
				Intent intent = new Intent();
				intent.setClass(HelpActivity.this, AnswerActivity.class);
				Log.d(TAG, "step 3---->" + testArray.size());

				intent.putExtra("question", adapter.getItem(position));
				startActivity(intent);
			}
		});

		// EditText变化监听
		editText.addTextChangedListener(new TextWatcher() {

			/**
			 * 正在输入
			 */
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				testArray.clear();
				if (!TextUtils.isEmpty(editText.getText().toString())) {// 判断输入内容是否为空，不为空，查找当前值
					// 查询相似数据--传入一个转换为拼音的字符串
					testArray = pictureAirDbManager.queryQuestionInfo(PinYin.getPinYin(editText.getText().toString()));
				} else {//如果数据为空
					testArray = pictureAirDbManager.findAllQuestions();
				}
				Log.d(TAG, "step 2 ------>" + testArray.size());
				adapter.refreshData(testArray);// Adapter刷新数据
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
			}
		});
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
			
		case R.id.relativeLayout1:
//			diaLogFeedBack();
			break;
		// Feedback
//		case R.id.tVCancel:
//			myFeedbackDialog.dismiss();
//			break;
//		case R.id.tVsend:
//			myFeedbackDialog.dismiss();
//			String feedbackStr = eTFeedback.getText().toString();
//			System.out.println("-------:" + feedbackStr);
//			break;

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
	
//	 private void diaLogFeedBack() {
//			View v = LayoutInflater.from(this).inflate(
//					R.layout.custom_dialog_feedback, null);
//			AlertDialog.Builder myBuilder = new AlertDialog.Builder(this);
//			myFeedbackDialog = myBuilder.create();
//			myFeedbackDialog.setView(new EditText(this));//自定义的dialog，必须在show（）之前加入此行，不然显示不了软键盘
//			myFeedbackDialog.show();
//			myFeedbackDialog.getWindow().setContentView(v);
//			eTFeedback = (EditText) v.findViewById(R.id.eTFeedback);
//			tVsend = (TextView) v.findViewById(R.id.tVsend);
//			tVCancel = (TextView) v.findViewById(R.id.tVCancel);
//			tVsend.setOnClickListener(this);
//			tVCancel.setOnClickListener(this);
//			eTFeedback.setOnClickListener(this);
//
//		}
	 
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