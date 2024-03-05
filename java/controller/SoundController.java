package com.coresaken.multiplication.controller;

import android.content.Context;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.data.Sound;

public class SoundController {

    public static SoundController _instance;
    public Sound sound;
    boolean enabled;
    private SoundController(){

    }

    public void initializeSounds(Context context, int[] soundsId){
        sound = new Sound(context, soundsId);
    }

    public void clickButton(){
        if(sound!=null && enabled){
            sound.play(R.raw.sound_click_button_2);
        }
    }

    public void enable(boolean value){
        enabled = value;
    }

    public static SoundController getInstance(){
        if(_instance==null){
            _instance = new SoundController();
        }

        return _instance;
    }
}
