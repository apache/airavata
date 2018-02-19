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
package org.apache.airavata.gfac.core;

import org.apache.airavata.common.utils.LocalEventPublisher;
import org.apache.airavata.gfac.core.context.ProcessContext;
import org.apache.curator.framework.CuratorFramework;

/**
 * This is the GFac CPI interface which needs to be implemented by an internal class, this simply have a single method to submit a job to
 * the resource, required data for the job has to be stored in registry prior to invoke this object.
 */
public interface GFac {

    /**
     * Initialized method, this method must call one time before use any other method.
     * @param curatorClient
     * @param publisher
     * @return
     */
    public boolean init(CuratorFramework curatorClient, LocalEventPublisher publisher);

    /**
     * This is the job launching method outsiders of GFac can use, this will invoke the GFac handler chain and providers
     * And update the registry occordingly, so the users can query the database to retrieve status and output from Registry
     *
     * @param experimentID
     * @return boolean Successful acceptence of the jobExecution returns a true value
     * @throws GFacException
     */
    public boolean submitJob(String experimentID,String taskID, String gatewayID, String tokenId) throws GFacException;

    /**
     * This method can be used in a handler to ivvoke outhandler asynchronously
     * @param processContext
     * @throws GFacException
     */
    public void invokeOutFlowHandlers(ProcessContext processContext) throws GFacException;

    /**
     * This method can be used to handle re-run case asynchronously
     * @param processContext
     * @throws GFacException
     */
    public void reInvokeOutFlowHandlers(ProcessContext processContext) throws GFacException;

    /**
     * This operation can be used to cancel an already running experiment
     * @return Successful cancellation will return true
     * @throws GFacException
     */
    public boolean cancel(String experimentID, String taskID, String gatewayID, String tokenId)throws GFacException;

}
