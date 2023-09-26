package com.example.rosproject.Views;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.rosproject.ControlApp;
import com.example.rosproject.Core.ControlMode;
import com.example.rosproject.R;

import org.ros.message.MessageListener;

import java.util.Timer;
import java.util.TimerTask;

import nav_msgs.Odometry;

public class JoystickView extends RelativeLayout implements AnimationListener,
        MessageListener<nav_msgs.Odometry> {

    private static final String TAG = "JoystickView";

    private static final float BOX_TO_CIRCLE_RATIO = 1.363636F;
    private float magnetTheta = 10.0F;
    private static final float ORIENTATION_TACK_FADE_RANGE = 40.0F;
    private static final long TURN_IN_PLACE_CONFIRMATION_DELAY = 200L;
    private static final float FLOAT_EPSILON = 0.001F;
    private static final float THUMB_DIVET_RADIUS = 16.5F;
    private static final float POST_LOCK_MAGNET_THETA = 20.0F;
    private static final int INVALID_POINTER_ID = -1;

    private RelativeLayout mainLayout;
    private ImageView intensity;
    private ImageView thumbDivet;
    private ImageView lastVelocityDivet;
    private ImageView[] orientationWidget;
    private TextView magnitudeText;
    private float contactTheta;
    private float normalizedMagnitude;
    private float contactRadius;
    private float deadZoneRatio = Float.NaN;
    private float joystickRadius = Float.NaN;
    private float parentSize = Float.NaN;
    private float normalizingMultiplier;
    private ImageView currentRotationRange;
    private ImageView previousRotationRange;
    private volatile boolean turnInPlaceMode;
    private float turnInPlaceStartTheta = Float.NaN;
    private float rightTurnOffset;
    private volatile float currentOrientation;
    private int pointerId = INVALID_POINTER_ID;
    private Point contactUpLocation;
    private boolean previousVelocityMode;
    private boolean magnetizedXAxis;
    private boolean holonomic;

    private ControlMode controlMode = ControlMode.Joystick;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float[] tiltOffset = null;
    private boolean accelContactUp;

    private static final float MAX_TILT_ANGLE = 45.0f;
    private static final float MIN_TILT_AMOUNT = 0.15f;
    private static final float TO_DEGREES = 57.29578f;

    private final SensorEventListener ACCEL_LISTENER = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] vals = event.values;

            Display display = ((WindowManager)JoystickView.this.getContext().getSystemService(
                    Context.WINDOW_SERVICE)).getDefaultDisplay();

            int rotation = display.getRotation();

            if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
                float tmp = vals[0];
                vals[0] = vals[1];
                vals[1] = -tmp;

                if(rotation == Surface.ROTATION_180){
                    vals[0] = -vals[0];
                    vals[1] = -vals[1];
                }
            }else if(rotation == Surface.ROTATION_270){
                vals[0] = -vals[0];
                vals[1] = -vals[1];
            }

            for (int i = 0; i < vals.length; ++i)
                vals[i] /= SensorManager.GRAVITY_EARTH;

            float mag = (float) Math.sqrt((vals[0] * vals[0]) + (vals[1] * vals[1]) + (vals[2] * vals[2]));

            // Calculate the tilt amount
            float tiltX = (float) Math.round(Math.asin(vals[0] / mag) * TO_DEGREES);
            float tiltY = (float) Math.round(Math.asin(vals[1] / mag) * TO_DEGREES);

            if (tiltOffset == null) {
                tiltOffset = new float[]{tiltX, tiltY};
            } else {
                tiltX -= tiltOffset[0];
                tiltY -= tiltOffset[1];

                if (tiltX < -MAX_TILT_ANGLE) tiltX = -MAX_TILT_ANGLE;
                if (tiltX > MAX_TILT_ANGLE) tiltX = MAX_TILT_ANGLE;
                if (tiltY < -MAX_TILT_ANGLE) tiltY = -MAX_TILT_ANGLE;
                if (tiltY > MAX_TILT_ANGLE) tiltY = MAX_TILT_ANGLE;

                tiltX *= joystickRadius / MAX_TILT_ANGLE;
                tiltY *= joystickRadius / MAX_TILT_ANGLE;

                if (Math.abs(tiltX) < MIN_TILT_AMOUNT * joystickRadius) tiltX = 0.0f;
                if (Math.abs(tiltY) < MIN_TILT_AMOUNT * joystickRadius) tiltY = 0.0f;

                // Move the joystick
                if (tiltX != 0f || tiltY != 0f) {
                    onContactMove(joystickRadius + tiltY, joystickRadius + tiltX);

                    if (accelContactUp) {
                        accelContactUp = false;
                        onContactDown();
                    }
                } else {
                    if (!accelContactUp) {
                        onContactMove(joystickRadius, joystickRadius);
                    }

                    accelContactUp = true;
                    onContactUp();
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public JoystickView(Context context){
        super(context);
        init(context);
    }

    public JoystickView(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }

    public JoystickView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context){
        initVirtualJoystick(context);
        try{
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }catch (UnsupportedOperationException e){
            Log.w(TAG, "No tilt control");
        }
    }

    public void controlSchemeChanged()
    {
        Log.d(TAG,"Control Scheme Changed");
        if(accelerometer != null){
            if(controlMode == ControlMode.Tilt){
                tiltOffset = null;
                sensorManager.registerListener(ACCEL_LISTENER, accelerometer, SensorManager.SENSOR_DELAY_GAME);
                onContactDown();
            }else{
                sensorManager.unregisterListener(ACCEL_LISTENER);
                onContactUp();
            }
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {
        contactRadius = 0f;
        normalizedMagnitude = 0f;
        updateMagnitudeText();
    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onNewMessage(Odometry odometry) {
        double heading;
        double w = odometry.getPose().getPose().getOrientation().getW();
        double x = odometry.getPose().getPose().getOrientation().getX();
        double y = odometry.getPose().getPose().getOrientation().getY();
        double z = odometry.getPose().getPose().getOrientation().getZ();

        heading = Math.atan2(2 * y * w - 2 * x * z, x * x - y * y - z * z + w * w) * 180 / Math.PI;
        currentOrientation = (float) -heading;
        if(turnInPlaceMode){
            post(new Runnable() {
                @Override
                public void run() {
                    updateTurnInPlaceRotation();
                }
            });
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        if(controlMode == ControlMode.Tilt){
            tiltOffset = null;
            return true;
        }

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_MOVE:
                if(pointerId != INVALID_POINTER_ID){
                    if(previousVelocityMode){
                        if(inLastContactRange(event.getX(event.getActionIndex()),
                                event.getY(event.getActionIndex()))){
                            onContactMove(contactUpLocation.x + joystickRadius, contactUpLocation.y + joystickRadius);
                        }else{
                            previousVelocityMode = false;
                        }
                    }else {
                        onContactMove(event.getX(event.findPointerIndex(pointerId)),
                                event.getY(event.findPointerIndex(pointerId)));
                    }
                }
                break;
            case MotionEvent.ACTION_DOWN:
                pointerId = event.getPointerId(event.getActionIndex());
                onContactDown();
                if(inLastContactRange(event.getX(event.getActionIndex()),event.getY(event.getActionIndex()))){
                    previousVelocityMode = true;
                    onContactMove(contactUpLocation.x + joystickRadius, contactUpLocation.y + joystickRadius);
                }else{
                    onContactMove(event.getX(event.getActionIndex()), event.getY(event.getActionIndex()));
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                if((action & MotionEvent.ACTION_POINTER_ID_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT == pointerId){
                    onContactUp();
                }
                break;
        }
        return true;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int h){
        super.onLayout(changed, l, t, r, h);
        if(mainLayout.getWidth() != mainLayout.getHeight()){
            this.setOnTouchListener(null);
        }
        parentSize = mainLayout.getWidth();
        if(parentSize < 200 || parentSize > 400){
            this.setOnTouchListener(null);
        }

        joystickRadius = (float) mainLayout.getWidth() / 2;
        normalizingMultiplier = BOX_TO_CIRCLE_RATIO / (parentSize / 2);
        deadZoneRatio = THUMB_DIVET_RADIUS * normalizingMultiplier;
        magnitudeText.setTextSize(parentSize / 12);
    }

    private void animateIntensityCircle(float endScale){
        AnimationSet intensityCircleAnimation = new AnimationSet(true);
        intensityCircleAnimation.setInterpolator(new LinearInterpolator());
        intensityCircleAnimation.setFillAfter(true);
        RotateAnimation rotateAnimation;
        rotateAnimation = new RotateAnimation(contactTheta, contactTheta, joystickRadius, (float) getHeight() / 2);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(0);
        rotateAnimation.setFillAfter(true);
        intensityCircleAnimation.addAnimation(rotateAnimation);
        ScaleAnimation scaleAnimation;
        scaleAnimation = new ScaleAnimation(contactRadius, endScale, contactRadius, endScale, joystickRadius, (float) getHeight() / 2);
        scaleAnimation.setDuration(0);
        scaleAnimation.setFillAfter(true);
        intensityCircleAnimation.addAnimation(scaleAnimation);
        intensity.startAnimation(intensityCircleAnimation);
    }

    private void animateOrientationWidgets(){
        float deltaTheta;
        for (int i = 0; i < orientationWidget.length; i++) {
            deltaTheta = differenceBetweenAngles(i * 15, contactTheta);
            if (deltaTheta < ORIENTATION_TACK_FADE_RANGE) {
                orientationWidget[i].setAlpha(1.0f - deltaTheta / ORIENTATION_TACK_FADE_RANGE);
            } else {
                orientationWidget[i].setAlpha(0.0f);
            }
        }
    }

    private void animateIntensityCircle(float endScale, long duration){
        AnimationSet intensityCircleAnimation = new AnimationSet(true);
        intensityCircleAnimation.setInterpolator(new LinearInterpolator());
        intensityCircleAnimation.setFillAfter(true);
        intensityCircleAnimation.setAnimationListener(this);
        RotateAnimation rotateAnimation;
        rotateAnimation = new RotateAnimation(contactTheta, contactTheta, joystickRadius, (float) getHeight() / 2);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(duration);
        rotateAnimation.setFillAfter(true);
        intensityCircleAnimation.addAnimation(rotateAnimation);
        ScaleAnimation scaleAnimation;
        scaleAnimation = new ScaleAnimation(contactRadius, endScale, contactRadius, endScale, joystickRadius, (float) getHeight() / 2);
        scaleAnimation.setDuration(duration);
        scaleAnimation.setFillAfter(true);
        intensityCircleAnimation.addAnimation(scaleAnimation);
        intensity.startAnimation(intensityCircleAnimation);
    }

    private float differenceBetweenAngles(float angle0, float angle1){
        return Math.abs((angle0 + 180 - angle1) % 360 - 180);
    }

    private void endTurnInPlaceRotation(){
        turnInPlaceMode = false;
        currentRotationRange.setAlpha(0.0f);
        previousRotationRange.setAlpha(0.0f);
        intensity.setAlpha(1.0f);
    }

    private void initVirtualJoystick(Context context){
        setGravity(Gravity.CENTER);
        LayoutInflater.from(context).inflate(R.layout.virtual_joystick,this,true);
        mainLayout = (RelativeLayout) findViewById(R.id.virtual_joystick_layout);
        magnitudeText = (TextView) findViewById(R.id.magnitude);
        magnitudeText.setTextColor(0xFFFFFFFF);
        intensity = (ImageView) findViewById(R.id.intensity);
        thumbDivet = (ImageView) findViewById(R.id.thumb_divet);
        thumbDivet.setColorFilter(Color.RED);
        orientationWidget = new ImageView[24];
        orientationWidget[0] = (ImageView) findViewById(R.id.widget_0_degrees);
        orientationWidget[1] = (ImageView) findViewById(R.id.widget_15_degrees);
        orientationWidget[2] = (ImageView) findViewById(R.id.widget_30_degrees);
        orientationWidget[3] = (ImageView) findViewById(R.id.widget_45_degrees);
        orientationWidget[4] = (ImageView) findViewById(R.id.widget_60_degrees);
        orientationWidget[5] = (ImageView) findViewById(R.id.widget_75_degrees);
        orientationWidget[6] = (ImageView) findViewById(R.id.widget_90_degrees);
        orientationWidget[7] = (ImageView) findViewById(R.id.widget_105_degrees);
        orientationWidget[8] = (ImageView) findViewById(R.id.widget_120_degrees);
        orientationWidget[9] = (ImageView) findViewById(R.id.widget_135_degrees);
        orientationWidget[10] = (ImageView) findViewById(R.id.widget_150_degrees);
        orientationWidget[11] = (ImageView) findViewById(R.id.widget_165_degrees);
        orientationWidget[12] = (ImageView) findViewById(R.id.widget_180_degrees);
        orientationWidget[13] = (ImageView) findViewById(R.id.widget_195_degrees);
        orientationWidget[14] = (ImageView) findViewById(R.id.widget_210_degrees);
        orientationWidget[15] = (ImageView) findViewById(R.id.widget_225_degrees);
        orientationWidget[16] = (ImageView) findViewById(R.id.widget_240_degrees);
        orientationWidget[17] = (ImageView) findViewById(R.id.widget_255_degrees);
        orientationWidget[18] = (ImageView) findViewById(R.id.widget_270_degrees);
        orientationWidget[19] = (ImageView) findViewById(R.id.widget_285_degrees);
        orientationWidget[20] = (ImageView) findViewById(R.id.widget_300_degrees);
        orientationWidget[21] = (ImageView) findViewById(R.id.widget_315_degrees);
        orientationWidget[22] = (ImageView) findViewById(R.id.widget_330_degrees);
        orientationWidget[23] = (ImageView) findViewById(R.id.widget_345_degrees);

        for(ImageView tack : orientationWidget){
            tack.setAlpha(1.0f);
            tack.setVisibility(VISIBLE);
        }

        magnitudeText.setTranslationX((float) (40 * Math.cos((90 + contactTheta) * Math.PI / 180.0)));
        magnitudeText.setTranslationY((float) (40 * Math.sin((90 + contactTheta) * Math.PI / 180.0)));

        animateIntensityCircle(0);
        contactTheta = 0;
        animateOrientationWidgets();
        currentRotationRange = (ImageView) findViewById(R.id.top_angle_slice);
        previousRotationRange = (ImageView) findViewById(R.id.mid_angle_slice);

        currentRotationRange.setAlpha(0.0f);
        previousRotationRange.setAlpha(0.0f);
        lastVelocityDivet = (ImageView) findViewById(R.id.previous_velocity_divet);
        contactUpLocation = new Point(0, 0);
        holonomic = false;

        for (ImageView tack : orientationWidget) {
            tack.setVisibility(VISIBLE);
        }
    }

    private void onContactDown(){
        thumbDivet.setAlpha(1.0f);
        magnitudeText.setAlpha(1.0f);
        lastVelocityDivet.setAlpha(0.0f);
        for(ImageView tack : orientationWidget){
            tack.setVisibility(VISIBLE);
        }
    }

    private void onContactMove(float x, float y){
        float thumbDivetX = x - joystickRadius;
        float thumbDivetY = y - joystickRadius;

        contactTheta = (float) (Math.atan2(thumbDivetY, thumbDivetX) * 180 / Math.PI + 90);
        contactRadius = (float) Math.sqrt(thumbDivetX * thumbDivetX + thumbDivetY * thumbDivetY) * normalizingMultiplier;

        normalizedMagnitude = (contactRadius - deadZoneRatio) / (1 - deadZoneRatio);
        if(contactRadius >= 1f){
            thumbDivetX /= contactRadius;
            thumbDivetY /= contactRadius;
            normalizedMagnitude = 1f;
            contactRadius = 1f;
        }else if(contactRadius < deadZoneRatio){
            thumbDivetX = 0;
            thumbDivetY = 0;
            normalizedMagnitude = 0f;
        }

        if(!magnetizedXAxis){
            if((contactTheta + 360) % 90 < magnetTheta){
                contactTheta -= ((contactTheta + 360) % 90);
            }else if((contactTheta + 360) % 90 > (90 - magnetTheta)){
                contactTheta += (90 - ((contactTheta + 360) % 90));
            }
            if(floatCompare(contactTheta, 90) || floatCompare(contactTheta, 270)){
                magnetizedXAxis = true;
            }
        }else{
            if(differenceBetweenAngles((contactTheta + 360) % 360 , 90) < POST_LOCK_MAGNET_THETA){
                contactTheta = 90;
            }else if(differenceBetweenAngles((contactTheta + 360) % 360, 270) < POST_LOCK_MAGNET_THETA){
                contactTheta = 270;
            }else{
                magnetizedXAxis = false;
            }
        }

        animateIntensityCircle(contactRadius);
        animateOrientationWidgets();
        updateThumbDivet(thumbDivetX, thumbDivetY);
        updateMagnitudeText();

        if (holonomic) {
            publishVelocity(normalizedMagnitude * Math.cos(contactTheta * Math.PI / 180.0),
                    normalizedMagnitude * Math.sin(contactTheta * Math.PI / 180.0), 0);
        } else {
            publishVelocity(normalizedMagnitude * Math.cos(contactTheta * Math.PI / 180.0), 0,
                    normalizedMagnitude * Math.sin(contactTheta * Math.PI / 180.0));
        }

        updateTurnInPlaceMode();
    }

    private void onContactUp(){
        animateIntensityCircle(0,(long)(normalizedMagnitude * 1000));
        magnitudeText.setAlpha(0.4f);
        lastVelocityDivet.setTranslationX(thumbDivet.getTranslationX());
        lastVelocityDivet.setTranslationY(thumbDivet.getTranslationY());
        lastVelocityDivet.setAlpha(0.4f);
        contactUpLocation.x = (int) (thumbDivet.getTranslationX());
        contactUpLocation.y = (int) (thumbDivet.getTranslationY());

        updateThumbDivet(0,0);
        pointerId = INVALID_POINTER_ID;
        publishVelocity(0,0,0);
        endTurnInPlaceRotation();
        for(ImageView tack: orientationWidget){
            tack.setVisibility(INVISIBLE);
        }
    }

    private void updateTurnInPlaceMode(){
        if(!turnInPlaceMode){
            if(floatCompare(contactTheta, 270)){
                turnInPlaceMode = true;
                rightTurnOffset = 0;
            }else if(floatCompare(contactTheta, 90)){
                turnInPlaceMode = true;
                rightTurnOffset = 15;
            }else{
                return;
            }

            initiateTurnInPlace();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            if(turnInPlaceMode){
                                currentRotationRange.setAlpha(1.0f);
                                previousRotationRange.setAlpha(1.0f);
                                intensity.setAlpha(0.2f);
                            }
                        }
                    });
                    postInvalidate();
                }
            }, TURN_IN_PLACE_CONFIRMATION_DELAY);
        }else if(!(floatCompare(contactTheta, 270) || floatCompare(contactTheta, 90))){
            endTurnInPlaceRotation();
        }
    }

    private void updateTurnInPlaceRotation(){
        final float currentTheta = (currentOrientation + 360) % 360;
        float offsetTheta;
        offsetTheta = (turnInPlaceStartTheta - currentTheta + 360) % 360;
        offsetTheta = 360 - offsetTheta;
        magnitudeText.setText(String.valueOf((int) offsetTheta));
        offsetTheta = (int) (offsetTheta - (offsetTheta % 15));

        RotateAnimation rotateAnimation;
        rotateAnimation = new RotateAnimation(offsetTheta + rightTurnOffset, offsetTheta + rightTurnOffset,joystickRadius,joystickRadius);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(0);
        rotateAnimation.setFillAfter(true);
        currentRotationRange.startAnimation(rotateAnimation);
        rotateAnimation = new RotateAnimation(offsetTheta + 15, offsetTheta + 15, joystickRadius,joystickRadius);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(0);
        rotateAnimation.setFillAfter(true);
        previousRotationRange.startAnimation(rotateAnimation);
    }

    private void initiateTurnInPlace(){
        turnInPlaceStartTheta = (currentOrientation + 360) % 360;
        RotateAnimation rotateAnimation;
        rotateAnimation = new RotateAnimation(rightTurnOffset, rightTurnOffset, joystickRadius, joystickRadius);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(0);
        rotateAnimation.setFillAfter(true);
        currentRotationRange.startAnimation(rotateAnimation);
        rotateAnimation = new RotateAnimation(15,15,joystickRadius,joystickRadius);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(0);
        rotateAnimation.setFillAfter(true);
        previousRotationRange.startAnimation(rotateAnimation);
    }

    private void updateMagnitudeText(){
        if(!turnInPlaceMode){
            magnitudeText.setText(String.format(getResources().getString(R.string.percent_string),(int)(normalizedMagnitude * 100)));
            magnitudeText.setTranslationX((float) (parentSize / 4 * Math.cos((90 + contactTheta) * Math.PI / 180)));
            magnitudeText.setTranslationY((float) (parentSize / 4 * Math.sin((90 + contactTheta) * Math.PI / 180)));
        }
    }

    private void updateThumbDivet(float x, float y){
        thumbDivet.setTranslationX(-THUMB_DIVET_RADIUS);
        thumbDivet.setTranslationY(-THUMB_DIVET_RADIUS);
        thumbDivet.setRotation(contactTheta);
        thumbDivet.setTranslationX(x);
        thumbDivet.setTranslationY(y);
    }

    private void publishVelocity(double linearVelocityX, double linearVelocityY, double angularVelocityZ){
        ((ControlApp) getContext()).getRobotController().forceVelocity(linearVelocityX, linearVelocityY,
                angularVelocityZ);
    }

    private boolean floatCompare(float v1, float v2){
        return Math.abs(v1 - v2) < FLOAT_EPSILON;
    }

    private boolean inLastContactRange(float x, float y){
        return Math.sqrt((x - contactUpLocation.x - joystickRadius)
                *(x - contactUpLocation.x - joystickRadius) + (y - contactUpLocation.y - joystickRadius)
                *(y - contactUpLocation.y - joystickRadius)) < THUMB_DIVET_RADIUS;
    }

    public void setControlMode(ControlMode controlMode){
        this.controlMode = controlMode;
    }

    public boolean hasAccelerometer(){
        return accelerometer != null;
    }

    public void stop(){
        publishVelocity(0,0,0);
    }
}
