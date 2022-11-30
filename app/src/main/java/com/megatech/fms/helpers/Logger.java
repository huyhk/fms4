package com.megatech.fms.helpers;

import android.content.Context;
import android.os.Environment;

import com.megatech.fms.BuildConfig;
import com.megatech.fms.FMSApplication;
import com.megatech.fms.data.AppDatabase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class Logger {

    private static Timer timer;

    public static void appendLog(String logText) {
        appendLog(null, logText);
    }

    public static void appendLog(String tag, String logText) {
        Context ctx = FMSApplication.getApplication();


        String fileName = ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/fms.log";
        File logFile = new File(fileName);

        if (tag != null)
            logText = "[" + tag + "] " + logText;
        if (logFile.exists() && logFile.length() > 1024 * 1024 * 8) {
            Date d = new Date();

            logFile.delete();
           // logFile = new File(fileName);
        }
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            logText = "[" + format.format(new Date()) + "] " + logText;
            buf.append(logText);
            buf.newLine();
            buf.flush();
            buf.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (timer == null) {
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    sendLog();
                }
            }, 1000 * 60 * 6, 1000 * 60 * 60);
        }
    }

    public static boolean sendLog() {
        try {
            Context ctx = FMSApplication.getApplication();
            HttpClient client = new HttpClient();
            String fileName = ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/fms.log";
            String url = BuildConfig.API_BASE_URL + "api/log";
            return client.sendLog(url, fileName);


        } catch (Exception ex) {
            return false;
        }

    }

    public static void writePrintLog(String log)
    {
        String fileName = Environment.getExternalStorageDirectory() + "/data.log";
        File logFile = new File(fileName);
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            //buf.append(log);
            //buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void writeCharArray(String log) {
        String fileName = Environment.getExternalStorageDirectory() + "/data.log";
        File logFile = new File(fileName);
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));

            //SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            for (char ch : log.toCharArray()) {

                buf.append(String.format("%d ", (int) ch));



            }
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
