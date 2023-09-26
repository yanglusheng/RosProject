package com.example.rosproject.Core;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.rosproject.ControlApp;
import com.example.rosproject.R;

import org.ros.message.MessageListener;
import org.ros.namespace.GraphName;
import org.ros.node.ConnectedNode;
import org.ros.node.Node;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMain;
import org.ros.node.NodeMainExecutor;
import org.ros.node.topic.Publisher;
import org.ros.node.topic.Subscriber;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import geometry_msgs.PoseWithCovarianceStamped;
import geometry_msgs.Twist;
import nav_msgs.Odometry;
import sensor_msgs.CompressedImage;
import std_msgs.Float64;

public class RobotController implements NodeMain, Savable{

    private static final String TAG = "RobotController";

    private final ControlApp context;

    private boolean initialized;
    private Timer publisherTimer;
    private boolean publishVelocity;

    private Publisher<Twist> movePublisher;
    private Twist currentVelocityCommand;

    private Subscriber<Odometry> odometrySubscriber;
    private Odometry odometry;
    private final Object odometryMutex = new Object();

    private Subscriber<PoseWithCovarianceStamped> amclPoseSubscriber;
    private PoseWithCovarianceStamped amclPose;
    private final Object amclPoseMutex = new Object();

    private Subscriber<CompressedImage> imageSubscriber;
    private CompressedImage image;
    private final Object imageMutex = new Object();
    private MessageListener<CompressedImage> imageMessageReceived;

    private Subscriber<Float64> distanceSubscriber;
    private Float64 distance;
    private final Object distanceMutex = new Object();

    private ConnectedNode connectedNode;

    private final ArrayList<MessageListener<Odometry>> odometryListeners;
    private final ArrayList<MessageListener<PoseWithCovarianceStamped>> amclPoseListeners;
    private final ArrayList<MessageListener<Float64>> distanceListeners;

    public final PoseProvider poseProvider;
    public final OdomDataProvider odomDataProvider;
    public final DistanceProvider distanceProvider;

    private static double speed;

    public RobotController(ControlApp context){
        this.context = context;
        this.initialized = false;

        this.odometryListeners = new ArrayList<>();
        this.amclPoseListeners = new ArrayList<>();
        this.distanceListeners = new ArrayList<>();

        this.poseProvider = new PoseProvider();
        this.odomDataProvider = new OdomDataProvider();
        this.distanceProvider = new DistanceProvider();

        this.addPoseListener(this.poseProvider);
        this.addOdometryListener(this.odomDataProvider);
        this.addDistanceListener(this.distanceProvider);
    }

    public void addOdometryListener(MessageListener<Odometry> l){
        odometryListeners.add(l);
    }

    public void addPoseListener(MessageListener<PoseWithCovarianceStamped> l){
        amclPoseListeners.add(l);
    }

    public void addDistanceListener(MessageListener<Float64> l){
        distanceListeners.add(l);
    }

    public void initialize(NodeMainExecutor nodeMainExecutor, NodeConfiguration nodeConfiguration){
        nodeMainExecutor.execute(this,nodeConfiguration.setNodeName("android/robot_controller"));
    }


    @Override
    public void load(@NonNull Bundle bundle) {

    }

    @Override
    public void save(@NonNull Bundle bundle) {

    }

    public void publishVelocity(double linearVelocityX, double linearVelocityY, double angularVelocityZ){
        if(currentVelocityCommand != null){
            currentVelocityCommand.getLinear().setX(linearVelocityX);
            currentVelocityCommand.getLinear().setY(-linearVelocityY);
            currentVelocityCommand.getLinear().setZ(0.0);
            currentVelocityCommand.getAngular().setX(0.0);
            currentVelocityCommand.getAngular().setY(0.0);
            currentVelocityCommand.getAngular().setZ(-angularVelocityZ);
        }else {
            Log.w("Emergency Stop","currentVelocityCommand is null");
        }
    }

    public void forceVelocity(double linearVelocityX, double linearVelocityY, double angularVelocityZ){
        publishVelocity = true;
        publishVelocity(linearVelocityX, linearVelocityY, angularVelocityZ);
    }

    @Override
    public GraphName getDefaultNodeName() {
        return null;
    }

    @Override
    public void onStart(ConnectedNode connectedNode) {
        this.connectedNode = connectedNode;
        initialize();
    }

    public void initialize(){
        if(!initialized && connectedNode != null){
            refreshTopic();
            initialized = true;
        }
    }

    public void refreshTopic() {
        String moveTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_joystick_topic_edittext_key),
                        context.getString(R.string.joy_topic));

        String odometryTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_odometry_topic_edittext_key),
                        context.getString(R.string.odometry_topic));

        String imageTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_camera_topic_edittext_key),
                        context.getString(R.string.camera_topic));

        String poseTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_pose_topic_edittext_key),
                        context.getString(R.string.pose_topic));

        String distanceTopic = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(context.getString(R.string.prefs_distance_topic_edittext_key),
                        context.getString(R.string.distance_topic));

        if(movePublisher == null || !moveTopic.equals(movePublisher.getTopicName().toString())){

            if(publisherTimer != null){
                publisherTimer.cancel();
            }

            if(movePublisher != null){
                movePublisher.shutdown();
            }

            movePublisher = connectedNode.newPublisher(moveTopic, Twist._TYPE);
            currentVelocityCommand = movePublisher.newMessage();

            publisherTimer = new Timer();
            publisherTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if(publishVelocity){
                        movePublisher.publish(currentVelocityCommand);
                        if(currentVelocityCommand.getLinear().getX() == 0.0
                                && currentVelocityCommand.getAngular().getZ() == 0.0){
                            publishVelocity = false;
                        }
                    }
                }
            },0,100);
            publishVelocity = false;
        }

        if(odometrySubscriber == null || !odometryTopic.equals(odometrySubscriber.getTopicName().toString())){

            if(odometrySubscriber != null){
                odometrySubscriber.shutdown();
            }

            odometrySubscriber = connectedNode.newSubscriber(odometryTopic, Odometry._TYPE);
            odometrySubscriber.addMessageListener(new MessageListener<Odometry>() {
                @Override
                public void onNewMessage(Odometry odometry) {
                    setOdometry(odometry);
                }
            });
        }

        if(amclPoseSubscriber == null || !poseTopic.equals(amclPoseSubscriber.getTopicName().toString())){
            if(amclPoseSubscriber != null){
                amclPoseSubscriber.shutdown();
            }

            amclPoseSubscriber = connectedNode.newSubscriber(poseTopic, PoseWithCovarianceStamped._TYPE);
            amclPoseSubscriber.addMessageListener(new MessageListener<PoseWithCovarianceStamped>() {
                @Override
                public void onNewMessage(PoseWithCovarianceStamped poseWithCovarianceStamped) {
                    setAmclPose(poseWithCovarianceStamped);
                }
            });
        }

        if(imageSubscriber == null || !imageTopic.equals(imageSubscriber.getTopicName().toString())) {
            if (imageSubscriber != null)
                imageSubscriber.shutdown();

            imageSubscriber = connectedNode.newSubscriber(imageTopic, CompressedImage._TYPE);

            imageSubscriber.addMessageListener(new MessageListener<CompressedImage>() {
                @Override
                public void onNewMessage(CompressedImage image) {
                    setImage(image);
                    synchronized (imageMutex) {
                        if (imageMessageReceived != null) {
                            imageMessageReceived.onNewMessage(image);
                        }
                    }
                }
            });
        }

        if(distanceSubscriber == null || !distanceTopic.equals(distanceSubscriber.getTopicName().toString())) {
            if(distanceSubscriber != null)
                distanceSubscriber.shutdown();

            distanceSubscriber = connectedNode.newSubscriber(distanceTopic, Float64._TYPE);

            distanceSubscriber.addMessageListener(new MessageListener<Float64>() {
                @Override
                public void onNewMessage(Float64 aDouble) {
                    setDistance(aDouble);
                }
            });
        }
    }

    protected void setOdometry(Odometry odometry){
        synchronized (odometryMutex){
            this.odometry = odometry;

            for(MessageListener<Odometry> listener: odometryListeners){
                listener.onNewMessage(odometry);
            }

            speed = odometry.getTwist().getTwist().getLinear().getX();
        }
    }

    protected void setAmclPose(PoseWithCovarianceStamped poseWithCovarianceStamped){
        synchronized (amclPoseMutex){
            this.amclPose = poseWithCovarianceStamped;

            for(MessageListener<PoseWithCovarianceStamped> listener: amclPoseListeners){
                listener.onNewMessage(poseWithCovarianceStamped);
            }
        }
    }

    protected void setDistance(Float64 aDouble){
        synchronized (distanceMutex){
            this.distance = aDouble;
            for(MessageListener<Float64> listener : distanceListeners){
                listener.onNewMessage(aDouble);
            }
        }
    }

    public void shutdownTopics(){
        if(publisherTimer != null){
            publisherTimer.cancel();
        }

        if(movePublisher != null){
            movePublisher.shutdown();
        }

        if(odometrySubscriber != null){
            odometrySubscriber.shutdown();
        }

        if(amclPoseSubscriber != null){
            amclPoseSubscriber.shutdown();
        }
    }
    @Override
    public void onShutdown(Node node) {
        shutdownTopics();
    }

    @Override
    public void onShutdownComplete(Node node) {
        this.connectedNode = null;
    }

    @Override
    public void onError(Node node, Throwable throwable) {
        Log.e(TAG,"",throwable);
    }


    public void setImage(CompressedImage image) {
        synchronized (imageMutex) {
            this.image = image;

        }
    }

    public void setCameraMessageReceivedListener(MessageListener<CompressedImage> cameraMessageReceived) {
        this.imageMessageReceived = cameraMessageReceived;
    }

    public static double getSpeed(){
        return speed;
    }
    public void stop(){
        publishVelocity = false;
        publishVelocity(0,0,0);

        if(movePublisher != null){
            movePublisher.publish(currentVelocityCommand);
        }
    }
}
