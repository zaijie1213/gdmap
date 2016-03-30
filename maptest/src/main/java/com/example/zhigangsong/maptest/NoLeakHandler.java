package com.example.zhigangsong.maptest;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by zhigang.song on 2016/3/30.
 */
public abstract class NoLeakHandler<T> extends Handler
{
    private WeakReference<T> mT;

    public NoLeakHandler(T outClass)
    {
        mT = new WeakReference<>(outClass);
    }

    @Override
    public void handleMessage(Message msg)
    {
        T t = mT.get();
        handleMessage(msg, t);
    }

    public abstract void handleMessage(Message msg, T t);
}
