package com.megatech.fms.data;

import android.database.Cursor;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.megatech.fms.data.entity.Airline;
import com.megatech.fms.data.entity.BM2505;
import com.megatech.fms.data.entity.Flight;
import com.megatech.fms.data.entity.Invoice;
import com.megatech.fms.data.entity.ParkingLot;
import com.megatech.fms.data.entity.Receipt;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Shift;
import com.megatech.fms.data.entity.Truck;
import com.megatech.fms.data.entity.TruckFuel;
import com.megatech.fms.data.entity.User;
import com.megatech.fms.helpers.DateUtils;
import com.megatech.fms.helpers.HttpClient;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.BM2505Model;
import com.megatech.fms.model.FlightModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.ShiftModel;
import com.megatech.fms.model.TruckFuelModel;
import com.megatech.fms.model.TruckModel;
import com.megatech.fms.model.UserModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DataRepository {


    private final AppDatabase db;
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
    public List<TruckModel> getFHSTrucks() {
        List<Truck>  lst = db.truckDao().getFHS();
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
            localList = db.refuelItemDao().getOthers(truckNo, start, end);

        return toRefuelList(localList);
    }

    public List<RefuelItemData> getRefuelList(String truckNo, int truckId, boolean self, int type) {
        List<RefuelItem> localList;
        if (self)
            localList = db.refuelItemDao().getByTruckNo(truckNo);
        else
            localList = db.refuelItemDao().getOthers(truckNo);
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

    public RefuelItem getRefuel(String uniqueId) {
        RefuelItem item = null;

        if (uniqueId != null && !uniqueId.isEmpty())
            item = db.refuelItemDao().get(uniqueId);
        return item;
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
    public List<RefuelItem> getOthers(String uniqueId) {

        return db.refuelItemDao().getOtherItems(uniqueId);

    }
    public void deleteOldRefuels(int numDays) {

        Date d= new Date();
        d = new Date(d.getTime() - 24*60*60*1000 *numDays);

        db.refuelItemDao().deleteByDate(d.getTime());
    }

    public RefuelItemData getIncomplete(String truckNo) {
        Date d = new Date();
        RefuelItem item =  db.refuelItemDao().getIncomplete(truckNo, d.getTime() - 1000 * 60 * 60 * 24);
        if (item!=null)
            return item.toRefuelItemData();
        else return  null;
    }

    public List<TruckFuelModel> getTruckFuels(Date date) {
        Calendar cal =  Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTime().getTime();
        cal.add(Calendar.DATE,1);
        long end = cal.getTime().getTime();
        List<TruckFuel> localList = db.truckFuelDao().getAll(start, end);
        List<TruckFuelModel> returnList = new ArrayList();
        for (TruckFuel item : localList) {
            returnList.add(item.toTruckFuelModel());
        }
        return returnList;
    }

    public void insertTruckFuel(TruckFuel model) {

        TruckFuel item = db.truckFuelDao().get(model.getId(), model.getLocalId());

        if (item == null || (item.getId() == 0 &&  item.getLocalId() != model.getLocalId())) {
            db.truckFuelDao().insert(model);
        } else {
            //item.setJsonData(truckModel.getJsonData());

            model.setLocalId(item.getLocalId());
            db.truckFuelDao().update(model);
        }
    }


    public List<TruckFuel> getModifiedTruckFuel() {

        List<TruckFuel> modified = db.truckFuelDao().getModified();
        return modified;
    }

    public List<Invoice> getModifiedInvoice() {

        List<Invoice> modified = db.invoiceDao().getModified();
        return modified;
    }

    public void deleteTruckFuels(int[] ids) {
        db.truckFuelDao().delete(ids);
    }


    public <T> List<T> getModified()
    {
        return null;
    }

    public void insertInvoice(Invoice model) {
        Invoice item = db.invoiceDao().get(model.getId(), model.getLocalId());

        if (item == null || (item.getId() == 0 &&  item.getLocalId() != model.getLocalId())) {
            db.invoiceDao().insert(model);
        } else {

            model.setLocalId(item.getLocalId());
            db.invoiceDao().update(model);
        }
    }

    public List<BM2505Model> getBM2505List(Date date) {

        Calendar cal =  Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTime().getTime();
        cal.add(Calendar.DATE,1);
        long end = cal.getTime().getTime();
        List<BM2505> localList = db.bm2505Dao().getAll(start, end);
        List<BM2505Model> returnList = new ArrayList();
        for (BM2505 item : localList) {
            returnList.add(item.toModel());
        }
        return returnList;
    }

    public void insertFlight(Flight model) {
        Flight item = db.flightDao().get(model.getId(), model.getLocalId());

        if (item == null || (item.getId() == 0 &&  item.getLocalId() != model.getLocalId())) {
            db.flightDao().insert(model);
        } else {

            model.setLocalId(item.getLocalId());
            db.flightDao().update(model);
        }
    }

    public void insertBM2505(BM2505 model) {

        BM2505 item = db.bm2505Dao().get(model.getId(), model.getLocalId());

        if (item == null || (item.getId() == 0 &&  item.getLocalId() != model.getLocalId())) {
            db.bm2505Dao().insert(model);
        } else {


            model.setLocalId(item.getLocalId());
            db.bm2505Dao().update(model);
        }
    }

    public List<FlightModel> getFlights(Date date) {
        Calendar cal =  Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTime().getTime();
        cal.add(Calendar.DATE,1);
        long end = cal.getTime().getTime();
        List<Flight> localList = db.flightDao().getAll(start, end);
        List<FlightModel> returnList = new ArrayList();
        for (Flight item : localList) {
            returnList.add(item.toModel());
        }
        return returnList;
    }

    public void deleteBM2505(int[] ids) {
        db.bm2505Dao().delete(ids);
    }

    public List<BM2505> getModifiedBM2505() {
        List<BM2505> modified = db.bm2505Dao().getModified();
        return modified;
    }

    public int insertReceipt(Receipt model) {
        Receipt item = db.receiptDao().get(model.getNumber());

        if (item == null ) {
            return (int)db.receiptDao().insert(model);
        } else {

            model.setLocalId(item.getLocalId());
            model.setCancelled(item.isCancelled());
            model.setCancelReason(item.getCancelReason());
            return db.receiptDao().update(model);
        }
    }

    public List<Receipt> getModifiedReceipt() {

        List<Receipt> modified = db.receiptDao().getModified();
        return modified;
    }

    public void cancelReceipts(String[] printedItems, String reason ) {
        db.receiptDao().cancel(printedItems, reason);
    }

    public RefuelItem getRefuelByFlightAndTruck(Integer flightId, int truckId) {
        return db.refuelItemDao().getByFlightAndTruck(flightId,truckId);
    }

    public boolean getLocalModified() {
        SimpleSQLiteQuery query = new SimpleSQLiteQuery("Select SUM(CNT ) FROM (Select count(0) as CNT from RefuelItem where isLocalModified = 1 Union Select count(0) from Receipt where isLocalModified = 1)");
        Cursor cs = db.query(query);
        if (cs.getCount()>0) {
            cs.moveToFirst();
            int c = cs.getInt(0);
            return c > 0;
        }
        else return false;
    }

    public List<Receipt> getReceiptList(Date date) {

        Date next = DateUtils.getNextDate(date);

        List<Receipt> modified = db.receiptDao().getAll(date.getTime(), next.getTime());
        return modified;
    }
}
