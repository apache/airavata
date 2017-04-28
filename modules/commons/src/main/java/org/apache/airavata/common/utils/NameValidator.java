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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameValidator {

    /**
     * @param name
     * @return Is it valid name?
     */
    public static boolean validate(String name) {
        // Set the name pattern string
        Pattern p = Pattern.compile("([a-zA-Z]){1,}([0-9]|_|\\.|[a-zA-Z]){0,}$");

        // Match the given string with the pattern
        Matcher m = p.matcher(name);

        // Check whether match is found
        boolean matchFound = m.matches();

        return matchFound;
    }

    /**
     * @param args
     * @Description some quick tests
     */
    public static void main(String[] args) {
        System.out.println(validate("abc90_90abc")); // true

        System.out.println(validate("abc_abc_123")); // true

        System.out.println(validate("abc_abc_")); // true

        System.out.println(validate("abc_abc")); // true

        System.out.println(validate("abc.abc")); // true

        System.out.println(validate("9abc_abc")); // false, name cannot start with number

        System.out.println(validate("_abc_abc")); // false, name cannot start with "_"

        System.out.println(validate("\\abc_abc")); // false, name cannot start with "\"

        System.out.println(validate("abc\\_abc")); // false, name cannot contain "\"
    }

}