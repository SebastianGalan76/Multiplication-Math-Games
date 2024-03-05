package com.coresaken.multiplication.fragment.game;

import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.game.GameCardActivity;
import com.coresaken.multiplication.controller.GameController;
import com.coresaken.multiplication.data.AnsweredEquation;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.data.enums.GameType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AnsweredEquationFragment extends Fragment {
    ConstraintLayout cl_equationContainer;

    TextView tv_wrongAnswers;
    ConstraintLayout cl_equation;
    View[] lines;
    ImageView icon;

    int textSize = 20;
    HashMap<Integer, View> views;

    AnsweredEquation answeredEquation;
    GameCardActivity.AnsweredEquation answeredEquationCard;
    boolean makeLineRed;

    public AnsweredEquationFragment() {
        views = new HashMap<>();
    }

    public static AnsweredEquationFragment newInstance() {
        return new AnsweredEquationFragment();
    }

    public void setAnsweredEquation(AnsweredEquation answeredEquation, boolean makeLineRed){
        this.answeredEquation = answeredEquation;
        this.makeLineRed = makeLineRed;
    }

    public void setAnsweredEquation(GameCardActivity.AnsweredEquation answeredEquationCard, boolean makeLineRed){
        this.answeredEquationCard = answeredEquationCard;
        this.makeLineRed = makeLineRed;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_answered_equation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        cl_equationContainer = view.findViewById(R.id.cl_equation_container);

        tv_wrongAnswers = view.findViewById(R.id.tv_wrong_answers);
        cl_equation = view.findViewById(R.id.cl_equation);

        lines = new View[2];
        lines[1] = view.findViewById(R.id.view_line_down);

        icon = view.findViewById(R.id.i_icon);

        GameController gameController = GameController.getInstance();

        if(gameController.gameType == GameType.CARD || gameController.gameType == GameType.MATCH){
            tv_wrongAnswers.setVisibility(View.INVISIBLE);

            initializeCard();
        }
        else{
            initialize();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void initializeCard(){
        if(!answeredEquationCard.isCorrect){
            cl_equationContainer.setBackgroundColor(getResources().getColor(R.color.incorrect_equation));
            makeLineRed = true;

            icon.setImageDrawable(getResources().getDrawable(R.drawable.outline_highlight_off_24));
            icon.setColorFilter(getResources().getColor(R.color.correction));
        }
        else{
            tv_wrongAnswers.setVisibility(View.GONE);
            icon.setImageDrawable(getResources().getDrawable(R.drawable.round_check_circle_outline_24));
            icon.setColorFilter(getResources().getColor(R.color.correct_text));
        }

        createEquationCard();

        if(makeLineRed){
            lines[0].setBackgroundColor(getResources().getColor(R.color.incorrect_line));
            lines[1].setBackgroundColor(getResources().getColor(R.color.incorrect_line));
        }
        else{
            lines[0].setBackgroundColor(getResources().getColor(R.color.correct_line));
            lines[1].setBackgroundColor(getResources().getColor(R.color.correct_line));
        }
    }
    private void createEquationCard(){
        Context context = cl_equation.getContext();
        ConstraintSet set;

        List<String> elements = new ArrayList<>();
        elements.add(answeredEquationCard.values[0]);

        if(answeredEquationCard.isCorrect){
            elements.add(" = ");
        }
        else{
            elements.add(" ≠ ");
        }

        elements.add(answeredEquationCard.values[1]);

        int viewIndex = 0;
        for(int i=0;i<elements.size();i++){
            View view;
            int viewId = View.generateViewId();

            view = new TextView(context);
            view.setId(viewId);

            view.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            TextView textView = (TextView)view;

            textView.setText(elements.get(i));

            textView.setTextSize(textSize);
            textView.setTextColor(getResources().getColor(R.color.gray1));

            if(i == 1){
                textView.setTypeface(Typeface.DEFAULT_BOLD);
            }

            views.put(viewIndex, view);
            cl_equation.addView(view);

            int previousViewId = getViewId(viewIndex - 1);
            set = new ConstraintSet();
            set.clone(cl_equation);

            set.connect(viewId, ConstraintSet.TOP, previousViewId, ConstraintSet.TOP);
            set.connect(viewId, ConstraintSet.BOTTOM, previousViewId, ConstraintSet.BOTTOM);

            if(viewIndex==0){
                set.connect(viewId, ConstraintSet.LEFT, previousViewId, ConstraintSet.LEFT);
            }
            else{
                set.connect(viewId, ConstraintSet.LEFT, previousViewId, ConstraintSet.RIGHT);
            }

            set.applyTo(cl_equation);
            viewIndex++;
        }
    }

    private void initialize(){
        List<AnsweredEquation.Answer> wrongAnswersList = new ArrayList<>();
        for(AnsweredEquation.Answer answer : answeredEquation.answers){
            if(!answer.isCorrect){
                wrongAnswersList.add(answer);
            }
        }

        if(!answeredEquation.isCorrect){
            StringBuilder wrongAnswers = new StringBuilder();
            for(int i=0;i<wrongAnswersList.size();i++){
                if(i!= wrongAnswersList.size() - 1){
                    wrongAnswers.append(wrongAnswersList.get(i).value).append(", ");
                }
                else{
                    wrongAnswers.append(wrongAnswersList.get(i).value);
                }
            }

            tv_wrongAnswers.setText(wrongAnswers.toString());

            cl_equationContainer.setBackgroundColor(getResources().getColor(R.color.incorrect_equation));

            makeLineRed = true;

            icon.setImageDrawable(getResources().getDrawable(R.drawable.outline_highlight_off_24));
            icon.setColorFilter(getResources().getColor(R.color.correction));
        }
        else{
            tv_wrongAnswers.setVisibility(View.GONE);
            icon.setImageDrawable(getResources().getDrawable(R.drawable.round_check_circle_outline_24));
            icon.setColorFilter(getResources().getColor(R.color.correct_text));
        }

        createEquation(answeredEquation.equation);

        if(makeLineRed){
            lines[0].setBackgroundColor(getResources().getColor(R.color.correct_line));
            lines[1].setBackgroundColor(getResources().getColor(R.color.correct_line));
        }
        else{
            lines[0].setBackgroundColor(getResources().getColor(R.color.correct_line));
            lines[1].setBackgroundColor(getResources().getColor(R.color.correct_line));
        }
    }

    private void createEquation(UnknownEquation equation){
        Context context = cl_equation.getContext();
        ConstraintSet set;

        List<Equation.Element> elements = equation.equation.getElements();
        int unknownElementIndex = equation.getUnknownElementIndex();

        int viewIndex = 0;
        for(int i=0;i<elements.size();i++){
            View view;
            int viewId = View.generateViewId();

            if(i == unknownElementIndex){
                view = new TextView(context);
                view.setId(viewId);

                view.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView textView = (TextView)view;

                if(i == elements.size()-1){
                    textView.setText(String.valueOf(equation.getUnknownElementValue()));
                }
                else{
                    textView.setText(equation.getUnknownElementValue()+" ");
                }

                textView.setTextSize(textSize);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setTextColor(getResources().getColor(R.color.correct_text));
            }
            else{
                StringBuilder equationPart = new StringBuilder();

                for(;i<elements.size();i++){
                    if(i == unknownElementIndex){
                        i--;
                        break;
                    }
                    equationPart.append(elements.get(i).toString());

                    if(i != elements.size() - 1){
                        equationPart.append(" ");
                    }
                }

                view = new TextView(context);
                view.setId(viewId);

                view.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView textView = (TextView)view;

                textView.setText(equationPart.toString());

                textView.setTextSize(textSize);
                textView.setTextColor(getResources().getColor(R.color.gray1));
            }

            views.put(viewIndex, view);
            cl_equation.addView(view);

            int previousViewId = getViewId(viewIndex - 1);
            set = new ConstraintSet();
            set.clone(cl_equation);

            set.connect(viewId, ConstraintSet.TOP, previousViewId, ConstraintSet.TOP);
            set.connect(viewId, ConstraintSet.BOTTOM, previousViewId, ConstraintSet.BOTTOM);

            if(viewIndex==0){
                set.connect(viewId, ConstraintSet.LEFT, previousViewId, ConstraintSet.LEFT);
            }
            else{
                set.connect(viewId, ConstraintSet.LEFT, previousViewId, ConstraintSet.RIGHT);
            }

            set.applyTo(cl_equation);
            viewIndex++;
        }
    }
    private int getViewId(int currentViewIndex){
        if(currentViewIndex>=0){
            if(currentViewIndex>= views.size()){
                return -1;
            }

            return views.get(currentViewIndex).getId();
        }
        else{
            return cl_equation.getId();
        }
    }
}