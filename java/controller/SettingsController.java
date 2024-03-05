package com.coresaken.multiplication.controller;

import android.util.Log;

import com.coresaken.multiplication.activity.MainMenuActivity;
import com.coresaken.multiplication.activity.game.Game1Activity;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.GameType;

public class SettingsController {

    private static SettingsController _instance;

    public Settings settings;

    public Settings presetSettings;


    private SettingsController(){

    }

    public Class getActivity(){
        if(settings==null){
            Log.w("ERROR", "ERRRRRRRRRRR SettingsController");
            return MainMenuActivity.class;
        }
        else if(settings.gameType == null){
            return Game1Activity.class;
        }

        return settings.gameType.getActivityClass();
    }

    public static SettingsController getInstance(){
        if(_instance==null){
            _instance = new SettingsController();
        }

        return _instance;
    }
}
