package org.apache.airavata.credential.store.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Contains some utility methods.
 */
public class Utility {

    private static final String DATE_FORMAT = "MM/dd/yyyy HH:mm:ss";

    public static String convertDateToString(Date date) {

        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        return df.format(date);
    }

}
