package com.megatech.fms.data;

import com.megatech.fms.data.entity.Airline;
import com.megatech.fms.data.entity.ParkingLot;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Shift;
import com.megatech.fms.data.entity.Truck;
import com.megatech.fms.data.entity.User;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.ShiftModel;
import com.megatech.fms.model.TruckModel;
import com.megatech.fms.model.UserModel;

import java.util.ArrayList;
import java.util.Date;
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


    public List<RefuelItemData> getRefuelList(String truckNo, int truckId, boolean self, int type, long start, long end) {
        List<RefuelItem> localList;
        if (self)
            localList = db.refuelItemDao().getByTruckNo(truckNo, start, end, type);
        else
            localList = db.refuelItemDao().getOthers(truckNo, start, end, type);

        return toRefuelList(localList);
    }

    public List<RefuelItemData> getRefuelList(String truckNo, int truckId, boolean self, int type) {
        List<RefuelItem> localList;
        if (self)
            localList = db.refuelItemDao().getByTruckNo(truckNo, type);
        else
            localList = db.refuelItemDao().getOthers(truckNo, type);
        return toRefuelList(localList);
    }

    private List<RefuelItemData> toRefuelList(List<RefuelItem> localList) {

        List<RefuelItemData> returnList = new ArrayList();
        for (RefuelItem item: localList) {
            returnList.add(item.toRefuelItemData());
        }
        return returnList;
    }

    public void insertRefuel(RefuelItem localItem) {

        RefuelItem item = getRefuel(localItem.getId(), localItem.getLocalId());
        if (item == null) {

            localItem.setLocalId((int) db.refuelItemDao().insert(localItem));
        } else {

            //item.setJsonData(localItem.getJsonData());
            //item.setLocalModified(true);
            localItem.setLocalId(item.getLocalId());
            db.refuelItemDao().update(localItem);

        }
    }

    public RefuelItem getRefuel(Integer id, int localId) {
        RefuelItem item = null;

        if (id !=0)
            item = db.refuelItemDao().get(id);
        if (item == null && localId != 0)
            item = db.refuelItemDao().getLocal(localId);
        return  item;
    }

    public int[] getNotChangedRefuels() {
        return  db.refuelItemDao().getNotChanges();
    }

    public void removeDeletedRefuels(int[] ids) {
        db.refuelItemDao().removeDeleted(ids);
    }

    public List<RefuelItem> getModifiedRefuel() {
        List<RefuelItem> modified = db.refuelItemDao().getModified();
        return modified;
    }

    public List<AirlineModel> getAirlines() {
        List<Airline> localList = db.airlineDao().getAll();
        List<AirlineModel> returnList = new ArrayList();
        for (Airline item : localList) {
            returnList.add(item.toAirlineModel());
        }
        return returnList;
    }

    public void insertAirline(Airline model) {
        Airline item = db.airlineDao().get(model.getId());
        if (item == null) {
            db.airlineDao().insert(model);
        } else {
            //item.setJsonData(truckModel.getJsonData());
            model.setLocalId(item.getLocalId());
            db.airlineDao().update(model);
        }
    }

    //Users
    public List<UserModel> getUsers() {
        List<User> localList = db.userDao().getAll();
        List<UserModel> returnList = new ArrayList();
        for (User item : localList) {
            returnList.add(item.toUserModel());
        }
        return returnList;
    }

    public void insertUser(User model) {
        User item = db.userDao().get(model.getId());
        if (item == null) {
            db.userDao().insert(model);
        } else {
            //item.setJsonData(truckModel.getJsonData());
            model.setLocalId(item.getLocalId());
            db.userDao().update(model);
        }
    }

    //Shift
    public List<ShiftModel> getShifts() {
        List<Shift> localList = db.shiftDao().getAll();
        List<ShiftModel> returnList = new ArrayList();
        for (Shift item : localList) {
            returnList.add(item.toShiftModel());
        }
        return returnList;
    }

    public void insertShift(Shift model) {
        Shift item = db.shiftDao().get(model.getId());
        if (item == null) {
            db.shiftDao().insert(model);
        } else {
            //item.setJsonData(truckModel.getJsonData());
            model.setLocalId(item.getLocalId());
            db.shiftDao().update(model);
        }
    }


    public Date getLastModifiedRefuel() {
        return db.refuelItemDao().getLastModifiedDate();
    }

    public List<RefuelItem> getOthers(int localId) {

        return db.refuelItemDao().getOthers(localId);

    }
}
