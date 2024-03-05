package com.coresaken.multiplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.fragment.game_settings.GameSettingsAnswerTypeFragment;
import com.coresaken.multiplication.fragment.game_settings.GameSettingsLimitFragment;
import com.coresaken.multiplication.fragment.game_settings.GameSettingsModeFragment;
import com.coresaken.multiplication.fragment.game_settings.GameSettingsOperationFragment;
import com.coresaken.multiplication.fragment.game_settings.GameSettingsRangeFragment;
import com.coresaken.multiplication.service.JsonFileReader;

public class GameSettingsActivity extends AppCompatActivity {

    GameSettingsOperationFragment operationFragment;
    GameSettingsRangeFragment rangeFragment;
    GameSettingsModeFragment modeFragment;
    GameSettingsAnswerTypeFragment answerTypeFragment;
    GameSettingsLimitFragment limitFragment;

    ScrollView scrollView;

    JsonFileReader.GameMode selectedModeSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_settings);

        scrollView = findViewById(R.id.sv_settings);
        JsonFileReader.loadGameSettings(this);

        operationFragment = GameSettingsOperationFragment.newInstance();
        rangeFragment = GameSettingsRangeFragment.newInstance();
        modeFragment = GameSettingsModeFragment.newInstance();
        answerTypeFragment = GameSettingsAnswerTypeFragment.newInstance();
        limitFragment = GameSettingsLimitFragment.newInstance();

        Settings presetSettings = SettingsController.getInstance().presetSettings;
        if(presetSettings !=null){
            operationFragment.loadPresetSettings(presetSettings);
            rangeFragment.loadPresetSettings(presetSettings);
            modeFragment.loadPresetSettings(presetSettings);
            limitFragment.loadPresetSettings(presetSettings);
            answerTypeFragment.loadPresetSettings(presetSettings);

            if(presetSettings.gameType!=null){
                selectedModeSettings = getGameModeSettingsByGameType(presetSettings.gameType);

                limitFragment.loadSettingsForGameType(selectedModeSettings);
                answerTypeFragment.loadSettingsForGameType(selectedModeSettings);
            }
            else{
                limitFragment.hideFragment();
                answerTypeFragment.hideFragment();
            }
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.cl_operation, operationFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.cl_range, rangeFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.cl_mode, modeFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.cl_answer_type, answerTypeFragment).commit();
        getSupportFragmentManager().beginTransaction().replace(R.id.cl_limit, limitFragment).commit();
    }

    public void changeType(View view){
        SoundController.getInstance().clickButton();

        rangeFragment.changeType(view);
        modeFragment.changeType(view);
        answerTypeFragment.changeType(view);
        limitFragment.changeType(view);
    }

    public void selectGame(View view){
        SoundController.getInstance().clickButton();

        modeFragment.changeType(view);

        if(view.getId()==R.id.btn_mode_0){
            selectGameMode(GameType.GAME1);
        }else if(view.getId()==R.id.btn_mode_1){
            selectGameMode(GameType.TRUE_FALSE);
        }else if(view.getId()==R.id.btn_mode_2){
            selectGameMode(GameType.INPUT);
        } else if(view.getId()==R.id.btn_mode_3){
            selectGameMode(GameType.PUZZLE);
        }else if(view.getId()==R.id.btn_mode_4){
            selectGameMode(GameType.DUEL);
        }else if(view.getId()==R.id.btn_mode_5){
            selectGameMode(GameType.LAVA);
        }else if(view.getId()==R.id.btn_mode_6){
            selectGameMode(GameType.MATCH);
        } else if(view.getId()==R.id.btn_mode_7){
            selectGameMode(GameType.CARD);
        } else if(view.getId()==R.id.btn_mode_8){
            selectGameMode(GameType.DRAW);
        }
    }

    private void selectGameMode(GameType gameType){
        selectedModeSettings = getGameModeSettingsByGameType(gameType);
        limitFragment.changeMode(gameType, selectedModeSettings);
        answerTypeFragment.changeMode(gameType, selectedModeSettings);
    }

    private JsonFileReader.GameMode getGameModeSettingsByGameType(GameType gameType){
        return JsonFileReader.gameModeSettings.get(gameType.index);
    }

    public void selectValue(View view){
        SoundController.getInstance().clickButton();

        operationFragment.selectOperation(view);
    }

    public void startGame(View view){
        SoundController.getInstance().clickButton();

        saveData();

        if(!rangeFragment.checkRange(scrollView)){
            return;
        }
        if(!operationFragment.checkValue(scrollView)){
            return;
        }
        if(!modeFragment.checkValue(scrollView)){
            return;
        }


        startActivity(new Intent(this, SettingsController.getInstance().getActivity()));
        finish();

        SettingsController.getInstance().presetSettings = null;

        JsonFileReader.updateGameMode(this, selectedModeSettings);
    }

    public void closeActivity(View view){
        SoundController.getInstance().clickButton();
        saveData();

        finish();
    }

    public void saveData(){
        Settings settings = new Settings();

        if(SettingsController.getInstance().presetSettings!=null){
            settings.modeType = SettingsController.getInstance().presetSettings.modeType;
        }

        SettingsController.getInstance().settings = settings;

        operationFragment.saveData();
        rangeFragment.saveData();
        modeFragment.saveData();
        answerTypeFragment.saveData();
        limitFragment.saveData();
    }
}