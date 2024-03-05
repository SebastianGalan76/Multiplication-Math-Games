package com.coresaken.multiplication.data;

import android.util.Log;

public class GameStatistic {
    public int equationLimit;
    public int currentEquation;

    public int totalAnswer;
    public int correctAnswer;
    public int incorrectAnswer;
    private int gainedExp;
    public int gainedStar;

    public GameStatistic(){
        equationLimit = 0;
        currentEquation = 0;
        totalAnswer = 0;
        correctAnswer = 0;
        incorrectAnswer = 0;
        gainedExp = 0;
        gainedStar = 0;
    }

    public int getCorrectAnswerPercent(){
        return (int)((float)correctAnswer/(float) currentEquation * 100f);
    }
    public int getIncorrectAnswerPercent(){
        return 100 - getCorrectAnswerPercent();
    }

    public int getFinishProgressPercent(){
        return (int)((float)totalAnswer/(float) equationLimit * 100f);
    }

    public void changeGainedExp(int value){
        gainedExp+=value;

        if(gainedExp<0){
            gainedExp = 0;
        }
    }

    public int getGainedExp(){
        return gainedExp;
    }
}
