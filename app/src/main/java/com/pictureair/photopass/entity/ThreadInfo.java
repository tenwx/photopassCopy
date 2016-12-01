package com.pictureair.photopass.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 *
 * 线程信息
 * Created by bass on 16/3/1.
 */

@Entity
public class ThreadInfo implements Serializable {
    private static final long serialVersionUID = -7060210544600464481L;
    @Id
    private Long id;//自增长id
    private String url;
    private int threadId;
    private int start;
    private int end;
    private int finished;
    @Generated(hash = 1868453095)
    public ThreadInfo(Long id, String url, int threadId, int start, int end,
            int finished) {
        this.id = id;
        this.url = url;
        this.threadId = threadId;
        this.start = start;
        this.end = end;
        this.finished = finished;
    }
    @Generated(hash = 930225280)
    public ThreadInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public int getThreadId() {
        return this.threadId;
    }
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }
    public int getStart() {
        return this.start;
    }
    public void setStart(int start) {
        this.start = start;
    }
    public int getEnd() {
        return this.end;
    }
    public void setEnd(int end) {
        this.end = end;
    }
    public int getFinished() {
        return this.finished;
    }
    public void setFinished(int finished) {
        this.finished = finished;
    }
}
