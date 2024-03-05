package com.coresaken.multiplication.controller;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.data.UserLocale;

import java.util.Locale;

public class LocaleController {
    private static LocaleController _instance;

    public UserLocale[] locales;

    private LocaleController(){
        locales = new UserLocale[6];
        locales[0] = new UserLocale(R.drawable.flag_default, "system");
        locales[1] = new UserLocale(R.drawable.flag_gb, "en");
        locales[2] = new UserLocale(R.drawable.flag_es, "es");
        locales[3] = new UserLocale(R.drawable.flag_pl, "pl");
        locales[4] = new UserLocale(R.drawable.flag_no, "no");
        locales[5] = new UserLocale(R.drawable.flag_de, "de");
    }

    public void selectLocale(Activity activity, int languageIndex){
        Locale locale;
        if(languageIndex<=0){
            locale = Resources.getSystem().getConfiguration().getLocales().get(0);
        }
        else{
            locale = new Locale(locales[languageIndex].localeCode);
        }

        locale.setDefault(locale);

        Resources resources = activity.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(locale);
        resources.updateConfiguration(config, resources.getDisplayMetrics());
    }

    public static LocaleController getInstance(){
        if(_instance==null){
            _instance = new LocaleController();
        }

        return _instance;
    }
}
