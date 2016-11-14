package com.pictureair.photopass.eventbus;

import com.pictureair.photopass.entity.PhotoInfo;

import java.util.ArrayList;

public class StoryFragmentEvent implements BaseBusEvent {

	private ArrayList<PhotoInfo> photoInfos = new ArrayList<>();
	private ArrayList<PhotoInfo> targetInfos = new ArrayList<>();
	private int tab;
	private boolean isRefresh;

	public StoryFragmentEvent(ArrayList<PhotoInfo> photoInfos, ArrayList<PhotoInfo> targetInfos, int tab, boolean isRefresh) {
		this.photoInfos.clear();
		this.targetInfos.clear();
		this.photoInfos.addAll(photoInfos);
		this.targetInfos.addAll(targetInfos);
		this.tab = tab;
		this.isRefresh = isRefresh;
	}

	public ArrayList<PhotoInfo> getPhotoInfos() {
		return photoInfos;
	}
	
	public ArrayList<PhotoInfo> getTargetInfos() {
		return targetInfos;
	}
	
	public int getTab(){
		return tab;
	}

	public boolean isRefresh() {
		return isRefresh;
	}
}
