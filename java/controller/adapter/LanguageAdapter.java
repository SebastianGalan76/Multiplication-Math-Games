package com.coresaken.multiplication.controller.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.data.UserLocale;

public class LanguageAdapter extends ArrayAdapter<UserLocale> {
    public LanguageAdapter(@NonNull Context context, @NonNull UserLocale[] objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.spinner_language_item, parent, false
            );
        }

        ImageView flag = convertView.findViewById(R.id.img_flag);
        UserLocale locale = (UserLocale) getItem(position);
        if(locale!=null){
            flag.setImageDrawable(getContext().getDrawable(locale.flagId));
        }

        return convertView;
    }
}
