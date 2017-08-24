package com.pictureair.hkdlphotopass.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * 第一次进入的数据库类
 * Created by bauer_bao on 16/11/30.
 */
@Entity
public class FirstStartInfo {
    @Id
    private Long id;//自增长id
    private String event;//需要记录第一次的事件
    private String userId;//和用户绑定的userid
    @Generated(hash = 494365126)
    public FirstStartInfo(Long id, String event, String userId) {
        this.id = id;
        this.event = event;
        this.userId = userId;
    }
    @Generated(hash = 1718134203)
    public FirstStartInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getEvent() {
        return this.event;
    }
    public void setEvent(String event) {
        this.event = event;
    }
    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
}
