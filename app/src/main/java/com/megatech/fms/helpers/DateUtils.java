package com.megatech.fms.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {

    public static String formatDate(Date date, String pattern)
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

}
