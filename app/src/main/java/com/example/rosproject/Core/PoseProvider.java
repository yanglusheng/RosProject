package com.example.rosproject.Core;

import org.ros.message.MessageListener;

import geometry_msgs.PoseWithCovarianceStamped;

public class PoseProvider implements MessageListener<PoseWithCovarianceStamped> {

    private double posx;
    private double posy;
    public PoseProvider(){
        posx = 0.0;
        posy = 0.0;
    }

    @Override
    public void onNewMessage(PoseWithCovarianceStamped poseWithCovarianceStamped) {
        posx = poseWithCovarianceStamped.getPose().getPose().getPosition().getX();
        posy = poseWithCovarianceStamped.getPose().getPose().getPosition().getY();
    }

    public double getPosX(){
        return posx;
    }

    public double getPosY(){
        return posy;
    }
}
