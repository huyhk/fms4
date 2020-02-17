package com.megatech.fms;

import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.databinding.ActivityRefuelDetailBinding;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.LCRDataModel;
import com.megatech.fms.model.REFUEL_ITEM_STATUS;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.helpers.PrintWorker;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RefuelDetailActivity extends UserBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refuel_detail);
        this.activity = this;
        loaddata();
        String storedIP = currentApp.getDeviceIP();
        reader = LCRReader.create(this,storedIP,10001,false );
        addListeners();
        this.model = new LCRDataModel();
        model.setUserId(currentUser.getUserId());
        deviceIsReady = reader.getConnected();

        if (deviceIsReady)
        {
           setConnectionCheckmark(CONNECTION_STATUS.OK);
        }
    }

    private void setConnectionCheckmark(CONNECTION_STATUS status) {
        switch (status) {
            case OK :
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                ((CheckedTextView) findViewById(R.id.refuel_detail_chk_connect_lcr)).setChecked(true);
                ((TextView) findViewById(R.id.lbl_connection_status)).setText(getString(R.string.lcr_connection_ok));
                ((TextView) findViewById(R.id.lbl_connection_status)).setTextColor(getResources().getColor(R.color.colorDarkGreen));
                ((CheckedTextView) findViewById(R.id.refuel_detail_chk_connect_lcr)).setCheckMarkDrawable(R.drawable.ic_checked_circle);
                break;
            case CONNECTING:
                CheckedTextView chkTxt = (CheckedTextView) findViewById(R.id.refuel_detail_chk_connect_lcr);
                chkTxt.setCheckMarkDrawable(null);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                ((TextView)findViewById(R.id.lbl_connection_status)).setText(getString(R.string.lcr_connection_connecting));
                ((TextView)findViewById(R.id.lbl_connection_status)).setTextColor(Color.BLACK);
                break;
            case ERROR:
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                ((TextView)findViewById(R.id.lbl_connection_status)).setText(getString(R.string.lcr_connection_error));
                ((TextView)findViewById(R.id.lbl_connection_status)).setTextColor(Color.RED);
                ((CheckedTextView)findViewById(R.id.refuel_detail_chk_connect_lcr)).setCheckMarkDrawable(R.drawable.ic_error);
                break;
        }

    }

    private enum CONNECTION_STATUS
    {
        OK,
        ERROR,
        CONNECTING
    }
    private RefuelItemData mItem;
    private Activity activity;
    private boolean isEditing = false;
    private void loaddata() {
        Bundle b = getIntent().getExtras();
        Integer flightId = b.getInt("REFUEL_ID",2);
        String mData = b.getString("REFUEL","");
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        mItem = gson.fromJson(mData, RefuelItemData.class);
        ActivityRefuelDetailBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_refuel_detail);
        binding.setMItem(mItem);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (mItem != null) {
            /*Button btnRefuel =  findViewById(R.id.refuelitem_detail_bntreffuel);
            btnRefuel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refuel(mItem);
                }
            });
            */
            List<AirlineModel> airlines = (new HttpClient()).getAirlines();
            ArrayAdapter<AirlineModel> spinnerAdapter = new ArrayAdapter<AirlineModel>(this,R.layout.support_simple_spinner_dropdown_item,airlines);
            spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);

            Button btnPrint = findViewById(R.id.refuelitem_detail_bntprint);
            btnPrint.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new PrintWorker(activity).printItem(mItem);
                }
            });

            Button btnEdit = findViewById(R.id.btnEdit);
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleEdit();
                }
            });

            Button btnSave = findViewById(R.id.btnSave);
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    save();
                }
            });

            if(mItem.getStatus() == REFUEL_ITEM_STATUS.DONE)
                btnEdit.setVisibility(View.GONE);
            else
                mItem.setQualityNo(currentApp.getQCNo());

            /*((TextView) findViewById(R.id.refuelitem_detail_flightCode)).setText(mItem.getFlightCode());
            ((TextView) findViewById(R.id.refuelitem_detail_aircraftCode)).setText(mItem.getAircraftCode());
            ((TextView) findViewById(R.id.refuelitem_detail_parking)).setText(mItem.getParkingLot());
            ((TextView) findViewById(R.id.refuelitem_detail_estimateAmount)).setText(String.format("%.2f",mItem.getEstimateAmount()));
            */
            ((EditText) findViewById(R.id.edtAircraft)).setText(mItem.getAircraftCode());
            /*((TextView) findViewById(R.id.refuelitem_detail_startTime)).setText(simpleDateFormat.format(mItem.getStartTime()));
            ((TextView) findViewById(R.id.refuelitem_detail_endTime)).setText(simpleDateFormat.format(mItem.getEndTime()));
            ((TextView) findViewById(R.id.refuelitem_detail_Temperature)).setText(String.format("%.2f",mItem.getManualTemperature()));
            */
            ((EditText) findViewById(R.id.edtTemperature)).setText(String.format("%.2f",mItem.getManualTemperature()));
            //((TextView) findViewById(R.id.refuelitem_detail_Density)).setText(String.format("%.2f",mItem.getDensity()));
            ((EditText) findViewById(R.id.edtDensity)).setText(String.format("%.2f",mItem.getDensity()));
            //((TextView) findViewById(R.id.refuelitem_detail_qc_no)).setText(mItem.getQualityNo());

            Spinner airline_spinner = (Spinner) findViewById(R.id.refuelitem_detail_airline_spinner);
            airline_spinner.setAdapter(spinnerAdapter);

            if (mItem.getAirlineId()>0) {
                for (int i=0;i<airline_spinner.getCount();i++)
                {
                    AirlineModel item = (AirlineModel)  airline_spinner.getItemAtPosition(i);
                    if (mItem.getAirlineId() == item.getId())
                    {
                        airline_spinner.setSelection(i);
                        ((TextView) findViewById(R.id.refuelitem_detail_airline)).setText(item.getName());
                        mItem.setAirlineId(item.getId());
                        mItem.setProductName(item.getProductName());
                        mItem.setAirlineModel(item);
                        break;
                    }
                }
            }
            airline_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    AirlineModel selected = (AirlineModel) parent.getItemAtPosition(position);
                    mItem.setAirlineId(selected.getId());
                    mItem.setPrice(selected.getPrice());
                    mItem.setProductName(selected.getProductName());
                    mItem.setAirlineModel(selected);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (mItem.getArrivalTime() != new Date(Long.MIN_VALUE))
                ((TextView) findViewById(R.id.refuelitem_detail_arrival)).setText(simpleDateFormat.format(mItem.getArrivalTime()));
            if (mItem.getDepartureTime() != new Date(Long.MIN_VALUE))
                ((TextView) findViewById(R.id.refuelitem_detail_departure)).setText(simpleDateFormat.format(mItem.getDepartureTime()));


            REFUEL_ITEM_STATUS status = mItem.getStatus();

            if (status == REFUEL_ITEM_STATUS.DONE)
            {
                CardView cardView = findViewById(R.id.refuel_detail_card_refuel);
                cardView.setVisibility(View.GONE);
                //btnRefuel.setVisibility(View.GONE);
                if (mItem.getStartTime()!=null )
                    ((TextView) findViewById(R.id.refuelitem_detail_startTime)).setText(simpleDateFormat.format(mItem.getStartTime()));

                ((TextView) findViewById(R.id.refuelitem_detail_realAmount)).setText(String.format("%.2f",mItem.getRealAmount()));
            }
            else if (status != REFUEL_ITEM_STATUS.DONE)
            {
                btnPrint.setVisibility(View.GONE);
            }
        }
    }

    private void save() {
        mItem.setAircraftCode(((EditText)findViewById(R.id.edtAircraft)).getText().toString());
        ((TextView)findViewById(R.id.refuelitem_detail_aircraftCode)).setText(mItem.getAircraftCode());
        mItem.setManualTemperature(Float.parseFloat(((EditText)findViewById(R.id.edtTemperature)).getText().toString()));
        ((TextView) findViewById(R.id.refuelitem_detail_Temperature)).setText(String.format("%.2f",mItem.getManualTemperature()));
        mItem.setDensity(Float.parseFloat(((EditText)findViewById(R.id.edtDensity)).getText().toString()));
        ((TextView) findViewById(R.id.refuelitem_detail_Density)).setText(String.format("%.2f",mItem.getDensity()));

        Spinner airline_spinner = (Spinner) findViewById(R.id.refuelitem_detail_airline_spinner);
        AirlineModel item = (AirlineModel) airline_spinner.getSelectedItem();
        mItem.setAirlineId(item.getId());
        ((TextView) findViewById(R.id.refuelitem_detail_airline)).setText(item.getName());
        try {
            Spinner vatSpinner = (Spinner) findViewById(R.id.refuelitem_detail_vat_spinner);
            String vat = vatSpinner.getSelectedItem().toString();
            NumberFormat format = NumberFormat.getPercentInstance();
            mItem.setTaxRate(format.parse(vat).doubleValue());
            ((TextView) findViewById(R.id.refuelitem_detail_vat)).setText(vat);

        }
        catch (Exception e){}
        //new HttpClient().postRefuel(mItem);
        toggleEdit();
    }

    private void toggleEdit() {
        if (!isEditing) {
            ((EditText) findViewById(R.id.edtAircraft)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.edtTemperature)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.edtDensity)).setVisibility(View.VISIBLE);
            ((Spinner) findViewById(R.id.refuelitem_detail_airline_spinner)).setVisibility(View.VISIBLE);
            ((Spinner) findViewById(R.id.refuelitem_detail_vat_spinner)).setVisibility(View.VISIBLE);
            ((EditText) findViewById(R.id.edtAircraft)).requestFocus();
            findViewById(R.id.refuelitem_detail_aircraftCode).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_Temperature).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_Density).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_airline).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_vat).setVisibility(View.INVISIBLE);
            ((Button)findViewById(R.id.btnEdit)).setText(getString(R.string.back));
            findViewById(R.id.btnSave).setVisibility(View.VISIBLE);
            isEditing = true;
        }
        else
        {
            ((EditText) findViewById(R.id.edtAircraft)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.edtTemperature)).setVisibility(View.INVISIBLE);
            ((EditText) findViewById(R.id.edtDensity)).setVisibility(View.INVISIBLE);
            ((Spinner) findViewById(R.id.refuelitem_detail_airline_spinner)).setVisibility(View.INVISIBLE);
            ((Spinner) findViewById(R.id.refuelitem_detail_vat_spinner)).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_aircraftCode).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_Temperature).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_Density).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_airline).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_vat).setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.btnEdit)).setText(getString(R.string.edit_refuel));
            findViewById(R.id.btnSave).setVisibility(View.INVISIBLE);
            isEditing = false;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
        }
    }

    private void refuel(RefuelItemData mItem) {
        if (mItem.getStatus() == REFUEL_ITEM_STATUS.DONE) {
            Toast.makeText(this, "INFO: " + getString(R.string.refuel_item_already_fueled), Toast.LENGTH_LONG).show();

        }
        else {
            Intent intent = new Intent(this, RefuelActivity.class);
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            intent.putExtra("REFUEL_ID", mItem.getId());
            intent.putExtra("REFUEL",gson.toJson(mItem));
            startActivityForResult(intent, mItem.getId());
        }
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.refuel_detail_chk_connect_lcr){
            if (reader.isDeviceError())
                reconnect();
            return;
        }
        CheckedTextView chkTxt = (CheckedTextView) findViewById(id);

        boolean isChecked = chkTxt.isChecked();
        chkTxt.setChecked(!isChecked);
        chkTxt.setCheckMarkDrawable(isChecked ? R.drawable.ic_unchecked : R.drawable.ic_checked);
        if (id == R.id.refuel_detail_chk_condition)
            conditionIsReady = !isChecked;
        else
            inventoryIsReady = !isChecked;
        setEnableButton(deviceIsReady && conditionIsReady && inventoryIsReady);


    }
    private void reconnect() {
        setConnectionCheckmark(CONNECTION_STATUS.CONNECTING);
        reader.doConnectDevice();
    }

    ///Reader define

    private LCRReader reader;
    private LCRDataModel model;
    private List<String> loggerList = new ArrayList<>();
    HttpClient client = new HttpClient();
    private boolean deviceIsReady = false;
    private boolean conditionIsReady = false;
    private  boolean inventoryIsReady = false;
    private boolean startButtonPress = false;

    private void start() {

        startButtonPress = true;

        reader.start();

    }
    private void pause()
    {
        reader.pause();
    }
    private void stop() {
        reader.end();


    }
    private void addListeners() {

        Button btnStart  = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();

            }
        });

        Button btnStop  = findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();

            }
        });

        reader.setConnectionListener(new LCRReader.LCRConnectionListener() {
            @Override
            public void onConnected() {
                deviceIsReady = true;
                setConnectionCheckmark(CONNECTION_STATUS.OK);
                setEnableButton(deviceIsReady && conditionIsReady && inventoryIsReady);

            }

            @Override
            public void onError() {

               setConnectionCheckmark(CONNECTION_STATUS.ERROR);
            }

            @Override
            public void onDeviceAdded(boolean failed) {

                //reader.doConnectDevice();
            }

            @Override
            public void onDisconnected() {

            }
        });

        reader.setFieldDataListener(new LCRReader.LCRDataListener() {
            @Override
            public void onDataChanged(LCRDataModel dataModel) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
                TextView txtGrossQty = findViewById(R.id.txtGrossQty);
                txtGrossQty.setText(String.format("%.2f",dataModel.getGrossQty()));

                TextView txtTemp = findViewById(R.id.txtTemp);
                txtTemp.setText(String.format("%.2f",dataModel.getTemperature()));

                ((TextView)findViewById(R.id.txtStartMeter)).setText(String.format("%.2f",dataModel.getStartMeterNumber()));
                ((TextView)findViewById(R.id.txtEndMeter)).setText(String.format("%.2f",dataModel.getEndMeterNumber()));



                model = dataModel;
            }

            @Override
            public void onErrorMessage(String errorMsg) {
                loggerList.add(errorMsg);
                if (loggerList.size()>15)
                    loggerList.remove(0);
                String logText = "";
                for (String err: loggerList) {
                    logText = logText + err +"\n";

                }
            }
        });

        reader.setStateListener(new LCRReader.LCRStateListener() {
            @Override
            public void onEndDelivery() {

            }

            @Override
            public void onStart() {
                //reader.requestData();
                setEnableButton(false);
            }

            @Override
            public void onStop() {
                //reader.stopRequestData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        doStop();
                    }
                });
            }
        });
    }
    private void doStop() {

        //setEnableButton(true, false);
        ///post data
        try{
            if (mItem!=null) {
                mItem.setRealAmount( model.getGrossQty());
                mItem.setTemperature(model.getTemperature());
                mItem.setStartTime(model.getStartTime());
                mItem.setEndTime(model.getEndTime());
                mItem.setStartNumber(model.getStartMeterNumber());
                mItem.setEndNumber(model.getEndMeterNumber());

                currentApp.setCurrentAmount(currentApp.getCurrentAmount()-model.getGrossQty());
                openPreview();
                setResult(Activity.RESULT_OK);
                finish();
            }

        }
        catch (Exception e)
        {}
    }
    private int PREVIEW_OPEN = 1;
    private void openPreview() {

        mItem.setStatus(REFUEL_ITEM_STATUS.DONE);

        mItem = (new HttpClient()).postRefuel(mItem);

        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        String data = gson.toJson(mItem);


        Intent intent = new Intent(this, RefuelPreviewActivity.class);
        intent.putExtra("REFUEL", data);
        startActivityForResult(intent, PREVIEW_OPEN);
        //finishAffinity();


    }
    private void setEnableButton(boolean enabled) {
        findViewById(R.id.btnStart).setEnabled(enabled);
        findViewById(R.id.btnStop).setEnabled(!enabled && startButtonPress);

    }
}
