package com.bryan.inventoryapp.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bryan.inventoryapp.data.StockContract.StockEntry;

import androidx.annotation.Nullable;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "inventory.db";
    public static final int DB_VERSION = 1;
    public static final String LOG_TAG = InventoryDbHelper.class.getCanonicalName();

    public InventoryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(StockEntry.CREATE_TABLE_ITEM);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    // inserting items in database
    public void insertItem(StockItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(StockEntry.COLUMN_NAME, item.getProductName());
        values.put(StockEntry.COLUMN_PRICE, item.getProductPrice());
        values.put(StockEntry.COLUMN_QUANTITY, item.getProductQuantity());
        values.put(StockEntry.COLUMN_SUPPLIER_NAME, item.getSupplierName());
        values.put(StockEntry.COLUMN_SUPPLIER_PHONE, item.getSupplierPhone());
        values.put(StockEntry.COLUMN_SUPPLIER_EMAIL, item.getSupplierEmail());
        values.put(StockEntry.COLUMN_IMAGE, item.getProductImage());
        long id = db.insert(StockEntry.TABLE_NAME, null, values);
    }

    // for reading stocks in database
    public Cursor readStock() {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                StockEntry._ID,
                StockEntry.COLUMN_NAME,
                StockEntry.COLUMN_PRICE,
                StockEntry.COLUMN_QUANTITY,
                StockEntry.COLUMN_SUPPLIER_NAME,
                StockEntry.COLUMN_SUPPLIER_PHONE,
                StockEntry.COLUMN_SUPPLIER_EMAIL,
                StockEntry.COLUMN_IMAGE
        };

        Cursor cursor = db.query(StockEntry.TABLE_NAME,
                projection, null, null, null, null, null);
        return cursor;
    }

    // reading items in database
    public Cursor readItem(long itemId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {
                StockEntry._ID,
                StockEntry.COLUMN_NAME,
                StockEntry.COLUMN_PRICE,
                StockEntry.COLUMN_QUANTITY,
                StockEntry.COLUMN_SUPPLIER_NAME,
                StockEntry.COLUMN_SUPPLIER_PHONE,
                StockEntry.COLUMN_SUPPLIER_EMAIL,
                StockEntry.COLUMN_IMAGE
        };

        String selection = StockEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(itemId)};

        Cursor cursor = db.query(
                StockEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs, null, null, null);
        return cursor;
    }

    // for update the items in database
    public void updateItem(long currentItemId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(StockEntry.COLUMN_QUANTITY, quantity);
        String selection = StockEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(currentItemId)};

        db.update(StockEntry.TABLE_NAME, values, selection, selectionArgs);
    }

    // for selling item
    public void sellItem(long itemId, int quantity) {
        SQLiteDatabase db = getWritableDatabase();
        int newQuantity = 0;
        if (quantity > 0) {
            newQuantity = quantity - 1;
        }

        ContentValues values = new ContentValues();
        values.put(StockEntry.COLUMN_QUANTITY, newQuantity);
        String selection = StockEntry._ID + "=?";
        String[] selectionArgs = new String[]{String.valueOf(itemId)};
        db.update(StockEntry.TABLE_NAME, values, selection, selectionArgs);
    }

}
