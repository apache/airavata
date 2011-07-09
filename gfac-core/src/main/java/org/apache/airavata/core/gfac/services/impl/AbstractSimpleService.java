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

public abstract class AbstractSimpleService implements GenericService {

    public abstract void preProcess(InvocationContext context) throws GfacException;

    public abstract void postProcess(InvocationContext context) throws GfacException;

    public abstract Scheduler getScheduler(InvocationContext context) throws GfacException;

    public abstract PreExecuteChain[] getPreExecutionSteps(InvocationContext context) throws GfacException;

    public abstract PostExecuteChain[] getPostExecuteSteps(InvocationContext context) throws GfacException;

    public abstract DataServiceChain[] getDataChains(InvocationContext context) throws GfacException;

    public final void execute(InvocationContext context) throws GfacException {

        /*
         * Pre-Process
         */
        preProcess(context);

        /*
         * Determine provider
         */
        Provider provider = getScheduler(context).schedule(context);

        /*
         * Load data necessary data
         */
        buildChains(getDataChains(context)).start(context);

        /*
         * Init
         */
        provider.initialize(context);

        /*
         * Pre-Execution
         */
        buildChains(getPreExecutionSteps(context)).start(context);

        /*
         * Execute
         */
        provider.execute(context);

        /*
         * Post-Execution
         */
        buildChains(getPostExecuteSteps(context)).start(context);

        /*
         * Destroy
         */
        provider.dispose(context);

        /*
         * Pre-Process
         */
        postProcess(context);
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
