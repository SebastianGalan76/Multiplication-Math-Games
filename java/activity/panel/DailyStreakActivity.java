package com.coresaken.multiplication.activity.panel;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.StrictMode;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.airbnb.lottie.LottieAnimationView;
import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.DailyStreakController;
import com.coresaken.multiplication.controller.PanelController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.controller.adapter.CalendarAdapter;
import com.coresaken.multiplication.data.Panel;
import com.coresaken.multiplication.service.SQLiteHelper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class DailyStreakActivity extends Panel {

    LottieAnimationView lottie_calendar, lottie_confetti;

    RecyclerView calendarRecyclerView;
    LocalDate selectedDate;

    LinearLayout ll_calendarContainer;
    ConstraintLayout cl_calendarWrapper;

    ProgressBar pb_progress, pb_progress_light;

    SQLiteHelper sqLiteHelper;
    CalendarAdapter calendarAdapter;

    Timer t;
    int counter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_streak);

        lottie_calendar = findViewById(R.id.lottie_calendar);
        lottie_confetti = findViewById(R.id.lottie_confetti);

        lottie_calendar.setProgress(0.08f);

        sqLiteHelper = new SQLiteHelper(this);

        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        ll_calendarContainer = findViewById(R.id.ll_calendar_container);
        cl_calendarWrapper = findViewById(R.id.cl_calendar_wrapper);
        selectedDate = LocalDate.now();

        pb_progress = findViewById(R.id.pb_progress);
        pb_progress_light = findViewById(R.id.pb_progress_light);

        pb_progress_light.setMax(108);

        new CountDownTimer(500, 500){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                lottie_calendar.playAnimation();
                lottie_calendar.setProgress(0.08f);
            }
        }.start();

        setWeekView();
        changeProgressBar();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closePanel(null);
            }
        });
    }

    public void changeProgressBar(){
        DailyStreakController streakController = DailyStreakController.getInstance();
        final int startPercent = streakController.getLastShowedPercent();
        int endPercent = streakController.getPercent();

        sqLiteHelper.updateDailyStreak(LocalDate.now(), streakController.getTimeInSecond());

        pb_progress.setProgress(startPercent);
        pb_progress_light.setProgress(startPercent);

        t = new Timer();
        counter = startPercent;
        if(endPercent<8){
            endPercent = 8;
        }

        final int endPercentFinal = endPercent;

        TimerTask tt = new TimerTask() {
                    @Override
                    public void run() {
                        // Tutaj umieść kod do aktualizacji interfejsu użytkownika
                        counter++;
                        pb_progress.setProgress(counter);
                        pb_progress_light.setProgress(counter - 5);

                        if(counter>=100){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if(calendarAdapter!=null){
                                        if(calendarAdapter.todayViewHolder!=null){
                                            calendarAdapter.todayViewHolder.makeCellGreen(getResources());
                                            lottie_confetti.playAnimation();
                                        }
                                    }
                                }
                            });

                            t.cancel();
                            return;
                        }

                        if(counter>=endPercentFinal){
                            t.cancel();
                        }
                    }
        };

        t.schedule(tt, 1000, 10);
    }

    private void setWeekView(){
        int[] dailyTime = sqLiteHelper.getDailyStreakForWeek(mondayForDate(selectedDate));
        ArrayList<LocalDate> daysInWeek = daysInWeekArray(selectedDate);
        calendarAdapter = new CalendarAdapter(daysInWeek, dailyTime);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);

        calendarRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                RecyclerView.LayoutManager layoutManager = calendarRecyclerView.getLayoutManager();
                if (layoutManager != null) {
                    View firstCell = calendarRecyclerView.getChildAt(0);

                    if(firstCell!=null){
                        int cellSize = firstCell.getWidth();

                        ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
                        params.height = cellSize;
                        calendarRecyclerView.setLayoutParams(params);
                    }

                    cl_calendarWrapper.requestLayout();
                }
            }
        });
    }
    private ArrayList<LocalDate> daysInWeekArray(LocalDate selectedDate){
        ArrayList<LocalDate> days = new ArrayList<>();
        LocalDate current = mondayForDate(selectedDate);
        LocalDate endDay = current.plusWeeks(1);

        while(current.isBefore(endDay)){
            days.add(current);

            current = current.plusDays(1);
        }

        return days;
    }
    private LocalDate mondayForDate(LocalDate current) {
        LocalDate oneWeekAgo = current.minusWeeks(1);

        while(current.isAfter(oneWeekAgo)){
            if(current.getDayOfWeek() == DayOfWeek.MONDAY){
                return current;
            }

            current = current.minusDays(1);
        }

        return null;
    }

    public void closePanel(View view){
        SoundController.getInstance().clickButton();

        startActivity(new Intent(this, PanelController.getInstance().getNextPanel()));
        finish();
    }
}