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
package org.apache.airavata.job.monitor.state.impl;

import org.apache.airavata.gsi.ssh.impl.JobStatus;
import org.apache.airavata.job.monitor.state.JobStatusInfo;


/**
 * This can be used to keep information about a Grid job
 * which we can get from qstat polling or from amqp based
 * monitoring in Grid machines
 */
public class GridJobStatusInfo implements JobStatusInfo {
    public void setJobStatus(JobStatus jobState) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public JobStatus getJobStatus() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
