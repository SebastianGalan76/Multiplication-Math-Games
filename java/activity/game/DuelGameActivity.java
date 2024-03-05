package com.coresaken.multiplication.activity.game;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.airbnb.lottie.LottieAnimationView;
import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.AnswerController;
import com.coresaken.multiplication.controller.DuelGameController;
import com.coresaken.multiplication.controller.EquationController;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.DuelPlayer;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.Sound;
import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.util.Utils;

import java.util.HashMap;

public class DuelGameActivity extends AppCompatActivity {

    DuelPlayer[] players;

    DuelGameController duelGameController;
    EquationController equationController;
    AnswerController answerController;
    Settings settings;

    ConstraintLayout cl_player0_equation;
    ConstraintLayout cl_player0_buttons;
    Button[] player0_buttons;
    UnknownEquation[] equations;
    HashMap<Integer, int[]> answers;


    ConstraintLayout cl_player1_equation;
    ConstraintLayout cl_player1_buttons;
    Button[] player1_buttons;
    UnknownEquation[] reverseEquation;
    HashMap<Integer, int[]> reverseAnswers;

    ProgressBar pb_progress, pb_progress_light;
    LottieAnimationView progressBarBurst;
    ConstraintLayout cl_progress;
    ConstraintLayout cl_middle;
    int totalAnswers;
    int currentAnswer;

    Sound sound;

    Dialog exitDialog;
    CountDownTimer startTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        /*getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );*/

        setContentView(R.layout.activity_duel_game);

        if(PlayerSettings.getInstance().sounds){
            sound = new Sound(this, new int[] {R.raw.sound_stop_drawing, R.raw.sound_start_drawing});
        }

        cl_middle = findViewById(R.id.cl_middle);

        cl_progress = findViewById(R.id.cl_progressBar);
        progressBarBurst = findViewById(R.id.lottie_progressBar);

        pb_progress = findViewById(R.id.pb_progress);
        pb_progress_light = findViewById(R.id.pb_progress_light);

        settings = SettingsController.getInstance().settings;
        equationController = new EquationController();
        answerController = new AnswerController();

        duelGameController = DuelGameController.getInstance();

        //Load equations
        currentAnswer = 0;
        totalAnswers = 30;
        equations = new UnknownEquation[15];
        answers = new HashMap<>();

        for(int i=0;i<equations.length;i++){
            Equation equation = equationController.getEquation();
            equations[i] = new UnknownEquation(equation);
            equationController.setUnknownElement(equations[i]);

            answers.put(i, answerController.getAnswers(equations[i].equation, 4, equations[i].getUnknownElementIndex(), true));
        }

        reverseEquation = new UnknownEquation[equations.length];
        reverseAnswers = new HashMap<>();
        int index = 0;
        for(int i=equations.length-1;i>=0;i--){
            reverseEquation[index] = equations[i].clone();

            reverseAnswers.put(index, answers.get(i));
            index++;
        }

        //Load Buttons and ConstraintLayouts
        player0_buttons = new Button[4];
        player1_buttons = new Button[4];

        player0_buttons[0] = findViewById(R.id.btn_player0_answer_0);
        player0_buttons[1] = findViewById(R.id.btn_player0_answer_1);
        player0_buttons[2] = findViewById(R.id.btn_player0_answer_2);
        player0_buttons[3] = findViewById(R.id.btn_player0_answer_3);

        player1_buttons[0] = findViewById(R.id.btn_player1_answer_0);
        player1_buttons[1] = findViewById(R.id.btn_player1_answer_1);
        player1_buttons[2] = findViewById(R.id.btn_player1_answer_2);
        player1_buttons[3] = findViewById(R.id.btn_player1_answer_3);

        cl_player0_equation = findViewById(R.id.cl_player0_equation);
        cl_player1_equation = findViewById(R.id.cl_player1_equation);

        cl_player0_buttons = findViewById(R.id.cl_player0_buttons);
        cl_player1_buttons = findViewById(R.id.cl_player1_buttons);

        startTimer = new CountDownTimer(3000, 3000){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                //Create players
                cl_player0_buttons.setVisibility(View.VISIBLE);
                cl_player1_buttons.setVisibility(View.VISIBLE);

                cl_player0_equation.removeAllViews();
                cl_player1_equation.removeAllViews();

                cl_middle.setVisibility(View.VISIBLE);

                startGame();
            }
        }.start();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                tryCloseGame(null);
            }
        });

        players = new DuelPlayer[2];
        players[0] = new DuelPlayer(this, "Player 0", equations, answers, player0_buttons, cl_player0_equation, cl_player0_buttons, sound);
        players[1] = new DuelPlayer(this, "Player 1", reverseEquation, reverseAnswers, player1_buttons, cl_player1_equation, cl_player1_buttons, sound);

        duelGameController.players = players;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(startTimer!=null){
            startTimer.cancel();
        }

        if(exitDialog!=null){
            exitDialog.dismiss();
        }
    }

    private void startGame(){
        for(DuelPlayer player:players){
            player.startGame();
        }
    }

    private void changeProgressBar(){
        int progress = (int)((float)currentAnswer / (float)totalAnswers * 100);

        if(progress<8){
            progress = 8;
        }

        pb_progress.setProgress(progress);
        pb_progress_light.setProgress(progress-5);

        if(pb_progress.getProgress()<=progress){
            int left = cl_progress.getLeft() + Utils.convertDPToPixels(getResources(), 16);
            int right = cl_progress.getRight() - Utils.convertDPToPixels(getResources(), 16);

            int distance = right - left;
            distance = distance * progress/100;

            // Utwórz obiekt LayoutParams
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) progressBarBurst.getLayoutParams();
            params.leftMargin = distance - Utils.convertDPToPixels(getResources(), 14);
            progressBarBurst.setLayoutParams(params);
            progressBarBurst.playAnimation();
        }
    }

    public void answer_player0(View view){
        int index = 0;

        if(view.getId()==R.id.btn_player0_answer_1){
            index = 1;
        } else if(view.getId()==R.id.btn_player0_answer_2){
            index = 2;
        } else if(view.getId()==R.id.btn_player0_answer_3){
            index = 3;
        }

        currentAnswer++;
        changeProgressBar();

        players[0].answer(index);
    }
    public void answer_player1(View view){
        int index = 0;

        if(view.getId()==R.id.btn_player1_answer_1){
            index = 1;
        } else if(view.getId()==R.id.btn_player1_answer_2){
            index = 2;
        } else if(view.getId()==R.id.btn_player1_answer_3){
            index = 3;
        }

        currentAnswer++;
        changeProgressBar();

        players[1].answer(index);
    }

    public void checkFinish(){
        if(!(players[0].finished && players[1].finished)){
            return;
        }

        finishGame();
    }

    private void finishGame(){
        startActivity(new Intent(DuelGameActivity.this, DuelGameSummaryActivity.class));
        finish();
    }

    public void tryCloseGame(View view){
        exitDialog = new Dialog(this, R.style.DimOverlay);

        exitDialog.setContentView(R.layout.dialog_exit_game);
        Window window = exitDialog.getWindow();
        window.setBackgroundDrawableResource(R.color.dialog_bg);

        SoundController.getInstance().clickButton();

        Button btn_yes = exitDialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();

                exitDialog.dismiss();
                finishGame();
            }
        });

        Button btn_no = exitDialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();

                exitDialog.cancel();
                exitDialog.dismiss();
            }
        });

        ImageButton btn_close = exitDialog.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();

                exitDialog.cancel();
                exitDialog.dismiss();
            }
        });

        exitDialog.show();
    }
}