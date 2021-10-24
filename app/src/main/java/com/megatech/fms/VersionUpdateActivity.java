package com.megatech.fms;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.megatech.fms.helpers.HttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.megatech.fms.BuildConfig.API_BASE_URL;

public class VersionUpdateActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG)
        {
            throw new RuntimeException("Test Crash"); // Force a crash
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_version_update);
        TextView txt = findViewById(R.id.info_dialog_version);
        txt.setText(String.format("%s",BuildConfig.VERSION_NAME));

        new CheckVersionAsyncTask().execute(API_BASE_URL + "files/version.txt");
    }

    String update_url;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btnUpdate:

                findViewById(R.id.btnUpdate).setEnabled(false);
                ((Button) findViewById(R.id.btnUpdate)).setText(R.string.version_updating);
                if (checkStoragePermission())
                    new UpdateAsyncTask().execute(update_url);
                break;
            case R.id.btnBack:
                finish();
                break;
        }
    }

    private final class CheckVersionAsyncTask extends AsyncTask<String, Integer, String> {
        @Override
        protected String doInBackground(String... strings) {
            String url = strings[0];
            HttpClient client = new HttpClient();
            String data = client.getContent(url);
            if (data != null) {
                String[] info = data.split("\\-");

                return data;
            }
            return null;
        }

        @Override
        protected void onPostExecute(String versionInfo) {
            try {
                String[] info = versionInfo.split("-");
                String version = info[0];
                String build = info[1];
                long newVersion = Long.parseLong(version);
                long currentVersion = BuildConfig.VERSION_CODE;
                String currentVersionName = BuildConfig.VERSION_NAME;

                if (newVersion > currentVersion) {
                    ((TextView) findViewById(R.id.version_check_message)).setText(getString(R.string.new_version_available));
                    ((TextView) findViewById(R.id.version_check_message)).setTextColor(getResources().getColor(R.color.colorPrimary));
                    findViewById(R.id.btnUpdate).setEnabled(true);
                } else {
                    ((TextView) findViewById(R.id.version_check_message)).setText(getString(R.string.newest_version_using));
                }
                update_url = API_BASE_URL + "/files/" + "fms-release-" + versionInfo + ".apk";
                //if (DEBUG)
                 //   update_url = API_BASE_URL + "/files/" + "fms-debug-" + versionInfo + ".apk";
            }
            catch (Exception ex)
            {
                showErrorMessage(R.string.file_update_error);
            }
            //super.onPostExecute(aLong);
        }
    }

    private final class UpdateAsyncTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {

                URL url = new URL(urls[0]);


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");

                urlConnection.setRequestProperty("Content-Type", "application/octet-stream");
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 ( compatible ) ");
                urlConnection.setRequestProperty("Accept", "*/*");

                urlConnection.connect();
                File sdcard = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "fms");

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


            } catch (IOException e) {
                Log.e("FMS", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(String filePath) {

            try {
                if (filePath != null) {
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);

                    //i.setDataAndType(Uri.fromFile(new File(filePath)), "application/vnd.android.package-archive");
                    intent.setData(Uri.fromFile(new File(filePath)));
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    getApplicationContext().startActivity(intent);
                }
            } catch (Exception ex) {
                Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
            }
            finally {
                //currentApp.clearSetting();
            }
        }
    }

    private int REQUEST_WRITE_PERMISSION = 1;

    protected boolean checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.REQUEST_INSTALL_PACKAGES) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.REQUEST_INSTALL_PACKAGES}, REQUEST_WRITE_PERMISSION);
                return false;
            }
            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        new UpdateAsyncTask().execute(update_url);
    }

}
