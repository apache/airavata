/**
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
 */
package org.apache.airavata.gfac.core.monitor;

import org.apache.airavata.gfac.core.context.TaskContext;

public interface JobMonitor {

	/**
	 * Start monitor jobId on remote computer resource.
	 * @param jobId
	 * @param taskContext
	 */
	void monitor(String jobId, TaskContext taskContext);

	/**
	 * Stop monitoring for given jobId
	 */
	void stopMonitor(String jobId, boolean runOutFlow);

    /**
     * Return <code>true</code> if jobId is already monitoring by this Monitor, <code>false</code> if not
     */
    boolean isMonitoring(String jobId);

	/**
	 * make monitor service aware of cancelled jobs, in case job monitor details doesn't comes withing predefine time
	 * it will move job to CANCELED state and call output
	 */
	void canceledJob(String jobId);
}
