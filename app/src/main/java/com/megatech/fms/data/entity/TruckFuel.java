package com.megatech.fms.data.entity;

import com.megatech.fms.model.TruckFuelModel;

import java.util.Date;

public class TruckFuel extends BaseEntity {
    private int truckId;
    private String ticketNo;
    private String tankNo;
    private String maintenanceStaff;
    private String qcNo;
    private Date time;
    private int operatorId;
    private double amount; //litter
    private double accumulatedRefuelAmount;//litter
    private double truckCapacity;

    public static TruckFuel fromTruckFuelModel(TruckFuelModel model) {
        if (model != null)
            return gson.fromJson(model.toJson(), TruckFuel.class);
        else
            return null;
    }

    public  TruckFuelModel toTruckFuelModel() {

        return gson.fromJson(gson.toJson(this), TruckFuelModel.class);

    }
}
