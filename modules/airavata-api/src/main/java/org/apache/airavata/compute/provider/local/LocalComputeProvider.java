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
package org.apache.airavata.compute.provider.local;

import org.apache.airavata.compute.provider.ComputeProvider;
import org.apache.airavata.compute.provider.slurm.SlurmComputeProvider;
import org.apache.airavata.config.ServiceConditionals.ConditionalOnParticipant;
import org.apache.airavata.execution.dag.DagTaskResult;
import org.apache.airavata.execution.task.TaskContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Compute provider for local (PLAIN) resources.
 *
 * <p>Local compute shares SLURM's provisioning and deprovisioning logic (working
 * directory setup/cleanup) but does not require job submission — the machine is
 * already available for execution.
 */
@Component
@ConditionalOnParticipant
public class LocalComputeProvider implements ComputeProvider {

    private static final Logger logger = LoggerFactory.getLogger(LocalComputeProvider.class);

    private final SlurmComputeProvider slurmProvider;

    public LocalComputeProvider(SlurmComputeProvider slurmProvider) {
        this.slurmProvider = slurmProvider;
    }

    @Override
    public DagTaskResult provision(TaskContext context) {
        return slurmProvider.provision(context);
    }

    @Override
    public DagTaskResult submit(TaskContext context) {
        logger.info("Local provider: no submission needed for process {}", context.getProcessId());
        return new DagTaskResult.Success("No submission needed for local compute");
    }

    @Override
    public DagTaskResult monitor(TaskContext context) {
        logger.info("Local provider: no monitoring needed for process {}", context.getProcessId());
        return new DagTaskResult.Success("No monitoring needed for local compute");
    }

    @Override
    public DagTaskResult cancel(TaskContext context) {
        return slurmProvider.cancel(context);
    }

    @Override
    public DagTaskResult deprovision(TaskContext context) {
        return slurmProvider.deprovision(context);
    }
}
