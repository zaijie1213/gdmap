package com.example.zhigangsong.maptest;

import com.amap.api.maps2d.model.LatLngBounds;

import java.io.File;

/**
 * Created by zhigang.song on 2016/3/29.
 */
public class RadarImage {
    private String imgUrl;
    private double time;
    private LatLngBounds mLatLngBounds;
    private boolean isCached = false;
    private File path;

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public boolean isCached() {
        return isCached;
    }

    public void setIsCached(boolean isCached) {
        this.isCached = isCached;
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

    public LatLngBounds getLatLngBounds() {
        return mLatLngBounds;
    }

    public void setLatLngBounds(LatLngBounds latLngBounds) {
        mLatLngBounds = latLngBounds;
    }

    public RadarImage(String imgUrl, double time, LatLngBounds latLngBounds) {
        this.imgUrl = imgUrl;
        this.time = time;
        mLatLngBounds = latLngBounds;

    }
}

