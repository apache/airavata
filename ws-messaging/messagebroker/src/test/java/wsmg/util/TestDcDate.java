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

package wsmg.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import junit.framework.TestCase;

import org.apache.airavata.wsmg.util.DcDate;
import org.junit.Test;

public class TestDcDate extends TestCase {

    @Test
    public void testDcDate() {
        DcDate d;

        // d = new DcDate("1997-07-16T19:20:30.45+01:00");

        d = new DcDate(TimeZone.getDefault());

        String dc = d.getDcDate();

        // System.out.println(getClass()+" dc="+dc);

        new DcDate(dc);
        Calendar loc = new GregorianCalendar();
        assertEquals(d.getYear(), loc.get(Calendar.YEAR));
        assertEquals(d.getMonth(), loc.get(Calendar.MONTH) + 1);
        assertEquals(d.getDay(), loc.get(Calendar.DAY_OF_MONTH));
        assertEquals(d.getHour(), loc.get(Calendar.HOUR_OF_DAY));
        assertEquals(d.getMinute(), loc.get(Calendar.MINUTE));

        int tzOffset = TimeZone.getDefault().getRawOffset() / (60 * 1000);
        int tzOffsetHours = tzOffset / 60;
        assertEquals(tzOffset, d.getTimeZoneOffset());

        DcDate dZ = new DcDate(TimeZone.getTimeZone("GMT"));

        // System.out.println(getClass()+" dZ="+dZ);

        assertEquals(0, dZ.getTimeZoneOffset());
        // assertEquals(d.getDay() + tzOffset, dZ.getDay()); //may fail!
        int localHour = dZ.getHour() + tzOffsetHours;
        if (localHour > 24) {
            localHour -= 24;
        }
        if (localHour < 0) {
            localHour += 24;
        }
        // assertEquals(d.getHour(), localHour);

        //
        d = new DcDate("1997");
        assertEquals(1997, d.getYear());
        //
        d = new DcDate("1997-07");
        assertEquals(1997, d.getYear());
        assertEquals(7, d.getMonth());
        //
        d = new DcDate("1997-07-16");
        assertEquals(1997, d.getYear());
        assertEquals(7, d.getMonth());
        assertEquals(16, d.getDay());

        // 1997-07-16T19:20+01:00
        d = new DcDate("1997-07-16T19:20+01:00");
        assertEquals(1997, d.getYear());
        assertEquals(7, d.getMonth());
        assertEquals(16, d.getDay());
        assertEquals(19, d.getHour());
        assertEquals(20, d.getMinute());
        assertEquals(1 * 60, d.getTimeZoneOffset());
        // 1997-07-16T19:20:30+01:00
        d = new DcDate("1997-07-16T19:20:30+01:00");
        // 1997-07-16T19:20:30.45+01:00
        d = new DcDate("1997-07-16T19:20:30.45+01:00");
        assertEquals("45", d.getDecimalFraction());
        // Ex: 2004-04-13T11:53:15.4362784-04:00
        d = new DcDate("2004-04-13T11:53:15.4362784-04:00");
        assertEquals(13, d.getDay());
        assertEquals("4362784", d.getDecimalFraction());

    }

}
