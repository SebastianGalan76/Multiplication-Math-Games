package com.coresaken.multiplication.activity.competition;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.panel.LostHeartActivity;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.DailyStreakController;
import com.coresaken.multiplication.controller.EquationController;
import com.coresaken.multiplication.controller.GameController;
import com.coresaken.multiplication.controller.HeartLimit;
import com.coresaken.multiplication.controller.PanelController;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.data.AnsweredEquation;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.GameStatistic;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.Sound;
import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.data.enums.AnswerType;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.data.enums.RangeType;
import com.coresaken.multiplication.fragment.game.EquationFragment;
import com.coresaken.multiplication.listener.HeartLimitListener;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Competition0Activity extends AppCompatActivity {
    public static final int REQUEST_CODE = 1;
    SQLiteHelper sqLiteHelper;

    GameStatistic statistic;
    GameController controller;
    EquationController equationController;
    EquationFragment equationFragment;

    Random random;

    UnknownEquation currentUnknownEquation;
    AnsweredEquation currentAnsweredEquation;
    List<AnsweredEquation> answeredEquations;

    View[] buttons;
    StringBuilder currentValue;
    CountDownTimer answerDelayTimer;

    ProgressBar pb_progress;

    TextView tv_level, tv_equationAmount;
    TextView tv_heartAmount;

    Dialog exitDialog;

    int level;
    int numberMin, numberMax;
    boolean isFinished;

    CountDownTimer finishGameTimer;
    CountDownTimer timer;

    private HeartLimit heartLimit;
    private boolean allowRenewal;
    Sound sound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_competition0);

        pb_progress = findViewById(R.id.pb_progress);
        tv_level = findViewById(R.id.tv_level);
        tv_equationAmount= findViewById(R.id.tv_equation_amount);
        tv_heartAmount = findViewById(R.id.tv_heart_amount);

        if(PlayerSettings.getInstance().sounds){
            sound = new Sound(this, new int[]{R.raw.sound_correct, R.raw.sound_incorrect, R.raw.sound_click_button_2, R.raw.sound_start_drawing, R.raw.sound_stop_drawing});
        }

        SettingsController.getInstance().settings = new Settings.SettingsBuilder()
                .addOperator(OperatorType.MULTIPLICATION)
                .setModeType(ModeType.COMPETITION)
                .setAnswerType(AnswerType.MIXED)
                .setGameType(GameType.COMPETITION)
                .setRangeType(RangeType.AB).build();

        controller = GameController.getInstance();

        heartLimit = new HeartLimit(3, new HeartLimitListener() {
            @Override
            public void onChangeHeartAmount(int currentAmount) {
                changeHeartAmountText(currentAmount);
            }

            @Override
            public void onLostAllHeart() {
                lostGame();
            }
        });

        statistic = new GameStatistic();
        equationController = new EquationController();

        random = new Random();

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

        DailyStreakController.getInstance().startCounting();
        finishGameTimer = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {
                finishGame();
            }
        };

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                tryCloseGame(null);
            }
        });
    }

    private void increaseLevel(){
        level++;

        numberMin+=5;
        numberMax+=5;

        tv_level.setText(getString(R.string.level) +" "+level);
    }

    private void changeHeartAmountText(int value){
        tv_heartAmount.setText(String.valueOf(value));
    }

    protected void startGame(){
        controller.startNewGame(GameType.COMPETITION);
        controller.heartLimit = heartLimit;
        allowRenewal = true;

        tv_equationAmount.setText("0");
        level = 0;

        numberMin = 0;
        numberMax = 5;

        isFinished = false;
        loadNewEquation();

        level++;
        tv_level.setText(getString(R.string.level) +" "+level);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(isFinished){
            isFinished = false;

            loadNewEquation();
        }
    }

    protected void lostGame(){
        isFinished = true;

        if(allowRenewal){
            Intent intent = new Intent(this, LostHeartActivity.class);
            startActivityForResult(intent, REQUEST_CODE);

            allowRenewal = false;
        }
        else{
            finishGame();
        }
    }

    protected void finishGame(){
        isFinished = true;
        controller.finishGame(statistic);

        changeButtonsEnable(false);

        DailyStreakController.getInstance().stopCounting();
        PanelController.getInstance().finishGame();

        startActivity(new Intent(this, PanelController.getInstance().getNextPanel()));
        finish();

        controller.answeredEquations = answeredEquations;
    }

    protected void changeButtonsEnable(boolean enable){
        for(View button : buttons){
            button.setEnabled(enable);
        }
    }

    protected void onDestroy() {
        super.onDestroy();

        if(answerDelayTimer!=null){
            answerDelayTimer.cancel();
        }

        if(exitDialog!=null){
            exitDialog.dismiss();
        }

        timer.cancel();
    }

    private void loadNewEquation(){
        if(isFinished){
            return;
        }

        if(heartLimit.getHeartAmount()<=0){
            finishGame();
            return;
        }

        Equation equation = equationController.getEquationForCompetition(numberMin, numberMax);
        currentUnknownEquation = new UnknownEquation(equation);
        equationController.setUnknownElement(currentUnknownEquation);

        statistic.currentEquation++;

        if(statistic.currentEquation%10==0){
            increaseLevel();
        }

        currentValue = new StringBuilder();
        currentValue.append("?");

        currentAnsweredEquation = new AnsweredEquation(currentUnknownEquation);
        answeredEquations.add(currentAnsweredEquation);

        changeButtonsEnable(true);
        equationFragment.setEquation(currentUnknownEquation);

        if(timer!=null){
            timer.cancel();
        }

        timer = new CountDownTimer(5000, 40) {
            public void onTick(long millisUntilFinished) {
                int totalTime = 5000;
                int progress = (int) (((float)millisUntilFinished / (float) totalTime) * 100);

                pb_progress.setProgress(progress);
            }

            public void onFinish() {
                heartLimit.changeHeartAmount(-1);
                loadNewEquation();
            }
        }.start();
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

        long delay = 500;
        if(currentValue.length()==String.valueOf(currentUnknownEquation.getUnknownElementValue()).length() && !questionMark){
            changeButtonsEnable(false);
            int currentValueInt = Integer.parseInt(currentValue.toString());

            boolean isCorrect = currentValueInt == currentUnknownEquation.getUnknownElementValue();

            equationFragment.answer(currentValueInt, isCorrect);

            currentAnsweredEquation.addAnswer(currentValueInt, isCorrect);

            statistic.totalAnswer++;

            timer.cancel();

            answerDelayTimer = new CountDownTimer(delay, delay) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    loadNewEquation();
                }
            }.start();

            if(isCorrect){
                correctAnswer();
            }
            else{
                incorrectAnswer();
            }
        }
    }
    protected void correctAnswer() {
        statistic.correctAnswer++;
        statistic.changeGainedExp(random.nextInt(2) + 1);
        tv_equationAmount.setText(String.valueOf(statistic.correctAnswer));

        if(sound!=null){
            sound.play(R.raw.sound_correct);
        }

        updateEquationPoints(5);
    }
    protected void incorrectAnswer() {
        statistic.incorrectAnswer++;
        statistic.changeGainedExp(-random.nextInt(2));

        heartLimit.changeHeartAmount(-1);

        if(sound!=null){
            sound.play(R.raw.sound_incorrect);
        }

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
                        if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                            if(sound!=null){
                                sound.play(R.raw.sound_stop_drawing);
                            }
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
        //EquationFragment
        equationFragment = EquationFragment.newInstance();
        equationFragment.setListener(() -> startGame());
        equationFragment.initialize(EquationFragment.UnknownElementStyle.FIELD);
        getSupportFragmentManager().beginTransaction().add(R.id.cl_equation_container, equationFragment, null).commit();
    }

    private void playButtonClick(){
        if(sound!=null){
            sound.play(R.raw.sound_click_button_2);
        }
    }

    public void tryCloseGame(View view){
        exitDialog = new Dialog(this, R.style.DimOverlay);

        playButtonClick();

        exitDialog.setContentView(R.layout.dialog_exit_game);
        Window window = exitDialog.getWindow();
        window.setBackgroundDrawableResource(R.color.dialog_bg);

        Button btn_yes = exitDialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButtonClick();

                exitDialog.dismiss();
                finishGame();
            }
        });

        Button btn_no = exitDialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButtonClick();

                exitDialog.cancel();
                exitDialog.dismiss();
            }
        });

        ImageButton btn_close = exitDialog.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButtonClick();

                exitDialog.cancel();
                exitDialog.dismiss();
            }
        });

        exitDialog.show();
    }
}