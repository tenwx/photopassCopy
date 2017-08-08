package com.pictureair.photopassCopy.eventbus;

import com.pictureair.photopassCopy.entity.PPinfo;

import java.util.List;

/**
 * pp删除事件
 * Created by pengwu on 16/12/15.
 */

public class PPDeleteEvent implements BaseBusEvent {

    private List<PPinfo> ppList;

    public PPDeleteEvent(List<PPinfo> ppList) {
        this.ppList = ppList;
    }

    public List<PPinfo> getPpList() {
        return ppList;
    }

    public void setPpList(List<PPinfo> ppList) {
        this.ppList = ppList;
    }
}
