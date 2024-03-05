package com.coresaken.multiplication.fragment.game_settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.LimitType;
import com.coresaken.multiplication.service.JsonFileReader;

import java.util.List;


public class GameSettingsLimitFragment extends Fragment {
    private LimitType selectedLimitType;
    public Settings presetSettings;

    Button btn_equation, btn_heart, btn_time;
    ConstraintLayout cl_fragment_container;

    private boolean hideFragment;

    JsonFileReader.GameMode selectedGameModeSettings;
    int selectedLimitId;

    public static GameSettingsLimitFragment newInstance() {
        return new GameSettingsLimitFragment();
    }

    public void loadPresetSettings(Settings presetSettings){
        this.presetSettings = presetSettings;
    }

    public void loadSettingsForGameType(JsonFileReader.GameMode gameModeSettings){
        this.selectedGameModeSettings = gameModeSettings;
    }

    public void hideFragment(){
        hideFragment = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game_settings_limit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_equation = view.findViewById(R.id.btn_limit_equation);
        btn_heart = view.findViewById(R.id.btn_limit_heart);
        btn_time = view.findViewById(R.id.btn_limit_time);

        cl_fragment_container = view.findViewById(R.id.cl_fragment_container);

        if(hideFragment){
            cl_fragment_container.setVisibility(View.GONE);
        }

        if(presetSettings!=null && presetSettings.gameType!=null){
           changeMode(presetSettings.gameType, selectedGameModeSettings);
        }
    }

    public void changeMode(GameType gameType, JsonFileReader.GameMode gameMode){
        this.selectedGameModeSettings = gameMode;
        List<LimitType> allowedGameLimit = gameType.getAllowedLimits();

        if(!allowedGameLimit.contains(LimitType.EQUATION)){
            btn_equation.setVisibility(View.GONE);
        }
        else{
            btn_equation.setVisibility(View.VISIBLE);
        }
        if(!allowedGameLimit.contains(LimitType.HEART)){
            btn_heart.setVisibility(View.GONE);
        }
        else{
            btn_heart.setVisibility(View.VISIBLE);
        }
        if(!allowedGameLimit.contains(LimitType.TIME)){
            btn_time.setVisibility(View.GONE);
        }
        else{
            btn_time.setVisibility(View.VISIBLE);
        }

        if(allowedGameLimit.size()<=1){
            cl_fragment_container.setVisibility(View.GONE);
        }
        else{
            cl_fragment_container.setVisibility(View.VISIBLE);
        }

        selectLimit(gameMode.limitId);
    }

    public void changeType(View view){
        if(view.getId()==R.id.btn_limit_equation){
            selectLimit(0);
        } else if(view.getId()==R.id.btn_limit_heart){
            selectLimit(1);
        }else if(view.getId()==R.id.btn_limit_time){
            selectLimit(2);
        }
    }

    public void selectLimit(int id){
        selectedLimitId = id;

        if(id == 0){
            if(selectedLimitType != LimitType.EQUATION){
                selectedLimitType = LimitType.EQUATION;

                btn_equation.setEnabled(false);
                btn_heart.setEnabled(true);
                btn_time.setEnabled(true);

                btn_equation.setTextColor(getContext().getColor(R.color.white));
                btn_heart.setTextColor(getContext().getColor(R.color.normal_text));
                btn_time.setTextColor(getContext().getColor(R.color.normal_text));
            }
        }
        else if(id == 1){
            if(selectedLimitType != LimitType.HEART){
                selectedLimitType = LimitType.HEART;

                btn_equation.setEnabled(true);
                btn_heart.setEnabled(false);
                btn_time.setEnabled(true);

                btn_equation.setTextColor(getContext().getColor(R.color.normal_text));
                btn_heart.setTextColor(getContext().getColor(R.color.white));
                btn_time.setTextColor(getContext().getColor(R.color.normal_text));
            }
        }else if(id == 2){
            if(selectedLimitType != LimitType.TIME){
                selectedLimitType = LimitType.TIME;

                btn_equation.setEnabled(true);
                btn_heart.setEnabled(true);
                btn_time.setEnabled(false);

                btn_equation.setTextColor(getContext().getColor(R.color.normal_text));
                btn_heart.setTextColor(getContext().getColor(R.color.normal_text));
                btn_time.setTextColor(getContext().getColor(R.color.white));
            }
        }
    }

    public void saveData(){
        Settings settings = SettingsController.getInstance().settings;

        if(presetSettings!=null && presetSettings.limitType!=null){
            settings.limitType = presetSettings.limitType;

            return;
        }

        if(selectedGameModeSettings!=null){
            selectedGameModeSettings.limitId = selectedLimitId;
        }

        settings.limitType = selectedLimitType;
    }
}