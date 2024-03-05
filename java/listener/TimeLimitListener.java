package com.coresaken.multiplication.listener;

public interface TimeLimitListener {

    void onTimeFinish();
    void onTick(long timeLeft);
}
