package com.megatech.fms.data.entity;

import androidx.room.PrimaryKey;

public class BaseEntity {
    @PrimaryKey
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
