package com.megatech.fms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.megatech.fms.model.UserInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.megatech.fms.BuildConfig.API_BASE_URL;
import static com.megatech.fms.BuildConfig.DEBUG;


public class BaseActivity extends AppCompatActivity{
    protected FMSApplication currentApp;
    protected UserInfo currentUser;
    protected static final String TAG = BaseActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (checkStoragePermission())
            checkUpdate();
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        currentApp = (FMSApplication) this.getApplication();
        currentUser = currentApp.getUser();
        if (!currentApp.isFCMSubscribed())
            subscribeFCMTopic();
        
    }
    private void checkUpdate() {



//        if (DEBUG)
//            new UpdateAsyncTask().execute(API_BASE_URL + "files/version.txt",API_BASE_URL + "files/app-debug.apk");
//        else
//            new UpdateAsyncTask().execute(API_BASE_URL + "files/version.txt",API_BASE_URL + "files/app-release.apk");
    }

    private void subscribeFCMTopic() {
//        FirebaseMessaging.getInstance().subscribeToTopic("fms")
//                .addOnCompleteListener(new OnCompleteListener<Void>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Void> task) {
//                        task.notify();
//                    }
//                });
//        currentApp.setFCMSubscribed(true);
    }

    public static boolean isAppWentToBg = false;

    public static boolean isWindowFocused = false;

    public static boolean isMenuOpened = false;

    public static boolean isBackPressed = false;

    public static boolean isFrombg = false;
    @Override
    protected void onStart() {
        Log.d(TAG, "onStart isAppWentToBg " + isAppWentToBg);

        applicationWillEnterForeground();

        super.onStart();
    }
    private void applicationWillEnterForeground()
    {
        if (isAppWentToBg) {
            isAppWentToBg = false;
            //Toast.makeText(getApplicationContext(), "App is back from Background",
            //       Toast.LENGTH_SHORT).show();
            isFrombg = true;
        }
    }
    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop ");
        applicationdidenterbackground();
    }
    public void applicationdidenterbackground() {
        if (!isWindowFocused) {
            isAppWentToBg = true;

        }
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        isWindowFocused = hasFocus;

        if (isBackPressed && !hasFocus) {
            isBackPressed = false;
            isWindowFocused = true;
        }

        super.onWindowFocusChanged(hasFocus);
    }


    private final class UpdateAsyncTask extends AsyncTask<String, Integer, String> {



        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);

                HttpURLConnection urlConnection  = (HttpURLConnection)url.openConnection();
                urlConnection .setRequestMethod("GET");

                urlConnection .setRequestProperty("Content-Type","application/octet-stream");
                urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                urlConnection.setRequestProperty("Accept","*/*");

                urlConnection.connect();
                String version = urlConnection.getContent().toString();
                urlConnection.disconnect();
                if (version !=null) {
                    long newVersion = Long.parseLong(version);
                    long currentVersion = 1;
                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        currentVersion = pInfo.getLongVersionCode();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                    if (newVersion>currentVersion) {

                        url = new URL(urls[1]);

                        urlConnection  = (HttpURLConnection)url.openConnection();
                        urlConnection .setRequestMethod("GET");

                        urlConnection .setRequestProperty("Content-Type","application/octet-stream");
                        urlConnection.setRequestProperty("User-Agent","Mozilla/5.0 ( compatible ) ");
                        urlConnection.setRequestProperty("Accept","*/*");

                        urlConnection.connect();
                        File sdcard = new File(Environment.getExternalStorageDirectory() + File.separator + "fms");

                        if (!sdcard.exists())
                            sdcard.mkdirs();
                        File file = new File(sdcard, "fms-release.apk");

                        FileOutputStream fileOutput = new FileOutputStream(file);
                        InputStream inputStream = urlConnection.getInputStream();

                        byte[] buffer = new byte[1024];
                        int bufferLength = 0;

                        while ((bufferLength = inputStream.read(buffer)) > 0) {
                            fileOutput.write(buffer, 0, bufferLength);
                        }
                        fileOutput.close();
                        return file.toString();
                    }
                }

                    return  null;

            }catch (IOException e)
            {
                Log.e("FMS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String filePath) {
            if (filePath != null) {
                StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                StrictMode.setVmPolicy(builder.build());
                Intent i = new Intent(Intent.ACTION_VIEW);

                i.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                getApplicationContext().startActivity(i);
            }
        }
    }
    private int REQUEST_WRITE_PERMISSION = 1;
    private boolean checkStoragePermission()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_PERMISSION);
                return false;
            }
            return true;
        }
        return  true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        checkUpdate();
    }
}
