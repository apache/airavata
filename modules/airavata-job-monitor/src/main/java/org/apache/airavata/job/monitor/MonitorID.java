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
package org.apache.airavata.job.monitor;

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;

/*
This is the object which contains the data to identify a particular
Job to start the monitoring
*/
public class MonitorID {
    private final static Logger logger = LoggerFactory.getLogger(MonitorID.class);

    private String userName;

    private String jobID;

    private Timestamp jobStartedTime;

    private Timestamp lastMonitored;

    private HostDescription host;

    private int port = 22;

    private AuthenticationInfo authenticationInfo = null;

    public MonitorID(HostDescription host, String jobID, String userName) {
        this.host = host;
        this.jobID = jobID;
        this.jobStartedTime = new Timestamp((new Date()).getTime());
        this.userName = userName;
    }

    public MonitorID(HostDescription host, String jobID, String userName,AuthenticationInfo authenticationInfo) {
        this.host = host;
        this.jobID = jobID;
        this.jobStartedTime = new Timestamp((new Date()).getTime());
        this.authenticationInfo = authenticationInfo;
        this.userName = userName;
    }
    public HostDescription getHost() {
        return host;
    }

    public void setHost(HostDescription host) {
        this.host = host;
    }

    public Timestamp getLastMonitored() {
        return lastMonitored;
    }

    public void setLastMonitored(Timestamp lastMonitored) {
        this.lastMonitored = lastMonitored;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public Timestamp getJobStartedTime() {
        return jobStartedTime;
    }

    public void setJobStartedTime(Timestamp jobStartedTime) {
        this.jobStartedTime = jobStartedTime;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }
}
