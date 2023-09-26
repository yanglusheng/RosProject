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

public class CameraViewFragment extends RosFragment{

    private RosImageView<sensor_msgs.CompressedImage> cameraView;
    private TextView noCameraTextView;
    private RobotController controller;

    public CameraViewFragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        final View view = inflater.inflate(R.layout.fragment_camera_view,null);
        noCameraTextView = (TextView) view.findViewById(R.id.noCameraTextView);

        //noinspection unchecked
        cameraView = (RosImageView<sensor_msgs.CompressedImage>) view.findViewById(R.id.camera_fragment_camera_view);

        cameraView.setTopicName(PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString(getString(R.string.prefs_camera_topic_edittext_key),getString(R.string.camera_topic)));
        cameraView.setMessageType(CompressedImage._TYPE);
        cameraView.setMessageToBitmapCallable(new BitmapFromCompressedImage());

        try{
            controller = ((ControlApp)getActivity()).getRobotController();
        }catch (Exception ignore){

        }

        if(controller != null){
            controller.setCameraMessageReceivedListener(new MessageListener<CompressedImage>() {
                @Override
                public void onNewMessage(CompressedImage compressedImage) {
                    if(compressedImage != null){
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

        if(nodeConfiguration != null)
            nodeMainExecutor.execute(cameraView, nodeConfiguration.setNodeName("android/fragment_camera_view"));

        return  view;
    }

    @Override
    void shutdown() {
        nodeMainExecutor.shutdownNodeMain(cameraView);
    }
}
