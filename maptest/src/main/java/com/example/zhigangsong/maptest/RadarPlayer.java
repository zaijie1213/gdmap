package com.example.zhigangsong.maptest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.amap.api.maps2d.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhigang.song on 2016/3/30.
 * RadarImgPlay control
 */
public class RadarPlayer {
    public static final int MSG_PLAY = 1;
    public static final int MSG_STOP = 2;
    private static final String TAG = "huli";
    public static final int INTERVAL = 140;
    private boolean isPlay = true;
    private List<RadarImageEntity> mRadarImageEntities = new ArrayList<>();
    Handler mHandler;
    private int lastPlayIndex = 0;

    private ExecutorService mExecutor;
    private AnimateThread mLastThread;
    private RadarImgData mRadarImgData;

    public RadarPlayer(Handler handler) {
        this.mHandler = handler;
        mExecutor = Executors.newSingleThreadExecutor();
        mRadarImgData = new RadarImgData();
    }

    public void clearRadarImages() {
        this.mRadarImageEntities.clear();
    }


    private void resetRaderImages(List<RadarImageEntity> radarImageEntities) {
        this.mRadarImageEntities = radarImageEntities;
        this.start();
    }


    public void start() {
        if (mRadarImageEntities != null && mRadarImageEntities.size() > 0) {
            this.isPlay = true;
            if (mLastThread != null) {
                mLastThread.stopSelf();
            }
            mLastThread = new AnimateThread(0);
            mExecutor.execute(mLastThread);
        }
    }

    public void pause() {
        this.isPlay = false;
        if (null != mLastThread) {
            mLastThread.stopSelf();
        }
    }

    public void resume() {
        Log.d(TAG, "player resume from index " + lastPlayIndex);
        this.isPlay = true;
        mLastThread = new AnimateThread(lastPlayIndex);
        mExecutor.execute(mLastThread);
    }

    public void hide() {
        this.isPlay = false;
        Message message = Message.obtain();
        message.what = MSG_STOP;
        mHandler.sendMessage(message);
    }


    public void destory() {
        isPlay = false;
        if (mLastThread != null) {
            mLastThread.stopSelf();
        }
        mExecutor.shutdown();
    }

    public boolean isPlay() {
        return isPlay;
    }

    public void changPosition(LatLng target, float zoom) {
        Log.d(TAG, "player start cache img");
        mRadarImgData.requestImages(target, zoom, new RadarImgData.LoadImgListener() {
            @Override
            public void onSuccess(List<RadarImageEntity> radarImageEntities) {
                RadarPlayer.this.resetRaderImages(radarImageEntities);
                Log.d(TAG, "cache img success");
            }

            @Override
            public void onFail(String reason) {
                Log.d(TAG, "cache img fail");
            }
        });
    }

    private class AnimateThread extends Thread {
        private int startIndex;
        private boolean isPlay = true;

        public void stopSelf() {
            this.isPlay = false;
        }

        public AnimateThread(int startIndex) {
//            Log.d(TAG, "player init from  index " + startIndex);
            this.startIndex = startIndex;
        }

        @Override
        public void run() {
//            Log.d(TAG, "start play from " + startIndex);
            while (this.isPlay && RadarPlayer.this.isPlay) {
                for (int i = startIndex; i < mRadarImageEntities.size(); i++) {
                    if (this.isPlay && RadarPlayer.this.isPlay) {
//                        Log.d(TAG, "player play index " + i);
                        RadarImageEntity radarImageEntity = mRadarImageEntities.get(i);
                        Message message = Message.obtain();
                        message.obj = radarImageEntity;
                        message.what = MSG_PLAY;
                        lastPlayIndex = i;
                        mHandler.sendMessage(message);
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } else {
                        return;
                    }
                }
                startIndex = 0;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


}
