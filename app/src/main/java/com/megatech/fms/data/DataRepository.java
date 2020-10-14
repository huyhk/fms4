package com.megatech.fms.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.megatech.fms.data.entity.ParkingLot;
import com.megatech.fms.helpers.HttpClient;

import java.util.List;

public class DataRepository {

    private AppDatabase db;
    private static DataRepository sInstance;

    private DataRepository(final AppDatabase db) {
        this.db = db;
    }

    public static DataRepository getInstance(AppDatabase db) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(db);
                }
            }
        }
        return sInstance;
    }

    public List<ParkingLot> getParkingLot() {
        List<ParkingLot> lst = db.parkingLotDao().getAll();
        if (lst.size() == 0) {
            HttpClient client = new HttpClient();
            List<ParkingLot> parkingLots = client.getParking();
            db.parkingLotDao().insertAll(parkingLots);
        }
        return lst;
    }

    public List<ParkingLot> getParkingLot(int airportId) {
        return db.parkingLotDao().getAll(airportId);
    }

}
