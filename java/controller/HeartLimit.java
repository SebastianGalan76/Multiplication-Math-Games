package com.coresaken.multiplication.controller;

import com.coresaken.multiplication.listener.HeartLimitListener;

public class HeartLimit {

    public int heartAmount;
    HeartLimitListener listener;

    public HeartLimit(int heartAmount, HeartLimitListener listener){
        this.heartAmount = 0;
        this.listener = listener;

        changeHeartAmount(heartAmount);
    }

    public void changeHeartAmount(int value){
        heartAmount += value;

        listener.onChangeHeartAmount(heartAmount);

        if(heartAmount<=0){
            listener.onLostAllHeart();
        }
    }

    public int getHeartAmount(){
        return heartAmount;
    }
}
