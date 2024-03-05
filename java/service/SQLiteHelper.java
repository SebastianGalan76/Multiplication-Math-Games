package com.coresaken.multiplication.service;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.coresaken.multiplication.data.CorrectionEquation;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.enums.OperatorType;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class SQLiteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "DataDB.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "equation";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_POINT = "point";


    public SQLiteHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+TABLE_NAME+ " ("+COLUMN_ID+" TEXT PRIMARY KEY, "+ COLUMN_POINT +" INTEGER);";
        db.execSQL(query);

        query = "CREATE TABLE dailystreak ("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, year INTEGER, month INTEGER, day INTEGER, time INTEGER);";
        db.execSQL(query);

        query = "CREATE TABLE correction ("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, a INTEGER, b INTEGER, c INTEGER, operation INTEGER, points INTEGER);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS "+ TABLE_NAME);
        onCreate(db);
    }

    public void addEquation(String id, int points){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COLUMN_ID, id);
        cv.put(COLUMN_POINT, points);

        db.insert(TABLE_NAME, null, cv);
    }

    @SuppressLint("Range")
    public int getPoints(String id){
        int points = 0;

        String query = "SELECT * FROM "+TABLE_NAME+" WHERE "+COLUMN_ID+" = ? ";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        try{
            String[] selectionArgs = { id };
            cursor = db.rawQuery(query, selectionArgs);

            if(cursor!=null){
                if(cursor.moveToNext()){
                    points = cursor.getInt(cursor.getColumnIndex(COLUMN_POINT));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return points;
    }

    @SuppressLint("Range")
    public void updatePoints(String id, int points){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        db.beginTransaction();
        try{
            cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?", new String[]{id});
            if (cursor != null && cursor.moveToFirst()) {
                int currentPoints = cursor.getInt(cursor.getColumnIndex(COLUMN_POINT));
                int updatedPoints = currentPoints + points;

                if (updatedPoints < -15) {
                    updatedPoints = -15;
                }

                ContentValues cv = new ContentValues();
                cv.put(COLUMN_POINT, updatedPoints);

                // Aktualizuj istniejący rekord
                db.update(TABLE_NAME, cv, COLUMN_ID + "=?", new String[]{id});
            } else {
                // Dodaj nowy rekord, ponieważ rekord o podanym ID nie istnieje
                addEquation(id, points);
            }

            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }

            if (cursor != null) {
                cursor.close();
            }

            if (db.isOpen()) {
                db.close();
            }
        }
    }

    public void addDay(LocalDate day, int time){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("year", day.getYear());
        cv.put("month", day.getMonthValue());
        cv.put("day", day.getDayOfMonth());
        cv.put("time", time);

        db.insert("dailystreak", null, cv);
    }

    @SuppressLint("Range")
    public void updateDailyStreak(LocalDate day, int time){
        Log.d("SQLite", "UpdateStreak "+day.toString()+" "+time);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        db.beginTransaction();
        try{
            cursor = db.rawQuery("SELECT * FROM dailystreak WHERE year = ? AND month = ? AND day = ?;", new String[]{String.valueOf(day.getYear()), String.valueOf(day.getMonthValue()), String.valueOf(day.getDayOfMonth())});
            if (cursor != null && cursor.moveToFirst()) {
                ContentValues cv = new ContentValues();
                cv.put("time", time);

                // Aktualizuj istniejący rekord
                db.update("dailystreak", cv, "year = ? AND month = ? AND day = ?;", new String[]{String.valueOf(day.getYear()), String.valueOf(day.getMonthValue()), String.valueOf(day.getDayOfMonth())});
            } else {
                // Dodaj nowy rekord, ponieważ rekord o podanym ID nie istnieje
                addDay(day, time);
            }

            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }

            if (cursor != null) {
                cursor.close();
            }

            if (db.isOpen()) {
                db.close();
            }
        }
    }

    @SuppressLint("Range")
    public int[] getDailyStreakForMonth(LocalDate dayOfMonth){
        YearMonth yearMonth = YearMonth.from(dayOfMonth);
        int daysInMonth = yearMonth.lengthOfMonth();

        int[] time = new int[daysInMonth+1];

        String query = "SELECT * FROM dailystreak WHERE year = ? AND month = ?;";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        try{
            String[] selectionArgs = { String.valueOf(dayOfMonth.getYear()), String.valueOf(dayOfMonth.getMonthValue()) };
            cursor = db.rawQuery(query, selectionArgs);

            if(cursor!=null){
                while(cursor.moveToNext()){
                    time[cursor.getInt(cursor.getColumnIndex("day"))] = cursor.getInt(cursor.getColumnIndex("time"));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }
        return time;
    }

    @SuppressLint("Range")
    public int[] getDailyStreakForWeek(LocalDate startDay){
        YearMonth yearMonth = YearMonth.from(startDay);
        int[] time = new int[7];

        String query = "SELECT * FROM dailystreak WHERE year = ? AND month = ? AND day = ?;";
        SQLiteDatabase db = this.getReadableDatabase();

        LocalDate day = startDay;

        try{
            for(int i=0;i<7;i++){
                String[] selectionArgs = { String.valueOf(day.getYear()), String.valueOf(day.getMonthValue()), String.valueOf(day.getDayOfMonth()) };

                try(Cursor cursor = db.rawQuery(query, selectionArgs);) {
                    if(cursor!=null){
                        if(cursor.moveToNext()){
                            time[i] = cursor.getInt(cursor.getColumnIndex("time"));
                        }
                    }
                }

                day = day.plusDays(1);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return time;
    }

    public void updateIncorrectEquationPoints(Equation equation, int points){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;

        db.beginTransaction();
        try{
            List<Equation.Element> elementList = equation.getElements();
            int a = elementList.get(0).number;
            int b = elementList.get(2).number;
            int c = elementList.get(4).number;

            int operation = equation.operatorType.index;

            cursor = db.rawQuery("SELECT * FROM correction WHERE a = ? AND b = ? AND operation = ?", new String[]{String.valueOf(a),String.valueOf(b),String.valueOf(operation)});
            if(cursor != null && cursor.moveToFirst()){
                @SuppressLint("Range") int currentPoints = cursor.getInt(cursor.getColumnIndex("points"));
                int updatedPoints = currentPoints + points;

                if (updatedPoints < -15) {
                    updatedPoints = -30;
                }

                ContentValues cv = new ContentValues();
                cv.put("points", updatedPoints);

                if(updatedPoints>0){
                    db.delete("correction", "a = ? AND b = ? AND operation = ?", new String[]{String.valueOf(a),String.valueOf(b),String.valueOf(operation)});
                }
                else{
                    // Aktualizuj istniejący rekord
                    db.update("correction", cv, "a = ? AND b = ? AND operation = ?", new String[]{String.valueOf(a),String.valueOf(b),String.valueOf(operation)});
                }
            }
            else{
                // Dodaj nowy rekord, ponieważ rekord o podanym ID nie istnieje
                ContentValues cv = new ContentValues();
                cv.put("a", a);
                cv.put("b", b);
                cv.put("c", c);
                cv.put("operation", operation);
                cv.put("points", -10);

                db.insert("correction", null, cv);
            }

            db.setTransactionSuccessful();
        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (db.inTransaction()) {
                db.endTransaction();
            }

            if (cursor != null) {
                cursor.close();
            }

            if (db.isOpen()) {
                db.close();
            }
        }
    }

    @SuppressLint("Range")
    public List<CorrectionEquation> getCorrectionEquations(List<OperatorType> operatorTypes){
        List<CorrectionEquation> equations = new ArrayList<>();

        String query = "SELECT * FROM correction WHERE operation = ?";
        SQLiteDatabase db = this.getReadableDatabase();

        try{
            for(OperatorType operator:operatorTypes){
                String[] selectionArgs = { String.valueOf(operator.index) };

                try(Cursor cursor = db.rawQuery(query, selectionArgs)){
                    if(cursor!=null){
                        while(cursor.moveToNext()){
                            StringBuilder stringBuilder = new StringBuilder();

                            int a = cursor.getInt(cursor.getColumnIndex("a"));
                            int b = cursor.getInt(cursor.getColumnIndex("b"));
                            int c = cursor.getInt(cursor.getColumnIndex("c"));
                            int points = cursor.getInt(cursor.getColumnIndex("points"));

                            String sign = operator.sign;

                            stringBuilder.append(a).append(" ")
                                    .append(sign).append(" ")
                                    .append(b).append(" ")
                                    .append("= ").append(c);

                            equations.add(new CorrectionEquation(new Equation(stringBuilder.toString(), operator), points));
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return equations;
    }

    @SuppressLint("Range")
    public List<Equation> getEquationsFromCorrectionTable(List<OperatorType> operators){
        List<Equation> equations = new ArrayList<>();

        int additionAmount = 0, subtractionAmount = 0, multiplicationAmount = 0, divisionAmount = 0;

        if(operators.contains(OperatorType.ADDITION)){
            additionAmount = checkCorrectionEquationAmount(OperatorType.ADDITION);
        }
        if(operators.contains(OperatorType.SUBTRACTION)){
            subtractionAmount = checkCorrectionEquationAmount(OperatorType.SUBTRACTION);
        }
        if(operators.contains(OperatorType.MULTIPLICATION)){
            multiplicationAmount = checkCorrectionEquationAmount(OperatorType.MULTIPLICATION);
        }
        if(operators.contains(OperatorType.DIVISION)){
            divisionAmount = checkCorrectionEquationAmount(OperatorType.DIVISION);
        }

        equations.addAll(getEquationsForSign(OperatorType.ADDITION, additionAmount));
        equations.addAll(getEquationsForSign(OperatorType.SUBTRACTION, subtractionAmount));
        equations.addAll(getEquationsForSign(OperatorType.MULTIPLICATION, multiplicationAmount));
        equations.addAll(getEquationsForSign(OperatorType.DIVISION, divisionAmount));

        return equations;
    }

    @SuppressLint("Range")
    private List<Equation> getEquationsForSign(OperatorType operator, int amount){
        List<Equation> equations = new ArrayList<>();

        String query = "SELECT * FROM correction WHERE operation = ? ORDER BY random() LIMIT ?;";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;

        if(amount>50){
            amount = 50;
        }

        try{
            String[] selectionArgs = { String.valueOf(operator.index), String.valueOf(amount) };
            cursor = db.rawQuery(query, selectionArgs);

            if(cursor!=null){
                while(cursor.moveToNext()){
                    StringBuilder stringBuilder = new StringBuilder();

                    int a = cursor.getInt(cursor.getColumnIndex("a"));
                    int b = cursor.getInt(cursor.getColumnIndex("b"));
                    int c = cursor.getInt(cursor.getColumnIndex("c"));

                    stringBuilder.append(a).append(" ")
                            .append("UNKNOWN").append(" ")
                            .append(b).append(" ")
                            .append("= ").append(c);

                    equations.add(new Equation(stringBuilder.toString(), operator));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return equations;
    }

    public int getTotalCorrectionEquationAmount(){
        int amount = 0;

        amount += checkCorrectionEquationAmount(OperatorType.ADDITION);
        amount += checkCorrectionEquationAmount(OperatorType.SUBTRACTION);
        amount += checkCorrectionEquationAmount(OperatorType.MULTIPLICATION);
        amount += checkCorrectionEquationAmount(OperatorType.DIVISION);

        return amount;
    }

    @SuppressLint("Range")
    private int checkCorrectionEquationAmount(OperatorType operatorType){
        String query = "SELECT COUNT(*) AS amount FROM correction WHERE operation = ? ";
        SQLiteDatabase db = this.getReadableDatabase();

        int amount = 0;

        Cursor cursor = null;
        try{
            String[] selectionArgs = { String.valueOf(operatorType.index) };
            cursor = db.rawQuery(query, selectionArgs);

            if(cursor!=null){
                if(cursor.moveToNext()){
                    StringBuilder stringBuilder = new StringBuilder();

                    amount = cursor.getInt(cursor.getColumnIndex("amount"));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null && db.isOpen()) {
                db.close();
            }
        }

        return amount;
    }
}
