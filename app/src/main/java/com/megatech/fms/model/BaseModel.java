package com.megatech.fms.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.FMSApplication;

import java.util.Date;

public class BaseModel {

    protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    private boolean isDeleted;

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    private int localId = 0;
    private Date dateUpdated;

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    private String tabletSerial;

    public String getTabletSerial() {


        return tabletSerial;
    }

    public void setTabletSerial(String tabletSerial) {
        this.tabletSerial = tabletSerial;
    }

    public String toJson() {
        return gson.toJson(this);
    }
}
