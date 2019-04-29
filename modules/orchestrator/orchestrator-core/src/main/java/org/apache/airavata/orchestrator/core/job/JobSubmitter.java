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
package org.apache.airavata.orchestrator.core.job;

import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;

/**
 * This is the submitter interface, orchestrator can
 * submit jobs to gfac in different modes, gfac running embedded
 * or gfac running in server mode. This can be configured in
 * airavata-server.properties
 * todo provide a way to configure this in a dynamic way
 */
public interface JobSubmitter {


    void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException;

    /**
     * This is similar to submit with expId and taskId but this has extra param called token
     * @param experimentId
     * @param processId
     * @param tokenId
     * @return
     * @throws OrchestratorException
     */
    boolean submit(String experimentId,String processId,String tokenId) throws OrchestratorException;

    /**
     * This can be used to terminate the experiment
     * @param experimentId
     * @param processId
     * @return
     * @throws OrchestratorException
     */
    boolean terminate(String experimentId,String processId, String tokenId)throws OrchestratorException;
}
