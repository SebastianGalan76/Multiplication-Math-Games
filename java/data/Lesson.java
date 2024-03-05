package com.coresaken.multiplication.data;

import android.content.Context;
import android.util.Log;

import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.service.JsonFileReader;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Lesson {
    public OperatorType operatorType;

    public int number;
    public int starAmount;
    public boolean unlocked;
    public HashMap<Integer, Exercise> exercises;
    public List<Equation> equations;

    public Lesson(int number, boolean unlocked, OperatorType operatorType){
        this.number = number;
        this.unlocked = unlocked;
        this.operatorType = operatorType;

        exercises = new HashMap<>();
        equations = new ArrayList<>();
    }

    public void changeExercisesProgress(Context context, int exerciseIndex, int progress){
        Log.d("UpdateProgress", "UpdateProgress (Lesson.java) "+progress);

        Exercise exercise = exercises.get(exerciseIndex);
        if(exercise!=null){
            exercise.progress += progress;

            if(exercise.progress >= 100){
                Exercise nextExercise = exercises.get(exerciseIndex+1);

                if(nextExercise!=null){
                    nextExercise.unlocked = true;
                }
            }

            updateLesson(context);
        }
    }

    public void updateLesson(Context context){
        calculateStarAmount();

        JsonFileReader.updateLesson(context, this);
    }
    public void addExercise(int exerciseIndex, Exercise exercise){
        exercises.put(exerciseIndex, exercise);
    }

    public JSONObject getJSONObject(){
        JSONObject obj = new JSONObject();

        try{
            obj.put("id", number);
            obj.put("unlocked", unlocked);

            JSONArray array = new JSONArray();

            for(Exercise exercise:exercises.values()){
                array.put(exercise.getJSONObject());
            }

            obj.put("exercises", array);
        }catch (JSONException e){
            e.printStackTrace();
        }

        return obj;
    }
    public void calculateStarAmount(){
        starAmount = 0;

        for(Equation equation:equations){
            starAmount += Utils.getStarAmount(equation.getPoints());
        }
    }

    public void calculateStarAmount(SQLiteHelper sqLiteHelper){
        starAmount = 0;

        for(Equation equation:equations){
            starAmount += Utils.getStarAmount(sqLiteHelper.getPoints(equation.getId()));
        }
    }

    public static class Exercise {
        public int id;

        public boolean unlocked;
        public int progress;

        public Exercise(int id, boolean unlocked, int progress){
            this.id = id;
            this.unlocked = unlocked;
            this.progress = progress;
        }

        public JSONObject getJSONObject(){
            JSONObject obj = new JSONObject();

            try{
                obj.put("id", id);
                obj.put("unlocked", unlocked);
                obj.put("progress", progress);
            }catch (JSONException e){
                e.printStackTrace();
            }

            return obj;
        }
    }
}
