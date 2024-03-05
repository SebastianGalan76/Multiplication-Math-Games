package com.coresaken.multiplication.data.enums;

import com.coresaken.multiplication.activity.competition.Competition0Activity;
import com.coresaken.multiplication.activity.game.DuelGameActivity;
import com.coresaken.multiplication.activity.game.Game1Activity;
import com.coresaken.multiplication.activity.game.GameCardActivity;
import com.coresaken.multiplication.activity.game.GameDrawActivity;
import com.coresaken.multiplication.activity.game.GameInputActivity;
import com.coresaken.multiplication.activity.game.GameLavaActivity;
import com.coresaken.multiplication.activity.game.GameMatchActivity;
import com.coresaken.multiplication.activity.game.GamePuzzleActivity;
import com.coresaken.multiplication.activity.game.GameTrueFalseActivity;

import java.util.Arrays;
import java.util.List;

public enum GameType {
    GAME1(0,Game1Activity.class, Arrays.asList(LimitType.EQUATION, LimitType.TIME), Arrays.asList(AnswerType.RESULT, AnswerType.MIXED)),
    TRUE_FALSE(1,GameTrueFalseActivity.class, Arrays.asList(LimitType.EQUATION, LimitType.TIME), Arrays.asList(AnswerType.RESULT)),
    LAVA(3, GameLavaActivity.class, Arrays.asList(LimitType.EQUATION), Arrays.asList(AnswerType.RESULT, AnswerType.MIXED)),
    DRAW(7, GameDrawActivity.class, Arrays.asList(LimitType.HEART, LimitType.TIME), Arrays.asList(AnswerType.DEFAULT)),
    INPUT(2, GameInputActivity.class, Arrays.asList(LimitType.EQUATION, LimitType.TIME), Arrays.asList(AnswerType.RESULT, AnswerType.MIXED)),
    PUZZLE(5,GamePuzzleActivity.class, Arrays.asList(LimitType.TIME), Arrays.asList(AnswerType.DEFAULT)),
    CARD(6, GameCardActivity.class, Arrays.asList(LimitType.TIME, LimitType.HEART), Arrays.asList(AnswerType.DEFAULT)),
    DUEL(8, DuelGameActivity.class, Arrays.asList(LimitType.EQUATION), Arrays.asList(AnswerType.RESULT, AnswerType.MIXED)),
    MATCH(4, GameMatchActivity.class, Arrays.asList(LimitType.HEART, LimitType.TIME), Arrays.asList(AnswerType.DEFAULT)),
    COMPETITION(9, Competition0Activity.class, Arrays.asList(LimitType.HEART), Arrays.asList(AnswerType.MIXED));

    public int index;
    Class aClass;
    List<LimitType> allowedLimits;
    List<AnswerType> allowedAnswerTypes;

    GameType(int index, Class aClass, List<LimitType> allowedLimits, List<AnswerType> allowedAnswerTypes){
        this.index = index;
        this.aClass = aClass;
        this.allowedLimits = allowedLimits;
        this.allowedAnswerTypes = allowedAnswerTypes;
    }

    public Class getActivityClass(){
        return aClass;
    }
    public List<LimitType> getAllowedLimits(){
        return allowedLimits;
    }

    public List<AnswerType> getAllowedAnswerTypes(){
        return allowedAnswerTypes;
    }

    public static GameType getGameTypeByIndex(int index){
        switch (index){
            case 0:
                return GAME1;
            case 1:
                return TRUE_FALSE;
            case 2:
                return INPUT;
            case 3:
                return LAVA;
            case 4:
                return MATCH;
            case 5:
                return PUZZLE;
            case 6:
                return CARD;
            case 7:
                return DRAW;
            case 8:
                return DUEL;
        }

        return GAME1;
    }
}
