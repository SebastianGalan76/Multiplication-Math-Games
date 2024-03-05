package com.coresaken.multiplication.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.competition.Competition0Activity;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.DailyReminderController;
import com.coresaken.multiplication.controller.DailyStreakController;
import com.coresaken.multiplication.controller.GoogleSignInController;
import com.coresaken.multiplication.controller.LessonController;
import com.coresaken.multiplication.controller.LocaleController;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.User;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.PlayGames;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.time.LocalDate;
import java.util.Random;

public class MainMenuActivity extends AppCompatActivity {

    private static final int RC_LEADERBOARD_UI = 9004;

    TextView tv_level, tv_exp, tv_currentStreak, tv_competition0_record, tv_correctionAmount, tv_name;
    ProgressBar pb_level, pb_level_light;
    ImageView img_calendar;

    SQLiteHelper sqLiteHelper;
    DailyStreakController dailyStreakController;
    User user;

    Dialog rateUsDialog, exitDialog;
    SharedPreferences sharedPref;
    int lastLanguageIndex;

    boolean tryToExit = false;

    GoogleSignInController googleSignInController;

    //Challenges
    ConstraintLayout cl_challenges;
    ProgressBar pb_ch_daily_learning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LocaleController.getInstance().selectLocale(this, PlayerSettings.getInstance().language);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Random random = new Random();

        tv_level = findViewById(R.id.tv_level);
        tv_exp = findViewById(R.id.tv_exp);
        pb_level = findViewById(R.id.pb_level);
        pb_level_light = findViewById(R.id.pb_level_light);
        tv_currentStreak = findViewById(R.id.tv_currentStreak);
        tv_competition0_record = findViewById(R.id.tv_competition0_record);
        tv_correctionAmount = findViewById(R.id.tv_correction_amount);
        tv_name = findViewById(R.id.tv_name);
        cl_challenges = findViewById(R.id.cl_challenges);
        pb_ch_daily_learning = findViewById(R.id.pb_ch_daily_learning);

        sqLiteHelper = new SQLiteHelper(this);
        dailyStreakController = DailyStreakController.getInstance();

        String[] welcomes = getResources().getStringArray(R.array.welcome_array);
        int randomIndex = random.nextInt(welcomes.length);
        ((TextView) findViewById(R.id.tv_welcome)).setText(welcomes[randomIndex]);

        user = User.getInstance();
        sharedPref = getSharedPreferences("User", Context.MODE_PRIVATE);
        user.exp = sharedPref.getInt("exp", 0);
        user.launchMainMenu = sharedPref.getInt("launchMainMenu", 0);
        user.rateStatus = sharedPref.getInt("rateStatus", 0);
        user.competitionRecord[0] = sharedPref.getInt("competitionRecord_0", 0);
        user.name = sharedPref.getString("name", "");
        user.adValue = sharedPref.getInt("adValue", 0);

        img_calendar = findViewById(R.id.img_calendar);

        user.launchMainMenu++;
        sharedPref.edit().putInt("launchMainMenu", user.launchMainMenu).apply();

        lastLanguageIndex = PlayerSettings.getInstance().language;

        SoundController.getInstance().initializeSounds(this, new int[] {R.raw.sound_click_button_2});

        googleSignInController = new GoogleSignInController(this);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(tryToExit){
                    finish();
                }
                else{
                    exitDialog();
                    tryToExit = true;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshLevel(User.getInstance().exp);
        int dailyStreakPercent = dailyStreakController.getPercent();
        if(dailyStreakPercent<100){
            img_calendar.setImageTintList(ColorStateList.valueOf(getColor(R.color.background_dark)));

            if(dailyStreakPercent<=8 && dailyStreakPercent > 0){
                dailyStreakPercent = 8;
            }
            pb_ch_daily_learning.setProgress(dailyStreakPercent);

            cl_challenges.setVisibility(View.VISIBLE);
        }
        else{
            img_calendar.setImageTintList(null);

            cl_challenges.setVisibility(View.GONE);
        }

        tv_currentStreak.setText(String.valueOf(dailyStreakController.getCurrentStreak()));
        sqLiteHelper.updateDailyStreak(LocalDate.now(), dailyStreakController.getTimeInSecond());

        tv_correctionAmount.setText(String.valueOf(sqLiteHelper.getTotalCorrectionEquationAmount()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(lastLanguageIndex!=PlayerSettings.getInstance().language){
            recreate();
        }

        int currentLevel = user.getCurrentLevel();

        DailyReminderController.startNotification(this);

        TextView tv_level, tv_exp;
        tv_level = findViewById(R.id.tv_level);
        tv_exp = findViewById(R.id.tv_exp);

        tv_level.setText(getText(R.string.level) +" "+currentLevel);
        tv_exp.setText(user.exp +" EXP");

        if(user.name.length()<=1){
            tv_name.setText(getString(R.string.default_name));
        }
        else{
            tv_name.setText(user.name);
        }

        refreshLevel(user.exp);

        tv_competition0_record.setText(String.valueOf(user.competitionRecord[0]));

        AdSystem.getInstance().loadInterstitialAd(this);
        AdSystem.getInstance().loadVideoAd(this);
        AdSystem.getInstance().loadRewardedAd(this);

        sharedPref.edit().putInt("adValue", user.adValue).apply();

        SoundController.getInstance().enable(PlayerSettings.getInstance().sounds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(rateUsDialog!=null){
            rateUsDialog.dismiss();
        }
    }

    public void onClick(View view){
        SoundController.getInstance().clickButton();

        if(checkRateUs()){
            return;
        }

        if(view.getId()==R.id.cl_btn_learning_multiplication){
            LessonController.getInstance().openCourse(MainMenuActivity.this, OperatorType.MULTIPLICATION);
        }else if(view.getId()==R.id.cl_btn_learning_division){
            LessonController.getInstance().openCourse(MainMenuActivity.this, OperatorType.DIVISION);
        }
    }

    public void selectGameType(View view){
        SoundController.getInstance().clickButton();

        if(checkRateUs()){
            return;
        }

        Settings presetSettings = new Settings();
        presetSettings.modeType = ModeType.GAME;

        if(view.getId()==R.id.btn_mode_0){
            presetSettings.gameType = GameType.GAME1;
        }else if(view.getId()==R.id.btn_mode_1){
            presetSettings.gameType = GameType.TRUE_FALSE;
        }else if(view.getId()==R.id.btn_mode_2){
            presetSettings.gameType = GameType.INPUT;
        }else if(view.getId()==R.id.btn_mode_3){
            presetSettings.gameType = GameType.PUZZLE;
        } else if(view.getId()==R.id.btn_mode_4){
            presetSettings.gameType = GameType.DUEL;
        }else if(view.getId()==R.id.btn_mode_5){
            presetSettings.gameType = GameType.LAVA;
        } else if(view.getId()==R.id.btn_mode_6){
            presetSettings.gameType = GameType.MATCH;
        }else if(view.getId()==R.id.btn_mode_7){
            presetSettings.gameType = GameType.CARD;
        } else if(view.getId()==R.id.btn_mode_8){
            presetSettings.gameType = GameType.DRAW;
        }

        SettingsController.getInstance().presetSettings = presetSettings;
        startActivity(new Intent(this, GameSettingsActivity.class));
    }

    public void showCalendar(View view){
        SoundController.getInstance().clickButton();

        startActivity(new Intent(this, CalendarActivity.class));
    }

    public void startCompetition(View view){
        SoundController.getInstance().clickButton();

        startActivity(new Intent(this, Competition0Activity.class));
    }

    public void openCorrection(View view){
        SoundController.getInstance().clickButton();
        SettingsController.getInstance().settings = null;

        startActivity(new Intent(this, CorrectionActivity.class));
    }

    public void showLeaderboard(View view){
        SoundController.getInstance().clickButton();

        showLeaderboard();
    }

    private void showLeaderboard() {
        PlayGames.getLeaderboardsClient(this)
                .submitScore(getString(R.string.leaderboard_math_master), user.competitionRecord[0]);

        PlayGames.getLeaderboardsClient(this)
                .getLeaderboardIntent(getString(R.string.leaderboard_math_master))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    }
                });
    }

    public void openTableList(View view){
        SoundController.getInstance().clickButton();

        if(checkRateUs()){
            return;
        }

        startActivity(new Intent(this, TableActivity.class));
    }

    public void openCalendar(View view){
        SoundController.getInstance().clickButton();

        if(checkRateUs()){
            return;
        }

        startActivity(new Intent(this, CalendarActivity.class));
    }

    public void openSettings(View view){
        SoundController.getInstance().clickButton();

        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void refreshLevel(int exp){
        int level = exp/100 + 1;
        int progress = exp%100;

        if(progress<8){
            progress = 8;
        }

        tv_level.setText(getString(R.string.level) +" "+level);
        tv_exp.setText(String.valueOf(exp));
        pb_level.setProgress(progress);
    }

    private boolean checkRateUs(){
        if(user.rateStatus==0){
            if(user.launchMainMenu%5==0){
                user.launchMainMenu++;
                rateUsDialog();

                return true;
            }
        }else if(user.rateStatus==2){
            if(user.launchMainMenu%20==0){
                user.launchMainMenu++;
                rateUsDialog();

                return true;
            }
        }

        return false;
    }
    private void rateUsDialog(){
        rateUsDialog = new Dialog(this, R.style.DimOverlay);

        rateUsDialog.setContentView(R.layout.dialog_rate_us);
        Window window = rateUsDialog.getWindow();
        window.setBackgroundDrawableResource(R.color.dialog_bg);

        ImageButton btn_close = rateUsDialog.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();

                rateUsDialog.cancel();
                rateUsDialog.dismiss();
            }
        });

        TextView tv_remindLater = rateUsDialog.findViewById(R.id.tv_remind_later);
        tv_remindLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();

                sharedPref.edit().putInt("rateStatus", 2).apply();
                user.rateStatus = 2;

                rateUsDialog.dismiss();
            }
        });

        ImageView[] stars = new ImageView[5];
        stars[0] = rateUsDialog.findViewById(R.id.img_star0);
        stars[1] = rateUsDialog.findViewById(R.id.img_star1);
        stars[2] = rateUsDialog.findViewById(R.id.img_star2);
        stars[3] = rateUsDialog.findViewById(R.id.img_star3);
        stars[4] = rateUsDialog.findViewById(R.id.img_star4);

        for (ImageView star : stars) {
            star.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SoundController.getInstance().clickButton();

                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=com.coresaken.multiplication")));
                    } catch (android.content.ActivityNotFoundException e) {
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://play.google.com/store/apps/details?id=com.coresaken.multiplication")));
                    }

                    sharedPref.edit().putInt("rateStatus", 1).apply();
                    user.rateStatus = 1;

                    rateUsDialog.dismiss();
                }
            });
        }

        rateUsDialog.show();
    }

    private void exitDialog(){
        exitDialog = new Dialog(this, R.style.DimOverlay);

        exitDialog.setContentView(R.layout.dialog_exit_app);
        Window window = exitDialog.getWindow();
        window.setBackgroundDrawableResource(R.color.dialog_bg);

        Button btn_yes = exitDialog.findViewById(R.id.btn_yes);
        btn_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();

                exitDialog.dismiss();
                finish();
            }
        });

        Button btn_no = exitDialog.findViewById(R.id.btn_no);
        btn_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();
                tryToExit = false;

                exitDialog.cancel();
                exitDialog.dismiss();
            }
        });

        ImageButton btn_close = exitDialog.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();
                tryToExit = false;

                exitDialog.cancel();
                exitDialog.dismiss();
            }
        });

        exitDialog.show();
    }
}