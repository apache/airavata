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
package org.apache.airavata.monitor;

/**
 * Functional contract for job monitors. Implementations receive job status results
 * and are responsible for validating and forwarding them to the appropriate queue.
 */
public interface AbstractMonitor {

    /**
     * Submit a job status result for processing. Implementations should validate
     * the result and publish it to the job status queue.
     *
     * @param jobStatusResult the status result to submit
     * @throws MonitoringException if submission fails
     */
    void submitJobStatus(JobStatusResult jobStatusResult) throws MonitoringException;
}
