package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/23.
 * 添加图片的配置信息
 */
public class EmbedPhotoConfig implements Serializable {

    private List<Foreground> foreground;
    private Background background;
    private Output output;
    private List<Middle> middle;

    public List<Foreground> getForeground() {
        return foreground;
    }

    public void setForeground(List<Foreground> foreground) {
        this.foreground = foreground;
    }

    public Background getBackground() {
        return background;
    }

    public void setBackground(Background background) {
        this.background = background;
    }

    public Output getOutput() {
        return output;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public List<Middle> getMiddle() {
        return middle;
    }

    public void setMiddle(List<Middle> middle) {
        this.middle = middle;
    }

    private class Foreground implements Serializable {
        private String url; //; //string,前景图地址
        private int seq; //int,前景图序号
        private int width; //number,前景图宽度
        private int height; //number,前景图高度
        private int topMargin; //number,上边距
        private int leftMargin; //number 左边距

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getSeq() {
            return seq;
        }

        public void setSeq(int seq) {
            this.seq = seq;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getTopMargin() {
            return topMargin;
        }

        public void setTopMargin(int topMargin) {
            this.topMargin = topMargin;
        }

        public int getLeftMargin() {
            return leftMargin;
        }

        public void setLeftMargin(int leftMargin) {
            this.leftMargin = leftMargin;
        }
    }

    private class Background implements Serializable {
        private String url; //string,背景图地址
        private int width; //number,背景图宽度
        private int height; //number,背景图高度
        private int topMargin; //number,上边距
        private int leftMargin; //number 左边距

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getTopMargin() {
            return topMargin;
        }

        public void setTopMargin(int topMargin) {
            this.topMargin = topMargin;
        }

        public int getLeftMargin() {
            return leftMargin;
        }

        public void setLeftMargin(int leftMargin) {
            this.leftMargin = leftMargin;
        }
    }

    private class Output implements Serializable {
        private int width; //number,最终输出图宽度
        private int height; //number 最终输出图高度

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    private class Middle implements Serializable {
        private String url; //string, 填充图地址
        private int width; //number, 填充图宽度
        private int height; //number,填充图高度
        private int topMargin; //number, 上边距
        private int leftMargin; //number 左边距
        private int rotate; //number,旋转角度
        private int scale; //number 偏移度

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getTopMargin() {
            return topMargin;
        }

        public void setTopMargin(int topMargin) {
            this.topMargin = topMargin;
        }

        public int getLeftMargin() {
            return leftMargin;
        }

        public void setLeftMargin(int leftMargin) {
            this.leftMargin = leftMargin;
        }

        public int getRotate() {
            return rotate;
        }

        public void setRotate(int rotate) {
            this.rotate = rotate;
        }

        public int getScale() {
            return scale;
        }

        public void setScale(int scale) {
            this.scale = scale;
        }
    }

}
