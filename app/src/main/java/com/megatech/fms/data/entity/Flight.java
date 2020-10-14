package com.megatech.fms.data.entity;

import androidx.room.Embedded;
import androidx.room.Entity;

@Entity
public class Flight extends BaseEntity {
    private String code;
    private int airportId;

    public int getAirportId() {
        return airportId;
    }

    public void setAirportId(int airportId) {
        this.airportId = airportId;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }


}
