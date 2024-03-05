package com.coresaken.multiplication.controller;

import com.coresaken.multiplication.activity.game.GameCardActivity;
import com.coresaken.multiplication.data.AnsweredEquation;
import com.coresaken.multiplication.data.GameStatistic;
import com.coresaken.multiplication.data.enums.GameType;

import java.util.ArrayList;
import java.util.List;

public class GameController {
    private static GameController _instance;

    public List<AnsweredEquation> answeredEquations;
    public List<GameCardActivity.AnsweredEquation> answeredEquationsCard;


    public GameType gameType;

    public GameStatistic statistic;

    public HeartLimit heartLimit;
    public TimeLimit timeLimit;

    public int lessonProgressStart;
    public int lessonProgressEnd;

    private GameController(){
        answeredEquations = new ArrayList<>();
        answeredEquationsCard = new ArrayList<>();
    }
    public void startNewGame(GameType gameType){
        answeredEquations.clear();
        answeredEquationsCard.clear();

        heartLimit = null;
        timeLimit = null;

        this.gameType = gameType;
    }
    public void finishGame(GameStatistic statistic){
        this.statistic = statistic;
    }
    public static GameController getInstance(){
        if(_instance == null){
            _instance = new GameController();
        }

        return _instance;
    }
}
