package com.coresaken.multiplication.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.controller.LessonController;
import com.coresaken.multiplication.controller.SoundController;
import com.coresaken.multiplication.data.Lesson;
import com.coresaken.multiplication.controller.adapter.LessonAdapter;
import com.coresaken.multiplication.data.enums.OperatorType;
import com.coresaken.multiplication.service.SQLiteHelper;

import java.util.List;

public class LearnPathActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    LessonAdapter lessonAdapter;
    LinearLayoutManager layoutManager;

    List<Lesson> lessons;
    LessonColor[] lessonColors;

    TextView tv_starAmount;
    ConstraintLayout panelUp;

    SQLiteHelper sqLiteHelper;


    private RecyclerView.ViewHolder currentViewHolder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_path);

        transparentStatusAndNavigation();

        OperatorType operatorType = LessonController.getInstance().openedCourseOperatorType;

        panelUp = findViewById(R.id.cl_up_panel);
        tv_starAmount = findViewById(R.id.tv_star_amount);

        //Load colors
        TypedArray buttons = getResources().obtainTypedArray(R.array.lesson_button_small_array);
        TypedArray buttonsBig = getResources().obtainTypedArray(R.array.lesson_button_big_array);
        TypedArray header = getResources().obtainTypedArray(R.array.lesson_header_array);

        int[] colors = getResources().getIntArray(R.array.lesson_array);
        int[] colorsBg = getResources().getIntArray(R.array.lesson_bg_array);

        lessonColors = new LessonColor[11];
        for(int i=0;i<lessonColors.length;i++){
            lessonColors[i] = new LessonColor(colors[i], colorsBg[i], buttons.getDrawable(i), buttonsBig.getDrawable(i), header.getDrawable(i));
        }

        //Load lessons
        LessonController.getInstance().loadLessonsFromFile(LearnPathActivity.this);
        if(operatorType ==OperatorType.MULTIPLICATION){
            lessons = LessonController.getInstance().multiplicationLessons;
        }
        else if(operatorType == OperatorType.DIVISION){
            lessons = LessonController.getInstance().divisionLessons;
        }

        recyclerView = findViewById(R.id.rv_lesson_container);
        lessonAdapter = new LessonAdapter(lessons, LearnPathActivity.this, LearnPathActivity.this, lessonColors);
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(lessonAdapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // Sprawdź, czy jest wyświetlany co najmniej jeden kafelek
                if (layoutManager.findFirstVisibleItemPosition() != RecyclerView.NO_POSITION) {
                    // Pobierz ViewHolder dla pierwszego widocznego kafelka
                    RecyclerView.ViewHolder newViewHolder = recyclerView.findViewHolderForAdapterPosition(layoutManager.findFirstVisibleItemPosition());

                    if (newViewHolder != null && newViewHolder != currentViewHolder) {
                        currentViewHolder = newViewHolder;

                        if (currentViewHolder instanceof LessonAdapter.ViewHolder) {
                            LessonAdapter.ViewHolder yourViewHolder = (LessonAdapter.ViewHolder) currentViewHolder;

                            refreshActivity(yourViewHolder);
                        }
                    }
                }
            }
        });

        sqLiteHelper = new SQLiteHelper(LearnPathActivity.this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(lessons!=null){
            for(Lesson lesson:lessons){
                lesson.calculateStarAmount(sqLiteHelper);
            }
        }

        lessonAdapter.refreshLessons();
        refreshActivity((LessonAdapter.ViewHolder)currentViewHolder);
    }

    private void refreshActivity(LessonAdapter.ViewHolder viewHolder){
        if(viewHolder==null){
            return;
        }

        int lessonIndex = viewHolder.getLayoutPosition();

        Lesson lesson = lessons.get(lessonIndex);
        tv_starAmount.setText(lesson.starAmount +"/55");

        panelUp.setBackgroundTintList(ColorStateList.valueOf(viewHolder.color.color));
    }

    public void closeActivity(View view){
        SoundController.getInstance().clickButton();

        finish();
    }
    private void transparentStatusAndNavigation() {
        //make full transparent statusBar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );
        setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }
    private void setWindowFlag(final int bits, boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    public static class LessonColor{
        public int color;
        public int colorBg;
        public Drawable button;
        public Drawable bigButton;
        public Drawable header;

        public LessonColor(int color, int colorBg, Drawable button, Drawable bigButton, Drawable header){
            this.color = color;
            this.colorBg = colorBg;
            this.button = button;
            this.bigButton = bigButton;
            this.header = header;
        }
    }
}