package com.z012.chengdu.sc.impl;

import android.os.CountDownTimer;

public class CountDownTimerImpl extends CountDownTimer {

    public static final long SEC = 1000;

    public CountDownTimerImpl(int time, CountDownTimerListener listener) {
        super(time * 1000, 1000);
        this.listener = listener;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (null != listener) {
            listener.onTick(millisUntilFinished);
        }
    }

    @Override
    public void onFinish() {
        if (null != listener) {
            listener.onFinish();
        }
    }

    private CountDownTimerListener listener = null;

    public interface CountDownTimerListener {
        void onTick(long time);
        void onFinish();
    }

    public void stop() {
        if (null != listener) {
            listener = null;
        }
        cancel();
    }
}
