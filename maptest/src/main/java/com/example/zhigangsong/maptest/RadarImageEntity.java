package com.example.zhigangsong.maptest;

import com.amap.api.maps2d.model.LatLngBounds;

/**
 * Created by zhigang.song on 2016/3/29.
 */
public class RadarImageEntity {
    private String url;
    private double time;
    private LatLngBounds area;
    private boolean isCached = false;
    private String cachePath;

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(String cachePath) {
        this.cachePath = cachePath;
    }

    public boolean isCached() {
        return isCached;
    }

    public void setIsCached(boolean isCached) {
        this.isCached = isCached;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public LatLngBounds getArea() {
        return area;
    }

    public void setArea(LatLngBounds area) {
        this.area = area;
    }

    public RadarImageEntity(String url, double time, LatLngBounds area) {
        this.url = url;
        this.time = time;
        this.area = area;
    }
}

