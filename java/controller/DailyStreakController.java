package com.coresaken.multiplication.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.time.LocalDate;

public class DailyStreakController {
    private static DailyStreakController _instance;
    private long timeToday;
    private int lastShowedPercent;


    //Statistic
    private int longestStreak;
    private int currentStreak;
    private int totalStreakDays;
    String lastDayStreak;

    private long startCountingTime;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    private DailyStreakController(){
        timeToday = 0;
    }

    public void startCounting(){
        startCountingTime = System.currentTimeMillis();
    }

    public void stopCounting(){
        long currentTime = System.currentTimeMillis();
        long time = currentTime - startCountingTime;
        startCountingTime = currentTime;

        timeToday+=time;
        if(editor!=null){
            editor.putLong("timeToday", timeToday).apply();
        }
    }

    public int getTimeInSecond(){
        return (int)(timeToday/1000);
    }

    public int getPercent(){
        int percent = (int)(timeToday/3000);
        lastShowedPercent = percent;

        if(sharedPref!=null){
            if(editor == null){
                editor = sharedPref.edit();
            }
        }
        else{
            return 0;
        }

        if(editor==null){
            return 0;
        }

        editor.putInt("lastShowedPercent", lastShowedPercent);

        if(percent>=100 && !lastDayStreak.equalsIgnoreCase(LocalDate.now().toString())){
            currentStreak++;

            editor.putInt("currentStreak", currentStreak);
            lastDayStreak = LocalDate.now().toString();
            editor.putString("lastDayStreak", lastDayStreak);

            if(currentStreak>longestStreak){
                longestStreak = currentStreak;
                editor.putInt("longestStreak", longestStreak);
            }

            totalStreakDays++;
            editor.putInt("totalStreakDays", totalStreakDays);
        }

        editor.apply();

        return percent;
    }

    public int getLastShowedPercent(){
        return lastShowedPercent;
    }

    public int getLongestStreak(){
        return longestStreak;
    }

    public int getCurrentStreak(){
        return currentStreak;
    }

    public int getTotalStreakDays(){
        return totalStreakDays;
    }


    public void initialize(Context context){
        sharedPref = context.getSharedPreferences("DailyStreak", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        int savedDay = sharedPref.getInt("day", 0);

        LocalDate currentDay = LocalDate.now();
        int currentDayValue = currentDay.getDayOfMonth();
        if(savedDay != currentDayValue){
            editor.putInt("day", currentDayValue).apply();
            editor.putLong("timeToday", 0L);
            editor.putInt("lastShowedPercent", 0);
            timeToday = 0;

            Log.d("DailyStreak", "New Day!");
        }
        else{
            timeToday = sharedPref.getLong("timeToday", 0L);
            lastShowedPercent = sharedPref.getInt("lastShowedPercent", 0);

            Log.d("DailyStreak", "Load values!" + timeToday);
        }

        longestStreak = sharedPref.getInt("longestStreak", 0);
        totalStreakDays = sharedPref.getInt("totalStreakDays", 0);

        lastDayStreak = sharedPref.getString("lastDayStreak", "");
        if(lastDayStreak.equalsIgnoreCase(currentDay.minusDays(1).toString())
        || lastDayStreak.equalsIgnoreCase(currentDay.toString())){
            currentStreak = sharedPref.getInt("currentStreak", 0);
        }
        else{
            currentStreak = 0;
            editor.putInt("currentStreak", currentStreak).apply();
        }
    }

    public static DailyStreakController getInstance(){
        if(_instance == null){
            _instance = new DailyStreakController();
        }

        return _instance;
    }
}
