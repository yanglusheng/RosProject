package com.example.rosproject.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DistanceView extends View {

    private int xPoint = 100;
    private int yPoint = 500;

    private int xScale = 4;
    private int yScale = 40;

    private int xLength = 380;
    private int yLength = 480;

    private int MaxDataSize = xLength / xScale;

    private List<Double> data = new ArrayList<>();

    private String[] yLabel = new String[yLength/yScale];

    Paint paint = new Paint();

    public DistanceView(Context context) {
        super(context);
        for(int i = 0; i < yLabel.length; i++){
            DecimalFormat decimalFormat = new DecimalFormat("0.0");
            yLabel[i] = decimalFormat.format(0.1 * i) + "m";
        }
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setColor(Color.RED);

        canvas.drawLine(xPoint, yPoint - yLength, xPoint, yPoint, paint);
        canvas.drawLine(xPoint, yPoint - yLength, xPoint - 3, yPoint - yLength + 6, paint);
        canvas.drawLine(xPoint, yPoint - yLength, xPoint + 3, yPoint - yLength + 6, paint);
        for(int i = 0; i * yScale < yLength; i++){
            canvas.drawLine(xPoint, yPoint - i * yScale, xPoint + 5, yPoint - i * yScale, paint);
            canvas.drawText(yLabel[i], xPoint - 40, yPoint - i * yScale, paint);
        }

        canvas.drawLine(xPoint, yPoint, xPoint + xLength, yPoint, paint);
        canvas.drawLine(xPoint + xLength, yPoint, xPoint + xLength - 6, yPoint - 3, paint);
        canvas.drawLine(xPoint + xLength, yPoint, xPoint + xLength - 6, yPoint + 3, paint);

        if(data.size() > 1){
            paint.setColor(Color.BLUE);
            paint.setStrokeWidth(3f);
            for (int i = 1; i < data.size(); i++){
                canvas.drawLine(xPoint + (i - 1) * xScale, yPoint - Double.valueOf(data.get(i - 1) / 0.01).intValue() * (yScale / 10),
                        xPoint + i * xScale, yPoint - Double.valueOf(data.get(i) / 0.01).intValue() * (yScale/10), paint);
            }
        }
    }

    public void setData(List<Double> data){
        this.data = data;
        DistanceView.this.invalidate();
    }
}
