package com.megatech.fms.helpers;

import androidx.databinding.InverseMethod;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String formatDate(Date date, String pattern)
    {
        if (date == null) return "";
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    public static String getTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        return format.format(date);
    }

    @InverseMethod("getTime")
    public static Date setTime(String time) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            return format.parse(time);
        } catch (ParseException ex) {
            return null;
        }
    }

    public static String getDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        return format.format(date);
    }

    @InverseMethod("getDate")
    public static Date setDate(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            return format.parse(date);
        } catch (ParseException ex) {
            return null;
        }
    }
}
