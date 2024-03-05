package com.coresaken.multiplication.activity.game;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

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

public class Game1Activity extends Game {
    UnknownEquation currentUnknownEquation;
    AnsweredEquation currentAnsweredEquation;
    List<AnsweredEquation> answeredEquations;

    int answerAmount = 4;
    int[] answers;
    boolean firstAnswer;

    CountDownTimer answerDelayTimer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game1);

        sqLiteHelper = new SQLiteHelper(this);
        answeredEquations = new ArrayList<>();
        answers = new int[answerAmount];

        initializeTimers();
        initializeButtons();
        initializeFragments();

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));
    }

    protected void startGame(){
        super.startGame(GameType.GAME1);

        loadNewEquation();
    }

    @Override
    protected void startLesson(){
        equationController.loadLessonEquations(settings.equationLimit, settings.currentLesson);
        incorrectAnsweredEquations = new ArrayList<>();
    }

    @Override
    protected void startCorrection(){
        statistic.equationLimit = 30;
        equationController.loadCorrectionEquation(statistic.equationLimit);
        incorrectAnsweredEquations = new ArrayList<>();
    }

    @Override
    protected void lostGame(){
        if(settings.modeType==ModeType.LESSON){
            super.lostGame();
        }
        else{
            finishGameTimer.start();
        }
    }

    @Override
    protected void finishGame(){
        super.finishGame();

        controller.answeredEquations = answeredEquations;
        controller.statistic.equationLimit = controller.statistic.currentEquation;
    }

    @Override
    protected void completeLesson() {
        Lesson currentLesson = settings.currentLesson;

        int progress = statistic.getCorrectAnswerPercent();
        progress *= 0.7;

        if(currentLesson!=null){
            currentLesson.changeExercisesProgress(this, settings.exerciseIndex, progress);
        }
    }

    @Override
    protected void resumeGame(){
        super.resumeGame();

        isFinished = false;
        loadNewEquation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.stopAllTimers();
    }

    private void loadNewEquation(){
        if(isFinished){
            return;
        }

        if(!checkEquationLimit()){
            completeGame();
            return;
        }

        Equation equation = equationController.getEquation();
        if(equation==null){
            if(incorrectAnsweredEquations !=null && incorrectAnsweredEquations.size()>0){
                currentUnknownEquation = incorrectAnsweredEquations.remove(0);
            }
            else{
                completeGame();
                return;
            }
        }
        else{
            currentUnknownEquation = new UnknownEquation(equation);
            equationController.setUnknownElement(currentUnknownEquation);
        }
        statistic.currentEquation++;

        loadAnswers();
        equationFragment.setEquation(currentUnknownEquation);
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
            upPanelFragment.changeProgressBar(statistic);

            correctAnswer(buttonIndex);
        }
        else{
            incorrectAnswer(buttonIndex);
        }

        equationFragment.answer(selectedValue, isCorrect);
        currentAnsweredEquation.addAnswer(selectedValue, isCorrect);

        firstAnswer = false;
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    private void correctAnswer(int buttonIndex){
        super.correctAnswer(firstAnswer, true);

        if(firstAnswer){
            updateEquationPoints(5);
        }

        changeButtonsEnable(false);
        answerButtons[buttonIndex].setBackground(getDrawable(R.drawable.button_correct1));
        answerButtons[buttonIndex].setTextColor(getColor(R.color.correct_text));
        answerButtons[buttonIndex].setPadding(0, 0, 0 ,0);
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void incorrectAnswer(int buttonIndex){
        super.incorrectAnswer(firstAnswer, true);

        if(settings.modeType == ModeType.LESSON){
            if(firstAnswer){
                super.addIncorrectAnsweredEquation(currentUnknownEquation);
                statistic.equationLimit++;
            }
        }

        updateEquationPoints(-12);
        sqLiteHelper.updateIncorrectEquationPoints(currentUnknownEquation.equation, -10);


        answerButtons[buttonIndex].setEnabled(false);
        answerButtons[buttonIndex].setBackground(getDrawable(R.drawable.button_incorrect1));
        answerButtons[buttonIndex].setTextColor(getColor(R.color.incorrect_text));
        answerButtons[buttonIndex].setPadding(0, 38, 0 ,0);
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

        changeButtonsEnable(true);
    }
    @Override
    protected Context getContext() {
        return this;
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
        if(settings.limitType==null){
            settings.limitType = LimitType.EQUATION;
        }
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

    public void tryCloseGame(View view){
        super.tryCloseGame(view);
    }

    @Override
    public void onBackPressed() {
        tryCloseGame(null);
    }
}