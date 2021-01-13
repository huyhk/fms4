package com.megatech.fms.model;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.megatech.fms.FMSApplication;
import com.megatech.fms.helpers.NumberConvert;
import com.megatech.fms.helpers.PrintWorker;
import com.megatech.fms.helpers.ReadNumber;
import com.megatech.fms.helpers.VNCharacterUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.megatech.fms.model.RefuelItemData.GALLON_TO_LITTER;

public class InvoiceModel {

    private static  Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    public static InvoiceModel fromRefuel(RefuelItemData refuel, ArrayList<RefuelItemData> allItems) {
        InvoiceModel model = new InvoiceModel();
        if (allItems !=null)
            refuel = allItems.get(0);



        String data = gson.toJson(refuel);

        model = gson.fromJson(data, InvoiceModel.class);
        model.customerName = refuel.getAirlineModel().getName();
        model.address = refuel.getAirlineModel().getAddress();
        model.taxCode = refuel.getAirlineModel().getTaxCode();
        model.productName = refuel.getAirlineModel().getProductName();
        model.temperature = refuel.getManualTemperature();
        model.amount = refuel.getAmount();
        model.vatAmount = refuel.getVATAmount();
        model.totalAmount = refuel.getTotalAmount();

        // create invoice item list
        addInvoiceItem(model, refuel);
        double totalT = model.getVolume();
        double totalP = model.getTemperature() * model.getVolume();
        double totalW = model.getWeight();
        if (allItems !=null) {
            //model.items = new ArrayList<InvoiceItemModel>(Arrays.asList(gson.fromJson(gson.toJson(allItems), InvoiceItemModel[].class)));
            for (int i = 1; i < allItems.size(); i++) {
                RefuelItemData item = allItems.get(i);
                String itemData = gson.toJson(item);
                addInvoiceItem(model, item);
                //model.items.add(gson.fromJson(itemData, InvoiceItemModel.class));
                model.realAmount += item.getRealAmount();
                model.amount += item.getAmount();
                model.totalAmount += item.getTotalAmount();
                model.vatAmount += item.getVATAmount();

                totalT += item.getVolume();

                totalP += item.getManualTemperature() * item.getVolume();

                totalW += item.getWeight();
            }
            model.density = totalW / totalT;
            model.temperature = totalP / totalT;
        }
        model.inWords = VNCharacterUtils.removeAccent(NumberConvert.NumberToSentence(model.totalAmount, false, model.currency == RefuelItemData.CURRENCY.USD? "USD":"VND"));

        return model;
    }

    private static void addInvoiceItem(InvoiceModel model, RefuelItemData refuel) {
        if (model.items == null)
        model.items = new ArrayList<InvoiceItemModel>();
        String data = gson.toJson(refuel);
        if (refuel.getReturnAmount()>0){

            double returnAmount = refuel.getReturnAmount();
            double returnVolume = refuel.getDensity()>0? (returnAmount / refuel.getDensity()):0;
            double returnGallon = returnVolume / GALLON_TO_LITTER;
            double endNumber = Math.round(refuel.getStartNumber() + returnGallon);

            InvoiceItemModel invItem = gson.fromJson(data, InvoiceItemModel.class);
            invItem.setEndNumber(endNumber);
            invItem.setVolume( returnVolume);
            invItem.setReturn(true);
            model.items.add(invItem);
            InvoiceItemModel invItem2 = gson.fromJson(data, InvoiceItemModel.class);
            invItem2.setStartNumber(endNumber);
            invItem2.setEndNumber(refuel.getStartNumber() + refuel.getRealAmount());
            invItem2.setVolume(refuel.getVolume()- returnVolume);
            invItem2.setReturn(false);
            model.items.add(invItem2);
        }
        else {
            InvoiceItemModel invItem = gson.fromJson(data, InvoiceItemModel.class);

            invItem.setVolume( refuel.getVolume());
            invItem.setEndNumber( refuel.getStartNumber()+ refuel.getRealAmount());

            model.items.add(invItem);
        }
    }

    private Date date;
    private Date startTime;
    private Date endTime;
    private String productName;
    private String customerName;
    private String taxCode;
    private String address;
    private String aircraftType;
    private String aircraftCode;
    private String flightCode;
    private String routeName;
    private double temperature;
    private double density;
    private double volume;
    private double weight;
    private double price;
    private RefuelItemData.CURRENCY currency;
    private double amount;
    private double taxRate;
    private double vatAmount;
    private double totalAmount;
    private String inWords;

    private double realAmount;

    public double getRealAmount() {
        return realAmount;
    }

    public void setRealAmount(double realAmount) {
        this.realAmount = realAmount;
    }

    private List<InvoiceItemModel> items;

    public List<InvoiceItemModel> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemModel> items) {
        this.items = items;
    }

    public InvoiceModel() {

    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getDensity() {
        return density;
    }

    public void setDensity(double density) {
        this.density = density;
    }

    public double getVolume() {
        return Math.round(this.realAmount * RefuelItemData.GALLON_TO_LITTER);
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getWeight() {
        return Math.round(density * getVolume());
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public RefuelItemData.CURRENCY getCurrency() {
        return currency;
    }

    public void setCurrency(RefuelItemData.CURRENCY currency) {
        this.currency = currency;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = taxRate;
    }

    public double getVatAmount() {
        return vatAmount;
    }

    public void setVatAmount(double vatAmount) {
        this.vatAmount = vatAmount;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getInWords() {
        return inWords;
    }

    public void setInWords(String inWords) {
        this.inWords = inWords;
    }

    private String qualityNo;

    public String getQualityNo() {
        return qualityNo;
    }

    public void setQualityNo(String qualityNo) {
        this.qualityNo = qualityNo;
    }

    public ArrayList<String> createBillText()
    {
        Locale locale = new Locale("vi", "VN");
        Locale.setDefault(locale);
        String truckInfo = "";
        if (this.items.size()>3)
        {
            int i=0;
            for (InvoiceItemModel mItem: this.items) {

                truckInfo += String.format("%12s  ", mItem.getTruckNo());
                if (i == 2)
                    truckInfo += String.format("%,12.0f\n\n", this.getVolume());
                else if (i % 3 == 2)
                    truckInfo += "\n\n";
                i++;
            }

            truckInfo += new String(new char[(int)Math.ceil(4-this.items.size() / 2)]).replace('\0','\n');
        }
        else
        {
            for (InvoiceItemModel mItem: this.items)
            {

                truckInfo += String.format("%12s %,15.0f / %,-15.0f   %,12.0f %s\n\n", mItem.getTruckNo(), mItem.getStartNumber(), mItem.getEndNumber(), mItem.getVolume(), mItem.isReturn()?"HT":" ");

            }
            truckInfo += new String(new char[4-this.items.size()]).replace('\0','\n');

        }

        double realAmount = getRealAmount();
        double saleAmount = getAmount();
        double volume = getVolume();
        double weight = getWeight();
        double taxRate = getTaxRate();
        double vatAmount = getVatAmount();
        double totalSaleAmount = getTotalAmount();
        double price = getPrice();
        double temperature = getTemperature();
        double density = getDensity();

        double totalT = volume;
        double totalP = temperature * volume;


        FMSApplication app = FMSApplication.getApplication();
        SimpleDateFormat format = new SimpleDateFormat("          dd           MM         yyyy\n");
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
        ArrayList<String> dataToPrint;
        dataToPrint = new ArrayList<String>(Arrays.asList(
                new String(new char[]{27,51,15}),
                new String(new char[]{27,51,42}),
                format.format(getStartTime()) + "\n",
                new String(new char[]{27,51,20}),

                String.format("                         %s\n",getQualityNo()),
                String.format("                       %s  %s                       %s\n", timeformat.format((this.getStartTime())), timeformat.format(this.getEndTime()), VNCharacterUtils.removeAccent(this.getProductName())),

                new String(new char[]{27,51,8}),

                new String(new char[]{27,51,14}),
                //String.format("    %s           %.0f    %.0f                    %.2f\n",mItem.getTruckNo()  ,mItem.getStartNumber(), mItem.getEndNumber(), mItem.getVolume()),
                truckInfo,

                String.format("                   %s\n", VNCharacterUtils.removeAccent(this.getCustomerName())),
                String.format("                   %s\n", this.getTaxCode()),
                String.format("             %s\n", VNCharacterUtils.removeAccent(this.getAddress())),
                String.format("                    %-10s                          %s\n", this.getAircraftType(),this.getAircraftCode()),
                String.format("                    %-10s                       %s\n",this.getFlightCode(),this.getRouteName()),
                String.format("                            %.2f                       %.4f\n", this.getTemperature(), this.getDensity()),
                String.format("                 %,.0f                                %,.0f\n", this.getRealAmount(), this.getWeight()),
                String.format("                 %,.0f                                \n", this.getVolume())

        ));

        return dataToPrint;
    }

    public ArrayList<String> createInvoiceText()
    {
        Locale locale = new Locale("vi", "VN");
        Locale.setDefault(locale);
        String truckInfo = "";

        if (this.items.size()>3)
        {
            int i=0;
            for (InvoiceItemModel mItem: this.items) {

                truckInfo += String.format("%12s  ", mItem.getTruckNo());
                if (i == 1)
                    truckInfo += String.format("%,12.0f\n\n", this.getVolume());
                else if (i % 2 == 1)
                    truckInfo += "\n";
                i++;
            }
            truckInfo += new String(new char[(int)Math.ceil(4-this.items.size() / 2)]).replace('\0','\n');
        }
        else
        {
            for (InvoiceItemModel mItem: this.items)
            {
                truckInfo += String.format("%12s %,15.0f / %,-15.0f %s  %,12.0f\n\n", mItem.getTruckNo(), mItem.getStartNumber(), mItem.getEndNumber(), mItem.isReturn()?"HT":" ", mItem.getVolume());

            }

            truckInfo += new String(new char[4-this.items.size()]).replace('\0','\n');
        }
        double realAmount = this.getRealAmount();
        double saleAmount = this.getAmount();
        double volume = this.getVolume();
        double weight = this.getWeight();
        double taxRate = this.getTaxRate();
        double vatAmount = this.getVatAmount();
        double totalSaleAmount = this.getTotalAmount();
        double price = this.getPrice();
        double temperature = this.getTemperature();
        double density = this.getDensity();

        double totalT = volume;
        double totalP = temperature * volume;

        ArrayList<String> dataToPrint;


        FMSApplication app = FMSApplication.getApplication();
        SimpleDateFormat format = new SimpleDateFormat("          dd           MM         yyyy\n");
        SimpleDateFormat timeformat = new SimpleDateFormat("HH:mm");
        dataToPrint = new ArrayList<String>(Arrays.asList(
                new String(new char[]{27,51,15}),
                new String(new char[]{27,51,42}),
                format.format(this.getStartTime()) + "\n",
                new String(new char[]{27,51,20}),
                String.format("                         %s\n",this.getQualityNo()),
                String.format("                       %s  %s                       %s\n", timeformat.format((this.getStartTime())), timeformat.format(this.getEndTime()), VNCharacterUtils.removeAccent(this.getProductName())),

                new String(new char[]{27,51,8}),
                "\n",
                new String(new char[]{27,51,14}),
                //String.format("    %s           %.0f    %.0f                    %.2f\n",mItem.getTruckNo()  ,mItem.getStartNumber(), mItem.getEndNumber(), mItem.getVolume()),
                truckInfo,

                String.format("                   %s\n", VNCharacterUtils.removeAccent(this.getCustomerName())),
                String.format("                   %s\n",this.getTaxCode()),
                String.format("             %s\n", VNCharacterUtils.removeAccent(this.getAddress())),
                String.format("                    %-10s                          %s\n", this.getAircraftType(),this.getAircraftCode()),
                String.format("                    %-10s                       %s\n",this.getFlightCode(),this.getRouteName()),
                String.format("                            %.2f                       %.4f\n", temperature, density),
                String.format("                 %,.0f                                %,.0f\n", volume, weight),
                String.format("                   %,.0f                            %,.0f\n", realAmount, price),
                String.format(currency == RefuelItemData.CURRENCY.VND?
                        "                         %,.0f\n"
                        :"                         %,.2f\n", this.getAmount()),
                String.format(currency == RefuelItemData.CURRENCY.VND?
                        "                 %,.0f%%                                 %,.0f\n"
                        :"                 %,.0f%%                                 %,.2f\n", taxRate * 100, vatAmount),

                String.format(currency == RefuelItemData.CURRENCY.VND?
                        "                         %,.0f                     \n"
                        : "                         %,.2f                     \n", totalSaleAmount),
                String.format("                 %s                     \n",inWords)


        ));

        return dataToPrint;
    }
}
