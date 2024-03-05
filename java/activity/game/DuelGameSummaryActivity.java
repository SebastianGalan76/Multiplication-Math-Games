package com.coresaken.multiplication.activity.game;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.DuelGameController;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.DuelPlayer;

public class DuelGameSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duel_game_summary);

        DuelPlayer[] players = DuelGameController.getInstance().players;

        int winnerIndex;
        if(players[0].correctAnswer > players[1].correctAnswer){
            winnerIndex = 0;
        }
        else if(players[1].correctAnswer > players[0].correctAnswer){
            winnerIndex = 1;
        }
        else{
            winnerIndex = -1;
        }

        TextView tv_player0_name = findViewById(R.id.tv_player0_name);
        TextView tv_player1_name = findViewById(R.id.tv_player1_name);
        tv_player0_name.setText(players[0].name);
        tv_player1_name.setText(players[1].name);

        TextView tv_player0_points = findViewById(R.id.tv_player0_points);
        TextView tv_player1_points = findViewById(R.id.tv_player1_points);
        tv_player0_points.setText(String.valueOf(players[0].correctAnswer));
        tv_player1_points.setText(String.valueOf(players[1].correctAnswer));

        if(winnerIndex!=-1){
            ConstraintLayout cl_winner = findViewById(R.id.cl_winner);
            cl_winner.setVisibility(View.VISIBLE);

            TextView tv_winner_name = findViewById(R.id.tv_winner_name);
            tv_winner_name.setText(players[winnerIndex].name);

            ConstraintLayout cl_draw = findViewById(R.id.cl_draw);
            cl_draw.setVisibility(View.GONE);
        }
        else{
            ConstraintLayout cl_winner = findViewById(R.id.cl_winner);
            cl_winner.setVisibility(View.GONE);

            ConstraintLayout cl_draw = findViewById(R.id.cl_draw);
            cl_draw.setVisibility(View.VISIBLE);
        }

        transparentStatusAndNavigation();
    }

    public void showAnswers(View view) {
        SoundController.getInstance().clickButton();

        startActivity(new Intent(DuelGameSummaryActivity.this, DuelGameAnswersActivity.class));
    }

    public void goToMainMenu(View view) {

        SoundController.getInstance().clickButton();

        finish();
    }

    public void startAgain(View view){
        SoundController.getInstance().clickButton();

        startActivity(new Intent(DuelGameSummaryActivity.this, DuelGameActivity.class));
        finish();
    }


    private void transparentStatusAndNavigation() {
        //make full transparent statusBar
        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, true);
        }
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, false);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
        }
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