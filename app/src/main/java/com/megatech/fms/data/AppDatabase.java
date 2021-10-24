package com.megatech.fms.data;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.megatech.fms.BuildConfig;
import com.megatech.fms.data.dao.AirlineDao;
import com.megatech.fms.data.dao.ParkingLotDao;
import com.megatech.fms.data.dao.RefuelItemDao;
import com.megatech.fms.data.dao.ShiftDao;
import com.megatech.fms.data.dao.TruckDao;
import com.megatech.fms.data.dao.UserDao;
import com.megatech.fms.data.entity.Airline;
import com.megatech.fms.data.entity.Flight;
import com.megatech.fms.data.entity.ParkingLot;
import com.megatech.fms.data.entity.Price;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Shift;
import com.megatech.fms.data.entity.Truck;
import com.megatech.fms.data.entity.User;

@Database(entities = {RefuelItem.class,
        Airline.class,
        ParkingLot.class,
        Flight.class,
        Truck.class,
        User.class,
        Shift.class
        },
        version = 2,
        exportSchema = false
        )
@TypeConverters({Converters.class,
        RefuelItem.REFUEL_ITEM_STATUS.class,
        RefuelItem.FLIGHT_STATUS.class,
        RefuelItem.ITEM_PRINT_STATUS.class,
        RefuelItem.ITEM_POST_STATUS.class,
        RefuelItem.REFUEL_ITEM_TYPE.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract RefuelItemDao refuelItemDao();
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, BuildConfig.DB_FILE)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ParkingLotDao parkingLotDao();

    public abstract TruckDao truckDao();

    public abstract AirlineDao airlineDao();

    public abstract UserDao userDao();

    public abstract ShiftDao shiftDao();


}
