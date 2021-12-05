package com.megatech.fms.model;

import com.megatech.fms.FMSApplication;
import com.megatech.fms.helpers.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceiptModel extends BaseModel{

    public static ReceiptModel createReceipt(List<RefuelItemData> refuels)
    {
        ReceiptModel model = null;


        if (refuels.size()>0)
        {
            RefuelItemData refuel = refuels.get(0);
            String number = refuel.getTruckNo().substring(0,3) + refuel.getTruckNo().substring(refuel.getTruckNo().length()-2);
            number += DateUtils.formatDate(new Date(),"yyMMddHHmm");

            String data = gson.toJson(refuel);
            model = gson.fromJson(data, ReceiptModel.class);
            model.setNumber(number);
            model.setGallon(0);
            model.setDate(new Date());
            model.customerName = refuel.getInvoiceNameCharter().trim();
            if (model.customerName.isEmpty())
                model.customerName = refuel.getAirlineModel().getName().trim();
            model.customerCode = refuel.getAirlineModel().getCode().trim();
            model.customerAddress = refuel.getAirlineModel().getAddress().trim();
            model.taxCode = refuel.getAirlineModel().getTaxCode().trim();
            model.productName = refuel.getAirlineModel().getProductName();

            for(RefuelItemData itemData: refuels)
            {
                addItem(model,itemData);
                if (itemData.getStartTime().compareTo(model.getStartTime())<0)
                {
                    model.setStartTime(itemData.getStartTime());
                }

                if (itemData.getEndTime().compareTo(model.getStartTime())>0)
                {
                    model.setEndTime(itemData.getEndTime());
                }
            }
        }

        return  model;
    }

    private static void addItem(ReceiptModel model, RefuelItemData itemData) {
        if (model.items == null)
            model.items = new ArrayList<>();
        ReceiptItemModel itemModel = gson.fromJson(itemData.toJson(), ReceiptItemModel.class);
        itemModel.setRefuelItemId(  itemData.getUniqueId());
        model.items.add(itemModel);
        model.setGallon(model.getGallon() + itemData.getGallon());
        model.setVolume(model.getVolume() + itemData.getVolume());
        model.setWeight(model.getWeight() + itemData.getWeight());
    }

    public String createPrintText()
    {
        UserInfo user = FMSApplication.getApplication().getUser();

        String LS_18 =  new String(new char[]{27, 51, 18});
        String LS_24 =  new String(new char[]{27, 51, 24});
        String LS_DEFAULT =  new String(new char[]{27, 50});
        StringBuilder builder = new StringBuilder();
        builder.append("The Seller: " + user.getInvoiceName()+ "\n");
        builder.append("Tax code: " + user.getTaxCode() +"\n");
        builder.append("Address: "+ user.getAddress()+"\n");
        builder.append("------------------------------------------------------------------\n");
        builder.append("                  FUEL DELIVERY RECEIPT                           \n");
        builder.append("------------------------------------------------------------------\n");
        builder.append(String.format("No.: %-15s            (%s)\n",this.number, DateUtils.formatDate(this.date,"dd/MM/yyyy")));
        builder.append(LS_18);
        builder.append(String.format("Buyer: %s\n",this.customerName));
        builder.append(LS_DEFAULT);
        builder.append("\n");
        builder.append(String.format("A/C Type           : %-15s A/C reg     : %s\n",this.aircraftType, this.aircraftCode));
        builder.append(String.format("Flight No.         : %-15s Route       : %s\n",this.flightCode, this.routeName));
        builder.append(String.format("Quality Control No.: %-15s Product Name: %s\n",this.qualityNo, this.productName));
        builder.append(String.format("Start Time         :%16s End Time    :%s\n",DateUtils.formatDate(this.startTime,"HH:mm dd/MM/yyyy"), DateUtils.formatDate(this.endTime,"HH:mm dd/MM/yyyy")));
        builder.append("------------------------------------------------------------------\n");
        builder.append("|  Refueller No.    |   Temp.   |   USG    |  Litter  |   Kg     |\n");
        builder.append("| Start/End Meter   | Density   |          |          |          |\n");
        builder.append("------------------------------------------------------------------\n");
        for (ReceiptItemModel itemModel: this.items)
        {
            builder.append(String.format("|%-19s|%9.2foC|%10.0f|%10.0f|%10.0f|\n",itemModel.getTruckNo(), itemModel.getTemperature(), itemModel.getGallon(), itemModel.getVolume(), itemModel.getWeight()));
            builder.append(String.format("|%9.0f/%-9.0f|%6.4f kg/l|          |          |          |\n",itemModel.getStartNumber(), itemModel.getEndNumber(), itemModel.getDensity()));
            builder.append("------------------------------------------------------------------\n");
        }
        builder.append(String.format("| Total                         |%10.0f|%10.0f|%10.0f|\n", this.gallon, this.volume, this.weight));
        builder.append("------------------------------------------------------------------\n");
        builder.append("           Buyer                            Seller        \n");
        builder.append("  (Signature and full name)      (Signature and full name)     \n");
        return  builder.toString();

    }

    private String number;
    private Date date;

    private int customerId;
    private String customerName;
    private String customerCode;
    private String customerAddress;
    private String taxCode;
    private String productName;

    private int flightId;
    private String flightCode;
    private String aircraftCode;
    private String aircraftType;
    private String routeName;

    private String qualityNo;
    private Date startTime;
    private Date endTime;


    private double gallon;
    private double volume;
    private double weight;

    private double returnAmount;
    private String defuelingNo;

    private boolean invoiceSplit;
    private double splitAmount;

    List<ReceiptItemModel> items;

    public static ReceiptModel fromJson(String data) {

        return gson.fromJson(data, ReceiptModel.class);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public String getAircraftCode() {
        return aircraftCode;
    }

    public void setAircraftCode(String aircraftCode) {
        this.aircraftCode = aircraftCode;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getQualityNo() {
        return qualityNo;
    }

    public void setQualityNo(String qualityNo) {
        this.qualityNo = qualityNo;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public double getGallon() {
        return gallon;
    }

    public void setGallon(double gallon) {
        this.gallon = gallon;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(double returnAmount) {
        this.returnAmount = returnAmount;
    }

    public String getDefuelingNo() {
        return defuelingNo;
    }

    public void setDefuelingNo(String defuelingNo) {
        this.defuelingNo = defuelingNo;
    }

    public boolean isInvoiceSplit() {
        return invoiceSplit;
    }

    public void setInvoiceSplit(boolean invoiceSplit) {
        this.invoiceSplit = invoiceSplit;
    }

    public double getSplitAmount() {
        return splitAmount;
    }

    public void setSplitAmount(double splitAmount) {
        this.splitAmount = splitAmount;
    }

    public List<ReceiptItemModel> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItemModel> items) {
        this.items = items;
    }

    private String pdfImageString;

    public String getPdfImageString() {
        return pdfImageString;
    }

    public void setPdfImageString(String pdfImageString) {
        this.pdfImageString = pdfImageString;
    }

    private boolean printed = false;

    private boolean captured = false;

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }
}
