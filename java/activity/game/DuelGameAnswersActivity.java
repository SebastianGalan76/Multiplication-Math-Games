package com.coresaken.multiplication.activity.game;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.DuelGameController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.DuelPlayer;
import com.coresaken.multiplication.controller.adapter.UserAnswerAdapter;
import com.coresaken.multiplication.data.enums.GameType;

public class DuelGameAnswersActivity extends AppCompatActivity {

    DuelGameController gameController;

    DuelPlayer[] players;
    TextView tv_correct, tv_incorrect, tv_total;
    Button[] playerButtons;

    RecyclerView recyclerView;
    UserAnswerAdapter userAnswerAdapter;
    LinearLayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duel_game_answers);

        players = DuelGameController.getInstance().players;
        gameController = DuelGameController.getInstance();

        tv_correct = findViewById(R.id.tv_correct);
        tv_incorrect = findViewById(R.id.tv_incorrect);
        tv_total = findViewById(R.id.tv_total);
        recyclerView = findViewById(R.id.rv_container);

        playerButtons = new Button[2];
        playerButtons[0] = findViewById(R.id.btn_player_0);
        playerButtons[1] = findViewById(R.id.btn_player_1);

        playerButtons[0].setText(players[0].name);
        playerButtons[1].setText(players[1].name);

        changePlayer(0);
    }

    public void changePlayer(View view){
        SoundController.getInstance().clickButton();

        if(view.getId() == R.id.btn_player_1){
            playerButtons[1].setEnabled(false);
            playerButtons[1].setTextColor(getColor(R.color.white));

            playerButtons[0].setEnabled(true);
            playerButtons[0].setTextColor(getColor(R.color.normal_text));

            changePlayer(1);
        }
        else{
            playerButtons[0].setEnabled(false);
            playerButtons[0].setTextColor(getColor(R.color.white));

            playerButtons[1].setEnabled(true);
            playerButtons[1].setTextColor(getColor(R.color.normal_text));

            changePlayer(0);
        }
    }

    private void changePlayer(int playerIndex){
        DuelPlayer player = gameController.players[playerIndex];

        recyclerView.removeAllViews();

        userAnswerAdapter = new UserAnswerAdapter(player.answeredEquations, this, GameType.DUEL);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(userAnswerAdapter);

        int correctPercent = (int)(((float)player.correctAnswer/15f) * 100);
        int incorrectPercent = (int)(((float)player.incorrectAnswer/15f) * 100);

        tv_correct.setText(player.correctAnswer +" ("+correctPercent+"%)");
        tv_incorrect.setText(player.incorrectAnswer +" ("+incorrectPercent+"%)");
        tv_total.setText(String.valueOf(player.answeredEquations.size()));
    }

    public void closePanel(View view){
        SoundController.getInstance().clickButton();

        finish();
    }
}