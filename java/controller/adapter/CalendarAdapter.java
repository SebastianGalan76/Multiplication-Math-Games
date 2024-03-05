package com.coresaken.multiplication.controller.adapter;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.coresaken.multiplication.R;

import java.time.LocalDate;
import java.util.ArrayList;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final ArrayList<LocalDate> days;
    private final int[] dailyTime;
    private Resources resources;

    public CalendarAdapter.CalendarViewHolder todayViewHolder;

    int cellSize;

    public CalendarAdapter(ArrayList<LocalDate> days, int[] dailyTime) {
        this.days = days;
        this.dailyTime = dailyTime;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.calendar_cell, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        cellSize = (int) (parent.getWidth()/7);
        layoutParams.width = cellSize;
        layoutParams.height = cellSize;

        resources = view.getResources();

        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        LocalDate date = days.get(position);
        if(date!=null){
            if(date.isEqual(LocalDate.now())){
                todayViewHolder = holder;
            }

            if(days.size()>10){
                holder.initialize(resources, date.getDayOfMonth(), dailyTime[date.getDayOfMonth()]);
            }
            else{
                holder.initialize(resources, date.getDayOfMonth(), dailyTime[position]);
            }
        }
        else{
            holder.changeBackgroundColor(resources);
            holder.dayOfMonth.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder{
        public final View cell;
        public final TextView dayOfMonth;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            cell = itemView.findViewById(R.id.cl_cell);
            dayOfMonth = itemView.findViewById(R.id.cellDayText);
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void changeBackgroundColor(Resources res){
            cell.setBackground(res.getDrawable(R.drawable.calendar_cell_gray));
        }

        public void initialize(Resources res, int dayNumber, int time){
            if(time > 300){
                makeCellGreen(res);
            }
            else{
                dayOfMonth.setText(String.valueOf(dayNumber));
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        public void makeCellGreen(Resources res){
            cell.setBackground(res.getDrawable(R.drawable.calendar_cell_green));
            dayOfMonth.setText("");
        }
    }
}
