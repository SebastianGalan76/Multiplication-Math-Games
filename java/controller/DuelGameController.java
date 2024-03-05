package com.coresaken.multiplication.controller;

import com.coresaken.multiplication.data.DuelPlayer;


public class DuelGameController {
    private static DuelGameController _instance;

    public DuelPlayer[] players;

    private DuelGameController(){
        players = new DuelPlayer[2];
    }

    public static DuelGameController getInstance(){
        if(_instance == null){
            _instance = new DuelGameController();
        }

        return _instance;
    }
}
