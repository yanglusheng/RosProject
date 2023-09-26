package com.example.rosproject;

import android.app.DialogFragment;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.rosproject.Core.DrawerItem;
import com.example.rosproject.Core.NavDrawerAdapter;
import com.example.rosproject.Core.RobotInfo;
import com.example.rosproject.Core.RobotInfoAdapter;
import com.example.rosproject.Core.RobotStorage;
import com.example.rosproject.Dialogs.AddEditRobotDialogFragment;
import com.example.rosproject.Dialogs.ConfirmDeleteDialogFragment;
import com.example.rosproject.Fragments.AboutFragmentRobotChooser;
import com.example.rosproject.Fragments.HelpFragment;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RobotChooser extends AppCompatActivity implements AddEditRobotDialogFragment.DialogListener,
        ConfirmDeleteDialogFragment.DialogListener, ListView.OnItemClickListener {

        public static final String FIRST_TIME_LAUNCH_KEY = "FIRST_TIME_LAUNCH";

        private RecyclerView mRecyclerView;
        private RecyclerView.Adapter mAdapter;

        private ShowcaseView showcaseView;
        private Toolbar mToolbar;

        private String[] mFeatureTitles;
        private DrawerLayout mDrawerLayout;
        private ListView mDrawerList;

        private ActionBarDrawerToggle mDrawerToggle;

        private Fragment fragment = null;
        private FragmentManager fragmentManager;
        private int fragmentsCreatedCounter = 0;

        private static final String TAG = "RobotChooser";

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.setContentView(R.layout.robot_chooser);
            mRecyclerView = (RecyclerView) findViewById(R.id.robot_recycler_view);

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());
            mRecyclerView.setLayoutManager(mLayoutManager);

            mToolbar = (Toolbar) findViewById(R.id.robot_chooser_toolbar);
            setSupportActionBar(mToolbar);

            RobotStorage.load(this);

            mDrawerLayout = (DrawerLayout) findViewById(R.id.profileDrawer);
            mFeatureTitles = getResources().getStringArray(R.array.chooser_titles);
            mDrawerList = (ListView) findViewById(R.id.left_drawer2);

            if(getActionBar() != null){
                getActionBar().setDisplayHomeAsUpEnabled(true);
                getActionBar().setHomeButtonEnabled(true);
            }

            mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,
                    mToolbar,R.string.drawer_open,R.string.drawer_close){
                    public void onDrawerClosed(View view){
                            invalidateOptionsMenu();
                    }

                    public void onDrawerOpened(View drawerView){
                            invalidateOptionsMenu();
                    }
            };
            mDrawerToggle.syncState();
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
            mDrawerLayout.addDrawerListener(mDrawerToggle);

            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerToggle.syncState();
                }
            });

            int[] imgRes = new int[]{
                    R.drawable.ic_action_robot,
                    R.drawable.ic_action_help,
                    R.drawable.ic_action_about
            };

            List<DrawerItem> drawerItems = new ArrayList<>();

            for(int i = 0; i < mFeatureTitles.length; i++){
                drawerItems.add(new DrawerItem(mFeatureTitles[i], imgRes[i]));
            }

            NavDrawerAdapter drawerAdapter = new NavDrawerAdapter(this,R.layout.nav_drawer_menu_item,drawerItems);
            mDrawerList.setAdapter(drawerAdapter);
            mDrawerList.setOnItemClickListener(this);

            mAdapter = new RobotInfoAdapter(this, RobotStorage.getRobots());

            mRecyclerView.setAdapter(mAdapter);

            ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

            final boolean isFirstLaunch = PreferenceManager.getDefaultSharedPreferences(this)
                    .getBoolean(FIRST_TIME_LAUNCH_KEY, true);

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                if(RobotStorage.getRobots().size() == 0 && isFirstLaunch){
                                    showcaseView = new ShowcaseView.Builder(RobotChooser.this)
                                            .setTarget(new ToolbarActionItemTarget(mToolbar, R.id.action_add_robot))
                                            .setStyle(R.style.CustomShowcaseTheme2)
                                            .hideOnTouchOutside()
                                            .blockAllTouches()
                                            .setContentTitle("添加一个智能车")
                                            .setContentText("让我们开始吧！您可以使用此按钮添加要连接到的机器人。现在尝试添加一个。")
                                            .build();
                                    setupNextTutorialMessage();
                                }
                            }catch (Exception ignore){

                            }
                        }
                    });
                }
            };
            worker.schedule(task, 1, TimeUnit.SECONDS);
        }

        private void selectItem(int position){
            Bundle args = new Bundle();
            fragmentManager = getSupportFragmentManager();
            switch (position){
                case 0:
                    fragmentsCreatedCounter = 0;

                    if(fragment != null){
                        fragmentManager.beginTransaction().remove(fragment).commit();
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }

                    mDrawerLayout.closeDrawers();
                    return;

                case 1:
                    fragment = new HelpFragment();
                    fragment.setArguments(args);
                    fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                    mRecyclerView.setVisibility(View.GONE);

                    if(fragment != null){
                        fragmentManager.beginTransaction().replace(R.id.content_frame2, fragment)
                                .commit();
                    }
                    break;

                case 2:
                    fragment = new AboutFragmentRobotChooser();
                    fragment.setArguments(args);
                    fragmentsCreatedCounter = fragmentsCreatedCounter + 1;
                    mRecyclerView.setVisibility(View.GONE);
                    if(fragment != null){
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame2, fragment)
                                .commit();
                    }
                    break;

                default:
                    break;
            }
            mDrawerList.setItemChecked(position, true);
            mDrawerLayout.closeDrawer(mDrawerList);
            setTitle(mFeatureTitles[position]);
        }


        public void setTitle(CharSequence title){
            try{
                if(getActionBar() != null)
                    getActionBar().setTitle(title);
            }catch (NullPointerException e){
                //ignore
            }
        }


        @Override
        public void onPostCreate(Bundle savedInstanceState){
            super.onPostCreate(savedInstanceState);
            mDrawerToggle.syncState();
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig){
            super.onConfigurationChanged(newConfig);
            mDrawerToggle.onConfigurationChanged(newConfig);
        }

        @Override
        public void onBackPressed(){
            if(fragmentsCreatedCounter >= 1){
                selectItem(0);
                fragmentsCreatedCounter = 0;
            }else{
                super.onBackPressed();
            }
        }

        @Override
        protected void onDestroy(){
            super.onDestroy();
            if(showcaseView != null){
                showcaseView.hide();
            }
        }

        private void setupNextTutorialMessage(){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while(mRecyclerView.getChildCount() <= 0){
                        try{
                            Thread.sleep(100);
                        }catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    View v = null;
                    for(int i = 0; i < mRecyclerView.getChildCount(); i++){
                        v = mRecyclerView.getChildAt(i);
                        if(v != null){
                            v = v.findViewById(R.id.robot_info_text);
                            if(v != null) break;
                        }
                    }

                    final View layoutView = v;
                    if(layoutView != null){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showcaseView = new ShowcaseView.Builder(RobotChooser.this)
                                        .setTarget(new ViewTarget(layoutView))
                                        .setStyle(R.style.CustomShowcaseTheme2)
                                        .hideOnTouchOutside()
                                        .blockAllTouches()
                                        .setContentTitle("连接")
                                        .setContentText("要连接到此智能车，请点按其名称")
                                        .build();

                                PreferenceManager.getDefaultSharedPreferences(RobotChooser.this)
                                        .edit()
                                        .putBoolean(FIRST_TIME_LAUNCH_KEY,true)
                                        .apply();
                            }
                        });
                    }

                }
            }).start();
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu){
            getMenuInflater().inflate(R.menu.menu_robot_chooser, menu);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
            switch (item.getItemId()){
                case R.id.action_add_robot:
                    if(fragment != null){
                        //fragmentManager.beginTransaction().remove(fragment).commit();
                        //mRecyclerView.setVisibility(View.VISIBLE);
                        selectItem(0);
                    }

                    RobotInfo.resolveRobotCount(RobotStorage.getRobots());
                    AddEditRobotDialogFragment addEditRobotDialogFragment = new AddEditRobotDialogFragment();
                    addEditRobotDialogFragment.setArguments(null);
                    addEditRobotDialogFragment.show(getFragmentManager(),"addrobotdialog");
                    return true;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        @Override
        public void onAddEditDialogPositiveClick(RobotInfo info, int position) {
            if(position >=0 && position < RobotStorage.getRobots().size()){
                updateRobot(position, info);
            }else{
                addRobot(info);
            }
        }

        @Override
        public void onAddEditDialogNegativeClick(DialogFragment dialog) {

        }

        @Override
        public void onConfirmDeleteDialogPositiveClick(int position, String name) {
            if(position >= 0 && position < RobotStorage.getRobots().size()){
                removeRobot(position);
            }
        }

        @Override
        public void onConfrimDeleteDialogNegativeClick() {

        }

        public boolean addRobot(RobotInfo info){
            RobotStorage.add(this,info);
            mAdapter.notifyItemChanged(RobotStorage.getRobots().size() - 1);
            return true;
        }

        public void updateRobot(int position, RobotInfo info){
            RobotStorage.update(this, info);
            mAdapter.notifyItemChanged(position);
        }

        public RobotInfo removeRobot(int position){
            RobotInfo removed = RobotStorage.remove(this,position);
            if(removed != null){
                mAdapter.notifyItemRemoved(position);
            }
            if(RobotStorage.getRobots().size() == 0){
                mAdapter.notifyDataSetChanged();
            }
            return removed;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
}
