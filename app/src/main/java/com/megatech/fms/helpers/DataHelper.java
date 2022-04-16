package com.megatech.fms.helpers;

import android.content.Context;

import com.megatech.fms.BuildConfig;
import com.megatech.fms.FMSApplication;
import com.megatech.fms.UserBaseActivity;
import com.megatech.fms.data.AppDatabase;
import com.megatech.fms.data.DataRepository;
import com.megatech.fms.data.entity.Airline;
import com.megatech.fms.data.entity.BM2505;
import com.megatech.fms.data.entity.Flight;
import com.megatech.fms.data.entity.Invoice;
import com.megatech.fms.data.entity.Receipt;
import com.megatech.fms.data.entity.RefuelItem;
import com.megatech.fms.data.entity.Truck;
import com.megatech.fms.data.entity.TruckFuel;
import com.megatech.fms.data.entity.User;
import com.megatech.fms.model.AirlineModel;
import com.megatech.fms.model.BM2505Model;
import com.megatech.fms.model.FlightModel;
import com.megatech.fms.model.InvoiceFormModel;
import com.megatech.fms.model.InvoiceModel;
import com.megatech.fms.model.REFUEL_ITEM_STATUS;
import com.megatech.fms.model.ReceiptModel;
import com.megatech.fms.model.RefuelItemData;
import com.megatech.fms.model.ShiftModel;
import com.megatech.fms.model.TruckFuelModel;
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
                //repo.deleteOudateTrucks(ids);
            }


            return repo.getTrucks();
        }
        else
            return  httpClient.getTrucks();

    }
    public  static List<TruckModel> getFHSTrucks()
    {

            return repo.getFHSTrucks();


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
                ShiftModel model = httpClient.getShift();
                if (model!=null) {
                    FMSApplication.getApplication().saveShift(model);
                    shiftModel = model;
                }
            }
            if (shiftModel == null)
            {
                shiftModel = new ShiftModel();
                shiftModel.setSelected(true);
            }
            ShiftModel selected = shiftModel.isSelected() ? shiftModel : null;
            if (selected ==null )
            {
                if (shiftModel.getPrevShift()!=null && shiftModel.getPrevShift().isSelected())
                    selected = shiftModel.getPrevShift();
                else if (shiftModel.getNextShift()!=null && shiftModel.getNextShift().isSelected())
                    selected = shiftModel.getNextShift();
            }
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
    public static RefuelItemData getRefuelItem(String uniqueId)
    {
        return getRefuelItem(uniqueId, false);
    }
    public static RefuelItemData getRefuelItem(String uniqueId, boolean locked)
    {
        RefuelItemData remoteItem = httpClient.getRefuelItem(uniqueId);
        if (isDebug)
        {
            RefuelItem localItem = repo.getRefuel(uniqueId);

            if (localItem == null )
            { return null;
            }

            if (localItem.isLocalModified() || remoteItem == null) {
                remoteItem = localItem.toRefuelItemData();
                List<RefuelItem> others = repo.getOthers(uniqueId);
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

        return  remoteItem;
    }

    public static void lockSync()
    {
        locked = true;
    }
    public static void unlockSync() {
        locked = false;
        new Thread(() -> Synchronize()).start();
    }
    private static boolean locked = false;
    public static RefuelItemData getRefuelItem(Integer id, Integer localId)
    {
        RefuelItemData remoteItem = null;
        if (isDebug)
        {
            RefuelItem localItem = repo.getRefuel(id, localId);
            if (id==0 && localItem!=null)
                id = localItem.getId();
            Logger.appendLog("DTH","Start remote loading item " + id + " - " + localId);

            remoteItem = httpClient.getRefuelItem(id);
            Logger.appendLog("DTH","End remote loading item " + id + " - " + localId);
            if ( (localItem!=null && localItem.isLocalModified()) || remoteItem == null) {
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
        if (!processing && !locked) {
            processing = true;
            //post local modified data
            new Thread(new Runnable() {
                @Override
                public void run() {



                    Date d = repo.getLastModifiedRefuel();
                    List<RefuelItemData> remoteList = httpClient.getModifiedRefuels(0, d);
                    int[] notchanges = repo.getNotChangedRefuels();
                    if (remoteList != null) {
                        int[] ids = new int[remoteList.size()];
                        int i = 0;
                        for (RefuelItemData model : remoteList) {
                            if (!model.isDeleted()  ) {
                                RefuelItem remoteItem = RefuelItem.fromRefuelItemData(model);
                                RefuelItem localItem = repo.getRefuel(remoteItem.getUniqueId());
                                if (localItem == null)
                                {
                                    localItem = repo.getRefuel(remoteItem.getId(), remoteItem.getLocalId());
                                }
                                if (localItem == null || ( !localItem.isLocalModified() ))
                                    repo.insertRefuel(remoteItem);

                                //if (model.getStatus() != REFUEL_ITEM_STATUS.DONE) {
                                Flight flight = new Flight();
                                flight.setId(model.getFlightId());
                                flight.setCode(model.getFlightCode());
                                flight.setAircraftCode(model.getAircraftCode());
                                flight.setRefuelScheduledTime(model.getRefuelTime());

                                repo.insertFlight(flight);
                                //}
                            } else if (model.getId() > 0)
                                ids[i++] = model.getId();
                        }
                        repo.removeDeletedRefuels(ids);
                    }

                    List<RefuelItem> modified = repo.getModifiedRefuel();
                    if (modified.size() > 0) {
                        for (RefuelItem item : modified) {
                            RefuelItemData itemData = item.toRefuelItemData();
                            RefuelItemData newData = httpClient.postRefuel(itemData);
                            if (newData != null) {
                                item.setLocalModified(false);
                                item.setId(newData.getId());
                                item.setUniqueId(newData.getUniqueId());
                                item.setPostStatus(RefuelItem.ITEM_POST_STATUS.SUCCESS);
                                //item.setJsonData(newData.toJson());

                                repo.insertRefuel(item);
                            }
                        }
                    }
                    //Delete old records
                    repo.deleteOldRefuels(10);
                    processing = false;

                    //Logger.appendLog("DTH","END Synchornize");


                    List<Receipt> modifiedReceipt = repo.getModifiedReceipt();
                    ReceiptAPI client = new ReceiptAPI();
                    if (modifiedReceipt.size() > 0) {
                        for (Receipt item : modifiedReceipt) {
                            ReceiptModel itemData = item.toModel();
                            ReceiptModel newData = client.post(itemData);

                            if (newData != null) {
                                newData.setPdfPath(itemData.getPdfPath());
                                newData.setSignaturePath(itemData.getSignaturePath());
                                newData.setSellerSignaturePath(itemData.getSellerSignaturePath());
                                newData.setSignImageString(null);
                                newData.setPdfImageString(null);

                                item.setLocalModified(false);

                                item.setId(newData.getId());
                                item.setJsonData(newData.toJson());
                                repo.insertReceipt(item);
                            }
                        }
                    }
                    List<Invoice> modifiedInvoice = repo.getModifiedInvoice();
                    InvoiceAPI invoiceAPI = new InvoiceAPI();
                    if (modifiedInvoice.size() > 0) {
                        for (Invoice item : modifiedInvoice) {
                            InvoiceModel itemData = item.toModel();
                            InvoiceModel newData = invoiceAPI.post(itemData);

                            if (newData != null) {


                                item.setLocalModified(false);

                                item.setId(newData.getId());
                                item.setJsonData(newData.toJson());
                                repo.insertInvoice(item);
                            }
                        }
                    }
                }
             }).start();
            // synchronize truck fuels items
            new Thread(() -> {

                List<TruckFuel> modified = repo.getModifiedTruckFuel();
                if (modified.size() > 0) {
                    for (TruckFuel item : modified) {
                        TruckFuelModel itemData = item.toTruckFuelModel();
                        TruckFuelModel newData = httpClient.postTruckFuel(itemData);
                        if (newData != null) {
                            item.setLocalModified(false);
                            item.setId(newData.getId());
                            item.setJsonData(newData.toJson());
                            repo.insertTruckFuel(item);
                        }
                    }
                }
                List<TruckFuelModel> lstModel = httpClient.getTruckFuels();

                if (lstModel != null) {
                    int[] ids = new int[lstModel.size()];
                    int i = 0;
                    for (TruckFuelModel model : lstModel) {
                        repo.insertTruckFuel(TruckFuel.fromTruckFuelModel(model));

                    }

                }

                //Users


            }).start();

            //sync BM2505
            new Thread(() -> {

                List<BM2505> modified = repo.getModifiedBM2505();
                if (modified.size() > 0) {
                    for (BM2505 item : modified) {
                        BM2505Model itemData = item.toModel();
                        BM2505Model newData = httpClient.postBM2505(itemData);
                        if (newData != null) {
                            item.setLocalModified(false);
                            item.setId(newData.getId());
                            item.setJsonData(newData.toJson());
                            repo.insertBM2505(item);
                        }
                    }
                }
                List<BM2505Model> lstModel = httpClient.getBM2505List();

                if (lstModel != null) {
                    int[] ids = new int[lstModel.size()];
                    int i = 0;
                    for (BM2505Model model : lstModel) {
                        repo.insertBM2505(BM2505.fromModel(model));

                    }

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

                InvoiceFormModel[] invoiceForms =  getInvoiceForms();
                if (invoiceForms!=null)
                    FMSApplication.getApplication().saveInvoiceForms(invoiceForms);

                List<TruckModel> lstTrucks = httpClient.getTrucks();

                if (lstTrucks != null) {
                    int[] ids = new int[lstTrucks.size()];
                    int i = 0;
                    for (TruckModel model : lstTrucks) {
                        repo.insertTruck(Truck.fromTruckModel(model));

                        ids[i++] = model.getId();
                    }
                    //repo.deleteOudateTrucks(ids);
                }

            }).start();

            new Thread(() -> {
                Logger.sendLog();
            }).start();
                    //get n
        }


    }
    public static void postRefuels(List<RefuelItemData> refuels)
    {
        postRefuels(refuels, false);
    }
    public static void postRefuels(List<RefuelItemData> refuels, boolean remotePost )
    {
        try {
            for (RefuelItemData item : refuels) {
                postRefuel(item, remotePost);
            }
        }
        catch (Exception ex)
        {
            Logger.appendLog("DTH", ex.getMessage());
        }
        Synchronize();
    }
    public static RefuelItemData postRefuel(RefuelItemData refuelData)
    {
        RefuelItemData postedItem =  postRefuel(refuelData, true);
        // call synchronize to update remote database
        Synchronize();
        return  postedItem;
    }
    public static RefuelItemData postRefuel(RefuelItemData refuelData, boolean remotePost) {
        if (refuelData !=null) {
            Logger.appendLog("DTH", "postRefuel " + refuelData.getId() + " - " + refuelData.getLocalId());

            RefuelItem localItem = repo.getRefuel(refuelData.getId(), refuelData.getLocalId());
            if (localItem == null) {
                /// if item not exists in local database
                localItem = RefuelItem.fromRefuelItemData(refuelData);

            } else {
                if (localItem.getId()>0 && refuelData.getId() ==0)
                    refuelData.setId((localItem.getId()));
                //refuelData.setLocalModified(true);
                localItem.updateData(refuelData);
                //refuelData.setLocalId(localItem.getLocalId());

            }
            if (remotePost) {
                RefuelItemData postedItem = processing ? null : httpClient.postRefuel(refuelData);
                if (postedItem != null) {
                    localItem.setId(postedItem.getId());
                    localItem.setUniqueId(postedItem.getUniqueId());
                    localItem.setJsonData(postedItem.toJson());
                    localItem.setLocalModified(false);
                } else
                    localItem.setLocalModified(true);
            }
            else
                localItem.setLocalModified(true);

            repo.insertRefuel(localItem);
            refuelData.setLocalId(localItem.getLocalId());
            refuelData.setLocalModified(localItem.isLocalModified());

            //refuelData = localItem.toRefuelItemData();
            return refuelData;
        }
        else
            return  null;
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

    public static InvoiceFormModel[] getInvoiceForms() {

        return httpClient.getInvoiceForms();
    }

    public static void postInvoice(InvoiceModel model) {
        Invoice localModel = Invoice.fromModel(model);
        localModel.setLocalModified(true);
        repo.insertInvoice(localModel);
        InvoiceModel postInv =  new InvoiceAPI().post(model) ;
        if (postInv!=null) {
            localModel.setId(postInv.getId());
            localModel.setLocalModified(false);
            repo.insertInvoice(localModel);
        }
        Synchronize();
    }

    public static RefuelItemData getImcomplete() {
        RefuelItemData item =  repo.getIncomplete(FMSApplication.getApplication().getTruckNo());
        return item;
    }

    public static List<TruckFuelModel> getTruckFuels() {
        return getTruckFuels(new Date());
    }
    public static List<TruckFuelModel> getTruckFuels(Date date) {
        return repo.getTruckFuels(date);
    }

    public static void postTruckFuel(TruckFuelModel model) {
        TruckFuel localModel = TruckFuel.fromTruckFuelModel(model);
        localModel.setLocalModified(true);
        repo.insertTruckFuel(localModel);
        // call synchronize to update remote database
        Synchronize();

    }

    public static void deleteTruckFuels(int[] ids) {
        repo.deleteTruckFuels(ids);
        // call synchronize to update remote database
        Synchronize();

    }

    public static List<BM2505Model> getBM2505List(Date date) {
        return repo.getBM2505List(date);
    }

    public static void postBM2505(BM2505Model model) {

        BM2505 localModel = BM2505.fromModel(model);
        localModel.setLocalModified(true);
        repo.insertBM2505(localModel);
        // call synchronize to update remote database
        Synchronize();
    }

    public static List<FlightModel> getFlights() {
        return getFlights(new Date());
    }


    public static List<FlightModel> getFlights(Date date) {
        return repo.getFlights(date);
    }

    public static void deleteBM2505(int[] ids) {
        repo.deleteBM2505(ids);
    }

    public static void postReceipt(ReceiptModel model) {

        Logger.appendLog("DTH", "Post receipt :" + model.getNumber());
        Receipt localModel = Receipt.fromModel(model);
        localModel.setLocalModified(true);
        if (repo.insertReceipt(localModel)>0) {
            Logger.appendLog("DTH", "Post receipt success:" + model.getNumber());

            ReceiptModel postedReceipt =  new ReceiptAPI().post(model) ;
            if (postedReceipt!=null) {
                localModel.setId(postedReceipt.getId());
                localModel.setLocalModified(false);
                repo.insertReceipt(localModel);
            }
            // call synchronize to update remote database
            Synchronize();
        }
    }

    public static void cancelReceipts(String[] printedItems, String reason) {
        repo.cancelReceipts(printedItems, reason);
        Synchronize();
    }
}
