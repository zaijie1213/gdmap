package com.example.zhigangsong.maptest;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.amap.api.maps2d.model.LatLng;
import com.amap.api.maps2d.model.LatLngBounds;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Created by zhigang.song on 2016/3/31.
 */
public class RadarImgData {
    private AsyncHttpClient mAsyncHttpClient;
    private List<RadarImageEntity> mImageEntities = new ArrayList<>();



    public static final String COUNTRY_URL = "http://caiyunapp.com/fcgi-bin/v1/img.py?token=Y2FpeXVuIGFuZHJpb2QgYXBp";
    public static final String SITE_URL = "http://caiyunapp.com/fcgi-bin/v1/api.py?lonlat=%s,%s&format=json&product=minutes_prec&token=Y2FpeXVuIGFuZHJpb2QgYXBp";

    public static final int ZOOM_CHANGE = 7;

    private List<RadarImageEntity> parse(JSONObject jsonObject) {
        List<RadarImageEntity> radarImageEntities = new ArrayList<>();
        if (jsonObject.optString("status").equals("ok")) {
            JSONArray array = jsonObject.optJSONArray("radar_img");
            if (null == array) {
                return radarImageEntities;
            }
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONArray array1 = array.getJSONArray(i);
                    JSONArray array2 = array1.getJSONArray(2);
                    LatLngBounds bounds = new LatLngBounds(new LatLng(array2.getDouble(0), array2.getDouble(1)), new LatLng(array2.getDouble(2), array2.getDouble(3)));
                    String url = array1.optString(0);
                    if (!url.startsWith("http")) {
                        url = "http://caiyunapp.com" + url;
                    }
                    radarImageEntities.add(new RadarImageEntity(url, array1.optDouble(1), bounds));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return radarImageEntities;
    }

    public void requestImages(LatLng latLng, double zoom, final LoadImgListener listener) {
        String url;
        if (zoom < ZOOM_CHANGE) {
            url = COUNTRY_URL;
        } else {
            url = String.format(SITE_URL, latLng.longitude, latLng.latitude);
        }
        Log.d("huli", url);
        mAsyncHttpClient.get(url, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                mImageEntities = RadarImgData.this.parse(response);
                cacheImg(listener);
            }
        });
    }


    public RadarImgData() {
        mAsyncHttpClient = new AsyncHttpClient();
    }

    private void saveImgPath(LoadImgListener listener) {
        for (RadarImageEntity radarImageEntity : mImageEntities) {
            if (radarImageEntity.isCached()) {
                File file = ImageLoader.getInstance().getDiskCache().get(radarImageEntity.getUrl());
                radarImageEntity.setCachePath(file.getPath());
            }
        }
        listener.onSuccess(mImageEntities);
    }

    public void cacheImg(final LoadImgListener listener) {

        final int[] loadCount = {0};
        DisplayImageOptions options = new DisplayImageOptions.Builder()
                .cacheOnDisk(true)
                .cacheInMemory(false)
                .build();
        for (int i = 0; i < mImageEntities.size(); i++) {
            final int finalI = i;
            ImageLoader.getInstance().loadImage(mImageEntities.get(i).getUrl(), options, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    super.onLoadingComplete(imageUri, view, loadedImage);
                    if (mImageEntities.size() > finalI) {
                        mImageEntities.get(finalI).setIsCached(true);
                        loadCount[0]++;
                        if (loadCount[0] == mImageEntities.size()) {
                            RadarImgData.this.saveImgPath(listener);
                        }
                    }
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    super.onLoadingFailed(imageUri, view, failReason);
                    loadCount[0]++;
                    if (loadCount[0] == mImageEntities.size()) {
                        RadarImgData.this.saveImgPath(listener);
                    }
                }
            });
        }
    }

    public interface LoadImgListener {
        void onSuccess(List<RadarImageEntity> radarImageEntities);

        void onFail(String reason);
    }
}
