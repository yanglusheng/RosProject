package com.example.rosproject.Core;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rosproject.ControlApp;
import com.example.rosproject.Dialogs.AddEditRobotDialogFragment;
import com.example.rosproject.Dialogs.ConfirmDeleteDialogFragment;
import com.example.rosproject.R;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class RobotInfoAdapter extends RecyclerView.Adapter<RobotInfoAdapter.ViewHolder> {

    private final List<RobotInfo> mDataset;

    private static AppCompatActivity activity;

    public RobotInfoAdapter(AppCompatActivity activity, List<RobotInfo> dataset){
        RobotInfoAdapter.activity = activity;
        mDataset = dataset;
    }

    @Override
    public RobotInfoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.robot_info_view, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RobotInfoAdapter.ViewHolder holder, int position) {
        holder.mRobotNameTextView.setText(mDataset.get(position).getName());
        holder.mMasterUriTextView.setText(mDataset.get(position).getUri().toString());
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public TextView mRobotNameTextView;
        public TextView mMasterUriTextView;

        private final ImageButton mEditButton;
        private final ImageButton mDeleteButton;
        private final ImageView mImageView;

        public ViewHolder(View v){
            super(v);
            v.setClickable(true);
            v.setOnClickListener(this);
            mRobotNameTextView = (TextView) v.findViewById(R.id.robot_name_text_view);
            mMasterUriTextView = (TextView) v.findViewById(R.id.master_uri_text_view);

            mEditButton = (ImageButton) v.findViewById(R.id.robot_edit_button);
            mEditButton.setOnClickListener(this);

            mDeleteButton = (ImageButton) v.findViewById(R.id.robot_delete_button);
            mDeleteButton.setOnClickListener(this);

            mImageView = (ImageView) v.findViewById(R.id.robot_wifi_image);
            mImageView.setImageResource(R.drawable.wifi_0);

            Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        int position = getAdapterPosition();
                        final RobotInfo info = mDataset.get(position);
                        if (isPortOpen(info.getUri().getHost(), info.getUri().getPort(), 10000)) {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setImageResource(R.drawable.wifi_4);
                                }
                            });
                        } else {
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mImageView.setImageResource(R.drawable.wifi_0);
                                }
                            });
                        }
                        Thread.sleep(1000);
                    }catch (Exception ignore){

                    }
                }
            },1000,1500);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Bundle bundle;
            final RobotInfo info = mDataset.get(position);

            switch (v.getId()){
                case R.id.robot_edit_button:
                    AddEditRobotDialogFragment editRobotDialogFragment = new AddEditRobotDialogFragment();
                    bundle = new Bundle();
                    info.save(bundle);
                    bundle.putInt(AddEditRobotDialogFragment.POSITION_KEY,position);
                    editRobotDialogFragment.setArguments(bundle);
                    editRobotDialogFragment.show(activity.getFragmentManager(), "editrobotdialog");
                    break;

                case R.id.robot_delete_button:
                    ConfirmDeleteDialogFragment confirmDeleteDialogFragment = new ConfirmDeleteDialogFragment();
                    bundle = new Bundle();

                    bundle.putInt(ConfirmDeleteDialogFragment.POSITION_KEY,position);
                    bundle.putString(ConfirmDeleteDialogFragment.NAME_KEY, info.getName());
                    confirmDeleteDialogFragment.setArguments(bundle);

                    confirmDeleteDialogFragment.show(activity.getFragmentManager(), "deleterobotdialog");
                    break;

                default:
                    FragmentManager fragmentManager = activity.getFragmentManager();
                    ConnectionProgressDialogFragment f = new ConnectionProgressDialogFragment();
                    Bundle robotinfo = new Bundle();
                    info.save(robotinfo);

                    f.setArguments(robotinfo);
                    f.show(fragmentManager,"ConnectionProgressDialog");
                    break;
            }
        }
    }

    public static boolean isPortOpen(final String ip, final int port, final int timeout){
        try {
            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(ip, port), timeout);
            socket.close();
            return true;
        }catch (ConnectException ce){
            return false;
        }catch (Exception ex){
            return false;
        }
    }

    public static class ConnectionProgressDialogFragment extends DialogFragment{
        private static final String TAG = "ConnectionProgress";

        private final RobotInfo INFO;
        private Thread thread;

        public ConnectionProgressDialogFragment(){
            INFO = new RobotInfo();
        }

        @Override
        public void setArguments(Bundle args){
            super.setArguments(args);
            if(args != null ){
                Log.d(TAG,"success");
                INFO.load(args);
            }
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState){
            final ProgressDialog progressDialog = ProgressDialog.show(activity,"Connecting", "Connecting to"
            + INFO.getName()+"("+INFO.getUri().toString()+")",true,false);

            run();

            return progressDialog;
        }

        @Override
        public void onDestroy() {
            thread.interrupt();
            super.onDestroy();
        }

        private void run(){
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        if(!isPortOpen(INFO.getUri().getHost(), INFO.getUri().getPort(), 10000)){
                            throw new Exception("无法连接到ROS。请确保ROS正在运行并且主URI正确。");
                        }

                        final Intent intent = new Intent(activity, ControlApp.class);
                        ControlApp.ROBOT_INFO = INFO;

                        dismiss();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                activity.startActivity(intent);
                            }
                        });

                    }catch (final NetworkOnMainThreadException e){
                        dismiss();
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity,"不合法的主机URI",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }catch (InterruptedException e){
                        Log.d(TAG,"interrupted");
                    }catch (final Exception e){
                        if(ConnectionProgressDialogFragment.this.getFragmentManager() != null)
                            dismiss();

                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, e.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
            thread.start();
        }
    }
}
