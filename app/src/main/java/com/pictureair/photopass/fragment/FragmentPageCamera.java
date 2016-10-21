package com.pictureair.photopass.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.pictureair.photopass.R;
import com.pictureair.photopass.activity.BaseLazyFragment;
import com.pictureair.photopass.util.PictureAirLog;

/**
 * Camera界面，需要跳转至camera界面
 *
 * @author bauer_bao
 */
public class FragmentPageCamera extends BaseLazyFragment {
    private View view;
    // 标志fragment是否初始化完成
    private boolean isPrepared;
    private boolean isInited;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_camera, null);
            isPrepared = true;
            onLazyLoad();
        }

        return view;
    }

    @Override
    protected void onLazyLoad() {
        if (!isPrepared || !isVisible || isInited) {
            return;
        }
        isInited = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
    }

    public void onResume() {
        super.onResume();
        if (isPrepared && isVisible) {
            PictureAirLog.out("FragmentPageCamera" + "  ==onResume");
        }
    }

    public void onPause() {
        super.onPause();
        if (isPrepared && isVisible) {
            PictureAirLog.out("FragmentPageCamera" + "  ==onPause");
        }
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        if (isPrepared && isVisible) {
            PictureAirLog.out("FragmentPageCamera" + "  ==onVisible");
        }
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
        if (isPrepared && !isVisible) {
            PictureAirLog.out("FragmentPageCamera" + "  ==onInVisible");
        }
    }
}