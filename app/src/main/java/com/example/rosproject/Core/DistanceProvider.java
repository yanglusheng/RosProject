package com.example.rosproject.Core;

import android.util.Log;

import org.ros.message.MessageListener;


import std_msgs.Float64;

public class DistanceProvider implements MessageListener<Float64> {

    private double data;

    public DistanceProvider(){
        data = -1;
    }


    @Override
    public void onNewMessage(Float64 dis) {
        data = dis.getData();
    }

    public double getData(){
        return data;
    }
}