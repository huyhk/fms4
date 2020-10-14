package com.megatech.fms.model;

import static com.megatech.fms.model.RefuelItemData.GALLON_TO_LITTER;

public class InvoiceItemModel {
    private String truckNo;
    private double startNumber;
    private double endNumber;
    private double realAmount;
    private double volume;

    public double getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(double startNumber) {
        this.startNumber = startNumber;
    }

    public double getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(double endNumber) {
        this.endNumber = endNumber;
    }

    public double getVolume() {
        return realAmount * GALLON_TO_LITTER;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public String getTruckNo() {
        return truckNo;
    }

    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }


    public double getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(double realAmount) {
        this.realAmount = realAmount;
    }
}
