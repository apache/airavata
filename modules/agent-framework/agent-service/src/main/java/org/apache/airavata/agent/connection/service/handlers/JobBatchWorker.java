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
package org.apache.airavata.agent.connection.service.handlers;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.airavata.agent.connection.service.db.entity.JobBatchEntity;
import org.apache.airavata.agent.connection.service.db.entity.JobUnitEntity;
import org.apache.airavata.agent.connection.service.db.repo.JobUnitRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service("jobBatchWorker")
public class JobBatchWorker {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobBatchWorker.class);

    private final JobUnitRepo jobUnitRepo;

    public JobBatchWorker(JobUnitRepo jobUnitRepo) {
        this.jobUnitRepo = jobUnitRepo;
    }

    /**
     * Expands the parameter grid and persists JOB_UNIT rows in batch
     */
    @Async("batchExecutor")
    @Transactional(dontRollbackOn = Exception.class)
    public void expandAndPersistUnitsAsync(
            String experimentId, String batchId, String commandTemplate, Map<String, List<String>> grid) {

        if (grid == null || grid.isEmpty()) {
            return;
        }

        List<String> keys = new ArrayList<>(grid.keySet());
        Collections.sort(keys);

        List<String[]> values = new ArrayList<>(keys.size());
        for (String k : keys) {
            List<String> vs = grid.get(k);
            if (vs == null || vs.isEmpty()) {
                LOGGER.warn("Parameter '{}' has empty value list; skipping batch {}", k, batchId);
                return;
            }
            values.add(vs.toArray(new String[0]));
        }

        final int chunkSize = 100;
        List<JobUnitEntity> buffer = new ArrayList<>(chunkSize);

        int dims = values.size();
        int[] idx = new int[dims];
        boolean done = false;

        while (!done) {
            String resolved = renderCommand(commandTemplate, keys, values, idx);

            JobUnitEntity jobUnit = new JobUnitEntity();
            jobUnit.setId(UUID.randomUUID().toString());
            jobUnit.setExperimentId(experimentId);
            JobBatchEntity batchRef = new JobBatchEntity();
            batchRef.setId(batchId);
            jobUnit.setBatch(batchRef);
            jobUnit.setResolvedCommand(resolved);
            buffer.add(jobUnit);

            if (buffer.size() >= chunkSize) {
                jobUnitRepo.saveAll(buffer);
                buffer.clear();
            }

            for (int d = dims - 1; d >= 0; d--) {
                idx[d]++;
                if (idx[d] < values.get(d).length) {
                    break;
                } else {
                    idx[d] = 0;
                    if (d == 0) {
                        done = true;
                    }
                }
            }
        }

        if (!buffer.isEmpty()) {
            jobUnitRepo.saveAll(buffer);
        }

        LOGGER.info("Batch {} expansion complete (experiment {}).", batchId, experimentId);
    }

    private static String renderCommand(String template, List<String> keys, List<String[]> values, int[] idx) {
        String cmd = template;
        for (int i = 0; i < keys.size(); i++) {
            String placeholder = "{" + keys.get(i) + "}";
            cmd = cmd.replace(placeholder, values.get(i)[idx[i]]);
        }
        return cmd;
    }
}
