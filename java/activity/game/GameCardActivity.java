package com.coresaken.multiplication.activity.game;

import static com.coresaken.multiplication.util.Utils.shuffleArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.List;

public class GameCardActivity extends Game {
    Card[] cards;
    private List<AnsweredEquation> answeredEquations;

    public Card[] flippedCard;
    public boolean active;

    CountDownTimer showFrontCardTimer;
    CountDownTimer checkResultTimer;

    int totalCheck;
    int cardLeft;

    Sound sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_card);

        if(PlayerSettings.getInstance().sounds){
            sound = new Sound(this, new int[]{R.raw.sound_match_click});
        }

        sqLiteHelper = new SQLiteHelper(this);

        cards = new Card[18];
        flippedCard = new Card[2];
        answeredEquations = new ArrayList<>();

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
                else if(settings.modeType==ModeType.CORRECTION){
                    equation = equationController.getRandomCorrectionEquation();
                }
            }

            equations[i] = equation;
        }

        String[] mixedValues = new String[18];
        int valueIndex = 0;
        for (Equation equation : equations) {
            List<Equation.Element> elements = equation.getElements();

            mixedValues[valueIndex] = elements.get(0).number + " " + equation.operatorType.sign + " " + elements.get(2).number;
            mixedValues[valueIndex + 1] = String.valueOf(elements.get(4).number);

            valueIndex += 2;
        }
        shuffleArray(mixedValues);

        cards[0] = new Card(R.id.card_0, mixedValues[0]);
        cards[1] = new Card(R.id.card_1, mixedValues[1]);
        cards[2] = new Card(R.id.card_2, mixedValues[2]);
        cards[3] = new Card(R.id.card_3, mixedValues[3]);
        cards[4] = new Card(R.id.card_4, mixedValues[4]);
        cards[5] = new Card(R.id.card_5, mixedValues[5]);
        cards[6] = new Card(R.id.card_6, mixedValues[6]);
        cards[7] = new Card(R.id.card_7, mixedValues[7]);
        cards[8] = new Card(R.id.card_8, mixedValues[8]);
        cards[9] = new Card(R.id.card_9, mixedValues[9]);
        cards[10] = new Card(R.id.card_10, mixedValues[10]);
        cards[11] = new Card(R.id.card_11, mixedValues[11]);
        cards[12] = new Card(R.id.card_12, mixedValues[12]);
        cards[13] = new Card(R.id.card_13, mixedValues[13]);
        cards[14] = new Card(R.id.card_14, mixedValues[14]);
        cards[15] = new Card(R.id.card_15, mixedValues[15]);
        cards[16] = new Card(R.id.card_16, mixedValues[16]);
        cards[17] = new Card(R.id.card_17, mixedValues[17]);

        initializeTimers();
        initializeFragments();

        active = true;

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));
    }

    protected void startGame(){
        super.startGame(GameType.CARD);

        statistic.equationLimit = 9;

        totalCheck = 0;
        cardLeft = cards.length;
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

        controller.answeredEquationsCard = answeredEquations;
        statistic.currentEquation = totalCheck;
    }

    @Override
    protected void completeGame(){
        statistic.currentEquation = totalCheck;

        super.completeGame();
    }

    @Override
    protected void completeLesson() {
        Lesson currentLesson = settings.currentLesson;

        int progress = statistic.getCorrectAnswerPercent();
        progress *= 0.5;

        if(currentLesson!=null){
            currentLesson.changeExercisesProgress(GameCardActivity.this, settings.exerciseIndex, progress);
        }
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

    @Override
    protected void onResume() {
        super.onResume();

        if(isFinished){
            showFrontCardTimer.start();
            isFinished = false;
        }
    }

    public void flipCard(Card card){
        flippedCard[1] = flippedCard[0];
        flippedCard[0] = card;

        if(sound!=null){
            sound.play(R.raw.sound_match_click);
        }

        if(flippedCard[0]!=null && flippedCard[1]!=null){
            active = false;

            checkResultTimer.start();
            showFrontCardTimer.start();
        }
    }
    public void unflipCard(Card card){
        if(sound!=null){
            sound.play(R.raw.sound_match_click);
        }

        if(flippedCard[0]==card){
            flippedCard[0] = null;
        }
        if(flippedCard[1]==card){
            flippedCard[1] = null;
        }
    }
    protected void correctAnswer() {
        super.correctAnswer(true, true);

        flippedCard[0].makeGreen();
        flippedCard[1].makeGreen();

        flippedCard[0].changeFrontCardColor();
        flippedCard[1].changeFrontCardColor();

        cardLeft-=2;

        statistic.totalAnswer++;
        upPanelFragment.changeProgressBar(statistic);

        if(!flippedCard[0].onlyResult){
            sqLiteHelper.updatePoints(flippedCard[0].equationId, 5);
        }
        if(!flippedCard[1].onlyResult){
            sqLiteHelper.updatePoints(flippedCard[1].equationId, 5);
        }

        if(cardLeft<=0){
            completeGame();
        }
    }
    protected void incorrectAnswer() {
        super.incorrectAnswer(true, true);

        flippedCard[0].makeRed();
        flippedCard[1].makeRed();

        if(!flippedCard[0].onlyResult){
            sqLiteHelper.updatePoints(flippedCard[0].equationId, -8);
        }
        if(!flippedCard[1].onlyResult){
            sqLiteHelper.updatePoints(flippedCard[1].equationId, -8);
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
        showFrontCardTimer = new CountDownTimer(1500, 1500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if(!isFinished){
                    flippedCard[0].showFrontCard();
                    flippedCard[1].showFrontCard();

                    flippedCard[0] = null;
                    flippedCard[1] = null;

                    active = true;
                }
            }
        };
        checkResultTimer = new CountDownTimer(500, 500) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Expression expression0 = new Expression(flippedCard[0].value);
                Expression expression1 = new Expression(flippedCard[1].value);

                int result0 = expression0.eval().intValue();
                int result1 = expression1.eval().intValue();

                boolean isCorrect = result0 == result1;

                totalCheck++;
                answeredEquations.add(new AnsweredEquation(new String[]{flippedCard[0].value, flippedCard[1].value}, isCorrect));

                if(isCorrect){
                    correctAnswer();
                }
                else{
                    incorrectAnswer();
                }
            }
        };

        timers.add(showFrontCardTimer);
        timers.add(checkResultTimer);
    }

    public void tryCloseGame(View view){
        super.tryCloseGame(view);
    }

    class Card{
        public String equationId;
        ConstraintLayout button;
        String value;
        public boolean onlyResult;

        boolean showingBack;

        CardBackFragment cardBackFragment;
        CardFrontFragment cardFrontFragment;

        boolean active;

        CountDownTimer clickDelay = new CountDownTimer(500, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                active = true;
            }
        };

        public Card(int buttonId, String value){
            button = findViewById(buttonId);
            this.value = value;

            if(value.matches("[0-9]+")){
                onlyResult = true;
            }
            else{
                String buttonText = value;
                buttonText = buttonText.replace(" ","");
                equationId = buttonText;
            }

            showingBack = false;
            active = true;

            cardBackFragment = CardBackFragment.getInstance();
            cardBackFragment.initialize(value);

            cardFrontFragment = CardFrontFragment.getInstance();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(buttonId, cardFrontFragment)
                    .commit();

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    flipCard();
                }
            });
        }
        private void flipCard() {
            if(!active || !GameCardActivity.this.active){
                return;
            }
            active = false;

            if (showingBack) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.animator.card_flip_left_in,
                                R.animator.card_flip_right_out,
                                R.animator.card_flip_left_in,
                                R.animator.card_flip_left_out)
                        .replace(button.getId(), cardFrontFragment)
                        .commit();

                showingBack = false;
                unflipCard(this);
            }
            else{
                getSupportFragmentManager()
                        .beginTransaction()
                        .setCustomAnimations(
                                R.animator.card_flip_left_in,
                                R.animator.card_flip_right_out,
                                R.animator.card_flip_left_in,
                                R.animator.card_flip_left_out)
                        .replace(button.getId(), cardBackFragment)
                        .commit();

                showingBack = true;
                GameCardActivity.this.flipCard(this);
            }

            clickDelay.start();
        }

        public void showFrontCard(){
            active = false;
            showingBack = false;

            getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_right_out,
                            R.animator.card_flip_left_in,
                            R.animator.card_flip_left_out)
                    .replace(button.getId(), cardFrontFragment)
                    .commit();
            clickDelay.start();
        }

        private void makeRed(){
            cardBackFragment.makeRed(GameCardActivity.this);
        }
        private void makeGreen(){
            button.setEnabled(false);
            cardBackFragment.makeGreen(GameCardActivity.this);
        }
        private void changeFrontCardColor(){
            cardFrontFragment.changeBackgroundColor();
        }
    }

    public static class CardFrontFragment extends Fragment {

        ConstraintLayout cl_front;
        boolean changeBackgroundColor;

        public static CardFrontFragment getInstance(){
            return new CardFrontFragment();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_card_front, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            cl_front = view.findViewById(R.id.cl_card_front);

            if(changeBackgroundColor){
                cl_front.setBackgroundTintList(ColorStateList.valueOf(getActivity().getColor(R.color.background_dark)));
            }
        }

        public void changeBackgroundColor(){
            changeBackgroundColor = true;
        }
    }
    public static class CardBackFragment extends Fragment {
        String value;
        TextView tv_card;
        ConstraintLayout cl_back;

        public static CardBackFragment getInstance(){
            return new CardBackFragment();
        }

        public void initialize(String value){
            this.value = value;
        }
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_card_back, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            tv_card = view.findViewById(R.id.tv_card_value);
            cl_back = view.findViewById(R.id.cl_card_back);

            makeWhite(getActivity());
            tv_card.setText(Utils.convertOperatorSign(value));
        }

        public void makeRed(Context context){
            cl_back.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.incorrect_color_up)));
            tv_card.setTextColor(context.getColor(R.color.incorrect_text));
        }

        public void makeGreen(Context context){
            cl_back.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.correctAnswer)));

            tv_card.setTextColor(context.getColor(R.color.correct_text));
        }

        private void makeWhite(Context context){
            cl_back.setBackgroundTintList(ColorStateList.valueOf(context.getColor(R.color.white)));
            tv_card.setTextColor(context.getColor(R.color.normal_text));
        }
    }



    public static class AnsweredEquation{
        public String[] values;
        public boolean isCorrect;

        public AnsweredEquation(String[] values, boolean isCorrect){
            this.values = values;
            this.isCorrect = isCorrect;
        }
    }
}