package com.example.rosproject;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.example.rosproject.Core.ControlMode;
import com.example.rosproject.Core.DrawerItem;
import com.example.rosproject.Core.NavDrawerAdapter;
import com.example.rosproject.Core.RobotController;
import com.example.rosproject.Core.RobotInfo;
import com.example.rosproject.Core.RobotStorage;
import com.example.rosproject.Fragments.AccFragment;
import com.example.rosproject.Fragments.ButtonFragment;
import com.example.rosproject.Fragments.CameraViewFragment;
import com.example.rosproject.Fragments.ChartFragment;
import com.example.rosproject.Fragments.HUDFragment;
import com.example.rosproject.Fragments.JoystickFragment;
import com.example.rosproject.Fragments.LaserScanMapFragment;
import com.example.rosproject.Fragments.OverviewFragment;
import com.example.rosproject.Fragments.RosFragment;

import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.util.ArrayList;
import java.util.List;


public class ControlApp extends RosActivity implements ListView.OnItemClickListener,
        AdapterView.OnItemSelectedListener {

    public static final String NOTIFICATION_TICKER = "ROS Control";
    public static final String NOTIFICATION_TITLE = "ROS Control";

    public static RobotInfo ROBOT_INFO;

    private String[] mFeatureTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;

    private NodeMainExecutor nodeMainExecutor;
    private NodeConfiguration nodeConfiguration;

    private HUDFragment hudFragment;
    private JoystickFragment joystickFragment;
    private ButtonFragment buttonFragment;
    private AccFragment accFragment;

    private RobotController controller;

    private Fragment fragment = null;
    FragmentManager fragmentManager;
    int fragmentsCreatedCounter = 0;

    private Spinner actionMenuSpinner;

    private int drawerIndex = 1;

    private static final String TAG = "ControlApp";

    public ControlApp(){
        super(NOTIFICATION_TICKER,NOTIFICATION_TITLE, ROBOT_INFO.getUri());
    }


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();

        //PreferenceManager.setDefaultValues(this,R.xml.prefs, false);

        if(ROBOT_INFO != null){
            ROBOT_INFO.save(editor);
        }
        editor.apply();

        this.setContentView(R.layout.main);

        mFeatureTitles = getResources().getStringArray(R.array.feature_titles);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);

        if(getActionBar() != null){
            ActionBar actionBar = getActionBar();

            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);

            LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.actionbar_dropdown_menu,null);

            actionMenuSpinner = (Spinner) v.findViewById(R.id.spinner_control_mode);

            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,R.array.motion_plans,
                    android.R.layout.simple_spinner_item);

            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            actionMenuSpinner.setAdapter(adapter);
            actionMenuSpinner.setOnItemSelectedListener(this);

            actionBar.setCustomView(v);
            actionBar.setDisplayShowCustomEnabled(true);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.drawer_open,R.string.drawer_close){
            public void onDrawerClosed(View view){
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerview){
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        int[] imgRes = new int[]{
                R.drawable.ic_action_robot,
                R.drawable.ic_action_overview,
                R.drawable.ic_action_camera,
                R.drawable.ic_action_map,
                R.drawable.ic_action_line
        };

        List<DrawerItem> drawerItems = new ArrayList<>();

        for(int i = 0; i < mFeatureTitles.length; i++){
            drawerItems.add(new DrawerItem(mFeatureTitles[i], imgRes[i]));
        }

        NavDrawerAdapter drawerAdapter = new NavDrawerAdapter(this, R.layout.nav_drawer_menu_item, drawerItems);

        mDrawerList.setAdapter(drawerAdapter);
        mDrawerList.setOnItemClickListener(this);

        controller = new RobotController(this);

        hudFragment = (HUDFragment) getFragmentManager().findFragmentById(R.id.hud_fragment);

        joystickFragment = (JoystickFragment) getFragmentManager().findFragmentById(R.id.joystick_fragment);
        buttonFragment = (ButtonFragment) getFragmentManager().findFragmentById(R.id.button_fragment);
        accFragment = (AccFragment)  getFragmentManager().findFragmentById(R.id.acc_fragment);

        if(actionMenuSpinner != null){
            actionMenuSpinner.setSelection(getControlMode().ordinal());
        }
    }

    @Override
    protected void onStop() {
        RobotStorage.update(this, ROBOT_INFO);
        if(controller != null)
            controller.stop();

        if(joystickFragment != null){
            joystickFragment.stop();
        }

        if(buttonFragment != null){
            buttonFragment.stop();
        }

        if(accFragment != null){
            accFragment.stop();
        }

        onTrimMemory(TRIM_MEMORY_BACKGROUND);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){

    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        try{
            java.net.Socket socket = new java.net.Socket(getMasterUri().getHost(), getMasterUri().getPort());
            java.net.InetAddress local_network_address = socket.getLocalAddress();
            socket.close();

            this.nodeMainExecutor = nodeMainExecutor;
            this.nodeConfiguration = NodeConfiguration.newPublic(local_network_address.getHostAddress(), getMasterUri());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    joystickFragment.invalidate();
                }
            });

            controller.initialize(nodeMainExecutor,nodeConfiguration);
            controller.addOdometryListener(hudFragment);
            controller.addOdometryListener(joystickFragment.getJoystickView());

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    View temp = mDrawerList.getChildAt(drawerIndex);
                    mDrawerList.onTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(),SystemClock.uptimeMillis(),
                            MotionEvent.ACTION_DOWN, temp.getX(), temp.getY(),0));

                    selectItem(drawerIndex);
                }
            });
        }catch (Exception e){
            Log.e(TAG,"socket error trying to get networking information from the master uri",e);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectItem(position);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setControlMode(ControlMode.values()[position]);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void selectItem(int position){
        Bundle args = new Bundle();

        if(joystickFragment != null && getControlMode().ordinal() <= ControlMode.Tilt.ordinal()){
            joystickFragment.show();
            buttonFragment.hide();
            accFragment.hide();
        }

        if(buttonFragment != null && getControlMode().ordinal() == ControlMode.ButtonKey.ordinal()){
            buttonFragment.show();
            joystickFragment.hide();
            accFragment.hide();
        }

        if(accFragment != null && getControlMode().ordinal() == ControlMode.AccKey.ordinal()){
            accFragment.show();
            joystickFragment.hide();
            buttonFragment.hide();
        }

        if(hudFragment != null){
            hudFragment.show();
        }

        if(controller != null){
            controller.initialize();
        }

        fragmentManager = getFragmentManager();
        setActionMenuEnable(true);

        switch (position){
            case 0:
                fragmentsCreatedCounter = 0;
                int count = fragmentManager.getBackStackEntryCount();
                for(int i = 0; i < count; i++){
                    fragmentManager.popBackStackImmediate();
                }
                if(controller != null){
                    controller.shutdownTopics();
                    new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params){
                            nodeMainExecutor.shutdownNodeMain(controller);
                            return null;
                        }
                    }.execute();
                }
                finish();
                return;

            case 1:
                fragment = new OverviewFragment();
                fragmentsCreatedCounter = 0;
                break;

            case 2:
                fragment = new CameraViewFragment();
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                break;

            case 3:
                fragment = new LaserScanMapFragment();
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                break;

            case 4:
                fragment = new ChartFragment();
                fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                break;
            default:
                break;
        }

        drawerIndex = position;

        try{
            ((RosFragment) fragment).initialize(nodeMainExecutor, nodeConfiguration);
        }catch (Exception e){
            //ignore
        }

        if(fragment != null){
            fragment.setArguments(args);
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        }

        mDrawerList.setItemChecked(position,true);
        mDrawerLayout.closeDrawer(mDrawerList);
        setTitle(mFeatureTitles[position]);
    }

    @Override
    public void setTitle(CharSequence title){
        try {
            //noinspection ConstantConditions
            getActionBar().setTitle(title);
        } catch (NullPointerException e) {
            // Ignore
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_joystick_control:
                setControlMode(ControlMode.Joystick);
                return true;

            case R.id.action_motion_control:
                setControlMode(ControlMode.Tilt);
                return true;

            case R.id.action_button_control:
                setControlMode(ControlMode.ButtonKey);
                return true;

            case R.id.action_acc_control:
                setControlMode(ControlMode.AccKey);
                return true;

            default:
                return mDrawerToggle.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onBackPressed(){
        if(fragmentsCreatedCounter >= 1){
            selectItem(1);
            fragmentsCreatedCounter = 0;
        }else{
            super.onBackPressed();
        }
    }

    public void onPreferencesChanged(SharedPreferences prefs){
        controller.refreshTopic();
    }

    public ControlMode getControlMode(){
        return joystickFragment.getControlMode();
    }

    public void setControlMode(ControlMode controlMode){
        if(joystickFragment.getControlMode() == controlMode)
            return;

        joystickFragment.setControlMode(controlMode);
        buttonFragment.setControlMode(controlMode);
        accFragment.setControlMode(controlMode);

        invalidateOptionsMenu();
    }


    public RobotController getRobotController(){
        return controller;
    }

    public void setActionMenuEnable(boolean enable){
        if(actionMenuSpinner != null){
            actionMenuSpinner.setEnabled(enable);
        }
    }

    public NodeMainExecutor getNodeMainExecutor(){
        return nodeMainExecutor;
    }

    public NodeConfiguration getNodeConfiguration(){
        return nodeConfiguration;
    }

}
