package com.coresaken.multiplication.fragment.game;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
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
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.coresaken.multiplication.R;
import com.coresaken.multiplication.data.Equation;

import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.listener.ViewCreatedListener;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;
import com.udojava.evalex.Expression;

import java.util.HashMap;
import java.util.List;

public class EquationFragment extends Fragment {

    ViewCreatedListener listener;

    LottieAnimationView[] stars;
    ConstraintLayout cl_equationContainer;
    TextView tv_correction;

    UnknownEquation currentEquation;
    HashMap<Integer, View> views;
    int unknownViewIndex;


    UnknownElementStyle unknownElementStyle;
    TextView tv_unknownElement;
    int textSize = 40;

    boolean starsAreHidden;

    TextView tv_correctionInfo;
    SQLiteHelper sqLiteHelper;

    int starAmount;

    public EquationFragment() {
        unknownElementStyle = UnknownElementStyle.QUESTION_MARK;
    }


    public void setListener(ViewCreatedListener viewCreatedListener){
        this.listener = viewCreatedListener;
    }

    public static EquationFragment newInstance() {
        return new EquationFragment();
    }

    public void initialize(UnknownElementStyle unknownElementStyle){
        this.unknownElementStyle = unknownElementStyle;
    }
    public void changeTextSize(int textSize){
        this.textSize = textSize;
    }
    public void changeStarSize(int size){
        if(starsAreHidden){
            return;
        }

        int sizeInPixels = Utils.convertDPToPixels(getResources(), size);

        for (LottieAnimationView star : stars) {
            ViewGroup.LayoutParams params = star.getLayoutParams();
            params.height = sizeInPixels;
            params.width = sizeInPixels;
            star.setLayoutParams(params);
        }
    }
    public void hideStars(){
        starsAreHidden = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        views = new HashMap<>();

        if(starsAreHidden){
            view.findViewById(R.id.ll_star_container).setVisibility(View.INVISIBLE);
        }
        else{
            stars = new LottieAnimationView[5];

            stars[0] = view.findViewById(R.id.lottie_star0);
            stars[1] = view.findViewById(R.id.lottie_star1);
            stars[2] = view.findViewById(R.id.lottie_star2);
            stars[3] = view.findViewById(R.id.lottie_star3);
            stars[4] = view.findViewById(R.id.lottie_star4);
        }

        cl_equationContainer = view.findViewById(R.id.cl_equationContainer);
        tv_correctionInfo = view.findViewById(R.id.tv_correction_info);

        tv_correction = view.findViewById(R.id.tv_correction);
        tv_correction.setVisibility(View.INVISIBLE);

        sqLiteHelper = new SQLiteHelper(getContext());

        if(listener!=null){
            listener.onViewCreated();
        }
    }

    public void setEquation(UnknownEquation currentEquation){
        if(!views.isEmpty()){
            for(View view : views.values()){
                cl_equationContainer.removeView(view);
            }
        }
        views.clear();

        this.currentEquation = currentEquation;
        tv_correction.setVisibility(View.INVISIBLE);

        if(currentEquation.correction){
            tv_correctionInfo.setVisibility(View.VISIBLE);
        }
        else{
            tv_correctionInfo.setVisibility(View.INVISIBLE);
        }

        loadStars();
        generateEquation();
    }

    public void changeElementColor(int elementIndex, int colorId){
        Context context = cl_equationContainer.getContext();
        ((TextView)views.get(elementIndex)).setTextColor(context.getResources().getColor(colorId));
    }
    @SuppressLint("SetTextI18n")
    public void changeElement(int elementIndex, String value){
            cl_equationContainer.removeView(views.get(elementIndex));

            ConstraintSet set = new ConstraintSet();
            Context context = cl_equationContainer.getContext();
            TextView view = new TextView(context);

            int viewId = View.generateViewId();
            view.setId(viewId);

            int previousViewId = getViewId(elementIndex - 1);
            int nextViewId = getViewId(elementIndex + 1);

            if(nextViewId!=-1){
                view.setText(value + " ");
            }
            else{
                view.setText(String.valueOf(value));
            }

            view.setTextSize(textSize);
            view.setTypeface(Typeface.DEFAULT_BOLD);
            view.setTextColor(context.getResources().getColor(R.color.white));

            views.replace(elementIndex, view);
            cl_equationContainer.addView(view);

            set.clone(cl_equationContainer);

            set.connect(viewId, ConstraintSet.TOP, previousViewId, ConstraintSet.TOP);
            set.connect(viewId, ConstraintSet.BOTTOM, previousViewId, ConstraintSet.BOTTOM);

            if(elementIndex==0){
                set.connect(viewId, ConstraintSet.LEFT, previousViewId, ConstraintSet.LEFT);
            }
            else{
                set.connect(viewId, ConstraintSet.LEFT, previousViewId, ConstraintSet.RIGHT);
            }

            if(nextViewId!=-1){
                set.connect(nextViewId, ConstraintSet.LEFT, viewId, ConstraintSet.RIGHT);
            }
            set.applyTo(cl_equationContainer);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void answer(int value, boolean isCorrect){
        tv_correction.setVisibility(View.INVISIBLE);

        if(!isCorrect){
            if(unknownElementStyle==UnknownElementStyle.FIELD){
                views.get(unknownViewIndex).setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.incorrect_color_up)));
            }

            if(currentEquation.getUnknownElementIndex()!=4){
                StringBuilder equationBuilder = new StringBuilder();

                List<Equation.Element> elements = currentEquation.equation.getElements();

                for(int i=0;i<elements.size();i++){
                    if(elements.get(i).toString().equals("=")){
                        break;
                    }

                    if(i == currentEquation.getUnknownElementIndex()){
                        equationBuilder.append(value).append(" ");
                    }
                    else{
                        equationBuilder.append(elements.get(i)).append(" ");
                    }
                }

                String equation = equationBuilder.toString();
                try {
                    Expression expression = new Expression(equation);
                    float result = expression.eval().floatValue();

                    tv_correction.setVisibility(View.VISIBLE);
                    tv_correction.setText(Utils.convertOperatorSign(equation + " = "+floatToString(result)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        else{
            changeElement(unknownViewIndex, String.valueOf(value));
            changeElementColor(unknownViewIndex, R.color.correctAnswer);
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void answer(int value, boolean isCorrect, boolean putIncorrectValue){
        tv_correction.setVisibility(View.INVISIBLE);

        if(!isCorrect){
            if(putIncorrectValue){
                changeElement(unknownViewIndex, String.valueOf(value));
                changeElementColor(unknownViewIndex, R.color.correction);
            }
        }

        answer(value, isCorrect);
    }

    public long updateStarAmount(int totalPoints){
        int starAmountAfterAnswer = Utils.getStarAmount(totalPoints);
        long starDuration = 0;

        if(starAmountAfterAnswer > starAmount){
            for(int i=starAmount;i<starAmountAfterAnswer;i++){
                stars[i].playAnimation();
                starDuration = stars[i].getDuration();
            }
        }

        return starDuration;
    }

    //TRUE FALSE Game
    public void answer(boolean isCorrect){
        tv_correction.setVisibility(View.INVISIBLE);

        if(!isCorrect){
            StringBuilder equationBuilder = new StringBuilder();

            List<Equation.Element> elements = currentEquation.equation.getElements();

            for(int i=0;i<elements.size();i++){
                if(elements.get(i).toString().equals("=")){
                    break;
                }
                else{
                    equationBuilder.append(elements.get(i)).append(" ");
                }
            }

            String equation = equationBuilder.toString();
            try {
                Expression expression = new Expression(equation);
                float result = expression.eval().floatValue();

                tv_correction.setVisibility(View.VISIBLE);
                tv_correction.setText(Utils.convertOperatorSign(equation + " = "+floatToString(result)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void generateEquation(){
        Context context = cl_equationContainer.getContext();
        ConstraintSet set;

        List<Equation.Element> elements = currentEquation.equation.getElements();
        int unknownElementIndex = currentEquation.getUnknownElementIndex();

        int viewIndex = 0;
        for(int i=0;i<elements.size();i++){
            View view;
            int viewId = View.generateViewId();

            if(i == unknownElementIndex){
                unknownViewIndex = viewIndex;

                if(unknownElementStyle==UnknownElementStyle.QUESTION_MARK){
                    view = new TextView(context);
                    view.setId(viewId);

                    view.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    TextView textView = (TextView)view;

                    if(i == elements.size()-1){
                        textView.setText("?");
                    }
                    else{
                        textView.setText("? ");
                    }

                    textView.setTextSize(textSize);
                    textView.setTypeface(Typeface.DEFAULT_BOLD);
                    textView.setTextColor(Color.WHITE);
                }
                else{
                    view = new ConstraintLayout(context);
                    view.setId(viewId);

                    view.setBackground(getActivity().getDrawable(R.drawable.button2));
                    view.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.background_dark)));
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,20,0);
                    view.setLayoutParams(params);

                    view.setPadding(30,10,30,10);

                    TextView textView = new TextView(context);
                    textView.setTextSize(textSize - 10);
                    textView.setTextColor(Color.WHITE);
                    textView.setText("?");
                    textView.setTypeface(Typeface.DEFAULT_BOLD);

                    ConstraintLayout cl_wrapper = (ConstraintLayout)view;
                    cl_wrapper.addView(textView);

                    int tv_viewId = View.generateViewId();
                    textView.setId(tv_viewId);

                    ConstraintSet set2 = new ConstraintSet();
                    set2.clone(cl_wrapper);

                    set2.connect(tv_viewId, ConstraintSet.TOP, cl_wrapper.getId(), ConstraintSet.TOP);
                    set2.connect(tv_viewId, ConstraintSet.BOTTOM, cl_wrapper.getId(), ConstraintSet.BOTTOM);
                    set2.connect(tv_viewId, ConstraintSet.LEFT, cl_wrapper.getId(), ConstraintSet.LEFT);
                    set2.connect(tv_viewId, ConstraintSet.RIGHT, cl_wrapper.getId(), ConstraintSet.RIGHT);

                    set2.applyTo(cl_wrapper);

                    tv_unknownElement = textView;
                }
            }
            else{
                view = new TextView(context);
                view.setId(viewId);

                view.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                TextView textView = (TextView)view;

                if(i == elements.size()-1){
                    textView.setText(elements.get(i).toString());
                }
                else{
                    if(i == 1){
                        textView.setText(currentEquation.equation.operatorType.displaySign +" ");
                    }
                    else{
                        textView.setText(elements.get(i).toString() + " ");
                    }
                }

                textView.setTextSize(textSize);
                textView.setTypeface(Typeface.DEFAULT_BOLD);
                textView.setTextColor(Color.WHITE);
            }

            views.put(viewIndex, view);
            cl_equationContainer.addView(view);

            int previousViewId = getViewId(viewIndex - 1);
            set = new ConstraintSet();
            set.clone(cl_equationContainer);

            set.connect(viewId, ConstraintSet.TOP, previousViewId, ConstraintSet.TOP);
            set.connect(viewId, ConstraintSet.BOTTOM, previousViewId, ConstraintSet.BOTTOM);

            if(viewIndex==0){
                set.connect(viewId, ConstraintSet.LEFT, previousViewId, ConstraintSet.LEFT);
            }
            else{
                set.connect(viewId, ConstraintSet.LEFT, previousViewId, ConstraintSet.RIGHT);
            }

            if(previousViewId!=-1){
                set.connect(previousViewId, ConstraintSet.RIGHT, viewId, ConstraintSet.LEFT);
            }

            set.applyTo(cl_equationContainer);
            viewIndex++;
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void loadStars(){
        if(starsAreHidden){
            return;
        }

        int points = sqLiteHelper.getPoints(currentEquation.equation.getId());
        currentEquation.equation.setPoints(points);
        starAmount = Utils.getStarAmount(points);

        for(int i=0;i<starAmount;i++){
            stars[i].setProgress(1f);
        }
        for(int i=starAmount;i<5;i++){
            stars[i].cancelAnimation();
            stars[i].setProgress(0f);
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
            return cl_equationContainer.getId();
        }
    }

    public void setUnknownElementFieldValue(String value){
        if(tv_unknownElement!=null){
            tv_unknownElement.setText(value);
        }
    }

    public String floatToString(float number) {
        String result = String.valueOf(number);

        if (result.endsWith(".0")) {
            result = result.substring(0, result.length() - 2);
        }

        int indexOfDecimalPoint = result.indexOf(".");
        if (indexOfDecimalPoint != -1 && result.length() > indexOfDecimalPoint + 3) {
            result = result.substring(0, indexOfDecimalPoint + 3);
        }

        if (result.contains(".")) {
            while (result.endsWith("0")) {
                result = result.substring(0, result.length() - 1);
            }
        }

        return result;
    }

    public enum UnknownElementStyle{
        QUESTION_MARK, FIELD;
    }
}