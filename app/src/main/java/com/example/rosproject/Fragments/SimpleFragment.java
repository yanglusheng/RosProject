package com.example.rosproject.Fragments;

import android.app.Fragment;

import com.example.rosproject.ControlApp;

public class SimpleFragment extends Fragment {

    public void show(){
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

    public ControlApp getControlApp(){
        if(getActivity() instanceof ControlApp)
            return (ControlApp) getActivity();
        else
            return null;
    }
}
