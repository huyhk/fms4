package com.megatech.fms;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_COMMAND;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_DEVICE_CONNECTION_STATE;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.model.LCRDataModel;
import com.megatech.fms.model.TruckModel;

import java.lang.ref.WeakReference;
import java.util.List;

//import static com.megatech.fms.BuildConfig.IMEI_LIST;

public class SettingActivity extends UserBaseActivity {

    private Context ctx = this;

    private  LCRReader reader;

    private TruckModel settingModel;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        if (!currentApp.isFirstUse())
            finish();
        settingModel = currentApp.getSetting();


        final Button btnTest = findViewById(R.id.btnTest);
        final Button btnSave = findViewById(R.id.btnUpdate);
        final EditText txtIp = findViewById(R.id.txtIP);
        final EditText txtPrinter = findViewById(R.id.txtPrinter);
        final EditText txtIMEI = findViewById(R.id.txtIMEI);


        new LoadTrucksAsync(this).execute();

        txtIp.setText(settingModel.getDeviceIP());
        txtPrinter.setText(settingModel.getPrinterIP());

        getDeviceIMEI();

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setEnabled(false);
                btnSave.setEnabled(false);
                btnTest.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                String ip = txtIp.getText().toString();
                reader =  new LCRReader(ctx, ip);
                reader.doConnectDevice();
                reader.setConnectionListener(new LCRReader.LCRConnectionListener() {
                    @Override
                    public void onConnected() {

                        reader.requestSerial();

                    }

                    @Override
                    public void onError() {
                        v.setEnabled(true);
                        btnSave.setEnabled(true);
                        btnTest.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_error, getTheme()), null, null, null);
                        reader.destroy();
                    }

                    @Override
                    public void onDeviceAdded(boolean failed) {

                    }

                    @Override
                    public void onDisconnected() {

                    }

                    @Override
                    public void onCommandError(LCR_COMMAND command) {

                    }

                    @Override
                    public void onConnectionStateChange(LCR_DEVICE_CONNECTION_STATE state) {

                    }
                });

                reader.setFieldDataListener(new LCRReader.LCRDataListener() {
                    @Override
                    public void onDataChanged(LCRDataModel dataModel, LCRReader.FIELD_CHANGE field_change) {
                        v.setEnabled(true);
                        btnSave.setEnabled(true);
                        if (field_change == LCRReader.FIELD_CHANGE.SERIAL) {
                            settingModel.setDeviceSerial(dataModel.getSerialId());
                            reader.destroy();
                            btnTest.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_check, getTheme()), null, null, null);
                        }
                    }

                    @Override
                    public void onErrorMessage(String errorMsg) {

                    }

                    @Override
                    public void onFieldAddSucess(String field_name) {

                    }
                });
            }
        });
        ProgressBar loading_bar = findViewById(R.id.loading_bar);

        btnSave.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                loading_bar.setVisibility(View.VISIBLE);
                String ip = txtIp.getText().toString();

                String printerAddress = txtPrinter.getText().toString();

                settingModel.setDeviceIP(ip);
                settingModel.setPrinterIP(printerAddress);
                EditText editText = findViewById(R.id.txtCurrentAmount);
                float currentAmount = Float.parseFloat(editText.getText().toString());
                settingModel.setCurrentAmount(currentAmount);
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
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_READ_PHONE_STATE);
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
        //List<String> imeilist = (new HttpClient()).getIMEIList();// Arrays.asList(getString(R.string.imei_list).split(","));
        //String currentIMEI = settingModel.getTabletSerial();
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

    public static class LoadTrucksAsync extends AsyncTask<Void, Void, List<TruckModel>>
    {
        private WeakReference<SettingActivity> activityWeakReference;

        public  LoadTrucksAsync(SettingActivity ctx)
        {
                activityWeakReference = new WeakReference<>(ctx);
        }
        @Override
        protected List<TruckModel> doInBackground(Void... voids) {

            return DataHelper.getTrucks();
        }

        @Override
        protected void onPostExecute(List<TruckModel> trucks) {
            activityWeakReference.get().populateTrucks(trucks);
        }
    }

    private void populateTrucks(List<TruckModel> trucks) {

        ArrayAdapter<TruckModel> spinnerAdapter = new ArrayAdapter<TruckModel>(this, R.layout.support_simple_spinner_dropdown_item, trucks);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_list_item_single_choice);

        final Spinner truckSpinner = findViewById(R.id.spinner_truck);
        truckSpinner.setAdapter(spinnerAdapter);
        truckSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TruckModel model = (TruckModel) parent.getItemAtPosition(position);
                if (model != null) {
                    settingModel.setTruckNo(model.getTruckNo());
                    settingModel.setId(model.getId());
                    settingModel.setCapacity(model.getCapacity());
                    ((EditText) findViewById(R.id.txtCurrentAmount)).setText(String.format("%.0f", model.getCurrentAmount()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if (trucks.size() > 0) {
            TruckModel truck = (TruckModel) truckSpinner.getItemAtPosition(0);
            if (truck != null) {
                String truckNo = truck.toString();// txtTruckNo.getText().toString();
                settingModel.setCode(truckNo);
                settingModel.setId(truck.getId());
            }
        }
    }
}
