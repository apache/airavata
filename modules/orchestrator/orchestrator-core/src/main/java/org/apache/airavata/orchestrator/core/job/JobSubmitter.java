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
package org.apache.airavata.orchestrator.core.job;

import java.util.List;

import org.apache.airavata.orchestrator.core.context.OrchestratorContext;
import org.apache.airavata.orchestrator.core.exception.OrchestratorException;
import org.apache.airavata.orchestrator.core.gfac.GFACInstance;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.JobRequest;

/**
 * This is the submitter interface, orchestrator can
 * submit jobs to gfac in different modes, gfac running embedded
 * or gfac running in server mode. This can be configured in
 * orchestrator.properties
 * todo provide a way to configure this in a dynamic way
 */
public interface JobSubmitter {


    void initialize(OrchestratorContext orchestratorContext) throws OrchestratorException;

    /**
     * This will return a single GFACInstance among multiple if available
     * @return
     */
    GFACInstance selectGFACInstance() throws OrchestratorException;

    /**
     * @param gfac
     * @return
     */
    boolean submitJob(GFACInstance gfac,List<String> experimentIDList) throws OrchestratorException;

    /**
     * This can be used when user doesn't want to run in a threaded pull mode
     * just get the request data and do the submission
     * @param request
     * @return
     */
    boolean directJobSubmit(JobRequest request) throws OrchestratorException;
}
