package com.coresaken.multiplication;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import com.coresaken.multiplication.activity.MainMenuActivity;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.DailyStreakController;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.data.User;
import com.coresaken.multiplication.fragment.main_menu.SelectAgeFragment;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.games.PlayGamesSdk;

public class MainActivity extends AppCompatActivity {
    private final int USER_SET_DATE_OF_BIRTH = -10000;
    User user;
    SelectAgeFragment selectAgeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlayGamesSdk.initialize(this);
        DailyStreakController.getInstance().initialize(this);

        initializeSettings();
        initializeUser();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }

    private void initializeUser(){
        SharedPreferences sharedPref = getSharedPreferences("User", Context.MODE_PRIVATE);

        user = User.getInstance();
        int birthValue = sharedPref.getInt("birth_value", -1);

        if(birthValue < 0 && birthValue!=USER_SET_DATE_OF_BIRTH){
            selectAgeFragment = SelectAgeFragment.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.cl_container, selectAgeFragment).commit();
        }
        else{
            if(birthValue!=USER_SET_DATE_OF_BIRTH){
                sharedPref.edit().putInt("birth_value", birthValue-1).apply();
            }

            user.setAge(sharedPref);
            initializeAds();
        }
    }
    private void initializeSettings(){
        PlayerSettings playerSettings = PlayerSettings.getInstance();
        SharedPreferences sharedPrefSettings = getSharedPreferences("PlayerSettings", Context.MODE_PRIVATE);

        //Reminder
        playerSettings.reminder = sharedPrefSettings.getBoolean("reminder", false);
        playerSettings.reminderTime[0] = sharedPrefSettings.getInt("reminderHour", 18);
        playerSettings.reminderTime[1] = sharedPrefSettings.getInt("reminderMinute", 30);

        playerSettings.sounds = sharedPrefSettings.getBoolean("sounds", true);
        playerSettings.language = sharedPrefSettings.getInt("language", -1);
    }

    private void initializeAds(){
        if(user.currentAge<=13){
            AdSystem.getInstance().initializeConfigurationForChildren();
        }

        AdSystem.getInstance().initializeAds(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                startActivity(new Intent(MainActivity.this, MainMenuActivity.class));
                finish();
            }
        });
    }

    public void skip(View view){
        if(selectAgeFragment!=null){
            User user = User.getInstance();
            user.day = -1;
            user.month = -1;
            user.year = -1;

            SharedPreferences sharedPref = getSharedPreferences("User", Context.MODE_PRIVATE);
            user.setAge(sharedPref);

            sharedPref.edit().putInt("birth_value", 10).apply();
            selectAgeFragment.finishOperation();

            initializeAds();
        }
    }

    public void confirm(View view){
        if(selectAgeFragment!=null){
            User user = User.getInstance();
            user.day = selectAgeFragment.selectedDay;
            user.month = selectAgeFragment.selectedMonth;
            user.year = selectAgeFragment.selectedYear;

            SharedPreferences sharedPref = getSharedPreferences("User", Context.MODE_PRIVATE);
            sharedPref.edit().putInt("day_birth", user.day).apply();
            sharedPref.edit().putInt("month_birth", user.month).apply();
            sharedPref.edit().putInt("year_birth", user.year).apply();
            sharedPref.edit().putInt("birth_value", USER_SET_DATE_OF_BIRTH).apply();
            selectAgeFragment.finishOperation();

            user.setAge(sharedPref);

            initializeAds();
        }
    }
}