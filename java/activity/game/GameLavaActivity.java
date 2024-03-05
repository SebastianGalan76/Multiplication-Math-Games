package com.coresaken.multiplication.activity.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.data.AnsweredEquation;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Game;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.LimitType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.fragment.game.EquationFragment;
import com.coresaken.multiplication.fragment.game.GameUpPanelFragment;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class GameLavaActivity extends Game {

    UnknownEquation currentUnknownEquation;
    AnsweredEquation currentAnsweredEquation;
    List<AnsweredEquation> answeredEquations;

    int answerAmount = 4;
    int[] answers;
    boolean firstAnswer;

    CountDownTimer answerDelayTimer;

    CountDownTimer lavaTimer;
    ProgressBar progressBarLava;

    long timeLeft;
    final long maxTime = 30000;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_lava);

        sqLiteHelper = new SQLiteHelper(this);
        answeredEquations = new ArrayList<>();
        answers = new int[answerAmount];

        progressBarLava = findViewById(R.id.progressBar);
        progressBarLava.setMax((int)maxTime);
        changeTimerTime(maxTime);

        settings.limitType = LimitType.DEFAULT;

        initializeButtons();
        initializeTimers();
        initializeFragments();

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));
    }

    private void changeTimerTime(long value){
        timeLeft+=value;

        if(timeLeft>maxTime){
            timeLeft = maxTime;
        }

        if(lavaTimer!=null){
            lavaTimer.cancel();
        }

        lavaTimer = new CountDownTimer(timeLeft, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                timeLeft = millisUntilFinished;
                progressBarLava.setProgress((int)maxTime - (int)millisUntilFinished);
            }

            @Override
            public void onFinish() {
                completeGame();
            }
        }.start();
    }

    protected void startGame(){
        super.startGame(GameType.LAVA);

        loadNewEquation();
    }

    @Override
    public void lostGame(){
        if(settings.modeType==ModeType.LESSON){
            super.lostGame();
        }
        else{
            finishGameTimer.start();
        }
    }

    protected void finishGame(){
        super.finishGame();

        lavaTimer.cancel();
        controller.answeredEquations = answeredEquations;
        controller.statistic.equationLimit = controller.statistic.currentEquation;
    }

    @Override
    protected void completeLesson() {
        Lesson currentLesson = settings.currentLesson;

        int progress = (int)((float)statistic.correctAnswer/(float)30 * 100f);

        if(currentLesson!=null){
            currentLesson.changeExercisesProgress(GameLavaActivity.this, settings.exerciseIndex, progress);
        }
    }

    private void loadNewEquation(){
        if(isFinished){
            return;
        }

        Equation equation = equationController.getEquation();

        //Jeśli equation == 0, to znaczy, że musi to być lekcja lub poprawka i pobieramy losowe rówanie
        if(settings.modeType==ModeType.LESSON){
            equation = equationController.getRandomLessonEquation(settings.currentLesson);
        }
        else if(settings.modeType==ModeType.CORRECTION){
            equation = equationController.getRandomCorrectionEquation();
        }

        currentUnknownEquation = new UnknownEquation(equation);
        equationController.setUnknownElement(currentUnknownEquation);

        statistic.currentEquation++;

        loadAnswers();
        changeButtonsEnable(true);
        equationFragment.setEquation(currentUnknownEquation);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadAnswers(){
        firstAnswer = true;
        answers = answerController.getAnswers(currentUnknownEquation.equation, answerAmount, currentUnknownEquation.getUnknownElementIndex(), true);

        for(int i=0;i<answerAmount;i++){
            answerButtons[i].setBackground(getDrawable(R.drawable.button_normal1));
            answerButtons[i].setTextColor(getColor(R.color.normal_text));
            answerButtons[i].setPadding(0, 0, 0 ,0);
            answerButtons[i].setText(String.valueOf(this.answers[i]));
        }
    }

    @Override
    protected void startLesson(){

    }

    @Override
    protected void startCorrection(){
        equationController.loadCorrectionEquation(30);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeButtons(){
        answerButtons = new Button[answerAmount];

        answerButtons[0] = findViewById(R.id.btn_answer_0);
        answerButtons[1] = findViewById(R.id.btn_answer_1);
        answerButtons[2] = findViewById(R.id.btn_answer_2);
        answerButtons[3] = findViewById(R.id.btn_answer_3);

        for(int i=0;i<answerButtons.length;i++){
            final int index = i;
            int padding = Utils.convertDPToPixels(getResources(), 7);
            answerButtons[index].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                        answerButtons[index].setPadding(0, padding, 0 ,0);
                        return false;
                    }
                    if(motionEvent.getAction()==MotionEvent.ACTION_UP || motionEvent.getAction()==MotionEvent.ACTION_CANCEL){
                        return false;
                    }

                    return true;
                }
            });
        }
    }
    private void initializeFragments(){
        //Initialize fragments
        upPanelFragment = GameUpPanelFragment.newInstance();
        upPanelFragment.initialize(settings.limitType);
        getSupportFragmentManager().beginTransaction().add(R.id.cl_panel_up, upPanelFragment, null).commit();

        //EquationFragment
        equationFragment = EquationFragment.newInstance();
        equationFragment.setListener(() -> startGame());
        getSupportFragmentManager().beginTransaction().add(R.id.cl_equation_container, equationFragment, null).commit();
    }
    private void initializeTimers(){
        answerDelayTimer = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                loadNewEquation();
            }
        };

        timers.add(answerDelayTimer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        lavaTimer.cancel();
        super.stopAllTimers();
    }

    @Override
    protected Context getContext() {
        return this;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void answer(View view){
        int buttonIndex = 0;

        if(view.getId()==R.id.btn_answer_1){
            buttonIndex = 1;
        }
        else if(view.getId()==R.id.btn_answer_2){
            buttonIndex = 2;
        }
        else if(view.getId()==R.id.btn_answer_3){
            buttonIndex = 3;
        }

        int selectedValue = answers[buttonIndex];
        boolean isCorrect = currentUnknownEquation.getUnknownElementValue() == selectedValue;

        //Jeśli obecna odpowiedź jest pierwszym wyborem
        if(firstAnswer){
            currentAnsweredEquation = new AnsweredEquation(currentUnknownEquation);
            answeredEquations.add(currentAnsweredEquation);

            statistic.totalAnswer++;
        }

        if(settings.modeType == ModeType.CORRECTION){
            if(firstAnswer){
                if(isCorrect){
                    sqLiteHelper.updateIncorrectEquationPoints(currentUnknownEquation.equation, 4);
                }
            }
        }

        if(isCorrect){
            answerDelayTimer.start();

            correctAnswer(buttonIndex);
        }
        else{
            incorrectAnswer(buttonIndex);
        }

        equationFragment.answer(selectedValue, isCorrect);
        upPanelFragment.changeProgressBar(statistic);
        currentAnsweredEquation.addAnswer(selectedValue, isCorrect);

        firstAnswer = false;
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void correctAnswer(int buttonIndex){
        super.correctAnswer(firstAnswer, true);

        if(firstAnswer){
            updateEquationPoints(5);
        }

        changeButtonsEnable(false);
        answerButtons[buttonIndex].setBackground(getDrawable(R.drawable.button_correct1));
        answerButtons[buttonIndex].setTextColor(getColor(R.color.correct_text));
        answerButtons[buttonIndex].setPadding(0, 0, 0 ,0);

        changeTimerTime(1000);
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void incorrectAnswer(int buttonIndex){
        super.incorrectAnswer(firstAnswer, true);

        updateEquationPoints(-10);

        answerButtons[buttonIndex].setEnabled(false);
        answerButtons[buttonIndex].setBackground(getDrawable(R.drawable.button_incorrect1));
        answerButtons[buttonIndex].setTextColor(getColor(R.color.incorrect_text));
        answerButtons[buttonIndex].setPadding(0, 38, 0 ,0);

        sqLiteHelper.updateIncorrectEquationPoints(currentUnknownEquation.equation, -10);

        changeTimerTime(-3000);
    }

    private void updateEquationPoints(int points){
        sqLiteHelper.updatePoints(currentUnknownEquation.equation.getId(), points);
        int newPoints = currentUnknownEquation.equation.getPoints() + points;

        long delay = equationFragment.updateStarAmount(newPoints);
        if(delay != 0){
            answerDelayTimer.cancel();

            new CountDownTimer(delay, delay){

                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    answerDelayTimer.start();
                }
            }.start();
        }
    }

    public void tryCloseGame(View view){
        super.tryCloseGame(view);
    }
}