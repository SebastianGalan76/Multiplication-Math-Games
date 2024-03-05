package com.coresaken.multiplication.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.Equation;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.fragment.main_menu.EquationTableFragment;
import com.coresaken.multiplication.service.SQLiteHelper;
import com.coresaken.multiplication.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class TableActivity extends AppCompatActivity {
    private ConstraintLayout[] cl_equations;
    private View currentNumberView;

    private OperatorType currentOperator;
    private int currentPage;
    private int currentNumber;

    private View numberZeroView;
    private View numberOneView;

    SQLiteHelper sqLiteHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);

        LinearLayout[] ll_button_containers = new LinearLayout[2];
        ll_button_containers[0] = findViewById(R.id.ll_button_up_container);
        ll_button_containers[1] = findViewById(R.id.ll_button_down_container);

        sqLiteHelper = new SQLiteHelper(this);

        for(int i=0;i<=100;i++){
            ll_button_containers[0].addView(createNewButton(i));
            i++;
            ll_button_containers[1].addView(createNewButton(i));
        }

        cl_equations = new ConstraintLayout[11];
        cl_equations[0] = findViewById(R.id.cl_equation_0);
        cl_equations[1] = findViewById(R.id.cl_equation_1);
        cl_equations[2] = findViewById(R.id.cl_equation_2);
        cl_equations[3] = findViewById(R.id.cl_equation_3);
        cl_equations[4] = findViewById(R.id.cl_equation_4);
        cl_equations[5] = findViewById(R.id.cl_equation_5);
        cl_equations[6] = findViewById(R.id.cl_equation_6);
        cl_equations[7] = findViewById(R.id.cl_equation_7);
        cl_equations[8] = findViewById(R.id.cl_equation_8);
        cl_equations[9] = findViewById(R.id.cl_equation_9);
        cl_equations[10] = findViewById(R.id.cl_equation_10);

        currentNumber = 1;
        currentPage = 0;
        changeOperator(OperatorType.MULTIPLICATION);
    }

    public void selectNumber(View view, int number){
        SoundController.getInstance().clickButton();

        if(number==0 && currentOperator == OperatorType.DIVISION){
            selectNumber(numberOneView, 1);
            return;
        }

        if(currentNumberView != null){
            Button btn = (Button) currentNumberView;

            btn.setEnabled(true);
            btn.setTextColor(getResources().getColor(R.color.normal_text));
        }

        currentNumber = number;
        currentNumberView = view;

        Button btn = (Button) view;

        btn.setEnabled(false);
        btn.setTextColor(getResources().getColor(R.color.white));

        if(currentNumber==0 && currentOperator == OperatorType.DIVISION){
            currentNumber = 1;
        }

        refreshTable();
    }
    public void changePage(View view){
        SoundController.getInstance().clickButton();

        if(view.getId() == R.id.cl_next_page){
            currentPage++;
        }
        if(view.getId() == R.id.cl_previous_page){
            currentPage--;
        }

        if(currentPage>100){
            currentPage = 100;
        }
        if(currentPage<0){
            currentPage = 0;
        }

        refreshTable();
    }
    public void changeOperator(View view){
        SoundController.getInstance().clickButton();

        if(view.getId() == R.id.cl_multiplication){
            changeOperator(OperatorType.MULTIPLICATION);
        }
        if(view.getId() == R.id.cl_division){
            changeOperator(OperatorType.DIVISION);
        }
    }

    private void changeOperator(OperatorType operatorType){
        if(operatorType == OperatorType.MULTIPLICATION){
            currentOperator = OperatorType.MULTIPLICATION;
            numberZeroView.setVisibility(View.VISIBLE);

            findViewById(R.id.cl_multiplication_background).setEnabled(false);
            findViewById(R.id.cl_division_background).setEnabled(true);

            ((TextView)findViewById(R.id.tv_multiplication)).setTextColor(getResources().getColor(R.color.white));
            ((TextView)findViewById(R.id.tv_division)).setTextColor(getResources().getColor(R.color.normal_text));
        }
        else{
            currentOperator = OperatorType.DIVISION;
            numberZeroView.setVisibility(View.INVISIBLE);

            if(currentNumber==0){
                selectNumber(numberOneView, 1);
            }

            findViewById(R.id.cl_division_background).setEnabled(false);
            findViewById(R.id.cl_multiplication_background).setEnabled(true);

            ((TextView)findViewById(R.id.tv_division)).setTextColor(getResources().getColor(R.color.white));
            ((TextView)findViewById(R.id.tv_multiplication)).setTextColor(getResources().getColor(R.color.normal_text));
        }

        refreshTable();
    }
    private void refreshTable(){
        int i;

        if(currentPage==0){
            i = 0;
        }
        else{
            i = 1;
        }
        Equation.Element aElement = new Equation.Element(currentNumber);
        Equation.Element signElement;
        signElement = new Equation.Element(currentOperator.sign);

        Equation.Element equalSignElement = new Equation.Element("=");

        for( ;i<=10;i++){
            if(currentPage!=0){
                cl_equations[0].removeAllViews();
            }

            int b = currentPage*10 + i;
            Equation.Element bElement = new Equation.Element(b);

            int c;
            if(currentOperator == OperatorType.MULTIPLICATION){
                c = currentNumber * b;
            }
            else{
                c = currentNumber * b;
            }

            Equation.Element cElement = new Equation.Element(c);

            List<Equation.Element> elements = new ArrayList<>();

            Equation equation;
            if(currentOperator == OperatorType.MULTIPLICATION){
                elements.add(aElement);
                elements.add(signElement);
                elements.add(bElement);
                elements.add(equalSignElement);
                elements.add(cElement);

                equation = new Equation(elements, OperatorType.MULTIPLICATION);
            }
            else{
                elements.add(cElement);
                elements.add(signElement);
                elements.add(aElement);
                elements.add(equalSignElement);
                elements.add(bElement);

                equation = new Equation(elements, OperatorType.DIVISION);
            }


            EquationTableFragment equationTableFragment = EquationTableFragment.newInstance(Utils.convertOperatorSign(equation.toString()));
            equationTableFragment.initialize(sqLiteHelper, equation.getId());
            getSupportFragmentManager().beginTransaction().replace(cl_equations[i].getId(), equationTableFragment).commit();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private View createNewButton(int value){
        Button button = new Button(this);

        int sizeInDp = Utils.convertDPToPixels(getResources(), 50);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(sizeInDp, sizeInDp);

        int marginInDp = Utils.convertDPToPixels(getResources(), 5);
        lp.setMargins(marginInDp, marginInDp, marginInDp, marginInDp);

        button.setLayoutParams(lp);
        button.setBackground(getDrawable(R.drawable.button2));
        button.setText(String.valueOf(value));

        if(value==1){
            button.setTextColor(getColor(R.color.white));
            button.setEnabled(false);

            currentNumberView = button;
        }
        else{
            button.setTextColor(getResources().getColor(R.color.normal_text));
        }

        button.setTextSize(20);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setId(View.generateViewId());

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectNumber(view, value);
            }
        });

        if(value==0){
            numberZeroView = button;
        }
        else if(value==1){
            numberOneView = button;
        }

        return button;
    }

    public void closeActivity(View view){
        SoundController.getInstance().clickButton();

        finish();
    }
}