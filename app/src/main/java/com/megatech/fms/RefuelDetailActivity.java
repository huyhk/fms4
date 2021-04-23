package com.megatech.fms;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.databinding.DataBindingUtil;

import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_COMMAND;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_DEVICE_CONNECTION_STATE;
import com.megatech.fms.databinding.ActivityRefuelDetailBinding;
import com.megatech.fms.databinding.EditRefuelDialogBinding;
import com.megatech.fms.helpers.DataHelper;
import com.megatech.fms.helpers.DateUtils;
import com.megatech.fms.helpers.LCRReader;
import com.megatech.fms.helpers.Logger;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.LCRDataModel;
import com.megatech.fms.model.REFUEL_ITEM_STATUS;
import com.megatech.fms.model.RefuelItemData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class RefuelDetailActivity extends UserBaseActivity implements View.OnClickListener {

    private List<AirlineModel> airlines = null;

    private Button btnReconnect;
    private Button btnRestart;
    Dialog inputDlg;
    private Button btnBack;
    private String TAG = "REFUEL_SCREEN";

    private enum CONNECTION_STATUS {
        OK,
        ERROR,
        CONNECTING
    }

    private RefuelItemData mItem;
    private Activity activity;
    private boolean isEditing = false;
    private boolean restartRequest = false;
    ActivityRefuelDetailBinding binding;
    Timer tmrCheckData = new Timer();
    private Button btnForceStop;
    private Date lastErrorTime = null;
    private int endRetry = 0;

    private void setTextBoxValue(int id, Object value) {
        setTextBoxValue(id, value, "%s");
    }

    private void setTextBoxValue(int id, Object value, String pattern) {
        if (value != null)
            ((TextView) findViewById(id)).setText(String.format(pattern, value));
    }

    private void reconnect() {

        setConnectionCheckmark(CONNECTION_STATUS.CONNECTING);

        reader.doConnectDevice();
    }

    private String m_Text = "";


    ///Reader define

    private LCRReader reader = null;
    private LCRDataModel model;
    private boolean deviceIsReady = false;
    private boolean deviceIsError = false;
    private boolean conditionIsReady = false;
    private boolean inventoryIsReady = false;
    private boolean startButtonPress = false;
    private boolean started = false;

    private Button btnStart;
    private int field_data_flag = FIELD_DATA_FLAG.FIELD_NOT_DEFINED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_refuel_detail);
        this.activity = this;
        started = false;
        Drawable drawable = getResources().getDrawable(R.drawable.ic_edit);
        drawable.setAlpha(90);

        btnReconnect = findViewById(R.id.btnReconnect);
        btnRestart = findViewById(R.id.btnRestart);
        btnForceStop = findViewById(R.id.btnForceStop);
        btnBack = findViewById(R.id.btnBack);
        btnStart = findViewById(R.id.btnStart);

        String storedIP = currentApp.getDeviceIP();
        reader = LCRReader.create(this, storedIP, 10001, false);
        //reader =  new LCRReader(this, storedIP);
        addListeners();


        this.model = new LCRDataModel();
        model.setUserId(currentUser.getUserId());
        deviceIsReady = reader.getConnected();
        //check if LCR status is started

        if (deviceIsReady)
        {
           setConnectionCheckmark(CONNECTION_STATUS.OK);
        }
        //shiftBinding = DataBindingUtil.setContentView(activity, R.layout.activity_refuel_detail, null);
        //shiftBinding.setMItem(mItem);

        Logger.appendLog("Start refueling");
        loaddata();
    }

    private void setConnectionCheckmark(CONNECTION_STATUS status) {

        switch (status) {
            case OK:
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                ((CheckedTextView) findViewById(R.id.refuel_detail_chk_connect_lcr)).setChecked(true);
                ((TextView) findViewById(R.id.lbl_connection_status)).setText(getString(R.string.lcr_connection_ok));
                ((TextView) findViewById(R.id.lbl_connection_status)).setTextColor(getResources().getColor(R.color.colorDarkGreen));
                ((CheckedTextView) findViewById(R.id.refuel_detail_chk_connect_lcr)).setCheckMarkDrawable(R.drawable.ic_checked_circle);
                btnReconnect.setVisibility(View.INVISIBLE);
                btnForceStop.setVisibility(View.INVISIBLE);
                break;
            case CONNECTING:
                CheckedTextView chkTxt = findViewById(R.id.refuel_detail_chk_connect_lcr);
                chkTxt.setCheckMarkDrawable(null);
                findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

                ((TextView) findViewById(R.id.lbl_connection_status)).setText(getString(R.string.lcr_connection_connecting));
                ((TextView) findViewById(R.id.lbl_connection_status)).setTextColor(Color.BLACK);
                btnReconnect.setVisibility(View.VISIBLE);
                btnRestart.setVisibility(View.VISIBLE);
                btnReconnect.setEnabled(false);
                //btnRestart.setEnabled(false);
                break;
            case ERROR:
                findViewById(R.id.progressBar).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.lbl_connection_status)).setText(getString(R.string.lcr_connection_error));
                ((TextView) findViewById(R.id.lbl_connection_status)).setTextColor(Color.RED);
                ((CheckedTextView) findViewById(R.id.refuel_detail_chk_connect_lcr)).setCheckMarkDrawable(R.drawable.ic_error);
                btnReconnect.setVisibility(View.VISIBLE);
                btnRestart.setVisibility(View.VISIBLE);
                btnForceStop.setVisibility(View.VISIBLE);
                btnReconnect.setEnabled(true);
                btnRestart.setEnabled(true);
                btnForceStop.setEnabled(true);
                setEnableButton(started);
                break;
        }

    }

    private void loaddata() {

        new Thread(() -> {
            airlines = DataHelper.getAirlines();
            Bundle b = getIntent().getExtras();
            Integer id = b.getInt("REFUEL_ID", 0);
            Integer localId = b.getInt("REFUEL_LOCAL_ID", 0);
            String mData = b.getString("REFUEL", "");
            Logger.appendLog("RFW", "Start loading Item : " + id);
            RefuelItemData itemData = null;
            if (mData != null && !mData.equals("")) {
                itemData = RefuelItemData.fromJson(mData);
            }
            if (itemData == null)
                itemData = DataHelper.getRefuelItem(id, localId);

            if (itemData != null)
                mItem = itemData;
            Logger.appendLog("RFW", "Flight Code: " + mItem.getFlightCode());
            runOnUiThread(() -> {
                if (!isFinishing())
                    showData();
            });

        }).start();

        if (reader.isStarted())
            onStarted();
    }

    private void showData() {
        //shiftBinding.setMItem(mItem);

        setTextBoxValue(R.id.refuelitem_detail_aircraftCode, mItem.getAircraftCode());
        setTextBoxValue(R.id.refuelitem_detail_Density, mItem.getDensity(), "%.4f");
        setTextBoxValue(R.id.refuelitem_detail_flightCode, mItem.getFlightCode());
        setTextBoxValue(R.id.refuelitem_detail_parking, mItem.getParkingLot());
        setTextBoxValue(R.id.refuelitem_detail_qc_no, mItem.getQualityNo());
        setTextBoxValue(R.id.refuelitem_detail_estimateAmount, mItem.getEstimateAmount(), "%.0f");
        setTextBoxValue(R.id.refuelitem_detail_Temperature, mItem.getTemperature(), "%.2f");
        setTextBoxValue(R.id.refuelitem_detail_realAmount, mItem.getRealAmount(), "%.0f");
        setTextBoxValue(R.id.refuelitem_detail_aircraftCode, mItem.getAircraftCode());
        setTextBoxValue(R.id.refuelitem_detail_aircraftCode, mItem.getAircraftCode());
        setTextBoxValue(R.id.refuelitem_detail_aircraftCode, mItem.getAircraftCode());

        activity = this;
        if (mItem.isAlert())
        {
            new AlertDialog.Builder(activity)
                    .setTitle(R.string.app_name)
                    .setMessage(R.string.inventory_alert)
                    .setIcon(R.drawable.ic_warning)
                    .setPositiveButton(R.string.btn_continue, (dialog, which) -> dialog.dismiss())
                    .setNegativeButton(R.string.btn_stop, (dialog, which) -> {
                        dialog.dismiss();
                        finish();
                    })
                    .create()
                    .show();
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        if (mItem != null) {


            ArrayAdapter<AirlineModel> spinnerAdapter = new ArrayAdapter<>(activity, R.layout.support_simple_spinner_dropdown_item, airlines);
            spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);


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
                        mItem.setUnit(item.getUnit());
                        mItem.setTaxRate(!mItem.isInternational() && item.isInternational() ? 0.1 : 0);
                        mItem.setCurrency(item.getCurrency());
                        setTextBoxValue(R.id.refuelitem_detail_airline, item.getName());
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
                    mItem.setCurrency(selected.getCurrency());
                    mItem.setUnit(selected.getUnit());
                    mItem.setTaxRate(!mItem.isInternational() && selected.isInternational() ? 0.1 : 0);
                    mItem.setProductName(selected.getProductName());
                    mItem.setAirlineModel(selected);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            if (!Objects.equals(mItem.getArrivalTime(), new Date(Long.MIN_VALUE)))
                ((TextView) findViewById(R.id.refuelitem_detail_arrival)).setText(simpleDateFormat.format(mItem.getArrivalTime()));
            if (!Objects.equals(mItem.getDepartureTime(), new Date(Long.MIN_VALUE)))
                ((TextView) findViewById(R.id.refuelitem_detail_departure)).setText(simpleDateFormat.format(mItem.getDepartureTime()));

        }

    }

    private void showConfirmDialog(int id) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage(id == R.id.btnStart ? R.string.start_confirm : R.string.stop_confirm);

        builder.setPositiveButton(getString(id == R.id.btnStart ? R.string.start : R.string.stop), (dialog, id12) -> {
            Logger.appendLog(id == R.id.btnStart ? "Confirm start " : "Confirm stop");
            if (id == R.id.btnStop) {
                stop();
            } else if (id == R.id.btnStart) {
                start();
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(getString(R.string.back), (dialog, id1) -> dialog.dismiss());

        builder.create().show();
    }

    private void pause() {
        reader.pause();
    }

    private void start() {

        startButtonPress = true;
        mItem.setStartTime(new Date());
        // send START DELIVERY command to LCR
        //reader.start();
        setRefuelStatus(REFUEL_STATUS.STARTING);

    }

    private void clearListeners() {
        reader.setConnectionListener(null);
        reader.setStateListener(null);
        reader.setFieldDataListener(null);
    }

    private void stop() {
        if (deviceIsError) {
            showForceStopDialog();
        } else {
            //send END DELIVERY command to LCR
            //reader.end(restartRequest);
            setRefuelStatus(REFUEL_STATUS.ENDING);
        }

    }

    public void onClick(View v) {
        int id = v.getId();
        Logger.appendLog("Click: " + v.toString());
        switch (id) {
            case R.id.btnTest:
                reader.requestData();
                break;
            case R.id.btnStart:
                showConfirmDialog(started ? R.id.btnStop : R.id.btnStart);
                break;
            case R.id.refuel_detail_chk_connect_lcr:
            case R.id.btnReconnect:

                reconnect();
                break;
            case R.id.btnRestart:
                //reader.initLCR();
                if (started)
                    currentApp.saveCurrentRefuel(mItem.getId(), mItem.getLocalId());
                showRestart();
                break;
            case R.id.btnForceStop:
                showForceStopDialog();
                break;

            case R.id.refuelitem_detail_aircraftCode:
                String m_Title = getString(R.string.update_aircraftCode);
                //showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                break;
            case R.id.refuelitem_detail_Density:
                m_Title = getString(R.string.update_density);
                //showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;
            case R.id.refuelitem_detail_Temperature:
                m_Title = getString(R.string.update_temparature);
                //showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;
            case R.id.refuelitem_detail_realAmount:
                m_Title = getString(R.string.update_real_amount);
                //showEditDialog(id, InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

                break;
            case R.id.refuelitem_detail_qc_no:
                m_Title = getString(R.string.update_qc_no);
                //showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                break;
            case R.id.refuelitem_detail_parking:
                m_Title = getString(R.string.update_parking_lot);
                //showEditDialog(id, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

                break;
            case R.id.refuelitem_detail_vat:
                //openVatSpinner();
                break;
            case R.id.refuelitem_detail_airline:
                //openAirlineSpinner();
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
                if (refuel_status ==REFUEL_STATUS.NONE) {
                    setEnableButton(deviceIsReady && conditionIsReady && inventoryIsReady);
                }
                break;
            case R.id.btnBack:
                finish();
                break;
            case R.id.dialog_endtime:

                showEndTimePicker();

                break;
        }

    }

    private void showEndTimePicker() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(mItem.getEndTime());
        TimePickerDialog timePicker = new TimePickerDialog(this, android.R.style.Theme_Material_Light_Dialog
                , new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                cal.set(Calendar.MINUTE, minute);
                //mItem.setEndTime(cal.getTime());
                ((EditText) inputDlg.findViewById(R.id.dialog_endtime)).setText(DateUtils.formatDate(cal.getTime(), "HH:mm"));
            }
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        timePicker.show();

    }

    @SuppressLint("StaticFieldLeak")
    private void restartReader() {

        Log.e("REFUEL", "restart reader");

        new Thread(() -> {
            reader.restart();
        }).start();


    }

    private void showForceStopDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.app_name);
        builder.setMessage( R.string.force_stop_confirm);

        builder.setPositiveButton(getString(R.string.stop), (dialog, id) -> {


            dialog.dismiss();
            showDataInput();
        });
        builder.setNegativeButton(getString(R.string.back), (dialog, id) -> {
            dialog.dismiss();

        });

        Dialog dlg = builder.create();
        dlg.show();
        ((TextView) dlg.findViewById(android.R.id.message)).setTextSize(18);
        ((TextView) dlg.findViewById(android.R.id.button1)).setTextSize(18);
        ((TextView) dlg.findViewById(android.R.id.button2)).setTextSize(18);

    }

    private void setButtonText(REFUEL_STATUS status)
    {
        int btnText = R.string.start;
        int color = R.color.bgPrimary;
        switch (status) {
            case NONE:
                btnText = R.string.start;
                break;
            case STARTING:
                btnText = R.string.sending_start;
                break;
            case STARTED:
                btnText = R.string.stop;
                break;
            case ENDING:
                btnText = R.string.sending_stop;
                break;
            case ENDED:
                btnText = R.string.start;
                break;
            default:
                break;
        }
        btnStart.setText(btnText);
    }

    private void addListeners() {


        reader.setConnectionListener(new LCRReader.LCRConnectionListener() {
            @Override
            public void onConnected() {
                Logger.appendLog("RFW", "onConnected");
                deviceIsReady = true;
                deviceIsError = false;
                setConnectionCheckmark(CONNECTION_STATUS.OK);
                setEnableButton(started || (deviceIsReady && conditionIsReady && inventoryIsReady));
                //if (started)
                //    reader.requestData();
                if (reader.isStarted())
                    onStarted();
            }

            @Override
            public void onError() {
                Logger.appendLog("RFW", " onConnectionError ");

                deviceIsReady = false;
                deviceIsError = true;
                lastErrorTime = new Date();
               setConnectionCheckmark(CONNECTION_STATUS.ERROR);
            }

            @Override
            public void onDeviceAdded(boolean failed) {
                Logger.appendLog("RFW", "  onDeviceAdded - ");

                //reader.doConnectDevice();
            }

            @Override
            public void onDisconnected() {
                Logger.appendLog("RFW", "  onDisconnected - ");

                deviceIsReady = false;
                deviceIsError = true;
                setConnectionCheckmark(CONNECTION_STATUS.ERROR);
            }

            @Override
            public void onCommandError(LCR_COMMAND command) {
                Logger.appendLog("RFW", "  onCommandError - " + command.toString());

            }

            @Override
            public void onConnectionStateChange(LCR_DEVICE_CONNECTION_STATE state) {
                Logger.appendLog("RFW", "  onConnectionStateChange - " + state.toString());

                if (state == LCR_DEVICE_CONNECTION_STATE.CONNECTING_NETWORK
                || state == LCR_DEVICE_CONNECTION_STATE.CONNECTING_DEVICE
                || state == LCR_DEVICE_CONNECTION_STATE.RECONNECTING)
                    setConnectionCheckmark(CONNECTION_STATUS.CONNECTING);

                if (state == LCR_DEVICE_CONNECTION_STATE.DISCONNECTED)
                    deviceIsError = true;

            }

        });

        reader.setFieldDataListener(new LCRReader.LCRDataListener() {
            @Override
            public void onDataChanged(LCRDataModel dataModel, LCRReader.FIELD_CHANGE field_change) {

                String errorLog = " onDataChanged " + field_change.toString() + ": ";
                switch (field_change  )
                {
                    case ENDTIME:

                        field_data_flag = field_data_flag | FIELD_DATA_FLAG.FIELD_ENDTIME;

                        if (refuel_status == REFUEL_STATUS.ENDED) {

                        }
                        errorLog += dataModel.getEndTime();
                        break;

                    case STARTTIME:
                        field_data_flag = field_data_flag | FIELD_DATA_FLAG.FIELD_STARTTIME;
                        if (started && !btnStart.isEnabled()) {

                            //Really started after STARTTIME field received
                            btnStart.setEnabled(true);

                            setRefuelStatus(REFUEL_STATUS.STARTED);
                        }
                        errorLog += dataModel.getStartTime();
                        break;
                    case GROSSQTY:
                        field_data_flag = field_data_flag | FIELD_DATA_FLAG.FIELD_GROSSQTY;
                        runOnUiThread(() -> {
                            TextView txtGrossQty = findViewById(R.id.txtGrossQty);
                            txtGrossQty.setText(String.format("%.0f", dataModel.getGrossQty()));
                        });
                        errorLog += dataModel.getGrossQty();
                        break;
                    case TOTALIZER:
                        field_data_flag = field_data_flag | FIELD_DATA_FLAG.FIELD_TOTALLIZER;
                        runOnUiThread(() -> {
                            ((TextView) findViewById(R.id.txtStartMeter)).setText(String.format("%,.0f", dataModel.getStartMeterNumber()));
                            ((TextView) findViewById(R.id.txtEndMeter)).setText(String.format("%,.0f", dataModel.getEndMeterNumber()));
                        });
                        errorLog += dataModel.getEndMeterNumber();
                        break;
                    case TEMPERATURE:
                        field_data_flag = field_data_flag | FIELD_DATA_FLAG.FIELD_TEMPERATURE;
                        runOnUiThread(() -> {
                            TextView txtTemp = findViewById(R.id.txtTemp);
                            txtTemp.setText(String.format("%.2f", dataModel.getTemperature()));
                        });
                        errorLog += dataModel.getTemperature();
                        break;
                }

                //Logger.appendLog("RFW",errorLog);
                model = dataModel;

                updateRefuelData();

            }

            @Override
            public void onErrorMessage(String errorMsg) {

                Logger.appendLog("LCR", errorMsg);
            }

            @Override
            public void onFieldAddSucess(String field_name) {

            }
        });

        reader.setStateListener(new LCRReader.LCRStateListener() {
            @Override
            public void onEndDelivery() {
                Logger.appendLog("RFW", "  onEndDelivery");
            }

            @Override
            public void onStart() {
                //reader.requestData();
                //setEnableButton(false);
                Logger.appendLog("RFW", "  onStart");
                onStarted();

            }

            @Override
            public void onStop() {
                Logger.appendLog("RFW", "  onStop");
                onStopped();
            }
        });
    }

    private void onStopped() {


        if (started) {
            refuel_status = REFUEL_STATUS.ENDING;
            setRefuelStatus(refuel_status);
        }
        if (started && endRetry < 3) {
            endRetry++;
            //wait 5s to receive END TIME and last GROSSQTY, TOTALIZER, TEMPERATURE data
            Timer tmrStop = new Timer();
            tmrStop.schedule(new TimerTask() {
                @Override
                public void run() {
                    // if not receive ENDTIME within 5s, restart LCR service
                    if ((field_data_flag & FIELD_DATA_FLAG.FIELD_ENDTIME) > 0) {
                        tmrStop.cancel();
                        doStop();

                    }
                }
            }, 1000 * 5, 1000 * 3);
        } else if (started) {
            showDataInput();
        }
    }

    private void showContinueConfirm() {

        new AlertDialog.Builder(this)
                .setMessage(R.string.refuelling_status)
                .setTitle(R.string.app_name)
                /*.setNegativeButton(R.string.restart, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Logger.appendLog("Confirm restart refuel");
                        restartRefuel();
                    }
                })*/
                .setPositiveButton(R.string.refuel_continue, (dialog, which) -> {

                    Logger.appendLog("Confirm continue refuel");
                    continueRefuel();
                })
                .create()
                .show();
    }

    private REFUEL_STATUS refuel_status = REFUEL_STATUS.NONE;

    private void continueRefuel() {


        startButtonPress = true;
        setRefuelStatus(REFUEL_STATUS.STARTED); //start();

    }

    private void showDataInput() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        EditRefuelDialogBinding binding = DataBindingUtil.inflate(this.getLayoutInflater(), R.layout.edit_refuel_dialog, null, false);
        binding.setMItem(mItem);
        builder.setView(binding.getRoot());
        builder.setTitle(R.string.app_name)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        AlertDialog v = (AlertDialog) dialog;
                        mItem.setEndNumber(Float.parseFloat(((EditText) v.findViewById(R.id.dialog_meter)).getText().toString().replaceAll(",", "")));
                        mItem.setRealAmount(Float.parseFloat(((EditText) v.findViewById(R.id.dialog_volume)).getText().toString().replaceAll(",", "")));
                        mItem.setTemperature(Float.parseFloat(((EditText) v.findViewById(R.id.dialog_temperature)).getText().toString().replaceAll(",", "")));
                        mItem.setManualTemperature(Float.parseFloat(((EditText) v.findViewById(R.id.dialog_temperature)).getText().toString().replaceAll(",", "")));
                        dialog.dismiss();
                        doStop();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        inputDlg = builder.create();
        inputDlg.show();
    }

    private void restartRefuel() {
        // turn on restart flag
        restartRequest = true;
        // call stop with restart flag ON
        stop();
    }

    private void onStarted() {
        if (!started) {
            reader.requestData();
            field_data_flag = FIELD_DATA_FLAG.FIELD_NOT_DEFINED;
            tmrCheckData = new Timer();
            TimerTask tmrTask = new TimerTask() {
                @Override
                public void run() {

                    if (!reader.isDeviceError()) {
                        if ((field_data_flag & FIELD_DATA_FLAG.FIELD_GROSSQTY) == 0
                                || (field_data_flag & FIELD_DATA_FLAG.FIELD_TOTALLIZER) == 0) {
                            tmrCheckData.cancel();
                            restartReader();
                        }

                    }
                    field_data_flag = FIELD_DATA_FLAG.FIELD_NOT_DEFINED;
                }
            };
            //tmrCheckData.schedule(tmrTask,1000*10);


            started = true;

            setRefuelStatus(REFUEL_STATUS.STARTED);
            new Thread(() -> {
                if (mItem != null) {
                    mItem.setStartTime(new Date());
                    mItem.setStatus(REFUEL_ITEM_STATUS.PROCESSING);
                    DataHelper.postRefuel(mItem);

                }
            }).start();


        }

    }

    private void setRefuelStatus(REFUEL_STATUS status) {
        runOnUiThread(() -> {
            TextView lblrefuelStatus = findViewById(R.id.lbl_refuel_status);
            refuel_status = status;
            setButtonText(status);
            btnBack.setEnabled(refuel_status == REFUEL_STATUS.NONE);

            switch (status) {

                case STARTING:
                case ENDING:
                    lblrefuelStatus.setText(R.string.refuel_ending);
                    lblrefuelStatus.setBackgroundColor(getResources().getColor(R.color.bgPrimary));
                    break;
                case ENDED:
                    lblrefuelStatus.setText(R.string.refuel_ended);
                    lblrefuelStatus.setBackgroundColor(getResources().getColor(R.color.bgInfo));
                    //setEnableButton(false);
                    break;


                case STARTED:
                    started = status == REFUEL_STATUS.STARTED;
                    lblrefuelStatus.setText(R.string.refuel_processing);
                    lblrefuelStatus.setBackgroundColor(getResources().getColor(R.color.bgSuccess));
                    //setEnableButton(true);
                    break;
                default:
                    break;


            }
        });
    }

    private void updateRefuelData() {
        if (mItem != null) {
            mItem.setRealAmount(model.getGrossQty());
            mItem.setGallon(model.getGrossQty());
            mItem.setTemperature(model.getTemperature());
            mItem.setManualTemperature(model.getTemperature());
            mItem.setDeviceStartTime(model.getStartTime());
            mItem.setDeviceEndTime(model.getEndTime());
            //mItem.setStartNumber(model.getEndMeterNumber() - model.getGrossQty());
            mItem.setStartNumber(model.getStartMeterNumber());
            mItem.setEndNumber(model.getEndMeterNumber());
        }
        Log.e("REFUEL", "Update refuel data");
    }

    private void doStop() {

        setRefuelStatus(REFUEL_STATUS.ENDED);


        try {
            if (mItem != null) {
                //mItem.setRealAmount( model.getGrossQty());
                //mItem.setGallon(model.getGrossQty());
                //mItem.setTemperature(model.getTemperature());
                //mItem.setManualTemperature(model.getTemperature());
                //mItem.setDeviceStartTime(model.getStartTime());
                //mItem.setDeviceEndTime(model.getEndTime());
                //mItem.setStartNumber(model.getEndMeterNumber() - model.getGrossQty());
                //mItem.setStartNumber(model.getStartMeterNumber());
                //mItem.setEndNumber(model.getEndMeterNumber());
                mItem.setEndTime(new Date());
                if (mItem.getDeviceEndTime() != null && mItem.getDeviceStartTime() != null) {
                    long dateDiff = mItem.getDeviceEndTime().getTime() - mItem.getDeviceStartTime().getTime();
                    mItem.setStartTime(new Date(mItem.getEndTime().getTime() - dateDiff));
                }
                boolean isExtract = mItem.getRefuelItemType() == RefuelItemData.REFUEL_ITEM_TYPE.EXTRACT;
                currentApp.setCurrentAmount(currentApp.getCurrentAmount() + (isExtract ? model.getGrossQty() : -model.getGrossQty()));


            }

            Log.e("REFUEL", "doStop");

        }
        catch (Exception e) {
            Logger.appendLog("RFW", " doStop Error: " + e.getLocalizedMessage());
        }
        openPreview();
    }

    private enum REFUEL_STATUS {
        NONE,
        STARTING,
        STARTED,
        ENDING,
        ENDED
    }

    private void openPreview() {

        mItem.setStatus(REFUEL_ITEM_STATUS.DONE);
        mItem.setTruckNo(currentApp.getTruckNo());
        new AsyncTask<Void, Void, RefuelItemData>() {
            @Override
            protected RefuelItemData doInBackground(Void... voids) {
                mItem = DataHelper.postRefuel(mItem);
                return mItem;
            }

            @Override
            protected void onPostExecute(RefuelItemData itemData) {
                postRefuelCompleted(itemData);
                super.onPostExecute(itemData);
            }
        }.execute();

        //reader.destroy();
    }

    private void postRefuelCompleted(RefuelItemData mItem) {
        if (mItem != null) {
            //Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();


            Intent intent = new Intent(this, RefuelPreviewActivity.class);
            intent.putExtra("REFUEL_ID", mItem.getId());
            intent.putExtra("REFUEL_LOCAL_ID", mItem.getLocalId());
            int PREVIEW_OPEN = 1;
            startActivityForResult(intent, PREVIEW_OPEN);
        }
        clearListeners();
        started = false;
        finish();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void setEnableButton(boolean enabled) {
        findViewById(R.id.btnStart).setEnabled(enabled);
    }

    private interface FIELD_DATA_FLAG {
        public static int FIELD_NOT_DEFINED = 0;
        public static int FIELD_ENDTIME = 1;
        public static int FIELD_STARTTIME = 2;
        public static int FIELD_GROSSQTY = 4;
        public static int FIELD_TOTALLIZER = 8;
        public static int FIELD_TEMPERATURE = 16;

        public static int FIELD_ALL = 31;
        public static int FIELD_ALL_DATA = 30;
        public static int FIELD_ALL_METER = 12;
    }
}
