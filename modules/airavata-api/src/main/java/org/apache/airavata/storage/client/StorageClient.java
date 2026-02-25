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
package org.apache.airavata.storage.client;

import org.apache.airavata.execution.dag.DagTaskResult;
import org.apache.airavata.execution.task.TaskContext;

/**
 * Full lifecycle contract for a storage provider.
 *
 * <p>Each storage backend (SFTP, S3, etc.) implements this interface as a
 * single class covering all data movement phases. The methods are registered as
 * individual {@link org.apache.airavata.execution.dag.DagTask} beans via
 * {@link StorageClientConfig} so the DAG engine can invoke them by name.
 *
 * <p>Lifecycle-independent logic (adapter resolution, transfer utilities) stays
 * in separate utility classes.
 */
public interface StorageClient {

    // --- Data movement lifecycle ---

    /** Stage input files from storage to the compute resource. */
    DagTaskResult stageIn(TaskContext context);

    /** Stage output files from the compute resource back to storage. */
    DagTaskResult stageOut(TaskContext context);

    /** Create and transfer an archive of job outputs to storage. */
    DagTaskResult archive(TaskContext context);
}
