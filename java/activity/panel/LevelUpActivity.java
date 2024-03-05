package com.coresaken.multiplication.activity.panel;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.GameController;
import com.coresaken.multiplication.controller.PanelController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.GameStatistic;
import com.coresaken.multiplication.data.Panel;
import com.coresaken.multiplication.data.User;

public class LevelUpActivity extends Panel {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_up);

        TextView tv_level = findViewById(R.id.tv_level);
        GameStatistic statistic = GameController.getInstance().statistic;

        if(statistic==null){
            closePanel(null);
        }
        else{
            tv_level.setText(String.valueOf(User.getInstance().getLevelAfterUpgrade(statistic.getGainedExp())));
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closePanel(null);
            }
        });
    }

    public void closePanel(View view){
        if(view!=null){
            SoundController.getInstance().clickButton();
        }

        startActivity(new Intent(this, PanelController.getInstance().getNextPanel()));
        finish();
    }
}