package com.megatech.fms.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RefuelItemData extends BaseModel implements Cloneable {

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public RefuelItemData()
    {
        Calendar c = Calendar.getInstance();

        refuelTime = new Date();
        c.setTime(refuelTime);
        c.add(Calendar.MINUTE, 15);

        departureTime = c.getTime();
        c.add(Calendar.MINUTE, -30);
        arrivalTime = c.getTime();
        status = REFUEL_ITEM_STATUS.NONE;
    }

    private int id = 0;

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private Integer flightId = 0;
    private String flightCode;

    private String aircraftType;
    private String aircraftCode;
    private String parkingLot;
    private Date refuelTime;
    private double estimateAmount;
    private double realAmount;
    private double temperature;
    private Date endTime = new Date();
    private Date startTime = new Date();
    private Date deviceStartTime = new Date();
    private Date deviceEndTime = new Date();
    private Date arrivalTime;
    private Date departureTime;
    private  double startNumber;
    private  double endNumber;
    private double manualTemperature;

    private Integer userId = 0;
    private Integer truckId = 0;

    private double density;

    private int airlineId = 0;

    //private double weight;
    //private double volume;

    private double price;

    private int unit;

    private boolean isInternational;

    public static RefuelItemData fromJson(String jsonData) {
        return gson.fromJson(jsonData, RefuelItemData.class);
    }

    public boolean isInternational() {
        return isInternational;
    }

    public void setInternational(boolean international) {
        isInternational = international;
    }

    public int getUnit() {
        return unit;
    }

    private String routeName;

    private AirlineModel airlineModel;

    private String productName;

    private String qualityNo;
    private double taxRate;

    public static double GALLON_TO_LITTER = 3.7854;
    public double getVolume (){
        return Math.round(Math.round(realAmount) * RefuelItemData.GALLON_TO_LITTER);
    }
    public  double getWeight(){
        return Math.round(density * getVolume());
    }
    public int getAirlineId() {
        return airlineId;
    }

    public double getPrice() {
        return price;
    }

    public void setUnit(int unit) {
        this.unit = unit;
    }

    public double getTaxRate(){
        return taxRate;
    }
    public void setTaxRate(double taxRate)
    {
        this.taxRate = taxRate;
    }
    public double getVATAmount(){
        return getAmount()* getTaxRate();
    }
    public double getTotalAmount(){
        return getAmount() + getVATAmount();
    }
    public void setPrice(double price) {
        this.price = price;
    }

    public void setAirlineId(int airlineId) {
        this.airlineId = airlineId;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double getManualTemperature() {
        return manualTemperature;
    }

    public void setManualTemperature(double manualTemparature) {
        this.manualTemperature = manualTemparature;
    }
    private ITEM_PRINT_STATUS printStatus = ITEM_PRINT_STATUS.NONE;

    public ITEM_PRINT_STATUS getPrintStatus() {
        return printStatus;
    }

    public void setPrintStatus(ITEM_PRINT_STATUS printStatus) {
        this.printStatus = printStatus;
    }

    private ITEM_POST_STATUS postStatus = ITEM_POST_STATUS.SUCCESS;

    public ITEM_POST_STATUS getPostStatus() {
        return postStatus;
    }

    public void setPostStatus(ITEM_POST_STATUS postStatus) {
        this.postStatus = postStatus;
    }

    public double getStartNumber() {
        return startNumber;
    }

    public void setStartNumber(double startNumber) {
        this.startNumber = startNumber;
    }

    public double getEndNumber() {
        return endNumber;
    }

    public void setEndNumber(double endNumber) {
        this.endNumber = endNumber;
    }

    public Date getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Date arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Date getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(Date departureTime) {
        this.departureTime = departureTime;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setFlightId(Integer flightId) {
        this.flightId = flightId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }



    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    private REFUEL_ITEM_STATUS status;

    public REFUEL_ITEM_STATUS getStatus() {
        return status;
    }

    public void setStatus(REFUEL_ITEM_STATUS status) {
        this.status = status;
    }
    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }
    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getAircraftCode() {
        return aircraftCode;
    }

    public void setAircraftCode(String aircraftCode) {
        this.aircraftCode = aircraftCode;
    }

    public String getParkingLot() {
        return parkingLot;
    }

    public void setParkingLot(String parkingLot) {
        this.parkingLot = parkingLot;
    }

    public Date getRefuelTime() {
        return refuelTime;
    }

    public void setRefuelTime(Date refuelTime) {
        this.refuelTime = refuelTime;
    }

    public double getEstimateAmount() {
        return estimateAmount;
    }

    public void setEstimateAmount(double estimateAmount) {
        this.estimateAmount = estimateAmount;
    }

    public double getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(double realAmount) {
        this.realAmount = Math.round(realAmount);
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getTruckId() {
        return truckId;
    }

    public void setTruckId(int truckId) {
        this.truckId = truckId;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    /*public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }*/

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public AirlineModel getAirlineModel() {
        return airlineModel;
    }

    public void setAirlineModel(AirlineModel airlineModel) {
        this.airlineModel = airlineModel;
    }

    public String getProductName() {
        return  this.productName;
    }

    public  void setProductName(String productName)
    {
        this.productName = productName;
    }

    public String getQualityNo() {
        return qualityNo;
    }

    public void setQualityNo(String qcNo) {
        this.qualityNo = qcNo;
    }


    public Date getDeviceStartTime() {
        return deviceStartTime;
    }

    public void setDeviceStartTime(Date deviceStartTime) {
        this.deviceStartTime = deviceStartTime;
    }

    public Date getDeviceEndTime() {
        return deviceEndTime;
    }

    public void setDeviceEndTime(Date deviceEndTime) {
        this.deviceEndTime = deviceEndTime;
    }

    private double gallon;

    public double getAmount() {
        int precise = this.currency == CURRENCY.VND ? 1 : 100;
        if (unit == 0)
            return (double) Math.round(getGallon() * getPrice() * precise) / precise;
        return (double) Math.round(getWeight() * getPrice() * precise) / precise;
    }

    public void setGallon(double gallon) {
        this.gallon = gallon;
    }

    private String truckNo;
    public String getTruckNo() {
        return  this.truckNo;
    }
    public void setTruckNo(String truckNo) {
        this.truckNo = truckNo;
    }
    public enum ITEM_POST_STATUS
    {
        NONE,
        SUCCESS,
        ERROR
    }
    public enum ITEM_PRINT_STATUS
    {
        NONE,
        SUCCESS,
        ERROR
    }

    private List<RefuelItemData> others = new ArrayList<>();

    public List<RefuelItemData> getOthers() {
        return others;
    }

    public void setOthers(List<RefuelItemData> others) {

        if (others != null)
            this.others = others;
        else this.others = new ArrayList<>();
    }
    private FLIGHT_STATUS flightStatus;

    public FLIGHT_STATUS getFlightStatus() {
        return flightStatus;
    }

    public void setFlightStatus(FLIGHT_STATUS flightStatus) {
        this.flightStatus = flightStatus;
    }

    private REFUEL_ITEM_TYPE refuelItemType;

    public void setTruckId(Integer truckId) {
        this.truckId = truckId;
    }

    public REFUEL_ITEM_TYPE getRefuelItemType() {
        return refuelItemType;
    }

    public void setRefuelItemType(REFUEL_ITEM_TYPE refuelItemType) {
        this.refuelItemType = refuelItemType;
    }

    public enum FLIGHT_STATUS {
        @SerializedName("0")    NONE(0),
        @SerializedName("1") ASSIGNED(1),
        @SerializedName("2") REFUELING(2),
        @SerializedName("3") REFUELED(3);

        private  int value;

        FLIGHT_STATUS(int i) {
            value = i;
        }

    }

    public enum REFUEL_ITEM_TYPE {
        @SerializedName("0") REFUEL(0),
        @SerializedName("1") EXTRACT(1),
        @SerializedName("2") TEST(2);


        private int value;

        REFUEL_ITEM_TYPE(int i) {
            value = i;
        }
    }


    private String invoiceNumber;
    private  double returnAmount;
    private String weightNote;

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public double getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(double returnAmount) {
        this.returnAmount = returnAmount;
    }

    public String getWeightNote() {
        return weightNote;
    }

    public void setWeightNote(String weightNote) {
        this.weightNote = weightNote;
    }

    public enum CURRENCY {
        @SerializedName("0") VND(0),
        @SerializedName("1") USD(1),
        @SerializedName("2") TEST(2);


        private int value;

        CURRENCY(int i) {
            value = i;
        }
    }

    private CURRENCY currency;

    public CURRENCY getCurrency() {
        return currency;
    }

    public void setCurrency(CURRENCY currency) {
        this.currency = currency;
    }

    private  int driverId;

    private  String driverName;

    private int operatorId;

    private String operatorName;

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public int getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(int operatorId) {
        this.operatorId = operatorId;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    private boolean isAlert = false;

    public boolean isAlert() {
        return isAlert;
    }

    public void setAlert(boolean alert) {
        isAlert = alert;
    }

    public double getGallon() {
        return Math.round(realAmount);
    }

    public String toJson()
    {
        return gson.toJson(this);
    }
}
