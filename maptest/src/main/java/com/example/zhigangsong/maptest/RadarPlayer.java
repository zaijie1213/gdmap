package com.example.zhigangsong.maptest;

import android.os.Handler;
import android.os.Message;

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
    private boolean isPlay = false;
    private List<RadarImage> mRadarImages;
    Handler mHandler;

    private ScheduledExecutorService mExecutor;

    public RadarPlayer(List<RadarImage> radarImages, Handler handler) {
        this.mRadarImages = radarImages;
        this.mHandler = handler;
        mExecutor = Executors.newSingleThreadScheduledExecutor();
    }

    public void clearRadarImgs() {
    }

    public void resetRaderImgs(List<RadarImage> radarImages) {
        this.mRadarImages = radarImages;
    }

    public void play() {
        mHandler.removeMessages(MSG_PLAY);
        for (int i = 0; i < mRadarImages.size(); i++) {
            mExecutor.execute(new PlayRaderImage(mRadarImages.get(i),i));
        }
    }

    public void pause() {
    }

    public void resume() {
    }

    public void stop() {
    }

    public boolean isPlay() {
        return false;
    }

    class PlayRaderImage implements Runnable {
        private RadarImage radarImage;
        private int index;

        public PlayRaderImage(RadarImage radarImage, int i) {
            this.radarImage = radarImage;
            this.index = i;
        }

        @Override
        public void run() {
            Message message = Message.obtain();
            message.what = RadarPlayer.MSG_PLAY;
            message.obj = radarImage;
            message.arg1 = index;
            mHandler.sendMessage(message);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
