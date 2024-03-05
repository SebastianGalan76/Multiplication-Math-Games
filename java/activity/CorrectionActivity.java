package com.coresaken.multiplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.CorrectionController;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.controller.adapter.CorrectionAdapter;
import com.coresaken.multiplication.data.CorrectionEquation;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.service.SQLiteHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CorrectionActivity extends AppCompatActivity {

    SQLiteHelper sqLiteHelper;
    RecyclerView recyclerView;
    CorrectionAdapter correctionAdapter;
    LinearLayoutManager layoutManager;

    boolean[] selectedOperation;
    Button[] operationButtons;
    TextView  tv_total;

    Button startButton;

    List<CorrectionEquation> equations;

    int equationAmount;
    SharedPreferences sharedPref;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_correction);

        sharedPref = getPreferences(Context.MODE_PRIVATE);

        sqLiteHelper = new SQLiteHelper(this);
        recyclerView = findViewById(R.id.rv_container);
        tv_total = findViewById(R.id.tv_total);

        selectedOperation = new boolean[4];
        operationButtons = new Button[4];

        operationButtons[0] = findViewById(R.id.btn_operation_addition);
        operationButtons[1] = findViewById(R.id.btn_operation_subtraction);
        operationButtons[2] = findViewById(R.id.btn_operation_multiplication);
        operationButtons[3] = findViewById(R.id.btn_operation_division);
        startButton = findViewById(R.id.btn_startCorrection);

        if(sharedPref.getBoolean("addition", true)){
            selectOperation(0);
        }
        if(sharedPref.getBoolean("subtraction", true)){
            selectOperation(1);
        }
        if(sharedPref.getBoolean("multiplication", true)){
            selectOperation(2);
        }
        if(sharedPref.getBoolean("division", true)){
            selectOperation(3);
        }

        refreshRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();

        refreshRecyclerView();
    }

    public void startCorrection(View view){
        SoundController.getInstance().clickButton();

        Settings.SettingsBuilder settingsBuilder = new Settings.SettingsBuilder().setModeType(ModeType.CORRECTION);

        for(OperatorType operatorType:getOperators()){
            settingsBuilder.addOperator(operatorType);
        }

        SettingsController.getInstance().presetSettings = settingsBuilder.build();
        startActivity(new Intent(this, GameSettingsActivity.class));

        CorrectionController.getInstance().selectedEquation = equations;
    }

    public void selectOperation(View view){
        SoundController.getInstance().clickButton();

        int index = 0;

        if(view.getId()==R.id.btn_operation_subtraction){
            index = 1;
        }
        else if(view.getId()==R.id.btn_operation_multiplication){
            index = 2;
        }
        else if(view.getId()==R.id.btn_operation_division){
            index = 3;
        }

        selectOperation(index);
    }

    private void selectOperation(int index){
        boolean isSelected = !selectedOperation[index];
        selectedOperation[index] = isSelected;

        operationButtons[index].setSelected(isSelected);

        if(!isSelected){
            operationButtons[index].setTextColor(getColor(R.color.normal_text));
        }
        else{
            operationButtons[index].setTextColor(getColor(R.color.white));
        }

        refreshRecyclerView();
    }

    public void closeActivity(View view){
        SoundController.getInstance().clickButton();

        finish();
    }

    private void refreshRecyclerView(){
        equations = sqLiteHelper.getCorrectionEquations(getOperators());
        equationAmount = equations.size();
        tv_total.setText(String.valueOf(equationAmount));

        if(equationAmount<=0){
            startButton.setVisibility(View.INVISIBLE);
        }
        else{
            startButton.setVisibility(View.VISIBLE);
        }

        recyclerView = findViewById(R.id.rv_container);
        correctionAdapter = new CorrectionAdapter(equations, this);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(correctionAdapter);
    }

    private List<OperatorType> getOperators(){
        List<OperatorType> operatorTypes = new ArrayList<>();

        if(selectedOperation[0]){
            operatorTypes.add(OperatorType.ADDITION);
        }
        if(selectedOperation[1]){
            operatorTypes.add(OperatorType.SUBTRACTION);
        }
        if(selectedOperation[2]){
            operatorTypes.add(OperatorType.MULTIPLICATION);
        }
        if(selectedOperation[3]){
            operatorTypes.add(OperatorType.DIVISION);
        }

        return operatorTypes;
    }

    private void saveData(){
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("addition", selectedOperation[0]);
        editor.putBoolean("subtraction", selectedOperation[1]);
        editor.putBoolean("multiplication", selectedOperation[2]);
        editor.putBoolean("division", selectedOperation[3]);

        editor.apply();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        saveData();
    }
}