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

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.gsissh.security.GSISecurityContext;
import org.apache.airavata.gfac.monitor.HostMonitorData;
import org.apache.airavata.gfac.monitor.MonitorID;
import org.apache.airavata.gsi.ssh.api.SSHApiException;
import org.apache.airavata.gsi.ssh.impl.JobStatus;
import org.apache.airavata.gsi.ssh.impl.PBSCluster;
import org.apache.airavata.model.workspace.experiment.JobState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public class ResourceConnection {
    private static final Logger log = LoggerFactory.getLogger(ResourceConnection.class);

    private PBSCluster cluster;


    public ResourceConnection(HostMonitorData hostMonitorData) throws SSHApiException {
        MonitorID monitorID = hostMonitorData.getMonitorIDs().get(0);
        try {
            GSISecurityContext securityContext = (GSISecurityContext)monitorID.getJobExecutionContext().getSecurityContext(GSISecurityContext.GSI_SECURITY_CONTEXT);
            cluster = (PBSCluster)securityContext.getPbsCluster();
        } catch (GFacException e) {
            log.error("Error reading data from job ExecutionContext");
        }
    }
    public JobState getJobStatus(MonitorID monitorID) throws SSHApiException {
        String jobID = monitorID.getJobID();
        //todo so currently we execute the qstat for each job but we can use user based monitoring
        //todo or we should concatenate all the commands and execute them in one go and parse the response
        return getStatusFromString(cluster.getJobStatus(jobID).toString());
    }

    public Map<String,JobState> getJobStatuses(List<MonitorID> monitorIDs) throws SSHApiException {
        Map<String,JobStatus> treeMap = new TreeMap<String,JobStatus>();
        Map<String,JobState> treeMap1 = new TreeMap<String,JobState>();
        // creating a sorted map with all the jobIds and with the predefined
        // status as UNKNOWN
        for (MonitorID monitorID : monitorIDs) {
            treeMap.put(monitorID.getJobID(), JobStatus.U);
        }
        String userName = cluster.getServerInfo().getUserName();
        //todo so currently we execute the qstat for each job but we can use user based monitoring
        //todo or we should concatenate all the commands and execute them in one go and parse the response
        cluster.getJobStatuses(userName,treeMap);
        for(String key:treeMap.keySet()){
            treeMap1.put(key,getStatusFromString(treeMap.get(key).toString()));
        }
        return treeMap1;
    }
    private JobState getStatusFromString(String status) {
        log.info("parsing the job status returned : " + status);
        if(status != null){
            if("C".equals(status) || "CD".equals(status)|| "E".equals(status) || "CG".equals(status)){
                return JobState.COMPLETE;
            }else if("H".equals(status) || "h".equals(status)){
                return JobState.HELD;
            }else if("Q".equals(status) || "qw".equals(status)){
                return JobState.QUEUED;
            }else if("R".equals(status)  || "CF".equals(status) || "r".equals(status)){
                return JobState.ACTIVE;
            }else if ("T".equals(status)) {
                return JobState.HELD;
            } else if ("W".equals(status) || "PD".equals(status)) {
                return JobState.QUEUED;
            } else if ("S".equals(status)) {
                return JobState.SUSPENDED;
            }else if("CA".equals(status)){
                return JobState.CANCELED;
            }else if ("F".equals(status) || "NF".equals(status) || "TO".equals(status)) {
                return JobState.FAILED;
            }else if ("PR".equals(status) || "Er".equals(status)) {
                return JobState.FAILED;
            }else if ("U".equals(status)){
                return JobState.UNKNOWN;
            }
        }
        return JobState.UNKNOWN;
    }

    public PBSCluster getCluster() {
        return cluster;
    }

    public void setCluster(PBSCluster cluster) {
        this.cluster = cluster;
    }
}
