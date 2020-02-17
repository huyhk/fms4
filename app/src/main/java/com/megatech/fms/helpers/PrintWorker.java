package com.megatech.fms.helpers;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;


import com.megatech.fms.FMSApplication;
import com.megatech.fms.model.FlightData;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.tcpclient.TcpClient;
import com.megatech.tcpclient.TcpEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.Observer;

public class PrintWorker implements Observer {
    public PrintWorker()
    {}
    private  Activity activity;
    public PrintWorker(Activity context)
    {
        activity = context;
    }

    private TcpClient mTcpClient;
    final int PRINTER_PORT = 9100;
    private ArrayList<String> dataToPrint;
    RefuelItemData itemToPrint;
    public boolean printItem(RefuelItemData mItem)
    {
        return  this.printItem(mItem, false);
    }
    public boolean printItem(RefuelItemData mItem, boolean printInvoice) {

        String truckInfo = String.format("    %s           %.0f    %.0f                    %.2f\n",mItem.getTruckNo()  ,mItem.getStartNumber(), mItem.getEndNumber(), mItem.getVolume());
        double realAmount = mItem.getRealAmount();
        double saleAmount = mItem.getAmount();
        double volume = mItem.getVolume();
        double weight = mItem.getWeight();
        double taxRate = mItem.getTaxRate();
        double vatAmount = mItem.getVATAmount();
        double totalSaleAmount = mItem.getTotalAmount();
        double price = mItem.getPrice();

        if (printInvoice){
            if (mItem.getOthers().size()>0)
            {
                for (int i = 0; i < mItem.getOthers().size() ; i++) {
                    truckInfo += String.format("\n    %s           %.0f    %.0f                    %.2f\n",mItem.getOthers().get(i).getTruckNo()  ,mItem.getOthers().get(i).getStartNumber(), mItem.getOthers().get(i).getEndNumber(), mItem.getOthers().get(i).getVolume());
                    realAmount += mItem.getOthers().get(i).getRealAmount();
                    saleAmount += mItem.getOthers().get(i).getAmount();
                    volume += mItem.getOthers().get(i).getVolume();
                    weight += mItem.getOthers().get(i).getWeight();
                    vatAmount += mItem.getOthers().get(i).getVATAmount();
                    totalSaleAmount += mItem.getOthers().get(i).getTotalAmount();

                }
            }
            truckInfo += new String(new char[4-mItem.getOthers().size()]).replace('\0','\n');
        }
        else
            truckInfo +=" \n \n \n \n \n";

        itemToPrint = mItem;
        FMSApplication app = FMSApplication.getApplication();
        SimpleDateFormat format = new SimpleDateFormat("          dd           MM         yyyy\n");
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
        dataToPrint = new ArrayList<String>(Arrays.asList(
                new String(new char[]{27,51,15}),
                "\n",
                new String(new char[]{27,51,42}),
                format.format(mItem.getStartTime()) + "\n",
                new String(new char[]{27,51,20}),
                String.format("                         %s\n",mItem.getQualityNo()),
                String.format("                       %s        %s                 %s\n",timeformat.format((mItem.getStartTime())) ,timeformat.format(mItem.getEndTime()),VNCharacterUtils.removeAccent(mItem.getProductName())),

                new String(new char[]{27,51,8}),
                "\n",
                new String(new char[]{27,51,14}),
                //String.format("    %s           %.0f    %.0f                    %.2f\n",mItem.getTruckNo()  ,mItem.getStartNumber(), mItem.getEndNumber(), mItem.getVolume()),
                truckInfo,

                String.format("                   %s\n",VNCharacterUtils.removeAccent(mItem.getAirlineModel().getName())),
                String.format("                   %s\n",mItem.getAirlineModel().getTaxCode()),
                String.format("             %s\n",VNCharacterUtils.removeAccent(mItem.getAirlineModel().getAddress())),
                String.format("                    %-10s                          %s\n", mItem.getAircraftType(),mItem.getAircraftCode()),
                String.format("                    %-10s                       %s\n",mItem.getFlightCode(),mItem.getRouteName()),
                String.format("                            %.2f                       %.2f\n",mItem.getManualTemperature(), mItem.getDensity()),
                String.format("                 %.2f                           %.2f\n",volume, weight),
                String.format("                   %.2f                       %,.2f\n",realAmount, price),
                String.format("                         %,.2f\n",mItem.getAmount()),
                String.format("                 %.0f%%                                 %,.2f\n",taxRate*100,vatAmount),

                String.format("                         %,.2f                     \n",totalSaleAmount),
                String.format("                 %s                     \n",VNCharacterUtils.removeAccent( ReadNumber.numberToString(totalSaleAmount)))


        ));
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
    private int printData() {
        //send the first data line and then remove from array
        if (dataToPrint.size()>0) {
            mTcpClient.sendMessage(dataToPrint.get(0));
            dataToPrint.remove(0);
        }
        return  dataToPrint.size();
    }

    @Override
    public void update(Observable o, Object arg) {
        TcpEvent event = (TcpEvent)arg;
        switch (event.getTcpEventType()) {
            case CONNECTION_FAILED:
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(activity, "ERROR: Printer connection failed ", Toast.LENGTH_LONG).show();
                    }
                });
                itemToPrint.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.ERROR);
                break;
            case MESSAGE_RECEIVED:
                break;
            case CONNECTION_ESTABLISHED:
                // printer connected, start sending data line to print;
                printData();
                break;
            case MESSAGE_SENT:
                // data lin sent to printer, send next line, if ZERO, no more data to send
                if (printData() == 0){
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, "INFO: Sending data to printer completed", Toast.LENGTH_LONG).show();
                        }
                    });
                    itemToPrint.setPrintStatus(RefuelItemData.ITEM_PRINT_STATUS.SUCCESS);
                }
                break;
        }
    }

    public boolean printInvoice(FlightData flightData) {

        SimpleDateFormat format = new SimpleDateFormat("            dd         MM         yyyy\n");
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");


        return true;

    }
}
