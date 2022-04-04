package org.apache.airavata.pga.tests.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Eroma on 11/24/14.
 */
public class CurrentDateTime {
    private static final String DATE_PATTERN = "-MM-dd_HH-mm-ss";

    public static String getTodayDate() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DATE_PATTERN);
        return simpleDateFormat.format(date);
    }
}
