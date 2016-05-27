package com.pictureworks.android.eventbus;

import com.pictureworks.android.entity.PhotoInfo;

import java.util.ArrayList;

public class StoryFragmentEvent implements BaseBusEvent {

	private ArrayList<PhotoInfo> photoInfos = new ArrayList<>();
	private ArrayList<PhotoInfo> targetInfos = new ArrayList<>();
	private int tab;

	public StoryFragmentEvent(ArrayList<PhotoInfo> photoInfos, ArrayList<PhotoInfo> targetInfos, int tab) {
		this.photoInfos.clear();
		this.targetInfos.clear();
		this.photoInfos.addAll(photoInfos);
		this.targetInfos.addAll(targetInfos);
		this.tab = tab;
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

}
