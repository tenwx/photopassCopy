package com.pictureair.photopass.selectHeadorBg;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.loopj.android.http.RequestParams;
import com.pictureair.photopass.R;
import com.pictureair.photopass.blur.UtilOfDraw;
import com.pictureair.photopass.util.API;
import com.pictureair.photopass.util.Common;
import com.pictureair.photopass.widget.CustomProgressBarPop;
import com.pictureair.photopass.widget.MyToast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/** 头像选取 */
public class SetHeadPhotoAct extends Activity implements OnClickListener {
	private ClipImageLayout mClipImageLayout;
	private final String IMAGE_TYPE = "image/*";
	private final int IMAGE_CODE = 0; // 这里的IMAGE_CODE是自己任意定义的
	private ImageView clip;
	private ImageView back;
	private SharedPreferences sp;
	private CustomProgressBarPop dialog;
	private MyToast myToast;
	private File headPhoto;
	
	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
//			Intent intent = null;
			switch (msg.what) {
			case API.UPLOAD_PHOTO_SUCCESS:
				Editor e = sp.edit();
				e.putString(Common.USERINFO_HEADPHOTO, Common.HEADPHOTO_PATH);
				e.commit();
				
				//上传成功之后，需要将临时的头像文件的名字改为正常的名字
				File oldFile = new File(Common.USER_PATH + Common.HEADPHOTO_PATH);
				if (oldFile.exists()) {//如果之前的文件存在
					//先删除之前的文件
					oldFile.delete();
					//修改现在的文件名字
					if (headPhoto.exists()) {
						headPhoto.renameTo(oldFile);
					}
				}
				
				dialog.dismiss();
				myToast.setTextAndShow(R.string.save_success, Common.TOAST_SHORT_TIME);
				
				
				finish();
//				startActivity(intent);
				break;
				
			case API.UPLOAD_PHOTO_FAILED:
				//删除头像的临时文件
				if (headPhoto.exists()) {
					headPhoto.delete();
				}
				
				dialog.dismiss();
				myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
				finish();
				break;
				
			case API.FAILURE:
				dialog.dismiss();
				myToast.setTextAndShow(R.string.http_failed, Common.TOAST_SHORT_TIME);
//				Toast.makeText(SetHeadPhotoAct.this, "修改失败请重试", 0).show();
//				intent = new Intent(SetHeadPhotoAct.this, MainTabActivity.class);
				finish();
//				startActivity(intent);
				break;

			default:
				break;
			}
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sp = getSharedPreferences(Common.USERINFO_NAME, MODE_PRIVATE);
		
		setContentView(R.layout.activity_set_head_photo);
		myToast = new MyToast(this);
		Intent getAlbum = new Intent(Intent.ACTION_GET_CONTENT);
		getAlbum.setType(IMAGE_TYPE);
		initView();
		startActivityForResult(getAlbum, IMAGE_CODE);
	}

	private void initView() {
		mClipImageLayout = (ClipImageLayout) findViewById(R.id.clipImageLayout);
		back = (ImageView) findViewById(R.id.back);
		back.setOnClickListener(this);
		clip = (ImageView) findViewById(R.id.clip);
		clip.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.clip:
//			MainTabActivity.instances.finish(); //上传成功之后结束 主要的Activity。
			dialog = new CustomProgressBarPop(this, findViewById(R.id.setHeadRelativeLayout), CustomProgressBarPop.TYPE_UPLOAD);
//			dialog = ProgressDialog.show(this, getString(R.string.loading___), getString(R.string.photo_is_uploading), true, true);
			dialog.show(0);
			Bitmap bitmap = mClipImageLayout.clip();
			bitmap = UtilOfDraw.comp(bitmap);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
			byte[] datas = baos.toByteArray();
			File userFile = new File(Common.USER_PATH);
			if (!userFile.exists()) {
				userFile.mkdirs();
			}
			headPhoto = new File(Common.USER_PATH + Common.HEADPHOTO_PATH + "_temp");
			BufferedOutputStream stream = null;
			try {
				headPhoto.createNewFile();
				FileOutputStream fstream = new FileOutputStream(headPhoto);
				stream = new BufferedOutputStream(fstream);
				stream.write(datas);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
				try {
					// 需要更新服务器中用户头像图片信息
					StringBuffer sb = new StringBuffer();
					sb.append(Common.BASE_URL).append(Common.SET_USER_PHOTO);
					String tokenId = sp.getString(Common.USERINFO_TOKENID, null);
					RequestParams params = new RequestParams();
					params.put(Common.USERINFO_TOKENID, tokenId);
					params.put("updateType", "avatar");
					params.put("file", headPhoto);
					API.SetPhoto(sb.toString(), params, handler, 0, dialog);
				} catch (FileNotFoundException ee) {
					// TODO Auto-generated catch block
					ee.printStackTrace();
				}
			}
			break;
			
		case R.id.back:
			finish();
			break;
			
		default:
			break;
			
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			System.out.println("--------->null");
			finish();
			return;
		}
		// 此处的用于判断接收的Activity是不是你想要的那个
		if (requestCode == IMAGE_CODE) {
			Uri originalUri = data.getData(); // 获得图片的uri
			mClipImageLayout.setImage(originalUri);
		}
	}
}
