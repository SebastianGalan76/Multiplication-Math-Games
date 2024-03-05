package com.coresaken.multiplication.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.DailyReminderController;
import com.coresaken.multiplication.controller.LocaleController;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.controller.adapter.LanguageAdapter;
import com.coresaken.multiplication.controller.adapter.TextAdapter;
import com.coresaken.multiplication.data.User;

public class SettingsActivity extends AppCompatActivity {

    private final int POST_NOTIFICATION_REQUEST_CODE = 101;

    Spinner s_reminderHour, s_reminderMinute, s_ageRange, s_language;

    PlayerSettings playerSettings;
    EditText v_name;

    View reminderSwitch, soundsSwitch;

    Drawable switchChecked, switchUnchecked;
    SharedPreferences sharedPref;
    User user;

    Dialog creditsDialog;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sharedPref = getSharedPreferences("PlayerSettings", Context.MODE_PRIVATE);

        user = User.getInstance();
        playerSettings = PlayerSettings.getInstance();

        s_reminderHour = findViewById(R.id.spinner_hour);
        s_reminderMinute = findViewById(R.id.spinner_minute);
        s_ageRange = findViewById(R.id.spinner_age);
        s_language = findViewById(R.id.spinner_language);

        reminderSwitch = findViewById(R.id.img_reminderSwitch);
        soundsSwitch = findViewById(R.id.img_soundsSwitch);

        switchChecked = getDrawable(R.drawable.settings_switch_checked);
        switchUnchecked = getDrawable(R.drawable.settings_switch_unchecked);

        //Name
        v_name = findViewById(R.id.v_name);
        v_name.setText(user.name);

        //Reminder
        loadReminderSettings();
        loadSoundsSettings();

        //Age
        /*String[] ageRange = new String[]{" - ", "0 - 7", "8 - 13", "14 - 18", "+18"};
        TextAdapter ageAdapter = new TextAdapter(this, ageRange);
        ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s_ageRange.setAdapter(ageAdapter);
        s_ageRange.setSelection(user.ageRange + 1);
        s_ageRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                user.ageRange = i - 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/



        //Language
        LanguageAdapter languageAdapter = new LanguageAdapter(this, LocaleController.getInstance().locales);
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        s_language.setAdapter(languageAdapter);
        s_language.setSelection(playerSettings.language);
        s_language.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectLocale(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                closeActivity(null);
            }
        });
    }

    public void switchReminder(View view){
        SoundController.getInstance().clickButton();

        boolean currentValue = playerSettings.reminder;

        currentValue = !currentValue;

        if(currentValue){
            if(DailyReminderController.checkPermission(this)){
                reminderSwitch.setBackground(switchChecked);
                DailyReminderController.startNotification(this);

                playerSettings.reminder = true;
            }
            else{
                reminderSwitch.setBackground(switchUnchecked);
                ActivityCompat.requestPermissions(this,
                        new String[]{"android.permission.POST_NOTIFICATIONS"},
                        POST_NOTIFICATION_REQUEST_CODE);

                playerSettings.reminder = false;
            }
        }
        else{
            DailyReminderController.stopNotification(this);
            playerSettings.reminder = false;
            reminderSwitch.setBackground(switchUnchecked);
        }

    }
    public void switchSounds(View view){
        SoundController.getInstance().clickButton();

        boolean currentValue = playerSettings.sounds;

        currentValue = !currentValue;

        SoundController.getInstance().enable(currentValue);

        if(currentValue){
            soundsSwitch.setBackground(switchChecked);
        }
        else{
            soundsSwitch.setBackground(switchUnchecked);
        }


        playerSettings.sounds = currentValue;
    }

    public void selectLocale(int languageIndex){
        if(languageIndex!=playerSettings.language){
            playerSettings.language = languageIndex;
            recreate();
        }

        LocaleController.getInstance().selectLocale(this, languageIndex);
    }

    public void rateUs(View view){
        SoundController.getInstance().clickButton();

        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.coresaken.multiplication")));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=com.coresaken.multiplication")));
        }

        user.rateStatus = 1;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(creditsDialog!=null){
            creditsDialog.dismiss();
        }
    }

    public void credits(View view){
        SoundController.getInstance().clickButton();

        creditsDialog();
    }

    public void closeActivity(View view){
        SoundController.getInstance().clickButton();
        saveData();

        finish();
    }



    private void saveData(){
        String userName = v_name.getText().toString();
        user.name = userName;

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean("reminder", playerSettings.reminder);
        editor.putInt("reminderHour", playerSettings.reminderTime[0]);
        editor.putInt("reminderMinute", playerSettings.reminderTime[1]);
        editor.putInt("language", playerSettings.language);
        editor.putBoolean("sounds", playerSettings.sounds);
        editor.apply();


        SharedPreferences sharedPrefUser = getSharedPreferences("User", Context.MODE_PRIVATE);
        SharedPreferences.Editor editorUser = sharedPrefUser.edit();
        editorUser.putString("name", userName);
        //editorUser.putInt("ageRange", user.ageRange);
        editorUser.apply();
    }

    public void loadReminderSettings(){
        if(playerSettings.reminder){
            reminderSwitch.setBackground(switchChecked);
        }
        else{
            reminderSwitch.setBackground(switchUnchecked);
        }

        String[] hours = new String[24];
        for(int i=0;i<24;i++){
            hours[i] = String.valueOf(i);
        }

        String[] minutes = new String[60];
        for(int i=0;i<60;i++){
            minutes[i] = String.valueOf(i);
        }

        TextAdapter hourAdapter = new TextAdapter(this, hours);
        TextAdapter minuteAdapter = new TextAdapter(this, minutes);

        hourAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minuteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        s_reminderHour.setAdapter(hourAdapter);
        s_reminderHour.setSelection(playerSettings.reminderTime[0]);
        s_reminderHour.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                playerSettings.reminderTime[0] = Integer.valueOf((String) adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        s_reminderMinute.setAdapter(minuteAdapter);
        s_reminderMinute.setSelection(playerSettings.reminderTime[1]);
        s_reminderMinute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                playerSettings.reminderTime[1] = Integer.valueOf((String) adapterView.getItemAtPosition(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    public void loadSoundsSettings(){
        if(playerSettings.sounds){
            soundsSwitch.setBackground(switchChecked);
        }
        else{
            soundsSwitch.setBackground(switchUnchecked);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == POST_NOTIFICATION_REQUEST_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                DailyReminderController.startNotification(this);
                reminderSwitch.setBackground(getDrawable(R.drawable.settings_switch_checked));

                playerSettings.reminder = true;
            }
            else{
                reminderSwitch.setBackground(getDrawable(R.drawable.settings_switch_unchecked));

                playerSettings.reminder = false;
            }
        }
    }

    private void creditsDialog(){
        creditsDialog = new Dialog(this, R.style.DimOverlay);

        creditsDialog.setContentView(R.layout.dialog_credits);
        Window window = creditsDialog.getWindow();
        window.setBackgroundDrawableResource(R.color.dialog_bg);

        ImageButton btn_close = creditsDialog.findViewById(R.id.btn_close);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SoundController.getInstance().clickButton();

                creditsDialog.cancel();
                creditsDialog.dismiss();
            }
        });

        creditsDialog.show();
    }
}