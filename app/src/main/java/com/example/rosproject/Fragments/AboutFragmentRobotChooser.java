package com.example.rosproject.Fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.example.rosproject.Core.Utils;
import com.example.rosproject.R;

public class AboutFragmentRobotChooser extends Fragment {
    public AboutFragmentRobotChooser() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.fragment_about, null);

        WebView webView = (WebView) view.findViewById(R.id.abouttxt);
        webView.loadData(Utils.readText(getActivity(), R.raw.about), "text/html", null);

        return view;


    }
}
