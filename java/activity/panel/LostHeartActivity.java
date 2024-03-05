package com.coresaken.multiplication.activity.panel;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.GameController;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.Sound;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;

public class LostHeartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lost_heart);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {

            }
        });
    }

    public void showAd(View view){
        AdSystem.getInstance().showRewardedAd(this, new OnUserEarnedRewardListener() {
            @Override
            public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                addHearts();
            }
        });
    }

    public void addHearts(){
        if(GameController.getInstance().heartLimit!=null){
            GameController.getInstance().heartLimit.changeHeartAmount(3);
        }
        else if(GameController.getInstance().timeLimit!=null){
            GameController.getInstance().timeLimit.startTimer(30000);
        }

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public void finish(View view){
        SoundController.getInstance().clickButton();

        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_CANCELED, returnIntent);

        finish();
    }
}