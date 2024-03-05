package com.coresaken.multiplication.activity.game;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.AdSystem;
import com.coresaken.multiplication.controller.PlayerSettings;
import com.coresaken.multiplication.data.AnsweredEquation;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.Game;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.data.Sound;
import com.coresaken.multiplication.data.UnknownEquation;
import com.coresaken.multiplication.data.enums.GameType;
import com.coresaken.multiplication.data.enums.ModeType;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.fragment.game.GameUpPanelFragment;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.udojava.evalex.Expression;

import java.util.ArrayList;
import java.util.List;

public class GamePuzzleActivity extends Game {
    Equation[] equations;
    EquationUI[] equationsUI;

    Drawable normalField;
    Drawable focusedField;

    Field[] fields;

    boolean isDragging = false;

    Button btn_check;
    View changeView;

    Sound sound;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_puzzle);

        if(PlayerSettings.getInstance().sounds){
            sound = new Sound(this, new int[] {R.raw.sound_start_drawing, R.raw.sound_stop_drawing});
        }

        btn_check = findViewById(R.id.btn_check);
        normalField = getDrawable(R.drawable.field1);
        focusedField = getDrawable(R.drawable.focused_field1);

        sqLiteHelper = new SQLiteHelper(this);
        equations = new Equation[6];

        if(settings.modeType==ModeType.CORRECTION){
            equationController.loadCorrectionEquation(6);
        }

        for(int i=0;i<equations.length;i++){
            Equation equation = equationController.getEquation();

            if(equation==null){
                if(settings.modeType==ModeType.LESSON){
                    equation = equationController.getRandomLessonEquation(settings.currentLesson);
                }
                else if(settings.modeType==ModeType.CORRECTION){
                    equation = equationController.getRandomCorrectionEquation();
                }

            }

            equations[i] = equation;
        }

        fields = new Field[12];
        fields[0] = new Field(R.id.cl_field_0);
        fields[1] = new Field(R.id.cl_field_1);
        fields[2] = new Field(R.id.cl_field_2);
        fields[3] = new Field(R.id.cl_field_3);
        fields[4] = new Field(R.id.cl_field_4);
        fields[5] = new Field(R.id.cl_field_5);
        fields[6] = new Field(R.id.cl_field_6);
        fields[7] = new Field(R.id.cl_field_7);
        fields[8] = new Field(R.id.cl_field_8);
        fields[9] = new Field(R.id.cl_field_9);
        fields[10] = new Field(R.id.cl_field_10);
        fields[11] = new Field(R.id.cl_field_11);

        equationsUI = new EquationUI[6];
        equationsUI[0] = new EquationUI(equations[0], new int[]{R.id.cl_target_0_1, R.id.tv_operator_0, R.id.cl_target_0_2, R.id.tv_equal_sign_0, R.id.cl_target_0_3});
        equationsUI[1] = new EquationUI(equations[1], new int[]{R.id.cl_target_1_1, R.id.tv_operator_1, R.id.cl_target_1_2, R.id.tv_equal_sign_1, R.id.cl_target_1_3});
        equationsUI[2] = new EquationUI(equations[2], new int[]{R.id.cl_target_2_1, R.id.tv_operator_2, R.id.cl_target_2_2, R.id.tv_equal_sign_2, R.id.cl_target_2_3});
        equationsUI[3] = new EquationUI(equations[3], new int[]{R.id.cl_target_3_1, R.id.tv_operator_3, R.id.cl_target_3_2, R.id.tv_equal_sign_3, R.id.cl_target_3_3});
        equationsUI[4] = new EquationUI(equations[4], new int[]{R.id.cl_target_4_1, R.id.tv_operator_4, R.id.cl_target_4_2, R.id.tv_equal_sign_4, R.id.cl_target_4_3});
        equationsUI[5] = new EquationUI(equations[5], new int[]{R.id.cl_target_5_1, R.id.tv_operator_5, R.id.cl_target_5_2, R.id.tv_equal_sign_5, R.id.cl_target_5_3});

        initializeFragments();

        AdSystem.getInstance().loadBanner(findViewById(R.id.adView));
    }

    public void startGame(){
        super.startGame(GameType.PUZZLE);
        statistic.equationLimit = 12;
    }

    @Override
    protected void startLesson(){
        equationController.loadLessonEquations(6, settings.currentLesson);
    }

    @Override
    protected void lostGame(){
        if(settings.modeType== ModeType.LESSON){
            super.lostGame();
        }
        else{
            finishGameTimer.start();
        }
    }

    @Override
    public void finishGame(){
        super.finishGame();

        List<AnsweredEquation> answeredEquations = new ArrayList<>();

        for(EquationUI equationUI:equationsUI){
            AnsweredEquation answeredEquation = equationUI.convertToAnsweredEquation();

            if(answeredEquation!=null){
                answeredEquations.add(answeredEquation);
            }
        }

        controller.answeredEquations = answeredEquations;
        statistic.equationLimit = 6;
    }

    @Override
    protected void startCorrection(){
        //Loaded in onCreate
        //equationController.loadCorrectionEquation(sqLiteHelper,9, settings.operators);
    }

    @Override
    protected void completeGame() {
        for(EquationUI equationUI : equationsUI){
            equationUI.checkResult();
        }

        super.completeGame();
    }

    @Override
    protected void completeLesson() {
        Lesson currentLesson = settings.currentLesson;

        int progress = statistic.getCorrectAnswerPercent();
        progress *= 0.6;

        if(currentLesson!=null){
            currentLesson.changeExercisesProgress(GamePuzzleActivity.this, settings.exerciseIndex, progress);
        }
    }

    private void initializeFragments(){
        upPanelFragment = GameUpPanelFragment.newInstance();
        upPanelFragment.initialize(settings.limitType);
        upPanelFragment.loadProgressBar();
        upPanelFragment.setListener(() -> startGame() );
        getSupportFragmentManager().beginTransaction().add(R.id.cl_panel_up, upPanelFragment, null).commit();
    }


    //Przypisane do przycisku
    public void checkResult(View view){
        completeGame();
    }
    private void checkFinish(){
        int totalFilledFields = 0;

        for(Field field : fields){
            if(field.getButtonId()!=-1){
                totalFilledFields++;
            }
        }

        if(totalFilledFields==0){
            btn_check.setVisibility(View.VISIBLE);
        }
        else{
            btn_check.setVisibility(View.INVISIBLE);
        }

        statistic.totalAnswer = 12 - totalFilledFields;
        upPanelFragment.changeProgressBar(statistic);
    }

    View.OnDragListener dragListener = new View.OnDragListener(){

        @SuppressLint("UseCompatLoadingForDrawables")
        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {
            int dragEventInt = dragEvent.getAction();
            final View draggedElement = (View) dragEvent.getLocalState();

            if(draggedElement==null){
                return false;
            }

            Field field = findElementByFieldId(view.getId());
            Field oldField = findElementByButtonId(draggedElement.getId());

            if(field ==null){
                draggedElement.setVisibility(View.VISIBLE);
                return false;
            }

            switch (dragEventInt){
                case DragEvent.ACTION_DRAG_ENTERED:
                    view.setBackground(focusedField);

                    if(field.getButtonId()!=-1){
                        field.field.removeAllViews();
                        oldField.field.removeAllViews();

                        oldField.field.addView(field.button);
                        changeView = field.button;

                        return true;
                    }

                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    view.setBackground(normalField);

                    if(field.getButtonId()!=-1){
                        field.field.removeAllViews();
                        oldField.field.removeAllViews();

                        field.field.addView(changeView);

                        return true;
                    }
                    break;
                case DragEvent.ACTION_DROP:
                    view.setBackground(normalField);

                    if(sound!=null){
                        sound.play(R.raw.sound_stop_drawing);
                    }

                    if(field.getButtonId()!=-1){
                        //Switch
                        field.field.removeAllViews();
                        oldField.field.removeAllViews();

                        oldField.field.addView(field.button);
                        oldField.button = field.button;

                        if(draggedElement.getParent()!=null){
                            ((ConstraintLayout)draggedElement.getParent()).removeAllViews();
                        }

                        field.button = draggedElement;
                        field.field.addView(draggedElement);

                        return true;
                    }

                    if(oldField !=null){
                        oldField.button = null;
                        oldField.field.removeAllViews();
                    }

                    if(draggedElement.getParent()!=null){
                        ((ConstraintLayout)draggedElement.getParent()).removeAllViews();
                    }

                    field.field.addView(draggedElement);
                    field.button = draggedElement;
                    draggedElement.setVisibility(View.VISIBLE);

                    isDragging = false;

                    checkFinish();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    view.setBackground(normalField);

                    draggedElement.setVisibility(View.VISIBLE);
                    if(oldField != null){
                        if(draggedElement.getParent()!=null){
                            ((ConstraintLayout)draggedElement.getParent()).removeAllViews();
                        }

                        oldField.field.addView(draggedElement);
                    }

                    isDragging = false;
                    break;
                case DragEvent.ACTION_DRAG_STARTED:
                    draggedElement.setVisibility(View.INVISIBLE);
                    break;
            }

            return true;
        }
    };
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(isDragging){
                return true;
            }

            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                isDragging = true;

                if(sound!=null){
                    sound.play(R.raw.sound_start_drawing);
                }

                view.startDragAndDrop(null, new View.DragShadowBuilder(view), view, 0);
            }else if(motionEvent.getAction()==MotionEvent.ACTION_UP || motionEvent.getAction()==MotionEvent.ACTION_CANCEL || motionEvent.getAction()==MotionEvent.ACTION_BUTTON_RELEASE){
                isDragging = false;


                if(sound!=null){
                    sound.play(R.raw.sound_stop_drawing);
                }
            }

            return true;
        }
    };

    private Field findElementByFieldId(int fieldId){
        for(EquationUI equationUI : equationsUI){
            if(equationUI!=null){
                for(Field field : equationUI.fields){
                    if(field !=null){
                        if(field.fieldId == fieldId){
                            return field;
                        }
                    }
                }
            }
        }

        for(Field field : fields){
            if(field.fieldId == fieldId){
                return field;
            }
        }

        return null;
    }
    private Field findElementByButtonId(int buttonId){
        for(EquationUI equationUI : equationsUI){
            if(equationUI!=null){
                for(Field field : equationUI.fields){
                    if(field !=null){
                        if(field.getButtonId() == buttonId){
                            return field;
                        }
                    }
                }
            }
        }

        for(Field field : fields){
            if(field.getButtonId() == buttonId){
                return field;
            }
        }

        return null;
    }
    private Field getRandomEmptyField(){
        int randomIndex = random.nextInt(fields.length);

        randomIndex = randomIndex%fields.length;

        while (fields[randomIndex].button!=null){
            randomIndex++;
            randomIndex = randomIndex%fields.length;
        }

        return fields[randomIndex];
    }

    @Override
    protected Context getContext() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        super.stopAllTimers();
    }

    public void tryCloseGame(View view){
        super.tryCloseGame(view);
    }

    class EquationUI{
        public Field[] fields;
        public Equation equation;

        private boolean isCorrect;

        public EquationUI(Equation equation, int[] elementID){
            this.equation = equation;

            fields = new Field[5];
            for(int i = 0; i< fields.length; i++){
                fields[i] = new Field(elementID[i]);
            }

            List<Equation.Element> elements = equation.getElements();
            fields[1].changeSign(elementID[1], equation.operatorType.displaySign);

            for(int i=0;i<3;i+=2){
                Field field = getRandomEmptyField();
                field.createButton(elements.get(i).number);
            }

            fields[4].createTextView(elements.get(4).number);
        }

        public void checkResult(){
            String sign = equation.getElements().get(1).sign;

            int a = fields[0].getValue();
            int b = fields[2].getValue();
            int c = fields[4].getValue();

            StringBuilder equationSB = new StringBuilder();
            equationSB.append(a).append(sign).append(b);

            Expression expression = new Expression(equationSB.toString());
            statistic.currentEquation++;

            isCorrect = c == expression.eval().intValue();

            if(settings.modeType == ModeType.CORRECTION){
                if(isCorrect){
                    sqLiteHelper.updateIncorrectEquationPoints(equation, 4);
                }
            }


            if(isCorrect){
                //Correct answer
                sqLiteHelper.updatePoints(equation.getId(), 5);
                correctAnswer(true, false);

                for(Field field :fields){
                    field.makeButtonGreen();
                }
            }
            else{
                //Incorrect answer
                sqLiteHelper.updatePoints(equation.getId(), -8);
                incorrectAnswer(true, false);

                for(Field field :fields){
                    field.makeButtonRed();
                }

                sqLiteHelper.updateIncorrectEquationPoints(equation, -10);
            }
        }

        public AnsweredEquation convertToAnsweredEquation(){
            UnknownEquation unknownEquation = new UnknownEquation(equation, 4);
            AnsweredEquation answeredEquation = new AnsweredEquation(unknownEquation);

            if(equation.operatorType== OperatorType.DIVISION){
                if(fields[2].getValue()==0){
                    return null;
                }
            }

            Expression expression = new Expression(fields[0].getValue() + " " + equation.getElements().get(1).sign + " "+fields[2].getValue());
            int result = expression.eval().intValue();

            answeredEquation.isCorrect = isCorrect;
            answeredEquation.answers.add(new AnsweredEquation.Answer(result, isCorrect));

            return answeredEquation;
        }
    }
    class Field {
        public int fieldId;

        public ConstraintLayout field;
        public View button;

        int value;

        public Field(int fieldId){
            this.fieldId = fieldId;

            try{
                field = findViewById(fieldId);

                if(field != null){
                    field.setOnDragListener(dragListener);
                }
            }catch (ClassCastException e){

            }
        }

        public void changeSign(int textViewId, String sign){
            TextView tv = findViewById(textViewId);

            if(tv!=null){
                tv.setText(sign);
            }
        }

        public int getButtonId(){
            if(button !=null){
                return button.getId();
            }

            return -1;
        }
        public int getValue(){
            if(button!=null){
                return Integer.parseInt(((Button)button).getText().toString());
            }

            return value;
        }

        public void createButton(int value){
            Button button = new Button(GamePuzzleActivity.this);
            button.setOnTouchListener(touchListener);
            button.setBackground(getDrawable(R.drawable.button2));
            button.setStateListAnimator(null);

            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            button.setLayoutParams(params);

            button.setId(View.generateViewId());
            button.setText(String.valueOf(value));
            button.setTextColor(getColor(R.color.normal_text));
            button.setTextSize(20);
            button.setTypeface(Typeface.DEFAULT_BOLD);
            field.addView(button);
            this.button = button;
        }
        public void createTextView(int value){
            this.value = value;

            TextView textView = new TextView(GamePuzzleActivity.this);
            ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(params);

            textView.setId(View.generateViewId());
            textView.setText(String.valueOf(value));
            textView.setTextColor(Color.WHITE);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setTextSize(34);

            field.setBackground(null);
            field.setOnDragListener(null);
            field.addView(textView);
        }

        public void makeButtonRed(){
            if(button!=null){
                ((Button)button).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.incorrect_color_up)));
                ((Button)button).setTextColor(getColor(R.color.incorrect_text));
            }
        }

        public void makeButtonGreen(){
            if(button!=null){
                ((Button)button).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.correct_color_up)));
                ((Button)button).setTextColor(getColor(R.color.correct_text));
            }
        }
    }
}