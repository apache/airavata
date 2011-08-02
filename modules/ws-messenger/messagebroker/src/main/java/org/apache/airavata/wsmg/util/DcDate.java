/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.apache.airavata.wsmg.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

//TODO: support for fractional seconds

/**
 * Represent dc:date as an immutable value object. http://www.w3.org/TR/NOTE-datetime
 * http://www.sics.se/~preben/DC/date_help.html
 * 
 */
public final class DcDate implements Cloneable {

    public Object clone() throws CloneNotSupportedException {
        // it is easy as object is immutable
        DcDate dd = (DcDate) super.clone();
        return dd;
    }

    private String canonical;

    // private Date date;
    private int year;

    private int month; // 1-12

    private int day; // 1-31

    private int hh = -1; // 00-23

    private int mm = -1; // 00-59

    private int ss = -1; // 00-59

    private String decimalFraction = null; // decimal fraction

    private int zoneOffset; // in minutes +/-(12*60) ???

    private Calendar instant;

    private int millisOffset = -1; // milliseconds

    // public DcDate() throws SException {
    //
    // this(System.currentTimeMillis());
    // }
    //
    // public DcDate(long timeMillis) throws SException {
    // this(timeMillis, null);
    // Calendar local = new GregorianCalendar();
    // local.setTimeInMillis(timeMillis);
    // }

    // public DcDate(String tzName) throws SException {
    // TimeZone tz = tzName != null ? TimeZone.getTimeZone(tzName) :
    // TimeZone.getDefault();
    // TimeZone.getTimeZone("GMT")
    public DcDate(TimeZone tz) throws RuntimeException {

        // based on http://javaalmanac.com/egs/java.util/GetAllZones.html?l=rel

        Calendar cal = new GregorianCalendar(tz);
        init(cal, tz);
    }

    public DcDate(Calendar cal) throws RuntimeException {
        init(cal, cal.getTimeZone());
    }

    private void init(Calendar cal, TimeZone tz) throws RuntimeException {
        // Get the number of hours from GMT
        int rawOffset = tz.getRawOffset();
        zoneOffset = rawOffset / (60 * 1000);

        instant = cal;
        // http://javaalmanac.com/egs/java.util/GetCurDate.html
        year = cal.get(Calendar.YEAR);
        month = cal.get(Calendar.MONTH) + 1; // 0=Jan, 1=Feb, ...
        day = cal.get(Calendar.DAY_OF_MONTH);
        hh = cal.get(Calendar.HOUR_OF_DAY); // 0..23
        mm = cal.get(Calendar.MINUTE); // 0..59
        ss = cal.get(Calendar.SECOND); // 0..59
        // int ms = cal.get(Calendar.MILLISECOND); // 0..999
        // date = cal.getTime();
        canonical = computeCanonical();
    }

    public DcDate(String dcDate) throws RuntimeException {
        // try {
        // synchronized(sdf) { //TODO REVISIT: SimpleFormat is not multi-thread
        // safe!
        // d = sdf.parse(dcDate);
        // }
        // } catch (ParseException e) {
        // throw new SException("could not parse dc:date "+dcDate+getCtx(pp),
        // e);
        // }
        // 2003-05-06T23:07:04Z
        // 2003-08-09T18:36:00-05:00
        // 1234567890123456789012345
        // 2004-02-02T19:09:46-05:00
        // 1997
        // 1997-07
        // 1997-07-16
        // 1997-07-16T19:20+01:00
        // 1997-07-16T19:20:30+01:00
        // 1997-07-16T19:20:30.45+01:00
        assert dcDate != null;
        // assert pp != null;
        canonical = dcDate;
        year = getInt(dcDate, 1, 4); // add min, max check for all getInt
        if (dcDate.length() == 4) {
            return;
        }
        check(dcDate, 5, '-');
        month = getInt(dcDate, 6, 2);
        if (dcDate.length() == 7) {
            return;
        }
        check(dcDate, 8, '-');
        day = getInt(dcDate, 9, 2);
        if (dcDate.length() == 10) {
            return;
        }
        check(dcDate, 11, 'T');
        hh = getInt(dcDate, 12, 2);
        check(dcDate, 14, ':');
        mm = getInt(dcDate, 15, 2);
        if (dcDate.length() == 16) {
            throw new RuntimeException("expected date formatted as YYYY-MM-DDThh:mm[:ss[.mmm]]TZD and not " + dcDate);
        }
        int pos = 17;
        char c17 = dcDate.charAt(pos - 1);
        if (c17 == ':') {
            check(dcDate, 17, ':');
            ss = getInt(dcDate, 18, 2);
            pos = 20;
        }
        char zoneIndicator = dcDate.charAt(pos - 1);
        if (zoneIndicator == '.') { // OK we have yet millliseocnds to parse
            // (and ignore ...)
            // Ex: 2004-04-13T11:53:15.4362784-04:00
            // eat digits
            char d;
            int oldPos = pos;
            do {
                d = dcDate.charAt(pos);
                ++pos;
            } while (d >= '0' && d <= '9');
            if (oldPos + 1 == pos) {
                throw new RuntimeException("expected date formtted as YYYY-MM-DDThh:mm[:ss[.s]]TZD and not " + dcDate);

            }
            zoneIndicator = d;
            int newPos = pos;
            decimalFraction = dcDate.substring(oldPos, newPos - 1);
            if (newPos - oldPos >= 3) {
                newPos = oldPos + 3;
            }
            int len = newPos - (oldPos + 1);
            int ii = getInt(dcDate, oldPos + 1, len);
            if (len == 1) {
                millisOffset = 100 * ii;
            } else if (len == 2) {
                millisOffset = 10 * ii;
            } else if (len == 3) {
                millisOffset = ii;
            }
        }
        if (zoneIndicator == 'Z') {
            // done
        } else if (zoneIndicator == '-' || zoneIndicator == '+') {
            int zoneHH = getInt(dcDate, pos + 1, 2);
            check(dcDate, pos + 3, ':');
            int zoneMM = getInt(dcDate, pos + 4, 2);
            zoneOffset = 60 * zoneHH + zoneMM;
            if (zoneIndicator == '-') {
                zoneOffset *= -1;
            }
        } else {
            throw new RuntimeException("unknown zone indicator " + zoneIndicator + " in " + dcDate);
        }
        // Get the number of hours from GMT

        // TimeZone tz = TimeZone.
        instant = new GregorianCalendar();
        int rawOffset = zoneOffset * 60 * 1000;
        instant.set(Calendar.ZONE_OFFSET, rawOffset);
        instant.set(Calendar.YEAR, year);
        instant.set(Calendar.MONTH, month - 1); // 0=Jan, 1=Feb, ...
        instant.set(Calendar.DAY_OF_MONTH, day);
        instant.set(Calendar.HOUR_OF_DAY, hh); // 0..23
        instant.set(Calendar.MINUTE, mm); // 0..59
        instant.set(Calendar.SECOND, ss); // 0..59
        instant.set(Calendar.MILLISECOND, millisOffset); // /0..999 ?
        instant.getTimeInMillis(); // full time in ms -- test?
    }

    public long getTimeInMillis() {
        return instant.getTimeInMillis();
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getHour() {
        return hh;
    }

    public int getMinute() {
        return mm;
    }

    public int getSecond() {
        return ss;
    }

    public int getTimeZoneOffset() {
        return zoneOffset;
    }

    public String getDcDate() {
        return canonical;
    }

    public String getDecimalFraction() {
        return decimalFraction;
    }

    private String computeCanonical() {
        // 2003-08-09T18:36:00-05:00
        // 1234567890123456789012345
        StringBuffer sb = new StringBuffer();
        fill(sb, year, 4);
        if (month > 0) {
            sb.append('-');
            fill(sb, month, 2);
            if (day > 0) {
                sb.append('-');
                fill(sb, day, 2);
                if (hh > -1) {
                    sb.append('T');
                    fill(sb, hh, 2);
                    sb.append(':');
                    fill(sb, mm, 2);
                    if (ss > -1) {
                        sb.append(':');
                        fill(sb, ss, 2);
                    }
                    if (decimalFraction != null) {
                        sb.append('.');
                        sb.append(decimalFraction);
                    }
                    if (zoneOffset == 0) {
                        sb.append('Z');
                    } else {
                        int off = zoneOffset;
                        if (zoneOffset > 0) {
                            sb.append('+');
                        } else {
                            sb.append('-');
                            off *= -1;
                        }
                        int zoneHH = off / 60;
                        int zoneMM = off % 60;
                        fill(sb, zoneHH, 2);
                        sb.append(':');
                        fill(sb, zoneMM, 2);
                    }
                }
            }
        }
        return sb.toString();
    }

    public String toString() {
        return canonical;
    }

    public final static String LOCATION_PROPERTY = "http://xmlpull.org/v1/doc/properties.html#location";

    public static String printable(char ch) {
        return "'" + escape(ch) + "'";
    }

    public static String printable(String s) {
        return "\"" + escape(s) + "\"";
    }

    public static String escape(char ch) {
        if (ch == '\n') {
            return "\\n";
        } else if (ch == '\r') {
            return "\\r";
        } else if (ch == '\t') {
            return "\\t";
        } else if (ch == '\'') {
            return "\\'";
        }
        if (ch > 127 || ch < 32) {
            return "\\u" + Integer.toHexString((int) ch);
        }
        return "" + ch;
    }

    public static String escape(String s) {
        if (s == null) {
            return null;
        }
        final int sLen = s.length();
        StringBuffer buf = new StringBuffer(sLen + 10);
        for (int i = 0; i < sLen; ++i) {
            buf.append(escape(s.charAt(i)));
        }
        s = buf.toString();
        return s;
    }

    private static final int LOOKUP_10S[] = { 1, 10, 10 * 10, 10 * 100, 100 * 100 };

    public static void fill(StringBuffer sb, int value, int fields) {
        assert fields > 0;
        assert fields <= 4;
        assert value >= 0;
        int mm = LOOKUP_10S[fields];
        assert value < mm;
        // assert mm > 0
        // TODO: optimize it ...
        while (fields-- > 0) {
            mm /= 10;
            if (value >= mm) {
                sb.append((value / mm) % 10);
                // i /= 10;
            } else {
                sb.append('0');
            }
        }

        // String s = sb.toString();
        // assert s.toString().length == fields;
        // return s;
    }

    public static void check(String dcDate, int pos, char ch) {
        if (pos > dcDate.length()) {
            throw new RuntimeException("expected " + printable(ch) + " at position " + pos + " but " + dcDate
                    + " is too short");
        }
        char c = dcDate.charAt(pos - 1);
        if (c != ch) {
            throw new RuntimeException("expected " + printable(ch) + " but got " + printable(c) + " in " + dcDate);
        }
    }

    public static int getInt(String dcDate, int pos, int len) throws RuntimeException {
        assert len > 0;
        int end = pos + len - 1;
        String s = dcDate.substring(pos - 1, end);
        try {
            int i = Integer.parseInt(s);
            return i;
        } catch (NumberFormatException e) {
            throw new RuntimeException("expected number for " + printable(s) + " in " + dcDate, e);

        }
    }

    /**
     * Take string and return string that has no spaces, only alpahnumerics, each word is capitalized (like WikiWords)
     * sometimes called CamelCase?!.
     */
    public static String getWikiTitle(String title) {
        StringBuffer sb = new StringBuffer();
        List<String> words = breakIntoWords(title);
        boolean start = true;
        for (Iterator<String> it = words.iterator(); it.hasNext();) {
            String word = it.next();
            boolean wordStart = true;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    if (wordStart && !start) {
                        sb.append(Character.toUpperCase(c));
                    } else {
                        sb.append(c);
                    }
                    wordStart = false;
                }
            }
            start = false;
        }
        return sb.toString();
    }

    public static List<String> breakIntoWords(String s) {
        List<String> words = new ArrayList<String>(s.length() / 5);
        boolean inWord = true;
        int wordStart = 0;
        for (int pos = 0; pos < s.length(); ++pos) {
            char ch = s.charAt(pos);
            boolean isWordSeparator = Character.isWhitespace(ch);
            if (ch == ',') {
                isWordSeparator = true;
            }
            if (ch == '.') {
                isWordSeparator = true;
            }
            if (isWordSeparator) {
                if (inWord) {
                    words.add(s.substring(wordStart, pos));
                    inWord = false;
                }
            } else {
                if (!inWord) {
                    inWord = true;
                    wordStart = pos;
                }
            }
            assert inWord == !isWordSeparator;
        }
        if (inWord) {
            words.add(s.substring(wordStart));
        }
        return words;
    }

    public static String makeTwoDigit(int oneOrTwoDigits) {
        if (oneOrTwoDigits < 0 || oneOrTwoDigits > 99) {
            throw new IllegalArgumentException();
        }
        if (oneOrTwoDigits < 10) {
            return "0" + oneOrTwoDigits;
        }
        return "" + oneOrTwoDigits;
    }

}
