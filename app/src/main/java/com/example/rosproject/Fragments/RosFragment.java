package com.example.rosproject.Fragments;

import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

public abstract class RosFragment extends SimpleFragment{
    protected NodeMainExecutor nodeMainExecutor;
    protected NodeConfiguration nodeConfiguration;

    private boolean initialized;

    @Override
    public void onDestroyView(){
        shutdown();
        super.onDestroyView();
    }

    abstract void shutdown();

    public void initialize(NodeMainExecutor mainExecutor, NodeConfiguration nodeConfiguration){
        this.nodeMainExecutor = mainExecutor;
        this.nodeConfiguration = nodeConfiguration;
        setInitialized(true);
    }

    public boolean isInitialized(){
        return initialized;
    }

    public void setInitialized(boolean initialized){
        this.initialized = initialized;
    }
}
