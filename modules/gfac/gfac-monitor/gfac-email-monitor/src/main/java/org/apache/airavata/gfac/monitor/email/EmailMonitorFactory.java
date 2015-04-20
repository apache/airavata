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
package org.apache.airavata.gfac.monitor.email;

import org.apache.airavata.model.appcatalog.computeresource.EmailMonitorProperty;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;

import java.util.HashMap;
import java.util.Map;

public class EmailMonitorFactory {

    private static Map<String, EmailBasedMonitor> emailMonitors = new HashMap<String, EmailBasedMonitor>();


    public static EmailBasedMonitor getEmailBasedMonitor(EmailMonitorProperty emailMonitorProp,
                                                         ResourceJobManagerType resourceJobManagerType) {
        String key = getKey(emailMonitorProp);
        EmailBasedMonitor monitor = emailMonitors.get(key);
        if (monitor == null) {
            synchronized (emailMonitors){
                if (monitor == null) {
                    monitor = new EmailBasedMonitor(emailMonitorProp, resourceJobManagerType);
                    emailMonitors.put(key, monitor);
                    new Thread(monitor).start();
                }
            }
        }
        return monitor;
    }

    public static void stopAllMonitors() {
        for (EmailBasedMonitor emailBasedMonitor : emailMonitors.values()) {
            emailBasedMonitor.stopMonitoring();
        }
    }

    private static String getKey(EmailMonitorProperty emailMonitorProp) {
        StringBuffer sb = new StringBuffer(emailMonitorProp.getHost().trim());
        sb.append("_").append(emailMonitorProp.getStoreProtocol().name());
        sb.append("_").append(emailMonitorProp.getEmailAddress().trim());
        sb.append("_").append(emailMonitorProp.getFolderName().trim());
        return sb.toString();
    }

}
