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
package org.apache.airavata.mapper;

import com.google.protobuf.ByteString;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Shared MapStruct type converters. Mappers that need these conversions
 * should extend this interface.
 */
public interface CommonMapperConversions {

    default Timestamp longToTimestamp(long millis) {
        return millis == 0 ? null : new Timestamp(millis);
    }

    default long timestampToLong(Timestamp ts) {
        return ts == null ? 0 : ts.getTime();
    }

    default long dateToLong(Date date) {
        return date == null ? 0 : date.getTime();
    }

    default Date longToDate(long millis) {
        return millis == 0 ? null : new Date(millis);
    }

    default int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    default boolean intToBoolean(int value) {
        return value != 0;
    }

    default boolean shortToBoolean(short value) {
        return value != 0;
    }

    default short booleanToShort(boolean value) {
        return (short) (value ? 1 : 0);
    }

    default byte[] byteStringToBytes(ByteString value) {
        return value != null ? value.toByteArray() : null;
    }

    default ByteString bytesToByteString(byte[] value) {
        return value != null ? ByteString.copyFrom(value) : ByteString.EMPTY;
    }

    default List<String> csvToList(String csv) {
        if (csv == null || csv.isEmpty()) return null;
        return Arrays.asList(csv.split(","));
    }

    default String listToCsv(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }
}
