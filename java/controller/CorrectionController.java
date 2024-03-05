package com.coresaken.multiplication.controller;

import com.coresaken.multiplication.data.CorrectionEquation;
import com.coresaken.multiplication.data.Equation;

import org.checkerframework.checker.units.qual.C;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CorrectionController {
    private static CorrectionController _instance;

    public List<CorrectionEquation> selectedEquation;

    Random random;

    private CorrectionController(){
        random = new Random();
    }

    public Equation getRandomEquation(){
        Equation randomEquation = selectedEquation.get(random.nextInt(selectedEquation.size())).equation;
        return randomEquation.clone();
    }

    public List<Equation> getEquations(int amount){
        List<Equation> equations = new ArrayList<>();

        for(int i =0;i<amount;i++){
            equations.add(getRandomEquation());
        }

        return equations;
    }

    public static CorrectionController getInstance(){
        if(_instance == null){
            _instance = new CorrectionController();
        }

        return _instance;
    }

}
