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

package org.apache.airavata.core.gfac.provider;

import java.util.Map;

import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.exception.*;
import org.apache.airavata.core.gfac.notification.GFacNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AbstractProvider wraps up steps of execution for Provider. <br/>
 * The steps in execution are <br/>
 * - makeDirectory <br/>
 * - setupEnvironment <br/>
 * - executeApplication <br/>
 * - retrieveOutput <br/>
 */
public abstract class AbstractProvider implements Provider {
    protected final Logger log = LoggerFactory.getLogger(this.getClass());

    public void initialize(InvocationContext invocationContext) throws ProviderException {
        /*
         * Make a directory on the host
         */
        makeDirectory(invocationContext);
    }

    public void dispose(InvocationContext invocationContext) throws GfacException {
    }

    public Map<String, ?> execute(InvocationContext invocationContext) throws ProviderException {

    	processInput(invocationContext);
    	/*
         * Setup necessary environment
         */
        setupEnvironment(invocationContext);

        GFacNotifier notifier = invocationContext.getExecutionContext().getNotifier();

        notifier.startExecution(invocationContext);

        /*
         * Execution application
         */
        executeApplication(invocationContext);

        notifier.finishExecution(invocationContext);

        /*
         * Process output information
         */
        return processOutput(invocationContext);
    }

    protected abstract void makeDirectory(InvocationContext invocationContext) throws ProviderException;

    protected abstract void setupEnvironment(InvocationContext invocationContext) throws ProviderException;

    protected abstract void executeApplication(InvocationContext invocationContext) throws ProviderException;

    protected abstract Map<String, ?> processOutput(InvocationContext invocationContext) throws ProviderException;

    protected abstract Map<String, ?> processInput(InvocationContext invocationContext) throws ProviderException;
}
