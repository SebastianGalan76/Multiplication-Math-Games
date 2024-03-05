package com.coresaken.multiplication.controller.adapter;

import com.udojava.evalex.Expression;

public class ExpressionAdapter {

    Expression expression;
    public ExpressionAdapter(String expression){
        expression = expression.replace("×", "*");
        expression = expression.replace("÷", "/");

        this.expression = new Expression(expression);
    }

    public int getResults(){
        return expression.eval().intValue();
    }
}
