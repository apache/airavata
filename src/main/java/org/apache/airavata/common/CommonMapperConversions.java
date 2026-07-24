package org.apache.airavata.common;

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
        if (csv == null || csv.isEmpty())
            return null;
        return Arrays.asList(csv.split(","));
    }

    default String listToCsv(List<String> list) {
        if (list == null || list.isEmpty())
            return null;
        return String.join(",", list);
    }
}
