package com.pictureair.hkdlphotopass.entity;

import java.io.Serializable;

/**
 * 文件信息
 * Created by bass on 16/3/1.
 */
public class FileInfo implements Serializable {
    private String fileName;
    private int id;
    private int length;
    private String url;
    private int finished;

    public FileInfo() {
        super();
    }

    public FileInfo(String fileName, int id, int length, String url, int finished) {
        this.fileName = fileName;
        this.id = id;
        this.length = length;
        this.url = url;
        this.finished = finished;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getFinished() {
        return finished;
    }

    public void setFinished(int finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
