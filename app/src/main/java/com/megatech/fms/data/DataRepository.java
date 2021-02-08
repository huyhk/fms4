package com.megatech.fms.data;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.megatech.fms.data.entity.ParkingLot;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Truck;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.TruckModel;

import java.util.ArrayList;
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

    public List<TruckModel> getTrucks() {
        List<Truck>  lst = db.truckDao().getAll();
        List<TruckModel> lstModel = new ArrayList<>();
        for (Truck item: lst)
        {
            lstModel.add(item.toTruckModel());
        }
        return lstModel;
    }

    public void insertTruck(Truck truckModel) {
        Truck item = db.truckDao().get(truckModel.getId());
        if (item == null)
        {
            db.truckDao().insert(truckModel);
        }
        else {
            item.setJsonData(truckModel.getJsonData());
            db.truckDao().update(item);
        }
    }

    public void deleteOudateTrucks(int[] ids) {
        db.truckDao().deleteNotIds(ids);
    }

    public List<RefuelItemData> getRefuelList(String truckNo, int truckId, boolean self, int type) {
        List<RefuelItem> localList;
        if (self)
            localList = db.refuelItemDao().getByTruckNo(truckNo);
        else
            localList = db.refuelItemDao().getOthers(truckNo);
        List<RefuelItemData> returnList = new ArrayList();
        for (RefuelItem item: localList)
        {
            returnList.add(item.toRefuelItemData());
        }
        return returnList;
    }

    public void insertRefuel(RefuelItem localItem) {

        RefuelItem item = db.refuelItemDao().get(localItem.getId());
        if (item == null) {
            localItem.setLocalId((int) db.refuelItemDao().insert(localItem));
        } else {

                item.setJsonData(localItem.getJsonData());
                item.setLocalModified(true);
                db.refuelItemDao().update(item);

        }
    }

    public RefuelItem getRefuel(Integer id, int localId) {
        RefuelItem item = null;

        if (id !=0)
            item = db.refuelItemDao().get(id);
        else if (localId !=0)
            item = db.refuelItemDao().getLocal(localId);
        return  item;
    }

    public int[] getNotChangedRefuels() {
        return  db.refuelItemDao().getNotChanges();
    }

    public void removeDeletedRefuels(int[] ids) {
        db.refuelItemDao().removeDeleted(ids);
    }
}
