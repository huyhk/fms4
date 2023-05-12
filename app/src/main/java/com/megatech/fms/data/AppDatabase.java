package com.megatech.fms.data;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.megatech.fms.BuildConfig;
import com.megatech.fms.data.dao.AirlineDao;
import com.megatech.fms.data.dao.BM2505Dao;
import com.megatech.fms.data.dao.FlightDao;
import com.megatech.fms.data.dao.InvoiceDao;
import com.megatech.fms.data.dao.LogEntryDao;
import com.megatech.fms.data.dao.ParkingLotDao;
import com.megatech.fms.data.dao.ReceiptDao;
import com.megatech.fms.data.dao.RefuelItemDao;
import com.megatech.fms.data.dao.ReviewDao;
import com.megatech.fms.data.dao.ShiftDao;
import com.megatech.fms.data.dao.TruckDao;
import com.megatech.fms.data.dao.TruckFuelDao;
import com.megatech.fms.data.dao.UserDao;
import com.megatech.fms.data.entity.Airline;
import com.megatech.fms.data.entity.BM2505;
import com.megatech.fms.data.entity.Flight;
import com.megatech.fms.data.entity.Invoice;
import com.megatech.fms.data.entity.LogEntry;
import com.megatech.fms.data.entity.ParkingLot;
import com.megatech.fms.data.entity.Receipt;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Review;
import com.megatech.fms.data.entity.Shift;
import com.megatech.fms.data.entity.Truck;
import com.megatech.fms.data.entity.TruckFuel;
import com.megatech.fms.data.entity.User;
import com.megatech.fms.enums.INVOICE_TYPE;

@Database(entities = {RefuelItem.class,
        Airline.class,
        ParkingLot.class,
        Flight.class,
        Truck.class,
        User.class,
        Shift.class,
        TruckFuel.class,
        Invoice.class,
        BM2505.class,
        Receipt.class,
        LogEntry.class,
        Review.class
        },
        version = 5,
        exportSchema = false
        )
@TypeConverters({Converters.class,
        RefuelItem.REFUEL_ITEM_STATUS.class,
        RefuelItem.FLIGHT_STATUS.class,
        RefuelItem.ITEM_PRINT_STATUS.class,
        RefuelItem.ITEM_POST_STATUS.class,
        RefuelItem.REFUEL_ITEM_TYPE.class,
        INVOICE_TYPE.class})
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

    public abstract TruckFuelDao truckFuelDao();
    public abstract BM2505Dao bm2505Dao();

    public abstract InvoiceDao invoiceDao();

    public  abstract FlightDao flightDao();

    public abstract ReceiptDao receiptDao();

    public abstract LogEntryDao logEntryDao();

    public abstract ReviewDao reviewDao();
}
