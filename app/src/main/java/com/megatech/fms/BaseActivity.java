package com.megatech.fms;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.StrictMode;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.megatech.fms.model.UserInfo;


public class BaseActivity extends AppCompatActivity {
    protected FMSApplication currentApp;
    protected UserInfo currentUser;
    protected static final String TAG = BaseActivity.class.getName();


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //if (checkStoragePermission())
        //    checkUpdate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        currentApp = (FMSApplication) this.getApplication();
        currentUser = currentApp.getUser();


    }

    @Override
    public void onBackPressed() {
        return;
    }

    private void subscribeFCMTopic() {
//        FirebaseMessaging.getInstance().subscribeToTopic("fms")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        task.notify();
//                    }
//                });
//        currentApp.setFCMSubscribed(true);
    }

    public static boolean isAppWentToBg = false;

    public static boolean isWindowFocused = false;

    public static boolean isMenuOpened = false;

    public static boolean isBackPressed = false;

    public static boolean isFrombg = false;




}
