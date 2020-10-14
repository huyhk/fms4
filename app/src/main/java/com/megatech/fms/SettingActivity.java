package com.megatech.fms;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.megatech.fms.data.entity.Setting;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.TruckModel;

import java.util.List;

//import static com.megatech.fms.BuildConfig.IMEI_LIST;

public class SettingActivity extends UserBaseActivity {

    private Context ctx = this;

    private TruckModel settingModel;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (!currentApp.isFirstUse())
            finish();
        settingModel = currentApp.getSetting();


        final Button btnTest = findViewById(R.id.btnTest);
        final EditText txtIp = findViewById(R.id.txtIP);
        final EditText txtTruckNo = findViewById(R.id.txtTruckNo);
        final EditText txtPrinter = findViewById(R.id.txtPrinter);
        final EditText txtIMEI = findViewById(R.id.txtIMEI);

        List<TruckModel> trucks = (new HttpClient()).getTrucks();
        ArrayAdapter<TruckModel> spinnerAdapter = new ArrayAdapter<TruckModel>(this, R.layout.support_simple_spinner_dropdown_item, trucks);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

        final Spinner truckSpinner = findViewById(R.id.spinner_truck);
        truckSpinner.setAdapter(spinnerAdapter);

        txtIp.setText(settingModel.getDeviceIP());
        txtTruckNo.setText(settingModel.getTruckNo());
        txtPrinter.setText(settingModel.getPrinterIP());

        getDeviceIMEI();

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ip = txtIp.getText().toString();


            }
        });
        ProgressBar loading_bar = findViewById(R.id.loading_bar);
        Button btnSave = findViewById(R.id.btnUpdate);
        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                loading_bar.setVisibility(View.VISIBLE);
                String ip = txtIp.getText().toString();
                String truckNo = truckSpinner.getSelectedItem().toString();// txtTruckNo.getText().toString();
                String printerAddress = txtPrinter.getText().toString();

                settingModel.setCode(truckNo);
                settingModel.setDeviceIP(ip);
                settingModel.setPrinterIP(printerAddress);

                HttpClient client = new HttpClient();
                settingModel = client.postTruck(settingModel);

                //settingModel.setTruckId(truckId);
                if (settingModel != null)
                    currentApp.saveSetting(settingModel);

                loading_bar.setVisibility(View.GONE);

                setResult(Activity.RESULT_OK);

                finish();
                restartApp();
            }
        });

    }

    private void restartApp() {
        Intent intent = new Intent(this, StartupActivity.class);
        int mPendingIntentId = MAGICAL_NUMBER;
        PendingIntent mPendingIntent = PendingIntent.getActivity(this, mPendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    private final int MAGICAL_NUMBER = 245634;
    private final int REQUEST_READ_PHONE_STATE=1;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_PHONE_STATE) {
            assignIMEI();
        }
    }

    public void getDeviceIMEI() {

        try {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
            } else {
                //TODO

                assignIMEI();

            }
        }catch (SecurityException e)
        {

            Log.e("ERROR",e.getMessage());
        }

    }

    private void assignIMEI() {
        try {
            String deviceUniqueIdentifier = null;
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != tm) {
                deviceUniqueIdentifier = tm.getDeviceId();
            }
            if (null == deviceUniqueIdentifier || 0 == deviceUniqueIdentifier.length()) {
                deviceUniqueIdentifier = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
            }
            settingModel.setTabletSerial(deviceUniqueIdentifier);
            ((EditText)findViewById(R.id.txtIMEI)).setText(settingModel.getTabletSerial());
        }
        catch (SecurityException e)
        {

        }
        List<String> imeilist = (new HttpClient()).getIMEIList();// Arrays.asList(getString(R.string.imei_list).split(","));
        String currentIMEI = settingModel.getTabletSerial();
        /*if (imeilist !=null &&  !imeilist.contains(currentIMEI))
        {
            new AlertDialog.Builder(this).setPositiveButton("QUIT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                }
            }).setMessage(R.string.not_granted_device).setTitle(R.string.not_granted_device_title).show();

        }
        else if (imeilist == null   ) {
            new AlertDialog.Builder(this).setPositiveButton("QUIT", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                }
            }).setMessage(R.string.not_get_device_list).setTitle(R.string.not_granted_device_title).show();
        }*/
    }

    private void showMessage(String message, boolean error)
    {
        if (error)
        {
            final EditText txtIp = findViewById(R.id.txtIP);
            txtIp.setError(message);
            Log.e("Failed ",message);

        }
        else {
            Toast.makeText(this,message,Toast.LENGTH_LONG).show();

            Log.i("OK",message);
        }

    }
    private void setButtonEnable(boolean enabled)
    {
        Button btnTest = findViewById(R.id.btnTest);
        btnTest.setEnabled(enabled);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}