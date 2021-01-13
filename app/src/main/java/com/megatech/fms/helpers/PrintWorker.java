package com.megatech.fms.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;


import com.megatech.fms.FMSApplication;
import com.megatech.fms.R;
import com.megatech.fms.model.FlightData;
import com.megatech.fms.model.InvoiceModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.tcpclient.TcpClient;
import com.megatech.tcpclient.TcpEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import static com.megatech.fms.model.RefuelItemData.GALLON_TO_LITTER;

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
    private void onSuccess()
    {
        if (printStateListener !=null)
            printStateListener.onSuccess();
    }

    private TcpClient mTcpClient;
    final int PRINTER_PORT = 9100;
    private ArrayList<String> dataToPrint;
    //RefuelItemData itemToPrint;
    public boolean printItem(RefuelItemData mItem)
    {
        return this.printItem(mItem, PRINT_MODE.ALL_ITEM, PRINT_TEMPLATE.INVOICE);
    }
    public boolean printItem(RefuelItemData mItem, PRINT_MODE mode, PRINT_TEMPLATE template){
        if (template == PRINT_TEMPLATE.BILL)
            return printBill(mItem, mode);
        else
            return printInvoice(mItem, mode);
    }

    public boolean printInvoice(RefuelItemData mItem, PRINT_MODE mode) {

        Locale locale = new Locale("vi", "VN");
        Locale.setDefault(locale);
        String truckInfo = String.format("%12s %,15.0f / %,-15.0f   %,12.0f\n", mItem.getTruckNo(), mItem.getStartNumber(), mItem.getStartNumber() + mItem.getRealAmount(), mItem.getVolume());
        double realAmount = mItem.getRealAmount();
        double saleAmount = mItem.getAmount();
        double volume = mItem.getVolume();
        double weight = mItem.getWeight();
        double taxRate = mItem.getTaxRate();
        double vatAmount = mItem.getVATAmount();
        double totalSaleAmount = mItem.getTotalAmount();
        double price = mItem.getPrice();
        double temperature = mItem.getManualTemperature();
        double density = mItem.getDensity();

        double totalT = volume;
        double totalP = temperature * volume;

        if (mode == PRINT_MODE.ALL_ITEM){
            if (mItem.getOthers().size()>0)
            {
                for (int i = 0; i < mItem.getOthers().size() ; i++) {
                    truckInfo += String.format("\n%12s %,15.0f / %,-15.0f   %,12.0f\n", mItem.getOthers().get(i).getTruckNo(), mItem.getOthers().get(i).getStartNumber(), mItem.getOthers().get(i).getStartNumber() + mItem.getOthers().get(i).getRealAmount(), mItem.getOthers().get(i).getVolume());
                    realAmount += mItem.getOthers().get(i).getRealAmount();
                    saleAmount += mItem.getOthers().get(i).getAmount();
                    volume += mItem.getOthers().get(i).getVolume();
                    weight += mItem.getOthers().get(i).getWeight();
                    vatAmount += mItem.getOthers().get(i).getVATAmount();
                    totalSaleAmount += mItem.getOthers().get(i).getTotalAmount();
                    totalT += mItem.getOthers().get(i).getVolume();
                    totalP += mItem.getOthers().get(i).getManualTemperature() * mItem.getOthers().get(i).getVolume();
                }
                density = weight / volume;
                temperature = totalP / totalT;

            }

            truckInfo += new String(new char[4-mItem.getOthers().size()]).replace('\0','\n');
        }
        else
            truckInfo +=" \n \n \n \n \n";

        //itemToPrint = mItem;
        FMSApplication app = FMSApplication.getApplication();
        SimpleDateFormat format = new SimpleDateFormat("          dd           MM         yyyy\n");
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
        dataToPrint = new ArrayList<String>(Arrays.asList(
                new String(new char[]{27,51,15}),
                new String(new char[]{27,51,42}),
                format.format(mItem.getStartTime()) + "\n",
                new String(new char[]{27,51,20}),
                String.format("                         %s\n",mItem.getQualityNo()),
                String.format("                       %s  %s                       %s\n", timeformat.format((mItem.getStartTime())), timeformat.format(mItem.getEndTime()), VNCharacterUtils.removeAccent(mItem.getProductName())),

                new String(new char[]{27,51,8}),
                "\n",
                new String(new char[]{27,51,14}),
                //String.format("    %s           %.0f    %.0f                    %.2f\n",mItem.getTruckNo()  ,mItem.getStartNumber(), mItem.getEndNumber(), mItem.getVolume()),
                truckInfo,

                String.format("                   %s\n", VNCharacterUtils.removeAccent(mItem.getAirlineModel().getInvoiceName())),
                String.format("                   %s\n", mItem.getAirlineModel().getInvoiceTaxCode()),
                String.format("             %s\n", VNCharacterUtils.removeAccent(mItem.getAirlineModel().getInvoiceAddress())),
                String.format("                    %-10s                          %s\n", mItem.getAircraftType(),mItem.getAircraftCode()),
                String.format("                    %-10s                       %s\n",mItem.getFlightCode(),mItem.getRouteName()),
                String.format("                            %.2f                       %.4f\n", temperature, density),
                String.format("                 %,.0f                                %,.0f\n", volume, weight),
                String.format("                   %,.0f                            %,.0f\n", realAmount, price),
                String.format("                         %,.0f\n", mItem.getAmount()),
                String.format("                 %,.0f%%                                 %,.0f\n", taxRate * 100, vatAmount),

                String.format("                         %,.0f                     \n", totalSaleAmount),
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

    public boolean printBill(RefuelItemData mItem, PRINT_MODE mode) {

        Locale locale = new Locale("vi", "VN");
        Locale.setDefault(locale);
        String truckInfo = "";
        if (mItem.getReturnAmount() ==0)
            truckInfo = String.format("%12s %,15.0f / %,-15.0f   %,12.0f\n", mItem.getTruckNo(), mItem.getStartNumber(), mItem.getStartNumber() + mItem.getRealAmount(), mItem.getVolume());
        else
        {
            double returnAmount = mItem.getReturnAmount();
            double returnVolume = mItem.getDensity()>0? (returnAmount / mItem.getDensity()):0;
            double returnGallon = returnVolume / GALLON_TO_LITTER;
            double endNumber = Math.round(mItem.getStartNumber() + returnGallon);

            truckInfo = String.format("%12s %,15.0f / %,-15.0f HT  %,10.0f\n", mItem.getTruckNo(), mItem.getStartNumber(), endNumber , returnVolume);

            truckInfo += String.format("\n%12s %,15.0f / %,-15.0f   %,12.0f\n", mItem.getTruckNo(), endNumber, mItem.getStartNumber() + mItem.getRealAmount() , mItem.getVolume() - returnVolume);

        }
        double realAmount = mItem.getRealAmount();
        double saleAmount = mItem.getAmount();
        double volume = mItem.getVolume();
        double weight = mItem.getWeight();
        double taxRate = mItem.getTaxRate();
        double vatAmount = mItem.getVATAmount();
        double totalSaleAmount = mItem.getTotalAmount();
        double price = mItem.getPrice();
        double temperature = mItem.getManualTemperature();
        double density = mItem.getDensity();

        double totalT = volume;
        double totalP = temperature * volume;


        if (mode == PRINT_MODE.ALL_ITEM){
            if (mItem.getOthers().size()>0)
            {
                for (int i = 0; i < mItem.getOthers().size() ; i++) {
                    truckInfo += String.format("\n%12s %,15.0f / %,-15.0f   %,12.0f\n", mItem.getOthers().get(i).getTruckNo(), mItem.getOthers().get(i).getStartNumber(), mItem.getOthers().get(i).getStartNumber() + mItem.getOthers().get(i).getRealAmount(), mItem.getOthers().get(i).getVolume());
                    realAmount += mItem.getOthers().get(i).getRealAmount();
                    saleAmount += mItem.getOthers().get(i).getAmount();
                    volume += mItem.getOthers().get(i).getVolume();
                    weight += mItem.getOthers().get(i).getWeight();
                    vatAmount += mItem.getOthers().get(i).getVATAmount();
                    totalSaleAmount += mItem.getOthers().get(i).getTotalAmount();
                    totalT += mItem.getOthers().get(i).getVolume();
                    totalP += mItem.getOthers().get(i).getManualTemperature() * mItem.getOthers().get(i).getVolume();
                }
                density = weight / volume;
                temperature = totalP / totalT;

            }

            truckInfo += new String(new char[4-mItem.getOthers().size()]).replace('\0','\n');
        }
        else
            truckInfo +=" \n \n \n \n \n";

        //itemToPrint = mItem;
        FMSApplication app = FMSApplication.getApplication();
        SimpleDateFormat format = new SimpleDateFormat("          dd           MM         yyyy\n");
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
        dataToPrint = new ArrayList<String>(Arrays.asList(
                new String(new char[]{27,51,15}),
                new String(new char[]{27,51,42}),
                format.format(mItem.getStartTime()) + "\n",
                new String(new char[]{27,51,20}),
                "\n",
                String.format("                         %s\n",mItem.getQualityNo()),
                String.format("                       %s  %s                       %s\n", timeformat.format((mItem.getStartTime())), timeformat.format(mItem.getEndTime()), VNCharacterUtils.removeAccent(mItem.getProductName())),

                new String(new char[]{27,51,8}),

                new String(new char[]{27,51,14}),
                //String.format("    %s           %.0f    %.0f                    %.2f\n",mItem.getTruckNo()  ,mItem.getStartNumber(), mItem.getEndNumber(), mItem.getVolume()),
                truckInfo,

                String.format("                   %s\n", VNCharacterUtils.removeAccent(mItem.getAirlineModel().getInvoiceName())),
                String.format("                   %s\n", mItem.getAirlineModel().getInvoiceTaxCode()),
                String.format("             %s\n", VNCharacterUtils.removeAccent(mItem.getAirlineModel().getInvoiceAddress())),
                String.format("                    %-10s                          %s\n", mItem.getAircraftType(),mItem.getAircraftCode()),
                String.format("                    %-10s                       %s\n",mItem.getFlightCode(),mItem.getRouteName()),
                String.format("                            %.2f                       %.4f\n", temperature, density),
                String.format("                 %,.0f                                %,.0f\n", realAmount, weight),
                String.format("                 %,.0f                                \n", volume)

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


    public  boolean printBill(InvoiceModel invoiceModel)
    {
        dataToPrint = invoiceModel.createBillText();

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

    public  boolean printInvoice(InvoiceModel invoiceModel)
    {
        dataToPrint = invoiceModel.createInvoiceText();
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
                // data lin sent to printer, send next line, if ZERO, no more data to send
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
