package com.megatech.fms.helpers;

import android.content.Context;

import com.megatech.fms.BuildConfig;
import com.megatech.fms.FMSApplication;
import com.megatech.fms.data.AppDatabase;
import com.megatech.fms.data.DataRepository;
import com.megatech.fms.data.entity.Airline;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Truck;
import com.megatech.fms.data.entity.User;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.ShiftModel;
import com.megatech.fms.model.TruckModel;
import com.megatech.fms.model.UserModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataHelper {

    private static boolean isDebug = BuildConfig.DEBUG || true;

    private static  Context context = FMSApplication.getApplication();
    private static HttpClient httpClient = new HttpClient();
    private static DataRepository repo = DataRepository.getInstance(AppDatabase.getInstance(context));
    private static boolean processing = false;

    public  static List<TruckModel> getTrucks()
    {
        if (isDebug) {


            List<TruckModel> lstModel = httpClient.getTrucks();

            if (lstModel != null) {
                int[] ids = new int[lstModel.size()];
                int i = 0;
                for (TruckModel model : lstModel) {
                    repo.insertTruck(Truck.fromTruckModel(model));

                    ids[i++] = model.getId();
                }
                repo.deleteOudateTrucks(ids);
            }


            return repo.getTrucks();
        }
        else
            return  httpClient.getTrucks();

    }

    public static List<RefuelItemData> getRefuelList(boolean self, int type) {
        if (isDebug) {

            new Runnable() {
                @Override
                public void run() {

                    Synchronize();
                }
            }.run();

            ShiftModel shiftModel = FMSApplication.getApplication().getShift();
            Date d = new Date();
            if (shiftModel == null || d.compareTo(shiftModel.getStartTime()) < 0 || d.compareTo(shiftModel.getEndTime()) > 0) {
                shiftModel = httpClient.getShift();
                FMSApplication.getApplication().saveShift(shiftModel);
            }
            ShiftModel selected = shiftModel.isSelected() ? shiftModel : shiftModel.getPrevShift().isSelected() ? shiftModel.getPrevShift() : shiftModel.getNextShift();
            if (selected != null) {


                long start = selected.getStartTime().getTime() - 30 * 60 * 1000;
                long end = selected.getEndTime().getTime() + 30 * 60 * 1000;

                return repo.getRefuelList(FMSApplication.getApplication().getTruckNo(), FMSApplication.getApplication().getTruckId(), self, type, start, end);
            }
            return repo.getRefuelList(FMSApplication.getApplication().getTruckNo(), FMSApplication.getApplication().getTruckId(), self, type);
        }
        else
            return httpClient.getRefuelList(self);
    }

    public static RefuelItemData getRefuelItem(Integer id, Integer localId)
    {
        RefuelItemData remoteItem = null;
        if (isDebug)
        {
            RefuelItem localItem = repo.getRefuel(id, localId);
            remoteItem = httpClient.getRefuelItem(id);

            if (localItem.isLocalModified() || remoteItem == null) {
                remoteItem = localItem.toRefuelItemData();

                List<RefuelItem> others = repo.getOthers(localId);
                remoteItem.setOthers(new ArrayList<>());
                for (RefuelItem item : others) {
                    remoteItem.getOthers().add(item.toRefuelItemData());
                }
            }

            if (remoteItem!=null && localItem == null) {


                    localItem = RefuelItem.fromRefuelItemData(remoteItem);
                    repo.insertRefuel(localItem);
                    remoteItem.setLocalId(localItem.getLocalId());

            }

            return  remoteItem;
        }
        remoteItem = httpClient.getRefuelItem(id);
        return  remoteItem;
    }

    public static void Synchronize() {
        if (!processing) {
            processing = true;
            //post local modified data

            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<RefuelItem> modified = repo.getModifiedRefuel();
                    if (modified.size() > 0) {
                        for (RefuelItem item : modified) {
                            RefuelItemData itemData = item.toRefuelItemData();
                            RefuelItemData newData = httpClient.postRefuel(itemData);
                            if (newData != null) {
                                item.setLocalModified(false);
                                item.setId(newData.getId());
                                item.setJsonData(newData.toJson());
                                repo.insertRefuel(item);
                            }
                        }
                    }

                    Date d = repo.getLastModifiedRefuel();
                    List<RefuelItemData> remoteList = httpClient.getModifiedRefuels(0, d);
                    int[] notchanges = repo.getNotChangedRefuels();
                    if (remoteList != null) {
                        int[] ids = new int[remoteList.size()];
                        int i = 0;
                        for (RefuelItemData model : remoteList) {
                            if (!model.isDeleted())
                                repo.insertRefuel(RefuelItem.fromRefuelItemData(model));
                            else if (model.getId() > 0)
                                ids[i++] = model.getId();
                        }
                        repo.removeDeletedRefuels(ids);
                    }


                    processing = false;
                }
            }).start();

            //update airlines, users from another thread
            new Thread(() -> {
                List<AirlineModel> lstModel = httpClient.getAirlines();

                if (lstModel != null) {
                    int[] ids = new int[lstModel.size()];
                    int i = 0;
                    for (AirlineModel model : lstModel) {
                        repo.insertAirline(Airline.fromAirlineModel(model));

                        //ids[i++] = model.getId();
                    }
                    //repo.deleteOudateTrucks(ids);
                }

                //Users

                List<UserModel> lstUser = httpClient.getUsers();

                if (lstUser != null) {
                    int[] ids = new int[lstUser.size()];
                    int i = 0;
                    for (UserModel model : lstUser) {
                        repo.insertUser(User.fromUserModel(model));

                        //ids[i++] = model.getId();
                    }
                    //repo.deleteOudateTrucks(ids);
                }
            }).start();
        }
        //get new remote items


    }

    public static RefuelItemData postRefuel(RefuelItemData refuelData) {
        if (isDebug) {
            Logger.appendLog("DTH", "postRefuel " + refuelData.getId() + " - " + refuelData.getLocalId());
            RefuelItem localItem = repo.getRefuel(refuelData.getId(), refuelData.getLocalId());
            if (localItem == null) {
                localItem = RefuelItem.fromRefuelItemData(refuelData);

            } else {
                localItem.setJsonData(refuelData.toJson());

            }
            RefuelItemData postedItem = httpClient.postRefuel(refuelData);
            if (postedItem != null) {
                localItem.setId(refuelData.getId());
                localItem.setJsonData(postedItem.toJson());
            } else
                localItem.setLocalModified(true);


            repo.insertRefuel(localItem);
            refuelData.setLocalId(localItem.getLocalId());

            // call synchronize to update remote database
            Synchronize();

            return refuelData;
        }
        else
            return  httpClient.postRefuel(refuelData);
    }


    public static List<AirlineModel> getAirlines() {
        if (isDebug) {


            List<AirlineModel> localList = repo.getAirlines();

            return localList;
        }
        return httpClient.getAirlines();

    }

    public static List<UserModel> getUsers() {
        if (isDebug) {
            return repo.getUsers();
        }
        return httpClient.getUsers();
    }
}
