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
package org.apache.airavata.gfac.monitor;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;

import java.util.ArrayList;
import java.util.List;

public class HostMonitorData {
    private HostDescription host;

    private List<MonitorID> monitorIDs;

    public HostMonitorData(HostDescription host) {
        this.host = host;
        monitorIDs = new ArrayList<MonitorID>();
    }

    public HostMonitorData(HostDescription host, List<MonitorID> monitorIDs) {
        this.host = host;
        this.monitorIDs = monitorIDs;
    }

    public HostDescription getHost() {
        return host;
    }

    public void setHost(HostDescription host) {
        this.host = host;
    }

    public List<MonitorID> getMonitorIDs() {
        return monitorIDs;
    }

    public void setMonitorIDs(List<MonitorID> monitorIDs) {
        this.monitorIDs = monitorIDs;
    }

    /**
     * this method get called by CommonUtils and it will check the right place before adding
     * so there will not be a mismatch between this.host and monitorID.host
     * @param monitorID
     * @throws org.apache.airavata.gfac.monitor.exception.AiravataMonitorException
     */
    public void addMonitorIDForHost(MonitorID monitorID)throws AiravataMonitorException {
        monitorIDs.add(monitorID);
    }
}
