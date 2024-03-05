package com.coresaken.multiplication.listener;

public interface HeartLimitListener {
    void onChangeHeartAmount(int currentAmount);
    void onLostAllHeart();
}
