package com.coresaken.multiplication.controller;

public class PlayerSettings {
    private static PlayerSettings _instance;

    public boolean sounds;
    public boolean reminder;
    public int[] reminderTime;
    public int language;

    private PlayerSettings(){
        reminderTime = new int[2];
        reminder = true;
    }


    public static PlayerSettings getInstance(){
        if(_instance == null){
            _instance = new PlayerSettings();
        }

        return _instance;
    }
}
