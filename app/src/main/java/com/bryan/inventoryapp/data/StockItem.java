package com.bryan.inventoryapp.data;

import androidx.annotation.NonNull;

public class StockItem {

    private final String productName;
    private final double productPrice;
    private final int productQuantity;
    private final String supplierName;
    private final String supplierPhone;
    private final String supplierEmail;
    private final String productImage;

    public StockItem(String productName, double productPrice, int productQuantity,
                     String supplierName, String supplierPhone, String supplierEmail, String productImage) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.productQuantity = productQuantity;
        this.supplierName = supplierName;
        this.supplierPhone = supplierPhone;
        this.supplierEmail = supplierEmail;
        this.productImage = productImage;
    }

    public String getProductName() {
        return productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public int getProductQuantity() {
        return productQuantity;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getSupplierPhone() {
        return supplierPhone;
    }

    public String getSupplierEmail() {
        return supplierEmail;
    }

    public String getProductImage() {
        return productImage;
    }

    @NonNull
    @Override
    public String toString() {
        return "StockItem{" +
                "productName='" + productName + '\'' +
                ", productPrice=" + productPrice +
                ", productQuantity='" + productQuantity +
                ", supplierName='" + supplierName + '\'' +
                ", supplierPhone='" + supplierPhone + '\'' +
                ", supplierEmail='" + supplierEmail + '\'' +
                '}';
    }
}
