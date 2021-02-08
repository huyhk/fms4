package com.megatech.fms.data;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.megatech.fms.data.dao.ParkingLotDao;
import com.megatech.fms.data.dao.RefuelItemDao;
import com.megatech.fms.data.dao.TruckDao;
import com.megatech.fms.data.entity.Customer;
import com.megatech.fms.data.entity.Flight;
import com.megatech.fms.data.entity.ParkingLot;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Truck;

@Database(entities = {RefuelItem.class, Customer.class, ParkingLot.class, Flight.class, Truck.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class,
        RefuelItem.REFUEL_ITEM_STATUS.class,
        RefuelItem.FLIGHT_STATUS.class,
        RefuelItem.ITEM_PRINT_STATUS.class,
        RefuelItem.ITEM_POST_STATUS.class,
        RefuelItem.REFUEL_ITEM_TYPE.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract RefuelItemDao refuelItemDao();

    public abstract ParkingLotDao parkingLotDao();

    public abstract TruckDao truckDao();

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "fms.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }


}
