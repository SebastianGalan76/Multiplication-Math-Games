package com.coresaken.multiplication.fragment.main_menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.adapter.TextAdapter;
import com.coresaken.multiplication.data.User;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class SelectAgeFragment extends Fragment {

    Button btn_continue, btn_confirm;
    Spinner daySpinner, monthSpinner, yearSpinner;
    int changeDateOfBirth = 5;

    public int selectedDay = 1, selectedMonth = 1, selectedYear = Calendar.YEAR;

    public static SelectAgeFragment newInstance() {
        return new SelectAgeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_select_age, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_continue = view.findViewById(R.id.btn_continue);
        btn_confirm = view.findViewById(R.id.btn_confirm);

        daySpinner = view.findViewById(R.id.s_day);
        monthSpinner = view.findViewById(R.id.s_month);
        yearSpinner = view.findViewById(R.id.s_year);

        String[] days = getDaysNumber(0, Calendar.YEAR);
        String[] months = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        String[] years = new String[100];

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int year = currentYear;
        for(int i=0;i<years.length;i++){
            years[i] = String.valueOf(year);
            year--;
        }

        TextAdapter dayAdapter = new TextAdapter(view.getContext(), days);
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedDay = i+1;

                changeDateOfBirth--;
                if(changeDateOfBirth<=0){
                    btn_confirm.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        TextAdapter monthAdapter = new TextAdapter(view.getContext(), months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedMonth = i+1;

                String[] days = getDaysNumber(i, selectedYear);
                TextAdapter dayAdapter = new TextAdapter(view.getContext(), days);
                daySpinner.setAdapter(dayAdapter);

                if(selectedDay>getNumberOfDays(i, selectedYear-1)){
                    daySpinner.setSelection(0);
                    selectedDay = 1;
                }
                else{
                    daySpinner.setSelection(selectedDay-1);
                }

                changeDateOfBirth--;
                if(changeDateOfBirth<=0){
                    btn_confirm.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        TextAdapter yearAdapter = new TextAdapter(view.getContext(), years);
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedYear = currentYear - i;

                String[] days = getDaysNumber(selectedMonth - 1, selectedYear);
                TextAdapter dayAdapter = new TextAdapter(view.getContext(), days);
                daySpinner.setAdapter(dayAdapter);

                if(selectedDay>getNumberOfDays(selectedMonth-1, selectedYear-1)){
                    daySpinner.setSelection(0);
                    selectedDay = 1;
                }
                else{
                    daySpinner.setSelection(selectedDay-1);
                }

                changeDateOfBirth--;
                if(changeDateOfBirth<=0){
                    btn_confirm.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public void finishOperation(){
        btn_continue.setVisibility(View.INVISIBLE);
        btn_confirm.setVisibility(View.INVISIBLE);
    }

    private String[] getDaysNumber(int monthIndex, int yearIndex){
        int numberOfDays = getNumberOfDays(monthIndex, yearIndex);

        String[] days = new String[numberOfDays];
        for(int i=0;i<numberOfDays;i++){
            days[i] = String.valueOf(i+1);
        }

        return days;
    }

    private int getNumberOfDays(int month, int year){
        Calendar calendar = new GregorianCalendar(year, month, 1);
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    }
}