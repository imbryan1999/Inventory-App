package com.bryan.inventoryapp.view;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NavUtils;
import androidx.core.content.ContextCompat;

import com.bryan.inventoryapp.R;
import com.bryan.inventoryapp.data.InventoryDbHelper;
import com.bryan.inventoryapp.data.StockContract.StockEntry;
import com.bryan.inventoryapp.data.StockItem;

public class DetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();
    private static final int MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private InventoryDbHelper dbHelper;

    EditText nameEdit;
    EditText priceEdit;
    EditText quantityEdit;
    EditText supplierNameEdit;
    EditText supplierPhoneEdit;
    EditText supplierEmailEdit;

    long currentItemId;

    ImageView productImage;

    ImageButton increaseBtn;
    ImageButton decreaseBtn;
    Button imgBtn;

    Uri actualUri;

    private static final int PICK_IMAGE_REQUEST = 0;
    Boolean infoItemHasChanged = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        nameEdit = findViewById(R.id.product_name_edit_text);
        priceEdit = findViewById(R.id.product_price_edit_text);
        quantityEdit = findViewById(R.id.quantity_edit);
        supplierNameEdit = findViewById(R.id.supplier_name_edit_text);
        supplierPhoneEdit = findViewById(R.id.supplier_phone_edit_text);
        supplierEmailEdit = findViewById(R.id.supplier_email_edit_text);
        increaseBtn = findViewById(R.id.increase_quantity);
        decreaseBtn = findViewById(R.id.decrease_quantity);
        productImage = findViewById(R.id.product_image);
        imgBtn = findViewById(R.id.add_image_btn);

        dbHelper = new InventoryDbHelper(this);
        currentItemId = getIntent().getLongExtra("itemId", 0);

        if (currentItemId == 0) {
            setTitle(getString(R.string.editor_activity_add_item));
        } else {
            setTitle(getString(R.string.editor_activity_edit_item));
            addValuesToEditItem(currentItemId);
        }

        decreaseBtn.setOnClickListener(v -> {
            subtractOneToQuantity();
            infoItemHasChanged = true;
        });

        increaseBtn.setOnClickListener(v -> {
            sumOneToQuantity();
            infoItemHasChanged = true;
        });

        imgBtn.setOnClickListener(v -> {
            tryToOpenImageSelector();
            infoItemHasChanged = true;
        });
    }

    @Override
    public void onBackPressed() {
        if (!infoItemHasChanged) {
            super.onBackPressed();
            return;
        }
    }

    DialogInterface.OnClickListener discardButtonClickListener =
            (dialog, which) -> {
                // User clicked "Discard" button, close the current activity.
                finish();
            };

//    Show dialog that there are unsaved changes
//    showUnsavedChangesDialog(discardButtonClickListener);

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void subtractOneToQuantity() {
        String previousValueString = quantityEdit.getText().toString();
        int previousValue;

        if (previousValueString.isEmpty()) {
            return;
        } else if (previousValueString.equals("0")) {
            return;
        } else {
            previousValue = Integer.parseInt(previousValueString);
            quantityEdit.setText(String.valueOf(previousValue - 1));
        }
    }

    private void sumOneToQuantity() {
        String previousValueString = quantityEdit.getText().toString();
        int previousValue;

        if (previousValueString.isEmpty()) {
            previousValue = 0;
        } else {
            previousValue = Integer.parseInt(previousValueString);
        }
        quantityEdit.setText(String.valueOf(previousValue + 1));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (currentItemId == 0) {
            MenuItem deleteOneItemMenuItem = menu.findItem(R.id.delete_item);
            MenuItem deleteAllItemMenuItem = menu.findItem(R.id.delete_all_item);
            MenuItem orderMenuItem = menu.findItem(R.id.action_order);
            deleteOneItemMenuItem.setVisible(false);
            deleteAllItemMenuItem.setVisible(false);
            orderMenuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // save item in DB
                if (!addItemToDb()) {
                    // saying to onOptionsItemSelected that user clicked button
                    return true;
                }
                finish();
                return true;

            case R.id.home:
                if (!infoItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        (dialog, which) -> {
                            NavUtils.navigateUpFromSameTask(DetailsActivity.this);
                        };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

            case R.id.action_order:
                // dialog with phone and email
                showOrderConfirmationDialog();
                return true;

            case R.id.delete_item:
                // delete one item
                showDeleteConfirmationDialog(currentItemId);
                return true;

            case R.id.delete_all_item:
                // delete all data
                showDeleteConfirmationDialog(0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean addItemToDb() {
        boolean isAllOk = true;
        if (!checkIfValueSet(nameEdit, "name")) {
            isAllOk = false;
        }
        if (!checkIfValueSet(priceEdit, "price")) {
            isAllOk = false;
        }
        if (!checkIfValueSet(quantityEdit, "quantity")) {
            isAllOk = false;
        }
        if (!checkIfValueSet(supplierNameEdit, "supplier name")) {
            isAllOk = false;
        }
        if (!checkIfValueSet(supplierPhoneEdit, "supplier phone")) {
            isAllOk = false;
        }
        if (!checkIfValueSet(supplierEmailEdit, "supplier email")) {
            isAllOk = false;
        }
        if (actualUri == null && currentItemId == 0) {
            isAllOk = false;
            imgBtn.setError("Missing Error");
        }
        if (!isAllOk) {
            return false;
        }

        if (currentItemId == 0) {
            StockItem item = new StockItem(
                    nameEdit.getText().toString().trim(),
                    Double.parseDouble(priceEdit.getText().toString().trim()),
                    Integer.parseInt(quantityEdit.getText().toString().trim()),
                    supplierNameEdit.getText().toString().trim(),
                    supplierPhoneEdit.getText().toString().trim(),
                    supplierEmailEdit.getText().toString().trim(),
                    actualUri.toString());
            dbHelper.insertItem(item);
        } else {
            int quantity = Integer.parseInt(quantityEdit.getText().toString().trim());
            dbHelper.updateItem(currentItemId, quantity);
        }
        return true;
    }

    private boolean checkIfValueSet(EditText text, String description) {
        if (TextUtils.isEmpty(text.getText())) {
            text.setError("Missing Product" + description);
            return false;
        } else {
            text.setError(null);
            return true;
        }
    }

    private void addValuesToEditItem(long itemId) {
        Cursor cursor = dbHelper.readItem(itemId);
        cursor.moveToFirst();

        nameEdit.setText(cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_NAME)));
        priceEdit.setText(cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_PRICE)));
        quantityEdit.setText(cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_QUANTITY)));
        supplierNameEdit.setText(cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_SUPPLIER_NAME)));
        supplierPhoneEdit.setText(cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_SUPPLIER_PHONE)));
        supplierEmailEdit.setText(cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_SUPPLIER_EMAIL)));

        productImage.setImageURI(Uri.parse(cursor.getString(cursor.getColumnIndex(StockEntry.COLUMN_IMAGE))));
        nameEdit.setEnabled(false);
        priceEdit.setEnabled(false);
        quantityEdit.setEnabled(false);
        supplierNameEdit.setEnabled(false);
        supplierPhoneEdit.setEnabled(false);
        supplierEmailEdit.setEnabled(false);
        imgBtn.setEnabled(false);
    }

    private void showOrderConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.order_message);
        builder.setPositiveButton(R.string.phone, (dialog, which) -> {
            // intent to phone
            Intent intent = new Intent();
            intent.setData(Uri.parse("tel:" + supplierPhoneEdit.getText().toString().trim()));
            startActivity(intent);
        });

        builder.setNegativeButton(R.string.email, (dialog, which) -> {
            // intent to email
            Intent intent = new Intent();
            intent.setData(Uri.parse("mailto:" + supplierEmailEdit.getText().toString().trim()));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Recurrent new Order");
            String bodyMessage = "Please send us as soon as possible more " +
                    nameEdit.getText().toString().trim() + "!!!";
            intent.putExtra(Intent.EXTRA_TEXT, bodyMessage);
            startActivity(intent);
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private int deleteAllRowsFromTable() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(StockEntry.TABLE_NAME, null, null);
    }

    private int deleteOneItemFromTable(long itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String selection = StockEntry._ID + "=?";
        String[] selectionArgs = {String.valueOf(itemId)};
        int rowsDeleted = db.delete(StockEntry.TABLE_NAME, selection, selectionArgs);
        return rowsDeleted;
    }

    private void showDeleteConfirmationDialog(final long itemId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.delete, (dialog, which) -> {
            if (itemId == 0) {
                deleteAllRowsFromTable();
            } else {
                deleteOneItemFromTable(itemId);
            }
            finish();
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void tryToOpenImageSelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_EXTERNAL_STORAGE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSelector();
                    // permission was granted
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here does't match, it's the response to some other intent,
        // and the below code should't run at all.
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"
            if (data != null) {
                actualUri = data.getData();
                productImage.setImageURI(actualUri);
                productImage.invalidate();
            }
        }
    }
}