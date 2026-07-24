package org.apache.airavata.common;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

public class AiravataUtils {

    // Unambiguous Crockford-style base32 alphabet (no 0/1/I/L/O) for short,
    // human-readable,
    // URL-safe ids.
    private static final char[] READABLE_ID_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int READABLE_ID_LENGTH = 8;
    private static final SecureRandom READABLE_ID_RANDOM = new SecureRandom();

    public static Timestamp getCurrentTimestamp() {
        Calendar calender = Calendar.getInstance();
        // java.util.Date d = calender.getTimeInMillis();
        return new Timestamp(calender.getTimeInMillis());
    }

    public static Timestamp getTime(long time) {
        if (time == 0 || time < 0) {
            return getCurrentTimestamp();
        }
        return new Timestamp(time);
    }

    public static String getId(String name) {
        String id = name.trim().replaceAll("\\s|\\.|/|\\\\", "_");
        return id + "_" + UUID.randomUUID();
    }

    /**
     * Generate a short, human-readable, URL-safe id of the form
     * {@code <prefix>-XXXXXXXX} using an
     * unambiguous alphabet (no 0/1/I/L/O). The id carries no semantic meaning;
     * callers that use it as
     * a primary key should regenerate it on the (vanishingly rare) collision.
     */
    public static String getReadableId(String prefix) {
        StringBuilder sb = new StringBuilder(prefix).append('-');
        for (int i = 0; i < READABLE_ID_LENGTH; i++) {
            sb.append(READABLE_ID_ALPHABET[READABLE_ID_RANDOM.nextInt(READABLE_ID_ALPHABET.length)]);
        }
        return sb.toString();
    }
}
