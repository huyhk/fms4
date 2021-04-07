package com.megatech.fms;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class StartupActivity extends BaseActivity {
    protected int SETTING_CODE = 2;
    protected int MAGICAL_NUMBER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_startup);

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
        startActivityForResult(intent,LOGIN_CODE );
    }

    private void showMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private int LOGIN_CODE = 44563;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == LOGIN_CODE)
        {
            if (resultCode == 0 && ! currentApp.isFirstUse()){
                showMain();
            }
            else
                setting();
        }
        else
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, SETTING_CODE);
        finish();
    }

}
