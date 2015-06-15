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
package org.apache.airavata.gfac.monitor.impl.pull.qstat;

import org.apache.airavata.gfac.core.GFacException;
import org.apache.airavata.gfac.core.SecurityContext;
import org.apache.airavata.gfac.core.authentication.AuthenticationInfo;
import org.apache.airavata.gfac.core.monitor.MonitorID;
import org.apache.airavata.gfac.gsi.ssh.impl.HPCRemoteCluster;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.monitor.HostMonitorData;
import org.apache.airavata.gfac.ssh.security.SSHSecurityContext;
import org.apache.airavata.gfac.core.SSHApiException;
import org.apache.airavata.gfac.core.cluster.JobStatus;
import org.apache.airavata.model.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class ResourceConnection {
    private static final Logger log = LoggerFactory.getLogger(ResourceConnection.class);

    private HPCRemoteCluster cluster;

    private AuthenticationInfo authenticationInfo;


    public ResourceConnection(HostMonitorData hostMonitorData,AuthenticationInfo authInfo) throws SSHApiException {
        MonitorID monitorID = hostMonitorData.getMonitorIDs().get(0);
        try {
            SecurityContext securityContext = monitorID.getJobExecutionContext().getSecurityContext(monitorID.getComputeResourceDescription().getHostName());
            if(securityContext != null) {
                if (securityContext instanceof GSISecurityContext) {
                    GSISecurityContext gsiSecurityContext = (GSISecurityContext) securityContext;
                    cluster = (HPCRemoteCluster) gsiSecurityContext.getRemoteCluster();
                } else if (securityContext instanceof  SSHSecurityContext) {
                    SSHSecurityContext sshSecurityContext = (SSHSecurityContext)
                            securityContext;
                    cluster = (HPCRemoteCluster) sshSecurityContext.getRemoteCluster();
                }
            }
            // we just use cluster configuration from the incoming request and construct a new cluster because for monitoring
            // we are using our own credentials and not using one users account to do everything.
            authenticationInfo = authInfo;
        } catch (GFacException e) {
            log.error("Error reading data from job ExecutionContext");
        }
    }

    public ResourceConnection(HostMonitorData hostMonitorData) throws SSHApiException {
        MonitorID monitorID = hostMonitorData.getMonitorIDs().get(0);
        try {
            GSISecurityContext securityContext = (GSISecurityContext)
                    monitorID.getJobExecutionContext().getSecurityContext(monitorID.getComputeResourceDescription().getHostName());
            cluster = (HPCRemoteCluster) securityContext.getRemoteCluster();

            // we just use cluster configuration from the incoming request and construct a new cluster because for monitoring
            // we are using our own credentials and not using one users account to do everything.
            cluster = new HPCRemoteCluster(cluster.getServerInfo(), authenticationInfo, cluster.getJobManagerConfiguration());
        } catch (GFacException e) {
            log.error("Error reading data from job ExecutionContext");
        }
    }

    public JobState getJobStatus(MonitorID monitorID) throws SSHApiException {
        String jobID = monitorID.getJobID();
        //todo so currently we execute the qstat for each job but we can use user based monitoring
        //todo or we should concatenate all the commands and execute them in one go and parseSingleJob the response
        return getStatusFromString(cluster.getJobStatus(jobID).toString());
    }

    public Map<String, JobState> getJobStatuses(List<MonitorID> monitorIDs) throws SSHApiException {
        Map<String, JobStatus> treeMap = new TreeMap<String, JobStatus>();
        Map<String, JobState> treeMap1 = new TreeMap<String, JobState>();
        // creating a sorted map with all the jobIds and with the predefined
        // status as UNKNOWN
        for (MonitorID monitorID : monitorIDs) {
            treeMap.put(monitorID.getJobID()+","+monitorID.getJobName(), JobStatus.U);
        }
        String userName = cluster.getServerInfo().getUserName();
        //todo so currently we execute the qstat for each job but we can use user based monitoring
        //todo or we should concatenate all the commands and execute them in one go and parseSingleJob the response
        //
        cluster.getJobStatuses(userName, treeMap);
        for (String key : treeMap.keySet()) {
            treeMap1.put(key, getStatusFromString(treeMap.get(key).toString()));
        }
        return treeMap1;
    }

    private JobState getStatusFromString(String status) {
        log.info("parsing the job status returned : " + status);
        if (status != null) {
            if ("C".equals(status) || "CD".equals(status) || "E".equals(status) || "CG".equals(status) || "DONE".equals(status)) {
                return JobState.COMPLETE;
            } else if ("H".equals(status) || "h".equals(status)) {
                return JobState.HELD;
            } else if ("Q".equals(status) || "qw".equals(status) || "PEND".equals(status)) {
                return JobState.QUEUED;
            } else if ("R".equals(status) || "CF".equals(status) || "r".equals(status) || "RUN".equals(status)) {
                return JobState.ACTIVE;
            } else if ("T".equals(status)) {
                return JobState.HELD;
            } else if ("W".equals(status) || "PD".equals(status)) {
                return JobState.QUEUED;
            } else if ("S".equals(status) || "PSUSP".equals(status) || "USUSP".equals(status) || "SSUSP".equals(status)) {
                return JobState.SUSPENDED;
            } else if ("CA".equals(status)) {
                return JobState.CANCELED;
            } else if ("F".equals(status) || "NF".equals(status) || "TO".equals(status) || "EXIT".equals(status)) {
                return JobState.FAILED;
            } else if ("PR".equals(status) || "Er".equals(status)) {
                return JobState.FAILED;
            } else if ("U".equals(status) || ("UNKWN".equals(status))) {
                return JobState.UNKNOWN;
            }
        }
        return JobState.UNKNOWN;
    }

    public HPCRemoteCluster getCluster() {
        return cluster;
    }

    public void setCluster(HPCRemoteCluster cluster) {
        this.cluster = cluster;
    }

    public boolean isConnected(){
        return this.cluster.getSession().isConnected();
    }
}
