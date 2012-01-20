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

package org.apache.airavata.core.gfac.extension.post;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.MessageContext;
import org.apache.airavata.core.gfac.context.message.impl.WorkflowContextImpl;
import org.apache.airavata.core.gfac.exception.ExtensionException;
import org.apache.airavata.core.gfac.extension.PostExecuteChain;
import org.apache.airavata.registry.api.DataRegistry;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Register output to Registry
 */
public class OutputRegister extends PostExecuteChain {

    private static final Logger log = LoggerFactory.getLogger(OutputRegister.class);

    public boolean execute(InvocationContext context) throws ExtensionException {
        // output context
        MessageContext<ActualParameter> outputContext = context.getOutput();

        // workflow context
        MessageContext<String> workflowContext = context.getMessageContext(WorkflowContextImpl.WORKFLOW_CONTEXT_NAME);

        // registry
        AiravataRegistry registry = context.getExecutionContext().getRegistryService();

        if (outputContext != null && workflowContext != null) {

            String workflowId = workflowContext.getValue(WorkflowContextImpl.WORKFLOW_ID);
            List<ActualParameter> outputs = new ArrayList<ActualParameter>();

            for (Iterator<String> iterator = outputContext.getNames(); iterator.hasNext();) {
                String key = iterator.next();
                outputs.add(outputContext.getValue(key));
            }

            if (registry != null && DataRegistry.class.isAssignableFrom(registry.getClass())) {
                try {
					((DataRegistry) registry).saveOutput(workflowId, outputs);
				} catch (RegistryException e) {
					log.error(e.getLocalizedMessage(), e);
				}
            } else {
                log.debug("Registry does not support for Data Catalog, CLass: " + registry.getClass());
            }

        } else {
            log.debug("Context is null");
        }
        return false;
    }
}
