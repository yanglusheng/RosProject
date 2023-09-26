package com.example.rosproject.Dialogs;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.rosproject.Core.RobotInfo;
import com.example.rosproject.R;

public class AddEditRobotDialogFragment extends DialogFragment {

    public static final String POSITION_KEY = "POSITION_KEY";

    private final RobotInfo mInfo = new RobotInfo();

    private DialogListener mListener;

    private EditText mNameEditTextView;
    private EditText mMasterUriEditTextView;
    private View mAdvancedOptionsView;
    private EditText mJoystickTopicEditTextView;
    private EditText mLaserScanTopicEditTextView;
    private EditText mMapTopicEditTextView;
    private EditText mCameraTopicEditTextView;
    private EditText mGestureTopicEditTextView;
    private EditText mOdometryTopicEditTextView;
    private EditText mPoseTopicEditTextView;
    private EditText mDistanceTopicEditTextView;
    private EditText mGoalTopicEditTextView;

    private int mPosition = -1;

    @Override
    public void setArguments(Bundle args){
        super.setArguments(args);
        if(args != null){
            mPosition = args.getInt(POSITION_KEY, -1);
            mInfo.load(args);
        }
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try{
            mListener = (DialogListener) activity;
        }catch (ClassCastException e){
            //ingnore
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_robot, null);
        mNameEditTextView = (EditText) v.findViewById(R.id.robot_name_edit_text);
        mMasterUriEditTextView = (EditText) v.findViewById(R.id.master_uri_edit_view);

        CheckBox mAdvancedOptionsCheckbox = (CheckBox) v.findViewById(R.id.advanced_options_checkbox_view);
        mAdvancedOptionsView = v.findViewById(R.id.advanced_options_view);
        mJoystickTopicEditTextView = (EditText) v.findViewById(R.id.joystick_topic_edit_text);
        mLaserScanTopicEditTextView = (EditText) v.findViewById(R.id.laser_scan_edit_view);
        mMapTopicEditTextView = (EditText) v.findViewById(R.id.map_topic_edit_view);
        mCameraTopicEditTextView = (EditText) v.findViewById(R.id.camera_topic_edit_view);
        mGestureTopicEditTextView = (EditText) v.findViewById(R.id.gesture_topic_edit_view);
        mOdometryTopicEditTextView = (EditText) v.findViewById(R.id.odometry_topic_edit_view);
        mPoseTopicEditTextView = (EditText) v.findViewById(R.id.pose_topic_edit_view);
        mDistanceTopicEditTextView = (EditText) v.findViewById(R.id.distance_topic_edit_view);
        mGoalTopicEditTextView = (EditText) v.findViewById(R.id.goal_topic_edit_view);

        mNameEditTextView.setText(mInfo.getName());
        mMasterUriEditTextView.setText(mInfo.getMasterUriString());

        mAdvancedOptionsCheckbox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if(((CheckBox)v).isChecked()){
                    mAdvancedOptionsView.setVisibility(View.VISIBLE);
                }else{
                    mAdvancedOptionsView.setVisibility(View.GONE);
                }
            }
        });

        mJoystickTopicEditTextView.setText(mInfo.getJoystickTopic());
        mLaserScanTopicEditTextView.setText(mInfo.getLaserTopic());
        mMapTopicEditTextView.setText(mInfo.getMapTopic());
        mCameraTopicEditTextView.setText(mInfo.getCameraTopic());
        mGestureTopicEditTextView.setText(mInfo.getNavsatTopic());
        mOdometryTopicEditTextView.setText(mInfo.getOdometryTopic());
        mPoseTopicEditTextView.setText(mInfo.getPoseTopic());
        mDistanceTopicEditTextView.setText(mInfo.getDistanceTopic());
        mGoalTopicEditTextView.setText(mInfo.getGoalTopic());

        builder.setTitle(R.string.add_edit_robot)
                .setView(v)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = mNameEditTextView.getText().toString().trim();
                        String masterUri = mMasterUriEditTextView.getText().toString().trim();
                        String joystickTopic = mJoystickTopicEditTextView.getText().toString().trim();
                        String laserScanTopic = mLaserScanTopicEditTextView.getText().toString().trim();
                        String mapTopic = mMapTopicEditTextView.getText().toString().trim();
                        String cameraTopic = mCameraTopicEditTextView.getText().toString().trim();
                        String navsatTopic = mGestureTopicEditTextView.getText().toString().trim();
                        String odometryTopic = mOdometryTopicEditTextView.getText().toString().trim();
                        String poseTopic = mPoseTopicEditTextView.getText().toString().trim();
                        String distanceTopic = mDistanceTopicEditTextView.getText().toString().trim();
                        String goalTopic = mGoalTopicEditTextView.getText().toString().trim();

                        if(masterUri.equals("")){
                            Toast.makeText(getActivity(),"Master URI required",Toast.LENGTH_SHORT).show();
                        }else if(joystickTopic.equals("") || laserScanTopic.equals("") || cameraTopic.equals("")
                                || navsatTopic.equals("") || odometryTopic.equals("") || poseTopic.equals("")
                                ||mapTopic.equals("")||distanceTopic.equals("") || goalTopic.equals("")){
                            Toast.makeText(getActivity(), "All topic names are required", Toast.LENGTH_SHORT).show();
                        }else if(!name.equals("")){
                            mListener.onAddEditDialogPositiveClick(new RobotInfo(mInfo.getId(), name,
                                    masterUri, joystickTopic,laserScanTopic,mapTopic,cameraTopic,navsatTopic,odometryTopic,
                                    poseTopic,distanceTopic,goalTopic),mPosition);
                            dialog.dismiss();
                        }else{
                            Toast.makeText(getActivity(),"Robot name required", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onAddEditDialogNegativeClick(AddEditRobotDialogFragment.this);
                dialog.cancel();
            }
        });

        return builder.create();
    }
    public interface DialogListener{
        void onAddEditDialogPositiveClick(RobotInfo info, int position);
        void onAddEditDialogNegativeClick(DialogFragment dialog);
    }
}
