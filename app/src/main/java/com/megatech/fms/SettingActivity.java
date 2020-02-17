package com.megatech.fms;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.model.SettingModel;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

//import static com.megatech.fms.BuildConfig.IMEI_LIST;

public class SettingActivity extends UserBaseActivity {

    private Context ctx = this;
    private LCRReader reader;
    private SettingModel settingModel ;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        setToolbar();
        settingModel = currentApp.getSetting();

        final Button btnTest = findViewById(R.id.btnTest);
        final EditText txtIp = findViewById(R.id.txtIP);
        final EditText txtTruckNo = findViewById(R.id.txtTruckNo);
        final EditText txtPrinter = findViewById(R.id.txtPrinter);
        final EditText txtIMEI = findViewById(R.id.txtIMEI);

        txtIp.setText(settingModel.getDeviceIP());
        txtTruckNo.setText(settingModel.getTruckNo());
        txtPrinter.setText(settingModel.getPrinterIP());

        getDeviceIMEI();

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String ip = txtIp.getText().toString();
                testConnection(ip);

            }
        });
        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String ip = txtIp.getText().toString();
                String truckNo = txtTruckNo.getText().toString();
                String printerAddress = txtPrinter.getText().toString();

                settingModel.setCode(truckNo);
                settingModel.setDeviceIP(ip);
                settingModel.setPrinterIP(printerAddress);

                currentApp.saveSetting(settingModel);

                HttpClient client = new HttpClient();
                client.getTruckAmount(truckNo);
                if (reader!=null)
                    reader.doDisconnectDevice();
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

    }
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
    };
    public void testConnection(String ip)
    {
        try {

           reader= new LCRReader(ctx, ip);

            reader.setConnectionListener(new LCRReader.LCRConnectionListener() {
                @Override
                public void onConnected() {
                    showMessage(getString(R.string.lcr_connection_ok),false);
                    setButtonEnable(true);
                    reader.doDisconnectDevice();
                }

                @Override
                public void onError() {

                }


                @Override
                public void onDeviceAdded(boolean failed) {
                    if (!failed)
                        reader.doConnectDevice();
                    else {
                        showMessage(getString(R.string.lcr_connection_error) , true);
                        setButtonEnable(true);
                    }

                }

                @Override
                public void onDisconnected() {

                }
            });
           setButtonEnable(false);


        }
        catch (Exception e)
        {
            showMessage(e.getMessage(), true);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (reader!=null)
            reader.destroy();
    }
}
