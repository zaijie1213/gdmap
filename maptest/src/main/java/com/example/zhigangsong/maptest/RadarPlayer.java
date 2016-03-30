package com.example.zhigangsong.maptest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by zhigang.song on 2016/3/30.
 */
public class RadarPlayer {
    public static final int MSG_PLAY = 1;
    public static final int MSG_STOP = 2;
    public static final int MSG_PAUSE = 3;
    public static final int MSG_RESUME = 4;
    private static final String TAG = "huli";
    private boolean isPlay = true;
    private List<RadarImage> mRadarImages;
    Handler mHandler;
    private int lastPlayIndex = 0;

    private ExecutorService mExecutor;
    private AnimateThread mLastThread;

    public RadarPlayer(List<RadarImage> radarImages, Handler handler) {
        this.mRadarImages = radarImages;
        this.mHandler = handler;
        mExecutor = Executors.newSingleThreadExecutor();
    }

    public void clearRadarImgs() {
    }

    public void resetRaderImgs(List<RadarImage> radarImages) {
        this.mRadarImages = radarImages;
    }

    public void start() {
//        this.clearLast();
        mLastThread = new AnimateThread(0);
        mExecutor.execute(mLastThread);
    }

    private void clearLast() {
        mHandler.removeMessages(MSG_PLAY);
        if (mLastThread != null) {
            mLastThread.stopSelf();
        }
    }

    public void pause() {
        this.isPlay = false;
    }

    public void resume() {
        this.isPlay = true;
        mLastThread = new AnimateThread(lastPlayIndex);
        mExecutor.execute(mLastThread);
    }

    public void stop() {
        this.isPlay = false;
        this.lastPlayIndex = 0;
    }

    public void destory() {
        mExecutor.shutdown();
    }

    public boolean isPlay() {
        return isPlay;
    }

    private class AnimateThread extends Thread {
        private int startIndex;
        private boolean isPlay = true;

        public void stopSelf() {
            this.isPlay = false;
        }

        public AnimateThread(int startIndex) {
            this.startIndex = startIndex;
        }

        @Override
        public void run() {
            Log.d(TAG, Thread.currentThread().getName());
            while (RadarPlayer.this.isPlay) {
                for (int i = startIndex; i < mRadarImages.size(); i++) {
                    if (RadarPlayer.this.isPlay) {
                        RadarImage radarImage = mRadarImages.get(i);
                        Message message = Message.obtain();
                        message.obj = radarImage;
                        message.what = MSG_PLAY;
                        message.arg1 = i;
                        lastPlayIndex = i;
                        Log.e(TAG,"index is "+ i);
                        mHandler.sendMessage(message);
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                startIndex = 0;
            }
        }
    }
}
