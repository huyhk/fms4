package com.megatech.fms.helpers;

import com.megatech.fms.FMSApplication;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    public static void appendLog(String logText) {
        String fileName = FMSApplication.getApplication().getApplicationContext().getFilesDir() + "/refuel.log";
        File logFile = new File(fileName);

        if (!logFile.exists() || logFile.length() > 1024 * 1024) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            logText = "[" + format.format(new Date()) + "]" + logText;
            buf.append(logText);
            buf.newLine();
            buf.close();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
