package com.coresaken.multiplication.data.enums;

public enum OperatorType {
    ADDITION("+", "+",0),
    SUBTRACTION("-", "-",1),
    MULTIPLICATION("×", "*",2),
    DIVISION("÷", "/",3);

    public String displaySign;
    public String sign;
    public int index;
    OperatorType(String displaySign, String sign, int index){
        this.displaySign = displaySign;
        this.sign = sign;
        this.index = index;
    }

    public static OperatorType getOperatorTypeByIndex(int index){
        switch (index){
            case 0:
                return ADDITION;
            case 1:
                return SUBTRACTION;
            case 2:
                return MULTIPLICATION;
            case 3:
                return DIVISION;
        }
        return null;
    }
}
