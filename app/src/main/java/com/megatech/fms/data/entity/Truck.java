package com.megatech.fms.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.TruckModel;

@Entity
public class Truck extends BaseEntity {
    private static final Gson gson = new GsonBuilder().create();
    public TruckModel toTruckModel()
    {
        return  gson.fromJson(getJsonData(),TruckModel.class);
    }

    public static Truck fromTruckModel(TruckModel itemData)
    {
        Truck item = new Truck();
        item.setId(itemData.getId());
        item.setFHS(itemData.isFHS());
        item.setJsonData(gson.toJson(itemData));
        return  item;
    }

    private boolean isFHS;

    public boolean isFHS() {
        return isFHS;
    }

    public void setFHS(boolean FHS) {
        isFHS = FHS;
    }
}
