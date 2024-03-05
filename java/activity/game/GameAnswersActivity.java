package com.coresaken.multiplication.activity.game;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.GameController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.controller.adapter.UserAnswerAdapter;
import com.coresaken.multiplication.data.enums.GameType;

public class GameAnswersActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    UserAnswerAdapter userAnswerAdapter;
    LinearLayoutManager layoutManager;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_answers);

        GameController gameController = GameController.getInstance();

        TextView tv_correct, tv_incorrect, tv_total;
        tv_correct = findViewById(R.id.tv_correct);
        tv_incorrect = findViewById(R.id.tv_incorrect);
        tv_total = findViewById(R.id.tv_total);

        int correctPercent = gameController.statistic.getCorrectAnswerPercent();
        int incorrectPercent = 100 - correctPercent;

        tv_correct.setText(gameController.statistic.correctAnswer +" ("+correctPercent+"%)");
        tv_incorrect.setText(gameController.statistic.incorrectAnswer +" ("+incorrectPercent+"%)");

        recyclerView = findViewById(R.id.rv_container);
        if(gameController.gameType == GameType.CARD
                || gameController.gameType == GameType.MATCH
                || gameController.gameType == GameType.DRAW){
            userAnswerAdapter = new UserAnswerAdapter(gameController.answeredEquationsCard, this, gameController.gameType,true);
            tv_total.setText(String.valueOf(gameController.answeredEquationsCard.size()));
        }
        else{
            userAnswerAdapter = new UserAnswerAdapter(gameController.answeredEquations, this, gameController.gameType);
            tv_total.setText(String.valueOf(gameController.answeredEquations.size()));
        }
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(userAnswerAdapter);

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));
    }

    public void closePanel(View view){
        SoundController.getInstance().clickButton();

        finish();
    }
}