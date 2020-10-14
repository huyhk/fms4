package com.megatech.fms.model;

import java.util.Date;

public class LCRDataModel {

    private int flightId;

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    private int userId;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    private float grossQty;


    public float getGrossQty() {

        return grossQty;
    }
    public void setGrossQty(float qty){
        this.grossQty = qty;
    }
    private float netQty;

    public float getNetQty() {
        return netQty;
    }

    public void setNetQty(float netQty) {
        this.netQty = netQty;
    }

    private float temperature;

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    private Date startTime;

    private Date endTime;

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

    private float meterNumber;
    private  float startMeterNumber;
    private  float endMeterNumber;

    public float getMeterNumber() {
        return meterNumber;
    }

    public void setMeterNumber(float meterNumber) {
        this.meterNumber = meterNumber;
    }

    public float getStartMeterNumber() {
        return startMeterNumber;
    }

    public void setStartMeterNumber(float startMeterNumber) {
        this.startMeterNumber = startMeterNumber;
    }

    public float getEndMeterNumber() {
        return endMeterNumber;
    }

    public void setEndMeterNumber(float endMeterNumber) {
        this.endMeterNumber = endMeterNumber;
    }
}
