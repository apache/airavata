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

package org.apache.airavata.gfac;

import org.apache.airavata.gfac.context.JobExecutionContext;
import org.apache.airavata.gfac.handler.GFacHandler;
import org.apache.airavata.gfac.handler.GFacHandlerException;
import org.apache.airavata.gfac.notification.events.ExecutionFailEvent;
import org.apache.airavata.gfac.notification.events.FinishExecutionEvent;
import org.apache.airavata.gfac.notification.listeners.LoggingListener;
import org.apache.airavata.gfac.notification.listeners.WorkflowTrackingListener;
import org.apache.airavata.gfac.provider.GFacProvider;
import org.apache.airavata.gfac.provider.GFacProviderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class GFacAPI {
    private static final Logger log = LoggerFactory.getLogger(GFacAPI.class);



    public void submitJob(JobExecutionContext jobExecutionContext) throws GFacException {
        // We need to check whether this job is submitted as a part of a large workflow. If yes,
        // we need to setup workflow tracking listerner.
        String workflowInstanceID = null;
        if ((workflowInstanceID = (String) jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_INSTANCE_ID)) != null) {
            // This mean we need to register workflow tracking listener.
            registerWorkflowTrackingListener(workflowInstanceID, jobExecutionContext);
        }
        // Register log event listener. This is required in all scenarios.
        jobExecutionContext.getNotificationService().registerListener(new LoggingListener());
        schedule(jobExecutionContext);
    }

    private void schedule(JobExecutionContext jobExecutionContext) throws GFacException {
        // Scheduler will decide the execution flow of handlers and provider which handles
        // the job.
        Scheduler.schedule(jobExecutionContext);

        // Executing in handlers in the order as they have configured in GFac configuration
        invokeInFlowHandlers(jobExecutionContext);


        // After executing the in handlers provider instance should be set to job execution context.
        // We get the provider instance and execute it.
        GFacProvider provider = jobExecutionContext.getProvider();
        if (provider != null) {
            initProvider(provider, jobExecutionContext);
            executeProvider(provider, jobExecutionContext);
            disposeProvider(provider, jobExecutionContext);
        }

        invokeOutFlowHandlers(jobExecutionContext);
    }

    private void initProvider(GFacProvider provider, JobExecutionContext jobExecutionContext) throws GFacException {
        try {
            provider.initialize(jobExecutionContext);
        } catch (GFacProviderException e) {
            throw new GFacException("Error while initializing provider " + provider.getClass().getName() + ".", e);
        }
    }

    private void executeProvider(GFacProvider provider, JobExecutionContext jobExecutionContext) throws GFacException {
        try {
            provider.execute(jobExecutionContext);
        } catch (GFacProviderException e) {
            throw new GFacException("Error while executing provider " + provider.getClass().getName() + " functionality.", e);
        }
    }

    private void disposeProvider(GFacProvider provider, JobExecutionContext jobExecutionContext) throws GFacException {
        try {
            provider.dispose(jobExecutionContext);
        } catch (GFacProviderException e) {
            throw new GFacException("Error while invoking provider " + provider.getClass().getName() + " dispose method.", e);
        }
    }

    private void registerWorkflowTrackingListener(String workflowInstanceID, JobExecutionContext jobExecutionContext) {
        String workflowNodeID = (String) jobExecutionContext.getProperty(Constants.PROP_WORKFLOW_NODE_ID);
        String topic = (String) jobExecutionContext.getProperty(Constants.PROP_TOPIC);
        String brokerUrl = (String) jobExecutionContext.getProperty(Constants.PROP_BROKER_URL);
        jobExecutionContext.getNotificationService().registerListener(
                new WorkflowTrackingListener(workflowInstanceID, workflowNodeID, brokerUrl, topic));

    }

    private void invokeInFlowHandlers(JobExecutionContext jobExecutionContext) throws GFacException {
        List<String> handlers = jobExecutionContext.getGFacConfiguration().getInHandlers();

        for (String handlerClassName : handlers) {
            Class<? extends GFacHandler> handlerClass;
            GFacHandler handler;
            try {
                handlerClass = Class.forName(handlerClassName.trim()).asSubclass(GFacHandler.class);
                handler = handlerClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new GFacException("Cannot load handler class " + handlerClassName, e);
            } catch (InstantiationException e) {
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            } catch (IllegalAccessException e) {
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            }


            try {
                handler.invoke(jobExecutionContext);
                jobExecutionContext.getNotificationService().publish(new FinishExecutionEvent());
            } catch (GFacHandlerException e) {
                // TODO: Better error reporting.
                jobExecutionContext.getNotificationService().publish(new ExecutionFailEvent(e));
                log.error("Error occurred during in handler " + handlerClassName + " execution.");
                break;
            }
        }
    }

    private void invokeOutFlowHandlers(JobExecutionContext jobExecutionContext) throws GFacException {
        List<String> handlers = jobExecutionContext.getGFacConfiguration().getOutHandlers();

        for (String handlerClassName : handlers) {
            Class<? extends GFacHandler> handlerClass;
            GFacHandler handler;
            try {
                handlerClass = Class.forName(handlerClassName.trim()).asSubclass(GFacHandler.class);
                handler = handlerClass.newInstance();
            } catch (ClassNotFoundException e) {
                throw new GFacException("Cannot load handler class " + handlerClassName, e);
            } catch (InstantiationException e) {
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            } catch (IllegalAccessException e) {
                throw new GFacException("Cannot instantiate handler class " + handlerClassName, e);
            }


            try {
                handler.invoke(jobExecutionContext);
                jobExecutionContext.getNotificationService().publish(new FinishExecutionEvent());
            } catch (GFacHandlerException e) {
                // TODO: Better error reporting.
                jobExecutionContext.getNotificationService().publish(new ExecutionFailEvent(e));
                log.error("Error occurred during out handler " + handlerClassName + " execution.");
                break;
            }
        }
    }

}
