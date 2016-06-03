package com.pictureair.hkdlphotopass.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pictureair.hkdlphotopass.R;
import com.pictureair.hkdlphotopass.activity.BaseFragment;
import com.pictureair.hkdlphotopass.util.DisneyVideoTool;
import com.pictureworks.android.util.PictureAirLog;

/**
 * Camera界面，需要跳转至camera界面
 *
 * @author bauer_bao
 */
public class FragmentPageCamera extends BaseFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        DisneyVideoTool.getIsOneGoToDisneyVideoPage(getActivity());
        return inflater.inflate(R.layout.fragment_camera, null);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        PictureAirLog.out("FragmentPageCamera" + "  ==onResume");
    }

    public void onPause() {
        super.onPause();
    }

}