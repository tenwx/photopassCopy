package com.pictureair.photopass.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseFragment;
import com.pictureair.photopass.activity.CameraActivity;
/**
 * Camera界面，需要跳转至camera界面
 * @author bauer_bao
 *
 */
public class FragmentPageCamera extends BaseFragment{
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
	}
	public void onPause() {
	    super.onPause();
	}
	
}