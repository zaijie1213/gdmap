package com.example.zhigangsong.maptest;

/**
 * Created by zhigang.song on 2016/3/29.
 */
public class ImgBean {
    private String imgUrl;
    private double time;
    private double top;
    private double left;
    private double bottom;

    public ImgBean(String imgUrl, double time, double top, double left, double bottom, double right) {
        this.imgUrl = imgUrl;
        this.time = time;
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public String getImgUrl() {

        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public double getTop() {
        return top;
    }

    public void setTop(double top) {
        this.top = top;
    }

    public double getLeft() {
        return left;
    }

    public void setLeft(double left) {
        this.left = left;
    }

    public double getBottom() {
        return bottom;
    }

    public void setBottom(double bottom) {
        this.bottom = bottom;
    }

    public double getRight() {
        return right;
    }

    public void setRight(double right) {
        this.right = right;
    }

    private double right;

}

