package com.coresaken.multiplication.activity.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import com.coresaken.multiplication.R;
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

public class GameTrueFalseActivity extends Game {
    UnknownEquation currentUnknownEquation;
    AnsweredEquation currentAnsweredEquation;
    List<AnsweredEquation> answeredEquations;

    boolean equationIsCorrect;
    int wrongAnswerValue;

    int answerAmount = 2;
    ImageButton[] answerButtons;

    CountDownTimer answerDelayTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_true_false);

        sqLiteHelper = new SQLiteHelper(this);
        answeredEquations = new ArrayList<>();

        initializeButtons();
        initializeFragments();

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));
    }

    protected void startGame(){
        super.startGame(GameType.TRUE_FALSE);

        loadNewEquation();
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
    protected void resumeGame(){
        super.resumeGame();

        if(isFinished){
            isFinished = false;
            loadNewEquation();
        }
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

    protected void finishGame(){
        super.finishGame();

        controller.answeredEquations = answeredEquations;
        controller.statistic.equationLimit = controller.statistic.currentEquation;
    }

    @Override
    protected void completeLesson() {
        Lesson currentLesson = settings.currentLesson;

        int progress = statistic.getCorrectAnswerPercent();
        progress *= 0.75;

        if(currentLesson!=null){
            currentLesson.changeExercisesProgress(this, settings.exerciseIndex, progress);
        }
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
        }

        statistic.currentEquation++;

        loadAnswers();

        equationFragment.setEquation(currentUnknownEquation);
        if(random.nextInt(100)<50){
            wrongAnswerValue = answerController.getAnswers(currentUnknownEquation.equation, 2, 4, false)[1];
            equationFragment.changeElement(4, String.valueOf(wrongAnswerValue));

            equationIsCorrect = false;
        }
        else{
            equationIsCorrect = true;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadAnswers(){
        for(int i=0;i<answerAmount;i++){
            answerButtons[i].setBackground(getDrawable(R.drawable.button_normal1));
            answerButtons[i].setColorFilter(getColor(R.color.normal_text));
        }

        changeButtonsEnable(true);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeButtons(){
        answerButtons = new ImageButton[answerAmount];

        answerButtons[0] = findViewById(R.id.btn_answer_true);
        answerButtons[1] = findViewById(R.id.btn_answer_false);

        int normalPadding = Utils.convertDPToPixels(getResources(), 50);
        int pressPadding = Utils.convertDPToPixels(getResources(), 60);

        for(int i=0;i<answerAmount;i++){
            final int index = i;
            answerButtons[index].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                        answerButtons[index].setPadding(normalPadding, pressPadding, normalPadding ,normalPadding);
                        return false;
                    }
                    if(motionEvent.getAction()==MotionEvent.ACTION_UP || motionEvent.getAction()==MotionEvent.ACTION_CANCEL){
                        answerButtons[index].setPadding(normalPadding, normalPadding, normalPadding ,normalPadding);
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

    public void changeButtonsEnable(boolean enable){
        for(ImageButton button : answerButtons){
            button.setEnabled(enable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(answerDelayTimer!=null){
            answerDelayTimer.cancel();
        }

        super.stopAllTimers();
    }

    @Override
    protected Context getContext() {
        return this;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void answer(View view){
        int index = 0;
        boolean selectedValue = true;

        if(view.getId()==R.id.btn_answer_false){
            index = 1;
            selectedValue = false;
        }

        boolean isCorrect = selectedValue == equationIsCorrect;

        statistic.totalAnswer++;

        long delay;
        if(equationIsCorrect && isCorrect){
            delay = 250;
        }
        else{
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

        if(settings.modeType==ModeType.CORRECTION){
            if(isCorrect){
                sqLiteHelper.updateIncorrectEquationPoints(currentUnknownEquation.equation, 4);
            }
        }

        if(isCorrect){
            correctAnswer(index);
            upPanelFragment.changeProgressBar(statistic);
        }
        else{
            incorrectAnswer(index);
        }

        changeButtonsEnable(false);
        equationFragment.answer(isCorrect);

        currentUnknownEquation.setUnknownElementIndex(4);
        currentAnsweredEquation = new AnsweredEquation(currentUnknownEquation);
        currentAnsweredEquation.equationIsCorrect = equationIsCorrect;

        if(equationIsCorrect){
            currentAnsweredEquation.addAnswer(currentUnknownEquation.getUnknownElementValue(), isCorrect);
        }
        else{
            currentAnsweredEquation.addAnswer(wrongAnswerValue, isCorrect);
        }

        answeredEquations.add(currentAnsweredEquation);
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void correctAnswer(int buttonIndex){
        super.correctAnswer(true, true);

        updateEquationPoints(4);

        changeButtonsEnable(false);
        answerButtons[buttonIndex].setBackground(getDrawable(R.drawable.button_correct1));
        answerButtons[buttonIndex].setColorFilter(getColor(R.color.correct_text));

        if(!equationIsCorrect){
            equationFragment.changeElement(4, String.valueOf(currentUnknownEquation.equation.getElements().get(4).number));
            equationFragment.changeElementColor(4, R.color.correctAnswer);
        }
    }
    @SuppressLint("UseCompatLoadingForDrawables")
    protected void incorrectAnswer(int buttonIndex){
        super.incorrectAnswer(true, true);

        if(settings.modeType == ModeType.LESSON){
            super.addIncorrectAnsweredEquation(currentUnknownEquation);
            statistic.equationLimit++;
        }

        sqLiteHelper.updateIncorrectEquationPoints(currentUnknownEquation.equation, -10);

        updateEquationPoints(-12);

        answerButtons[buttonIndex].setEnabled(false);
        answerButtons[buttonIndex].setBackground(getDrawable(R.drawable.button_incorrect1));
        answerButtons[buttonIndex].setColorFilter(getColor(R.color.incorrect_text));
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