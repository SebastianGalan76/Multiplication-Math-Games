package com.coresaken.multiplication.controller;

import android.os.CountDownTimer;

import com.coresaken.multiplication.listener.TimeLimitListener;

public class TimeLimit {

    CountDownTimer timer;
    public long timeLeft;

    TimeLimitListener listener;

    public TimeLimit(long duration, TimeLimitListener listener){
        this.listener = listener;
        startTimer(duration);
    }

    public void startTimer(long duration){
        if(timer!=null){
            timer.cancel();
        }
        timeLeft = duration;

        timer = new CountDownTimer(duration, 1000) {
            @Override
            public void onTick(long l) {
                timeLeft -= 1000;
                listener.onTick(timeLeft);
            }

            @Override
            public void onFinish() {
                listener.onTimeFinish();
            }
        }.start();
    }

    public void stopTimer(){
        timer.cancel();
    }
    public long getTimeLeft(){
        return timeLeft;
    }
}
