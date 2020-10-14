package com.megatech.fms.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.helpers.ReadNumber;
import com.megatech.fms.helpers.VNCharacterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class InvoiceModel {

    public static InvoiceModel fromRefuel(RefuelItemData refuel, boolean single) {
        InvoiceModel model = new InvoiceModel();
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        String data = gson.toJson(refuel);

        model = gson.fromJson(data, InvoiceModel.class);
        model.customerName = refuel.getAirlineModel().getName();
        model.address = refuel.getAirlineModel().getAddress();
        model.taxCode = refuel.getAirlineModel().getTaxCode();
        model.productName = refuel.getAirlineModel().getProductName();
        model.temperature = refuel.getManualTemperature();
        model.amount = refuel.getAmount();
        model.vatAmount = refuel.getVATAmount();
        model.totalAmount = refuel.getTotalAmount();


        model.items = new ArrayList<InvoiceItemModel>(Arrays.asList(gson.fromJson(data, InvoiceItemModel.class)));
        double totalT = model.getVolume();
        double totalP = model.temperature * model.getVolume();
        double totalW = model.getWeight();
        if (!single && refuel.getOthers().size() > 0) {
            for (int i = 0; i < refuel.getOthers().size(); i++) {
                RefuelItemData item = refuel.getOthers().get(i);
                String itemData = gson.toJson(item);
                model.items.add(gson.fromJson(itemData, InvoiceItemModel.class));
                model.realAmount += item.getRealAmount();
                model.amount += item.getAmount();
                model.totalAmount += item.getTotalAmount();
                model.vatAmount += item.getVATAmount();

                totalT += item.getVolume();

                totalP += item.getManualTemperature() * item.getVolume();

                totalW += item.getWeight();
            }
            model.density = totalW / totalT;
            model.temperature = totalP / totalT;
        }
        model.inWords = VNCharacterUtils.removeAccent(ReadNumber.numberToString(model.totalAmount));

        return model;
    }

    private Date date;
    private Date startTime;
    private Date endTime;
    private String productName;
    private String customerName;
    private String taxCode;
    private String address;
    private String aircraftType;
    private String aircraftCode;
    private String flightCode;
    private String routeName;
    private double temperature;
    private double density;
    private double volume;
    private double weight;
    private double price;
    private double amount;
    private double taxRate;
    private double vatAmount;
    private double totalAmount;
    private String inWords;

    private double realAmount;

    public double getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(double realAmount) {
        this.realAmount = realAmount;
    }

    private List<InvoiceItemModel> items;

    public List<InvoiceItemModel> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemModel> items) {
        this.items = items;
    }

    public InvoiceModel() {

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getAircraftCode() {
        return aircraftCode;
    }

    public void setAircraftCode(String aircraftCode) {
        this.aircraftCode = aircraftCode;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double getVolume() {
        return Math.round(this.realAmount * RefuelItemData.GALLON_TO_LITTER);
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getWeight() {
        return Math.round(density * getVolume());
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getInWords() {
        return inWords;
    }

    public void setInWords(String inWords) {
        this.inWords = inWords;
    }
}
