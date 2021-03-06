package com.megatech.fms;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;

import androidx.appcompat.app.AlertDialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.megatech.fms.model.TruckModel;
import com.megatech.fms.model.UserInfo;
import com.megatech.fms.helpers.HttpClient;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        activity = this;
        if (currentApp.isLoggedin())
        {

            showMain();
            return;
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        setContentView(R.layout.activity_login);
        EditText edt = findViewById(R.id.userName);

        //setTitle(API_BASE_URL);
        Button btnSignin = findViewById(R.id.btnSignin);
        btnSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText txtUser = findViewById(R.id.userName);
                EditText txtPwd = findViewById(R.id.password);
                String user = txtUser.getText().toString();
                String pwd = txtPwd.getText().toString();

                if (!TextUtils.isEmpty(user) && !TextUtils.isEmpty(pwd))
                {
                    HttpClient client = new HttpClient();
                    JSONObject loginData = client.login(user, pwd);
                    if (loginData == null)
                    {
                        showError();
                    }
                    else
                    {
                        if (loginData.has("userName"))
                        {
                            try {
                                currentUser = new UserInfo(0, loginData.getString("userName"), loginData.getString("access_token"), loginData.getInt("permission"));
                                currentUser.addToSharePreferences(currentApp);


                                //check setting to make sure valid truck
                                if (!currentApp.isFirstUse()) {
                                    TruckModel settingModel = currentApp.getSetting();
                                    if (!client.checkTruck(settingModel.getTruckId(), settingModel.getTruckNo())) {
                                        showTruckError();

                                    }
                                }
                                showMain();
                                finish();
                            }
                            catch (JSONException e)
                            {
                                showError();
                            }
                        }
                        else
                            showError();
                    }
                }
                else
                    txtUser.setError("Invalid username or password");
            }
        });




    }

    private void showTruckError() {
        currentApp.clearTruckInfo();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.invalid_truck_info);
        builder.setIcon(R.drawable.ic_warning);

        builder.setPositiveButton(getString(R.string.accept),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        setResult(LOGIN_RESULT_MODIFIED);
                    }
                });
        // after calling setter methods
        builder.create().show();

    }
    private int LOGIN_RESULT_OK = 0;
    private int LOGIN_RESULT_MODIFIED = 1;

    private Activity activity;
    private void showMain() {
        //Intent intent = new Intent(this, MainActivity.class);
        //startActivity(intent);
        setResult(LOGIN_RESULT_OK);
        //finish();
    }

    private void showError()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle(R.string.app_name);
        builder.setMessage(R.string.login_error_message);

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
}
