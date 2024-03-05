package com.coresaken.multiplication.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.coresaken.multiplication.data.enums.OperatorType;

import java.util.List;

public class UnknownEquation {
    public Equation equation;
    public boolean correction;
    private int unknownElementIndex;


    public UnknownEquation(Equation equation) {
        this.equation = equation;

        this.unknownElementIndex = -1;
    }

    public UnknownEquation(Equation equation, int unknownElementIndex) {
        this.equation = equation;

        this.unknownElementIndex = unknownElementIndex;
    }

    public void setUnknownElementIndex(int value){
        unknownElementIndex = value;
    }
    public int getUnknownElementIndex(){
        return unknownElementIndex;
    }
    //Zwraca wartość niewiadomej liczby
    public int getUnknownElementValue(){
        return equation.getElements().get(unknownElementIndex).number;
    }

    @NonNull
    public UnknownEquation clone(){
        return new UnknownEquation(equation, unknownElementIndex);
    }
}
