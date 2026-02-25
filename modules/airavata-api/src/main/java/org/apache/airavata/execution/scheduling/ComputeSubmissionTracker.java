/**
*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements. See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership. The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License. You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.apache.airavata.execution.scheduling;

import org.apache.airavata.core.util.IdGenerator;
import org.apache.airavata.execution.entity.ComputeSubmissionTrackingEntity;
import org.apache.airavata.execution.repository.ComputeSubmissionTrackingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tracks job submissions per compute resource to enable smart cluster monitoring.
 * Cluster status checks are skipped if jobs were submitted recently to avoid unnecessary overhead.
 * Backed by COMPUTE_SUBMISSION_TRACKING DB table.
 */
@Component
public class ComputeSubmissionTracker {

    private final ComputeSubmissionTrackingRepository repository;

    public ComputeSubmissionTracker(ComputeSubmissionTrackingRepository repository) {
        this.repository = repository;
    }

    /**
     * Record a job submission for a compute resource.
     *
     * @param computeResourceId the compute resource ID
     */
    @Transactional
    public void recordSubmission(String computeResourceId) {
        if (computeResourceId != null && !computeResourceId.isEmpty()) {
            var entity = new ComputeSubmissionTrackingEntity();
            entity.setComputeResourceId(computeResourceId);
            entity.setLastSubmissionTime(IdGenerator.getUniqueTimestamp().getTime());
            repository.save(entity);
        }
    }

    /**
     * Get the last submission time for a compute resource.
     *
     * @param computeResourceId the compute resource ID
     * @return the timestamp of the last submission, or null if no submissions recorded
     */
    public Long getLastSubmissionTime(String computeResourceId) {
        return repository
                .findById(computeResourceId)
                .map(ComputeSubmissionTrackingEntity::getLastSubmissionTime)
                .orElse(null);
    }

    /**
     * Check if there were recent submissions to a compute resource within the specified time window.
     *
     * @param computeResourceId the compute resource ID
     * @param timeWindowSeconds the time window in seconds
     * @return true if jobs were submitted within the time window, false otherwise
     */
    public boolean hasRecentSubmissions(String computeResourceId, long timeWindowSeconds) {
        var lastSubmissionTime = getLastSubmissionTime(computeResourceId);
        if (lastSubmissionTime == null) {
            return false;
        }
        long timeWindowMillis = timeWindowSeconds * 1000;
        long currentTime = IdGenerator.getUniqueTimestamp().getTime();
        return (currentTime - lastSubmissionTime) < timeWindowMillis;
    }
}
