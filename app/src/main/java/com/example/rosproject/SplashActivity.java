package com.example.rosproject;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

public class SplashActivity extends Activity {

    private static final String TAG = "SplashActivity";

    private final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,};

    private AlertDialog dialog;

    private static final int OVERLAY_REQUEST_CODE = 0xABCD1234;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //checkDrawOverlayPermission();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            int cnt = 0;
            for (String permission : permissions) {
                int res = ContextCompat.checkSelfPermission(this, permission);
                if (res != PackageManager.PERMISSION_GRANTED) {
                    cnt++;
                    //showDialogTipUserRequestPermission();
                }
            }
            if(cnt == 0)
                goToNextActivity(2000L);
            else{
                ActivityCompat.requestPermissions(this, permissions, 100);
            }
        }else{
            goToNextActivity(2000L);
        }
    }

    /*
    private void showDialogTipUserRequestPermission(){
        new AlertDialog.Builder(this)
                .setTitle("存储权限不可用")
                .setMessage("由于车辆交互需要存储空间，存储车辆运动信息。")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startRequestPermission();
                        goToNextActivity(2000L);
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }
    */

    private void startRequestPermission(){
        ActivityCompat.requestPermissions(this,permissions,100);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean hasPermissionDismiss = false;
        if(requestCode == 100){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                for (int grantResult : grantResults) {
                    if (grantResult != PackageManager.PERMISSION_GRANTED) {
                        hasPermissionDismiss = true;
                        break;
                    }
                }
                if(hasPermissionDismiss){
                    showPermissionDialog();
                }else{
                    goToNextActivity(2000L);
                }
            }
        }
    }

    /**
     *  6.不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;


    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();

                            Uri packageURI = Uri.parse("package:" + getPackageName());
                            Intent intent = new Intent(Settings.
                                    ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                            SplashActivity.this.finish();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

    /*
    private void showDialogTipUserGoToAppSettting(){
        dialog = new AlertDialog.Builder(this)
                .setTitle("存储权限不可用")
                .setMessage("请在-应用设置-权限-中，允许车辆交互使用存储权限来保存车辆数据")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                }).setCancelable(false).show();
    }



    private void goToAppSetting(){
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package",getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 123);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                if(i!= PackageManager.PERMISSION_GRANTED){
                    showDialogTipUserGoToAppSettting();
                }else{
                    if(dialog != null && dialog.isShowing()){
                        dialog.dismiss();
                    }
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    */

    @Override
    public void onResume(){
        super.onResume();
        //checkDrawOverlayPermission();
    }


    /*
    public void checkDrawOverlayPermission(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(!Settings.canDrawOverlays(this)){
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, OVERLAY_REQUEST_CODE);
            }else{
                goToNextActivity(2000L);
            }
        }else{
            goToNextActivity(2000L);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == OVERLAY_REQUEST_CODE){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                if(!Settings.canDrawOverlays(this)){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Permission Required!").setMessage("The app needs this permission" + "to run, it will now be closed.")
                            .create().show();
                    finish();
                }else{
                    goToNextActivity(100L);
                }
            }else{
                goToNextActivity(100L);
            }
        }
    }
    */

    private void goToNextActivity(long delay){
        Intent intent = new Intent(this, RobotChooser.class);
        try{
            Thread.sleep(delay,0);
        }catch (InterruptedException e){
            Log.e(TAG,"",e);
        }
        startActivity(intent);
        finish();
    }
}
