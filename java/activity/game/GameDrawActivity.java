package com.coresaken.multiplication.activity.game;

import static com.coresaken.multiplication.util.Utils.shuffleArray;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.data.Display;
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

import java.util.ArrayList;
import java.util.List;

public class GameDrawActivity extends Game {
    public static Path path = new Path();
    public static Paint paint_brush = new Paint();

    Rect outRect = new Rect();
    int[] location = new int[2];

    Card[] cards;
    int totalCheck, cardsLeft;
    boolean isCorrect;
    public boolean active;

    Card[] selectedCards;
    List<GameCardActivity.AnsweredEquation> answeredEquations;

    CountDownTimer checkResultTimer;
    CountDownTimer displayResultTimer;
    Display display;

    Sound sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_draw);

        if(PlayerSettings.getInstance().sounds){
            sound = new Sound(this, new int[] {R.raw.sound_start_drawing, R.raw.sound_stop_drawing});
        }

        sqLiteHelper = new SQLiteHelper(this);
        answeredEquations = new ArrayList<>();
        selectedCards = new Card[2];

        display = findViewById(R.id.display);
        display.setActivity(this, sound);

        ConstraintLayout cl_container = findViewById(R.id.cl_container);
        int childNumber = cl_container.getChildCount();
        cardsLeft = childNumber;
        cards = new Card[childNumber];

        for(int i=0;i<childNumber;i++){
            TextView view = (TextView) cl_container.getChildAt(i);

            cards[i] = new Card(view);
        }

        if(settings!=null && settings.modeType==ModeType.CORRECTION){
            equationController.loadCorrectionEquation(9);
        }

        Equation[] equations = new Equation[9];
        for(int i=0;i<equations.length;i++){
            Equation equation = equationController.getEquation();

            if(equation==null){
                if(settings.modeType==ModeType.LESSON){
                    equation = equationController.getRandomLessonEquation(settings.currentLesson);
                }
                else if(settings.modeType==ModeType.CORRECTION){
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

        initializeTimers();
        initializeFragments();

        int textIndex = 0;
        for(int i=0;i<cards.length;i++){
            cards[i].setValue(buttonLeft[textIndex]);
            i++;
            cards[i].setValue(buttonRight[textIndex]);
            textIndex++;
        }

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));
    }

    protected void startGame() {
        super.startGame(GameType.DRAW);
        statistic.equationLimit = 9;

        totalCheck = 0;
        active = true;
    }

    @Override
    protected void startLesson(){
        equationController.loadLessonEquations(9, settings.currentLesson);
    }

    @Override
    protected void startCorrection(){
        //loaded in onCreate
        //equationController.loadCorrectionEquation(sqLiteHelper,9, settings.operators);
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
    @Override
    protected void finishGame() {
        super.finishGame();

        controller.answeredEquationsCard = answeredEquations;
        statistic.currentEquation = totalCheck;
    }

    @Override
    protected void completeGame() {
        statistic.currentEquation = totalCheck;

        super.completeGame();
    }

    @Override
    protected void completeLesson() {
        Lesson currentLesson = settings.currentLesson;

        int progress = statistic.getCorrectAnswerPercent();
        progress *= 0.7;

        if(currentLesson!=null){
            currentLesson.changeExercisesProgress(GameDrawActivity.this, settings.exerciseIndex, progress);
        }
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
                isCorrect = selectedCards[0].value == selectedCards[1].value;
                totalCheck++;

                if(isCorrect){
                    correctAnswer(true, true);

                    selectedCards[0].makeGreen();
                    selectedCards[1].makeGreen();

                    statistic.totalAnswer++;
                    upPanelFragment.changeProgressBar(statistic);

                    cardsLeft -= 2;

                    paint_brush.setColor(getColor(R.color.correctAnswer));
                    display.invalidate();
                }
                else{
                    incorrectAnswer(true, true);

                    selectedCards[0].makeRed();
                    selectedCards[1].makeRed();

                    paint_brush.setColor(getColor(R.color.incorrect_color_up));
                    display.invalidate();
                }

                answeredEquations.add(new GameCardActivity.AnsweredEquation(new String[]{selectedCards[0].textView.getText().toString(), selectedCards[1].textView.getText().toString()}, isCorrect));
                displayResultTimer.start();

                changeButtonsEnable(true);

                if(cardsLeft<=0){
                    completeGame();
                }
            }
        };
        displayResultTimer = new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if(!isCorrect){
                    selectedCards[0].makeWhite();
                    selectedCards[1].makeWhite();
                }
                else {
                    selectedCards[0].textView.setVisibility(View.INVISIBLE);
                    selectedCards[1].textView.setVisibility(View.INVISIBLE);
                }

                display.pathList.clear();
                path.reset();
                display.invalidate();

                selectedCards[0] = null;
                selectedCards[1] = null;
                active = true;
            }
        };

        timers.add(displayResultTimer);
        timers.add(checkResultTimer);
        timers.add(finishGameTimer);
    }

    public void unselectCards(){
        if(selectedCards[0]!=null){
            selectedCards[0].unselectCard();
        }
        if(selectedCards[1]!=null){
            selectedCards[1].unselectCard();
        }
    }
    public void selectCard(Card card, int index){
        selectedCards[index] = card;
        card.selectCard();
        card.selected = true;

        if(selectedCards[0]!=null && selectedCards[1]!=null){
            active = false;
            checkResultTimer.start();
        }
    }
    @Override
    protected void correctAnswer(boolean firstAnswer, boolean playSound) {
        super.correctAnswer(firstAnswer, playSound);

        if(!selectedCards[0].onlyResult){
            sqLiteHelper.updatePoints(selectedCards[0].equationId, 5);
        }
        if(!selectedCards[1].onlyResult){
            sqLiteHelper.updatePoints(selectedCards[1].equationId, 5);
        }
    }
    @Override
    protected void incorrectAnswer(boolean firstAnswer, boolean playSound) {
        super.incorrectAnswer(firstAnswer, playSound);

        if(!selectedCards[0].onlyResult){
            sqLiteHelper.updatePoints(selectedCards[0].equationId, -8);
        }
        if(!selectedCards[1].onlyResult){
            sqLiteHelper.updatePoints(selectedCards[1].equationId, -8);
        }
    }

    public boolean findView(int x, int y, int index){
        for(Card card : cards){
            if(!card.selected){
                if(isViewInBounds(card.textView, x, y)){
                    selectCard(card, index);

                    return true;
                }
            }
        }

        return false;
    }
    private boolean isViewInBounds(View view, int x, int y){
        view.getDrawingRect(outRect);
        view.getLocationOnScreen(location);
        outRect.offset(location[0], location[1]);
        return outRect.contains(x, y);
    }

    @Override
    protected Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.stopAllTimers();
    }

    public void tryCloseGame(View view){
        super.tryCloseGame(view);
    }

    private class Card{
        public String equationId;
        public TextView textView;
        public int value;
        public boolean onlyResult;

        private boolean solved;
        private boolean active;
        public boolean selected;

        public Card(TextView textView){
            this.textView = textView;
            this.active = true;
        }

        public void setValue(String value){
            Expression expression = new Expression(value);
            this.value = expression.eval().intValue();

            textView.setText(Utils.convertOperatorSign(value));

            if(value.matches("[0-9]+")){
                onlyResult = true;
            }
            else{
                equationId = value.replace(" ","");
            }
        }

        public void selectCard(){
            textView.setTextColor(Color.WHITE);

            textView.setBackgroundColor(getColor(R.color.white));
            textView.setTextColor(getColor(R.color.normal_text));

            selected = true;
        }

        public void unselectCard(){
            textView.setTextColor(getColor(R.color.normal_text));

            makeWhite();
            selected = false;
        }
        public void makeGreen(){
            solved = true;
            active = false;

            textView.setSelected(false);
            textView.setBackgroundColor(getColor(R.color.correctAnswer));
            textView.setTextColor(getColor(R.color.correct_text));

            textView.setEnabled(false);
        }
        public void makeRed(){
            active = false;

            textView.setSelected(false);
            textView.setBackgroundColor(getColor(R.color.incorrect_color_up));
            textView.setTextColor(getColor(R.color.incorrect_text));
        }
        public void makeWhite(){
            textView.setSelected(false);
            textView.setBackgroundColor(0);
            textView.setTextColor(getColor(R.color.white));

            active = true;
            selected = false;
        }
    }
}