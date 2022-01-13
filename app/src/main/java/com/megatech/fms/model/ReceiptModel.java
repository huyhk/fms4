package com.megatech.fms.model;

import com.megatech.fms.FMSApplication;
import com.megatech.fms.helpers.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ReceiptModel extends BaseModel{



    public static ReceiptModel createReceipt(List<RefuelItemData> refuels)
    {
        return createReceipt(refuels, false);
    }
    public static ReceiptModel createReceipt(List<RefuelItemData> refuels, boolean isReturn )
    {
        ReceiptModel model = null;


        if (refuels.size()>0)
        {
            RefuelItemData refuel = refuels.get(0);
            String number = refuel.getTruckNo().substring(0,3) + refuel.getTruckNo().substring(refuel.getTruckNo().length()-2);
            //number += DateUtils.formatDate(new Date(),"yyMMddHHmm");

            String data = gson.toJson(refuel);
            model = gson.fromJson(data, ReceiptModel.class);
            model.setNumber(number);
            model.setGallon(0);
            //model.setDate(new Date());
            model.customerId = refuel.getAirlineId();
            model.customerName = refuel.getInvoiceNameCharter().trim();
            if (model.customerName.isEmpty())
                model.customerName = refuel.getAirlineModel().getName().trim();
            model.customerCode = refuel.getAirlineModel().getCode().trim();
            model.customerAddress = refuel.getAirlineModel().getAddress().trim();
            model.taxCode = refuel.getAirlineModel().getTaxCode().trim();
            model.productName = refuel.getAirlineModel().getProductName();
            model.isReturn = isReturn;

            int receiptCount = 1;

            for(RefuelItemData itemData: refuels)
            {

                addItem(model,itemData);
                if (itemData.getStartTime().compareTo(model.getStartTime())<0)
                {
                    model.setStartTime(itemData.getStartTime());
                }

                if (itemData.getEndTime().compareTo(model.getEndTime())>0)
                {
                    model.setEndTime(itemData.getEndTime());
                }
                if (itemData.getReceiptCount()>0)
                    receiptCount = Math.max(receiptCount, refuel.getReceiptCount()+1);
            }

            model.setDate(model.getEndTime());
            number += DateUtils.formatDate(model.getDate(),"yyMMddHHmm") + receiptCount;
            if (model.isReturn )
                number +="HT";

            model.setNumber(number);
        }

        return  model;
    }

    private static void addItem(ReceiptModel model, RefuelItemData itemData) {
        if (model.items == null)
            model.items = new ArrayList<>();

        if (!model.isReturn || itemData.getReturnAmount()>0) {
            ReceiptItemModel itemModel = gson.fromJson(itemData.toJson(), ReceiptItemModel.class);
            itemModel.setRefuelItemId(itemData.getUniqueId());
            itemModel.setTemperature(itemData.getManualTemperature());
            itemModel.setQualityNo(itemData.getQualityNo());
            if (itemData.getReturnAmount() > 0) {

                double returnA = itemData.getReturnAmount();
                double returnV = Math.round(returnA / itemData.getDensity());
                double returnG = Math.round(returnV / RefuelItemData.GALLON_TO_LITTER);

                if (model.isReturn)
                {
                    itemModel.setGallon( returnG);
                    itemModel.setVolume(  returnV);
                    itemModel.setWeight(returnA);
                    itemModel.setEndNumber(itemModel.getStartNumber() + itemModel.getGallon());
                    model.setReturnAmount(model.getReturnAmount() +returnA);
                }
                else
                {
                    itemModel.setGallon(itemModel.getGallon() -  returnG);
                    itemModel.setVolume(itemModel.getVolume() - returnV);
                    itemModel.setWeight(itemModel.getWeight() - returnA);
                    itemModel.setStartNumber(itemModel.getStartNumber() + returnG);
                }

            }

                model.setGallon(model.getGallon() + itemModel.getGallon());
                model.setVolume(model.getVolume() + itemModel.getVolume());
                model.setWeight(model.getWeight() + itemModel.getWeight());
                //model.setReturnAmount(model.getReturnAmount() + itemModel.getReturnAmount());


            model.items.add(itemModel);
        }
    }

    public String createPrintText()
    {
        UserInfo user = FMSApplication.getApplication().getUser();

        String LS_18 =  new String(new char[]{27, 51, 18});
        String LS_24 =  new String(new char[]{27, 51, 24});
        String LS_DEFAULT =  new String(new char[]{27, 50});
        StringBuilder builder = new StringBuilder();
        builder.append("The Seller: " + user.getInvoiceName()+ "\n");
        builder.append("Tax code: " + user.getTaxCode() +"\n");
        builder.append("Address: "+ user.getAddress()+"\n");
        builder.append("------------------------------------------------------------------\n");
        builder.append("                    FUEL DELIVERY RECEIPT                         \n");
        builder.append("                    Phiếu Giao Nhiên Liệu                         \n");
        builder.append("------------------------------------------------------------------\n");
        builder.append(String.format("No.: %-15s            (%s)\n",this.number, DateUtils.formatDate(this.date,"dd/MM/yyyy")));
        builder.append(LS_18);
        builder.append(String.format("Buyer: %s\n",this.customerName));
        builder.append(LS_DEFAULT);
        builder.append("\n");
        builder.append(String.format("A/C Type         : %-16s A/C reg     : %s\n",this.aircraftType, this.aircraftCode));
        builder.append(String.format("Flight No.       : %-16s Route       : %s\n",this.flightCode, this.routeName));
        builder.append(String.format("Cert No.         : %-16s Product Name: %s\n",this.qualityNo, this.productName));
        builder.append(String.format("Start Time       : %-16s End Time    : %s\n",DateUtils.formatDate(this.startTime,"HH:mm dd/MM/yyyy"), DateUtils.formatDate(this.endTime,"HH:mm dd/MM/yyyy")));
        builder.append("Refueling Method : Refueler\n");
        builder.append("------------------------------------------------------------------\n");
        builder.append("| # |  Refueler No.     |   Temp.   |   USG  |  Litter |    Kg   |\n");
        builder.append(LS_18);
        builder.append("|   | Start/End Meter   | Density   |        |         |         |\n");
        builder.append(LS_DEFAULT);
        builder.append("------------------------------------------------------------------\n");
        int i=1;
        for (ReceiptItemModel itemModel: this.items)
        {
            builder.append(String.format("|%2d |%-19s|%8.2f oC|%8.0f|%9.0f|%9.0f|\n",i++,itemModel.getTruckNo(), itemModel.getTemperature(), itemModel.getGallon(), itemModel.getVolume(), itemModel.getWeight()));
            builder.append(LS_18);
            builder.append(String.format("|   |%9.0f/%-9.0f|%6.4f kg/l|        |         |         |\n",itemModel.getStartNumber(), itemModel.getEndNumber(), itemModel.getDensity()));
            builder.append(LS_DEFAULT);
            builder.append("------------------------------------------------------------------\n");
        }
        builder.append(LS_18);
        builder.append(String.format("|   | Total                         |%8.0f|%9.0f|%9.0f|\n", this.gallon, this.volume, this.weight));
        builder.append(LS_DEFAULT);
        builder.append("------------------------------------------------------------------\n");
        builder.append("           Buyer                            Seller        \n");
        builder.append("  (Signature and full name)      (Signature and full name)     \n");
        return  builder.toString();

    }
    public String createThermalText()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("^XA");
        builder.append("^CWZ,E:OPENSANS-RE.TTF^FS  \n" +
                "^LH100,0\n" +
                "^CI28");
        builder.append("^CFZ,25\n" +
                "^FO10,50^FB600,2,0,C,0^FDVIETNAM AIR PETROL ONE MEMBER COMPANY LIMITED^FS\n" +
                "^CFZ,40\n" +
                "^FO10,100^FB600,1,0,C,0^FDFUEL DELIVERY RECEIPT^FS\n" +
                "^FO10,140^FB600,1,0,C,0^FD(PHIẾU GIAO NHIÊN LIỆU)^FS");
        builder.append("^CFZ,20\n" +
                "^FO10,180^FB600,1,0,C,0^FDReceipt No. : " + this.number + "^FS\n" +
                "^FO10,200^FB600,1,0,C,0^FD" + DateUtils.formatDate(this.date, "dd/MM/yyyy")+ "^FS\n" +
                "^FO10,220^GB700,1,3^FS");
        builder.append("^CFZ,30");
        builder.append("^FO10,230^FB250,1,0,L,0^FDBuyer ^FS\n" +
                "^FO260,230^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,230^FB350,2,0,L,0^FD" + this.customerName + "^FS");


        builder.append("^FO10,290^FB250,1,0,L,0^FDFlight No.        ^FS\n" +
                "^FO260,290^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,290^FB350,1,0,R,0^FD\t"+flightCode+"^FS\n" +
                "^FO10,320^FB250,1,0,L,0^FDRoute        ^FS\n" +
                "^FO260,320^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,320^FB350,1,0,R,0^FD\t"+routeName+ "^FS\n" +
                "^FO10,350^FB250,1,0,L,0^FDA/C Type        ^FS\n" +
                "^FO260,350^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,350^FB350,1,0,R,0^FD\t"+aircraftType+"^FS\n" +
                "^FO10,380^FB250,1,0,L,0^FDA/C Reg        ^FS\n" +
                "^FO260,380^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,380^FB350,1,0,R,0^FD\t"+aircraftCode+"^FS\n" +
                "^FO10,410^FB250,1,0,L,0^FDCert No.        ^FS\n" +
                "^FO260,410^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,410^FB350,1,0,R,0^FD\t"+qualityNo+"^FS\n" +
                "^FO10,440^FB250,1,0,L,0^FDStart Time        ^FS\n" +
                "^FO260,440^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,440^FB350,1,0,R,0^FD\t"+ DateUtils.formatDate(startTime,"HH:mm dd/MM/yyyy")+"^FS\n" +
                "^FO10,470^FB250,1,0,L,0^FDEnd Time        ^FS\n" +
                "^FO260,470^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,470^FB350,1,0,R,0^FD\t"+DateUtils.formatDate(endTime,"HH:mm dd/MM/yyyy")+"^FS\n" +
                "^FO10,500^FB250,1,0,L,0^FDProduct Name        ^FS\n" +
                "^FO260,500^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,500^FB350,1,0,R,0^FD\tJet A-1^FS\n" +
                "^FO10,530^FB250,1,0,L,0^FDRefueling Method^FS\n" +
                "^FO260,530^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,530^FB350,1,0,R,0^FD\tFHS^FS  \n" +
                "^FO10,560^GB700,1,3^FS");

        builder.append("^CFZ,40\n" +
                "^FO10,570^FB600,1,0,C,0^FDDETAIL^FS\n" +
                "^FO10,610^GB700,1,3^FS");

        builder.append("^CFZ,30");
        int i = 1;
        int height = 620;
        for (ReceiptItemModel item: items) {
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FD#        ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + i + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDRefueler No.        ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + item.getTruckNo() + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDStart Meter        ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f", item.getStartNumber()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDEnd Meter        ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",item.getEndNumber()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDTemp.(°C)       ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.2f",item.getTemperature()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDDensity(kg/l)      ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.4f",item.getDensity()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDUSG      ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",item.getGallon()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDLiter      ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",item.getVolume()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDKg      ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",item.getWeight())+ "^FS");
            height +=30;
            builder.append(" \"^FO10,"+height+"^GB700,1,3^FS\"");
            height +=10;
            i++;

        }


        builder.append("^CFZ,40\n" +
                "^FO10,"+height+"^FB600,1,0,C,0^FDTOTAL^FS" );
        height +=40;
        builder.append("^FO10,"+height+"^GB700,1,3^FS");
        builder.append("^CFZ,30\n");
        height +=10;
        builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDUSG      ^FS\n" +
                "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",gallon) + "^FS");
        height +=30;
        builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDLiter      ^FS\n" +
                "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",volume) + "^FS");
        height +=30;
        builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDKg      ^FS\n" +
                "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",weight)+ "^FS");
        height +=30;
        builder.append("^FO10,"+height+"^GB700,1,3^FS");
        height +=10;
        builder.append("^FO10,"+height+"^FB600,1,0,C,0^FDBuyer^FS");
        //print signature
        if (signaturePath !=null)
        {
            height +=20;
            builder.append("^FO150,"+height+"^XGE:BUYER.GRF,1,1^FS");
        }
        height +=200;
        builder.append("^FO10,"+height+"^FB600,1,0,C,0^FDSeller^FS");

        if (sellerSignaturePath !=null)
        {
            height +=20;
            builder.append("^FO150,"+height+"^XGE:SELLER.GRF,1,1^FS");
        }
        builder.append("^PQ1");

        builder.append("^XZ");
        return builder.toString();

    }
    public String createReturnThermalText()
    {
        StringBuilder builder = new StringBuilder();

        builder.append("^XA");
        builder.append("^CWZ,E:OPENSANS-RE.TTF^FS  \n" +
                "^LH100,0\n" +
                "^CI28");
        builder.append("^CFZ,25\n" +
                "^FO10,50^FB600,2,0,C,0^FDVIETNAM AIR PETROL ONE MEMBER COMPANY LIMITED^FS\n" +
                "^CFZ,40\n" +
                "^FO10,100^FB600,1,0,C,0^FDFUEL RETURNING FORM^FS\n" +
                "^FO10,140^FB600,1,0,C,0^FD(PHIẾU HOÀN TRẢ NHIÊN LIỆU)^FS");
        builder.append("^CFZ,20\n" +
                "^FO10,180^FB600,1,0,C,0^FDReceipt No. : " + this.number + "^FS\n" +
                "^FO10,200^FB600,1,0,C,0^FD" + DateUtils.formatDate(this.date, "dd/MM/yyyy")+ "^FS\n" +
                "^FO10,220^GB700,1,3^FS");
        builder.append("^CFZ,30");
        builder.append("^FO10,230^FB250,1,0,L,0^FDBuyer ^FS\n" +
                "^FO260,230^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,230^FB350,2,0,L,0^FD" + this.customerName + "^FS");


        builder.append("^FO10,290^FB250,1,0,L,0^FDFlight No.        ^FS\n" +
        "^FO260,290^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,290^FB350,1,0,R,0^FD\t"+flightCode+"^FS\n" +
        "^FO10,320^FB250,1,0,L,0^FDRoute        ^FS\n" +
        "^FO260,320^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,320^FB350,1,0,R,0^FD\t"+routeName+ "^FS\n" +
        "^FO10,350^FB250,1,0,L,0^FDA/C Type        ^FS\n" +
        "^FO260,350^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,350^FB350,1,0,R,0^FD\t"+aircraftType+"^FS\n" +
        "^FO10,380^FB250,1,0,L,0^FDA/C Reg        ^FS\n" +
        "^FO260,380^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,380^FB350,1,0,R,0^FD\t"+aircraftCode+"^FS\n" +
        "^FO10,410^FB250,1,0,L,0^FDCert No.        ^FS\n" +
        "^FO260,410^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,410^FB350,1,0,R,0^FD\t"+qualityNo+"^FS\n" +
        "^FO10,440^FB250,1,0,L,0^FDStart Time        ^FS\n" +
        "^FO260,440^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,440^FB350,1,0,R,0^FD\t"+ DateUtils.formatDate(startTime,"HH:mm dd/MM/yyyy")+"^FS\n" +
        "^FO10,470^FB250,1,0,L,0^FDEnd Time        ^FS\n" +
        "^FO260,470^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,470^FB350,1,0,R,0^FD\t"+DateUtils.formatDate(endTime,"HH:mm dd/MM/yyyy")+"^FS\n" +
        "^FO10,500^FB250,1,0,L,0^FDProduct Name        ^FS\n" +
        "^FO260,500^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,500^FB350,1,0,R,0^FD\tJet A-1^FS\n" +
        "^FO10,530^FB250,1,0,L,0^FDRefueling Method^FS\n" +
        "^FO260,530^FB10,1,0,C,0^FD:^FS\n" +
        "^FO270,530^FB350,1,0,R,0^FD\tFHS^FS  \n" +
        "^FO10,560^GB700,1,3^FS");

        builder.append("^CFZ,40\n" +
                "^FO10,570^FB600,1,0,C,0^FDDETAIL^FS\n" +
                "^FO10,610^GB700,1,3^FS");

        builder.append("^CFZ,30");
        int i = 1;
        int height = 620;
        for (ReceiptItemModel item: items) {
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FD#        ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + i + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDRefueler No.        ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + item.getTruckNo() + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDStart Meter        ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f", item.getStartNumber()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDEnd Meter        ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",item.getEndNumber()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDTemp.(°C)       ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.2f",item.getTemperature()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDDensity(kg/l)      ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.4f",item.getDensity()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDUSG      ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",item.getGallon()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDLiter      ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",item.getVolume()) + "^FS");
            height +=30;
            builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDKg      ^FS\n" +
                    "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                    "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",item.getWeight())+ "^FS");
            height +=30;
            builder.append(" \"^FO10,"+height+"^GB700,1,3^FS\"");
            height +=10;
            i++;

        }

        
        builder.append("^CFZ,40\n" +
                "^FO10,"+height+"^FB600,1,0,C,0^FDTOTAL^FS" );
        height +=40;
        builder.append("^FO10,"+height+"^GB700,1,3^FS");
        builder.append("^CFZ,30\n");
        height +=10;
        builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDUSG      ^FS\n" +
                "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",gallon) + "^FS");
        height +=30;
        builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDLiter      ^FS\n" +
                "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",volume) + "^FS");
        height +=30;
        builder.append("^FO10,"+height+"^FB250,1,0,L,0^FDKg      ^FS\n" +
                "^FO260,"+height+"^FB10,1,0,C,0^FD:^FS\n" +
                "^FO270,"+height+"^FB350,1,0,R,0^FD\t" + String.format("%.0f",weight)+ "^FS");
        height +=30;
        builder.append("^FO10,"+height+"^GB700,1,3^FS");
        height +=10;
        builder.append("^FO10,"+height+"^FB600,1,0,C,0^FDBuyer^FS");
        //print signature
        if (signaturePath !=null)
        {
            height +=20;
            builder.append("^FO150,"+height+"^XGE:BUYER.GRF,1,1^FS");
        }
        height +=200;
        builder.append("^FO10,"+height+"^FB600,1,0,C,0^FDSeller^FS");

        if (sellerSignaturePath !=null)
        {
            height +=20;
            builder.append("^FO150,"+height+"^XGE:SELLER.GRF,1,1^FS");
        }
        builder.append("^PQ1");

        builder.append("^XZ");
        return builder.toString();

    }
    public String createReturnText()
    {
        UserInfo user = FMSApplication.getApplication().getUser();

        String LS_18 =  new String(new char[]{27, 51, 18});
        String LS_24 =  new String(new char[]{27, 51, 24});
        String LS_DEFAULT =  new String(new char[]{27, 50});
        StringBuilder builder = new StringBuilder();
        builder.append("The Seller: " + user.getInvoiceName()+ "\n");
        builder.append("Tax code: " + user.getTaxCode() +"\n");
        builder.append("Address: "+ user.getAddress()+"\n");
        builder.append("------------------------------------------------------------------\n");
        builder.append("                    FUEL RETURNING FORM                           \n");
        builder.append("                    Phiếu Hoàn Trả Nhiên Liệu                     \n");
        builder.append("------------------------------------------------------------------\n");
        builder.append(String.format("No.: %-15s            (%s)\n",this.number, DateUtils.formatDate(this.date,"dd/MM/yyyy")));
        builder.append(LS_18);
        builder.append(String.format("Buyer: %s\n",this.customerName));
        builder.append(LS_DEFAULT);
        builder.append("\n");
        builder.append(String.format("A/C Type         : %-16s A/C reg     : %s\n",this.aircraftType, this.aircraftCode));
        builder.append(String.format("Flight No.       : %-16s Route       : %s\n",this.flightCode, this.routeName));
        builder.append(String.format("Cert No.         : %-16s Product Name: %s\n",this.qualityNo, this.productName));
        builder.append(String.format("Start Time       : %-16s End Time    : %s\n",DateUtils.formatDate(this.startTime,"HH:mm dd/MM/yyyy"), DateUtils.formatDate(this.endTime,"HH:mm dd/MM/yyyy")));
        builder.append("Refueling Method : Refueler\n");
        builder.append("------------------------------------------------------------------\n");
        builder.append("| # |  Refueler No.     |   Temp.   |   USG  |  Litter |    Kg   |\n");
        builder.append(LS_18);
        builder.append("|   | Start/End Meter   | Density   |        |         |         |\n");
        builder.append(LS_DEFAULT);
        builder.append("------------------------------------------------------------------\n");
        int i=1;
        for (ReceiptItemModel itemModel: this.items)
        {
            builder.append(String.format("|%2d |%-19s|%8.2f oC|%8.0f|%9.0f|%9.0f|\n",i++,itemModel.getTruckNo(), itemModel.getTemperature(), itemModel.getGallon(), itemModel.getVolume(), itemModel.getWeight()));
            builder.append(LS_18);
            builder.append(String.format("|   |%9.0f/%-9.0f|%6.4f kg/l|        |         |         |\n",itemModel.getStartNumber(), itemModel.getEndNumber(), itemModel.getDensity()));
            builder.append(LS_DEFAULT);
            builder.append("------------------------------------------------------------------\n");
        }
        builder.append(LS_18);
        builder.append(String.format("|   | Total                         |%8.0f|%9.0f|%9.0f|\n", this.gallon, this.volume, this.weight));
        builder.append(LS_DEFAULT);
        builder.append("------------------------------------------------------------------\n");
        builder.append("           Buyer                            Seller        \n");
        builder.append("  (Signature and full name)      (Signature and full name)     \n");
        return  builder.toString();
    }
    private String number;
    private Date date;

    private int customerId;
    private String customerName;
    private String customerCode;
    private String customerAddress;
    private String taxCode;
    private String productName;

    private int flightId;
    private String flightCode;
    private String aircraftCode;
    private String aircraftType;
    private String routeName;

    private String qualityNo;
    private Date startTime;
    private Date endTime;


    private double gallon;
    private double volume;
    private double weight;

    private double returnAmount;
    private String defuelingNo;

    private boolean invoiceSplit;
    private double splitAmount;

    List<ReceiptItemModel> items;

    public static ReceiptModel fromJson(String data) {

        return gson.fromJson(data, ReceiptModel.class);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getFlightId() {
        return flightId;
    }

    public void setFlightId(int flightId) {
        this.flightId = flightId;
    }

    public String getFlightCode() {
        return flightCode;
    }

    public void setFlightCode(String flightCode) {
        this.flightCode = flightCode;
    }

    public String getAircraftCode() {
        return aircraftCode;
    }

    public void setAircraftCode(String aircraftCode) {
        this.aircraftCode = aircraftCode;
    }

    public String getAircraftType() {
        return aircraftType;
    }

    public void setAircraftType(String aircraftType) {
        this.aircraftType = aircraftType;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getQualityNo() {
        return qualityNo;
    }

    public void setQualityNo(String qualityNo) {
        this.qualityNo = qualityNo;
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

    public double getGallon() {
        return gallon;
    }

    public void setGallon(double gallon) {
        this.gallon = gallon;
    }

    public double getVolume() {
        return volume;
    }

    public void setVolume(double volume) {
        this.volume = volume;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public double getReturnAmount() {
        return returnAmount;
    }

    public void setReturnAmount(double returnAmount) {
        this.returnAmount = returnAmount;
    }

    public String getDefuelingNo() {
        return defuelingNo;
    }

    public void setDefuelingNo(String defuelingNo) {
        this.defuelingNo = defuelingNo;
    }

    public boolean isInvoiceSplit() {
        return invoiceSplit;
    }

    public void setInvoiceSplit(boolean invoiceSplit) {
        this.invoiceSplit = invoiceSplit;
    }

    public double getSplitAmount() {
        return splitAmount;
    }

    public void setSplitAmount(double splitAmount) {
        this.splitAmount = splitAmount;
    }

    public List<ReceiptItemModel> getItems() {
        return items;
    }

    public void setItems(List<ReceiptItemModel> items) {
        this.items = items;
    }

    private String pdfImageString;

    public String getPdfImageString() {
        return pdfImageString;
    }

    public void setPdfImageString(String pdfImageString) {
        this.pdfImageString = pdfImageString;
    }

    private String pdfPath;
    private String signaturePath;
    private String sellerSignaturePath;

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public String getSellerSignaturePath() {
        return sellerSignaturePath;
    }

    public void setSellerSignaturePath(String sellerSignaturePath) {
        this.sellerSignaturePath = sellerSignaturePath;
    }

    private String signImageString;

    public String getSignImageString() {
        return signImageString;
    }

    public void setSignImageString(String signImageString) {
        this.signImageString = signImageString;
    }

    private boolean printed = false;

    private boolean captured = false;

    public boolean isPrinted() {
        return printed;
    }

    public void setPrinted(boolean printed) {
        this.printed = printed;
    }

    public boolean isCaptured() {
        return captured;
    }

    public void setCaptured(boolean captured) {
        this.captured = captured;
    }

    private REFUEL_METHOD refuelMethod;

    public REFUEL_METHOD getRefuelMethod() {
        return refuelMethod;
    }

    public void setRefuelMethod(REFUEL_METHOD refuelMethod) {
        this.refuelMethod = refuelMethod;
    }

    public enum REFUEL_METHOD
    {
        REFUELER,
        FHS
    }

    private boolean isReturn;

    public boolean isReturn() {
        return isReturn;
    }

    public void setReturn(boolean aReturn) {
        isReturn = aReturn;
    }
}
