package com.megatech.fms.helpers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.megatech.fms.model.ReceiptModel;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveredPrinterBluetooth;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.io.File;

public class ZebraWorker {

    public ZebraWorker(Context ctx)
    {
        context = ctx;
        findPrinter(null);
    }

    private void saveAddress(String macAddress)
    {
        final SharedPreferences preferences = context.getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("ZEBRA_MAC_ADDRESS", macAddress);
        editor.apply();
    }
    private void clearAddress()
    {
        final SharedPreferences preferences = context.getSharedPreferences("FMS", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("ZEBRA_MAC_ADDRESS");
        editor.apply();
    }
    private String getAddress()
    {
        final SharedPreferences preferences = context.getSharedPreferences("FMS", MODE_PRIVATE);
        return preferences.getString("ZEBRA_MAC_ADDRESS",null);
    }

    private void findPrinter(ReceiptModel model) {
        try {
            String macAddress = getAddress();
            if (macAddress != null)
            {
                con = new BluetoothConnection(macAddress);
                if (model!=null)
                    print(model);
                return;
            }
            BluetoothDiscoverer.findPrinters(context, new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    printer = discoveredPrinter;
                    printerBluetooth = (DiscoveredPrinterBluetooth) printer;
                    if (printerBluetooth!=null)
                        saveAddress(printerBluetooth.address);

                }

                @Override
                public void discoveryFinished() {
                    if (printer != null) {
                        con = printer.getConnection();
                        if (model != null)
                            print(model);
                    } else {
                        if (model!=null)
                        onConnectionError();
                    }
                }

                @Override
                public void discoveryError(String s) {
                    if (model!=null)
                        onConnectionError();
                }
            });
        }catch (Exception ex)
        {
            if (model!=null)
                onConnectionError();
        }
    }

    Context context;
    DiscoveredPrinter printer ;

    DiscoveredPrinterBluetooth printerBluetooth;

    public void printReceipt(ReceiptModel receiptModel)
    {

        if ((con == null || !con.isConnected()) && printer == null)
            findPrinter(receiptModel);
        else {
            if (con == null || !con.isConnected())
                con = printer.getConnection();
            print(receiptModel);
        }
    }
    private  void print(ReceiptModel receiptModel)
    {
        try {

            con.open();
            String zpl = receiptModel.isReturn()? receiptModel.createReturnThermalText(): receiptModel.createThermalText();

            if (receiptModel.getSignaturePath() != null) {
                File f = new File(receiptModel.getSignaturePath());
                if (f.exists()) {
                    ZebraPrinter zebraPrinter = ZebraPrinterFactory.getInstance(con);
                    zebraPrinter.storeImage("E:BUYER.GRF", f.getAbsolutePath(), 300, 200);
                }

            }
            if (receiptModel.getSellerSignaturePath() != null) {
                File f = new File(receiptModel.getSellerSignaturePath());
                if (f.exists()) {
                    ZebraPrinter zebraPrinter = ZebraPrinterFactory.getInstance(con);
                    zebraPrinter.storeImage("E:SELLER.GRF", f.getAbsolutePath(), 300, 200);

                }

            }

            print(zpl);

            con.close();
            onSuccess();
        }
        catch (Exception ex)
        {
            clearAddress();
            onError();
        }

    }
    Connection con;
    public void print(String zpl) {

        if (con != null && con.isConnected()) {
            try {

                con.write(zpl.getBytes());


            } catch (Exception ex) {
                Log.e("ZEBRA", ex.getMessage());
            }
        } else {
            onConnectionError();
        }

    }

    public interface ZebraStateListener{
        void onConnectionError();
        void onError();
        void onSuccess();
    }

    private void onConnectionError(){
        if (stateListener!=null)
            stateListener.onConnectionError();
    }
    private void onError() {
        if (stateListener != null)
            stateListener.onError();
    }
    private void onSuccess() {
        if (stateListener != null)
            stateListener.onSuccess();
    }
    private  ZebraStateListener stateListener;

    public ZebraStateListener getStateListener() {
        return stateListener;
    }

    public void setStateListener(ZebraStateListener stateListener) {
        this.stateListener = stateListener;
    }
}
