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

package org.apache.airavata.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class StringUtil {

    /**
     * Converts object to String without worrying about null check.
     * 
     * @param object
     * @return The object.toString if object is not null; "" otherwise.
     */
    public static String toString(Object object) {
        if (object == null) {
            return "";
        } else {
            return object.toString();
        }
    }

    /**
     * Trims a specified string, and makes it null if the result is empty string.
     * 
     * @param string
     * @return the string processed
     */
    public static String trimAndNullify(String string) {
        if (string != null) {
            string = string.trim();
            if (string.equals("")) {
                string = null;
            }
        }
        return string;
    }

    /**
     * @param oldName
     * @return Trimmed String
     */
    public static String trimSpaceInString(String oldName) {
        if (oldName == null) {
            return "";
        }
        return oldName.replace(" ", "");
    }

    /**
     * Converts a specified string to a Java identifier.
     * 
     * @param name
     * @return the Java identifier
     */
    public static String convertToJavaIdentifier(String name) {

        final char REPLACE_CHAR = '_';

        if (name == null || name.length() == 0) {
            return "" + REPLACE_CHAR;
        }

        StringBuilder buf = new StringBuilder();

        char c = name.charAt(0);
        if (!Character.isJavaIdentifierStart(c)) {
            // Add _ at the beggining instead of replacing it to _. This is
            // more readable if the name is like 3D_Model.
            buf.append(REPLACE_CHAR);
        }

        for (int i = 0; i < name.length(); i++) {
            c = name.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                buf.append(c);
            } else {
                buf.append(REPLACE_CHAR);
            }
        }

        return buf.toString();
    }

    /**
     * Creates a new name by incrementing the number after the underscore at the end of the old name. If there is no
     * underscore and number at the end, put "_2" at the end.
     * 
     * @param oldName
     * @return the new name
     */
    public static String incrementName(String oldName) {

        final char PREFIX = '_';

        String newName;
        if (oldName == null || oldName.length() == 0) {
            newName = "noName";
        } else {
            int lastDashIndex = oldName.lastIndexOf(PREFIX);
            if (lastDashIndex < 0) {
                newName = oldName + PREFIX + 2;
            } else {
                String suffix = oldName.substring(lastDashIndex + 1);
                try {
                    int number = Integer.parseInt(suffix);
                    int newNumber = number + 1;
                    newName = oldName.substring(0, lastDashIndex + 1) + newNumber;
                } catch (RuntimeException e) {
                    // It was not a number
                    newName = oldName + PREFIX + 2;
                }
            }
        }
        return newName;
    }

    /**
     * Returns the local class name of a specified class.
     * 
     * @param klass
     *            The specified class
     * @return The local class name
     */
    public static String getClassName(Class klass) {
        String fullName = klass.getName();
        int index = fullName.lastIndexOf(".");
        if (index < 0) {
            return fullName;
        } else {
            return fullName.substring(index + 1);
        }
    }

    /**
     * @param throwable
     * @return The stackTrace in String
     */
    public static String getStackTraceInString(Throwable throwable) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        throwable.printStackTrace(printStream);
        printStream.flush();
        return byteArrayOutputStream.toString();
    }

}