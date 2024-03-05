package com.coresaken.multiplication.fragment.game_settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.service.JsonFileReader;

import java.util.ArrayList;
import java.util.List;

public class GameSettingsOperationFragment extends Fragment {

    Settings presetSettings;
    ConstraintLayout cl_fragment_container;

    boolean[] selectedOperation;
    Button[] operationButtons;
    TextView info;

    public GameSettingsOperationFragment() {
        selectedOperation = new boolean[4];
    }

    public static GameSettingsOperationFragment newInstance() {
        return new GameSettingsOperationFragment();
    }

    public void loadPresetSettings(Settings presetSettings){
        this.presetSettings = presetSettings;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_settings_operation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        operationButtons = new Button[4];

        operationButtons[0] = view.findViewById(R.id.btn_operation_addition);
        operationButtons[1] = view.findViewById(R.id.btn_operation_subtraction);
        operationButtons[2] = view.findViewById(R.id.btn_operation_multiplication);
        operationButtons[3] = view.findViewById(R.id.btn_operation_division);

        info = view.findViewById(R.id.tv_operation_info);
        info.setVisibility(View.GONE);

        cl_fragment_container = view.findViewById(R.id.cl_fragment_container);

        if(presetSettings!=null && presetSettings.modeType == ModeType.CORRECTION){
            cl_fragment_container.setVisibility(View.GONE);
        }
        else{
            for(OperatorType operatorType:JsonFileReader.selectedOperators){
                selectOperation(operatorType.index);
            }
        }
    }

    public void selectOperation(View view){
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

    public void selectOperation(int id){
        boolean isSelected = !selectedOperation[id];
        selectedOperation[id] = isSelected;

        operationButtons[id].setSelected(isSelected);

        if(!isSelected){
            operationButtons[id].setTextColor(getContext().getColor(R.color.normal_text));
        }
        else{
            operationButtons[id].setTextColor(getContext().getColor(R.color.white));
        }
    }

    public boolean checkValue(ScrollView scrollView){
        if(SettingsController.getInstance().settings.operators.size()>0){
            return true;
        }

        info.setVisibility(View.VISIBLE);
        scrollView.smoothScrollTo(0, info.getScrollY());
        return false;
    }

    public void saveData(){
        Settings settings = SettingsController.getInstance().settings;

        if(settings.operators==null){
            settings.operators = new ArrayList<>();
        }

        settings.operators.clear();

        if(presetSettings!=null && presetSettings.operators!=null && presetSettings.operators.size()>0){
            settings.operators = presetSettings.operators;

            return;
        }

        List<Integer> selectedOperatorId = new ArrayList<>();

        if(selectedOperation[0]){
            settings.operators.add(OperatorType.ADDITION);
            selectedOperatorId.add(0);
        }
        if(selectedOperation[1]){
            settings.operators.add(OperatorType.SUBTRACTION);
            selectedOperatorId.add(1);
        }
        if(selectedOperation[2]){
            settings.operators.add(OperatorType.MULTIPLICATION);
            selectedOperatorId.add(2);
        }
        if(selectedOperation[3]){
            settings.operators.add(OperatorType.DIVISION);
            selectedOperatorId.add(3);
        }

        JsonFileReader.updateOperators(getContext(), settings.operators);
    }
}