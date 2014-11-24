package org.apache.airavata.gfac.monitor;/*
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

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.SecurityContext;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gsi.ssh.api.ServerInfo;
import org.apache.airavata.gsi.ssh.api.authentication.AuthenticationInfo;
import org.apache.airavata.gsi.ssh.impl.authentication.MyProxyAuthenticationInfo;
import org.apache.airavata.model.appcatalog.computeresource.ComputeResourceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;

public class HPCMonitorID extends MonitorID {
    private final static Logger logger = LoggerFactory.getLogger(HPCMonitorID.class);


    private AuthenticationInfo authenticationInfo = null;

    public HPCMonitorID(ComputeResourceDescription computeResourceDescription, String jobID, String taskID, String workflowNodeID,
                        String experimentID, String userName,String jobName) {
        super(computeResourceDescription, jobID, taskID, workflowNodeID, experimentID, userName,jobName);
        setComputeResourceDescription(computeResourceDescription);
        setJobStartedTime(new Timestamp((new Date()).getTime()));
        setUserName(userName);
        setJobID(jobID);
        setTaskID(taskID);
        setExperimentID(experimentID);
        setWorkflowNodeID(workflowNodeID);
    }

    public HPCMonitorID(AuthenticationInfo authenticationInfo, JobExecutionContext jobExecutionContext) {
        super(jobExecutionContext);
        this.authenticationInfo = authenticationInfo;
        if (this.authenticationInfo != null) {
            try {
                String hostAddress = jobExecutionContext.getHostName();
                SecurityContext securityContext = jobExecutionContext.getSecurityContext(hostAddress);
                ServerInfo serverInfo = null;
                if (securityContext != null) {
                    if (securityContext instanceof  GSISecurityContext){
                        serverInfo = (((GSISecurityContext) securityContext).getPbsCluster()).getServerInfo();
                        if (serverInfo.getUserName() != null) {
                            setUserName(serverInfo.getUserName());
                        }
                    }
                    if (securityContext instanceof SSHSecurityContext){
                        serverInfo = (((SSHSecurityContext) securityContext).getPbsCluster()).getServerInfo();
                        if (serverInfo.getUserName() != null) {
                            setUserName(serverInfo.getUserName());
                        }
                    }
                }
            } catch (GFacException e) {
                logger.error("Error while getting security context", e);
            }
        }
    }

    public HPCMonitorID(ComputeResourceDescription computeResourceDescription, String jobID, String taskID, String workflowNodeID, String experimentID, String userName, AuthenticationInfo authenticationInfo) {
        setComputeResourceDescription(computeResourceDescription);
        setJobStartedTime(new Timestamp((new Date()).getTime()));
        this.authenticationInfo = authenticationInfo;
        // if we give myproxyauthenticationInfo, so we try to use myproxy user as the user
        if (this.authenticationInfo != null) {
            if (this.authenticationInfo instanceof MyProxyAuthenticationInfo) {
                setUserName(((MyProxyAuthenticationInfo) this.authenticationInfo).getUserName());
            }
        }
        setJobID(jobID);
        setTaskID(taskID);
        setExperimentID(experimentID);
        setWorkflowNodeID(workflowNodeID);
    }

    public AuthenticationInfo getAuthenticationInfo() {
        return authenticationInfo;
    }

    public void setAuthenticationInfo(AuthenticationInfo authenticationInfo) {
        this.authenticationInfo = authenticationInfo;
    }
}
