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
package org.apache.airavata.gfac.core.handler;

import org.apache.airavata.common.utils.MonitorPublisher;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.cpi.BetterGfacImpl;
import org.apache.airavata.gfac.core.states.GfacPluginState;
import org.apache.airavata.gfac.core.utils.GFacUtils;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.messaging.event.TaskIdentifier;
import org.apache.airavata.model.messaging.event.TaskOutputChangeEvent;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractRecoverableHandler implements GFacRecoverableHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppDescriptorCheckHandler.class);
    protected Registry registry = null;

    protected MonitorPublisher publisher = null;

    protected AbstractRecoverableHandler() {
        publisher = BetterGfacImpl.getMonitorPublisher();   // This will not be null because this will be initialize in GFacIml
    }

    public void invoke(JobExecutionContext jobExecutionContext) throws GFacHandlerException {
        try {
            GFacUtils.updatePluginState(jobExecutionContext.getZk(), jobExecutionContext, this.getClass().getName(), GfacPluginState.INVOKED);
        } catch (Exception e) {
            logger.error("Error saving Recoverable provider state", e);
        }
        registry = jobExecutionContext.getRegistry();
        if (registry == null) {
            try {
                registry = RegistryFactory.getDefaultRegistry();
            } catch (RegistryException e) {
                throw new GFacHandlerException("unable to create registry instance", e);
            }
        }
    }

    public MonitorPublisher getPublisher() {
        return publisher;
    }

    public void setPublisher(MonitorPublisher publisher) {
        this.publisher = publisher;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    protected void fireTaskOutputChangeEvent(JobExecutionContext jobExecutionContext, List<OutputDataObjectType> outputArray) {
        TaskIdentifier taskIdentity = new TaskIdentifier(jobExecutionContext.getTaskData().getTaskID(),
                jobExecutionContext.getWorkflowNodeDetails().getNodeInstanceId(),
                jobExecutionContext.getExperimentID(),
                jobExecutionContext.getGatewayID());
        publisher.publish(new TaskOutputChangeEvent(outputArray, taskIdentity));
    }
}
