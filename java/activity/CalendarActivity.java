package com.coresaken.multiplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.DailyStreakController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.controller.adapter.CalendarAdapter;
import com.coresaken.multiplication.service.SQLiteHelper;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class CalendarActivity extends AppCompatActivity{

    TextView monthYearText;
    RecyclerView calendarRecyclerView;
    LocalDate selectedDate;

    LinearLayout ll_calendarContainer;
    ConstraintLayout cl_calendarWrapper;
    SQLiteHelper sqLiteHelper;

    ImageButton btn_nextMonth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        monthYearText = findViewById(R.id.month_id);
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        ll_calendarContainer = findViewById(R.id.ll_calendar_container);
        cl_calendarWrapper = findViewById(R.id.cl_calendar_wrapper);
        btn_nextMonth = findViewById(R.id.btn_next_month);
        selectedDate = LocalDate.now();

        TextView tv_totalStreakDays = findViewById(R.id.tv_totalStreakDays);
        TextView tv_longestStreak = findViewById(R.id.tv_longestStreak);
        TextView tv_currentStreak = findViewById(R.id.tv_currentStreak);

        DailyStreakController dailyStreakController = DailyStreakController.getInstance();

        tv_totalStreakDays.setText(String.valueOf(dailyStreakController.getTotalStreakDays()));
        tv_longestStreak.setText(String.valueOf(dailyStreakController.getLongestStreak()));
        tv_currentStreak.setText(String.valueOf(dailyStreakController.getCurrentStreak()));

        sqLiteHelper = new SQLiteHelper(this);

        setMonthView();
    }

    private void setMonthView(){
        LocalDate nextMonth = selectedDate.plusMonths(1);
        if(nextMonth.isAfter(LocalDate.now())){
            btn_nextMonth.setVisibility(View.INVISIBLE);
        }
        else{
            btn_nextMonth.setVisibility(View.VISIBLE);
        }

        monthYearText.setText(monthYearFromDate(selectedDate));

        int[] dailyTime = sqLiteHelper.getDailyStreakForMonth(selectedDate);
        ArrayList<LocalDate> daysInMonth = daysInMonthArray(selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(daysInMonth, dailyTime);
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
                        int cellSize =firstCell.getWidth();
                        int totalHeight = cellSize * 6;

                        ViewGroup.LayoutParams params = calendarRecyclerView.getLayoutParams();
                        params.height = totalHeight;
                        calendarRecyclerView.setLayoutParams(params);
                    }

                    cl_calendarWrapper.requestLayout();
                }
            }
        });
    }

    private ArrayList<LocalDate> daysInMonthArray(LocalDate selectedDate) {
        ArrayList<LocalDate> daysInMonthArray = new ArrayList<>();
        YearMonth yearMonth = YearMonth.from(selectedDate);

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstOfMonth = selectedDate.withDayOfMonth(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue();

        dayOfWeek -= 1;

        for(int i=0;i<42; i++){
            //Dni przed 1 dniem miesiąca
            if(i<dayOfWeek){
                daysInMonthArray.add(null);
            }
            else if(i + 1 > daysInMonth + dayOfWeek){
                daysInMonthArray.add(null);
            }
            else{
                daysInMonthArray.add(LocalDate.of(selectedDate.getYear(), selectedDate.getMonth(), i+1 - dayOfWeek));
            }
        }

        return daysInMonthArray;
    }

    private String monthYearFromDate(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");

        return date.format(formatter);
    }

    public void previousMonth(View view){
        SoundController.getInstance().clickButton();

        selectedDate = selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonth(View view){
        SoundController.getInstance().clickButton();

        LocalDate nextMonth = selectedDate.plusMonths(1);
        if(nextMonth.isAfter(LocalDate.now())){
           return;
        }

        selectedDate = nextMonth;
        setMonthView();
    }

    public void closeActivity(View view){
        SoundController.getInstance().clickButton();

        finish();
    }
}