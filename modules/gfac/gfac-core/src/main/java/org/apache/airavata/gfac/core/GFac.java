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
package org.apache.airavata.gfac.core;

import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.airavata.registry.cpi.AppCatalog;
import org.apache.airavata.common.utils.LocalEventPublisher;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.registry.cpi.ExperimentCatalog;
import org.apache.curator.framework.CuratorFramework;

/**
 * This is the GFac CPI interface which needs to be implemented by an internal class, this simply have a single method to submit a job to
 * the resource, required data for the job has to be stored in registry prior to invoke this object.
 */
public interface GFac {

    /**
     * Launching a process, this method run process inflow task and job submission task.
     *
     * @param processContext
     * @return boolean Successful acceptence of the jobExecution returns a true value
     * @throws GFacException
     */
    public boolean submitProcess(ProcessContext processContext) throws GFacException;

    /**
     *  This will invoke outflow tasks for a given process.
     * @param processContext
     * @throws GFacException
     */
    public void invokeProcessOutFlow(ProcessContext processContext) throws GFacException;

    /**
     * This will reInvoke outflow tasks for a given process.
     * @param processContext
     * @throws GFacException
     */
    public void reInvokeProcessOutFlow(ProcessContext processContext) throws GFacException;

    /**
     * This operation can be used to cancel an already running process.
     * @return Successful cancellation will return true
     * @throws GFacException
     */
    public boolean cancelProcess(ProcessContext processContext)throws GFacException;

}
