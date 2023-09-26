package com.example.rosproject.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.rosproject.Core.ControlMode;
import com.example.rosproject.R;

import java.util.Timer;
import java.util.TimerTask;

public class ButtonFragment extends SimpleFragment {

    private final String TAG = "ButtonFragment";
    private View view;
    private final double linear_vel_step = 0.05;
    private final double linear_vel_max = 1.5;
    private final double angular_vel_step = 0.33;
    private final double angular_vel_max = 6.6;
    private double linearVelocityX = 0;
    private double angularVelocityZ = 0;
    private boolean last_zero_vel_sent = true;
    private Timer timer;
    private ControlMode controlMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view == null){
            view = inflater.inflate(R.layout.fragment_button_view, container, false);

            Button left = (Button) view.findViewById(R.id.left_button);
            Button right = (Button) view.findViewById(R.id.right_button);
            Button up = (Button) view.findViewById(R.id.up_button);
            Button down = (Button) view.findViewById(R.id.down_button);
            Button stop = (Button) view.findViewById(R.id.stop_button);

            left.setOnClickListener(new ButtonListener());
            right.setOnClickListener(new ButtonListener());
            up.setOnClickListener(new ButtonListener());
            down.setOnClickListener(new ButtonListener());
            stop.setOnClickListener(new ButtonListener());
        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if((linearVelocityX != 0.0) || (angularVelocityZ != 0.0)){
                    publishVelocity(linearVelocityX,0,angularVelocityZ);
                    last_zero_vel_sent = false;
                }
                else if(!last_zero_vel_sent){
                    publishVelocity(linearVelocityX,0,angularVelocityZ);
                    last_zero_vel_sent = true;
                }
            }
        },0,100);

        /*
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(alive){
                    if((linearVelocityX != 0.0) || angularVelocityZ != 0.0){
                        publishVelocity(linearVelocityX,0,angularVelocityZ);
                        last_zero_vel_sent = false;
                    }
                    else if(last_zero_vel_sent = false){
                        publishVelocity(linearVelocityX,0,angularVelocityZ);
                        last_zero_vel_sent = true;
                    }
                }
            }
        }).start();*/

        return view;
    }


    @Override
    public void onDestroy(){
        timer.cancel();
        super.onDestroy();
    }

    private class ButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View v){
            switch(v.getId()) {
                case R.id.right_button:
                    incrementAngularVelocity();
                    break;
                case R.id.left_button:
                    decrementAngularVelocity();
                    break;
                case R.id.up_button:
                    incremnetLinearVelocity();
                    break;
                case R.id.down_button:
                    decrementLinearVelocity();
                    break;
                case R.id.stop_button:
                    linearVelocityX = 0.0;
                    angularVelocityZ = 0.0;
                    break;
            }
        }
    }

    private void incremnetLinearVelocity(){
        if(linearVelocityX < linear_vel_max){
            linearVelocityX += linear_vel_step;
        }
    }

    private void decrementLinearVelocity(){
        if(linearVelocityX > -linear_vel_max){
            linearVelocityX -= linear_vel_step;
        }
    }

    private void incrementAngularVelocity(){
        if(angularVelocityZ < angular_vel_max){
            angularVelocityZ += angular_vel_step;
        }
    }

    private void decrementAngularVelocity(){
        if(angularVelocityZ > -angular_vel_max){
            angularVelocityZ -= angular_vel_step;
        }
    }

    private void publishVelocity(double linearVelocityX, double linearVelocityY, double angularVelocityZ){
        getControlApp().getRobotController().forceVelocity(linearVelocityX, linearVelocityY,
                angularVelocityZ);
    }

    public void setControlMode(ControlMode controlMode){
        this.controlMode = controlMode;
        this.invalidate();
    }

    public void invalidate() {
        switch (controlMode){
            case ButtonKey:
                show();
                break;
            default:
                hide();
                break;
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden){
        super.onHiddenChanged(hidden);
        linearVelocityX = 0;
        angularVelocityZ = 0;
    }

    public void stop(){
        publishVelocity(0,0,0);
    }
}
