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

package org.apache.airavata.core.gfac.services.impl;

import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.exception.GfacException;
import org.apache.airavata.core.gfac.extension.DataServiceChain;
import org.apache.airavata.core.gfac.extension.ExitableChain;
import org.apache.airavata.core.gfac.extension.PostExecuteChain;
import org.apache.airavata.core.gfac.extension.PreExecuteChain;
import org.apache.airavata.core.gfac.provider.Provider;
import org.apache.airavata.core.gfac.scheduler.Scheduler;
import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public abstract class AbstractSimpleService implements GenericService {
    
    private static Log log = LogFactory.getLog(AbstractSimpleService.class);

    public abstract void preProcess(InvocationContext context) throws GfacException;

    public abstract void postProcess(InvocationContext context) throws GfacException;

    public abstract Scheduler getScheduler(InvocationContext context) throws GfacException;

    public abstract PreExecuteChain[] getPreExecutionSteps(InvocationContext context) throws GfacException;

    public abstract PostExecuteChain[] getPostExecuteSteps(InvocationContext context) throws GfacException;

    public abstract DataServiceChain[] getDataChains(InvocationContext context) throws GfacException;

    public final void execute(InvocationContext context) throws GfacException {

        log.debug("Before preprocess");
        
        /*
         * Pre-Process
         */
        preProcess(context);
        
        log.debug("After preprocess, try to get Scheduler and schedule");

        /*
         * Determine provider
         */
        Provider provider = getScheduler(context).schedule(context);
        
        log.debug("After scheduling, try to run data chain");

        /*
         * Load data necessary data
         */
        buildChains(getDataChains(context)).start(context);

        log.debug("After data chain, try to init provider");
        
        /*
         * Init
         */
        provider.initialize(context);
        
        log.debug("After provider initialization, try to run pre-execution chain");

        /*
         * Pre-Execution
         */
        buildChains(getPreExecutionSteps(context)).start(context);

        log.debug("After pre-execution chain, try to execute provider");
        
        /*
         * Execute
         */
        provider.execute(context);
        
        log.debug("After provider execution, try to run post-execution chain");

        /*
         * Post-Execution
         */
        buildChains(getPostExecuteSteps(context)).start(context);

        log.debug("After pre-execution chain, try to dispose provider");
        
        /*
         * Destroy
         */
        provider.dispose(context);
        
        log.debug("After provider disposal, try to run postprocess");

        /*
         * Pre-Process
         */
        postProcess(context);
        
        log.debug("After postprocess");
    }

    private ExitableChain buildChains(ExitableChain[] list) {

        /*
         * Validation check and return doing-nothing chain object
         */
        if (list == null || list.length == 0) {
            return new ExitableChain() {
                @Override
                protected boolean execute(InvocationContext context) {
                    return true;
                }
            };
        }

        ExitableChain currentPoint = list[0];
        for (int i = 1; i < list.length; i++) {
            currentPoint = currentPoint.setNext(list[i]);
        }
        return currentPoint;
    }
}
