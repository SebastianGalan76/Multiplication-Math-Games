package com.coresaken.multiplication.activity.game;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.panel.LostHeartActivity;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.data.AnsweredEquation;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Game;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.fragment.game.EquationFragment;
import com.coresaken.multiplication.fragment.game.GameUpPanelFragment;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class GameInputActivity extends Game {
    UnknownEquation currentUnknownEquation;
    AnsweredEquation currentAnsweredEquation;
    List<AnsweredEquation> answeredEquations;

    View[] buttons;
    StringBuilder currentValue;

    CountDownTimer answerDelayTimer;
    CountDownTimer checkAnswerTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_input);

        sqLiteHelper = new SQLiteHelper(this);
        answeredEquations = new ArrayList<>();

        ConstraintLayout cl_buttons = findViewById(R.id.cl_buttons);
        int buttonAmount = cl_buttons.getChildCount();
        buttons = new View[buttonAmount];
        for(int i=0;i<buttonAmount;i++){
            buttons[i] = cl_buttons.getChildAt(i);
        }

        initializeButtons();
        initializeFragments();

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));

        if(sound!=null){
            sound.addSound(this, R.raw.sound_start_drawing);
            sound.addSound(this, R.raw.sound_stop_drawing);
        }
    }

    protected void startGame(){
        super.startGame(GameType.INPUT);

        loadNewEquation();
    }

    @Override
    protected void resumeGame(){
        super.resumeGame();

        if(isFinished){
            isFinished = false;

            loadNewEquation();
        }
    }

    @Override
    protected void startLesson(){
        equationController.loadLessonEquations(settings.equationLimit, settings.currentLesson);
        incorrectAnsweredEquations = new ArrayList<>();
    }

    @Override
    protected void startCorrection(){
        equationController.loadCorrectionEquation(30);
        incorrectAnsweredEquations = new ArrayList<>();
    }

    @Override
    protected void lostGame(){
        isFinished = true;

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
        progress *= 0.5;

        if(currentLesson!=null){
            currentLesson.changeExercisesProgress(GameInputActivity.this, settings.exerciseIndex, progress);
        }
    }

    @Override
    protected void changeButtonsEnable(boolean enable){
        for(View button : buttons){
            button.setEnabled(enable);
        }
    }

    @Override
    protected Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(answerDelayTimer!=null){
            answerDelayTimer.cancel();
        }

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
        }else{
            currentUnknownEquation = new UnknownEquation(equation);
            equationController.setUnknownElement(currentUnknownEquation);
        }

        statistic.currentEquation++;

        currentValue = new StringBuilder();
        currentValue.append("?");

        currentAnsweredEquation = new AnsweredEquation(currentUnknownEquation);
        answeredEquations.add(currentAnsweredEquation);

        changeButtonsEnable(true);
        equationFragment.setEquation(currentUnknownEquation);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void answer(View view){
        if(currentValue.length()==1 && currentValue.toString().equals("?")){
            currentValue.deleteCharAt(0);
        } else if(currentValue.length()==1 && currentValue.toString().equals("0")){
            currentValue.deleteCharAt(0);
        }

        if(view.getId()==R.id.btn_input_0){
            currentValue.append("0");
        }else if(view.getId()==R.id.btn_input_1){
            currentValue.append("1");
        }else if(view.getId()==R.id.btn_input_2){
            currentValue.append("2");
        }else if(view.getId()==R.id.btn_input_3){
            currentValue.append("3");
        }else if(view.getId()==R.id.btn_input_4){
            currentValue.append("4");
        }else if(view.getId()==R.id.btn_input_5){
            currentValue.append("5");
        }else if(view.getId()==R.id.btn_input_6){
            currentValue.append("6");
        }else if(view.getId()==R.id.btn_input_7){
            currentValue.append("7");
        }else if(view.getId()==R.id.btn_input_8){
            currentValue.append("8");
        }else if(view.getId()==R.id.btn_input_9){
            currentValue.append("9");
        } else if(view.getId()==R.id.btn_input_remove){
            if(currentValue.length()>=1){
                currentValue.deleteCharAt(currentValue.length()-1);
            }
        }

        if(currentValue.length()==0){
            currentValue.append("?");
        }

        equationFragment.setUnknownElementFieldValue(currentValue.toString());
        boolean questionMark = currentValue.length()==1 && currentValue.toString().equals("?");

        if(currentValue.length()==String.valueOf(currentUnknownEquation.getUnknownElementValue()).length() && !questionMark){
            changeButtonsEnable(false);

            if(checkAnswerTimer!=null){
                checkAnswerTimer.cancel();
            }

            checkAnswerTimer = new CountDownTimer(500, 500){

                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    long delay = 500;
                    int currentValueInt = Integer.parseInt(currentValue.toString());

                    boolean isCorrect = currentValueInt == currentUnknownEquation.getUnknownElementValue();

                    statistic.totalAnswer++;

                    if(!isCorrect){
                        delay = 1000;
                    }

                    answerDelayTimer = new CountDownTimer(delay, delay) {
                        @Override
                        public void onTick(long l) {

                        }

                        @Override
                        public void onFinish() {
                            loadNewEquation();
                        }
                    }.start();

                    if(settings.modeType == ModeType.CORRECTION){
                        if(isCorrect){
                            sqLiteHelper.updateIncorrectEquationPoints(currentUnknownEquation.equation, 4);
                        }
                    }

                    if(isCorrect){
                        correctAnswer();
                    }
                    else{
                        incorrectAnswer();
                    }

                    equationFragment.answer(currentValueInt, isCorrect);
                    upPanelFragment.changeProgressBar(statistic);
                    currentAnsweredEquation.addAnswer(currentValueInt, isCorrect);
                }
            }.start();
        }
    }
    protected void correctAnswer() {
        super.correctAnswer(true, true);

        updateEquationPoints(5);
    }

    protected void incorrectAnswer() {
        super.incorrectAnswer(true, true);

        if(settings.modeType== ModeType.LESSON){
            super.addIncorrectAnsweredEquation(currentUnknownEquation);
            statistic.equationLimit++;
        }

        sqLiteHelper.updateIncorrectEquationPoints(currentUnknownEquation.equation, -10);

        updateEquationPoints(-10);
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

    @SuppressLint("ClickableViewAccessibility")
    private void initializeButtons(){
        int padding = Utils.convertDPToPixels(getResources(), 7);

        for(int i=0;i<buttons.length;i++){
            final int index = i;
            buttons[index].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                        if(sound!=null){
                            sound.play(R.raw.sound_start_drawing);
                        }

                        buttons[index].setPadding(0, padding, 0 ,0);
                        return false;
                    }
                    if(motionEvent.getAction()==MotionEvent.ACTION_UP || motionEvent.getAction()==MotionEvent.ACTION_CANCEL){
                        if(sound!=null){
                            sound.play(R.raw.sound_stop_drawing);
                        }

                        buttons[index].setPadding(0, 0, 0 ,0);
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
        equationFragment.initialize(EquationFragment.UnknownElementStyle.FIELD);
        equationFragment.setListener(() -> startGame());
        getSupportFragmentManager().beginTransaction().add(R.id.cl_equation_container, equationFragment, null).commit();
    }

    public void tryCloseGame(View view){
        super.tryCloseGame(view);
    }
}