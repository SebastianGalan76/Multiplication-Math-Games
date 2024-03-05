package com.coresaken.multiplication.controller.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.GameSettingsActivity;
import com.coresaken.multiplication.activity.LearnPathActivity;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.LessonController;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.RangeType;
import com.coresaken.multiplication.service.JsonFileReader;
import com.coresaken.multiplication.util.Utils;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.rewarded.RewardItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class LessonAdapter extends RecyclerView.Adapter<LessonAdapter.ViewHolder> {

    public List<ViewHolder> viewHolders;

    private final List<Lesson> lessons;
    LearnPathActivity.LessonColor[] colors;

    private final Context context;
    private final Activity activity;

    int viewIndex = 0;

    public int height;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        Lesson lesson;

        TextView[] equations;
        FrameLayout[] exerciseButtons;
        View exercisesButton;

        TextView tv_header_number;

        //Change color view
        View cl_equation, cl_learnPath, cl_number;

        View cl_locked;

        private Context context;
        private Activity activity;

        public LearnPathActivity.LessonColor color, lockColor;


        public ViewHolder(View view, int viewIndex) {
            super(view);

            ConstraintLayout inflatedLayout;
            if(viewIndex%2==0){
                inflatedLayout = (ConstraintLayout) LayoutInflater.from(view.getContext()).inflate(R.layout.learn_path_left, null);
            }
            else{
                inflatedLayout = (ConstraintLayout) LayoutInflater.from(view.getContext()).inflate(R.layout.learn_path_right, null);
            }
            ConstraintLayout container = view.findViewById(R.id.cl_container);

            ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
            );
            inflatedLayout.setLayoutParams(layoutParams);
            container.addView(inflatedLayout);

            tv_header_number = view.findViewById(R.id.tv_number);

            equations = new TextView[11];
            equations[0] = view.findViewById(R.id.tv_equation_0);
            equations[1] = view.findViewById(R.id.tv_equation_1);
            equations[2] = view.findViewById(R.id.tv_equation_2);
            equations[3] = view.findViewById(R.id.tv_equation_3);
            equations[4] = view.findViewById(R.id.tv_equation_4);
            equations[5] = view.findViewById(R.id.tv_equation_5);
            equations[6] = view.findViewById(R.id.tv_equation_6);
            equations[7] = view.findViewById(R.id.tv_equation_7);
            equations[8] = view.findViewById(R.id.tv_equation_8);
            equations[9] = view.findViewById(R.id.tv_equation_9);
            equations[10] = view.findViewById(R.id.tv_equation_10);

            exerciseButtons = new FrameLayout[8];
            exerciseButtons[0] = view.findViewById(R.id.btn_exercise_0);
            exerciseButtons[1] = view.findViewById(R.id.btn_exercise_1);
            exerciseButtons[2] = view.findViewById(R.id.btn_exercise_2);
            exerciseButtons[3] = view.findViewById(R.id.btn_exercise_3);
            exerciseButtons[4] = view.findViewById(R.id.btn_exercise_4);
            exerciseButtons[5] = view.findViewById(R.id.btn_exercise_5);
            exerciseButtons[6] = view.findViewById(R.id.btn_exercise_6);
            exerciseButtons[7] = view.findViewById(R.id.btn_exercise_7);
            exercisesButton = view.findViewById(R.id.btn_exercises);

            for(int i=0;i<exerciseButtons.length;i++){
                final int index = i;
                exerciseButtons[i].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openExercise(view.getContext(), index);
                    }
                });
            }
            exercisesButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    openExercisesSettings(context);
                }
            });


            cl_equation = view.findViewById(R.id.cl_equations);
            cl_learnPath = view.findViewById(R.id.cl_learn_path);
            cl_number = view.findViewById(R.id.cl_number);
            cl_locked = view.findViewById(R.id.cl_locked);
        }

        public void onBindView(Context context, Activity activity, Lesson lesson, LearnPathActivity.LessonColor color, LearnPathActivity.LessonColor lockColor){
            this.lesson = lesson;
            this.context = context;
            this.activity = activity;
            this.color = color;
            this.lockColor = lockColor;

            tv_header_number.setText(String.valueOf(lesson.number));

            for(int i=0;i<equations.length;i++){
                Equation equation = lesson.equations.get(i);
                equations[i].setText(Utils.convertOperatorSign(equation.toString()));
            }

            refreshFragment();
        }
        public void refreshFragment(){
            HashMap<Integer, Lesson.Exercise> exercises = lesson.exercises;

            boolean insertNewExercise = false;
            for(int i=0;i<exerciseButtons.length;i++){
                Lesson.Exercise exercise;

                if(i<exercises.size()){
                    exercise = exercises.get(i);
                }
                else{
                    boolean previousExerciseIsFinished = true;
                    if(i>=1){
                        previousExerciseIsFinished = exercises.get(i-1).progress >= 100;
                    }

                    exercise = new Lesson.Exercise(i, previousExerciseIsFinished, 0);
                    insertNewExercise = true;
                    lesson.addExercise(i, exercise);
                }

                if(exercise==null){
                    Log.d("ERROR", "LearnPathFragment 123");

                    continue;
                }

                exerciseButtons[i].removeAllViews();
                if(exercise.progress >= 100){
                    ConstraintLayout cl_finished = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.lesson_finished, null);

                    exerciseButtons[i].addView(cl_finished);
                }
                else{
                    if(exercise.unlocked && exercise.progress>0){
                        ConstraintLayout cl_inProgress = (ConstraintLayout) LayoutInflater.from(context).inflate(R.layout.lesson_in_progress, null);

                        ProgressBar pb = cl_inProgress.findViewById(R.id.pb_lesson);
                        pb.setProgress(exercise.progress);
                        exerciseButtons[i].addView(cl_inProgress);
                    }
                }

                if(exercise.unlocked){
                    exerciseButtons[i].setBackground(color.button);
                }
                else{
                    exerciseButtons[i].setBackground(lockColor.button);
                }
            }

            Lesson.Exercise lastExercise = lesson.exercises.get(lesson.exercises.size() - 1);
            if(lastExercise!=null && lastExercise.progress >= 100){
                exercisesButton.setBackground(color.bigButton);
            }
            else{
                exercisesButton.setBackground(lockColor.bigButton);
            }

            if(lesson.unlocked){
                cl_locked.setVisibility(View.GONE);
            }
            else{
                cl_locked.setVisibility(View.VISIBLE);

                cl_locked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tryToUnlockLesson();
                    }
                });
            }

            if(insertNewExercise){
                JsonFileReader.updateLesson(context, lesson);
            }

            changeColor(color, lockColor);
        }
        private void openExercise(Context context, int exerciseIndex){
            if(lesson.exercises.get(exerciseIndex).unlocked){
                SoundController.getInstance().clickButton();

                LessonController.getInstance().loadSettings(lesson, exerciseIndex, lesson.operatorType);
                context.startActivity(new Intent(context, SettingsController.getInstance().getActivity()));
            }
        }

        private void tryToUnlockLesson(){
            SoundController.getInstance().clickButton();
            AdSystem.getInstance().showRewardedAd(activity, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    unlockLesson();
                }
            });
        }

        private void unlockLesson(){
            lesson.unlocked = true;
            refreshFragment();

            JsonFileReader.updateLesson(context, lesson);
        }

        private void openExercisesSettings(Context context){
            if(lesson.exercises.get(lesson.exercises.size()-1).progress>=100){
                SoundController.getInstance().clickButton();
                Settings presetSettings = new Settings();

                presetSettings.operators = Collections.singletonList(lesson.operatorType);
                presetSettings.rangeType = RangeType.AB;
                presetSettings.aMin = lesson.number;
                presetSettings.aMax = lesson.number;
                presetSettings.bMin = 0;
                presetSettings.bMax = 10;

                SettingsController.getInstance().presetSettings = presetSettings;
                context.startActivity(new Intent(context, GameSettingsActivity.class));
            }
        }

        public void changeColor(LearnPathActivity.LessonColor color, LearnPathActivity.LessonColor lockColor){
            if(lesson.unlocked){
                cl_equation.setBackgroundTintList(ColorStateList.valueOf(color.color));
                cl_learnPath.setBackgroundColor(color.colorBg);

                int buttonIndex = 0;
                for(FrameLayout btn : exerciseButtons){
                    if(lesson.exercises.size() > buttonIndex && lesson.exercises.get(buttonIndex).unlocked){
                        btn.setBackground(color.button);
                    }
                    else{
                        btn.setBackground(lockColor.button);
                    }
                    buttonIndex++;
                }

                cl_number.setBackground(color.header);

                Lesson.Exercise lastExercise = lesson.exercises.get(lesson.exercises.size() - 1);
                if(lastExercise!=null && lastExercise.progress >= 100){
                    exercisesButton.setBackground(color.bigButton);
                }
                else{
                    exercisesButton.setBackground(lockColor.bigButton);
                }
            }
            else{
                cl_equation.setBackgroundTintList(ColorStateList.valueOf(lockColor.color));
                cl_learnPath.setBackgroundColor(lockColor.colorBg);

                for(FrameLayout btn : exerciseButtons){
                    btn.setBackground(lockColor.button);
                }

                cl_number.setBackground(lockColor.header);
                exercisesButton.setBackground(lockColor.bigButton);
            }
        }
    }

    public LessonAdapter(List<Lesson> dataSet, Context context, Activity activity, LearnPathActivity.LessonColor[] colors) {
        lessons = dataSet;
        this.context = context;
        this.colors = colors;
        this.activity = activity;

        viewHolders = new ArrayList<>();
    }

    public void refreshLessons(){
        for(ViewHolder vh:viewHolders){
            vh.refreshFragment();
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_learn_path, viewGroup, false);

        viewIndex++;
        ViewHolder viewHolder = new ViewHolder(view, viewIndex);
        viewHolders.add(viewHolder);

        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        height = view.getMeasuredHeight();

        return viewHolder;
    }
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        Lesson lesson = lessons.get(position);
        int colorIndex = (lesson.number - 1)%10;

        viewHolder.onBindView(context, activity, lesson, colors[colorIndex], colors[colors.length-1]);
    }
    @Override
    public int getItemCount() {
        if(lessons==null){
            return 0;
        }

        return lessons.size();
    }
}

