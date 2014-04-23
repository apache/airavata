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

import org.apache.airavata.gfac.monitor.exception.AiravataMonitorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the datastructure to keep the user centric job data, rather keeping
 * the individual jobs we keep the jobs based on the each user
 */
public class UserMonitorData {
    private final static Logger logger = LoggerFactory.getLogger(UserMonitorData.class);

    private String  userName;

    private List<HostMonitorData> hostMonitorData;


    public UserMonitorData(String userName) {
        this.userName = userName;
        hostMonitorData = new ArrayList<HostMonitorData>();
    }

    public UserMonitorData(String userName, List<HostMonitorData> hostMonitorDataList) {
        this.hostMonitorData = hostMonitorDataList;
        this.userName = userName;
    }

    public List<HostMonitorData> getHostMonitorData() {
        return hostMonitorData;
    }

    public void setHostMonitorData(List<HostMonitorData> hostMonitorData) {
        this.hostMonitorData = hostMonitorData;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /*
    This method will add element to the MonitorID list, user should not
    duplicate it, we do not check it because its going to be used by airavata
    so we have to use carefully and this method will add a host if its a new host
     */
    public void addHostMonitorData(HostMonitorData hostMonitorData) throws AiravataMonitorException {
        this.hostMonitorData.add(hostMonitorData);
    }
}
