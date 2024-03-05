package com.coresaken.multiplication.controller;

import android.content.Intent;
import android.util.Log;

import com.coresaken.multiplication.activity.game.GameSummaryActivity;
import com.coresaken.multiplication.activity.panel.DailyStreakActivity;
import com.coresaken.multiplication.activity.panel.LessonProgressActivity;
import com.coresaken.multiplication.activity.panel.LevelUpActivity;
import com.coresaken.multiplication.activity.panel.LostHeartActivity;
import com.coresaken.multiplication.data.User;
import com.coresaken.multiplication.data.enums.ModeType;

import java.util.ArrayList;
import java.util.List;

public class PanelController {
    private static PanelController _instance;

    private List<Class> classes;



    private PanelController(){

    }

    public void finishGame(){
        classes = new ArrayList<>();
        classes.add(DailyStreakActivity.class);
        classes.add(LevelUpActivity.class);
        classes.add(LessonProgressActivity.class);
    }

    public Class getNextPanel(){
        if(classes!=null && classes.size()>0){
            Class nextActivity = classes.remove(0);

            if(nextActivity == LostHeartActivity.class){

            }
            else if(nextActivity == DailyStreakActivity.class){
                if(DailyStreakController.getInstance().getLastShowedPercent()<100){
                    return DailyStreakActivity.class;
                }
                return getNextPanel();
            }
            else if(nextActivity == LevelUpActivity.class){
                if(User.getInstance().reachedNewLevel(GameController.getInstance().statistic.getGainedExp())){
                    return LevelUpActivity.class;
                }
                return getNextPanel();
            }
            else if(nextActivity == LessonProgressActivity.class){
                if(SettingsController.getInstance().settings.modeType == ModeType.LESSON){
                    if(GameController.getInstance().lessonProgressStart<100){
                        return LessonProgressActivity.class;
                    }

                    return getNextPanel();
                }

                return getNextPanel();
            }
            else{
                return getNextPanel();
            }
        }

        return GameSummaryActivity.class;
    }


    public static PanelController getInstance(){
        if(_instance==null){
            _instance = new PanelController();
        }

        return _instance;
    }
}
