package com.example.rosproject.Fragments;

import android.content.Context;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rosproject.ControlApp;
import com.example.rosproject.Core.RobotController;
import com.example.rosproject.R;

import org.ros.message.MessageListener;

import java.util.Timer;
import java.util.TimerTask;

import nav_msgs.Odometry;

public class HUDFragment extends SimpleFragment implements MessageListener<Odometry> {

    private static final String TAG = "HUDFragment";

    private View view;
    private TextView speedView, turnrateView, posxView, posyView;
    private ImageView wifiStrengthView;

    private final UpdateUIRunnable UPDATE_UI_RUNNABLE = new UpdateUIRunnable();
    private final Updater UPDATER = new Updater();
    private WifiManager wifiManager;

    private double lastSpeed, lastTurnrate;
    private int lastWifiImage;


    private static final int[] WIFI_ICONS;
    static {
        WIFI_ICONS = new int[]{
                R.drawable.wifi_0,
                R.drawable.wifi_1,
                R.drawable.wifi_2,
                R.drawable.wifi_3,
                R.drawable.wifi_4
        };
    }

    private final ToneGenerator toneGenerator;
    private long lastToneTime;
    private static final long TONE_DELAY = 300L;

    private float warnAmount;
    private long lastWarn;
    private static final long WARN_DELAY = 100L;
    private static final float WARN_AMOUNT_INCR = 0.02f;
    private static final float WARN_AMOUNT_ATTEN = 0.75f;
    private long lastWarnTime;
    public static final float DANGER_WARN_AMOUNT = 0.3f;

    private final Timer timer;
    boolean flag = true;
    public HUDFragment(){
        toneGenerator = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(view != null && view.getHeight() > 0) {
                    double dis = getControlApp().getRobotController().distanceProvider.getData();
                    if (dis <= 0.2 && dis >= 0) {
                        warn();
                        if(flag){
                            Toast.makeText(getControlApp(),"间距过近",Toast.LENGTH_LONG).show();
                            flag = false;
                        }
                    }
                }
            }
        },0,20);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        if(view == null){
            view = inflater.inflate(R.layout.fragment_hud, container, false);

            speedView = (TextView) view.findViewById(R.id.hud_speed);
            turnrateView = (TextView) view.findViewById(R.id.hud_turnrate);

            posxView = (TextView) view.findViewById(R.id.hud_pos_x);
            posyView = (TextView) view.findViewById(R.id.hud_pos_y);

            wifiStrengthView = (ImageView) view.findViewById(R.id.hud_wifi_strength);

            updateUI(0.0,0.0);
         }

        wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        new Thread(UPDATER).start();

        return view;
    }

    @Override
    public void onDestroy(){
        timer.cancel();
        super.onDestroy();
        UPDATER.kill();
        toneGenerator.stopTone();
        toneGenerator.release();
    }

    @Override
    public void onNewMessage(Odometry odometry) {
        updateUI(odometry.getTwist().getTwist().getLinear().getX(),
                odometry.getTwist().getTwist().getAngular().getZ());
    }

    public void updateUI(final double speed, final double turnrate){
        if(!isDetached()){
            lastSpeed = speed;
            lastTurnrate = turnrate;
            view.post(UPDATE_UI_RUNNABLE);
        }
    }

    private class Updater implements Runnable{
        private boolean alive;
        private static final long SLEEP = 1000L;

        @Override
        public void run(){
            alive = true;
            int rssi, temp;
            while(alive){
                rssi = wifiManager.getConnectionInfo().getRssi();
                temp = lastWifiImage;
                lastWifiImage = WifiManager.calculateSignalLevel(rssi,5);
                if(temp != lastWifiImage)
                    view.post(UPDATE_UI_RUNNABLE);
                try{
                    Thread.sleep(SLEEP);
                }catch (InterruptedException e){
                    //ignore
                }
            }
        }
        public void kill(){alive = false;}
    }

    private class UpdateUIRunnable implements Runnable
    {
        @Override
        public void run()
        {
            if(isDetached())
                return;
            try{
                double speed = (int) (lastSpeed * 100.0) / 100.0;
                double turnrate = (int) (Math.toDegrees(lastTurnrate) * 100.0) / 100.0;

                if(speedView != null){
                    speedView.setText(String.format((String) getText(R.string.speed_string), speed));
                }

                if(turnrateView != null){
                    turnrateView.setText(String.format((String) getText(R.string.turnrate_string),turnrate));
                }

                if(posxView != null){
                    double posx = ((ControlApp)getActivity()).getRobotController().poseProvider.getPosX();
                    posxView.setText("X= "+String.format((String) getText(R.string.pos_string), posx));
                }

                if(posyView != null){
                    double posy = ((ControlApp)getActivity()).getRobotController().poseProvider.getPosY();
                    posyView.setText("Y= "+String.format((String) getText(R.string.pos_string), posy));
                }

                if(wifiStrengthView != null){
                    wifiStrengthView.setImageResource(WIFI_ICONS[lastWifiImage]);
                }

                if (warnAmount > 0.0f)
                {
                    view.setBackgroundColor(getBackgroundColor());
                    if(System.currentTimeMillis() - lastWarn > WARN_DELAY){
                        warnAmount *= WARN_AMOUNT_ATTEN;

                        if(warnAmount < 0.05f)
                            warnAmount = 0.0f;
                    }
                }
            }catch (IllegalStateException e){
                //Ignore
            }
        }
    }

    public void warn(){

        if(System.currentTimeMillis() - lastWarnTime > WARN_DELAY){
            lastWarnTime = System.currentTimeMillis();

            warnAmount = Math.min(1.0f, warnAmount + WARN_AMOUNT_INCR);
            lastWarn = System.currentTimeMillis();

            if(warnAmount > DANGER_WARN_AMOUNT && lastWarn - lastToneTime > TONE_DELAY
                    && RobotController.getSpeed() > 0.01){
                lastToneTime = lastWarn;
                toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, (int) TONE_DELAY / 2);
            }
        }
    }

    private int getBackgroundColor()
    {
        final float p = (((System.currentTimeMillis() >> 7) & 1) == 0) ? warnAmount: 0.0f;
        final float q = 1.0f - p;

        return Color.argb(0xFF, (int)(p * 0xFF + q * 0xF0), (int)(q * 0xF0), (int)(q * 0xF0));
    }
}
