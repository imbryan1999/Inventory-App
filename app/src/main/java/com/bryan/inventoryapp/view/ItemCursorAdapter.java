package com.bryan.inventoryapp.view;

import android.content.Context;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bryan.inventoryapp.R;
import com.bryan.inventoryapp.data.StockContract.StockEntry;

public class ItemCursorAdapter extends CursorAdapter {

    private final MainActivity activity;

    public ItemCursorAdapter(MainActivity context, Cursor c) {
        super(context, c, 0);
        this.activity = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.product_name);
        TextView priceTextView = view.findViewById(R.id.product_price);
        TextView quantityTextView = view.findViewById(R.id.product_stock_quantity);
        Button saleBtn = view.findViewById(R.id.sale_btn);
        ImageView image = view.findViewById(R.id.product_image);

        String name = cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_NAME));
        Double price = cursor.getDouble(cursor.getColumnIndex(StockEntry.COLUMN_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(StockEntry.COLUMN_QUANTITY));

        image.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_IMAGE))));

        nameTextView.setText(name);
        priceTextView.setText(String.valueOf(price));
        quantityTextView.setText(String.valueOf(quantity));

        final long id = cursor.getLong(cursor.getColumnIndex(StockEntry._ID));

        view.setOnClickListener(v -> {
            activity.clickOnViewItem(id);
        });

        saleBtn.setOnClickListener(v -> {
            activity.clickOnSale(id, quantity);
        });
    }
}
