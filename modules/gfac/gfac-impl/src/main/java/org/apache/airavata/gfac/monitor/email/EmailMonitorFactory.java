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

import org.apache.airavata.common.exception.AiravataException;
import org.apache.airavata.model.appcatalog.computeresource.ResourceJobManagerType;

import java.util.Calendar;
import java.util.Date;

public class EmailMonitorFactory {

    private static EmailBasedMonitor emailBasedMonitor;
    private static Date startMonitorDate = Calendar.getInstance().getTime();

    public static EmailBasedMonitor getEmailBasedMonitor(ResourceJobManagerType resourceJobManagerType) throws AiravataException {
        if (emailBasedMonitor == null) {
            synchronized (EmailMonitorFactory.class){
                if (emailBasedMonitor == null) {
                    emailBasedMonitor = new EmailBasedMonitor(resourceJobManagerType);
                    emailBasedMonitor.setDate(startMonitorDate);
                    new Thread(emailBasedMonitor).start();
                }
            }
        }
        return emailBasedMonitor;
    }

}
