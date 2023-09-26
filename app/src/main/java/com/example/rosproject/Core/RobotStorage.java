package com.example.rosproject.Core;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.rosproject.R;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

public class RobotStorage {
    private static final String ROBOT_INFOS_KEY = "ROBOT_INFOS_KEY";

    private static List<RobotInfo> RobotInfos = Lists.newArrayList();
    private static Gson gson = new Gson();
    private static TreeMap<String, String> PrefKeyMap = new TreeMap<>();

    private static final String TAG = "RobotStorage";

    public static synchronized void load(Activity activity){
        SharedPreferences pref = activity.getPreferences(Context.MODE_PRIVATE);
        String defaultJson = gson.toJson(new ArrayList<RobotInfo>());

        String robotInfoJson = pref.getString(ROBOT_INFOS_KEY, defaultJson);
        Type listOfRobotInfoType = new TypeToken<List<RobotInfo>>(){}.getType();

        RobotInfos = gson.fromJson(robotInfoJson, listOfRobotInfoType);

        PrefKeyMap.put(RobotInfo.JOYSTICK_TOPIC_KEY, activity.getString(R.string.prefs_joystick_topic_edittext_key));
        PrefKeyMap.put(RobotInfo.CAMERA_TOPIC_KEY,activity.getString(R.string.prefs_camera_topic_edittext_key));
        PrefKeyMap.put(RobotInfo.LASER_SCAN_TOPIC_KEY, activity.getString(R.string.prefs_laserscan_topic_edittext_key));
        PrefKeyMap.put(RobotInfo.GESTURE_TOPIC_KEY,activity.getString(R.string.prefs_gesture_topic_edittext_key));
        PrefKeyMap.put(RobotInfo.ODOMETRY_TOPIC_KEY, activity.getString(R.string.prefs_odometry_topic_edittext_key));
        PrefKeyMap.put(RobotInfo.MAP_TOPIC_KEY, activity.getString(R.string.prefs_map_topic_edittext_key));
        PrefKeyMap.put(RobotInfo.POSE_TOPIC_KEY,activity.getString(R.string.prefs_pose_topic_edittext_key));
        PrefKeyMap.put(RobotInfo.DISTANCE_TOPIC_KEY,activity.getString(R.string.prefs_distance_topic_edittext_key));
        PrefKeyMap.put(RobotInfo.GOAL_TOPIC_KEY,activity.getString(R.string.prefs_goal_topic_edittext_key));
    }

    public static String getPreferenceKey(String bundleKey){
        return PrefKeyMap.get(bundleKey);
    }

    public static synchronized List<RobotInfo> getRobots(){
        return RobotInfos;
    }

    public static synchronized RobotInfo remove(Activity activity, int index){
        RobotInfo removed = RobotInfos.remove(index);
        save(activity);
        return removed;
    }

    public static synchronized boolean update(Activity activity, RobotInfo robot){
        boolean updated = false;
        for(int i = 0; i < RobotInfos.size(); i++){
            if(RobotInfos.get(i).compareTo(robot) == 0){
                RobotInfos.set(i, robot);
                save(activity);
                updated = true;
                break;
            }
        }
        return updated;
    }

    public static synchronized boolean add(Activity activity, RobotInfo robot){
        boolean added = RobotInfos.add(robot);
        save(activity);
        return added;
    }

    public static synchronized void save(Activity activity){
        String robotInfosJson = gson.toJson(RobotInfos);
        SharedPreferences.Editor editor = activity.getPreferences(Context.MODE_PRIVATE).edit();
        editor.putString(ROBOT_INFOS_KEY, robotInfosJson);
        editor.apply();
    }
}
