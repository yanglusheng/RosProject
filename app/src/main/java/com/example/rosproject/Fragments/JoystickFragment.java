package com.example.rosproject.Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.rosproject.Core.ControlMode;
import com.example.rosproject.R;
import com.example.rosproject.Views.JoystickView;

public class JoystickFragment extends Fragment {
    private JoystickView virtualJoystick;
    private View view;
    private ControlMode controlMode = ControlMode.Joystick;

    public JoystickFragment(){
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view == null){
            view = inflater.inflate(R.layout.fragment_joystick_view, container, false);
            virtualJoystick = (JoystickView) view.findViewById(R.id.joystick_view);
        }
        return view;
    }

    public JoystickView getJoystickView(){
        return virtualJoystick;
    }

    public ControlMode getControlMode(){
        return controlMode;
    }

    public void setControlMode(ControlMode controlMode){
        this.controlMode = controlMode;
        this.invalidate();
    }

    public void invalidate(){
        switch (controlMode){
            case Joystick:
            case Tilt:
                show();
                break;
            default:
                hide();
                break;
        }
        virtualJoystick.setControlMode(controlMode);
        virtualJoystick.controlSchemeChanged();
    }

    public void show() {
        getFragmentManager()
                .beginTransaction()
                .show(this)
                .commit();
    }

    public void hide(){
        getFragmentManager()
                .beginTransaction()
                .hide(this)
                .commit();
    }

    public void stop(){
        virtualJoystick.stop();
    }
}
