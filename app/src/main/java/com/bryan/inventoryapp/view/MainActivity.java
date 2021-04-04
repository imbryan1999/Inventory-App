package com.bryan.inventoryapp.view;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.bryan.inventoryapp.R;
import com.bryan.inventoryapp.data.InventoryDbHelper;
import com.bryan.inventoryapp.data.StockItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    InventoryDbHelper dbHelper;
    ItemCursorAdapter adapter;
    int lastVisibleItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new InventoryDbHelper(this);

        final FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, DetailsActivity.class);
            startActivity(intent);
        });

        final ListView listView = findViewById(R.id.list_view);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        Cursor cursor = dbHelper.readStock();

        adapter = new ItemCursorAdapter(this, cursor);
        listView.setAdapter(adapter);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == 0) {
                    return;
                }
                final int currentFirstVisibleItem = view.getFirstVisiblePosition();
                if (currentFirstVisibleItem > lastVisibleItem) {
                    fab.show();
                } else if (currentFirstVisibleItem < lastVisibleItem) {
                    fab.hide();
                }
                lastVisibleItem = currentFirstVisibleItem;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        adapter.swapCursor(dbHelper.readStock());
    }

    public void clickOnViewItem(long id) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("itemId", id);
        startActivity(intent);
    }

    public void clickOnSale(long id, int quantity) {
        dbHelper.sellItem(id, quantity);
        adapter.swapCursor(dbHelper.readStock());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_dummy_data:
                // add dummy data for testing
                addDummyData();
                adapter.swapCursor(dbHelper.readStock());
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add data for demo purposes
     */
    private void addDummyData() {
        StockItem iphoneX = new StockItem(
                "Iphone X",
                55000,
                45,
                "Sunil Sharma",
                "9875694865",
                "sunil@gmail.com",
                "android.resource://com.bryan.inventoryapp/drawable/iphonex");
        dbHelper.insertItem(iphoneX);

        StockItem htc = new StockItem(
                "HTC",
                26000, //₹
                35,
                "Shivam Sharma",
                "98487S4865",
                "shivam@gmail.com",
                "android.resource://com.bryan.inventoryapp/drawable/htc");
        dbHelper.insertItem(htc);

        StockItem phillips = new StockItem(
                "Phillips",
                20000,
                55,
                "Rajnish Singh",
                "9875245879",
                "rajnish@gmail.com",
                "android.resource://com.bryan.inventoryapp/drawable/phillips");
        dbHelper.insertItem(phillips);

        StockItem iphone5 = new StockItem(
                "Iphone 5",
                15000,
                22,
                "Rishab Rathore",
                "9874751865",
                "rishab@gmail.com",
                "android.resource://com.bryan.inventoryapp/drawable/iphone5");
        dbHelper.insertItem(iphone5);

        StockItem iphone4 = new StockItem(
                "Iphone 4",
                10000,
                45,
                "Manoj Shrestha",
                "9874123655",
                "manoj@gmail.com",
                "android.resource://com.bryan.inventoryapp/drawable/iphone4");
        dbHelper.insertItem(iphone4);

        StockItem samsung = new StockItem(
                "Samsung",
                18000,
                65,
                "Vishal Shrestha",
                "9875691245",
                "vishal@gmail.com",
                "android.resource://com.bryan.inventoryapp/drawable/samsung");
        dbHelper.insertItem(samsung);

        StockItem sony = new StockItem(
                "Sony",
                40000,
                25,
                "Akash Shrestha",
                "9871254789",
                "akash@gmail.com",
                "android.resource://com.bryan.inventoryapp/drawable/sony");
        dbHelper.insertItem(sony);
    }
}