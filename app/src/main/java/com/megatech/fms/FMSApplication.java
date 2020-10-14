package com.megatech.fms;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.megatech.fms.data.AppDatabase;
import com.megatech.fms.data.DataRepository;
import com.megatech.fms.model.TruckModel;
import com.megatech.fms.model.UserInfo;
import com.megatech.fms.helpers.HttpClient;

import java.util.Date;

public class FMSApplication extends Application implements LifecycleObserver {

    private static FMSApplication cApp;
    @Override
    public void onCreate() {
        super.onCreate();
        cApp = this;
    }

    public AppDatabase getDatabase() {
        return AppDatabase.getInstance(this);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
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
        //if (BuildConfig.DEBUG)
        //    return "viennam.ddns.net";
        return getSetting().getDeviceIP();
    }

    public void setDeviceIP(String deviceIP) {
        this.deviceIP = deviceIP;
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("IP",deviceIP);
        editor.apply();

    }

    public void logout() {
        UserInfo.logout(this);
    }

    public boolean isLoggedin()
    {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        String token = preferences.getString("TOKEN",null);
        long loginTime = preferences.getLong("LOGIN_TIME", 0);
        long currentTime = new Date().getTime();

        return token != null && (currentTime - loginTime) / 1000 / 60 < 30;

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
        editor.apply();

    }

    public boolean isFirstUse() {


        return getTruckNo() == null || getTruckNo().equals("");
    }

    public void saveSetting(TruckModel settingModel)
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

    public TruckModel getSetting()
    {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        String json = preferences.getString("SETTING","");
        return TruckModel.fromJson(json);
    }
    public String getTruckNo() {
        return getSetting().getTruckNo();
    }

    public float getCurrentAmount() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        return preferences.getFloat("CURRENT_AMOUNT",0);
    }

    public void setCurrentAmount(float currentAmount) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("CURRENT_AMOUNT", currentAmount);
        editor.apply();
        HttpClient client = new HttpClient();
        client.updateTruckAmount(getTruckNo(), currentAmount);

    }

    public String getQCNo() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        return preferences.getString("QC_NO","");
    }

    public void setInventory(final float currentAmount, String qcNo) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat("CURRENT_AMOUNT", currentAmount);
        editor.putString("QC_NO",qcNo);
        editor.apply();
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient client = new HttpClient();
                client.updateTruckAmount(getTruckNo(), currentAmount);
            }
        }).start();


    }

}

