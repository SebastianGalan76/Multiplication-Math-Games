package com.coresaken.multiplication.fragment.game_settings;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.SettingsController;
import com.coresaken.multiplication.data.Settings;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.ModeType;

public class GameSettingsModeFragment extends Fragment {
    GameType selectedGameType;
    ImageButton[] buttons = new ImageButton[9];
    public Settings presetSettings;

    TextView info;

    public static GameSettingsModeFragment newInstance() {
        return new GameSettingsModeFragment();
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
        return inflater.inflate(R.layout.fragment_game_settings_mode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        buttons[0] = view.findViewById(R.id.btn_mode_0);
        buttons[1] = view.findViewById(R.id.btn_mode_1);
        buttons[2] = view.findViewById(R.id.btn_mode_2);
        buttons[3] = view.findViewById(R.id.btn_mode_3);
        buttons[4] = view.findViewById(R.id.btn_mode_4);
        buttons[5] = view.findViewById(R.id.btn_mode_5);
        buttons[6] = view.findViewById(R.id.btn_mode_6);
        buttons[7] = view.findViewById(R.id.btn_mode_7);
        buttons[8] = view.findViewById(R.id.btn_mode_8);

        info = view.findViewById(R.id.tv_mode_info);

        if(presetSettings!=null){
            if(presetSettings.gameType!=null){
                ConstraintLayout cl_fragment_container = view.findViewById(R.id.cl_fragment_container);
                cl_fragment_container.setVisibility(View.GONE);
            }

            if(presetSettings.modeType== ModeType.CORRECTION){
                buttons[4].setVisibility(View.INVISIBLE);
            }
            else{
                buttons[4].setVisibility(View.INVISIBLE);
            }
        }
    }

    public boolean checkValue(ScrollView scrollView){
        if(SettingsController.getInstance().settings.gameType != null){
            return true;
        }

        info.setVisibility(View.VISIBLE);
        scrollView.smoothScrollTo(0, info.getScrollY());
        return false;
    }

    public void changeType(View view){
        if(view.getId()==R.id.btn_mode_0){
            enableAllButtons();
            selectedGameType = GameType.GAME1;

            view.setEnabled(false);
        }
        else if(view.getId()==R.id.btn_mode_1){
            enableAllButtons();
            selectedGameType = GameType.TRUE_FALSE;

            view.setEnabled(false);
        }
        else if(view.getId()==R.id.btn_mode_2){
            enableAllButtons();
            selectedGameType = GameType.INPUT;

            view.setEnabled(false);
        }
        else if(view.getId()==R.id.btn_mode_3){
            enableAllButtons();
            selectedGameType = GameType.PUZZLE;

            view.setEnabled(false);
        }
        else if(view.getId()==R.id.btn_mode_4){
            enableAllButtons();
            selectedGameType = GameType.DUEL;

            view.setEnabled(false);
        }
        else if(view.getId()==R.id.btn_mode_5){
            enableAllButtons();
            selectedGameType = GameType.LAVA;

            view.setEnabled(false);
        }
        else if(view.getId()==R.id.btn_mode_6){
            enableAllButtons();
            selectedGameType = GameType.MATCH;

            view.setEnabled(false);
        }
        else if(view.getId()==R.id.btn_mode_7){
            enableAllButtons();
            selectedGameType = GameType.CARD;

            view.setEnabled(false);
        }
        else if(view.getId()==R.id.btn_mode_8){
            enableAllButtons();
            selectedGameType = GameType.DRAW;

            view.setEnabled(false);
        }
    }

    private void enableAllButtons(){
        for(int i=0;i<buttons.length;i++){
            if(buttons[i]!=null){
                buttons[i].setEnabled(true);
            }
        }
    }

    public void saveData(){
        Settings settings = SettingsController.getInstance().settings;
        if(presetSettings!=null && presetSettings.gameType!=null){
            settings.gameType = presetSettings.gameType;

            return;
        }

        settings.gameType = selectedGameType;
    }
}