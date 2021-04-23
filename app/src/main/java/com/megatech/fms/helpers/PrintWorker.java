package com.megatech.fms.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.megatech.fms.FMSApplication;
import com.megatech.fms.R;
import com.megatech.fms.model.InvoiceModel;
import com.megatech.tcpclient.TcpClient;
import com.megatech.tcpclient.TcpEvent;

import java.util.Observable;
import java.util.Observer;
import java.util.Queue;

public class PrintWorker implements Observer {
    public PrintWorker()
    {}
    private  Activity activity;
    public PrintWorker(Activity context)
    {
        activity = context;
    }

    public interface PrintStateListener{
        void onError();
        void onSuccess();
    }

    private PrintStateListener printStateListener;

    public PrintStateListener getPrintStateListener() {
        return printStateListener;
    }

    public void setPrintStateListener(PrintStateListener printStateListener) {
        this.printStateListener = printStateListener;
    }

    private void onError()
    {
        if (printStateListener !=null)
            printStateListener.onError();
    }

    private Queue<String> dataToPrint;

    private TcpClient mTcpClient;
    final int PRINTER_PORT = 9100;

    private void onSuccess()
    {
        if (printStateListener !=null)
            printStateListener.onSuccess();
        new Runnable() {
            @Override
            public void run() {
                mTcpClient.destroy();
            }
        }.run();
    }
    //RefuelItemData itemToPrint;

    private int printData() {
        //send the first data line and then remove from array
        if (dataToPrint.size()>0) {
            String line = dataToPrint.poll();
            //dataToPrint.remove(0);
            if (line != null)
                mTcpClient.sendMessage(line);
        }
        return  dataToPrint.size();
    }

    public  boolean printBill(InvoiceModel invoiceModel) {
        return printBill(invoiceModel, false);
    }

    public boolean printBill(InvoiceModel invoiceModel, boolean old) {

        dataToPrint = old ? invoiceModel.createBillTextOld() : invoiceModel.createBillText();
        if (dataToPrint == null)
            return false;

        try {
            String printerAddress = FMSApplication.getApplication().getPrinterAddress();
            this.mTcpClient = new TcpClient(printerAddress, PRINTER_PORT);
            this.mTcpClient.addObserver(this);
            this.mTcpClient.connect();

        } catch (Exception e) {
            Log.e("ERROR", e.toString());
            return false;
        }
        return true;
    }
    public  boolean printInvoice(InvoiceModel invoiceModel)
    {
        return printInvoice(invoiceModel, false);
    }

    public boolean printInvoice(InvoiceModel invoiceModel, boolean old) {
        dataToPrint = old ? invoiceModel.createInvoiceTextOld() : invoiceModel.createInvoiceText();
        if (dataToPrint == null)
            return false;
        try {
            String printerAddress = FMSApplication.getApplication().getPrinterAddress();
            this.mTcpClient = new TcpClient(printerAddress, PRINTER_PORT);
            this.mTcpClient.addObserver(this);
            this.mTcpClient.connect();

        }
        catch (Exception e)
        {
            Log.e("ERROR", e.toString());
            return false;
        }
        return true;
    }

    @Override
    public void update(Observable o, Object arg) {
        TcpEvent event = (TcpEvent)arg;
        switch (event.getTcpEventType()) {
            case CONNECTION_FAILED:
                activity.runOnUiThread(new Runnable() {
                       public void run() {
                            new AlertDialog.Builder(activity)
                                .setTitle("FMS Delivery")
                                .setMessage(activity.getString(R.string.printer_error))
                                .setIcon(R.drawable.ic_error)
                                .show();

                    }
                });
                //itemToPrint.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
                onError();
                break;
            case MESSAGE_RECEIVED:
                break;
            case CONNECTION_ESTABLISHED:
                // printer connected, start sending data line to print;
                printData();
                break;
            case MESSAGE_SENT:
                // data line sent to printer, send next line, if ZERO, no more data to send
                if (dataToPrint.size() ==0){
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, activity.getString(R.string.print_completed), Toast.LENGTH_LONG).show();
                        }
                    });
                    //itemToPrint.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.SUCCESS);
                    onSuccess();
                }
                else
                    printData() ;
                break;

        }
    }


    public enum PRINT_MODE
    {
        ONE_ITEM,
        ALL_ITEM
    }

    public enum PRINT_TEMPLATE
    {
        BILL,
        INVOICE
    }
}
