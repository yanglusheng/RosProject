package com.example.rosproject.Core;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.net.URI;
import java.util.List;
import java.util.UUID;

public class RobotInfo implements Comparable<RobotInfo>, Savable {

    private static int robotCount = 1;

    public static final String UUID_KEY = "UUID_KEY";
    public static final String ROBOT_NAME_KEY = "ROBOT_NAME_KEY";
    public static final String MASTER_URI_KEY = "MASTER_URI_KEY";

    public static final String JOYSTICK_TOPIC_KEY = "JOYSTICK_TOPIC_KEY";
    public static final String LASER_SCAN_TOPIC_KEY = "LASER_SCAN_TOPIC_KEY";
    public static final String MAP_TOPIC_KEY = "MAP_TOPIC_KEY";
    public static final String CAMERA_TOPIC_KEY = "CAMERA_TOPIC_KEY";
    public static final String GESTURE_TOPIC_KEY = "GESTURE_TOPIC_KEY";
    public static final String ODOMETRY_TOPIC_KEY = "ODOMETRY_TOPIC_KEY";
    public static final String POSE_TOPIC_KEY = "POSE_TOPIC_KEY";
    public static final String DISTANCE_TOPIC_KEY = "DISTANCE_TOPIC_KEY";
    public static final String GOAL_TOPIC_KEY = "GOAL_TOPIC_KEY";

    private UUID id = UUID.randomUUID();
    private String name;
    private String masterUriString;

    private String joystickTopic;
    private String cameraTopic;
    private String laserTopic;
    private String mapTopic;
    private String gestureTopic;
    private String odometryTopic;
    private String poseTopic;
    private String distanceTopic;
    private String goalTopic;

    public RobotInfo(){
        name = "智能车" + robotCount++;
        masterUriString = "http://localhost:11311";
        joystickTopic = "/cmd_vel";
        cameraTopic = "/usb_cam/image_raw/compressed";
        laserTopic = "/scan";
        mapTopic = "/map";
        gestureTopic = "/base_footprint";
        odometryTopic = "/odom";
        poseTopic = "/amcl_pose";
        distanceTopic = "/salve";
        goalTopic = "/move_base_simple/goal";
    }

    public RobotInfo(UUID id, String name, String masterUriString, String joystickTopic, String laserTopic, String mapTopic,
                     String cameraTopic, String gestureTopic, String odometryTopic, String poseTopic, String distanceTopic,
                     String goalTopic){
        this.id = id;
        this.name = name;
        this.masterUriString = masterUriString;
        this.joystickTopic = joystickTopic;
        this.laserTopic = laserTopic;
        this.mapTopic = mapTopic;
        this.cameraTopic = cameraTopic;
        this.gestureTopic = gestureTopic;
        this.odometryTopic = odometryTopic;
        this.poseTopic = poseTopic;
        this.distanceTopic = distanceTopic;
        this.goalTopic = goalTopic;
    }


    public UUID getId(){
        return id;
    }

    public void setId(UUID id){
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getMasterUriString(){
        return masterUriString;
    }

    public void setMasterUriString(String masterUriString){
        this.masterUriString = masterUriString;
    }

    public String getJoystickTopic(){
        return joystickTopic;
    }

    public void setJoystickTopic(String joystickTopic){
        this.joystickTopic = joystickTopic;
    }

    public String getLaserTopic(){
        return laserTopic;
    }

    public void setLaserTopic(String laserTopic){
        this.laserTopic = laserTopic;
    }

    public String getMapTopic(){
        return mapTopic;
    }

    public void setMapTopic(String mapTopic){
        this.mapTopic = mapTopic;
    }

    public String getCameraTopic(){
        return cameraTopic;
    }

    public void setCameraTopic(String cameraTopic){
        this.cameraTopic = cameraTopic;
    }

    public String getOdometryTopic(){
        return odometryTopic;
    }

    public void setOdometryTopic(String odometryTopic){
        this.odometryTopic = odometryTopic;
    }

    public String getNavsatTopic(){
        return gestureTopic;
    }

    public  void setNavsatTopic(String gestureTopic){
        this.gestureTopic = gestureTopic;
    }

    public String getPoseTopic(){
        return poseTopic;
    }

    public void setPoseTopic(String poseTopic){
        this.poseTopic = poseTopic;
    }

    public String getDistanceTopic(){
        return distanceTopic;
    }

    public String getGoalTopic(){
        return goalTopic;
    }

    public void setGoalTopic(String goalTopic){
        this.goalTopic = goalTopic;
    }

    public URI getUri(){
        return URI.create(getMasterUriString());
    }

    @Override
    public void load(@NonNull Bundle bundle) {
        id = UUID.fromString(bundle.getString(UUID_KEY,UUID.randomUUID().toString()));
        name = bundle.getString(ROBOT_NAME_KEY,"");
        masterUriString = bundle.getString(MASTER_URI_KEY,"https://localhost:11311");
        joystickTopic = bundle.getString(JOYSTICK_TOPIC_KEY,"/cmd_vel");
        cameraTopic = bundle.getString(CAMERA_TOPIC_KEY, "/usb_cam/image_raw/compressed");
        mapTopic = bundle.getString(MAP_TOPIC_KEY,"/map");
        laserTopic = bundle.getString(LASER_SCAN_TOPIC_KEY,"/scan");
        gestureTopic = bundle.getString(GESTURE_TOPIC_KEY,"/base_footprint");
        odometryTopic = bundle.getString(ODOMETRY_TOPIC_KEY,"/odom");
        poseTopic = bundle.getString(POSE_TOPIC_KEY,"/amcl_pose");
        distanceTopic = bundle.getString(DISTANCE_TOPIC_KEY,"/salve");
        goalTopic = bundle.getString(GOAL_TOPIC_KEY,"/move_base_simple/goal");
    }

    public void load(@NonNull SharedPreferences prefs){
        joystickTopic = prefs.getString(RobotStorage.getPreferenceKey(JOYSTICK_TOPIC_KEY), "/joy_teleop/cmd_vel");
        cameraTopic = prefs.getString(RobotStorage.getPreferenceKey(CAMERA_TOPIC_KEY), "/usb_cam/image_raw/compressed");
        laserTopic = prefs.getString(RobotStorage.getPreferenceKey(LASER_SCAN_TOPIC_KEY), "/scan");
        mapTopic = prefs.getString(RobotStorage.getPreferenceKey(MAP_TOPIC_KEY),"/map");
        gestureTopic = prefs.getString(RobotStorage.getPreferenceKey(GESTURE_TOPIC_KEY), "/base_footprint");
        odometryTopic = prefs.getString(RobotStorage.getPreferenceKey(ODOMETRY_TOPIC_KEY), "/odom");
        poseTopic = prefs.getString(RobotStorage.getPreferenceKey(POSE_TOPIC_KEY), "/amcl_pose");
        distanceTopic = prefs.getString(RobotStorage.getPreferenceKey(DISTANCE_TOPIC_KEY),"/salve");
        goalTopic = prefs.getString(RobotStorage.getPreferenceKey(GOAL_TOPIC_KEY),"/move_base_simple/goal");
    }


    @Override
    public void save(@NonNull Bundle bundle) {
        bundle.putString(UUID_KEY,id.toString());
        bundle.putString(ROBOT_NAME_KEY, name);
        bundle.putString(MASTER_URI_KEY, masterUriString);
        bundle.putString(JOYSTICK_TOPIC_KEY, joystickTopic);
        bundle.putString(CAMERA_TOPIC_KEY, cameraTopic);
        bundle.putString(MAP_TOPIC_KEY, mapTopic);
        bundle.putString(LASER_SCAN_TOPIC_KEY, laserTopic);
        bundle.putString(GESTURE_TOPIC_KEY, gestureTopic);
        bundle.putString(ODOMETRY_TOPIC_KEY, odometryTopic);
        bundle.putString(POSE_TOPIC_KEY, poseTopic);
        bundle.putString(DISTANCE_TOPIC_KEY,distanceTopic);
        bundle.putString(GOAL_TOPIC_KEY, goalTopic);
    }

    public void save(@NonNull SharedPreferences.Editor prefs){
        prefs.putString(RobotStorage.getPreferenceKey(JOYSTICK_TOPIC_KEY), joystickTopic);
        prefs.putString(RobotStorage.getPreferenceKey(CAMERA_TOPIC_KEY),cameraTopic);
        prefs.putString(RobotStorage.getPreferenceKey(LASER_SCAN_TOPIC_KEY),laserTopic);
        prefs.putString(RobotStorage.getPreferenceKey(MAP_TOPIC_KEY), mapTopic);
        prefs.putString(RobotStorage.getPreferenceKey(GESTURE_TOPIC_KEY),gestureTopic);
        prefs.putString(RobotStorage.getPreferenceKey(ODOMETRY_TOPIC_KEY),odometryTopic);
        prefs.putString(RobotStorage.getPreferenceKey(POSE_TOPIC_KEY),poseTopic);
        prefs.putString(RobotStorage.getPreferenceKey(DISTANCE_TOPIC_KEY),distanceTopic);
        prefs.putString(RobotStorage.getPreferenceKey(GOAL_TOPIC_KEY),goalTopic);
    }

    @Override
    public int compareTo(@NonNull RobotInfo another) {
        if(this.getId() == null){
            return -1;
        }
        if(another.getId() == null){
            return 1;
        }

        return this.getId().compareTo(another.getId());
    }

    public static void resolveRobotCount(List<RobotInfo> list)
    {
        int max = 0;
        int val;

        for (RobotInfo info: list) {
            if (info.getName().startsWith("智能车")) {
                try {
                    val = Integer.parseInt(info.getName().substring(3));
                } catch (NumberFormatException e) {
                    val = -1;
                }
                if (val > max)
                    max = val;
            }
        }

        robotCount = max + 1;
    }
}
