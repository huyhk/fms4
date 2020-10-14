package com.megatech.fms.model;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ShiftModel {
    private String name;
    private Date startTime;
    private Date endTime;
    private int airportId;

    public ShiftModel() {
        name = "N/A";
        Calendar c = Calendar.getInstance();
        c.set(0, 0, 0, 0, 0);
        startTime = c.getTime();
        c.set(0, 0, 0, 23, 59);
        endTime = c.getTime();

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public int getAirportId() {
        return airportId;
    }

    public void setAirportId(int airportId) {
        this.airportId = airportId;
    }

    @NonNull
    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");

        return String.format("%s - %s - %s", this.name, formatter.format(this.startTime), formatter.format(this.endTime));
    }
}
