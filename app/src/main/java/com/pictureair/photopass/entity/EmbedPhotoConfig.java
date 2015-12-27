package com.pictureair.photopass.entity;

import java.io.Serializable;
import java.util.List;

/**
 * Created by milo on 15/12/23.
 * 添加图片的配置信息
 */
public class EmbedPhotoConfig implements Serializable {

    private List<Foreground> foreground = null;
    private Background background = null;
    private Output output = null;
    private List<Middle> middle = null;

    public EmbedPhotoConfig() {
    }

    public EmbedPhotoConfig(List<Foreground> foreground, Background background, Output output, List<Middle> middle) {
        this.foreground = foreground;
        this.background = background;
        this.output = output;
        this.middle = middle;
    }

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
        private String url = ""; //; //string,前景图地址
        private int seq = 0; //int,前景图序号
        private int width = 0; //number,前景图宽度
        private int height = 0; //number,前景图高度
        private int topMargin = 0; //number,上边距
        private int leftMargin = 0; //number 左边距

        public Foreground() {
        }

        public Foreground(String url, int seq, int width, int height, int topMargin, int leftMargin) {
            this.url = url;
            this.seq = seq;
            this.width = width;
            this.height = height;
            this.topMargin = topMargin;
            this.leftMargin = leftMargin;
        }

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
        private String url = ""; //string,背景图地址
        private int width = 0; //number,背景图宽度
        private int height = 0; //number,背景图高度
        private int topMargin = 0; //number,上边距
        private int leftMargin = 0; //number 左边距

        public Background(int leftMargin, String url, int width, int height, int topMargin) {
            this.leftMargin = leftMargin;
            this.url = url;
            this.width = width;
            this.height = height;
            this.topMargin = topMargin;
        }

        public Background() {
        }

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
        private int width = 0; //number,最终输出图宽度
        private int height = 0; //number 最终输出图高度

        public Output() {
        }

        public Output(int width, int height) {
            this.width = width;
            this.height = height;
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
    }

    private class Middle implements Serializable {
        private String url = ""; //string, 填充图地址
        private int width = 0; //number, 填充图宽度
        private int height = 0; //number,填充图高度
        private int topMargin = 0; //number, 上边距
        private int leftMargin = 0; //number 左边距
        private int rotate = 0; //number,旋转角度
        private int scale = 0; //number 偏移度

        public Middle() {
        }

        public Middle(String url, int width, int height, int topMargin, int leftMargin, int rotate, int scale) {
            this.url = url;
            this.width = width;
            this.height = height;
            this.topMargin = topMargin;
            this.leftMargin = leftMargin;
            this.rotate = rotate;
            this.scale = scale;
        }

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
