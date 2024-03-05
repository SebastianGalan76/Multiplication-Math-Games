package com.coresaken.multiplication.fragment.main_menu;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.LearnPathActivity;
import com.coresaken.multiplication.controller.LessonController;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.listener.ViewCreatedListener;
import com.coresaken.multiplication.service.JsonFileReader;

import java.util.HashMap;
import java.util.List;

public class LearnPathFragment extends Fragment {
    LearnPathActivity.LessonColor color;
    LearnPathActivity.LessonColor lockColor;

    Lesson lesson;

    TextView[] equations;
    FrameLayout[] exerciseButtons;
    View exercisesButton;

    ViewCreatedListener viewCreatedListener;

    public void setViewCreatedListener(ViewCreatedListener viewCreatedListener){
        this.viewCreatedListener = viewCreatedListener;
    }

    public void initializeLesson(Lesson lesson){
        this.lesson = lesson;
    }

    public static LearnPathFragment newInstance() {
        return new LearnPathFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_learn_path, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ConstraintLayout inflatedLayout;
        if(lesson.number%2==0){
            inflatedLayout = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.learn_path_left, null);
        }
        else{
            inflatedLayout = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.learn_path_right, null);
        }

        // Pobierz referencję do głównego ConstraintLayout, do którego chcesz dodać nadmuchane (inflated) ConstraintLayout
        ConstraintLayout container = view.findViewById(R.id.cl_container); // Załóżmy, że masz zdefiniowany ConstraintLayout w swoim widoku

        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        );
        inflatedLayout.setLayoutParams(layoutParams);
        container.addView(inflatedLayout);

        TextView tv_header_number = view.findViewById(R.id.tv_number);
        tv_header_number.setText(String.valueOf(lesson.number));

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

        for(int i=0;i<equations.length;i++){
            int c = i * lesson.number;

            equations[i].setText(lesson.number+" * "+i+" = "+c);
        }

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
                    openExercise(index);
                }
            });
        }

        if(viewCreatedListener!=null){
            viewCreatedListener.onViewCreated();
        }

        refreshFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

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
                Log.d("XD", "LearnPathFragment 123");

                continue;
            }

            exerciseButtons[i].removeAllViews();
            if(exercise.progress >= 100){
                ConstraintLayout cl_finished = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.lesson_finished, null);

                exerciseButtons[i].addView(cl_finished);
            }
            else{
                if(exercise.unlocked && exercise.progress>0){
                    ConstraintLayout cl_inProgress = (ConstraintLayout) LayoutInflater.from(getContext()).inflate(R.layout.lesson_in_progress, null);

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

        if(insertNewExercise){
            JsonFileReader.updateLesson(getContext(), lesson);
        }
    }

    private void openExercise(int exerciseIndex){
        if(lesson.exercises.get(exerciseIndex).unlocked){
            LessonController.getInstance().loadSettings(lesson, exerciseIndex, OperatorType.MULTIPLICATION);
            startActivity(new Intent(getActivity(), SettingsController.getInstance().getActivity()));
        }
    }

    private void unlockLesson(){

    }
}