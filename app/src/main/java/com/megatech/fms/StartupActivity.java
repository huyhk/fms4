package com.megatech.fms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.megatech.fms.helpers.Logger;

import java.util.concurrent.Callable;

public class StartupActivity extends BaseActivity {
    protected int SETTING_CODE = 2;
    protected int MAGICAL_NUMBER = 1;

    private int REFUEL_CONTINUE = 44567;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    private void showLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_CODE);
    }

    private int REQUEST_WRITE_PERMISSION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkStoragePermission();

        if (currentApp.isLoggedin()) {
            if (currentApp.isFirstUse()) {
                setting();
            } else {
                showMain();
            }
        } else {

            showLogin();
        }

    }

    private int LOGIN_CODE = 44563;

    private void showMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();

       /* // check incomplete refuel
        // if incomplete refuel exists, open it
        if (currentApp.getCurrentRefuel(true) > 0) {
            Logger.appendLog("STRT", "continue refuel local" + currentApp.getCurrentRefuel(true));
            continueRefuel(0, currentApp.getCurrentRefuel(true));
        } else if (currentApp.getCurrentRefuel(false) > 0) {
            Logger.appendLog("STRT", "continue refuel remote" + currentApp.getCurrentRefuel(false));

            continueRefuel(currentApp.getCurrentRefuel(false), 0);
        }*/
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == LOGIN_CODE) {
            if (resultCode == 0 && !currentApp.isFirstUse()) {
                showMain();
            } else
                setting();
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    private void setting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, SETTING_CODE);
        finish();
    }

    /*private void continueRefuel(int id, int localId) {
        showConfirmMessage(R.string.incomplete_continue, new Callable<Void>() {
            @Override
            public Void call() throws Exception {

                openIncompleteRefuel(id, localId);
                return null;
            }
        });

    }

    private void openIncompleteRefuel(int id, int localId) {
        Intent intent = new Intent(this, RefuelDetailActivity.class);
        intent.putExtra("REFUEL_ID", id);
        intent.putExtra("REFUEL_LOCAL_ID", localId);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);

        currentApp.clearCurrentRefuel();
    }*/

    protected boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.REQUEST_INSTALL_PACKAGES,
                        Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.CAMERA  }, REQUEST_WRITE_PERMISSION);
                return false;
            }

            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }

    private String getTabletId()
    {
        return null;
    }

}
