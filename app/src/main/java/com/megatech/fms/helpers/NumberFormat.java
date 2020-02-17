package com.megatech.fms.helpers;

import java.text.DecimalFormat;

public class NumberFormat {
    public static String format(String pattern, double number)
    {
        DecimalFormat format = new DecimalFormat(pattern);
        return format.format(number);
    }
}
