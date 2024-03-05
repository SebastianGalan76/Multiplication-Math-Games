package com.coresaken.multiplication.controller;

import android.util.Log;

import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnswerController {
    Random random;
    Settings settings;

    public AnswerController(){
        random = new Random();
        settings = SettingsController.getInstance().settings;
    }

    public int[] getAnswers(Equation equation, int answerAmount, int unknownIndex, boolean mixAnswer){
        List<Integer> answers = new ArrayList<>();
        OperatorType operator = equation.operatorType;

        if(operator == OperatorType.ADDITION){
            answers = addition(equation, answerAmount, unknownIndex);
        }
        else if(operator == OperatorType.SUBTRACTION){
            answers = subtraction(equation, answerAmount, unknownIndex);
        }
        else if(operator == OperatorType.MULTIPLICATION){
            answers = multiplication(equation, answerAmount, unknownIndex);
        }
        else if(operator == OperatorType.DIVISION){
            answers = division(equation, answerAmount, unknownIndex);
        }
        else{
            Log.d("ERROR", "Niepoprawny operator!");
        }

        int[] answersArray = Utils.convertToArray(answers);

        if(mixAnswer){
            int[] mixedAnswers = new int[answerAmount];

            boolean[] alreadyTaken = new boolean[answerAmount];
            for(int i=0;i<answerAmount;i++){
                boolean found = false;

                do{
                    int index = random.nextInt(answerAmount);

                    if(!alreadyTaken[index]){
                        mixedAnswers[i] = answersArray[index];
                        alreadyTaken[index] = true;
                        found = true;
                    }
                    else{
                        index++;

                        int newIndex = index%answerAmount;
                        if(!alreadyTaken[newIndex]){
                            mixedAnswers[i] = answersArray[newIndex];
                            alreadyTaken[newIndex] = true;
                            found = true;
                        }
                    }
                }while (!found);
            }

            return mixedAnswers;
        }

        return answersArray;
    }

    private List<Integer> addition(Equation equation,  int answerAmount, int unknownIndex){
        List<Equation.Element> elements = equation.getElements();
        int a = elements.get(0).number;
        int b = elements.get(2).number;
        int c = elements.get(4).number;

        List<Integer> answers = new ArrayList<>();
        int correctAnswer = elements.get(unknownIndex).number;
        answers.add(correctAnswer);

        boolean even = correctAnswer % 2 == 0;
        int recursive = 20;

        while(answers.size()<answerAmount){
            int randomValue = random.nextInt(10) - 5;

            int answer = correctAnswer + randomValue;
            if(even && answer%2!=0){
                recursive--;
                if(recursive>=0){
                    continue;
                }
            }

            if(answer<0){
                recursive--;
                if(recursive>=0){
                    continue;
                }
            }

            if(answer != correctAnswer){
                if(!answers.contains(answer)){
                    answers.add(answer);
                }
            }
        }

        return answers;
    }
    private List<Integer> subtraction(Equation equation,  int answerAmount, int unknownIndex){
        List<Equation.Element> elements = equation.getElements();
        int a = elements.get(0).number;
        int b = elements.get(2).number;
        int c = elements.get(4).number;

        List<Integer> answers = new ArrayList<>();
        int correctAnswer = elements.get(unknownIndex).number;
        answers.add(correctAnswer);

        boolean even = correctAnswer % 2 == 0;
        int recursive = 20;

        while(answers.size()<answerAmount){
            int randomValue = random.nextInt(10) - 5;

            int answer = correctAnswer + randomValue;
            if(even && answer%2!=0){
                recursive--;
                if(recursive>=0){
                    continue;
                }
            }

            if(answer<0){
                recursive--;
                if(recursive>=0){
                    answer = correctAnswer + random.nextInt(5);
                }
            }

            if(answer != correctAnswer){
                if(!answers.contains(answer)){
                    answers.add(answer);
                }
            }
        }

        return answers;
    }

    private List<Integer> multiplication(Equation equation,  int answerAmount, int unknownIndex){
        List<Equation.Element> elements = equation.getElements();
        int a = elements.get(0).number;
        int b = elements.get(2).number;
        int c = elements.get(4).number;

        List<Integer> answers = new ArrayList<>();
        int correctAnswer = elements.get(unknownIndex).number;
        answers.add(correctAnswer);

        boolean even = correctAnswer % 2 == 0;
        int nextAnswer;

        int sign = 1;
        int i = 1;
        int v = 1;

        //A * B = ?
        if(unknownIndex==4){
            if(c%5!=0){
                //Dodaj dwie liczby na podstawie innych elementów
                if(random.nextInt(100)<50){
                    int answer0, answer1;

                    if(a < b){
                        answer0 = a * (b + 1);
                        answer1 = a * (b - 1);
                    }
                    else{
                        answer0 = b * (a + 1);
                        answer1 = b * (a - 1);

                    }

                    if(answer0>=0){
                        if(!answers.contains(answer0)){
                            answers.add(answer0);
                        }
                    }
                    if(answer1>=0){
                        if(!answers.contains(answer1)){
                            answers.add(answer1);
                        }
                    }
                }
                else{
                    int answer;
                    sign = getRandomSign();

                    if(a < b){
                        answer = a * (b + sign);

                    }
                    else{
                        answer = b * (a + sign);
                    }

                    if(answer>=0){
                        if(!answers.contains(answer)){
                            answers.add(answer);
                        }
                    }
                }

                //Dodaj liczby na podstawie wyniku
                while (answers.size() < answerAmount){
                    int vTemp = 1;
                    if(even){
                        vTemp = 2;
                    }

                    nextAnswer = correctAnswer + (vTemp * sign * i);

                    if(nextAnswer>=0){
                        if(!answers.contains(nextAnswer)){
                            answers.add(nextAnswer);
                            i = 1;
                        }
                    }

                    i++;
                    sign *= (-1);
                }
            }
            else{
                if(c==0){
                    if(a==0){
                        if(answers.size() < answerAmount){
                            if(!answers.contains(b)){
                                answers.add(b);
                            }
                        }
                    }
                    if(b==0){
                        if(answers.size() < answerAmount){
                            if(!answers.contains(a)){
                                answers.add(a);
                            }
                        }
                    }

                    if(answers.size() < answerAmount){
                        if(!answers.contains(1)){
                            answers.add(1);
                        }
                    }
                }

                int value = a;
                if(a%5==0){
                    value = b;
                }

                sign = getRandomSign();

                v = 1;
                while (answers.size() < answerAmount){
                    nextAnswer = 5 * (value + (v * sign));

                    if(nextAnswer>=0){
                        if(!answers.contains(nextAnswer)){
                            answers.add(nextAnswer);
                        }
                    }

                    if(i%2==0){
                        v++;
                    }
                    i++;
                    sign *= (-1);
                }
            }
        }
        else{
            sign = getRandomSign();
            v = 1;

            int value = a;
            if(unknownIndex == 2){
                value = b;
            }

            while (answers.size() < answerAmount){
                nextAnswer = value + (v * sign);

                if(nextAnswer>=0){
                    if(!answers.contains(nextAnswer)){
                        answers.add(nextAnswer);
                    }
                }

                if(i%2==0){
                    v++;
                }
                i++;
                sign *= (-1);
            }
        }

        return answers;
    }

    private List<Integer> division(Equation equation,  int answerAmount, int unknownIndex){
        List<Equation.Element> elements = equation.getElements();
        int c = elements.get(0).number;
        int b = elements.get(2).number;
        int a = elements.get(4).number;

        List<Integer> answers = new ArrayList<>();
        int correctAnswer = elements.get(unknownIndex).number;
        answers.add(correctAnswer);
        boolean even = correctAnswer % 2 == 0;

        int nextAnswer;
        int sign = 1;
        int i = 1;
        int v = 1;

        //A * B = ?
        if(unknownIndex==0){
            if(c%5!=0){
                //Dodaj dwie liczby na podstawie innych elementów
                if(random.nextInt(100)<50){
                    int answer0, answer1;

                    if(a < b){
                        answer0 = a * (b + 1);
                        answer1 = a * (b - 1);
                    }
                    else{
                        answer0 = b * (a + 1);
                        answer1 = b * (a - 1);

                    }

                    if(answer0>=0){
                        if(!answers.contains(answer0)){
                            answers.add(answer0);
                        }
                    }
                    if(answer1>=0){
                        if(!answers.contains(answer1)){
                            answers.add(answer1);
                        }
                    }
                }
                else{
                    int answer;
                    sign = getRandomSign();

                    if(a < b){
                        answer = a * (b + sign);

                    }
                    else{
                        answer = b * (a + sign);
                    }

                    if(answer>=0){
                        if(!answers.contains(answer)){
                            answers.add(answer);
                        }
                    }
                }

                //Dodaj liczby na podstawie wyniku
                while (answers.size() < answerAmount){
                    int vTemp = 1;
                    if(even){
                        vTemp = 2;
                    }

                    nextAnswer = correctAnswer + (vTemp * sign * i);

                    if(nextAnswer>=0){
                        if(!answers.contains(nextAnswer)){
                            answers.add(nextAnswer);
                            i = 1;
                        }
                    }

                    i++;
                    sign *= (-1);
                }
            }
            else{
                int value = a;
                if(a%5==0){
                    value = b;
                }

                sign = getRandomSign();

                v = 1;
                while (answers.size() < answerAmount){
                    nextAnswer = 5 * (value + (v * sign));

                    if(nextAnswer>=0){
                        if(!answers.contains(nextAnswer)){
                            answers.add(nextAnswer);
                        }
                    }

                    if(i%2==0){
                        v++;
                    }
                    i++;
                    sign *= (-1);
                }
            }
        }
        else{
            sign = getRandomSign();
            v = 1;

            int value = a;
            if(unknownIndex == 2){
                value = b;
            }

            while (answers.size() < answerAmount){
                nextAnswer = value + (v * sign);

                if(nextAnswer>=0){
                    if(!answers.contains(nextAnswer)){
                        if(!(unknownIndex == 2 && nextAnswer == 0)){
                            answers.add(nextAnswer);
                        }
                    }
                }

                if(i%2==0){
                    v++;
                }
                i++;
                sign *= (-1);
            }
        }

        return answers;
    }


    //Return -1 or 1
    private int getRandomSign(){
        int vSign = random.nextInt(2);
        if(vSign==0){
            vSign = -1;
        }

        return vSign;
    }
}
