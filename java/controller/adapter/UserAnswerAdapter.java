package com.coresaken.multiplication.controller.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.game.GameCardActivity;
import com.coresaken.multiplication.data.AnsweredEquation;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserAnswerAdapter extends RecyclerView.Adapter<UserAnswerAdapter.ViewHolder>{
    List<AnsweredEquation> dataSet;
    List<GameCardActivity.AnsweredEquation> dataSetCard;
    List<UserAnswerAdapter.ViewHolder> viewHolders;
    Context context;

    boolean[] makeLineRed;
    boolean cardType;
    GameType gameType;

    public UserAnswerAdapter(List<AnsweredEquation> dataSet, Context context, GameType gameType) {
        this.dataSet = dataSet;
        this.context = context;
        this.gameType = gameType;

        makeLineRed = new boolean[dataSet.size()];

        for(int i=0;i<dataSet.size();i++){
            boolean correct = dataSet.get(i).isCorrect;

            makeLineRed[i] = !correct;

            if(i>0 && !correct){
                makeLineRed[i-1] = true;
            }
        }

        viewHolders = new ArrayList<>();
    }

    public UserAnswerAdapter(List<GameCardActivity.AnsweredEquation> dataSet, Context context, GameType gameType, boolean card) {
        this.dataSetCard = dataSet;
        this.context = context;
        this.gameType = gameType;
        this.cardType = true;

        makeLineRed = new boolean[dataSetCard.size()];

        for(int i=0;i<dataSetCard.size();i++){
            boolean correct = dataSetCard.get(i).isCorrect;

            makeLineRed[i] = !correct;

            if(i>0 && !correct){
                makeLineRed[i-1] = true;
            }
        }

        viewHolders = new ArrayList<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_answered_equation, viewGroup, false);

        UserAnswerAdapter.ViewHolder viewHolder = new UserAnswerAdapter.ViewHolder(view);
        viewHolders.add(viewHolder);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        if(cardType){
            GameCardActivity.AnsweredEquation answeredEquation = dataSetCard.get(position);

            viewHolder.onBindView(context, answeredEquation, makeLineRed[position]);
        }
        else{
            AnsweredEquation answeredEquation = dataSet.get(position);

            viewHolder.onBindView(context, answeredEquation, gameType, makeLineRed[position]);
        }
    }

    @Override
    public int getItemCount() {
        if(cardType){
            return dataSetCard.size();
        }
        return dataSet.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout cl_equationContainer;

        TextView tv_wrongAnswers;
        ConstraintLayout cl_equation;
        View line;
        ImageView icon;

        int textSize = 20;
        HashMap<Integer, View> views;

        AnsweredEquation answeredEquation;
        GameCardActivity.AnsweredEquation answeredEquationCard;

        Context context;


        public ViewHolder(View view) {
            super(view);

            cl_equationContainer = view.findViewById(R.id.cl_equation_container);

            tv_wrongAnswers = view.findViewById(R.id.tv_wrong_answers);
            cl_equation = view.findViewById(R.id.cl_equation);

            line = view.findViewById(R.id.view_line_down);

            icon = view.findViewById(R.id.i_icon);

            views = new HashMap<>();
        }

        public void onBindView(Context context, AnsweredEquation answeredEquation, GameType gameType, boolean makeLineRed){
            this.context = context;
            this.answeredEquation = answeredEquation;

            initialize();

            if(makeLineRed){
                line.setBackgroundColor(context.getColor(R.color.incorrect_line));
            }
            else{
                line.setBackgroundColor(context.getColor(R.color.correct_line));
            }
        }

        public void onBindView(Context context, GameCardActivity.AnsweredEquation answeredEquation, boolean makeLineRed){
            this.context = context;
            this.answeredEquationCard = answeredEquation;

            tv_wrongAnswers.setVisibility(View.INVISIBLE);

            initializeCard();

            if(makeLineRed){
                line.setBackgroundColor(context.getColor(R.color.incorrect_line));
            }
            else{
                line.setBackgroundColor(context.getColor(R.color.correct_line));
            }
        }

        @SuppressLint("UseCompatLoadingForDrawables")
        private void initializeCard(){
            if(!answeredEquationCard.isCorrect){
                cl_equationContainer.setBackgroundColor(context.getColor(R.color.incorrect_equation));

                icon.setImageDrawable(context.getDrawable(R.drawable.outline_highlight_off_24));
                icon.setColorFilter(context.getColor(R.color.correction));
            }
            else{
                cl_equationContainer.setBackgroundColor(context.getColor(R.color.white));

                tv_wrongAnswers.setVisibility(View.GONE);
                icon.setImageDrawable(context.getDrawable(R.drawable.round_check_circle_outline_24));
                icon.setColorFilter(context.getColor(R.color.correct_text));
            }

            createEquationCard();
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

                textView.setText(Utils.convertOperatorSign(elements.get(i)));

                textView.setTextSize(textSize);
                textView.setTextColor(context.getColor(R.color.gray1));

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


        private void initializeGameDuel(){

        }

        private void initialize(){
            if(!answeredEquation.isCorrect){
                if(!answeredEquation.equationIsCorrect){
                    tv_wrongAnswers.setVisibility(View.VISIBLE);
                    tv_wrongAnswers.setText(String.valueOf(answeredEquation.equation.getUnknownElementValue()));
                }
                else{
                    tv_wrongAnswers.setVisibility(View.GONE);
                }

                cl_equationContainer.setBackgroundColor(context.getColor(R.color.incorrect_equation));

                icon.setImageDrawable(context.getDrawable(R.drawable.outline_highlight_off_24));
                icon.setColorFilter(context.getColor(R.color.correction));
            }
            else{
                tv_wrongAnswers.setVisibility(View.GONE);
                cl_equationContainer.setBackgroundColor(context.getColor(R.color.white));

                icon.setImageDrawable(context.getDrawable(R.drawable.round_check_circle_outline_24));
                icon.setColorFilter(context.getColor(R.color.correct_text));
            }

            createEquation(answeredEquation.equation);
        }

        private void createEquation(UnknownEquation equation){
            Context context = cl_equation.getContext();
            ConstraintSet set;

            List<Equation.Element> elements = equation.equation.getElements();
            int unknownElementIndex = equation.getUnknownElementIndex();

            cl_equation.removeAllViews();

            int viewIndex = 0;
            for(int i=0;i<elements.size();i++){
                TextView view;
                int viewId = View.generateViewId();

                if(i == unknownElementIndex){
                    view = new TextView(context);
                    view.setId(viewId);

                    view.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView textView = view;

                    if(answeredEquation.isCorrect){
                        if(i == elements.size()-1){
                            textView.setText(String.valueOf(equation.getUnknownElementValue()));
                        }
                        else{
                            textView.setText(equation.getUnknownElementValue()+" ");
                        }

                        textView.setTextColor(context.getColor(R.color.correct_text));
                    }
                    else{
                        List<AnsweredEquation.Answer> wrongAnswersList = new ArrayList<>();
                        for(AnsweredEquation.Answer answer : answeredEquation.answers){
                            if(!answer.isCorrect){
                                wrongAnswersList.add(answer);
                            }
                        }

                        StringBuilder wrongAnswers = new StringBuilder();
                        if(wrongAnswersList.size()>1){
                            wrongAnswers.append("(");
                        }

                        for(int j=0;j<wrongAnswersList.size();j++){
                            if(j!= wrongAnswersList.size() - 1){
                                wrongAnswers.append(wrongAnswersList.get(j).value).append(", ");
                            }
                            else{
                                wrongAnswers.append(wrongAnswersList.get(j).value);
                            }
                        }

                        if(wrongAnswersList.size()>1){
                            wrongAnswers.append(")");
                        }


                        if(i == elements.size()-1){
                            textView.setText(wrongAnswers.toString());
                        }
                        else{
                            textView.setText(wrongAnswers+" ");
                        }

                        if(answeredEquation.equationIsCorrect){
                            textView.setTextColor(context.getColor(R.color.correct_text));
                        }
                        else{
                            textView.setTextColor(context.getColor(R.color.correction));
                        }
                    }

                    textView.setTextSize(textSize);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                }
                else{
                    view = new TextView(context);

                    String value;
                    if(elements.get(i).toString().equals("=")){
                        if(answeredEquation.isCorrect || answeredEquation.equationIsCorrect){
                            value = "=";
                        }
                        else{
                            value = "≠";
                        }
                    }
                    else{
                        if(i==1){
                            value = equation.equation.operatorType.displaySign;
                        }else{
                            value = elements.get(i).toString();
                        }
                    }


                    if(i != elements.size() - 1){
                        value += " ";
                    }


                    view.setText(value);
                    view.setId(viewId);
                    view.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    view.setTextSize(textSize);
                    view.setTextColor(context.getColor(R.color.gray1));
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
}
