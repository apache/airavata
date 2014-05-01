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
package org.apache.airavata.gfac.monitor.state;

import org.apache.airavata.gsi.ssh.impl.JobStatus;

/**
 * Based on the job status monitoring we can gather
 * different informaation about the job, its not simply
 * the job status, so we need a way to implement
 * different job statusinfo object to keep job status
 */
public interface JobStatusInfo {

    /**
     * This method can be used to get JobStatusInfo data and
     * decide the finalJobState
     *
     * @param jobState
     */
    void setJobStatus(JobStatus jobState);

    /**
     * After setting the jobState by processing jobinformation
     * this method can be used to get the JobStatus
     * @return
     */
    JobStatus getJobStatus();

}
