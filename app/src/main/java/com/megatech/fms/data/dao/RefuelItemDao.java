package com.megatech.fms.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.megatech.fms.data.entity.RefuelItem;

import java.util.Date;
import java.util.List;

@Dao
public interface RefuelItemDao {

    @Query("Select * from RefuelItem")
    List<RefuelItem> getAll();

    @Query("Select * from RefuelItem where id = :id")
    RefuelItem get(int id);

    @Query("Select * from RefuelItem where localId != :id AND flightId = (SELECT flightId from RefuelItem where localId= :id)")
    List<RefuelItem> getOthers(int id);

    @Query("Select * from RefuelItem where flightId = :id")
    List<RefuelItem> getByFlightId(int id);

    @Query("Select * from RefuelItem where truckNo = :truckNo and refuelTime between :start and :end")
    List<RefuelItem> getByTruckNo(String truckNo, long start, long end);


    @Query("Select * from RefuelItem where truckNo = :truckNo")
    List<RefuelItem> getByTruckNo(String truckNo);


    @Query("Select * from RefuelItem where truckNo != :truckNo and refuelTime between :start and :end")
    List<RefuelItem> getOthers(String truckNo, long start, long end);


    @Query("Select * from RefuelItem where truckNo != :truckNo")
    List<RefuelItem> getOthers(String truckNo);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RefuelItem> items);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(RefuelItem item);

    @Delete
    void delete(RefuelItem item);

    @Update( onConflict = OnConflictStrategy.REPLACE)
    void update(RefuelItem item);

    @Query("Select * from RefuelItem where localId = :id")
    RefuelItem getLocal(Integer id);

    @Query("Select id from refuelitem where isSynced")
    int[] getNotChanges();

    @Query("Delete from RefuelItem where id > 0 and id in (:ids) and NOT isLocalModified")
    void removeDeleted(int[] ids);

    @Query("Select * from RefuelItem where isLocalModified OR id = 0")
    List<RefuelItem> getModified();

    @Query("Select Max(dateUpdated) from RefuelItem ")
    Date getLastModifiedDate();
}
