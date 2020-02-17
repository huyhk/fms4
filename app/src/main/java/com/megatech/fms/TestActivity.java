package com.megatech.fms;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.liquidcontrols.lcr.iq.sdk.ConnectionOptions;
import com.liquidcontrols.lcr.iq.sdk.DeviceInfo;
import com.liquidcontrols.lcr.iq.sdk.FieldItem;
import com.liquidcontrols.lcr.iq.sdk.LCRCommunicationException;
import com.liquidcontrols.lcr.iq.sdk.LcrSdk;
import com.liquidcontrols.lcr.iq.sdk.RequestField;
import com.liquidcontrols.lcr.iq.sdk.ResponseField;
import com.liquidcontrols.lcr.iq.sdk.SDKDeviceException;
import com.liquidcontrols.lcr.iq.sdk.WiFiConnectionOptions;
import com.liquidcontrols.lcr.iq.sdk.interfaces.CommandListener;
import com.liquidcontrols.lcr.iq.sdk.interfaces.DeviceCommunicationListener;
import com.liquidcontrols.lcr.iq.sdk.interfaces.DeviceConnectionListener;
import com.liquidcontrols.lcr.iq.sdk.interfaces.DeviceListener;
import com.liquidcontrols.lcr.iq.sdk.interfaces.DeviceStatusListener;
import com.liquidcontrols.lcr.iq.sdk.interfaces.FieldListener;
import com.liquidcontrols.lcr.iq.sdk.interfaces.NetworkConnectionListener;
import com.liquidcontrols.lcr.iq.sdk.interfaces.PrinterStatusListener;
import com.liquidcontrols.lcr.iq.sdk.interfaces.SwitchStateListener;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.FIELDS.FIELD_REQUEST_STATES;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.FIELDS.UNITS;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.COMMAND_STATE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.FIELD_WRITE_STATE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_COMMAND;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_COMMUNICATION_STATUS;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_DELIVERY_CODE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_DELIVERY_STATUS;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_DEVICE_CONNECTION_STATE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_DEVICE_STATE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_MESSAGE_STATE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_PRINTER_STATUS;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_SECURITY_LEVEL;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_SWITCH_STATE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.LCR_THREAD_CONNECTION_STATE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.constants.LCR.PRINTING_STATE;
import com.liquidcontrols.lcr.iq.sdk.lc.api.device.InternalEvent;
import com.liquidcontrols.lcr.iq.sdk.lc.api.network.NETWORK_TYPE;
import com.liquidcontrols.lcr.iq.sdk.utils.AsyncCallback;
import com.megatech.fms.model.LCRDataModel;
import com.megatech.fms.helpers.LCRReader;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestActivity extends UserBaseActivity {

    ///private variables

    LcrSdk lcrSdk = new LcrSdk(this);

    private final List<FieldItem> availableLCRFields = new ArrayList<>();

    private enum CONNECTION_TYPE {
        BLUETOOTH,
        WIFI
    }
    private String deviceId = "LCR iQ";
    private final Integer lcpLCRAddress = 250;

    /** SDK LCP protocol address */
    private final Integer lcpSDKAddress = 20;

    private String wifiIpAddress = currentApp.getDeviceIP();
    private Integer wifiPort = 10001;


    /** LCR User fields to get data */
    private FieldItem grossQty = null;
    private FieldItem netQty = null;
    private FieldItem flowRate = null;
    private FieldItem temp = null;
    private FieldItem startTime = null;
    private FieldItem endTime = null;
    private FieldItem grossMeter = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        activity = this;

        lcrSdk.init(new AsyncCallback() {
            @Override
            public void onAsyncReturn(@Nullable Throwable error) {
                // Throwable only has data if error occurred
                if(error != null) {
                    // Error at init
                    String strError = "ERROR INIT SDK : " + error.getLocalizedMessage();
                    Log.e("Main",strError);

                } else {
                    // Add listeners to receive data from SDK
                    addSDKListeners();
                    // Add device to communicate with
                    doAddDevice();
                }
            }
        });



        addListerners();
        ((Button)findViewById(R.id.button2)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lcrReader.start();

            }
        });

        ((Button)findViewById(R.id.button3)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lcrReader.end();
            }
        });
    }
    @Nullable
    private FieldItem findUserFieldByName(@NonNull String name) {
        for(FieldItem item : availableLCRFields) {
            if(item.getFieldName().equals(name)) {
                return item;
            }
        }
        return null;
    }
    /** Add LCR Device object to SDK */
    private void doAddDevice() {
        if(lcrSdk == null) {
            return;
        }

        ConnectionOptions connectionOptions;

        // Check what type of connection to use

        connectionOptions = getWifiConnectionOptions(wifiIpAddress, wifiPort);

        // Set default text for device and network connection state

        // Synchronize way to add device (no callback need), using try-catch for error detection
        try {

            lcrSdk.addDevice(
                    getDeviceInfo(),
                    connectionOptions);

            /* !! NOTE !! Device add will be confirmed in DeviceListener */

        } catch (Exception e) {
            // Device add request fail
            String strError = "Device add request failed : " + e.getLocalizedMessage();
            Log.e("Main",strError);
        }
    }

    private DeviceInfo getDeviceInfo() {
        // Making device info with device id
        DeviceInfo returnDeviceInfo = new DeviceInfo(getDeviceId());

        // Set Device LCP address
        returnDeviceInfo.setDeviceAddress(lcpLCRAddress);

        // Set SDK LCP address
        returnDeviceInfo.setSdkAddress(lcpSDKAddress);

        return returnDeviceInfo;
    }
    @NonNull
    private String getDeviceId() {
        return this.deviceId;
    }

    private WiFiConnectionOptions getWifiConnectionOptions(String wifiIpAddress, int wifiPort) {

        return new WiFiConnectionOptions(
                // IP Address
                wifiIpAddress,
                // Port
                wifiPort);
    }
    private void addSDKListeners() {
        if(lcrSdk == null) {
            return;
        }
        // Device connection listener
        lcrSdk.addListener(deviceConnectionListener);
        // Field listener
        lcrSdk.addListener(fieldListener);
        // Command listener
        lcrSdk.addListener(commandListener);
        // Device status / state
        lcrSdk.addListener(deviceStatusListener);
        // Switch state listener
        lcrSdk.addListener(switchStateListener);
        // Printer status listener
        lcrSdk.addListener(printerStatusListener);

        // ** New listeners **
        // Add device communication listener
        lcrSdk.addListener(deviceCommunicationListener);
        // Device add/remove listener
        lcrSdk.addListener(deviceListener);
        // Network status listener (for logging purposes)
        lcrSdk.addListener(networkConnectionListener);

    }


    private String objToStrWithNullCheck(@Nullable Object valueToCheck) {
        return this.objToStrWithNullCheck(valueToCheck, "(null)");
    }

    /**
     * Small tool to check null values from objects.
     * @param valueToCheck	Object to check to check
     * @param valueIfNull	String to return if valueToCheck parameter is <code>null</code>
     * @return Checked value output as .toString() method call
     */
    @NonNull
    private String objToStrWithNullCheck(@Nullable Object valueToCheck, @NonNull String valueIfNull) {
        if(valueToCheck != null && valueToCheck.toString() != null) {
            return valueToCheck.toString();
        }
        return valueIfNull;
    }

    public DeviceListener deviceListener = new DeviceListener() {

        /**
         * Called when device add operation success
         * @param deviceId	Device identification
         */
        @Override
        public void onDeviceAddSuccess(@NonNull String deviceId) {


        }

        /**
         * Called when device add operation failed
         * @param deviceId	Device identification
         * @param cause		Cause of error
         */
        @Override
        public void onDeviceAddFailed(@NonNull String deviceId, SDKDeviceException cause) {
            String strCause = "(null)";
            if(cause != null) {
                strCause = cause.getMessage();
            }


        }

        /**
         * Called when device remove operation is success
         * @param deviceId	Device identification
         */
        @Override
        public void onDeviceRemoveSuccess(@NonNull String deviceId) {


        }

        /**
         * Called when device remove operation is failed
         * @param deviceId	Device identification
         * @param cause		Cause of error
         */
        @Override
        public void onDeviceRemoveFailed(@NonNull String deviceId, SDKDeviceException cause) {
            String strCause = "(null)";
            if(cause != null) {
                strCause = cause.getMessage();
            }


        }
    };
    /** Device connection listener */
    public DeviceConnectionListener deviceConnectionListener = new DeviceConnectionListener() {
        /**
         * Called when connection to LCR device is made with all relevant information.
         * @param deviceId 		Device identification string
         * @param deviceInfo	Device information
         */
        @Override
        public void deviceOnConnect(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo) {



            String logText = "Device on CONNECTED : "
                    + deviceId
                    + " LCP SDK Address : "
                    + deviceInfo.getSdkAddress().toString()
                    + " LCP Device Address : "
                    + deviceInfo.getDeviceAddress().toString();


            Log.d("DEVICE", logText);
        }
        /**
         * Called when device lost connection
         * @param deviceId		Device identification string
         * @param deviceInfo	Device information
         * @param cause			Reason for connection lost
         */
        @Override
        public void deviceOnDisconnect(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable Throwable cause) {



            // Log text about disconnecting
            String causeString = "unknown";
            if(cause != null) {
                causeString = cause.getLocalizedMessage();
            }

        }

        /**
         * Called when device connection enter in error state
         * @param deviceId		Device identification string
         * @param deviceInfo	Device information
         * @param cause			Cause of error
         */
        @Override
        public void deviceOnError(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable Throwable cause) {


            String errorMsg = "";
            if(cause != null) {
                errorMsg = objToStrWithNullCheck(cause.getLocalizedMessage());
            }

        }


        /**
         * Notify any status change events
         * @param deviceId		Device identification string
         * @param deviceInfo	Device information
         * @param newValue		New State
         * @param oldValue		Old State
         */
        @Override
        public void deviceConnectionStateChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable LCR_DEVICE_CONNECTION_STATE newValue,
                @Nullable LCR_DEVICE_CONNECTION_STATE oldValue) {


        }

        /**
         * Device network status changed
         * @param deviceId			Device identification string
         * @param deviceInfo		Device info
         * @param connectionOptions	Connection info
         * @param newValue			Network new State
         * @param oldValue			Network old state
         */
        @Override
        public void deviceNetworkStateChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable ConnectionOptions connectionOptions,
                @NonNull LCR_THREAD_CONNECTION_STATE newValue,
                @NonNull LCR_THREAD_CONNECTION_STATE oldValue) {

            Log.e("FMS", "Network connection state changed : " + oldValue + " -> " + newValue);
        }
    };
    /** Listener to handle all Field operation events */
    public FieldListener fieldListener = new FieldListener() {

        /**
         * onFieldInfoChanged event is activated when device field list is available or
         * field list status is changed.
         * Field Data read request can be done only for items what is in field list.
         *
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Device info
         * @param fields		List of available fields
         */
        @Override
        public void onFieldInfoChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable List<FieldItem> fields) {

            // Clear local list
            availableLCRFields.clear();
            if(fields != null) {
                String logText = "Field info arrived : " + fields.size() + " fields";

                // Add all list items to local list
                availableLCRFields.addAll(fields);
            }

        }

        /**
         * onFieldReadDataChanged event is activated when new data arrived in requested field
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Device info
         * @param responseField	Reply/response field with data
         * @param requestField 	Requested field info
         */
        @Override
        public void onFieldReadDataChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull ResponseField responseField,
                @NonNull RequestField requestField) {

            // Temporary variables for unit information
            String strMeasureUnit = "unknown";
            String strRateBaseUnit = "unknown";

            // For not log items what is displayed in separated fields
            Boolean showInLog = true;

            // Get measure unit
            if(responseField.getUnits().get(UNITS.MEASURE_UNIT) != null) {
                strMeasureUnit = responseField.getUnits().get(UNITS.MEASURE_UNIT).toLowerCase(Locale.getDefault());
            }
            // Get rate base unit (used in flow rate)
            if(responseField.getUnits().get(UNITS.RATE_BASE) != null) {
                strRateBaseUnit = responseField.getUnits().get(UNITS.RATE_BASE).toLowerCase(Locale.getDefault());
            }


            if(requestField.getItemToRequest().equals(grossQty)) {
                // Set logger off for this field
                showInLog = false;
                // Format setText string

            }
            if(requestField.getItemToRequest().equals(flowRate)) {
                // Set logger off for this field
                showInLog = false;
                // Format setText string

            }

            if(showInLog) {
                String logText = "Field data arrive : "
                        + responseField.getFieldItem().getFieldName()
                        + " - " + responseField.getOldValue()
                        + " -> "
                        + responseField.getNewValue();

                // Logging field data change event

            }
        }


        /**
         * Called when field data request run is success (data has received from LCR device)
         * (activated every time when data request has success, even received data values are same)
         * @param deviceId			Device Id
         * @param deviceInfo		Device info
         * @param responseField		Reply field with data
         * @param requestField		Requested field info
         */
        @Override
        public void onFieldDataRequestSuccess(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull ResponseField responseField,
                @NonNull RequestField requestField) {

            // Not logging this event

            /*
             * NOTE!
             * This event return field data request values (ResponseField), even data has not change
             */

        }

        /**
         * Called when field item data request is failed (Request data from LCR device is failed)
         * @param deviceId		Device Id
         * @param deviceInfo	Device info
         * @param requestField	Requested field info
         * @param cause			Error cause / message
         */
        @Override
        public void onFieldDataRequestFailed(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull RequestField requestField,
                @NonNull Throwable cause) {

            String logString;

            logString = String.format(Locale.getDefault(),
                    "Field data request failed %s\nCause : %s"
                    ,requestField.getItemToRequest().getFieldName()
                    ,cause.getLocalizedMessage());


        }

        /**
         * Called when field item data request {@link FIELD_REQUEST_STATES state} has changed
         * @param deviceId		Device Id
         * @param deviceInfo		Device info
         * @param requestField	Requested field info
         * @param newValue		New value of {@link FIELD_REQUEST_STATES}
         * @param oldValue		Old value of {@link FIELD_REQUEST_STATES}
         */
        @Override
        public void onFieldDataRequestStateChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull RequestField requestField,
                @NonNull FIELD_REQUEST_STATES newValue,
                @NonNull FIELD_REQUEST_STATES oldValue) {

        }

        /**
         * onFieldDataRequestAddSuccess event activated when new field data request add success
         * (activated only one time for each new field data request)
         * @param deviceId			Device Id
         * @param deviceInfo			Device info
         * @param requestField		Original requested field information
         * @param overWriteRequest	<code>true</code> if previous request from same field was replaced
         */
        @Override
        public void onFieldDataRequestAddSuccess(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull RequestField requestField,
                @NonNull Boolean overWriteRequest) {

            // Logging data request add success event
            Log.e("FMS", "Field data request add success : " + requestField.getItemToRequest().getFieldName());
        }

        /**
         * Called when new field data request task add failed
         * (activated only one time for each new field data request)
         * @param deviceId		Device Id
         * @param deviceInfo		Device info
         * @param requestField	Original requested field information
         * @param cause			Cause of error as throwable
         */
        @Override
        public void onFieldDataRequestAddFailed(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull RequestField requestField,
                @NonNull Throwable cause) {


        }

        /**
         * onFieldDataRequestRemoved event activated when field data request is removed
         * (activate only one time)
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Device info
         * @param requestField	Original requested field information
         * @param info			Information of remove (why removed)
         */
        @Override
        public void onFieldDataRequestRemoved(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull RequestField requestField,
                @NonNull String info) {

            Log.e("FMS","Field read data request removed : " + requestField.getItemToRequest().getFieldName() + " - " + info);
        }

        /**
         * onFieldWriteStatusChanged event is activated when write process status is changed
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Device info
         * @param fieldItem		Field information
         * @param data			Data to write into field
         * @param newValue		New Status
         * @param oldValue		Old Status
         */
        @Override
        public void onFieldWriteStatusChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull FieldItem fieldItem,
                @NonNull String data,
                @Nullable FIELD_WRITE_STATE newValue,
                @Nullable FIELD_WRITE_STATE oldValue) {


        }

        /**
         * onFieldWriteSuccess activated when write process is success to LCR device.
         * (activate only one time for each write process)
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Device Info
         * @param fieldItem		Field information
         * @param data			Data what is written to field
         */
        @Override
        public void onFieldWriteSuccess(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull FieldItem fieldItem,
                @NonNull String data) {


        }

        /**
         * onFieldWriteFailed activated when field write process has failed
         * (activate only one time for each write process)
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Device Info
         * @param fieldItem		Field info
         * @param data			Data what tried to write to field
         * @param cause			Cause of fail
         */
        @Override
        public void onFieldWriteFailed(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull FieldItem fieldItem,
                @NonNull String data,
                @Nullable Throwable cause) {

            String errorMsg = "(unknown)";
            if(cause != null) {
                errorMsg = cause.getLocalizedMessage();
            }

        }
    };

    /** Device command listener */
    public CommandListener commandListener = new CommandListener() {
        /**
         * onCommandStateChanged listener is activated when ever command state is changed
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Information about device
         * @param command		Command
         * @param newValue		New Value
         * @param oldValue		Old Value
         */
        @Override
        public void onCommandStateChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull LCR_COMMAND command,
                @Nullable COMMAND_STATE newValue,
                @Nullable COMMAND_STATE oldValue) {

            Log.e("FMS","Command state : " + oldValue + " -> " + newValue);
        }

        /**
         * onCommandSuccess listener is activated when command is run SUCCESSFULLY
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Information about device
         * @param command		Command
         */
        @Override
        public void onCommandSuccess(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull LCR_COMMAND command) {

            Log.e("FMS","Command success : " + command);
        }

        /**
         * onCommandFailed listener is activated when command is FAILED to run
         * @param deviceId		Device Id for using multiple devices same time
         * @param deviceInfo	Information about device
         * @param command		Command
         * @param cause			Cause for command fail (can be <code>null</code>)
         */
        @Override
        public void onCommandFailed(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull LCR_COMMAND command,
                @Nullable Throwable cause) {

            String errorMsg = "(unknown)";
            if(cause != null) {
                errorMsg = cause.getLocalizedMessage();
            }
            Log.e("FMS","Command failed : " + command + " Cause : " + errorMsg);
        }
    };



    /** Device operation switch state listener */
    public SwitchStateListener switchStateListener = new SwitchStateListener() {
        /**
         * Event of Switch State changed
         * @param deviceId		Device identification
         * @param deviceInfo	Device info
         * @param newValue		New switch state
         * @param oldValue		Old switch state
         */
        @Override
        public void onSwitchStateChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable LCR_SWITCH_STATE newValue,
                @Nullable LCR_SWITCH_STATE oldValue) {

            // Make variables for show status (getString formatting don't allow null)
            String newValueText = "(null)";
            if(newValue != null) {
                newValueText = newValue.toString();
            }

            Log.e("FMS","Switch state : " + oldValue + " -> " + newValue);
        }

    };

    /** Listener for most of Device status and state information */
    public DeviceStatusListener deviceStatusListener = new DeviceStatusListener() {

        /**
         * Event is activated when delivery active state is changed
         * @param deviceId				Device identification
         * @param deviceInfo			Device information
         * @param deliveryActiveState	Delivery active <code>true</code> delivery is active
         */
        @Override
        public void onDeliveryActiveStateChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull Boolean deliveryActiveState) {

            // Logging delivery active state
            Log.e("FMS","Delivery active state changed : " + deliveryActiveState);
        }

        /**
         * Event is activated when LCR Device {@link LCR_DEVICE_STATE state} is changed
         * @param deviceId		Device identification
         * @param deviceInfo	Device info
         * @param newValue		New device {@link LCR_DEVICE_STATE state}
         * @param oldValue		Old device {@link LCR_DEVICE_STATE state}
         */
        @Override
        public void onDeviceStateChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable LCR_DEVICE_STATE newValue,
                @Nullable LCR_DEVICE_STATE oldValue) {

            // Set device state text
            Log.e("FMS","Device state changed : " + oldValue + " -> " + newValue);
        }


        /**
         * Event is activated when one of {@link LCR_DELIVERY_CODE LCR_DELIVERY_CODE} status is changed.
         * Each delivery code has values of <code>null</code>, <code>true</code> or <code>false</code>
         * @param deviceId	Device identification
         * @param deviceInfo	Device info
         * @param code		Delivery code {@link LCR_DELIVERY_CODE}
         * @param newValue	New value
         * @param oldValue	Old value
         */
        @Override
        public void onDeliveryCodeChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull LCR_DELIVERY_CODE code,
                @Nullable Boolean newValue,
                @Nullable Boolean oldValue) {

             Log.e("FMS","Delivery code changed : " + code + " " + oldValue + " -> " + newValue);
        }

        /**
         * Event is activated when one of {@link LCR_DELIVERY_STATUS LCR_DELIVERY_STATUS} code is changed.
         * Each delivery status code has values of <code>null</code>, <code>true</code> or <code>false</code>
         * @param deviceId		Device identification
         * @param deviceInfo	Device info
         * @param code			{@link LCR_DELIVERY_STATUS LCR_DELIVERY_STATUS} code
         * @param newValue		New Value
         * @param oldValue		Old Value
         */
        public void onDeliveryStatusChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull LCR_DELIVERY_STATUS code,
                @Nullable Boolean newValue,
                @Nullable Boolean oldValue) {

            // Logging delivery status codes
            Log.e("FMS","Delivery Status changed : " + code + " " + objToStrWithNullCheck(oldValue) + " -> " + objToStrWithNullCheck(newValue));
        }

        /**
         * Event is activated when one of security level {@link LCR_SECURITY_LEVEL code} value is changed.
         * @param deviceId		Device identification
         * @param deviceInfo	Device info
         * @param newValue		New value of current security level code
         * @param oldValue		Old value of current security level code
         */
        public void onSecurityLevelChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable LCR_SECURITY_LEVEL newValue,
                @Nullable LCR_SECURITY_LEVEL oldValue) {

            // Logging security level (old security level -> new security level)
            Log.e("FMS","Security level changed : " + " " + oldValue + " -> " + newValue);
        }
    };

    /** Printer status monitoring */
    PrinterStatusListener printerStatusListener = new PrinterStatusListener() {
        /**
         * Event is activated when printer status is changed
         * @param DeviceID		Device Id
         * @param deviceInfo	Device info
         * @param statusCode	Printer status code
         * @param newValue		New state for current status code
         * @param oldValue		Old state for current status code
         */
        @Override
        public void onPrinterStatusChanged(
                @NonNull String DeviceID,
                @NonNull DeviceInfo deviceInfo,
                @NonNull LCR_PRINTER_STATUS statusCode,
                @Nullable Boolean newValue,
                @Nullable Boolean oldValue) {

            // Logging printer status codes and values
            Log.e("FMS","Printer Status : " + statusCode + " " + oldValue + " -> " + newValue);
        }

        /**
         * onPrintStatusChanged Listener is activated when print status for print item is changed.
         * Sta
         * @param DeviceId		Device identification
         * @param deviceInfo	Device information
         * @param workId		Print work identification
         * @param newValue		Print item new status
         * @param oldValue		Print item old status
         */
        @Override
        public void onPrintStatusChanged(
                @NonNull String DeviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull String workId,
                @Nullable PRINTING_STATE newValue,
                @Nullable PRINTING_STATE oldValue) {

            // Logging printing status change
            Log.e("FMS","Printing status : " + oldValue + " -> " + newValue);
        }

        /**
         * onPrintSuccess event is activated when print data is successfully send to LCR device.
         * @param deviceId		Device identification
         * @param deviceInfo	Device information
         * @param workId		Print work identification
         */
        @Override
        public void onPrintSuccess(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull String workId) {

            // Logging print success
            Log.e("FMS","Printing success");
        }

        /**
         * onPrintFailed event is activated when sending print data to LCR device has failed
         * @param deviceId		Device identification
         * @param deviceInfo	Device information
         * @param workId		Print work identification
         * @param cause			Error cause (note! can be <code>null</code>)
         */
        @Override
        public void onPrintFailed(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull String workId,
                @Nullable Throwable cause) {

            String strCause = "(null)";
            if(cause != null && cause.getMessage() != null) {
                strCause = String.format(
                        Locale.getDefault(),
                        "LCP Error : %s",
                        cause.getMessage());
            }
            // Logging print work failed
            Log.e("FMS","Print failed : " + strCause);
        }
    };

    /**
     * DeviceCommunicationListener report SDK communication information to LCR device
     */
    private DeviceCommunicationListener deviceCommunicationListener = new DeviceCommunicationListener() {

        /**
         * Notify when message state is changed between SDK and LCR device
         * NOTE! This listener update very high frequency
         * @param deviceId		Device identification string
         * @param deviceInfo	Device info
         * @param newValue		New value
         * @param oldValue		Old value
         */
        @Override
        public void onMessageStateChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable LCR_MESSAGE_STATE newValue,
                @Nullable LCR_MESSAGE_STATE oldValue) {

        }

        /**
         * Notify when SDK has detect communication error with LCR device.
         *
         * Also notify some important internal SDK errors (example. generated output message size errors).
         *
         * About event trace :
         * - Event trace start when device object get run turn from SDK DeviceRunner Thread
         * - Event list actions is add in key points of SDK and LCR device communicating
         * - Event list is cleared when device object finnish run or onCommunicationStatus error listeners is notified
         *
         * Event trace is not yet fully implemented inside SDK. More events will add recording later on.
         *
         * @param deviceId		Device identification string
         * @param deviceInfo	Device Info
         * @param cause			Special type of exception
         */
        @Override
        public void onCommunicationStatusError(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @NonNull LCRCommunicationException cause) {

            Log.e("ERROR_EVENT", "---------------------------------");
            Log.e("ERROR_EVENT", "Error event from : " + deviceId);
            Log.e("ERROR_EVENT", "-------- event data start--------");
            // Print events trace to lead up in error (not complete trace yet)
            Integer lineNumber = 1;
            for(InternalEvent event : cause.getEvents()) {
                // Print events (all type of events)
                Log.e("ERROR_EVENT", String.valueOf(lineNumber++) + " - " + objToStrWithNullCheck(event.toShortFormat()));
            }
            Log.e("ERROR_EVENT", "--------- event data end ---------");
        }

        /**
         * Notify when SDK communication status with LCR device is changed
         * !! NOTE !!
         * Not full implemented inside SDK
         * @param deviceId		Device identification string
         * @param deviceInfo	Device info
         * @param newValue		New Value
         * @param oldValue		Old Value
         */
        @Override
        public void onCommunicationStatusChanged(
                @NonNull String deviceId,
                @NonNull DeviceInfo deviceInfo,
                @Nullable LCR_COMMUNICATION_STATUS newValue,
                @Nullable LCR_COMMUNICATION_STATUS oldValue) {
        }
    };

    /** Monitor network connection states (for all devices) */
    public NetworkConnectionListener networkConnectionListener = new NetworkConnectionListener() {

        /**
         * Called when network connection state changed
         * @param networkType		Network type
         * @param connectionOptions	Connection options
         * @param attachedDevices	List of devices what is attach to network
         * @param newValue			New value
         * @param oldValue			Old Value
         */
        @Override
        public void onNetworkConnectionStateChange(
                @NonNull NETWORK_TYPE networkType,
                @NonNull ConnectionOptions connectionOptions,
                @NonNull List<DeviceInfo> attachedDevices,
                @Nullable LCR_THREAD_CONNECTION_STATE newValue,
                @Nullable LCR_THREAD_CONNECTION_STATE oldValue) {

            // Device level network status change is reported also in DeviceConnectionListener#deviceNetworkStateChanged
        }

        /**
         * Called when network connection is success
         * @param networkType		Network type
         * @param connectionOptions	Connection options
         * @param attachedDevices	Devices
         */
        @Override
        public void onNetworkConnected(
                @NonNull NETWORK_TYPE networkType,
                @NonNull ConnectionOptions connectionOptions,
                @NonNull List<DeviceInfo> attachedDevices) {

            // Logging network connect
            Log.e("FMS","Network disconnected : " + networkType.name());
        }

        /**
         * Called when network is disconnected
         * @param networkType		Network type
         * @param connectionOptions	Connection options
         * @param attachedDevices	Devices
         * @param cause				Cause of error
         */
        @Override
        public void onNetworkDisconnected(
                @NonNull NETWORK_TYPE networkType,
                @NonNull ConnectionOptions connectionOptions,
                @NonNull List<DeviceInfo> attachedDevices,
                @Nullable Throwable cause) {

            String strCause = "(null)";
            if(cause != null) {
                strCause = cause.getMessage();
            }
            // Logging network disconnecting
            Log.e("FMS","Network disconnected : " + networkType.name() + " : " + strCause);
        }

        /**
         * Called when network is on error (need user operations for recover)
         * @param networkType		Network type
         * @param connectionOptions	Connection options
         * @param attachedDevices	Devices
         * @param cause				Cause of error
         */
        @Override
        public void onNetworkError(
                @NonNull NETWORK_TYPE networkType,
                @NonNull ConnectionOptions connectionOptions,
                @NonNull List<DeviceInfo> attachedDevices,
                @Nullable Throwable cause) {

            String strCause = "(null)";
            if(cause != null) {
                strCause = cause.getMessage();
            }
            // Logging network error
            Log.e("FMS","Network error : " + networkType.name() + " : " + strCause);
        }
    };
    LCRDataModel model;
    Context activity;
    private void addListerners() {
        lcrReader.setStateListener(new LCRReader.LCRStateListener() {
            @Override
            public void onEndDelivery() {

            }

            @Override
            public void onStart() {
                findViewById(R.id.button2).setEnabled(false);
                findViewById(R.id.button3).setEnabled(true);

            }

            @Override
            public void onStop() {

                Toast.makeText(activity,"ON STOP " + model.getEndTime().toString(), Toast.LENGTH_LONG).show();
                finish();
            }
        });

        lcrReader.setFieldDataListener(new LCRReader.LCRDataListener() {
            @Override
            public void onDataChanged(LCRDataModel dataModel) {

                GsonBuilder builder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).setDateFormat("dd-MM-yyyy HH:mm:ss");
                Gson gson = builder.create();
                //((TextView)findViewById(R.id.textView11)).setText(gson.toJson(dataModel));
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
                ((TextView)findViewById(R.id.textView11)).setText(logText);
            }
        });

        lcrReader.setConnectionListener(new LCRReader.LCRConnectionListener() {
            @Override
            public void onConnected() {
                lcrReader.requestData();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((Button)findViewById(R.id.button2)).setEnabled(true);

                    }
                });
            }

            @Override
            public void onError() {

            }



            @Override
            public void onDeviceAdded(boolean failed) {

            }

            @Override
            public void onDisconnected() {
                lcrReader.stopRequestData();

            }
        });
    }

    LCRReader lcrReader ;
    private List<String> loggerList = new ArrayList<>();
    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
