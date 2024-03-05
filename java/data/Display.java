package com.coresaken.multiplication.data;

import static com.coresaken.multiplication.activity.game.GameDrawActivity.paint_brush;
import static com.coresaken.multiplication.activity.game.GameDrawActivity.path;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.coresaken.multiplication.R;
import com.coresaken.multiplication.activity.game.GameDrawActivity;

import java.util.ArrayList;

public class Display extends View {
    public ArrayList<Path> pathList = new ArrayList<>();

    GameDrawActivity activity;
    private boolean draw;
    Sound sound;

    public Display(Context context) {
        super(context);
        init(context);
    }

    public Display(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public Display(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void init(Context context){
        paint_brush.setAntiAlias(true);
        paint_brush.setColor(Color.WHITE);
        paint_brush.setStyle(Paint.Style.STROKE);
        paint_brush.setStrokeCap(Paint.Cap.ROUND);
        paint_brush.setStrokeJoin(Paint.Join.ROUND);
        paint_brush.setStrokeWidth(10f);
    }

    public void setActivity(GameDrawActivity activity, Sound sound){
        this.activity = activity;
        this.sound = sound;
    }

    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if(!activity.active){
            return false;
        }

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                activity.unselectCards();

                if(activity.findView((int)x,(int)y, 0)){
                    paint_brush.setColor(Color.WHITE);
                    pathList.clear();
                    path.reset();

                    draw = true;

                    path.moveTo(x,y);
                    invalidate();

                    if(sound!=null){
                        sound.play(R.raw.sound_start_drawing);
                    }
                }
                return true;
                case MotionEvent.ACTION_MOVE:
                    if(draw){
                        path.lineTo(x,y);
                        pathList.add(path);
                        invalidate();

                        if(activity.findView((int)x,(int)y, 1)){
                            draw = false;

                            if(sound!=null){
                                sound.play(R.raw.sound_stop_drawing);
                            }
                        }
                    }
                    return true;

            case MotionEvent.ACTION_UP:
                if(draw){
                    activity.unselectCards();
                    pathList.clear();
                    path.reset();
                    invalidate();

                    if(sound!=null){
                        sound.play(R.raw.sound_stop_drawing);
                    }
                    draw = false;
                }

                return true;
            default:
                return false;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for(int i=0;i<pathList.size();i++){
            canvas.drawPath(pathList.get(i), paint_brush);
        }
    }
}
