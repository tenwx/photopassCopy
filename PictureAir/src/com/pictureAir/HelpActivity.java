package com.pictureAir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import cn.sharesdk.framework.authorize.a;

import com.pictureAir.adapter.MyAdapter;
import com.pictureAir.db.DatabaseAdapter;
import com.pictureAir.entity.Question;
import com.pictureAir.util.AppManager;
import com.pictureAir.util.PinYin;
import com.pictureAir.widget.EditTextWithClear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;



/**
 * 搜索界面 实现问题的搜索功能 支持关键字搜索
 * 
 */
public class HelpActivity extends Activity implements OnClickListener{

	private final String TAG = "MainActivity ";
	// 申明控件
	private EditTextWithClear editText;
	private ListView mListView;
	//	private ImageButton cancel_btn;
	private ArrayList<Question> testArray = new ArrayList<Question>();
	private MyAdapter adapter;
	private ImageView back;
	private Question question;

	private DatabaseAdapter databaseAdapter;

	/**
	 * 数组
	 */


	String[] items = { "How to use PhotoPass?", "How do I make purchases？", "Where can I collect my purchased items?", "What if I lose my park ticket/PhotoPass card?" };

	String[] answer = { "How to use PhotoPass?", "How do I make purchases？", "Where can I collect my purchased items?", "What if I lose my park ticket/PhotoPass card?" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		initView();
		databaseAdapter = DatabaseAdapter.getIntance(this);
		// 每次进入前清空一下
		databaseAdapter.deleteAll();
		findAll();
		new Thread(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < items.length; i++) {
					System.out.println("------>" + i);
					question = new Question();
					// question.questionId = i;
					question.questionName = items[i];
					question.answer = answer[i];
					// i+"s answer";
					testArray.add(question);
				}
				// 向数据库中插入指定数据
				databaseAdapter.insertInfo(testArray);
			}
		}).start();	}

	/**
	 * 初始化视图
	 */
	private void initView() {
		AppManager.getInstance().addActivity(this);
		editText = (EditTextWithClear) findViewById(R.id.input_edit);
		mListView = (ListView) findViewById(R.id.auto_list);
		//		cancel_btn = (ImageButton) findViewById(R.id.cancel_btn);
		back = (ImageView) findViewById(R.id.back);
		// lvSearch=(ListView) findViewById(R.id.lvSearch);

		adapter = new MyAdapter(this, testArray);
		mListView.setAdapter(adapter);
		// adapter = new MyAdapter(this, toastArray);

		// mListView.setAdapter(adapter);// 设置Adapter，初始值为空
		// lvSearch.setAdapter(toastAdapter);

		//		OnClickListener myClickListener = new OnClickListener() {
		//
		//			@Override
		//			public void onClick(View v) {
		//				int vid = v.getId();
		//				if (vid == cancel_btn.getId()) {
		//					editText.setText("");// 清空编辑框
		//				}
		//				if (vid == back.getId()) {
		//					finish();
		//				}
		//			}
		//		};

		// 删除按钮的单击操作
		//		cancel_btn.setOnClickListener(myClickListener);
		back.setOnClickListener(this);
		// listView点击事件
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Log.d(TAG, "test step 1------>");

				mListView.setVisibility(View.VISIBLE);

				Intent intent = new Intent();
				intent.setClass(HelpActivity.this, AnswerActivity.class);
				Log.d(TAG, "step 3---->" + testArray.size());

				intent.putExtra("question", adapter.getItem(position));
				startActivity(intent);
				adapter.refreshData(testArray);// Adapter刷新数据
				editText.setText(adapter.getItem(position).questionName);
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

				testArray = new ArrayList<Question>();// 每次输入的时候，重新初始化数据列表
				// toastArray=new ArrayList<String>();
				testArray.clear();

				if (!TextUtils.isEmpty(editText.getText().toString())) {// 判断输入内容是否为空，为空则跳过
					// 查询相似数据--传入一个转换为拼音的字符串

					testArray = databaseAdapter
							.queryInfo(
									PinYin.getPinYin(editText.getText()
											.toString()));
					//					cancel_btn.setVisibility(View.VISIBLE);

				} else {

					testArray = databaseAdapter
							.findAll();
					//					cancel_btn.setVisibility(View.INVISIBLE);
				}
				Log.d(TAG, "step 2 ------>" + testArray.size());

				adapter.refreshData(testArray);// Adapter刷新数据
				mListView.setVisibility(View.VISIBLE);
			}

			/**
			 * 输入之前
			 */
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			/**
			 * 输入之后
			 */
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});
	}


	// 查找所有问题

	private void findAll() {
		// testArray = new ArrayList<String>();
		testArray.clear();
		testArray = databaseAdapter.findAll();
		Log.d(TAG, "test array size = " + testArray.size());
		adapter.refreshData(testArray);// Adapter刷新数据
		mListView.setAdapter(adapter);
		mListView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
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