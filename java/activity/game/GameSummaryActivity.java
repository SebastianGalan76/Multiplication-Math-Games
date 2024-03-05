package com.coresaken.multiplication.activity.game;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.competition.Competition0Activity;
import com.coresaken.multiplication.activity.panel.DailyStreakActivity;
import com.coresaken.multiplication.controller.DailyStreakController;
import com.coresaken.multiplication.controller.GameController;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.User;
import com.coresaken.multiplication.data.enums.ModeType;

import java.util.Random;

public class GameSummaryActivity extends AppCompatActivity {

    Settings settings;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_summary);
        transparentStatusAndNavigation();

        SharedPreferences sharedPref = getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        User user = User.getInstance();

        GameController gameController = GameController.getInstance();
        settings = SettingsController.getInstance().settings;

        TextView tv_correct, tv_incorrect, tv_percent;
        tv_correct = findViewById(R.id.tv_correct);
        tv_incorrect = findViewById(R.id.tv_incorrect);
        tv_percent = findViewById(R.id.tv_percent);

        if(gameController.statistic!=null){
            tv_correct.setText(String.valueOf(gameController.statistic.correctAnswer));
            tv_incorrect.setText(String.valueOf(gameController.statistic.incorrectAnswer));
        }
        else{
            tv_correct.setText(String.valueOf(0));
            tv_incorrect.setText(String.valueOf(0));
        }


        TextView tv_correct_text, tv_incorrect_text;

        if(settings.modeType== ModeType.COMPETITION){
            tv_correct_text = findViewById(R.id.tv_correct_text);
            tv_correct_text.setText(R.string.points);

            tv_incorrect_text = findViewById(R.id.tv_incorrect_text);
            tv_incorrect_text.setText(R.string.record);

            if(user.competitionRecord[0]<gameController.statistic.correctAnswer){
                user.setNewRecord(0, gameController.statistic.correctAnswer);
                editor.putInt("competitionRecord_0", gameController.statistic.correctAnswer);
            }

            tv_incorrect.setText(String.valueOf(user.competitionRecord[0]));
        }

        int correctPercent = gameController.statistic.getCorrectAnswerPercent();

        Random random = new Random();

        TypedArray summaryArray = getResources().obtainTypedArray(R.array.summary_array);
        int randomIndex = random.nextInt(summaryArray.length());
        int animationResource = summaryArray.getResourceId(randomIndex, -1);

        if (animationResource != -1) {
            LottieAnimationView lottieIllustration = findViewById(R.id.lottie_illustration);
            lottieIllustration.setAnimation(animationResource);
        }

        summaryArray.recycle();

        LottieAnimationView[] stars = new LottieAnimationView[5];
        stars[0] = findViewById(R.id.lottie_star0);
        stars[1] = findViewById(R.id.lottie_star1);
        stars[2] = findViewById(R.id.lottie_star2);
        stars[3] = findViewById(R.id.lottie_star3);
        stars[4] = findViewById(R.id.lottie_star4);

        int starAmount = 0;
        if(correctPercent>95){
            starAmount = 5;
        }else if(correctPercent>80){
            starAmount = 4;
        }else if(correctPercent>60){
            starAmount = 3;
        }else if(correctPercent>40){
            starAmount = 2;
        }else if(correctPercent>20){
            starAmount = 1;
        }

        starAmount--;
        for(int i=0;i<=starAmount;i++){
            int delay = (i * 250) + 1000;
            final int starIndex = i;
            new CountDownTimer(delay, delay){

                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    stars[starIndex].playAnimation();
                }
            }.start();
        }

        int delay = (starAmount+1) * 250 + 1000;
        new CountDownTimer(delay, delay){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                tv_percent.setText(correctPercent+"%");
            }
        }.start();

        user.exp += gameController.statistic.getGainedExp();
        editor.putInt("exp", user.exp);
        editor.apply();

        user.increaseAdValue(this, true);
    }

    public void showAnswers(View view) {
        SoundController.getInstance().clickButton();
        startActivity(new Intent(GameSummaryActivity.this, GameAnswersActivity.class));
    }

    public void goToMainMenu(View view) {

        SoundController.getInstance().clickButton();
        finish();
    }

    public void startAgain(View view){
        SoundController.getInstance().clickButton();

        startActivity(new Intent(this, GameController.getInstance().gameType.getActivityClass()));
        finish();
    }


    private void transparentStatusAndNavigation() {
        //make full transparent statusBar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
    private void setWindowFlag(final int bits, boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}