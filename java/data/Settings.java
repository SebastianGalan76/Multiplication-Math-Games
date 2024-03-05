package com.coresaken.multiplication.data;

import com.coresaken.multiplication.data.enums.AnswerType;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.LimitType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.data.enums.RangeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Settings {

    public ModeType modeType = ModeType.GAME;

    public Lesson currentLesson;
    public Lesson.Exercise currentExercise;
    public int exerciseIndex;

    //Operator
    public List<OperatorType> operators;

    //RangeType
    public RangeType rangeType;
    public int aMin = 0, aMax = 10;
    public int bMin = 0, bMax = 10;
    public int cMin = 0, cMax = 100;

    public LimitType limitType;
    public int equationLimit = 30;

    public AnswerType answerType;

    public GameType gameType;

    public Settings(){
        operators = new ArrayList<>();
    }

    public Settings(SettingsBuilder builder){
        this.modeType = builder.modeType;
        this.currentLesson = builder.currentLesson;
        this.exerciseIndex = builder.exerciseIndex;

        this.operators = builder.operators;

        this.rangeType = builder.rangeType;
        this.aMin = builder.aMin;
        this.aMax = builder.aMax;
        this.bMin = builder.bMin;
        this.bMax = builder.bMax;
        this.cMin = builder.cMin;
        this.cMax = builder.cMax;

        this.limitType = builder.limitType;
        this.equationLimit = builder.equationLimit;

        this.answerType = builder.answerType;
        this.gameType = builder.gameType;
    }

    public void initializeExercise(){
        currentExercise = currentLesson.exercises.get(exerciseIndex);
    }

    public OperatorType getRandomOperation(){
        Random random = new Random();

        if(operators.size()==0){
            return OperatorType.MULTIPLICATION;
        }

        int operatorIndex = random.nextInt(operators.size());
        return operators.get(operatorIndex);
    }

    public static class SettingsBuilder{
        public ModeType modeType;
        public Lesson currentLesson;
        public int exerciseIndex;

        private List<OperatorType> operators;

        //RangeType
        private RangeType rangeType;
        private int aMin = 0, aMax = 10;
        private int bMin = 0, bMax = 10;
        private int cMin = 0, cMax = 100;


        private LimitType limitType;
        private int equationLimit = 30;

        private AnswerType answerType;

        private GameType gameType;

        public SettingsBuilder setModeType(ModeType modeType){
            this.modeType = modeType;
            return this;
        }
        public SettingsBuilder setLesson(Lesson lesson){
            this.currentLesson = lesson;
            return this;
        }

        public SettingsBuilder setExerciseIndex(int index){
            this.exerciseIndex = index;
            return this;
        }

        public SettingsBuilder addOperator(OperatorType operatorType){
            if(operators==null){
                operators = new ArrayList<>();
            }

            operators.add(operatorType);
            return this;
        }

        public SettingsBuilder setOperators(List<OperatorType> operators){
            this.operators = operators;
            return this;
        }

        public SettingsBuilder setRangeType(RangeType rangeType){
            this.rangeType = rangeType;
            return this;
        }

        public SettingsBuilder setRangeAValues(int min, int max){
            aMin = min;
            aMax = max;

            return this;
        }
        public SettingsBuilder setRangeBValues(int min, int max){
            bMin = min;
            bMax = max;

            return this;
        }
        public SettingsBuilder setRangeCValues(int min, int max){
            cMin = min;
            cMax = max;

            return this;
        }

        public SettingsBuilder setLimitType(LimitType limitType){
            this.limitType = limitType;
            return this;
        }

        public SettingsBuilder setEquationLimit(int value){
            this.equationLimit = value;
            return this;
        }

        public SettingsBuilder setAnswerType(AnswerType answerType){
            this.answerType = answerType;
            return this;
        }
        public SettingsBuilder setGameType(GameType gameType){
            this.gameType= gameType;
            return this;
        }

        public Settings build(){
            return new Settings(this);
        }
    }
}
