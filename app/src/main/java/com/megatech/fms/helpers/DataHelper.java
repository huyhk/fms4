package com.megatech.fms.helpers;

import android.content.Context;
import android.os.Build;

import com.megatech.fms.BuildConfig;
import com.megatech.fms.FMSApplication;
import com.megatech.fms.data.AppDatabase;
import com.megatech.fms.data.DataRepository;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Truck;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.TruckModel;

import java.util.List;

public class DataHelper {

    private static  Context context = FMSApplication.getApplication();
    private static HttpClient httpClient = new HttpClient();
    private static DataRepository repo = DataRepository.getInstance(AppDatabase.getInstance(context));
    public  static List<TruckModel> getTrucks()
    {
        if (BuildConfig.DEBUG) {


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
        if (BuildConfig.DEBUG) {


            List<RefuelItemData> remoteList = httpClient.getRefuelList(self,0,true);
            int[] notchanges = repo.getNotChangedRefuels();
            if (remoteList != null) {
                int[] ids = new int[remoteList.size()];
                int i = 0;
                for (RefuelItemData model : remoteList) {
                    if (!model.isDeleted())
                        repo.insertRefuel(RefuelItem.fromRefuelItemData(model));
                    else
                        ids[i++] = model.getId();
                }
                repo.removeDeletedRefuels(ids);
            }

            return repo.getRefuelList(FMSApplication.getApplication().getTruckNo(), FMSApplication.getApplication().getTruckId(), self, type);
        }
        else
            return httpClient.getRefuelList(self);
    }


    public static RefuelItemData getRefuelItem(Integer id)
    {
        RefuelItemData remoteItem = null;
        if (BuildConfig.DEBUG)
        {
            RefuelItem localItem = repo.getRefuel(id, 0);
            remoteItem = httpClient.getRefuelItem(id);

            if (localItem.isLocalModified())
                remoteItem = localItem.toRefuelItemData();

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

    public static void Synchronize()
    {}

    public static RefuelItemData postRefuel(RefuelItemData refuelData) {
        if (BuildConfig.DEBUG) {
            new Thread( new Runnable() {
                @Override
                public void run() {
                    RefuelItem localItem = repo.getRefuel(refuelData.getId(), refuelData.getLocalId());
                    if (localItem == null) {
                        localItem = RefuelItem.fromRefuelItemData(refuelData);


                    }
                    else {
                        localItem.setJsonData(refuelData.toJson());

                    }
                    localItem.setLocalModified(true);
                    repo.insertRefuel(localItem);
                    refuelData.setLocalId(localItem.getLocalId());
                }
            }).start();
            return refuelData;
        }
        else
            return  httpClient.postRefuel(refuelData);
    }
}
