package com.megatech.fms.helpers;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import com.liquidcontrols.lcr.iq.sdk.BlueToothConnectionOptions;
import com.liquidcontrols.lcr.iq.sdk.ConnectionOptions;
import com.liquidcontrols.lcr.iq.sdk.DeviceInfo;
import com.liquidcontrols.lcr.iq.sdk.LCRCommunicationException;
import com.liquidcontrols.lcr.iq.sdk.LcrSdk;
import com.liquidcontrols.lcr.iq.sdk.FieldItem;

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
import com.liquidcontrols.lcr.iq.sdk.utils.TimeSet;
import com.megatech.fms.model.LCRDataModel;
import com.megatech.fms.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.text.NumberFormat;


public class LCRReader {

    private boolean deviceError = true;
    public boolean isDeviceError()
    {
        return  lcrSdk.getDeviceConnectionState(getDeviceId()) == LCR_DEVICE_CONNECTION_STATE.ERROR;
    }

    public boolean getConnected() {
        if (lcrSdk !=null)
        {
            return  lcrSdk.getDeviceConnectionState(getDeviceId()) == LCR_DEVICE_CONNECTION_STATE.CONNECTED;
        }
        else
            return false;
    }



    public LCRReader()
    {

    }
    public LCRReader(Context ctx)
    {
        this(ctx, "192.168.1.30", 10001);

    }
    public LCRReader(Context ctx,String ipAddress)
    {
        this(ctx, ipAddress, 10001);


    }
    public LCRReader(Context ctx,String ipAddress, int port)
    {
        this.dataListener = null;
        this.context = ctx;
        this.wifiIpAddress = ipAddress;
        this.wifiPort = port;
        initLCR();
    }
    private static LCRReader _reader;
    public static LCRReader create(Context ctx, String ip, int port, boolean renew) {

        if (_reader == null || renew)
            _reader = new LCRReader(ctx, ip, port);

        if (_reader.getConnected())
            _reader.onConnected();

        if (_reader.isDeviceError())
        {
            _reader.doConnectDevice();
        }

        _reader.reset();
        return _reader;
    }
    private void reset()
    {
        model = new LCRDataModel();
    }
    private void onDeviceAdded(boolean failed)
    {
        if (connectionListener!=null)
            connectionListener.onDeviceAdded(false);
        if (!failed)
            doConnectDevice();
    }
    private void onConnected() {
        lcrSdk.addListener(deviceStatusListener);
        if (connectionListener!=null)
            connectionListener.onConnected();
    }

    public void initLCR(){
        if (lcrSdk!=null)
            lcrSdk.disconnect(getDeviceId());
        lcrSdk = new LcrSdk(context);
        init();
    }
    private  Context context;

    public void destroy() {

            if(lcrSdk != null) {
                doDisconnectDevice();

                // Remove device
                lcrSdk.removeDevice(getDeviceId());
                // Remove used listeners
                lcrSdk.removeAllListeners();

                // Request SDK perform quit actions
                lcrSdk.quit();

            }

    }

    public void quit() {
        if (lcrSdk != null)
            lcrSdk.quit();
    }
    public interface LCRDataListener {
        void onDataChanged(LCRDataModel dataModel);
        void onErrorMessage(String errorMsg);
    }
    public interface  LCRConnectionListener    {
        void onConnected();
        void onError();
        void onDeviceAdded(boolean failed);
        void onDisconnected();
    }

    public interface  LCRStateListener{
        void onEndDelivery();
        void onStart();
        void onStop();
    }
    private LCRDataListener dataListener;

    public void setFieldDataListener(LCRDataListener listener)
    {
        this.dataListener = listener;
    }

    private LCRConnectionListener connectionListener;

    public void setConnectionListener(LCRConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    private LCRStateListener stateListener;
    public void setStateListener(LCRStateListener stateListener)
    {
        this.stateListener = stateListener;
    }
    private LCRDataModel model = new LCRDataModel();
    public void requestData()
    {
        grossQty = findUserFieldByName("GROSSQTY");
        grossMeter = findUserFieldByName("GROSSMETERQTY");
        endTime = findUserFieldByName("DELIVERYFINISH");
        startTime = findUserFieldByName("DELIVERYSTART");
        temp = findUserFieldByName("AVGTEMP");

        requestFieldData(grossQty);
        requestFieldData(endTime);
        requestFieldData(startTime);
        requestFieldData(grossMeter);
        requestFieldData(temp);

    }
    public void stopRequestData()
    {
        grossQty = findUserFieldByName("GROSSQTY");
        grossMeter = findUserFieldByName("GROSSMETERQTY");
        endTime = findUserFieldByName("DELIVERYFINISH");
        startTime = findUserFieldByName("DELIVERYSTART");
        temp = findUserFieldByName("AVGTEMP");

        removeFieldData(grossQty);
        removeFieldData(grossMeter);
        removeFieldData(endTime);
        removeFieldData(startTime);
        removeFieldData(temp);

    }
    private void requestFieldData(final FieldItem field) {
        if (field !=null)
        lcrSdk.requestFieldData(
                getDeviceId(),
                new RequestField(
                        field,
                        new TimeSet(1, TimeUnit.SECONDS)),
                new AsyncCallback() {
                    @Override
                    public void onAsyncReturn(@Nullable Throwable throwable) {
                        if(throwable != null) {
                            raiseError("SDK : Request command for field " + field.getFieldName() + " failed : " + throwable.getLocalizedMessage());
                        } else {
                            raiseError("SDK : Request command for field " + field.getFieldName() + " success");
                        }
                    }
                });
    }
    private void removeFieldData(final FieldItem field) {
        lcrSdk.removeFieldDataRequest(getDeviceId(), field);

    }


    // command methods
    public void start() {
        isStarted = false;
        isStopped = false;

        sendCommand(LCR_COMMAND.RUN);

    }
    private void onStarted()
    {
        isStarted = true;
        requestData();
        if (stateListener !=null ){
                stateListener.onStart();
        }
    }
    private void onStopped()
    {
        isStopped = false;
        stopRequestData();
        if (stateListener !=null ){
            stateListener.onStop();
        }

    }

    private void onEnded() {
        if (stateListener !=null ){

            stateListener.onEndDelivery();
        }
    }

    public void pause()
    {
        sendCommand(LCR_COMMAND.PAUSE);

    }
    private boolean isStopped = false;
    public void end()
    {
        isStopped = false;
        sendCommand(LCR_COMMAND.END_DELIVERY);
    }

    private void sendCommand(LCR_COMMAND command)
    {
        if (lcrSdk!=null) {
            LCR_DEVICE_CONNECTION_STATE state = lcrSdk.getDeviceConnectionState(getDeviceId());
            if (state == LCR_DEVICE_CONNECTION_STATE.CONNECTED)
                lcrSdk.addDeviceCommand(getDeviceId(), command);

            else
            {
                raiseError(context.getString(R.string.lcr_connection_error));
            }
        }

    }
    private void raiseError(String errorMsg)
    {
        Log.e("FMS",errorMsg);
        if (dataListener!=null)
            dataListener.onErrorMessage(errorMsg);

    }
    private enum DEVICE_ACTION {
        CONNECT,
        CONNECTING,
        DISCONNECT,
        DISCONNECTING
    }
    private enum CONNECTION_TYPE {
        BLUETOOTH,
        WIFI
    }

    private final String deviceId = "LCR.iQ";

    /** LCR device LCP protocol address */
    private final Integer lcpLCRAddress = 250;

    /** SDK LCP protocol address */
    private final Integer lcpSDKAddress = 20;

    /** Setup for WiFi connection */
    private  String wifiIpAddress = "192.168.1.30";
    private  Integer wifiPort = 10001;

    private final CONNECTION_TYPE connectionToUse = CONNECTION_TYPE.WIFI;

    private  LcrSdk lcrSdk;

    private final List<FieldItem> availableLCRFields = new ArrayList<>();


    /** LCR User fields to get data */
    private FieldItem grossQty = null;
    private FieldItem netQty = null;
    private FieldItem flowRate = null;
    private FieldItem temp = null;
    private FieldItem startTime = null;
    private FieldItem endTime = null;
    private FieldItem grossMeter = null;

    public void  init( )
    {

        Log.e("D", "call init()");
        lcrSdk.init(new AsyncCallback() {
            @Override
            public void onAsyncReturn(@Nullable Throwable error) {
                // Throwable only has data if error occurred
                if(error != null) {
                    // Error at init
                    String strError = "ERROR INIT SDK : " + error.getLocalizedMessage();
                    raiseError(strError);

                } else {
                    // Add listeners to receive data from SDK
                    addSDKListeners();
                    // Add device to communicate with
                    doAddDevice();
                }
            }
        });


    }
    private void doAddDevice(String ipAddress){
        if(lcrSdk == null) {
            return;

        }

        ConnectionOptions connectionOptions = null;

        // Check what type of connection to use
        if(connectionToUse.equals(CONNECTION_TYPE.WIFI)) {
            connectionOptions = getWifiConnectionOptions(ipAddress);
        } else {
        }
        // Set default text for device and network connection state

        try {

            lcrSdk.addDevice(
                    getDeviceInfo(),
                    connectionOptions);

            /* !! NOTE !! Device add will be confirmed in DeviceListener */

        } catch (Exception e) {
            // Device add request fail
            String strError = "Device add request failed : " + e.getLocalizedMessage();
            raiseError(strError);
        }
    }
    private void doAddDevice() {
        doAddDevice(wifiIpAddress);
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
    private WiFiConnectionOptions getWifiConnectionOptions(String lcrIpAddress) {
        return new WiFiConnectionOptions(
            // IP Address
            lcrIpAddress,
            // Port
            wifiPort);


    }
    private WiFiConnectionOptions getWifiConnectionOptions() {

        return new WiFiConnectionOptions(
                // IP Address
                wifiIpAddress,
                // Port
                wifiPort);
    }
    public void doDisconnectDevice() {
        // Check SDK object (if call this method after close object)
        if(lcrSdk == null) {
            return;
        }
        // Call SDK to make disconnect command for device
        lcrSdk.disconnect(getDeviceId());

    }

    /** Request SDK to connect to device */
    public void doConnectDevice() {
        // Check SDK object (if call this method after close object)
        if(lcrSdk == null) {
            return;
        }
        // Call SDK to make connect
        lcrSdk.connect(getDeviceId());
    }

    private String getDeviceId() {
        return this.deviceId;
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

    public FieldItem findUserFieldByName(@NonNull String name) {
        for(FieldItem item : availableLCRFields) {
            //Log.d("FIELD",item.getFieldName());
            if(item.getFieldName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    private String objToStrWithNullCheck(@Nullable Object valueToCheck, @NonNull String valueIfNull) {
        if(valueToCheck != null && valueToCheck.toString() != null) {
            return valueToCheck.toString();
        }
        return valueIfNull;
    }
    private String objToStrWithNullCheck(@Nullable Object valueToCheck) {
        return this.objToStrWithNullCheck(valueToCheck, "(null)");
    }
    /** Device add / remove listener */
    public DeviceListener deviceListener = new DeviceListener() {

        /**
         * Called when device add operation success
         * @param deviceId	Device identification
         */
        @Override
        public void onDeviceAddSuccess(@NonNull String deviceId) {

            // Logging success
            raiseError("Add device success : " + deviceId);

            // Set user interface objects for device add success
            //doUIActionsForDeviceAddSuccess();
            onDeviceAdded(false);
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
            // Logging add device error
            raiseError("Add device failed : " + strCause);
            onDeviceAdded(true);
        }

        /**
         * Called when device remove operation is success
         * @param deviceId	Device identification
         */
        @Override
        public void onDeviceRemoveSuccess(@NonNull String deviceId) {
            // Logging actions
            raiseError("Remove device success");

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
            // Logging remove device error
            raiseError("Remove device failed : " + strCause);

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

            // Set user interface for connected state
            //doUIActionsForDeviceConnected();

            String logText = "Device on CONNECTED : "
                    + deviceId
                    + " LCP SDK Address : "
                    + deviceInfo.getSdkAddress().toString()
                    + " LCP Device Address : "
                    + deviceInfo.getDeviceAddress().toString();

            raiseError(logText);
            onConnected();
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

            // Set user interface for disconnected state
            //doUIActionsForDeviceDisconnected();

            // Log text about disconnecting
            String causeString = "unknown";
            if(cause != null) {
                causeString = cause.getLocalizedMessage();
            }
            raiseError("Device on DISCONNECTED : " + deviceId + " Cause : " + causeString);
            if (connectionListener!=null)
                connectionListener.onDisconnected();
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

            // Set user interface for error connected state
            //doUIActionsForDeviceError();

            String errorMsg = "";
            if(cause != null) {
                errorMsg = cause.getLocalizedMessage();
            }
            raiseError("Device on ERROR : " + deviceId + " Cause : " + errorMsg);
            if (connectionListener!=null)
                connectionListener.onError();
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

            //textViewDeviceConnectionStateData.setText(objToStrWithNullCheck(newValue));

            raiseError("Device connection state changed : " + oldValue + " -> " + newValue);
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

            if(connectionOptions != null) {
                if(connectionOptions instanceof BlueToothConnectionOptions) {
                    //textViewNetworkTypeData.setText(R.string.text_network_type_bluetooth);
                } else if(connectionOptions instanceof WiFiConnectionOptions) {
                    //textViewNetworkTypeData.setText(R.string.text_network_type_wifi);
                } else {
                    //textViewNetworkTypeData.setText(R.string.text_network_type_unknown);
                }
            } else {
                //textViewNetworkTypeData.setText(R.string.text_network_type_unknown);
            }
            //textViewNetworkConnectionStateData.setText(newValue.toString());

            raiseError("Network connection state changed : " + oldValue + " -> " + newValue);
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
                raiseError(logText);
                // Add all list items to local list
                availableLCRFields.addAll(fields);
            }
            // Check GROSS_PRESET access and set UI Components
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

            Locale locale = Locale.getDefault();
            NumberFormat numberFormat = NumberFormat.getInstance(locale);


            // For not log items what is displayed in separated fields
            Boolean showInLog = true;
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

            // Get data to show in text views

            if(requestField.getItemToRequest().equals(grossQty)) {
                // Set logger off for this field
                showInLog = false;
                // Format setText string
                if (dataListener!=null) {
                    try {
                        model.setGrossQty(numberFormat.parse(responseField.getNewValue()).floatValue());
                        dataListener.onDataChanged(model);
                    } catch (ParseException e) {
                    }
                }
            }
            if(requestField.getItemToRequest().equals(startTime)) {
                // Set logger off for this field
                showInLog = false;
                // Format setText string
                if (dataListener!=null) {
                    try {
                        model.setStartTime(dateFormat.parse(responseField.getNewValue()));
                        dataListener.onDataChanged(model);
                    } catch (ParseException e) {
                    }
                }
            }
            if(requestField.getItemToRequest().equals(endTime)) {
                // Set logger off for this field
                showInLog = false;
                if (dataListener!=null) {
                    try {
                        model.setEndTime(dateFormat.parse(responseField.getNewValue()));
                        dataListener.onDataChanged(model);
                        if (isStopped)
                            onStopped();
                    } catch (ParseException e) {
                    }
                }
            }

            if(requestField.getItemToRequest().equals(grossMeter)) {
                // Set logger off for this field
                showInLog = false;
                // Format setText string
                if (dataListener!=null) {
                    try {
                        if (model.getStartMeterNumber() <= 0)
                            model.setStartMeterNumber(numberFormat.parse(responseField.getNewValue()).floatValue());

                        model.setEndMeterNumber(numberFormat.parse(responseField.getNewValue()).floatValue());
                        dataListener.onDataChanged(model);
                    } catch (ParseException e)
                    {}

                }
            }
            if(requestField.getItemToRequest().equals(temp)) {
                // Set logger off for this field
                showInLog = false;
                // Format setText string
                if (dataListener!=null) {
                    try {
                        model.setTemperature(numberFormat.parse(responseField.getNewValue()).floatValue());
                        dataListener.onDataChanged(model);
                    } catch (ParseException e)
                    {}
                }
            }
            if(showInLog) {
                String logText = "Field data arrive : "
                        + responseField.getFieldItem().getFieldName()
                        + " - " + responseField.getOldValue()
                        + " -> "
                        + responseField.getNewValue();

                // Logging field data change event
                raiseError(logText);
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

            // Write log text
            raiseError(logString);
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
            raiseError("Field data request add success : " + requestField.getItemToRequest().getFieldName());
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

            // Logging data request add failed
            raiseError("Field data request add failed : "
                    + requestField.getItemToRequest().getFieldName()
                    + "\nCause :"
                    + cause.getLocalizedMessage());
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

            raiseError("Field read data request removed : " + requestField.getItemToRequest().getFieldName() + " - " + info);
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

            raiseError("Write field state : " + oldValue + " -> " + newValue);
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

            raiseError("Field data write success : " + fieldItem.getFieldName());
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
            raiseError("Field data write failed : " + fieldItem.getFieldName() + " Cause : " + errorMsg);
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

            raiseError("Command state : " + oldValue + " -> " + newValue);
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

            raiseError("Command success : " + command);




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
            raiseError("Command failed : " + command + " Cause : " + errorMsg);
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

            // Set switch state text

            raiseError("Switch state : " + oldValue + " -> " + newValue);
        }

    };
    private boolean isStarted = false;
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
            raiseError("Delivery active state changed : " + deliveryActiveState);
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

            // Logging device state
            raiseError("Device state changed : " + oldValue + " -> " + newValue);

            if (newValue == LCR_DEVICE_STATE.STATE_RUN)
                onStarted();
            if (newValue == LCR_DEVICE_STATE.STATE_END_DELIVERY)
                isStopped = true && isStarted;

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

            // Check out FLOW_ACTIVE event
            if(code.equals(LCR_DELIVERY_CODE.FLOW_ACTIVE)) {
                if(newValue == null) {
                    //textViewFlowStateData.setText(R.string.text_state_unknown);
                } else if(newValue) {
                    //textViewFlowStateData.setText(R.string.text_flow_state_on);
                } else {
                    //textViewFlowStateData.setText(R.string.text_flow_state_off);
                }
            }
            // Logging delivery code status
            raiseError("Delivery code changed : " + code + " " + oldValue + " -> " + newValue);
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
            raiseError("Delivery Status changed : " + code + " " + objToStrWithNullCheck(oldValue) + " -> " + objToStrWithNullCheck(newValue));
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
            raiseError("Security level changed : " + " " + oldValue + " -> " + newValue);
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
            raiseError("Printer Status : " + statusCode + " " + oldValue + " -> " + newValue);
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
            raiseError("Printing status : " + oldValue + " -> " + newValue);
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
            raiseError("Printing success");
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
            raiseError("Print failed : " + strCause);
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
                Log.e("ERROR_EVENT", lineNumber++ + " - " + event.getData());
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
            raiseError("Network disconnected : " + networkType.name());
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
            raiseError("Network disconnected : " + networkType.name() + " : " + strCause);
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
            raiseError("Network error : " + networkType.name() + " : " + strCause);
        }
    };
}
