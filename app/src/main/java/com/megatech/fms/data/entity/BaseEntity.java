package com.megatech.fms.data.entity;

import androidx.room.PrimaryKey;

public class BaseEntity {

    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @PrimaryKey(autoGenerate = true)
    private int localId;

    public int getLocalId() {
        return localId;
    }

    public void setLocalId(int localId) {
        this.localId = localId;
    }

    private String jsonData;

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }

    private boolean isSynced = true;

    public boolean isSynced() {
        return isSynced;
    }

    public void setSynced(boolean synced) {
        isSynced = synced;
    }

    private boolean isLocalModified = false;

    public boolean isLocalModified() {
        return isLocalModified;
    }

    public void setLocalModified(boolean localModified) {
        isLocalModified = localModified;
    }
}
