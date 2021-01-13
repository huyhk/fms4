package com.megatech.fms.model;

import androidx.annotation.NonNull;
import com.megatech.fms.model.RefuelItemData.CURRENCY;

public class AirlineModel {
    private int id;
    private String name;
    private String taxCode;
    private String address;
    private double price;
    private CURRENCY currency;
    private String productName;
    private String code;

    private String invoiceName;
    private String invoiceTaxCode;
    private String invoiceAddress;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public CURRENCY getCurrency() {
        return currency;
    }

    public void setCurrency(CURRENCY currency) {
        this.currency = currency;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getInvoiceName() {
        if (invoiceName == null || invoiceName.isEmpty())
            return name;
        return invoiceName;

    }

    public void setInvoiceName(String invoiceName) {
        this.invoiceName = invoiceName;
    }

    public String getInvoiceTaxCode() {
        if (invoiceTaxCode == null || invoiceTaxCode.isEmpty())
            return taxCode;
        return invoiceTaxCode;
    }

    public void setInvoiceTaxCode(String invoiceTaxCode) {
        this.invoiceTaxCode = invoiceTaxCode;
    }

    public String getInvoiceAddress() {
        if (invoiceAddress == null || invoiceAddress.isEmpty())
            return address;
        return invoiceAddress;
    }

    public void setInvoiceAddress(String invoiceAddress) {
        this.invoiceAddress = invoiceAddress;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
