package com.coresaken.multiplication.activity.panel;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.airbnb.lottie.LottieAnimationView;
import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.GameController;
import com.coresaken.multiplication.controller.PanelController;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.Panel;

import java.util.Timer;
import java.util.TimerTask;

public class LessonProgressActivity extends Panel {

    ProgressBar pb_progress, pb_progress_light;

    int counter;
    Timer t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_progress);

        pb_progress = findViewById(R.id.pb_progress);
        pb_progress_light = findViewById(R.id.pb_progress_light);

        final int startProgress = GameController.getInstance().lessonProgressStart;
        final int endProgress = GameController.getInstance().lessonProgressEnd;

        LottieAnimationView confetti = findViewById(R.id.lottie_confetti);

        pb_progress.setProgress(startProgress);
        pb_progress_light.setProgress(startProgress);

        if(endProgress-startProgress>0){
            t = new Timer();
            counter = startProgress;
            if(counter<8){
                counter = 8;
            }
            TimerTask tt = new TimerTask() {
                @Override
                public void run() {
                    counter++;

                    pb_progress.setProgress(counter);
                    pb_progress_light.setProgress(counter - 5);

                    if(counter>=endProgress){
                        t.cancel();
                    }

                    if(counter>=100){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                confetti.playAnimation();
                            }
                        });

                        t.cancel();
                    }
                }
            };

            t.schedule(tt, 1000, 10);
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closePanel(null);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(t!=null){
            t.cancel();
        }
    }

    public void closePanel(View view){
        SoundController.getInstance().clickButton();

        startActivity(new Intent(this, PanelController.getInstance().getNextPanel()));
        finish();
    }
}