package com.pictureair.photopassCopy.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

import java.io.Serializable;

/**
 * 断点下载
 * 线程信息
 * Created by bass on 16/3/1.
 */

@Entity
public class ThreadInfo implements Serializable {
    private static final long serialVersionUID = -7060210544600464481L;
    @Id
    private Long id;//自增长id
    private int threadId;
    private String url;
    private long start;
    private long end;
    private long finished;
    @Generated(hash = 568823959)
    public ThreadInfo(Long id, int threadId, String url, long start, long end,
            long finished) {
        this.id = id;
        this.threadId = threadId;
        this.url = url;
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
    public int getThreadId() {
        return this.threadId;
    }
    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public long getStart() {
        return this.start;
    }
    public void setStart(long start) {
        this.start = start;
    }
    public long getEnd() {
        return this.end;
    }
    public void setEnd(long end) {
        this.end = end;
    }
    public long getFinished() {
        return this.finished;
    }
    public void setFinished(long finished) {
        this.finished = finished;
    }
}
