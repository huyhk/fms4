package com.megatech.fms;

import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.data.AppDatabase;
import com.megatech.fms.data.DataRepository;
import com.megatech.fms.enums.INVOICE_TYPE;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.Logger;
import com.megatech.fms.model.InvoiceFormModel;
import com.megatech.fms.model.InvoiceModel;
import com.megatech.fms.model.ShiftModel;
import com.megatech.fms.model.TruckModel;
import com.megatech.fms.model.UserInfo;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class FMSApplication extends Application implements LifecycleObserver {

    private static FMSApplication cApp;
    @Override
    public void onCreate() {
        super.onCreate();
        checkDatabase();
        cApp = this;
    }

    private void checkDatabase() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        int dbVersion = preferences.getInt("DB_VERSION",0);

        if (dbVersion < BuildConfig.DB_VERSION)
        {
            deleteDatabase(BuildConfig.DB_FILE);
            clearSetting();
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("DB_VERSION",BuildConfig.DB_VERSION);
        editor.apply();
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
        //Logger.appendLog("FMS", "Foreground");

    }
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onEnterBackground() {
        //Logger.appendLog("FMS", "Background");

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
        int build12 = preferences.getInt("BUILD", 0);
        long currentTime = new Date().getTime();

        boolean isLogged = token != null && build12>=12  && (currentTime - loginTime) / 1000 / 60 < 60*12;
        if (isLogged)
        {
            FirebaseCrashlytics.getInstance().setUserId( preferences.getString("USER_NAME",null));
        }
        return  isLogged;

    }
    private String printerAddress;
    public String getPrinterAddress() {
        //final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        //printerAddress = preferences.getString("PRINTER_ADDRESS","192.168.1.25");
        //return printerAddress;
        return getSetting().getPrinterIP();
    }


    public boolean isFirstUse() {

        return getTruckNo() == null || getTruckNo().equals("");
    }

    public void clearSetting()
    {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("FIRST_USER");
        editor.remove("IP");
        editor.remove("TRUCK_NO");
        editor.remove("PRINTER_ADDRESS");
        editor.remove("SETTING");
        editor.commit();
        setting = null;
    }
    public void clearTruckInfo()
    {

        TruckModel  settingModel = getSetting();
        settingModel.setTruckId(0);
        settingModel.setTruckNo("");
        saveSetting(settingModel);
    }
    public void saveSetting(TruckModel settingModel)
    {
        saveSetting(settingModel, true);
    }
    public void saveSetting(TruckModel settingModel, boolean post)
    {
        setting = settingModel;
        settingModel.setAppVersion(getAppVer());
        if (post) {
            HttpClient client = new HttpClient();
            TruckModel newModel = client.postTruck(settingModel);
            if (newModel != null)
                settingModel.setId(newModel.getId());
        }
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("FIRST_USER",false);
        editor.putString("IP",settingModel.getDeviceIP());
        editor.putString("TRUCK_NO",settingModel.getTruckNo());
        editor.putString("PRINTER_ADDRESS",printerAddress);
        editor.putString("SETTING",settingModel.toJson());
        editor.commit();



    }
    private TruckModel setting;
    public TruckModel getSetting()
    {
        if (setting == null) {
            final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
            String json = preferences.getString("SETTING", "");
            setting = TruckModel.fromJson(json);
            FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
            crashlytics.setCustomKey("Tablet-Id",setting.getTabletSerial());
            crashlytics.setCustomKey("Truck-No",setting.getTruckNo());
        }
        return  setting;
    }
    public String getTruckNo() {
        return getSetting().getTruckNo();
    }
    public int getTruckId() {
        return getSetting().getTruckId();
    }

    public String getAppVer()
    {
        return BuildConfig.VERSION_NAME;
    }
    public float getCurrentAmount() {
//        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
//        return preferences.getFloat("CURRENT_AMOUNT",0);
        return getSetting().getCurrentAmount();
    }

    //Set Current Amount and post to database
    public void setCurrentAmount(float currentAmount) {

        TruckModel setting = getSetting();
        setting.setCurrentAmount(currentAmount);
        saveSetting(setting, false);

    }
    public void addCurrentAmount(float newAmount) {

        TruckModel setting = getSetting();
        setting.setCurrentAmount(setting.getCurrentAmount() + newAmount);
        saveSetting(setting, false);

    }
    public void initCurrentAmount(float currentAmount) {
//        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putFloat("CURRENT_AMOUNT", currentAmount);
//        editor.commit();


    }
    public String getQCNo() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        return preferences.getString("QC_NO","");
    }

    public void setInventory(final float addedAmount, String qcNo) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        float   currentAmount = getSetting().getCurrentAmount();
        SharedPreferences.Editor editor = preferences.edit();
        //editor.putFloat("CURRENT_AMOUNT", currentAmount + addedAmount);
        editor.putString("QC_NO",qcNo);
        editor.apply();
        setCurrentAmount( currentAmount + addedAmount);


    }

    public void saveShift(ShiftModel model) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        float currentAmount = getSetting().getCurrentAmount();
        SharedPreferences.Editor editor = preferences.edit();
        //editor.putFloat("CURRENT_AMOUNT", currentAmount + addedAmount);
        editor.putString("SHIFT", model.toJson());
        editor.commit();
    }

    public ShiftModel getShift() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        String shiftJson = preferences.getString("SHIFT", "");
        ShiftModel model = ShiftModel.fromJson(shiftJson);
        return model;
    }

    public void saveCurrentRefuel(int id, int localId) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("REFUEL_ID", id);
        editor.putInt("REFUEL_LOCAL_ID", localId);
        editor.apply();
    }

    public void clearCurrentRefuel() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("REFUEL_ID");
        editor.remove("REFUEL_LOCAL_ID");
        editor.apply();
    }

    public int getCurrentRefuel(boolean local) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        return preferences.getInt(local ? "REFUEL_LOCAL_ID" : "REFUEL_ID", 0);
    }

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public InvoiceFormModel[] getInvoiceForms() {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        int default_form_invoice = preferences.getInt("DEFAULT_FORM_INVOICE",0);
        int default_form_bill = preferences.getInt("DEFAULT_FORM_BILL",0);
        String json = preferences.getString("INVOICE_FORM_DATA","");
        if (json!=null)
        {
            InvoiceFormModel[] forms = gson.fromJson(json,InvoiceFormModel[].class);
            for(InvoiceFormModel item: forms)
            {
                if (item.getId() == default_form_invoice && item.getPrintTemplate() == INVOICE_TYPE.INVOICE)
                    item.setLocalDefault(true);

                if (item.getId() == default_form_bill && item.getPrintTemplate() == INVOICE_TYPE.BILL)
                    item.setLocalDefault(true);


            }
            return  forms;
        }

        return null;
    }

    public void saveInvoiceForms(InvoiceFormModel[] invoiceFormModels) {
        final SharedPreferences preferences = getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        String json = gson.toJson(invoiceFormModels);

        editor.putString("INVOICE_FORM_DATA", json);
        editor.commit();
    }
}

