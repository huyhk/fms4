package com.megatech.fms;

import android.Manifest;
import android.app.Application;
import android.app.DownloadManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.gson.Gson;
import com.megatech.fms.model.SettingModel;
import com.megatech.fms.model.UserInfo;
import com.megatech.fms.helpers.HttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import static com.megatech.fms.BuildConfig.API_BASE_URL;
import static com.megatech.fms.BuildConfig.DEBUG;

public class FMSApplication extends Application implements LifecycleObserver {

    private static FMSApplication cApp;
    @Override
    public void onCreate() {
        super.onCreate();
        cApp = this;

    }



    public static FMSApplication getApplication()
    {
        return cApp;

    }
    ///////////////////////////////////////////////
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onEnterForeground() {
        Log.d("AppController", "Foreground");

    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        Log.d("AppController", "Background");

    }
///////////////////////////////////////////////


    public UserInfo getUser()
    {
        return UserInfo.fromSharedPreferences(this);
    }

    private String deviceIP;

    public String getDeviceIP() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        deviceIP = preferences.getString("IP","192.168.1.30");
        return deviceIP;
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("IP",deviceIP);
        editor.commit();

    }

    public void logout() {
        UserInfo.logout(this);
    }

    public boolean isLoggedin()
    {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        String token = preferences.getString("TOKEN",null);
        return token!=null;

    }
    private String printerAddress;
    public String getPrinterAddress() {
        //final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        //printerAddress = preferences.getString("PRINTER_ADDRESS","192.168.1.25");
        //return printerAddress;
        return getSetting().getPrinterIP();
    }
    private boolean fcmSubscribed;
    public boolean isFCMSubscribed() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        fcmSubscribed = preferences.getBoolean("FCM_SUBSCRIBED",false);
        return fcmSubscribed;
    }
    public void setFCMSubscribed(boolean val)
    {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("FCM_SUBSCRIBED",val);
        editor.commit();

    }

    public boolean isFirstUse() {


        return getTruckNo() == "";
    }

    public  void saveSetting(SettingModel settingModel)
    {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("FIRST_USER",false);
        editor.putString("IP",settingModel.getDeviceIP());
        editor.putString("TRUCK_NO",settingModel.getTruckNo());
        editor.putString("PRINTER_ADDRESS",printerAddress);
        editor.putString("SETTING",settingModel.toJson());
        editor.commit();
    }

    public SettingModel getSetting()
    {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        String json = preferences.getString("SETTING","");
        return  SettingModel.fromJson(json);
    }
    public String getTruckNo() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        return preferences.getString("TRUCK_NO","");
    }

    public float getCurrentAmount() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        return preferences.getFloat("CURRENT_AMOUNT",0);
    }

    public void setCurrentAmount(float currentAmount) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("CURRENT_AMOUNT",(float) currentAmount);
        editor.commit();
        HttpClient client = new HttpClient();
        client.updateTruckAmount(getTruckNo(), currentAmount);

    }

    public String getQCNo() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        return preferences.getString("QC_NO","");
    }

    public void setInventory(float currentAmount, String qcNo) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("CURRENT_AMOUNT",(float) currentAmount);
        editor.putString("QC_NO",qcNo);
        editor.commit();
        HttpClient client = new HttpClient();
        client.updateTruckAmount(getTruckNo(), currentAmount);

    }

}

