package com.example.rosproject.Fragments;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rosproject.ControlApp;
import com.example.rosproject.Core.RobotController;
import com.example.rosproject.R;

import org.ros.android.BitmapFromCompressedImage;
import org.ros.android.view.RosImageView;
import org.ros.message.MessageListener;

import sensor_msgs.CompressedImage;

public class OverviewFragment extends RosFragment{

    private View view;
    private TextView noCameraTextView;
    private RosImageView<sensor_msgs.CompressedImage> cameraView;
    private RobotController controller;

    public OverviewFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        if(view == null) {
            view = inflater.inflate(R.layout.activity_main, container, false);
            noCameraTextView = (TextView) view.findViewById(R.id.noCameraTextView);


            LaserScanMapFragment laserScanMapFragment = new LaserScanMapFragment();
            ((RosFragment) laserScanMapFragment).initialize(((ControlApp)getActivity()).getNodeMainExecutor(),
                    ((ControlApp)getActivity()).getNodeConfiguration());
            getFragmentManager().beginTransaction().replace(R.id.laser_scan_placeholder, laserScanMapFragment).commit();


            cameraView = (RosImageView<CompressedImage>) view.findViewById(R.id.camera_view);
            cameraView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString("prefs_camera_topic_edittext_key",getString(R.string.camera_topic)));
            cameraView.setMessageType(CompressedImage._TYPE);
            cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());


            try {
                controller = ((ControlApp) getActivity()).getRobotController();
            }
            catch(Exception ignore){
            }

            if(controller != null){
                controller.setCameraMessageReceivedListener(new MessageListener<CompressedImage>() {
                    @Override
                    public void onNewMessage(CompressedImage compressedImage) {
                        if (compressedImage != null) {
                            controller.setCameraMessageReceivedListener(null);
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    noCameraTextView.setVisibility(View.GONE);
                                }
                            });
                        }
                    }
                });
            }


            if (isInitialized()) {
                nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName("android/camera_view"));
            }

        }

        return view;
    }


    @Override
    public void shutdown() {
        if(isInitialized()){
            nodeMainExecutor.shutdownNodeMain(cameraView);
        }
    }
}
