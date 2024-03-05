package com.coresaken.multiplication.fragment.game;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.HeartLimit;
import com.coresaken.multiplication.controller.TimeLimit;
import com.coresaken.multiplication.data.GameStatistic;
import com.coresaken.multiplication.data.enums.LimitType;
import com.coresaken.multiplication.listener.ViewCreatedListener;
import com.coresaken.multiplication.util.Utils;

public class GameUpPanelFragment extends Fragment {

    ConstraintLayout cl_time;
    ConstraintLayout cl_heart;
    ConstraintLayout cl_progressBar;

    LimitType limitType;

    TextView tv_timer;
    TextView tv_heartAmount;

    ProgressBar pb_progress;
    ProgressBar pb_progress_light;

    ViewCreatedListener listener;

    LottieAnimationView progressBarBurst;

    private boolean loadProgressBar;

    public GameUpPanelFragment() {
    }

    public static GameUpPanelFragment newInstance() {
        return new GameUpPanelFragment();
    }
    public void setListener(ViewCreatedListener listener){
        this.listener = listener;
    }
    public void initialize(LimitType limitType){
        this.limitType = limitType;
    }

    public void loadProgressBar(){
        loadProgressBar = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_up_panel, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(limitType == LimitType.TIME){
            initializeTimeLimit(view);
        }
        else if(limitType == LimitType.HEART){
            initializeHeartLimit(view);
        }
        else if(limitType == LimitType.EQUATION || limitType == LimitType.EQUATION_HEART){
            initializeEquationLimit(view);

            if(limitType==LimitType.EQUATION_HEART){
                initializeHeartLimit(view);
            }
        }

        if(loadProgressBar && pb_progress==null){
            initializeEquationLimit(view);
        }

        if(listener!=null){
            listener.onViewCreated();
        }
    }

    public void changeProgressBar(GameStatistic statistic){
        if(pb_progress==null){
            return;
        }

        int percent = statistic.getFinishProgressPercent();

        if(percent>0){
            percent+=8;
        }

        if(pb_progress.getProgress()<=percent){
            int left = cl_progressBar.getLeft() + Utils.convertDPToPixels(getResources(), 16);
            int right = cl_progressBar.getRight() - Utils.convertDPToPixels(getResources(), 16);

            int distance = right - left;
            distance = distance * percent/108;

            // Utwórz obiekt LayoutParams
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) progressBarBurst.getLayoutParams();
            params.leftMargin = distance - Utils.convertDPToPixels(getResources(), 14);
            progressBarBurst.setLayoutParams(params);
            progressBarBurst.playAnimation();
        }

        pb_progress.setProgress(percent, true);

        if(percent>=10){
            pb_progress_light.setProgress(percent - 5, true);
        }
        else{
            pb_progress_light.setProgress(0, true);
        }
    }

    @SuppressLint("SetTextI18n")
    public void changeTime(long timeLong){
        int secondsLeft = (int)timeLong/1000;

        if(secondsLeft>=10){
            tv_timer.setText("00:"+secondsLeft);
        }
        else{
            tv_timer.setText("00:0"+secondsLeft);
        }
    }
    public void changeHeartAmount(int amount){
        tv_heartAmount.setText(String.valueOf(amount));
    }

    public void refreshPanel(HeartLimit heartLimit, TimeLimit timeLimit){
        if(heartLimit!=null){
            tv_heartAmount.setText(String.valueOf(heartLimit.heartAmount));
        }
        if(timeLimit!=null){
            tv_timer.setText(String.valueOf(timeLimit.getTimeLeft()));
        }
    }

    private void initializeHeartLimit(View view){
        cl_heart = view.findViewById(R.id.cl_heart_wrapper);
        cl_heart.setVisibility(View.VISIBLE);

        tv_heartAmount = view.findViewById(R.id.tv_heart_amount);
    }
    private void initializeTimeLimit(View view){
        cl_time = view.findViewById(R.id.cl_time_wrapper);
        tv_timer = view.findViewById(R.id.tv_timer);

        cl_time.setVisibility(View.VISIBLE);
    }
    private void initializeEquationLimit(View view){
        cl_progressBar = view.findViewById(R.id.cl_progressBar);
        cl_progressBar.setVisibility(View.VISIBLE);

        pb_progress = view.findViewById(R.id.pb_progress);
        pb_progress_light = view.findViewById(R.id.pb_progress_light);
        progressBarBurst = view.findViewById(R.id.lottie_progressBar);

        pb_progress.setMax(108);
        pb_progress_light.setMax(108);
    }
}