package com.coresaken.multiplication.controller;

import android.util.Log;

import androidx.annotation.Nullable;

import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.data.enums.AnswerType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.data.enums.RangeType;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EquationController {
    Settings settings;
    Random random;

    int[] lastAnswers;
    int recursive;

    Equation[] lessonEquation;
    List<Equation> correctionEquation;

    int specialEquationIndex;

    public EquationController(){
        settings = SettingsController.getInstance().settings;
        random = new Random();

        lastAnswers = new int[10];
    }

    @Nullable
    public Equation getEquation(){
        Equation equation = null;

        if(settings.modeType==ModeType.LESSON){
            if(lessonEquation!=null && specialEquationIndex <lessonEquation.length){
                equation = lessonEquation[specialEquationIndex];
                specialEquationIndex++;
                return equation;
            }
            else{
                //Wszystkie zaplanowane równania zostały rozwiązane. Brak nowych równań.
                //Następnie dobieramy równanie z błędnych równań, albo kończymy lekcje.
                return null;
            }
        }
        else if(settings.modeType == ModeType.CORRECTION){
            if(correctionEquation!=null && specialEquationIndex < correctionEquation.size()){
                equation = correctionEquation.get(specialEquationIndex);
                specialEquationIndex++;
                return equation;
            }
            else{
                //Wszystkie zaplanowane równania zostały rozwiązane. Brak nowych równań.
                //Następnie dobieramy równanie z błędnych równań, albo kończymy lekcje.
                return null;
            }
        }


        recursive = 20;
        while (equation==null){
            switch (settings.getRandomOperation()){
                case ADDITION:
                    equation = addition();
                    break;
                case SUBTRACTION:
                    equation = subtraction();
                    break;
                case MULTIPLICATION:
                    equation = multiplication();
                    break;
                case DIVISION:
                    equation = division();
                    break;
            }
        }

        return equation;
    }

    @Nullable
    public Equation getEquationForCompetition(int numberMin, int numberMax){
        Equation equation = null;

        /*if(settings.modeType==ModeType.LESSON){
            if(lessonEquation!=null && specialEquationIndex <lessonEquation.length){
                equation = lessonEquation[specialEquationIndex];
                specialEquationIndex++;
                return equation;
            }
            else{
                //Wszystkie zaplanowane równania zostały rozwiązane. Brak nowych równań.
                //Następnie dobieramy równanie z błędnych równań, albo kończymy lekcje.
                return null;
            }
        }*/

        while (equation==null){
            equation = multiplication(numberMin, numberMax);
        }

        return equation;
    }

    public Equation getRandomLessonEquation(Lesson lesson){
        Equation randomEquation = lesson.equations.get(random.nextInt(lesson.equations.size()));
        return randomEquation.clone();
    }
    public void loadLessonEquations(int equationAmount, Lesson lesson){
        lessonEquation = new Equation[equationAmount];

        //Ilość równań przypisanych do danej lekcji
        int equationInLesson = lesson.equations.size();
        int[] equationIndex = new int[equationAmount];

        int multiply = equationAmount/equationInLesson;

        for(int i=0;i<multiply * equationInLesson;i++){
            equationIndex[i] = i%equationInLesson;
        }

        for(int i=multiply*equationInLesson;i<equationAmount;i++){
            int index = random.nextInt(equationInLesson);
            equationIndex[i] = index;
        }

        Utils.shuffleArray(equationIndex);

        for(int i=0;i<equationAmount;i++){
            Equation equation = lesson.equations.get(equationIndex[i]);
            lessonEquation[i] = equation.clone();
        }
    }

    public Equation getRandomCorrectionEquation(){
        Equation randomEquation = correctionEquation.get(random.nextInt(correctionEquation.size()));
        return randomEquation.clone();
    }
    public void loadCorrectionEquation(int equationAmount){
        correctionEquation = CorrectionController.getInstance().getEquations(equationAmount);
    }

    public void setUnknownElement(UnknownEquation unknownEquation){
        if(settings.answerType== AnswerType.RESULT){
            unknownEquation.setUnknownElementIndex(unknownEquation.equation.getElements().size() - 1);
        }
        else{
            List<Equation.Element> elements = unknownEquation.equation.getElements();

            boolean found = false;
            do{
                if(elements.get(0).number == 0){
                    unknownEquation.setUnknownElementIndex(0);
                    found = true;
                    continue;
                }
                if(elements.get(2).number == 0){
                    unknownEquation.setUnknownElementIndex(2);
                    found = true;
                    continue;
                }

                int index = random.nextInt(elements.size());
                if(elements.get(index).type == Equation.ElementType.NUMBER){
                    if(index == 0){
                        if(settings.aMin == settings.aMax){
                            continue;
                        }
                    }
                    if(index == 2){
                        if(settings.bMin == settings.bMax){
                            continue;
                        }
                    }

                    unknownEquation.setUnknownElementIndex(index);
                    found = true;
                }
            }while (!found);
        }
    }

    private Equation addition(){
        recursive--;

        Random random = new Random();

        List<Equation.Element> elements = new ArrayList<>();
        int a, b, c;
        if(settings.rangeType == RangeType.AB){
            a = random.nextInt(settings.aMax - settings.aMin + 1) + settings.aMin;
            b = random.nextInt(settings.bMax - settings.bMin + 1) + settings.bMin;

            c = a + b;
        }
        else{
            c = random.nextInt(settings.cMax - settings.cMin + 1) + settings.cMin;

            a = random.nextInt(c);
            b = c - a;
        }

        Equation.Element aElement = new Equation.Element(a);
        Equation.Element bElement = new Equation.Element(b);
        Equation.Element cElement = new Equation.Element(c);

        Equation.Element signElement = new Equation.Element(OperatorType.ADDITION.sign);
        Equation.Element equalSignElement = new Equation.Element("=");

        elements.add(aElement);
        elements.add(signElement);
        elements.add(bElement);
        elements.add(equalSignElement);
        elements.add(cElement);

        return new Equation(elements, OperatorType.ADDITION);
    }
    private Equation subtraction(){
        recursive--;

        Random random = new Random();

        List<Equation.Element> elements = new ArrayList<>();
        int a, b, c;
        if(settings.rangeType == RangeType.AB){
            a = random.nextInt(settings.aMax - settings.aMin + 1) + settings.aMin;
            b = random.nextInt(settings.bMax - settings.bMin + 1) + settings.bMin;

            //Zmieniamy kolejność, aby nie wyszedł wynik ujemny
            if(a<b){
                int bCopy = b;
                b = a;
                a = bCopy;
            }

            c = a - b;
        }
        else{
            c = random.nextInt(settings.cMax - settings.cMin + 1) + settings.cMin;

            a = random.nextInt(c);
            b = c + a;

            if(a<b){
                int bCopy = b;
                b = a;
                a = bCopy;
            }
        }

        Equation.Element aElement = new Equation.Element(a);
        Equation.Element bElement = new Equation.Element(b);
        Equation.Element cElement = new Equation.Element(c);

        Equation.Element signElement = new Equation.Element(OperatorType.SUBTRACTION.sign);
        Equation.Element equalSignElement = new Equation.Element("=");

        elements.add(aElement);
        elements.add(signElement);
        elements.add(bElement);
        elements.add(equalSignElement);
        elements.add(cElement);

        return new Equation(elements, OperatorType.SUBTRACTION);
    }
    private Equation multiplication(){
        recursive--;

        Random random = new Random();

        List<Equation.Element> elements = new ArrayList<>();
        int a, b, c;
        if(settings.rangeType == RangeType.AB){
            do{
                a = random.nextInt(settings.aMax - settings.aMin + 1) + settings.aMin;
                b = random.nextInt(settings.bMax - settings.bMin + 1) + settings.bMin;

                c = a * b;

                recursive--;
            }while (lastAnswerContains(a, b) && recursive > 0);

            addLastAnswer(a);
        }
        else{
            c = random.nextInt(settings.cMax - settings.cMin + 1) + settings.cMin;

            List<Integer> divisors = new ArrayList<>();
            for(int i=2;i<c - 1;i++){
                if(c%i==0){
                    divisors.add(i);
                }
            }
            int divisorsAmount = divisors.size();

            if(divisorsAmount==0){
                return multiplication();
            }

            int randomIndex = random.nextInt(divisorsAmount);

            int i = 0;
            do{
                a = divisors.get(randomIndex%divisorsAmount);

                b = c / a;
                randomIndex++;

                i++;
            }while (lastAnswerContains(a, b) && i<divisorsAmount);

            if(lastAnswerContains(a, b)){
                if(recursive > 0){
                    return multiplication();
                }
            }

            addLastAnswer(a);
        }

        if(a==0 && b==0){
            return multiplication();
        }

        Equation.Element aElement = new Equation.Element(a);
        Equation.Element bElement = new Equation.Element(b);
        Equation.Element cElement = new Equation.Element(c);

        Equation.Element signElement = new Equation.Element(OperatorType.MULTIPLICATION.sign);
        Equation.Element equalSignElement = new Equation.Element("=");

        elements.add(aElement);
        elements.add(signElement);
        elements.add(bElement);
        elements.add(equalSignElement);
        elements.add(cElement);

        return new Equation(elements, OperatorType.MULTIPLICATION);
    }

    private Equation multiplication(int minNumber, int maxNumber){
        recursive--;

        Random random = new Random();

        List<Equation.Element> elements = new ArrayList<>();
        int a, b, c;
        a = random.nextInt(maxNumber - minNumber + 1) + minNumber;
        b = random.nextInt(11);

        c = a * b;

        if(a==0 && b==0){
            return multiplication(minNumber, maxNumber);
        }

        Equation.Element aElement = new Equation.Element(a);
        Equation.Element bElement = new Equation.Element(b);
        Equation.Element cElement = new Equation.Element(c);

        Equation.Element signElement = new Equation.Element(OperatorType.MULTIPLICATION.sign);
        Equation.Element equalSignElement = new Equation.Element("=");

        elements.add(aElement);
        elements.add(signElement);
        elements.add(bElement);
        elements.add(equalSignElement);
        elements.add(cElement);

        return new Equation(elements, OperatorType.MULTIPLICATION);
    }

    private Equation division(){
        recursive--;

        Random random = new Random();

        List<Equation.Element> elements = new ArrayList<>();
        int a, b, c;

        Equation.Element signElement = new Equation.Element(OperatorType.DIVISION.sign);
        Equation.Element equalSignElement = new Equation.Element("=");
        Equation.Element aElement, bElement, cElement;

        if(settings.rangeType == RangeType.AB){
            do{
                a = random.nextInt(settings.aMax - settings.aMin + 1) + settings.aMin;
                b = random.nextInt(settings.bMax - settings.bMin + 1) + settings.bMin;

                c = a * b;

                recursive--;
            }while (lastAnswerContains(a, b) && recursive > 0);

            addLastAnswer(a);

            if(a==0){
                return division();
            }

            aElement = new Equation.Element(a);
            bElement = new Equation.Element(b);
            cElement = new Equation.Element(c);

            elements.add(cElement);
            elements.add(signElement);
            elements.add(aElement);
            elements.add(equalSignElement);
            elements.add(bElement);
        }
        else{
            c = random.nextInt(settings.cMax - settings.cMin + 1) + settings.cMin;

            List<Integer> divisors = new ArrayList<>();
            for(int i=2;i<c - 1;i++){
                if(c%i==0){
                    divisors.add(i);
                }
            }
            int divisorsAmount = divisors.size();

            if(divisorsAmount==0){
                return division();
            }

            int randomIndex = random.nextInt(divisorsAmount);

            int i = 0;
            do{
                a = divisors.get(randomIndex%divisorsAmount);

                b = c / a;
                randomIndex++;

                i++;
            }while (lastAnswerContains(a, b) && i<divisorsAmount);

            if(lastAnswerContains(a, b)){
                if(recursive > 0){
                    return division();
                }
            }

            if(b==0 || c == 0){
                return division();
            }

            addLastAnswer(a);

            aElement = new Equation.Element(a);
            bElement = new Equation.Element(b);
            cElement = new Equation.Element(c);

            elements.add(cElement);
            elements.add(signElement);
            elements.add(bElement);
            elements.add(equalSignElement);
            elements.add(aElement);
        }

        if(b==0){
            return division();
        }

        return new Equation(elements, OperatorType.DIVISION);
    }

    private boolean lastAnswerContains(int a, int b){
        for(int i=0;i<lastAnswers.length;i++){
            if(lastAnswers[i]==a || lastAnswers[i]==b){
                return true;
            }
        }

        return false;
    }

    public Equation createEquation(int a, int b, int c, OperatorType operatorType){
        List<Equation.Element> elements = new ArrayList<>();

        Equation.Element aElement = new Equation.Element(a);
        Equation.Element bElement = new Equation.Element(b);
        Equation.Element cElement = new Equation.Element(c);

        Equation.Element signElement = new Equation.Element(operatorType.sign);
        Equation.Element equalSignElement = new Equation.Element("=");

        elements.add(aElement);
        elements.add(signElement);
        elements.add(bElement);
        elements.add(equalSignElement);
        elements.add(cElement);

        return new Equation(elements, operatorType);
    }

    private void addLastAnswer(int a){
        for(int i=lastAnswers.length - 1; i>=1 ;i--){
            lastAnswers[i] = lastAnswers[i-1];
        }

        lastAnswers[0] = a;
    }
}
