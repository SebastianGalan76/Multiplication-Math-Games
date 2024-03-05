package com.coresaken.multiplication.service;

import android.content.Context;
import android.content.res.AssetManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Semaphore;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.data.enums.RangeType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JsonFileReader {
    //Odpowiedzialny za blokowanie jednoczesnego zapisu do pliku
    private static Semaphore semaphore = new Semaphore(1);
    private static final Object lock = new Object();


    public static HashMap<Integer, GameMode> gameModeSettings;
    public static List<OperatorType> selectedOperators;
    public static RangeType selectedRangeType;
    public static int[] rangeValue;

    static JSONObject fileDate;
    static String fileName = "userdata.json";

    public static String loadJSONFromAsset(Context context){
        String json = null;
        try{
            File file = new File(context.getFilesDir(), fileName);
            if(file.exists()){
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                StringBuilder jsonStringBuilder = new StringBuilder();

                while((line=br.readLine())!=null){
                    jsonStringBuilder.append(line).append('\n');
                }
                br.close();

                json = jsonStringBuilder.toString();
            }
            else{
                AssetManager manager = context.getAssets();
                InputStream is = manager.open(fileName);

                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();

                json = new String(buffer, StandardCharsets.UTF_8);

                FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(json.getBytes(StandardCharsets.UTF_8));
                fos.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return json;
    }
    public static JSONObject loadJSONObjectFromAsset(Context context){
        JSONObject jsonObject = null;

        try{
            String jsonString = loadJSONFromAsset(context);

            if(jsonString != null){
                jsonObject = new JSONObject(jsonString);
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

        return jsonObject;
    }

    public static void loadGameSettings(Context context) {
        if(gameModeSettings !=null){
            return;
        }

        gameModeSettings = new HashMap<>();
        selectedOperators = new ArrayList<>();
        rangeValue = new int[6];

        JSONObject jsonObject = getFileDate(context);

        try{
            JSONObject gameSettingsJSON = jsonObject.getJSONObject("game-settings");

            //LoadMode
            JSONObject gameModeJSON = gameSettingsJSON.getJSONObject("mode");
            for (Iterator<String> it = gameModeJSON.keys(); it.hasNext(); ) {
                String key = it.next();

                JSONObject settingsJSON =gameModeJSON.getJSONObject(key);
                int limitId = settingsJSON.getInt("limit");
                int answerId = settingsJSON.getInt("answer");

                int keyInt = Integer.parseInt(key);
                gameModeSettings.put(keyInt, new GameMode(keyInt, limitId, answerId));
            }

            //Operators
            JSONArray operatorsJSON = gameSettingsJSON.getJSONArray("operatorId");
            for (int i = 0; i < operatorsJSON.length(); i++) {
                int id = operatorsJSON.getInt(i);

                selectedOperators.add(OperatorType.getOperatorTypeByIndex(id));
            }

            //RangeType
            JSONObject rangeJSON = gameSettingsJSON.getJSONObject("range");
            int rangeId = rangeJSON.getInt("rangeTypeId");

            switch (rangeId){
                case 0:
                    selectedRangeType = RangeType.AB;
                    break;
                case 1:
                    selectedRangeType = RangeType.RESULT;
            }

            JSONObject rangeValueJSON = rangeJSON.getJSONObject("values");

            rangeValue[0] = rangeValueJSON.getInt("aMin");
            rangeValue[1] = rangeValueJSON.getInt("aMax");

            rangeValue[2] = rangeValueJSON.getInt("bMin");
            rangeValue[3] = rangeValueJSON.getInt("bMax");

            rangeValue[4] = rangeValueJSON.getInt("cMin");
            rangeValue[5] = rangeValueJSON.getInt("cMax");
        }catch (JSONException e){
            e.printStackTrace();
        }
    }

    public static void updateGameMode(Context context, GameMode gameMode){
        if(gameMode==null){
            return;
        }

        synchronized (lock){
            gameModeSettings.replace(gameMode.gameId, gameMode);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = getFileDate(context);

                    try{
                        JSONObject gameSettingsJSON = jsonObject.getJSONObject("game-settings");
                        JSONObject gameModeJSON = gameSettingsJSON.getJSONObject("mode");

                        Iterator<String> keysIterator = gameModeJSON.keys();
                        while (keysIterator.hasNext()) {
                            String key = keysIterator.next();
                            if (key.equals(String.valueOf(gameMode.gameId))) {
                                keysIterator.remove();
                                break;
                            }
                        }

                        gameModeJSON.put(String.valueOf(gameMode.gameId), gameMode.getJSONObject());
                    }catch (JSONException e){
                        e.printStackTrace();
                    }

                    saveData(context);
                }
            }).start();
        }
    }

    public static void updateOperators(Context context, List<OperatorType> selectedOperators){
        synchronized (lock){
            JsonFileReader.selectedOperators = selectedOperators;
            int[] selectedOperatorArray = new int[selectedOperators.size()];

            for(int i=0;i<selectedOperatorArray.length;i++){
                if(selectedOperators.get(i)==OperatorType.ADDITION){
                    selectedOperatorArray[i] = 0;
                }
                else if(selectedOperators.get(i)==OperatorType.SUBTRACTION){
                    selectedOperatorArray[i] = 1;
                }
                else if(selectedOperators.get(i)==OperatorType.MULTIPLICATION){
                    selectedOperatorArray[i] = 2;
                }
                else if(selectedOperators.get(i)==OperatorType.DIVISION){
                    selectedOperatorArray[i] = 3;
                }
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = getFileDate(context);

                    try{
                        JSONObject gameSettingsJSON = jsonObject.getJSONObject("game-settings");
                        gameSettingsJSON.put("operatorId", new JSONArray());

                        JSONArray operatorJSON = gameSettingsJSON.getJSONArray("operatorId");

                        for (int j : selectedOperatorArray) {
                            operatorJSON.put(j);
                        }

                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void updateRange(Context context, RangeType rangeType, int[] rangeValue){
        synchronized (lock){
            selectedRangeType = rangeType;
            JsonFileReader.rangeValue = rangeValue;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject jsonObject = getFileDate(context);

                    try{
                        JSONObject gameSettingsJSON = jsonObject.getJSONObject("game-settings");
                        JSONObject rangeJSON = gameSettingsJSON.getJSONObject("range");

                        int rangeId = 0;
                        if(rangeType==RangeType.RESULT){
                            rangeId = 1;
                        }

                        rangeJSON.put("rangeTypeId", rangeId);

                        rangeJSON.put("values", new JSONObject());
                        JSONObject rangeValueJSON = rangeJSON.getJSONObject("values");

                        rangeValueJSON.put("aMin", rangeValue[0]);
                        rangeValueJSON.put("aMax", rangeValue[1]);

                        rangeValueJSON.put("bMin", rangeValue[2]);
                        rangeValueJSON.put("bMax", rangeValue[3]);

                        rangeValueJSON.put("cMin", rangeValue[4]);
                        rangeValueJSON.put("cMax", rangeValue[5]);
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }

    public static void updateLesson(Context context, Lesson lesson){
        synchronized (lock){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        JSONArray coursesJSON = getFileDate(context).getJSONArray("courses");

                        for(int i=0;i<coursesJSON.length();i++){
                            JSONObject courseJSON = coursesJSON.getJSONObject(i);

                            if(courseJSON.getInt("operator")!=lesson.operatorType.index){
                                continue;
                            }

                            JSONArray lessonsJSON = courseJSON.getJSONArray("lessons");

                            for(int j=0;j<lessonsJSON.length();j++){
                                if(lessonsJSON.getJSONObject(j).getInt("id")!=lesson.number){
                                    continue;
                                }

                                lessonsJSON.remove(j);
                                lessonsJSON.put(lesson.getJSONObject());
                            }
                        }
                    }
                    catch (JSONException e){
                        e.printStackTrace();
                    }

                    saveData(context);
                }
            }).start();
        }
    }

    public static void addNewLesson(Context context, Lesson lesson){
        synchronized (lock){
            try{
                JSONArray coursesJSON = getFileDate(context).getJSONArray("courses");

                for(int i=0;i<coursesJSON.length();i++){
                    JSONObject courseJSON = coursesJSON.getJSONObject(i);

                    int operatorIndex = courseJSON.getInt("operator");
                    if(operatorIndex!=lesson.operatorType.index){
                        continue;
                    }
                    JSONArray lessonsJSON = courseJSON.getJSONArray("lessons");

                    JSONObject newLessonJSON = lesson.getJSONObject();
                    lessonsJSON.put(newLessonJSON);
                }
            }catch (JSONException e){
                e.printStackTrace();
            }

            saveData(context);
        }
    }

    public static void saveData(Context context){
        synchronized (lock){
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        semaphore.acquire();
                        File file = new File(context.getFilesDir(),fileName);
                        FileWriter fileWriter = new FileWriter(file);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        bufferedWriter.write(getFileDate(context).toString());
                        bufferedWriter.close();
                    }catch (IOException | InterruptedException e){
                        e.printStackTrace();
                    }
                    finally {
                        semaphore.release();
                    }
                }
            });

            thread.start();
        }
    }

    public static JSONObject getFileDate(Context context){
        if(fileDate==null){
            fileDate = loadJSONObjectFromAsset(context);
        }

        return fileDate;
    }

    public static class GameMode {
        public int gameId;
        public int limitId;
        public int answerId;

        public GameMode(int gameId, int limitId, int answerId) {
            this.gameId = gameId;
            this.limitId = limitId;
            this.answerId = answerId;
        }

        public JSONObject getJSONObject(){
            JSONObject jsonObject = new JSONObject();

            try{
                jsonObject.put("limit", limitId);
                jsonObject.put("answer", answerId);
            }catch (JSONException e){
                e.printStackTrace();
            }

            return jsonObject;
        }
    }
}
