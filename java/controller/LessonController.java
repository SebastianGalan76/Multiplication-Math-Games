package com.coresaken.multiplication.controller;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.coresaken.multiplication.activity.LearnPathActivity;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.AnswerType;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.LimitType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.data.enums.RangeType;
import com.coresaken.multiplication.service.JsonFileReader;
import com.coresaken.multiplication.service.SQLiteHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LessonController{
    private static LessonController _instance;
    private final EquationController equationController;

    public List<Lesson> multiplicationLessons;
    public List<Lesson> divisionLessons;

    public OperatorType openedCourseOperatorType;

    private LessonController(){
        equationController = new EquationController();
    }

    public void loadLessonsFromFile(Context context){
        SQLiteHelper sqLiteHelper = new SQLiteHelper(context);
        JSONObject fileData = JsonFileReader.getFileDate(context);

        multiplicationLessons = new ArrayList<>();
        divisionLessons = new ArrayList<>();

        try{
            JSONArray coursesJSONArray = fileData.getJSONArray("courses");

            for(int o=0;o<coursesJSONArray.length();o++){
                JSONObject courseJSON = coursesJSONArray.getJSONObject(o);

                int operatorIndex = courseJSON.getInt("operator");
                OperatorType currentCourseOperator = null;
                if(operatorIndex == OperatorType.MULTIPLICATION.index){
                    currentCourseOperator = OperatorType.MULTIPLICATION;
                }
                if(operatorIndex == OperatorType.DIVISION.index){
                    currentCourseOperator = OperatorType.DIVISION;
                }

                JSONArray lessonJSONArray = courseJSON.getJSONArray("lessons");
                Lesson[] lessonArray = new Lesson[lessonJSONArray.length()];

                for(int i=0;i<lessonJSONArray.length();i++){
                    JSONObject lessonJSON = lessonJSONArray.getJSONObject(i);

                    int number = lessonJSON.getInt("id");
                    boolean unlocked = lessonJSON.getBoolean("unlocked");
                    Lesson lesson = new Lesson(number, unlocked, currentCourseOperator);

                    JSONArray exerciseArray = lessonJSON.getJSONArray("exercises");
                    for(int j=0;j<exerciseArray.length();j++){
                        JSONObject exerciseJSON = exerciseArray.getJSONObject(j);

                        int idE = exerciseJSON.getInt("id");
                        boolean unlockedE = exerciseJSON.getBoolean("unlocked");
                        int progressE = exerciseJSON.getInt("progress");

                        lesson.addExercise(idE, new Lesson.Exercise(idE, unlockedE, progressE));
                    }

                    lesson.equations = loadEquationsForLesson(sqLiteHelper, lesson.number, currentCourseOperator);
                    lesson.calculateStarAmount();

                    lessonArray[lesson.number - 1] = lesson;
                }

                if(currentCourseOperator == OperatorType.MULTIPLICATION){
                    multiplicationLessons.addAll(Arrays.asList(lessonArray));
                }
                else if(currentCourseOperator == OperatorType.DIVISION){
                    divisionLessons.addAll(Arrays.asList(lessonArray));
                }
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        if(multiplicationLessons.get(multiplicationLessons.size()-1).unlocked){
            createNewLesson(context, sqLiteHelper,multiplicationLessons.size() + 1, OperatorType.MULTIPLICATION);
        }
        if(divisionLessons.get(divisionLessons.size()-1).unlocked){
            createNewLesson(context, sqLiteHelper,divisionLessons.size() + 1, OperatorType.DIVISION);
        }
    }

    private void createNewLesson(Context context, SQLiteHelper sqLiteHelper, int number, OperatorType operatorType){
        Lesson newLesson = new Lesson(number, false, operatorType);
        newLesson.equations = loadEquationsForLesson(sqLiteHelper, number, operatorType);

        if(operatorType==OperatorType.MULTIPLICATION){
            multiplicationLessons.add(newLesson);
        }
        else{
            divisionLessons.add(newLesson);
        }

        JsonFileReader.addNewLesson(context, newLesson);
    }

    private List<Equation> loadEquationsForLesson(SQLiteHelper sqLiteHelper, int number, OperatorType operatorType){
        List<Equation> equations = new ArrayList<>();

        for(int i=0;i<=10;i++){
            int c = i * number;
            Equation equation = null;
            if(operatorType==OperatorType.MULTIPLICATION){
                equation = equationController.createEquation(number, i, c, operatorType);
            }
            else if(operatorType == OperatorType.DIVISION){
                equation = equationController.createEquation(c, number, i, operatorType);
            }

            equation.changePoints(sqLiteHelper.getPoints(equation.getId()));
            equations.add(equation);
        }

        return equations;
    }

    public void loadSettings(Lesson lesson, int lessonIndex, OperatorType operatorType){
        int number = lesson.number;

        Settings settings = null;
        if(lessonIndex==0){
            settings = new Settings.SettingsBuilder()
                    .setModeType(ModeType.LESSON)
                    .setLesson(lesson)
                    .setExerciseIndex(lessonIndex)
                    .setOperators(Collections.singletonList(operatorType))
                    .setRangeType(RangeType.AB)
                    .setRangeAValues(number, number)
                    .setRangeBValues(0, 10)
                    .setLimitType(LimitType.EQUATION_HEART)
                    .setEquationLimit(22)
                    .setAnswerType(AnswerType.RESULT)
                    .setGameType(GameType.GAME1)
                    .build();
        }
        else if(lessonIndex==1){
            settings = new Settings.SettingsBuilder()
                    .setModeType(ModeType.LESSON)
                    .setLesson(lesson)
                    .setExerciseIndex(lessonIndex)
                    .setOperators(Collections.singletonList(operatorType))
                    .setRangeType(RangeType.AB)
                    .setRangeAValues(number, number)
                    .setRangeBValues(0, 10)
                    .setLimitType(LimitType.EQUATION_HEART)
                    .setAnswerType(AnswerType.DEFAULT)
                    .setGameType(GameType.TRUE_FALSE)
                    .build();
        }
        else if(lessonIndex==2){
            settings = new Settings.SettingsBuilder()
                    .setModeType(ModeType.LESSON)
                    .setLesson(lesson)
                    .setExerciseIndex(lessonIndex)
                    .setOperators(Collections.singletonList(operatorType))
                    .setRangeType(RangeType.AB)
                    .setRangeAValues(number, number)
                    .setRangeBValues(0, 10)
                    .setLimitType(LimitType.EQUATION_HEART)
                    .setAnswerType(AnswerType.MIXED)
                    .setGameType(GameType.INPUT)
                    .build();
        }
        else if(lessonIndex==3){
            settings = new Settings.SettingsBuilder()
                    .setModeType(ModeType.LESSON)
                    .setLesson(lesson)
                    .setExerciseIndex(lessonIndex)
                    .setOperators(Collections.singletonList(operatorType))
                    .setRangeType(RangeType.AB)
                    .setRangeAValues(number, number)
                    .setRangeBValues(0, 10)
                    .setLimitType(LimitType.DEFAULT)
                    .setAnswerType(AnswerType.MIXED)
                    .setGameType(GameType.LAVA)
                    .build();
        }
        else if(lessonIndex==4){
            settings = new Settings.SettingsBuilder()
                    .setModeType(ModeType.LESSON)
                    .setLesson(lesson)
                    .setExerciseIndex(lessonIndex)
                    .setOperators(Collections.singletonList(operatorType))
                    .setRangeType(RangeType.AB)
                    .setRangeAValues(number, number)
                    .setRangeBValues(0, 10)
                    .setLimitType(LimitType.HEART)
                    .setAnswerType(AnswerType.DEFAULT)
                    .setGameType(GameType.MATCH)
                    .build();
        }
        else if(lessonIndex==5){
            settings = new Settings.SettingsBuilder()
                    .setModeType(ModeType.LESSON)
                    .setLesson(lesson)
                    .setExerciseIndex(lessonIndex)
                    .setOperators(Collections.singletonList(operatorType))
                    .setRangeType(RangeType.AB)
                    .setRangeAValues(number, number)
                    .setRangeBValues(0, 10)
                    .setLimitType(LimitType.DEFAULT)
                    .setAnswerType(AnswerType.DEFAULT)
                    .setGameType(GameType.PUZZLE)
                    .build();
        }
        else if(lessonIndex==6){
            settings = new Settings.SettingsBuilder()
                    .setModeType(ModeType.LESSON)
                    .setLesson(lesson)
                    .setExerciseIndex(lessonIndex)
                    .setOperators(Collections.singletonList(operatorType))
                    .setRangeType(RangeType.AB)
                    .setRangeAValues(number, number)
                    .setRangeBValues(0, 10)
                    .setLimitType(LimitType.HEART)
                    .setAnswerType(AnswerType.DEFAULT)
                    .setGameType(GameType.CARD)
                    .build();
        }
        else if(lessonIndex==7){
            settings = new Settings.SettingsBuilder()
                    .setModeType(ModeType.LESSON)
                    .setLesson(lesson)
                    .setExerciseIndex(lessonIndex)
                    .setOperators(Collections.singletonList(operatorType))
                    .setRangeType(RangeType.AB)
                    .setRangeAValues(number, number)
                    .setRangeBValues(0, 10)
                    .setLimitType(LimitType.HEART)
                    .setAnswerType(AnswerType.DEFAULT)
                    .setGameType(GameType.DRAW)
                    .build();
        }

        SettingsController.getInstance().settings = settings;
    }


    public void openCourse(Context context, OperatorType operatorType){
        openedCourseOperatorType = operatorType;

        context.startActivity(new Intent(context, LearnPathActivity.class));
    }

    public static LessonController getInstance(){
        if(_instance == null){
            _instance = new LessonController();
        }

        return _instance;
    }
}
