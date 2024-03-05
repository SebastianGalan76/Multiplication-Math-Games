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
import com.coresaken.multiplication.data.enums.AnswerType;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.service.JsonFileReader;

import java.util.List;

public class GameSettingsAnswerTypeFragment extends Fragment {

    private AnswerType selectedAnswerType;
    public Settings presetSettings;

    Button btn_only_result;
    Button btn_mixed;
    ConstraintLayout cl_fragment_container;

    JsonFileReader.GameMode selectedGameModeSettings;
    int selectedAnswerId;

    private boolean hideFragment;

    public static GameSettingsAnswerTypeFragment newInstance() {
        return new GameSettingsAnswerTypeFragment();
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
        return inflater.inflate(R.layout.fragment_game_settings_answer_type, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btn_only_result = view.findViewById(R.id.btn_answer_type_only_result);
        btn_mixed = view.findViewById(R.id.btn_answer_type_mixed);

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

        List<AnswerType> allowedGameAnswerTypes = gameType.getAllowedAnswerTypes();

        if(!allowedGameAnswerTypes.contains(AnswerType.RESULT)){
            btn_only_result.setVisibility(View.GONE);
        }else{
            btn_only_result.setVisibility(View.VISIBLE);
        }

        if(!allowedGameAnswerTypes.contains(AnswerType.MIXED)){
            btn_mixed.setVisibility(View.GONE);
        }
        else{
            btn_mixed.setVisibility(View.VISIBLE);
        }

        if(allowedGameAnswerTypes.size()<=1){
            cl_fragment_container.setVisibility(View.GONE);
        }
        else{
            cl_fragment_container.setVisibility(View.VISIBLE);
        }

        selectAnswerType(gameMode.answerId);
    }

    public void changeType(View view){
        if(view.getId()==R.id.btn_answer_type_only_result){
            selectAnswerType(0);
        }
        else if(view.getId()==R.id.btn_answer_type_mixed){
            selectAnswerType(1);
        }
    }

    public void selectAnswerType(int id){
        selectedAnswerId = id;

        if(id == 0){
            if(selectedAnswerType != AnswerType.RESULT){
                selectedAnswerType = AnswerType.RESULT;

                btn_only_result.setEnabled(false);
                btn_mixed.setEnabled(true);

                btn_only_result.setTextColor(getContext().getColor(R.color.white));
                btn_mixed.setTextColor(getContext().getColor(R.color.normal_text));
            }
        }
        else if(id == 1){
            if(selectedAnswerType != AnswerType.MIXED){
                selectedAnswerType = AnswerType.MIXED;

                btn_only_result.setEnabled(true);
                btn_mixed.setEnabled(false);

                btn_only_result.setTextColor(getContext().getColor(R.color.normal_text));
                btn_mixed.setTextColor(getContext().getColor(R.color.white));
            }
        }
    }

    public void saveData(){
        Settings settings = SettingsController.getInstance().settings;
        if(presetSettings!=null && presetSettings.answerType!=null){
            settings.answerType = presetSettings.answerType;

            return;
        }

        if(selectedGameModeSettings!=null){
            selectedGameModeSettings.answerId = selectedAnswerId;
        }

        settings.answerType = selectedAnswerType;
    }
}