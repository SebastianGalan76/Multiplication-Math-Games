package com.coresaken.multiplication.data;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.panel.LostHeartActivity;
import com.coresaken.multiplication.controller.AnswerController;
import com.coresaken.multiplication.controller.DailyStreakController;
import com.coresaken.multiplication.controller.EquationController;
import com.coresaken.multiplication.controller.GameController;
import com.coresaken.multiplication.controller.HeartLimit;
import com.coresaken.multiplication.controller.PanelController;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.TimeLimit;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.LimitType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.fragment.game.EquationFragment;
import com.coresaken.multiplication.fragment.game.GameUpPanelFragment;
import com.coresaken.multiplication.listener.HeartLimitListener;
import com.coresaken.multiplication.listener.TimeLimitListener;
import com.coresaken.multiplication.service.SQLiteHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Game extends AppCompatActivity implements TimeLimitListener, HeartLimitListener {
    public static final int REQUEST_CODE = 1;

    protected SQLiteHelper sqLiteHelper;

    protected Settings settings;
    protected GameStatistic statistic;
    protected GameController controller;
    protected EquationController equationController;
    protected AnswerController answerController;

    protected Random random;
    protected Button[] answerButtons;

    protected List<CountDownTimer> timers;

    protected Sound sound;

    TimeLimit timeLimit;
    HeartLimit heartLimit;

    Dialog exitDialog;

    protected List<UnknownEquation> incorrectAnsweredEquations;
    protected EquationFragment equationFragment;
    protected GameUpPanelFragment upPanelFragment;

    protected boolean isFinished;
    protected CountDownTimer finishGameTimer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        if(PlayerSettings.getInstance().sounds){
            sound = new Sound(getContext(), new int[]{R.raw.sound_correct, R.raw.sound_incorrect, R.raw.sound_click_button_2});
        }
    }

    public Game(){
        controller = GameController.getInstance();
        settings = SettingsController.getInstance().settings;

        statistic = new GameStatistic();
        equationController = new EquationController();
        answerController = new AnswerController();

        random = new Random();
        timers = new ArrayList<>();

        finishGameTimer = new CountDownTimer(1000, 1000) {
            public void onTick(long millisUntilFinished) {

            }
            public void onFinish() {
                finishGame();
            }
        };
        timers.add(finishGameTimer);

        DailyStreakController.getInstance().startCounting();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                tryCloseGame(null);
            }
        });
    }

    protected void startGame(GameType gameType){
        controller.startNewGame(gameType);
        initializeLimits();

        if(settings.modeType == ModeType.LESSON){
            settings.initializeExercise();
            startLesson();
        }
        else if(settings.modeType == ModeType.CORRECTION){
            startCorrection();
        }
    }


    //Executes when the user watches an ad.
    protected void resumeGame(){
        refreshUpPanel();
    }

    //Execute before launching the summary activity
    protected void finishGame(){
        isFinished = true;
        controller.finishGame(statistic);

        stopAllTimers();
        changeButtonsEnable(false);

        DailyStreakController.getInstance().stopCounting();
        PanelController.getInstance().finishGame();

        startActivity(new Intent(getContext(), PanelController.getInstance().getNextPanel()));
        finish();
    }

    //Executes when the user successfully completes the game
    protected void completeGame(){
        isFinished = true;
        if(settings.modeType == ModeType.LESSON){
            controller.lessonProgressStart = settings.currentExercise.progress;
            completeLesson();
            controller.lessonProgressEnd = settings.currentExercise.progress;
        }

        finishGameTimer.start();
    }

    //Executes when the user loses all hearts or time runs out
    protected void lostGame(){
        isFinished = true;

        if(settings.modeType==ModeType.LESSON){
            int lessonProgress = settings.currentExercise.progress;
            controller.lessonProgressStart = lessonProgress;
            controller.lessonProgressEnd = lessonProgress;
        }

        Intent intent = new Intent(this, LostHeartActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                resumeGame();
            }
            if(resultCode== Activity.RESULT_CANCELED){
                finishGame();
            }
        }
    }

    public void refreshUpPanel(){
        upPanelFragment.refreshPanel(heartLimit, timeLimit);
    }

    //Puzzle game didn't use it
    protected void correctAnswer(boolean firstAnswer, boolean playSound){
        if(firstAnswer){
            statistic.correctAnswer++;
        }

        if(sound!=null && playSound){
            sound.play(R.raw.sound_correct);
        }

        statistic.changeGainedExp(random.nextInt(2) + 1);
    }

    //Puzzle game didn't use it
    protected void incorrectAnswer(boolean firstAnswer, boolean playSound){
        if(firstAnswer){
            statistic.incorrectAnswer++;
        }

        statistic.changeGainedExp(-random.nextInt(2));

        if(sound!=null && playSound){
            sound.play(R.raw.sound_incorrect);
        }

        if(heartLimit!=null){
            heartLimit.changeHeartAmount(-1);
        }
    }

    protected void clickButtonSound(){
        if(sound!=null){
            sound.play(R.raw.sound_click_button_2);
        }
    }

    protected void addIncorrectAnsweredEquation(UnknownEquation equation){
        equation.correction = true;
        incorrectAnsweredEquations.add(equation);
    }

    protected void stopAllTimers(){
        for(CountDownTimer timer : timers){
            timer.cancel();
        }

        if(timeLimit!=null){
            timeLimit.stopTimer();
        }
    }

    protected void tryCloseGame(View view){
        exitDialog = new Dialog(getContext(), R.style.DimOverlay);

        exitDialog.setContentView(R.layout.dialog_exit_game);
        Window window = exitDialog.getWindow();
        window.setBackgroundDrawableResource(R.color.dialog_bg);

        clickButtonSound();

        Button btn_yes = exitDialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickButtonSound();

                exitDialog.dismiss();
                finishGame();
            }
        });

        Button btn_no = exitDialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickButtonSound();

                exitDialog.cancel();
                exitDialog.dismiss();
            }
        });

        ImageButton btn_close = exitDialog.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickButtonSound();

                exitDialog.cancel();
                exitDialog.dismiss();
            }
        });

        exitDialog.show();
    }

    @Override
    public void onTick(long timeLeft) {
        upPanelFragment.changeTime(timeLeft);
    }
    @Override
    public void onTimeFinish() {
        changeButtonsEnable(false);
        finishGame();
    }

    @Override
    public void onChangeHeartAmount(int currentAmount) {
        upPanelFragment.changeHeartAmount(currentAmount);
    }
    @Override
    public void onLostAllHeart() {
        changeButtonsEnable(false);
        isFinished = true;
        lostGame();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(exitDialog!=null){
            exitDialog.dismiss();
        }
    }

    protected void changeButtonsEnable(boolean enable){
        if(answerButtons!=null){
            for(Button button : answerButtons){
                button.setEnabled(enable);
            }
        }
    }

    private void initializeLimits(){
        if(settings.limitType == LimitType.TIME){
            timeLimit = new TimeLimit(60000, this);
        } else if(settings.limitType == LimitType.HEART){
            heartLimit = new HeartLimit(3, this);
        } else if(settings.limitType == LimitType.EQUATION){
            statistic.equationLimit = settings.equationLimit;
        } else if(settings.limitType == LimitType.EQUATION_HEART){
            statistic.equationLimit = settings.equationLimit;
            heartLimit = new HeartLimit(3, this);

            statistic.equationLimit = settings.equationLimit;
        }

        controller.heartLimit = heartLimit;
        controller.timeLimit = timeLimit;
    }

    //Return true if we can continue the game
    protected boolean checkEquationLimit(){
        if(settings.limitType == LimitType.EQUATION || settings.limitType == LimitType.EQUATION_HEART){
            if(settings.modeType == ModeType.LESSON){
                return true;
            }

            return statistic.currentEquation < statistic.equationLimit;
        }

        return true;
    }

    protected abstract void startLesson();
    protected abstract void completeLesson();

    protected abstract  void startCorrection();

    protected abstract Context getContext();

    private void setWindowFlag(Activity activity, final int bits, boolean on) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }
}
