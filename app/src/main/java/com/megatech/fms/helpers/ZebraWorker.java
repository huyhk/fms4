package com.megatech.fms.helpers;

import android.content.Context;
import android.util.Log;

import com.megatech.fms.model.ReceiptModel;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;
import com.zebra.sdk.printer.discovery.BluetoothDiscoverer;
import com.zebra.sdk.printer.discovery.DiscoveredPrinter;
import com.zebra.sdk.printer.discovery.DiscoveryHandler;

import java.io.File;

public class ZebraWorker {

    public ZebraWorker(Context ctx)
    {
        context = ctx;
        findPrinter(null);
    }

    private void findPrinter(ReceiptModel model) {
        try {

            BluetoothDiscoverer.findPrinters(context, new DiscoveryHandler() {
                @Override
                public void foundPrinter(DiscoveredPrinter discoveredPrinter) {
                    printer = discoveredPrinter;
                }

                @Override
                public void discoveryFinished() {
                    if (printer != null) {
                        if (model != null)
                            print(model);
                    } else {
                        if (stateListener != null)
                            stateListener.onConnectionError();
                    }
                }

                @Override
                public void discoveryError(String s) {
                    if (stateListener != null)
                        stateListener.onConnectionError();
                }
            });
        }catch (Exception ex)
        {
            if (stateListener != null)
                stateListener.onConnectionError();
        }
    }

    Context context;
    DiscoveredPrinter printer ;

    public void printReceipt(ReceiptModel receiptModel)
    {

        if (printer == null)
            findPrinter(receiptModel);
        else
            print(receiptModel);
    }
    private  void print(ReceiptModel receiptModel)
    {
        try {
            con = printer.getConnection();
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
            if (stateListener != null)
                stateListener.onSuccess();
        }
        catch (Exception ex)
        {
            if (stateListener!=null)
                stateListener.onError();
        }

    }
    Connection con;
    public void print(String zpl) {

        if (printer != null) {
            try {

                con.write(zpl.getBytes());


            } catch (Exception ex) {
                Log.e("ZEBRA", ex.getMessage());
            }
        } else {
            if (stateListener != null)
                stateListener.onConnectionError();
        }

    }

    public interface ZebraStateListener{
        void onConnectionError();
        void onError();
        void onSuccess();
    }

    private  ZebraStateListener stateListener;

    public ZebraStateListener getStateListener() {
        return stateListener;
    }

    public void setStateListener(ZebraStateListener stateListener) {
        this.stateListener = stateListener;
    }
}
