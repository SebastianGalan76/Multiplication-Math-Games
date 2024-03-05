package com.coresaken.multiplication.data;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import java.time.LocalDate;
import java.time.Period;

import com.coresaken.multiplication.controller.AdSystem;

public class User {
    private static User _instance;

    public String name;
    public int exp;

    public int launchMainMenu;
    public int rateStatus; //0 - new, 1 - rate, 2 - remind later

    public int adValue;

    public int[] competitionRecord;

    private int lastShowedLevel;


    //Date of birth
    public int day, month, year;
    public int currentAge;

    private User(){
        name = "";
        exp = 0;

        competitionRecord = new int[1];
    }

    public static User getInstance(){
        if(_instance==null){
            _instance = new User();
        }

        return _instance;
    }

    public int getCurrentLevel(){
        lastShowedLevel = exp/100 +1;

        return lastShowedLevel;
    }

    public boolean reachedNewLevel(int gainedExp){
        int lvl = getLevelAfterUpgrade(gainedExp);
        if( lvl > lastShowedLevel){
            lastShowedLevel = lvl;

            return true;
        }

        return false;
    }

    public int getLevelAfterUpgrade(int gainedExp){
        return (gainedExp + exp) / 100 + 1;
    }

    public void setNewRecord(int competitionIndex, int record){
        competitionRecord[competitionIndex] = record;
    }

    public void changeAdValue(int value){
        adValue += value;
        if(adValue<-20){
            adValue = -20;
        }

        if(adValue>20){
            adValue = 20;
        }
    }

    public void increaseAdValue(Activity activity, boolean showAd){
        changeAdValue(1);

        if(showAd && checkAdValue()){
            AdSystem.getInstance().showAd(activity);
        }

        Log.d("AdMob", "AdValue: " + adValue);
    }

    public boolean checkAdValue(){
        return adValue > 0 && adValue >= 10;
    }

    public void setAge(SharedPreferences sharedPref){
        day = sharedPref.getInt("day_birth", -1);
        month = sharedPref.getInt("month_birth", -1);
        year = sharedPref.getInt("year_birth", -1);

        if(day==-1 || month ==-1 || year==-1){
            currentAge = -1;
        }
        else{
            currentAge = getAgeByDateOfBirth(day, month, year);
        }

        Log.d("XD", "Current age: "+currentAge);
    }

    public int getAgeByDateOfBirth(int day, int month, int year){
        LocalDate currentDate = LocalDate.now();
        LocalDate birthDate = LocalDate.of(year, month, day);
        return  Period.between(birthDate, currentDate).getYears();
    }
}
