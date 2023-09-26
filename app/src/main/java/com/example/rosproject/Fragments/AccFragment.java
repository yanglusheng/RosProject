package com.example.rosproject.Fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rosproject.Core.ControlMode;
import com.example.rosproject.R;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;


public class AccFragment extends SimpleFragment{

    private final String TAG = "AccFragment";
    private View view;
    private Timer timer;
    private final Pattern VELOCITY_PATTERN = Pattern.compile("0\\.\\d{1,2}");
    private final Pattern TIME_PATTERN = Pattern.compile("[0-1]?\\d");

    private double linear_vel_step = 0;
    private final double linear_vel_max = 1.5;
    private double last_velocity = 0;
    private double linearVelocityX = 0;
    private double angularVelocityZ = 0;
    private final double angular_vel_step = 0.33;
    private final double angular_vel_max = 6.6;

    private boolean last_zero_vel_sent = true;
    private EditText velocity, time;

    private ControlMode controlMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view == null){
            view = inflater.inflate(R.layout.fragment_velocity_time, container, false);

            velocity = (EditText) view.findViewById(R.id.velocity_number);
            time = (EditText) view.findViewById(R.id.time_number);
            Button   ok = (Button) view.findViewById(R.id.OK);
            Button left = (Button) view.findViewById(R.id.acc_left);
            Button right = (Button) view.findViewById(R.id.acc_right);
            Button stop = (Button) view.findViewById(R.id.acc_stop);

            ok.setOnClickListener(new ButtonListener());
            left.setOnClickListener(new ButtonListener());
            right.setOnClickListener(new ButtonListener());
            stop.setOnClickListener(new ButtonListener());

        }

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if((linearVelocityX != 0.0) || (angularVelocityZ != 0.0)){
                    publishVelocity(linearVelocityX,0,angularVelocityZ);

                    linearVelocityX += linear_vel_step;
                    if(linearVelocityX > last_velocity){
                        linearVelocityX = last_velocity;
                    }
                    last_zero_vel_sent = false;
                }
                else if(!last_zero_vel_sent){
                    publishVelocity(linearVelocityX,0,angularVelocityZ);
                    last_zero_vel_sent = true;
                }
            }
        },0,100);

        return view;
    }


    private class ButtonListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.OK:

                    String vel = velocity.getText().toString().trim();
                    if(!VELOCITY_PATTERN.matcher(vel).matches()){
                        Toast.makeText(getControlApp(),"输入不合法，速度范围为[0,0.4]。请重新输入",Toast.LENGTH_LONG).show();
                        break;
                    }

                    String t = time.getText().toString().trim();
                    if(!TIME_PATTERN.matcher(t).matches()){
                        Toast.makeText(getControlApp(),"输入不合法，时间范围为[0,0.4]。请重新输入",Toast.LENGTH_LONG).show();
                        break;
                    }

                    double acc = Double.parseDouble(vel) / Integer.parseInt(t);
                    linear_vel_step = acc * 0.1;
                    linearVelocityX = linear_vel_step;
                    last_velocity = Math.min(Double.parseDouble(vel), linear_vel_max);
                    break;

                case R.id.acc_right:
                    if(angularVelocityZ < angular_vel_max)
                        angularVelocityZ += angular_vel_step;
                    break;

                case R.id.acc_left:
                    if(angularVelocityZ > -angular_vel_max)
                        angularVelocityZ -= angular_vel_step;
                    break;

                case R.id.acc_stop:
                    linearVelocityX = 0.0;
                    angularVelocityZ = 0.0;
                    linear_vel_step = 0.0;
            }
        }
    }

    @Override
    public void onDestroy(){
        timer.cancel();
        super.onDestroy();
    }


    private void publishVelocity(double linearVelocityX, double linearVelocityY, double angularVelocityZ){
        getControlApp().getRobotController().forceVelocity(linearVelocityX, linearVelocityY,angularVelocityZ);
    }

    public void setControlMode(ControlMode controlMode){
        this.controlMode = controlMode;
        this.invalidate();
    }

    public void invalidate(){
        switch (controlMode){
            case AccKey:
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
