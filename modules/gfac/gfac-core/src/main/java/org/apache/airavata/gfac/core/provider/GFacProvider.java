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

package org.apache.airavata.gfac.core.provider;

import java.util.Map;

import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;

public interface GFacProvider{

    void initProperties(Map<String,String> properties) throws GFacProviderException,GFacException;
    /**
     * Initialize environment required for invoking the execute method of the provider. If environment setup is
     * done during the in handler execution, validation of environment will go here.
     * @param jobExecutionContext containing job execution related information.
     * @throws GFacProviderException in case of a error initializing the environment.
     */
    void initialize(JobExecutionContext jobExecutionContext) throws GFacProviderException,GFacException;

    /**
     * Invoke the providers intended functionality using information and data in job execution context.
     * @param jobExecutionContext containing job execution related information.
     * @throws GFacProviderException in case of a error executing the job.
     */
    void execute(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException;

    /**
     * Cleans up the acquired resources during initialization and execution of the job.
     * @param jobExecutionContext containing job execution related information.
     * @throws GFacProviderException in case of a error cleaning resources.
     */
    void dispose(JobExecutionContext jobExecutionContext) throws GFacProviderException,GFacException;

    /**
     * Cancels all jobs relevant to an experiment.
     * @param jobExecutionContext The job execution context, contains runtime information.
     * @return <code>true</code> if cancel operation success. <code>false</code> otherwise.
     * @throws GFacException If an error occurred while cancelling the job.
     */
    public boolean cancelJob(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException;


    /**
     * This method can be used to implement recovering part of the stateful handler
     * If you do not want to recover an already ran handler you leave this recover method empty.
     *
     * @param jobExecutionContext
     */
    public void recover(JobExecutionContext jobExecutionContext)throws GFacProviderException,GFacException;

    /**
     * FIXME :Added this method to make an quick fix, need to remove this monitor logic from provider and invoke separately.
     * @param jobExecutionContext
     * @throws GFacProviderException
     * @throws GFacException
     */
    public void monitor(JobExecutionContext jobExecutionContext) throws GFacProviderException, GFacException;
}
