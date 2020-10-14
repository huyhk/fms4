package com.megatech.fms;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;

public class StartupActivity extends BaseActivity {
    protected int SETTING_CODE = 2;
    protected int MAGICAL_NUMBER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        if (currentApp.isLoggedin()) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        } else {

            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }
        finish();
    }


    private void setting() {
        Intent intent = new Intent(this, SettingActivity.class);
        startActivityForResult(intent, SETTING_CODE);
    }

}
