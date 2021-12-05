package com.megatech.fms.model;

import android.database.DatabaseErrorHandler;

import com.megatech.fms.FMSApplication;

import java.util.Date;

public class TruckFuelModel extends BaseModel {

    private int truckId;
    private String truckNo;

    private String ticketNo;
    private String tankNo;
    private String maintenanceStaff;
    private String qcNo;
    private Date time;
    private int operatorId;
    private String operatorName;
    private double amount; //litter
    private double accumulatedRefuelAmount;//litter
    private double truckCapacity;

    public TruckFuelModel() {
        TruckModel setting = FMSApplication.getApplication().getSetting();
        this.truckId = setting.getTruckId();
        this.truckNo = setting.getTruckNo();
        this.qcNo = FMSApplication.getApplication().getQCNo();
        this.time = new Date();
    }

    public int getTruckId() {
        return truckId;
    }

    public void setTruckId(int truckId) {
        this.truckId = truckId;
    }

    public String getTruckNo() {
        return truckNo;
    }

    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }

    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getTankNo() {
        return tankNo;
    }

    public void setTankNo(String tankNo) {
        this.tankNo = tankNo;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getMaintenanceStaff() {
        return maintenanceStaff;
    }

    public void setMaintenanceStaff(String maintenanceStaff) {
        this.maintenanceStaff = maintenanceStaff;
    }

    public String getQcNo() {
        return qcNo;
    }

    public void setQcNo(String qcNo) {
        this.qcNo = qcNo;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getAccumulatedRefuelAmount() {
        return accumulatedRefuelAmount;
    }

    public void setAccumulatedRefuelAmount(double accumulatedRefuelAmount) {
        this.accumulatedRefuelAmount = accumulatedRefuelAmount;
    }

    public double getTruckCapacity() {
        return truckCapacity;
    }

    public void setTruckCapacity(double truckCapacity) {
        this.truckCapacity = truckCapacity;
    }
}
