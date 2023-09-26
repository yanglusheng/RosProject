package com.example.rosproject.Core;

import org.ros.message.MessageListener;

import java.util.ArrayList;
import java.util.List;

import nav_msgs.Odometry;

public class OdomDataProvider implements MessageListener<Odometry> {

    private double data;

    public OdomDataProvider(){
        data = 0.0;
    }

    @Override
    public void onNewMessage(Odometry odometry) {
        data = odometry.getTwist().getTwist().getLinear().getX();
    }

    public Double getData(){
        return data;
    }
}
