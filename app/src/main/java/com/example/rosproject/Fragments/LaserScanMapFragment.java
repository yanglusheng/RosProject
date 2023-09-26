package com.example.rosproject.Fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rosproject.R;
import com.google.common.collect.Lists;

import org.ros.android.view.visualization.VisualizationView;
import org.ros.android.view.visualization.layer.CameraControlLayer;
import org.ros.android.view.visualization.layer.LaserScanLayer;
import org.ros.android.view.visualization.layer.Layer;
import org.ros.android.view.visualization.layer.OccupancyGridLayer;
import org.ros.android.view.visualization.layer.PathLayer;
import org.ros.android.view.visualization.layer.PosePublisherLayer;
import org.ros.android.view.visualization.layer.RobotLayer;

public class LaserScanMapFragment extends RosFragment{

    private VisualizationView visualizationView;
    public LaserScanMapFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View view = inflater.inflate(R.layout.fragment_lasercan_map, null);

        String laserScanTopic = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.prefs_laserscan_topic_edittext_key), getString(R.string.laser_scan_topic));

        String mapTopic = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.prefs_map_topic_edittext_key),getString(R.string.map_topic));

        String gestureTopic = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.prefs_gesture_topic_edittext_key),getString(R.string.gesture_topic));

        String goalTopic = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.prefs_goal_topic_edittext_key),getString(R.string.goal_topic));


        visualizationView = (VisualizationView) view.findViewById(R.id.laserScanMap);

        visualizationView.getCamera().jumpToFrame("map");

        visualizationView.onCreate(Lists.<Layer>newArrayList(
                new CameraControlLayer(),
                new OccupancyGridLayer(mapTopic),
                new LaserScanLayer(laserScanTopic),
                new PosePublisherLayer(goalTopic),
                new PathLayer("move_base/TebLocalPlannerROS/global_plan"),
                new RobotLayer(gestureTopic)));

        visualizationView.init(nodeMainExecutor);
        if(nodeConfiguration != null){
            nodeMainExecutor.execute(visualizationView, nodeConfiguration.setNodeName("android/fragmentLaserScanMap"));
        }
        return view;
    }

    @Override
    void shutdown() {
        nodeMainExecutor.shutdownNodeMain(visualizationView);
    }
}
