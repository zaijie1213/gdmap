package com.example.zhigangsong.maptest;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by zhigang.song on 2016/3/30.
 */
public class RadarPlayer {
    public static final int MSG_PLAY = 1;
    public static final int MSG_STOP = 2;
    private static final String TAG = "huli";
    public static final int INTERVAL = 200;
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
        this.isPlay = true;
        if (mLastThread != null) {
            mLastThread.stopSelf();
        }
        mLastThread = new AnimateThread(0);
        mExecutor.execute(mLastThread);
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
        mHandler.removeMessages(MSG_PLAY);
        this.isPlay = false;
        this.lastPlayIndex = 0;
    }

    public void clear() {
        this.stop();
        Message message = Message.obtain();
        message.what = MSG_STOP;
        mHandler.sendMessage(message);
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
            while (this.isPlay && RadarPlayer.this.isPlay) {
                for (int i = startIndex; i < mRadarImages.size(); i++) {
                    if (this.isPlay && RadarPlayer.this.isPlay) {
                        RadarImage radarImage = mRadarImages.get(i);
                        Message message = Message.obtain();
                        message.obj = radarImage;
                        message.what = MSG_PLAY;
                        message.arg1 = i;
                        lastPlayIndex = i;
                        mHandler.sendMessage(message);
                        try {
                            Thread.sleep(INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startIndex = 0;
            }
        }
    }
}
