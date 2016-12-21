package com.pictureair.photopass.eventbus;

import com.pictureair.photopass.entity.PPinfo;

import java.util.List;

/**
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
