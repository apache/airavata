/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.util;

import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

public class AiravataUtils {

    // Unambiguous Crockford-style base32 alphabet (no 0/1/I/L/O) for short, human-readable,
    // URL-safe ids.
    private static final char[] READABLE_ID_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final int READABLE_ID_LENGTH = 8;
    private static final SecureRandom READABLE_ID_RANDOM = new SecureRandom();

    public static Timestamp getCurrentTimestamp() {
        Calendar calender = Calendar.getInstance();
        //        java.util.Date d = calender.getTimeInMillis();
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
     * Generate a short, human-readable, URL-safe id of the form {@code <prefix>-XXXXXXXX} using an
     * unambiguous alphabet (no 0/1/I/L/O). The id carries no semantic meaning; callers that use it as
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
