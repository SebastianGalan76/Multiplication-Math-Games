package com.coresaken.multiplication.util;

import android.content.res.Resources;

import java.util.List;
import java.util.Random;

public class Utils {

    public static int convertDPToPixels(Resources res, int sizeInDp){
        float scale = res.getDisplayMetrics().density;
        return (int) (sizeInDp*scale + 0.5f);
    }

    public static String convertOperatorSign(String equation){
        String expression = equation.replace("*", "×");
        expression = expression.replace("/", "÷");

        return expression;
    }

    public static int getStarAmount(int points){
        if (points < 10) {
            return 0;
        } else if (points < 30) {
            return 1;
        } else if (points < 50) {
            return 2;
        } else if (points < 70) {
            return 3;
        } else if (points < 90) {
            return 4;
        } else {
            return 5;
        }
    }

    public static int[] convertToArray(List<Integer> list){
        int[] array = new int[list.size()];

        for(int i=0;i<list.size();i++){
            array[i] = list.get(i);
        }

        return array;
    }

    public static void shuffleArray(String[] array) {
        Random random = new Random();
        if (array == null || array.length < 2) {
            return;
        }

        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);

            String temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }
    }
    public static void shuffleArray(int[] array) {
        Random random = new Random();
        if (array == null || array.length < 2) {
            return;
        }

        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);

            int temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }
    }
    public static void shuffleArray(Object[] array) {
        Random random = new Random();
        if (array == null || array.length < 2) {
            return;
        }

        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);

            Object temp = array[i];
            array[i] = array[index];
            array[index] = temp;
        }
    }
}
