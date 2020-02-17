package com.megatech.fms;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class UserBaseActivity extends BaseActivity {
    @Override
    protected  void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (!checkLogin()) {
            finish();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

/*
        if (currentApp.isFirstUse())
        {

            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        }
*/

    }

    @Override
    protected void onResume() {
        super.onResume();
        setTruckInfo();

    }

    protected void setTruckInfo() {
        TextView lblInventory = findViewById(R.id.lbltoolbar_Inventory);
        if (lblInventory!=null)
            lblInventory.setText(String.format("%s: %.2f", currentApp.getTruckNo(), currentApp.getCurrentAmount()));
    }

    protected void setToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar!=null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            Button btnInvoice = findViewById(R.id.btnInvoice);
            btnInvoice.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    invoice();
                }
            });
            Button btnInventory = findViewById(R.id.btnAddInventory);
            btnInventory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addInventory();
                }
            });
            Button btnSetting = findViewById(R.id.btnSetting);
            btnSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setting();
                }
            });
            Button btnLogout = findViewById(R.id.btnLogout);
            btnLogout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout();
                }
            });

            setTruckInfo();
        }

    }

    private void addInventory() {
        try {
            Intent intent = new Intent(this, InventoryActivity.class);
            startActivity(intent);
        }
        catch (Exception ex)
        {
            Log.e("INVENTORY", ex.getMessage());
        }

    }

    private void invoice() {
        Intent intent = new Intent(this, InvoiceActivity.class);
        startActivity(intent);

    }


    private boolean checkLogin() {

        return currentApp.isLoggedin();

    }
    protected int SETTING_CODE = 2;
    public void setting()
    {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, SETTING_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //super.onOptionsItemSelected(item);
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_settings:
                setting();
                return true;
            case R.id.action_extract:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
    private  void logout()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.logout_message);
        builder.setPositiveButton(R.string.exit,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doLogout();
                    }
                });
        builder.setNegativeButton(getString(R.string.back),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        // after calling setter methods
        builder.create().show();
    }
    private void doLogout() {
        currentApp.logout();
        finish();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }


}
