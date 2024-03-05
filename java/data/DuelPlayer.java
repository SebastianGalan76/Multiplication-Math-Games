package com.coresaken.multiplication.data;

import android.annotation.SuppressLint;
import android.os.CountDownTimer;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.game.DuelGameActivity;
import com.coresaken.multiplication.fragment.game.EquationFragment;
import com.coresaken.multiplication.listener.ViewCreatedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DuelPlayer {
    public String name;
    public int correctAnswer;
    public int incorrectAnswer;
    public boolean finished;

    UnknownEquation[] equations;
    UnknownEquation currentEquation;
    HashMap<Integer, int[]> answers;
    public List<AnsweredEquation> answeredEquations;

    Button[] buttons;

    EquationFragment equationFragment;

    int currentEquationIndex;

    DuelGameActivity activity;

    ConstraintLayout cl_equation, cl_buttons;

    CountDownTimer changeEquationTimer;

    private Sound sound;


    public DuelPlayer(DuelGameActivity activity, String name, UnknownEquation[] equations, HashMap<Integer, int[]> answers, Button[] buttons, ConstraintLayout cl_equation, ConstraintLayout cl_buttons, Sound sound){
        this.activity = activity;
        this.name = name;
        this.equations = equations;
        this.answers = answers;
        this.buttons = buttons;

        this.cl_equation = cl_equation;
        this.cl_buttons = cl_buttons;

        this.sound = sound;

        finished = false;

        currentEquationIndex = -1;
        correctAnswer = 0;
        incorrectAnswer = 0;

        answeredEquations = new ArrayList<>();

        changeEquationTimer = new CountDownTimer(500, 1000){

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                changeEquation();
            }
        };

        setButtonListener();
    }

    public void startGame(){
        equationFragment = EquationFragment.newInstance();
        equationFragment.changeTextSize(32);
        equationFragment.hideStars();
        equationFragment.setListener(new ViewCreatedListener() {
            @Override
            public void onViewCreated() {
                changeEquation();
            }
        });

        activity.getSupportFragmentManager().beginTransaction().add(cl_equation.getId(), equationFragment, null).commitNow();
    }

    public void answer(int index){
        int selectedValue = answers.get(currentEquationIndex)[index];
        boolean isCorrect = selectedValue == equations[currentEquationIndex].getUnknownElementValue();

        changeButtonsActivity(false);

        if(isCorrect){
            correctAnswer++;
        }
        else{
            incorrectAnswer++;
        }

        AnsweredEquation answeredEquation = new AnsweredEquation(currentEquation);
        answeredEquation.addAnswer(selectedValue, isCorrect);
        answeredEquations.add(answeredEquation);

        equationFragment.answer(selectedValue, isCorrect, true);

        changeEquationTimer.start();
    }

    private void changeEquation(){
        currentEquationIndex++;

        if(currentEquationIndex>=equations.length){
            finishGame();
            return;
        }

        currentEquation = equations[currentEquationIndex];
        int[] answers = this.answers.get(currentEquationIndex);
        for(int i=0;i<buttons.length;i++){
            buttons[i].setText(String.valueOf(answers[i]));
        }

        equationFragment.setEquation(currentEquation);
        changeButtonsActivity(true);
    }

    public void finishGame(){
        if(changeEquationTimer!=null){
            changeEquationTimer.cancel();
        }

        finished = true;

        cl_buttons.setVisibility(View.INVISIBLE);
        cl_equation.removeAllViews();

        activity.checkFinish();
    }

    private void changeButtonsActivity(boolean enable){
        for(Button button : buttons){
            button.setEnabled(enable);
        }
    }

    public int correctAnswerPercent(){
        return (int)((float)correctAnswer/(float)answeredEquations.size() * 100);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setButtonListener(){
        for(int i=0;i< buttons.length;i++){
            final int buttonIndex = i;
            buttons[i].setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                        if(sound!=null){
                            sound.play(R.raw.sound_start_drawing);
                        }

                        buttons[buttonIndex].setPadding(0, 20, 0 ,0);
                        return false;
                    }
                    if(motionEvent.getAction()==MotionEvent.ACTION_UP || motionEvent.getAction()==MotionEvent.ACTION_CANCEL){
                        if(sound!=null){
                            sound.play(R.raw.sound_stop_drawing);
                        }

                        buttons[buttonIndex].setPadding(0, 0, 0 ,0);
                        return false;
                    }

                    return true;
                }
            });
        }
    }
}
