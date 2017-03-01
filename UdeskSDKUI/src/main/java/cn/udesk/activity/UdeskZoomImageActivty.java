package cn.udesk.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.udesk.R;
import cn.udesk.widget.KeyBoardUtil;
import cn.udesk.widget.UdeskZoomImageView;

public class UdeskZoomImageActivty extends Activity implements
		OnClickListener {

	private UdeskZoomImageView zoomImageView;
	private View saveIdBtn;
	private String url;
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.udesk_zoom_imageview);
		zoomImageView = (UdeskZoomImageView) findViewById(R.id.udesk_zoom_imageview);
		try{
			Bundle bundle = getIntent().getExtras();
			url = bundle.getString("image_path");
			if (url != null){
				Glide.with(this)
						.load(url)
						.dontAnimate()
						.into(zoomImageView);
			}
		}catch (Exception e){
			e.printStackTrace();
		}

		saveIdBtn = findViewById(R.id.udesk_zoom_save);
		saveIdBtn.setOnClickListener(this);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.udesk_zoom_save) {
			new Thread() {
				public void run() {
					saveImage();
				}

			}.start();
		}

	}
	public void saveImage() {
		if (url == null) {
			return;
		}
		// 获取参数中的原文件路径
		String oldPath = url;
		File oldFile = new File(oldPath);
		// 修改文件路径
		String newName = oldFile.getName();
		if(!newName.contains(".png")){
			newName = newName+".png" ;
		}
		final File folder = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
		final File newFile = new File(folder, newName);
		// 拷贝，成功或者失败 都提示下
		if (copyFile(oldFile, newFile)) {

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(
							UdeskZoomImageActivty.this,
							getResources().getString(
									R.string.udesk_success_save_image)+folder.getAbsolutePath(),
							Toast.LENGTH_SHORT).show();
					UdeskZoomImageActivty.this.finish();
				}
			});

			// scann
//			Uri contentUri = Uri.fromFile(newFile);
//			Intent mediaScanIntent = new Intent(
//					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, contentUri);
//			UdeskZoomImageActivty.this.sendBroadcast(mediaScanIntent);

		} else {

			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(
							UdeskZoomImageActivty.this,
							getResources().getString(
									R.string.udesk_fail_save_image),
							Toast.LENGTH_SHORT).show();
					UdeskZoomImageActivty.this.finish();
				}
			});
		}

	}

	private boolean copyFile(File srcFile, File destFile) {
		boolean result = false;
		try {
			InputStream in = new FileInputStream(srcFile);
			try {
				result = copyToFile(in, destFile);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			result = false;
		}
		return result;
	}

	private boolean copyToFile(InputStream inputStream, File destFile) {
		try {
			if (destFile.exists()) {
				destFile.delete();
			}
			FileOutputStream out = new FileOutputStream(destFile);
			try {
				byte[] buffer = new byte[4096];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) >= 0) {
					out.write(buffer, 0, bytesRead);
				}
			} finally {
				out.flush();
				try {
					out.getFD().sync();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out.close();
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	protected void onDestroy() {
		KeyBoardUtil.fixFocusedViewLeak(this);
		super.onDestroy();
	}
}
