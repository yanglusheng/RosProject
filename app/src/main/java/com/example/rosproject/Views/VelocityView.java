package com.example.rosproject.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class VelocityView extends View {

    private int xPoint = 100;
    private int yPoint = 250;

    private int xScale = 40;
    private int yScale = 40;

    private int xLength = 800;
    private int yLength = 200;

    private int MaxDataSize = xLength / xScale;

    private List<Double> velData = new ArrayList<>();
    private List<String> timeData = new ArrayList<>();

    private String[] y1 = new String[yLength/yScale];   //y正半轴
    private String[] y2 = new String[yLength/yScale];   //y负半轴

    Paint paintXY = new Paint();
    Paint paintD = new Paint();

    public VelocityView(Context context) {
        super(context);
        for(int i = 0; i < y1.length; i++){
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            y1[i] = decimalFormat.format(0.1 * i) + "m/s";
        }

        for(int i = 1; i < y2.length; i++){
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            y2[i] = "-" + decimalFormat.format(0.1 * i) + "m/s";
        }

        paintXY.setStyle(Paint.Style.STROKE);
        paintXY.setAntiAlias(true);
        paintXY.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        drawY(canvas);
        drawX(canvas);

        if(velData.size() > 1){
            paintD.setStyle(Paint.Style.STROKE);
            paintD.setAntiAlias(true);
            paintD.setColor(Color.BLUE);
            paintD.setStrokeWidth(3f);
            for (int i = 1; i < velData.size(); i++){
                canvas.drawLine(xPoint + (i - 1) * xScale, yPoint - Double.valueOf(velData.get(i - 1) / 0.01).intValue() * (yScale / 10),
                        xPoint + i * xScale, yPoint - Double.valueOf(velData.get(i) / 0.01).intValue() * (yScale / 10), paintD);
            }
        }
    }

    public void setData(List<Double> velData, List<String> timeData){
        this.velData = velData;
        this.timeData = timeData;
        VelocityView.this.invalidate();
    }

    private void drawY(Canvas canvas){

        //绘制y正半轴
        canvas.drawLine(xPoint, yPoint - yLength, xPoint, yPoint, paintXY);
        //绘制y负半轴
        canvas.drawLine(xPoint, yPoint + yLength, xPoint, yPoint, paintXY);
        //绘制y正半轴箭头
        canvas.drawLine(xPoint, yPoint - yLength, xPoint - 3, yPoint - yLength + 6, paintXY);
        canvas.drawLine(xPoint, yPoint - yLength, xPoint + 3, yPoint - yLength + 6, paintXY);
        //绘制y负半轴箭头
        canvas.drawLine(xPoint, yPoint + yLength, xPoint - 3, yPoint + yLength - 6, paintXY);
        canvas.drawLine(xPoint, yPoint + yLength, xPoint + 3, yPoint + yLength - 6, paintXY);

        //绘制y轴正半轴m/s
        for(int i = 0; i * yScale < yLength; i++){
            canvas.drawLine(xPoint, yPoint - i * yScale, xPoint + 5, yPoint - i * yScale, paintXY);
            canvas.drawText(y1[i], xPoint - 50, yPoint - i * yScale, paintXY);
        }

        //绘制y轴负半轴m/s
        for(int i = 1; i * yScale < yLength; i++){
            canvas.drawLine(xPoint, yPoint + i * yScale, xPoint + 5, yPoint + i * yScale, paintXY);
            canvas.drawText(y2[i], xPoint - 50, yPoint + i * yScale, paintXY);
        }
    }

    public void drawX(Canvas canvas){
        canvas.drawLine(xPoint, yPoint, xPoint + xLength, yPoint, paintXY);
        canvas.drawLine(xPoint + xLength, yPoint, xPoint + xLength - 6, yPoint - 3, paintXY);
        canvas.drawLine(xPoint + xLength, yPoint, xPoint + xLength - 6, yPoint + 3, paintXY);

        for(int i = 0; i < timeData.size(); i++){
            canvas.drawText(timeData.get(i), xPoint + i * xScale, yPoint + 20, paintXY);
        }
    }
}
