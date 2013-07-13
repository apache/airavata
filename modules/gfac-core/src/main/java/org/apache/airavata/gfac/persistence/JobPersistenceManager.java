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

package org.apache.airavata.gfac.persistence;


import org.apache.airavata.gfac.GFacException;

import java.util.List;

/**
 * User: AmilaJ (amilaj@apache.org)
 * Date: 6/18/13
 * Time: 2:23 PM
 */

/**
 * Responsible persisting job data. This data is useful during a restart.
 * When restarting Airavata can resume monitoring currently executing jobs.
 */
public interface JobPersistenceManager {

    /**
     * Updates the job state in the persisting storage.
     * @param jobData Job data to update.
     * @throws GFacException If an error occurred while updating job data.
     */
    void updateJobStatus (JobData jobData) throws GFacException;

    /**
     * Get all running jobs.
     * @return Job ids which are not failed nor completed.
     * @throws GFacException If an error occurred while querying job data.
     */
    List<JobData> getRunningJobs() throws GFacException;

    /**
     * Get all failed job ids.
     * @return Failed job ids.
     * @throws GFacException If an error occurred while querying job data.
     */
    List<JobData> getFailedJobs() throws GFacException;

    /**
     * Get all un-submitted job ids.
     * @return Un-submitted job ids.
     * @throws GFacException If an error occurred while querying job data.
     */
    List<JobData> getUnSubmittedJobs() throws GFacException;

    /**
     * Get all successfully completed job ids.
     * @return Successfully completed job ids.
     * @throws GFacException If an error occurred while querying job data.
     */
    List<JobData> getSuccessfullyCompletedJobs() throws GFacException;

}
