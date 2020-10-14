package com.megatech.fms.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Setting {
    @PrimaryKey
    private int airportId;
    private int userId;
    private int truckId;
    private String truckNo;

    public int getAirportId() {
        return airportId;
    }

    public void setAirportId(int airportId) {
        this.airportId = airportId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTruckId() {
        return truckId;
    }

    public void setTruckId(int trucId) {
        this.truckId = trucId;
    }

    public String getTruckNo() {
        return truckNo;
    }

    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }
}
