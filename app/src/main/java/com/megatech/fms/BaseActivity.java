package com.megatech.fms;

import android.annotation.SuppressLint;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.megatech.fms.helpers.Logger;
import com.megatech.fms.model.UserInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;


public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    protected FMSApplication currentApp;
    protected UserInfo currentUser;
    protected  final String TAG = this.getClass().getName();


    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //if (checkStoragePermission())
        //    checkUpdate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        currentApp = (FMSApplication) this.getApplication();
        currentUser = currentApp.getUser();

        //Logger.appendLog("============================ " + TAG + " ======================== ");
    }

    FloatingActionButton fab;

    private void createFAB() {
        fab = new FloatingActionButton(this);
        fab.setSize(FloatingActionButton.SIZE_MINI);
        RelativeLayout.LayoutParams lay = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        lay.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lay.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lay.setMargins(2, 2, 2, 2);
        fab.setLayoutParams(lay);
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_no_internet, null));

        ((ViewGroup) this.getWindow().getDecorView()).addView(fab);
    }


    @Override
    public void onBackPressed() {
        return;
    }

    AlertDialog progressDialog;

    public void setProgressDialog() {

        int llPadding = 30;
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.HORIZONTAL);
        ll.setPadding(llPadding, llPadding, llPadding, llPadding);
        ll.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llParam = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        ll.setLayoutParams(llParam);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        progressBar.setPadding(0, 0, llPadding, 0);
        progressBar.setLayoutParams(llParam);

        llParam = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        llParam.gravity = Gravity.CENTER;
        TextView tvText = new TextView(this);
        tvText.setText("Loading ...");
        tvText.setTextColor(Color.parseColor("#000000"));
        tvText.setTextSize(20);
        tvText.setLayoutParams(llParam);

        ll.addView(progressBar);
        ll.addView(tvText);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setView(ll);

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(dialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutParams);
        }
        progressDialog = dialog;
    }

    public void closeProgressDialog() {
        if (progressDialog != null)
            progressDialog.dismiss();
    }

    public  void showConfirmMessage( int messageId, Callable<Void> positiveClick) {
        showMessage(R.string.confirm, messageId, R.drawable.ic_question, positiveClick);

    }


    public void showMessage(int titleId, int messageId, int iconId, Callable<Void> positiveClick)
    {
        new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(messageId)
                .setIcon(iconId)
                .setPositiveButton(R.string.accept, (dialog1, which) -> {
                    try {
                        positiveClick.call();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    dialog1.dismiss();
                })
                .setNegativeButton(R.string.back, (dialog1, which) -> {

                    dialog1.dismiss();
                })
                .create()
                .show();

    }
    public  void showWarningMessage( String message) {
        showErrorMessage(R.string.info, message, R.drawable.ic_warning);

    }
    public  void showWarningMessage( int messageId) {
        showErrorMessage(R.string.info, messageId, R.drawable.ic_warning);

    }
    public  void showErrorMessage( int messageId) {
        showErrorMessage(R.string.validate, messageId, R.drawable.ic_error);

    }

    public void showErrorMessage(int titleId, int messageId, int iconId) {
        new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(messageId)
                .setIcon(iconId)
                .setPositiveButton("OK", (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .create()
                .show();
    }
    public void showErrorMessage(int titleId, String message, int iconId) {
        new AlertDialog.Builder(this)
                .setTitle(titleId)
                .setMessage(message)
                .setIcon(iconId)
                .setPositiveButton("OK", (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .create()
                .show();
    }



    @Override
    public void onClick(View view) {
        Logger.appendLog("Click: " + view.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.appendLog("============================ END " + TAG + " ==================== ");
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        return super.onKeyUp(keyCode, event);
    }
}
