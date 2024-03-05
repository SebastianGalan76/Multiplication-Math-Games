package com.coresaken.multiplication.data;

import java.util.ArrayList;
import java.util.List;

public class AnsweredEquation {
    public UnknownEquation equation;
    public List<Answer> answers;
    public boolean isCorrect;
    public boolean equationIsCorrect;

    public AnsweredEquation(UnknownEquation equation){
        this.equation = equation;
        answers = new ArrayList<>();
        isCorrect = false;
    }

    public void addAnswer(int value, boolean isCorrect){
        if(answers.size()==0 && isCorrect){
            this.isCorrect = true;
        }

        answers.add(new Answer(value, isCorrect));
    }

    public static class Answer{
        public int value;
        public boolean isCorrect;

        public Answer(int value, boolean isCorrect){
            this.value = value;
            this.isCorrect = isCorrect;
        }
    }
}
