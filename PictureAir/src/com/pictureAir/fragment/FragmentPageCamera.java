package com.pictureAir.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pictureAir.CameraActivity;
import com.pictureAir.R;
import com.umeng.analytics.MobclickAgent;
/**
 * Camera界面，需要跳转至camera界面
 * @author bauer_bao
 *
 */
public class FragmentPageCamera extends Fragment{
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {	
		Intent intent = new Intent(getActivity(), CameraActivity.class);
		FragmentPageCamera.this.startActivity(intent);
		return inflater.inflate(R.layout.fragment_camera, null);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}
	
	public void onResume() {
	    super.onResume();
	    MobclickAgent.onPageStart("FragmentPageCamera"); //统计页面
	}
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPageEnd("FragmentPageCamera"); 
	}
	
}