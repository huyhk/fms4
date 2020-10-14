package com.megatech.fms;

import androidx.cardview.widget.CardView;
import androidx.databinding.DataBindingUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RefuelDetailActivity extends UserBaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refuel_detail);
        this.activity = this;
        Drawable drawable = getResources().getDrawable(R.drawable.ic_edit);
        drawable.setAlpha(90);
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
                CheckedTextView chkTxt = findViewById(R.id.refuel_detail_chk_connect_lcr);
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
    ActivityRefuelDetailBinding binding;

    private void loaddata() {
        Bundle b = getIntent().getExtras();
        Integer id = b.getInt("REFUEL_ID", 2);
        String mData = b.getString("REFUEL","");
        if (mData != null && !mData.equals("")) {
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

            mItem = gson.fromJson(mData, RefuelItemData.class);
        }
        if (mItem == null)
            mItem = new HttpClient().getRefuelItem(id);
        if (mItem == null) {
            Toast.makeText(this, getString(R.string.error_not_found), Toast.LENGTH_LONG);
            return;
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_refuel_detail);
        binding.setMItem(mItem);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (mItem != null) {

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

            Button btnSave = findViewById(R.id.btnUpdate);
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

            /*((TextView) findViewById(R.id.refuelitem_detail_flightCode)).setText(refuelData.getFlightCode());
            ((TextView) findViewById(R.id.refuelitem_detail_aircraftCode)).setText(refuelData.getAircraftCode());
            ((TextView) findViewById(R.id.refuelitem_detail_parking)).setText(refuelData.getParkingLot());
            ((TextView) findViewById(R.id.refuelitem_detail_estimateAmount)).setText(String.format("%.2f",refuelData.getEstimateAmount()));
            */
            //((EditText) findViewById(R.id.edtAircraft)).setText(refuelData.getAircraftCode());
            /*((TextView) findViewById(R.id.refuelitem_detail_startTime)).setText(simpleDateFormat.format(refuelData.getStartTime()));
            ((TextView) findViewById(R.id.refuelitem_detail_endTime)).setText(simpleDateFormat.format(refuelData.getEndTime()));
            ((TextView) findViewById(R.id.refuelitem_detail_Temperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));
            */
            //((EditText) findViewById(R.id.edtTemperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));
            //((TextView) findViewById(R.id.refuelitem_detail_Density)).setText(String.format("%.2f",refuelData.getDensity()));
            //((EditText) findViewById(R.id.edtDensity)).setText(String.format("%.2f",refuelData.getDensity()));
            //((TextView) findViewById(R.id.refuelitem_detail_qc_no)).setText(refuelData.getQualityNo());

            Spinner airline_spinner = findViewById(R.id.refuelitem_detail_airline_spinner);
            airline_spinner.setAdapter(spinnerAdapter);

            if (mItem.getAirlineId()>0) {
                for (int i=0;i<airline_spinner.getCount();i++)
                {
                    AirlineModel item = (AirlineModel)  airline_spinner.getItemAtPosition(i);
                    if (mItem.getAirlineId() == item.getId())
                    {
                        airline_spinner.setSelection(i);
                        //((TextView) findViewById(R.id.refuelitem_detail_airline)).setText(item.getName());
                        mItem.setAirlineId(item.getId());
                        mItem.setProductName(item.getProductName());
                        mItem.setAirlineModel(item);
                        mItem.setPrice(item.getPrice());
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
        //refuelData.setManualTemperature(Float.parseFloat(((EditText)findViewById(R.id.edtTemperature)).getText().toString()));
        //((TextView) findViewById(R.id.refuelitem_detail_Temperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));
        mItem.setDensity(Double.parseDouble(((EditText) findViewById(R.id.edtDensity)).getText().toString()));
        ((TextView) findViewById(R.id.refuelitem_detail_Density)).setText(String.format("%.4f", mItem.getDensity()));

        Spinner airline_spinner = findViewById(R.id.refuelitem_detail_airline_spinner);
        AirlineModel item = (AirlineModel) airline_spinner.getSelectedItem();
        mItem.setAirlineId(item.getId());
        ((TextView) findViewById(R.id.refuelitem_detail_airline)).setText(item.getName());
        try {
            Spinner vatSpinner = findViewById(R.id.refuelitem_detail_vat_spinner);
            String vat = vatSpinner.getSelectedItem().toString();
            NumberFormat format = NumberFormat.getPercentInstance();
            mItem.setTaxRate(format.parse(vat).doubleValue());
            ((TextView) findViewById(R.id.refuelitem_detail_vat)).setText(vat);

        }
        catch (Exception e){}
        new Thread(new Runnable() {
            @Override
            public void run() {
                new HttpClient().postRefuel(mItem);
            }
        }).start();

        toggleEdit();
    }

    private void toggleEdit() {
        if (!isEditing) {
            findViewById(R.id.edtAircraft).setVisibility(View.VISIBLE);
            //((EditText) findViewById(R.id.edtTemperature)).setVisibility(View.VISIBLE);
            findViewById(R.id.edtDensity).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_airline_spinner).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_vat_spinner).setVisibility(View.VISIBLE);
            findViewById(R.id.edtAircraft).requestFocus();
            findViewById(R.id.refuelitem_detail_aircraftCode).setVisibility(View.INVISIBLE);
            //findViewById(R.id.refuelitem_detail_Temperature).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_Density).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_airline).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_vat).setVisibility(View.INVISIBLE);
            ((Button)findViewById(R.id.btnEdit)).setText(getString(R.string.back));
            findViewById(R.id.btnUpdate).setVisibility(View.VISIBLE);
            isEditing = true;
        }
        else
        {
            findViewById(R.id.edtAircraft).setVisibility(View.INVISIBLE);
            //((EditText) findViewById(R.id.edtTemperature)).setVisibility(View.INVISIBLE);
            findViewById(R.id.edtDensity).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_airline_spinner).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_vat_spinner).setVisibility(View.INVISIBLE);
            findViewById(R.id.refuelitem_detail_aircraftCode).setVisibility(View.VISIBLE);
            //findViewById(R.id.refuelitem_detail_Temperature).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_Density).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_airline).setVisibility(View.VISIBLE);
            findViewById(R.id.refuelitem_detail_vat).setVisibility(View.VISIBLE);
            ((Button)findViewById(R.id.btnEdit)).setText(getString(R.string.edit_refuel));
            findViewById(R.id.btnUpdate).setVisibility(View.INVISIBLE);
            isEditing = false;
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(),0);
        }
    }



    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.refuel_detail_chk_connect_lcr:
                if (reader.isDeviceError())
                    reconnect();

                break;
            case R.id.refuelitem_detail_aircraftCode:
                m_Title = getString(R.string.update_aircraftCode);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                break;
            case R.id.refuelitem_detail_Density:
                m_Title = getString(R.string.update_density);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;
            case R.id.refuelitem_detail_Temperature:
                m_Title = getString(R.string.update_temparature);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;
            case R.id.refuelitem_detail_realAmount:
                m_Title = getString(R.string.update_real_amount);
                showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;
            case R.id.refuelitem_detail_qc_no:
                m_Title = getString(R.string.update_qc_no);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                break;
            case R.id.refuelitem_detail_parking:
                m_Title = getString(R.string.update_parking_lot);
                showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                break;
            case R.id.refuelitem_detail_vat:
                openVatSpinner();
                break;
            case R.id.refuelitem_detail_airline:
                openAirlineSpinner();
                break;

            case R.id.refuel_detail_chk_condition:
            case R.id.refuel_detail_chk_inventory:
                CheckedTextView chkTxt = findViewById(id);

                boolean isChecked = chkTxt.isChecked();
                chkTxt.setChecked(!isChecked);
                chkTxt.setCheckMarkDrawable(isChecked ? R.drawable.ic_unchecked : R.drawable.ic_checked);
                if (id == R.id.refuel_detail_chk_condition)
                    conditionIsReady = !isChecked;
                else
                    inventoryIsReady = !isChecked;
                setEnableButton(deviceIsReady && conditionIsReady && inventoryIsReady);

                break;
            case R.id.btnBack:
                finish();
                break;
        }


    }

    private boolean vatFirstClick = true;
    private boolean airlineFirstClick = true;

    private void openVatSpinner() {

        Spinner vatSpinner = findViewById(R.id.refuelitem_detail_vat_spinner);

        vatSpinner.setVisibility(View.VISIBLE);
        findViewById(R.id.refuelitem_detail_vat).setVisibility(View.GONE);


        vatSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (vatFirstClick) {
                    vatFirstClick = false;
                    return;

                }
                Spinner vatSpinner = (Spinner) parent;
                String vat = vatSpinner.getSelectedItem().toString();
                NumberFormat format = NumberFormat.getPercentInstance();
                try {
                    mItem.setTaxRate(format.parse(vat).doubleValue());
                } catch (ParseException e) {

                }
                vatSpinner.setVisibility(View.GONE);
                findViewById(R.id.refuelitem_detail_vat).setVisibility(View.VISIBLE);
                updateBinding();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Spinner vatSpinner = (Spinner) parent;
                vatSpinner.getSelectedItem();
            }
        });

        vatSpinner.performClick();

    }

    private void openAirlineSpinner() {

        Spinner airline_spinner = findViewById(R.id.refuelitem_detail_airline_spinner);

        airline_spinner.setVisibility(View.VISIBLE);
        findViewById(R.id.refuelitem_detail_airline).setVisibility(View.GONE);

        airline_spinner.performClick();

        airline_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (airlineFirstClick) {
                    airlineFirstClick = false;
                    return;

                }
                Spinner airline_spinner = (Spinner) parent;
                AirlineModel selected = (AirlineModel) parent.getItemAtPosition(position);
                mItem.setAirlineId(selected.getId());
                mItem.setPrice(selected.getPrice());
                mItem.setProductName(selected.getProductName());
                mItem.setAirlineModel(selected);
                airline_spinner.setVisibility(View.GONE);
                findViewById(R.id.refuelitem_detail_airline).setVisibility(View.VISIBLE);
                updateBinding();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void reconnect() {
        setConnectionCheckmark(CONNECTION_STATUS.CONNECTING);
        reader.doConnectDevice();
    }

    private String m_Title = "";
    private String m_Text = "";

    private void showEditDialog(final int id, int inputType) {


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(m_Title);
        final EditText input = new EditText(this);
        input.setInputType(inputType);
        input.setText(((TextView) findViewById(id)).getText());
        input.setSelectAllOnFocus(true);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        input.setGravity(Gravity.CENTER_HORIZONTAL);
        input.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                return actionId == EditorInfo.IME_ACTION_DONE;
            }


        });
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                doUpdateResult();

            }

            private void doUpdateResult() {
                m_Text = input.getText().toString();
                switch (id) {
                    case R.id.refuelitem_detail_aircraftCode:
                        mItem.setAircraftCode(m_Text);
                        //((TextView)findViewById(R.id.lblAircraftNo)).setText(refuelData.getAircraftCode());
                        break;
                    case R.id.refuelitem_detail_Density:
                        mItem.setDensity(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_Density)).setText(String.format("%.2f",refuelData.getDensity()));
                        break;
                    case R.id.refuelitem_detail_Temperature:

                        mItem.setManualTemperature(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_Temperature)).setText(String.format("%.2f",refuelData.getManualTemperature()));

                        break;
                    case R.id.refuelitem_detail_realAmount:
                        mItem.setRealAmount(Double.parseDouble(m_Text.replace(",", "")));
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;
                    case R.id.refuelitem_detail_qc_no:
                        mItem.setQualityNo(m_Text);
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;
                    case R.id.refuelitem_detail_parking:
                        mItem.setParkingLot(m_Text);
                        //((TextView)findViewById(R.id.refuel_preview_realAmount)).setText(String.format("%.2f",refuelData.getRealAmount()));

                        break;
                }
                updateBinding();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();

    }

    private void updateBinding() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                RefuelItemData nItem = new HttpClient().postRefuel(mItem);
                mItem.setId(nItem.getId());
            }
        }).start();
        binding.invalidateAll();


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
    private boolean started = false;

    private Button btnStart;

    private void showConfirmDialog(int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(id == R.id.btnStart ? R.string.start_confirm : R.string.stop_confirm);
        int senderId = id;
        builder.setPositiveButton(getString(senderId == R.id.btnStart ? R.string.start : R.string.stop), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (senderId == R.id.btnStop) {
                    stop();
                } else if (senderId == R.id.btnStart) {
                    start();

                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.back), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

    private void start() {

        startButtonPress = true;
        mItem.setStartTime(new Date());
        reader.start();
        btnStart.setText(R.string.sending_start);
        btnStart.setEnabled(false);
    }
    private void pause()
    {
        reader.pause();
    }
    private void stop() {
        btnStart.setEnabled(false);
        reader.end();
        btnStart.setText(R.string.sending_stop);

    }
    private void addListeners() {

        btnStart = findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog(started ? R.id.btnStop : R.id.btnStart);
//                if (started)
//                    stop();
//                else
//                    start();

            }
        });

//        final Button btnStop  = findViewById(R.id.btnStop);
//        btnStop.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stop();
//
//            }
//        });

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
                txtGrossQty.setText(String.format("%.0f", dataModel.getGrossQty()));

                TextView txtTemp = findViewById(R.id.txtTemp);
                txtTemp.setText(String.format("%.2f",dataModel.getTemperature()));

                ((TextView) findViewById(R.id.txtStartMeter)).setText(String.format("%,.0f", dataModel.getStartMeterNumber()));
                ((TextView) findViewById(R.id.txtEndMeter)).setText(String.format("%,.0f", dataModel.getEndMeterNumber()));



                model = dataModel;

                if (started && !btnStart.isEnabled()) {
                    btnStart.setEnabled(true);
                    btnStart.setText(R.string.stop);
                }
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
                //setEnableButton(false);
                started = true;

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
                mItem.setGallon(model.getGrossQty());
                mItem.setTemperature(model.getTemperature());
                mItem.setManualTemperature(model.getTemperature());
                mItem.setDeviceStartTime(model.getStartTime());
                mItem.setDeviceEndTime(model.getEndTime());
                mItem.setStartNumber(model.getStartMeterNumber());
                mItem.setEndNumber(model.getEndMeterNumber());
                mItem.setEndTime(new Date());
                Boolean isExtract = mItem.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.EXTRACT;
                currentApp.setCurrentAmount(currentApp.getCurrentAmount() + (isExtract ? model.getGrossQty() : -model.getGrossQty()));
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
        mItem.setTruckNo(currentApp.getTruckNo());

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
        //findViewById(R.id.btnStop).setEnabled(!enabled && startButtonPress);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //reader.destroy();
        //reader.quit();
    }
}