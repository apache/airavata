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
package org.apache.airavata.compute.provider;

import org.apache.airavata.execution.dag.DagTaskResult;
import org.apache.airavata.execution.task.TaskContext;

/**
 * Full lifecycle contract for a compute provider.
 *
 * <p>Each compute backend (SLURM, AWS, Local) implements this interface as a
 * single class covering all lifecycle phases. The methods are registered as
 * individual {@link org.apache.airavata.execution.dag.DagTask} beans via
 * {@link ComputeProviderConfig} so the DAG engine can invoke them by name.
 *
 * <p>Lifecycle-independent logic (parsers, job specs, SSH adapters) stays in
 * separate utility classes.
 */
public interface ComputeProvider {

    // --- Resource lifecycle ---

    /** Acquire and prepare compute resources (e.g. create working directory, launch EC2). */
    DagTaskResult provision(TaskContext context);

    /** Release compute resources (cleanup working directory, terminate instance). */
    DagTaskResult deprovision(TaskContext context);

    // --- Job lifecycle ---

    /** Submit a job to the provisioned compute resource. */
    DagTaskResult submit(TaskContext context);

    /** Poll job status until the job reaches a terminal state. */
    DagTaskResult monitor(TaskContext context);

    /** Cancel running jobs on the compute resource. */
    DagTaskResult cancel(TaskContext context);
}
