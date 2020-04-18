/**
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
 */
package org.apache.airavata.common.utils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.UUID;

public class AiravataUtils {

    public static Timestamp getCurrentTimestamp() {
        Calendar calender = Calendar.getInstance();
//        java.util.Date d = calender.getTimeInMillis();
        return new Timestamp(calender.getTimeInMillis());
    }

    public static Timestamp getTime(long time) {
        if (time == 0 || time < 0){
            return getCurrentTimestamp();
        }
        return new Timestamp(time);
    }

    public static String getId (String name){
        String id = name.trim().replaceAll("\\s|\\.|/|\\\\", "_");
        return id + "_" + UUID.randomUUID();
    }
}
