package com.example.rosproject.Fragments;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import com.example.rosproject.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class VelFragment extends SimpleFragment {

    private View view;
    private Timer ltimer, ttimer;
    private final List<Double> vel = new ArrayList<>();
    private final List<String> time = new ArrayList<>();
    private X_View x_view;
    private Y_View y_view;

    /* 用于获取滑动窗口最大值*/
    private final Deque<Pair<String, Double>> dq_vel = new LinkedList<>();
    private double vel_max = 0.0;
    private int idx = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_layout, container, false);
            LinearLayout linearLayout = view.findViewById(R.id.layout_fragment);

            y_view = new Y_View(getControlApp());
            linearLayout.addView(y_view,0);

            HorizontalScrollView horizontalScrollView = new HorizontalScrollView(getControlApp());

            x_view = new X_View(getControlApp());
            horizontalScrollView.addView(x_view);

            horizontalScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
            horizontalScrollView.setHorizontalScrollBarEnabled(false);
            linearLayout.addView(horizontalScrollView);
        }

        /*每隔100ms获取当前智能车速度*/
        ltimer = new Timer();
        ltimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Double v = getControlApp().getRobotController().odomDataProvider.getData();
                if(vel.size() > 190){
                    vel.remove(0);
                }
                vel.add(v);

                /* 滑动窗口取速度最大值*/
                double tv = Math.abs(v);
                while(!dq_vel.isEmpty() && tv >= dq_vel.getLast().second) dq_vel.pollLast();
                dq_vel.addLast(new Pair<>(String.valueOf(idx), tv));
                if(idx - Integer.parseInt(dq_vel.getFirst().first) > 190) dq_vel.pollFirst();
                vel_max = dq_vel.getFirst().second;
                idx = idx + 1;

                y_view.postInvalidate();
                x_view.setData(vel, time);

            }
        },0,100);

        /*每隔一秒获取当前时间*/
        ttimer = new Timer();
        ttimer.schedule(new TimerTask() {
            @Override
            public void run() {
                String t = new SimpleDateFormat("hh:mm:ss").format(new Date());
                time.add(t);
                if(time.size() > 20){
                    time.remove(0);
                }
            }
        },0,1000);

        return view;
    }

    class Y_View extends View{

        private int yScale = 0;
        private int yHeight = 0;
        private final Paint yPaint = new Paint();
        private final Paint tPaint = new Paint();

        private final int yd = 100;

        private int yn; //y轴坐标值个数

        public Y_View(Context context){
            super(context);
            yPaint.setColor(Color.BLACK);
            yPaint.setStyle(Paint.Style.STROKE);
            yPaint.setAntiAlias(true);
            yPaint.setStrokeWidth(3f);

            tPaint.setColor(Color.DKGRAY);
            tPaint.setStyle(Paint.Style.STROKE);
            tPaint.setAntiAlias(true);
            tPaint.setTextSize(20f);
        }

        private final DecimalFormat decimalFormat = new DecimalFormat("0.0");

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
            yHeight = MeasureSpec.getSize(heightMeasureSpec) / 2;
            setMeasuredDimension(MeasureSpec.getSize(80),yHeight + yd);
        }

        @Override
        protected void onDraw(Canvas canvas){

            canvas.drawLine(80, 0, 80, getHeight(), yPaint);

            yn = (int)Math.ceil(vel_max / 0.1);
            if(yn == 0) yn = 1;
            yScale = yHeight / (yn * 2);
            yHeight = yScale * (yn * 2);

            /*绘制y正半轴坐标值*/
            for(int i = 0; i <= yn; i++){
                String v = decimalFormat.format(0.1 * i);
                canvas.drawText(v, 40, (int)(yd / 2)+ (int)(yHeight / 2) - yScale * i + 8, tPaint);
            }

            /*绘制y负半轴坐标值*/
            for(int i = 1; i <= yn; i++){
                String v = "-" + decimalFormat.format(0.1 * i);
                canvas.drawText(v, 30, (int)(yd / 2) + (int)(yHeight / 2) + yScale * i + 8, tPaint);
            }

            canvas.drawText("m/s", 40, (int)(yd / 4) - 2, tPaint);
            canvas.drawText("m/s", 40, yd - 2 + yHeight, tPaint);
        }
    }

    class X_View extends View{

        private int xScale = 0;
        private final int xD = 40;
        private final int currentSize = 10;
        private final int size = 20;
        private Paint xPaint = new Paint();
        private Paint lPaint = new Paint();
        private Paint tPaint = new Paint();

        List<Double> vel = new ArrayList<>();
        List<String> time = new ArrayList<>();

        public X_View(Context context){
            super(context);

            xPaint.setStyle(Paint.Style.STROKE);
            xPaint.setAntiAlias(true);


            lPaint.setColor(Color.RED);
            lPaint.setStyle(Paint.Style.STROKE);
            lPaint.setAntiAlias(true);
            lPaint.setStrokeWidth(3f);

            tPaint.setColor(Color.BLACK);
            tPaint.setStyle(Paint.Style.STROKE);
            tPaint.setAntiAlias(true);
            tPaint.setTextSize(20f);
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
            xScale = MeasureSpec.getSize(widthMeasureSpec) / currentSize;
            int d = xScale / 10;
            xScale = d * 10;
            setMeasuredDimension(xScale * size + xD, y_view.yHeight + y_view.yd);
        }

        @Override
        protected void onDraw(Canvas canvas){
            super.onDraw(canvas);

            xPaint.setStrokeWidth(3f);
            xPaint.setColor(Color.BLACK);
            canvas.drawLine(0,y_view.yd / 2 + y_view.yHeight / 2, xScale * size,y_view.yd / 2 + y_view.yHeight / 2,xPaint);

            xPaint.setStrokeWidth(1f);
            xPaint.setColor(Color.DKGRAY);

            int nPoints = xScale * size / 5 / 2;

            /*绘制”------“*/
            for(int i = 1; i <= y_view.yn; i++){
                for(int j = 0; j < nPoints; j++){
                    int currentX = j * 10;
                    int currentY = y_view.yd / 2 + y_view.yHeight / 2 - y_view.yScale * i;
                    canvas.drawLine(currentX,currentY,currentX + 5, currentY, xPaint);
                }
            }

            for(int i = 1; i <= y_view.yn; i++){
                for(int j = 0; j < nPoints; j++){
                    int currentX = j * 10;
                    int currentY = y_view.yd / 2 + y_view.yHeight / 2 + y_view.yScale * i;
                    canvas.drawLine(currentX,currentY,currentX + 5, currentY, xPaint);
                }
            }

            /*绘制“|”*/
            for(int i = 0; i < 20; i++) {
                canvas.drawLine(xD + xScale * i, y_view.yd / 2 + y_view.yHeight / 2, xD + xScale * i, y_view.yd / 2 - 10 + y_view.yHeight / 2, xPaint);
            }

            drawX(canvas);
        }

        /*绘制速度曲线*/
        public void drawX(Canvas canvas){
            if(vel.size() > 1){
                for(int i = 0; i < vel.size() - 1; i++){
                    int currentX = xD + xScale / 10 * i;
                    int currentY = y_view.yd / 2 + y_view.yHeight / 2 - (int)Math.round(vel.get(i) * 10 * y_view.yScale);
                    int nextX = xD + xScale / 10 * (i + 1);
                    int nextY = y_view.yd / 2 + y_view.yHeight / 2 - (int)Math.round(vel.get(i + 1) * 10 * y_view.yScale);
                    canvas.drawLine(currentX, currentY, nextX, nextY, lPaint);

                    if(i < time.size()){
                        canvas.drawText(time.get(i), xD + xScale * i - 35,  y_view.yd / 2 + 20 + y_view.yHeight / 2, tPaint);
                    }
                }
            }
        }

        public void setData(List<Double> vel, List<String> t){
            this.vel = vel;
            this.time = t;
            X_View.this.postInvalidate();
        }
    }

    @Override
    public void onDestroy(){
        ltimer.cancel();
        ttimer.cancel();
        super.onDestroy();
    }
}
