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

package org.apache.airavata.workflow.tracking.common;

/**
 * One place to put and check required version number.
 */
public class NotifierVersion {

    private final static String TYPES_VERSION = "2.6";
    private final static String IMPL_VERSION = "2.8.0";

    public static String getTypesVersion() {
        return TYPES_VERSION;
    }

    public static String getImplVersion() {
        return IMPL_VERSION;
    }

    /**
     * Print version when exxecuted from command line.
     */
    public static void main(String[] args) {
        String IMPL_OPT = "-impl";
        String TYPE_OPT = "-types";
        if (IMPL_OPT.equals(args[0])) {
            System.out.println(IMPL_VERSION);
        } else if (TYPE_OPT.equals(args[0])) {
            System.out.println(TYPES_VERSION);
        } else {
            System.out.println(NotifierVersion.class.getName() + " Error: " + TYPE_OPT + " or " + IMPL_OPT
                    + " is required");
            System.exit(-1);
        }
    }
}
