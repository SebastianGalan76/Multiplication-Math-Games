package com.coresaken.multiplication.activity.game;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Game;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.Sound;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.fragment.game.GameUpPanelFragment;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;
import com.udojava.evalex.Expression;

import static com.coresaken.multiplication.util.Utils.shuffleArray;

import java.util.ArrayList;
import java.util.List;

public class GameMatchActivity extends Game {
    Card selectedLeftCard;
    Card selectedRightCard;

    List<GameCardActivity.AnsweredEquation> answeredEquations;

    CountDownTimer checkResultTimer;

    Card[] cards;
    int cardsLeft;
    int totalCheck;

    Sound sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_match);

        if(PlayerSettings.getInstance().sounds){
            sound = new Sound(this, new int[]{R.raw.sound_match_click});
        }

        sqLiteHelper = new SQLiteHelper(this);
        answeredEquations = new ArrayList<>();

        ConstraintLayout cl_buttons = findViewById(R.id.cl_buttons);
        int buttonAmount = cl_buttons.getChildCount();
        cardsLeft = buttonAmount;
        cards = new Card[buttonAmount];
        answerButtons = new Button[buttonAmount];
        for(int i=0;i<buttonAmount;i++){
            ConstraintLayout cl_button = (ConstraintLayout) cl_buttons.getChildAt(i);

            Button button = (Button) cl_button.getChildAt(0);

            cards[i] = new Card(button);
            answerButtons[i] = button;
        }

        if(settings.modeType==ModeType.CORRECTION){
            equationController.loadCorrectionEquation(9);
        }

        Equation[] equations = new Equation[9];
        for(int i=0;i<equations.length;i++){
            Equation equation = equationController.getEquation();

            if(equation==null){
                if(settings.modeType==ModeType.LESSON){
                    equation = equationController.getRandomLessonEquation(settings.currentLesson);
                }
                else if(settings.modeType == ModeType.CORRECTION){
                    equation = equationController.getRandomCorrectionEquation();
                }
            }

            equations[i] = equation;
        }

        String[] buttonLeft = new String[9];
        String[] buttonRight = new String[9];
        for(int i=0;i<equations.length;i++){
            StringBuilder buttonLeftSB = new StringBuilder();

            List<Equation.Element> elements = equations[i].getElements();

            buttonLeftSB.append(elements.get(0).number).append(" ").append(equations[i].operatorType.sign).append(" ").append(elements.get(2).number);

            buttonLeft[i] = buttonLeftSB.toString();
            buttonRight[i] = String.valueOf(elements.get(4).number);
        }

        shuffleArray(buttonLeft);
        shuffleArray(buttonRight);

        int textIndex = 0;
        for(int i=0;i<cards.length;i++){
            cards[i].setButtonValue(buttonLeft[textIndex], Position.LEFT);
            i++;
            cards[i].setButtonValue(buttonRight[textIndex], Position.RIGHT);
            textIndex++;
        }

        initializeTimers();
        initializeFragments();

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));
    }



    protected void startGame() {
        super.startGame(GameType.MATCH);

        statistic.equationLimit = 9;
        statistic.currentEquation = 9;
    }

    @Override
    protected void startLesson(){
        equationController.loadLessonEquations(9, settings.currentLesson);
    }

    @Override
    protected void startCorrection(){
        //Already loaded in onCreate
        //equationController.loadCorrectionEquation(sqLiteHelper,9, settings.operators);
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
    protected void finishGame() {
        super.finishGame();

        controller.answeredEquationsCard = answeredEquations;
        controller.statistic.currentEquation = totalCheck;


    }

    @Override
    protected void completeLesson() {
        Lesson currentLesson = settings.currentLesson;

        int progress = statistic.getCorrectAnswerPercent();
        progress *= 0.6;

        if(currentLesson!=null){
            currentLesson.changeExercisesProgress(GameMatchActivity.this, settings.exerciseIndex, progress);
        }
    }

    public void selectCard(Card card){
        Position position = card.position;

        if(sound!=null){
            sound.play(R.raw.sound_match_click);
        }

        if(position == Position.LEFT){
            if(selectedLeftCard!=null){
                selectedLeftCard.unselectCard();

                if(selectedLeftCard==card){
                    selectedLeftCard = null;
                    return;
                }
            }

            selectedLeftCard = card;
            selectedLeftCard.selectCard();
        }
        else if(position == Position.RIGHT){
            if(selectedRightCard!=null){
                selectedRightCard.unselectCard();

                if(selectedRightCard==card){
                    selectedRightCard = null;
                    return;
                }
            }

            selectedRightCard = card;
            selectedRightCard.selectCard();
        }

        if(selectedLeftCard!=null && selectedRightCard!=null){
            changeButtonsEnable(false);

            checkResultTimer.start();
        }
    }
    protected void correctAnswer(boolean firstAnswer, String equationId) {
        super.correctAnswer(firstAnswer, true);

        statistic.totalAnswer++;
        upPanelFragment.changeProgressBar(statistic);

        sqLiteHelper.updatePoints(equationId, 5);
    }
    protected void incorrectAnswer(boolean firstAnswer, String equationId) {
        super.incorrectAnswer(firstAnswer, true);

        sqLiteHelper.updatePoints(equationId, -8);
    }

    private void initializeFragments(){
        upPanelFragment = GameUpPanelFragment.newInstance();
        upPanelFragment.initialize(settings.limitType);
        upPanelFragment.loadProgressBar();
        upPanelFragment.setListener(() -> startGame());
        getSupportFragmentManager().beginTransaction().add(R.id.cl_panel_up, upPanelFragment, null).commit();
    }
    private void initializeTimers(){
        checkResultTimer = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                boolean isCorrect = selectedLeftCard.value == selectedRightCard.value;
                totalCheck++;

                //Tworzenie equationID na podstawie zawwartości przycisku po lewej stronie
                String buttonText = selectedLeftCard.button.getText().toString();
                buttonText = buttonText.replace(" ","");

                buttonText = buttonText.replace("×", "*");
                buttonText = buttonText.replace("÷", "/");

                String equationId = buttonText;

                if(isCorrect){
                    correctAnswer(true, equationId);

                    selectedLeftCard.makeGreen();
                    selectedRightCard.makeGreen();

                    cardsLeft -= 2;
                }
                else{
                    incorrectAnswer(true, equationId);

                    selectedLeftCard.makeRed();
                    selectedRightCard.makeRed();
                }

                answeredEquations.add(new GameCardActivity.AnsweredEquation(new String[]{selectedLeftCard.button.getText().toString(), selectedRightCard.button.getText().toString()}, isCorrect));

                selectedLeftCard = null;
                selectedRightCard = null;

                changeButtonsEnable(true);

                if(cardsLeft<=0){
                    completeGame();
                }
            }
        };

        timers.add(checkResultTimer);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.stopAllTimers();
    }

    @Override
    protected Context getContext() {
        return this;
    }

    public void tryCloseGame(View view){
        super.tryCloseGame(view);
    }

    private class Card{
        public Button button;
        public int value;
        Position position;

        private boolean solved;
        private boolean active;

        CountDownTimer timer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if(!solved){
                    makeWhite();
                }
            }
        };

        public Card(Button button){
            this.button = button;
            this.active = true;

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickCard();
                }
            });
        }

        private void clickCard(){
            if(active){
                GameMatchActivity.this.selectCard(this);
            }
        }

        public void setButtonValue(String value, Position position){
            this.position = position;

            Expression expression = new Expression(value);
            this.value = expression.eval().intValue();

            button.setText(Utils.convertOperatorSign(value));
        }

        public void selectCard(){
            button.setTextColor(Color.WHITE);

            button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.checked_up)));
            button.setTextColor(getColor(R.color.white));
        }

        public void unselectCard(){
            button.setTextColor(getColor(R.color.normal_text));

            makeWhite();
        }

        public void makeGreen(){
            solved = true;
            active = false;

            button.setSelected(false);
            button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.correctAnswer)));
            button.setTextColor(getColor(R.color.correct_text));

            button.setEnabled(false);

            timer.start();
        }

        public void makeRed(){
            active = false;

            button.setSelected(false);
            button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.incorrect_color_up)));
            button.setTextColor(getColor(R.color.incorrect_text));

            timer.start();
        }

        public void makeWhite(){
            button.setSelected(false);
            button.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.white)));
            button.setTextColor(getColor(R.color.normal_text));

            active = true;
        }
    }

    private enum Position{
        LEFT, RIGHT
    }
}