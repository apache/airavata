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

package org.apache.airavata.core.gfac.utils;

import java.util.Enumeration;
import java.util.Properties;

import org.slf4j.Logger;

public class LogUtils {

    private LogUtils() {
    }

    /**
     * Print out properties' items into a log with the format: key = value. Use debug level if it is enabled otherwise
     * use info level.
     * 
     * @param log
     * @param prop
     */
    public static void displayProperties(Logger log, Properties prop) {
        Enumeration em = prop.keys();
        while (em.hasMoreElements()) {
            String key = em.nextElement().toString();
            String value = prop.getProperty(key);
            String msg = key + " = " + value;
            if (log.isDebugEnabled()) {
                log.debug(msg);
            } else {
                log.info(msg);
            }
        }
    }
}
