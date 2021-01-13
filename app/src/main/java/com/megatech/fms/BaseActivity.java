package com.megatech.fms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;


import com.megatech.fms.data.AppDatabase;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.model.UserInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.megatech.fms.BuildConfig.API_BASE_URL;
import static com.megatech.fms.BuildConfig.DEBUG;


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
        if (!currentApp.isFCMSubscribed())
            subscribeFCMTopic();

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

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart isAppWentToBg " + isAppWentToBg);

        applicationWillEnterForeground();

        super.onStart();
    }

    private void applicationWillEnterForeground() {
        if (isAppWentToBg) {
            isAppWentToBg = false;
            //Toast.makeText(getApplicationContext(), "App is back from Background",
            //       Toast.LENGTH_SHORT).show();
            isFrombg = true;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop ");
        applicationdidenterbackground();
    }

    public void applicationdidenterbackground() {
        if (!isWindowFocused) {
            isAppWentToBg = true;

        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;

        if (isBackPressed && !hasFocus) {
            isBackPressed = false;
            isWindowFocused = true;
        }

        super.onWindowFocusChanged(hasFocus);
    }


}
