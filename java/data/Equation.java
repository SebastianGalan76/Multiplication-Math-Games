package com.coresaken.multiplication.data;
import androidx.annotation.NonNull;

import com.coresaken.multiplication.data.enums.OperatorType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Equation {
    private final String id; //2*3, 5/2, 2+5
    private final List<Element> elements;
    public OperatorType operatorType;
    private int points;

    public Equation(List<Element> elements, OperatorType operatorType){
        this.elements = elements;
        this.operatorType = operatorType;

        StringBuilder equationBuilder = new StringBuilder();
        int size = elements.size();
        for(int i=0;i<size;i++){
            if(i!=size-1){
                equationBuilder.append(elements.get(i).toString()).append(" ");
            }
            else{
                equationBuilder.append(elements.get(i).toString());
            }
        }

        id = elements.get(0).number+elements.get(1).sign+elements.get(2).number;
    }

    public Equation(String equation, OperatorType operatorType){
        String[] elementsString = equation.split(" ");

        List<Element> elementsList = new ArrayList<>();
        elementsList.add(new Element(Integer.parseInt(elementsString[0])));
        elementsList.add(new Element(operatorType.sign));
        elementsList.add(new Element(Integer.parseInt(elementsString[2])));
        elementsList.add(new Element("="));
        elementsList.add(new Element(Integer.parseInt(elementsString[4])));

        this.elements = elementsList;
        this.operatorType = operatorType;
        this.id = elementsString[0]+operatorType.sign+elementsString[2];
    }

    @NonNull
    public Equation clone(){
        return new Equation(elements, operatorType);
    }

    //Zmiana wynik równania. Używany np. w grze prawda/fałsz
    public void setResult(int value){
        elements.get(elements.size()-1).number = value;
    }
    public List<Element> getElements(){
        return elements;
    }

    @NonNull
    public String toString(){
        return elements.get(0).number+" "+operatorType.sign+" "+elements.get(2).number +" = "+elements.get(4).number;
    }

    public String getId(){
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return Objects.equals(toString(), ((Equation) o).toString());
    }

    public void changePoints(int value){
        points+=value;

        if(points<-15){
            points=-15;
        }

        if(points>=50){
            points=50;
        }
    }
    public int getPoints(){
        return points;
    }

    public void setPoints(int value){
        points = value;
    }

    public static class Element{
        public ElementType type;
        public int number;
        public String sign;

        public Element(int number){
            type = ElementType.NUMBER;
            this.number = number;
        }
        public Element(String sign){
            type = ElementType.SIGN;
            this.sign = sign;
        }

        @NonNull
        public String toString(){
            if(type==ElementType.NUMBER){
                return String.valueOf(number);
            }
            return sign;
        }
    }
    public enum ElementType{
        NUMBER, SIGN
    }
}
