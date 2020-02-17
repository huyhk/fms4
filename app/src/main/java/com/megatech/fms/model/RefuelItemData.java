package com.megatech.fms.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RefuelItemData {

    public RefuelItemData()
    {

        status = REFUEL_ITEM_STATUS.NONE;
    }
    private  Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private Integer flightId;
    private String flightCode;

    private String aircraftType;
    private String aircraftCode;
    private String parkingLot;
    private Date refuelTime;
    private double estimateAmount;
    private double realAmount;
    private double temperature;
    private Date endTime = new Date();
    private Integer userId;
    private Integer truckId;
    private Date startTime = new Date();
    private Date arrivalTime;
    private Date departureTime;
    private  double startNumber;
    private  double endNumber;
    private double manualTemperature;

    private double density;

    private int airlineId;

    private double weight;
    private double volume;

    private double price;

    private String routeName;

    private AirlineModel airlineModel;

    private String productName;

    private String qualityNo;
    private double taxRate;

    public double getVolume (){
        return realAmount * 3.7856;
    }
    public  double getWeight(){
            return density * getVolume();
    }
    public int getAirlineId() {
        return airlineId;
    }

    public double getPrice() {
        return price;
    }

    public double getAmount(){
        return getWeight()*getPrice();
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

    public void setTruckId(Integer truckId) {
        this.truckId = truckId;
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
        this.realAmount = realAmount;
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

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

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
        this.others = others;
    }
    private FLIGHT_STATUS flightStatus;

    public FLIGHT_STATUS getFlightStatus() {
        return flightStatus;
    }

    public void setFlightStatus(FLIGHT_STATUS flightStatus) {
        this.flightStatus = flightStatus;
    }

    public enum FLIGHT_STATUS
    {
        @SerializedName("0")    NONE(0),
        @SerializedName("1") ASSIGNED(1),
        @SerializedName("2") REFUELING(2),
        @SerializedName("3") REFUELED(3);

        private  int value;
        FLIGHT_STATUS(int i) {
            value = i;
        }

    }

}
