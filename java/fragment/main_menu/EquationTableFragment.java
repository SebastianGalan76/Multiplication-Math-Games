package com.coresaken.multiplication.fragment.main_menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.data.Equation;

import com.coresaken.multiplication.listener.ViewCreatedListener;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;
import com.udojava.evalex.Expression;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class EquationTableFragment extends Fragment {
    private static final String ARG_EQUATION = "equation";

    ImageView[] stars;
    TextView tv_equation;
    String equation;

    SQLiteHelper sqLiteHelper;
    String equationId;

    public static EquationTableFragment newInstance(String equation) {
        EquationTableFragment fragment = new EquationTableFragment();
        Bundle args = new Bundle();
        args.putString(ARG_EQUATION, equation);
        fragment.setArguments(args);

        return fragment;
    }

    public void initialize(SQLiteHelper sqLiteHelper, String equationId){
        this.sqLiteHelper = sqLiteHelper;
        this.equationId = equationId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            equation = getArguments().getString(ARG_EQUATION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equation_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        stars = new ImageView[5];

        stars[0] = view.findViewById(R.id.img_star0);
        stars[1] = view.findViewById(R.id.img_star1);
        stars[2] = view.findViewById(R.id.img_star2);
        stars[3] = view.findViewById(R.id.img_star3);
        stars[4] = view.findViewById(R.id.img_star4);

        tv_equation = view.findViewById(R.id.tv_equation);
        tv_equation.setText(equation);
        loadStars();
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadStars(){
        int starAmount = Utils.getStarAmount(sqLiteHelper.getPoints(equationId));

        for(int i=0;i<starAmount;i++){
            stars[i].setImageDrawable(getResources().getDrawable(R.drawable.star_gold));
        }
        for(int i=starAmount;i<5;i++){
            stars[i].setImageDrawable(getResources().getDrawable(R.drawable.star_blue));
        }
    }
}