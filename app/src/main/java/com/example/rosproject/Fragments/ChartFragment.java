package com.example.rosproject.Fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.example.rosproject.R;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ChartFragment extends SimpleFragment {

    private View view;
    private Button vel_Button;
    private Button acc_Button;
    private Button dis_Button;
    private Button start_save;
    private Button end_save;

    private Fragment fragment = null;
    private Timer dtimer;
    private List<Double> data;
    private double prev = 0;
    private int cnt = 0;
    int flag = -1;

    public ChartFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_chart_view,container,false);
            vel_Button = (Button) view.findViewById(R.id.vel_button);
            acc_Button = (Button) view.findViewById(R.id.acc_button);
            dis_Button = (Button) view.findViewById(R.id.dis_button);
            start_save = (Button) view.findViewById(R.id.start_save);
            end_save = (Button) view.findViewById(R.id.end_save);

            vel_Button.setOnClickListener(new ButtonListener());
            acc_Button.setOnClickListener(new ButtonListener());
            dis_Button.setOnClickListener(new ButtonListener());
            start_save.setOnClickListener(new ButtonListener());
            end_save.setOnClickListener(new ButtonListener());

            selectItem(0);
            vel_Button.setEnabled(false);
            acc_Button.setEnabled(true);
            dis_Button.setEnabled(true);
        }
        return view;
    }


    public void selectItem(int pos){
        switch (pos){
            case 0:
                fragment = new VelFragment();
                flag = 0;
                getFragmentManager().beginTransaction().replace(R.id.show_fragment, fragment).commit();
                break;
            case 1:
                fragment = new DisFragment();
                flag = 1;
                getFragmentManager().beginTransaction().replace(R.id.show_fragment, fragment).commit();
                break;

            case 2:
                fragment = new AccLineFragment();
                flag = 2;
                getFragmentManager().beginTransaction().replace(R.id.show_fragment, fragment).commit();
            default:
                break;
        }
    }

    private class ButtonListener implements View.OnClickListener{
        public void onClick(View v){
            switch(v.getId()) {
                case R.id.vel_button:
                    vel_Button.setEnabled(false);
                    acc_Button.setEnabled(true);
                    dis_Button.setEnabled(true);
                    selectItem(0);
                    break;

                case R.id.dis_button:
                    vel_Button.setEnabled(true);
                    acc_Button.setEnabled(true);
                    dis_Button.setEnabled(false);
                    selectItem(1);
                    break;

                case R.id.acc_button:
                    vel_Button.setEnabled(true);
                    acc_Button.setEnabled(false);
                    dis_Button.setEnabled(true);
                    selectItem(2);
                    break;

                case R.id.start_save:
                    start_save.setEnabled(false);
                    data = new ArrayList<>();
                    getData(flag);
                    break;

                case R.id.end_save:
                    start_save.setEnabled(true);
                    saveDate(flag);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroy(){
        if(dtimer != null)
            dtimer.cancel();
        super.onDestroy();
    }

    private void getData(int t){

        final DecimalFormat df = new DecimalFormat("0.00");
        dtimer = new Timer();
        dtimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(t == 0){
                    double v = getControlApp().getRobotController().odomDataProvider.getData();
                    v =  Double.parseDouble(df.format(v));
                    data.add(v);
                    if(data.size() > 10 * 60 * 30){
                        data.remove(0);
                    }
                }else if(t == 1){
                    double d = getControlApp().getRobotController().distanceProvider.getData();
                    d =  Double.parseDouble(df.format(d));
                    if(d > -1){
                        data.add(d);
                        if(data.size() > 10 * 60 * 30){
                            data.remove(0);
                        }
                    }
                }else if(t == 2){
                    double v = getControlApp().getRobotController().odomDataProvider.getData();
                    cnt++;
                    if(cnt > 1){
                        double acc = (v - prev) * 10;
                        acc = Double.parseDouble(df.format(acc));
                        data.add(acc);
                    }
                    if(data.size() > 10 * 60 * 30){
                        data.remove(0);
                    }
                    prev = v;
                }
            }
        },0 ,100);
    }

    private void saveDate(int t){
        if(dtimer != null){
            dtimer.cancel();
        }
        if(data != null){
            try{
                String file = Environment.getExternalStorageDirectory().getPath();

                String filename = "/sdcard/Download/";
                if(t == 0){
                    filename += "vel.txt";
                }else if(t == 1){
                    filename += "dis.txt";
                }else if(t == 2){
                    filename += "acc.txt";
                }
                FileWriter fw = new FileWriter(filename);
                for(int i = 0; i < data.size(); i++){
                    fw.write(data.get(i)+"\r\n");
                }
                fw.close();
                Toast.makeText(getControlApp(),"保存成功",Toast.LENGTH_LONG).show();
            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getControlApp(),"保存失败",Toast.LENGTH_LONG).show();
            }
        }
    }
}
