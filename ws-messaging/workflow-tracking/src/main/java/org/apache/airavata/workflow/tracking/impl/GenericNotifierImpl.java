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

package org.apache.airavata.workflow.tracking.impl;

import org.apache.airavata.workflow.tracking.AbstractNotifier;
import org.apache.airavata.workflow.tracking.GenericNotifier;
import org.apache.airavata.workflow.tracking.WorkflowTrackingException;
import org.apache.airavata.workflow.tracking.common.InvocationContext;
import org.apache.airavata.workflow.tracking.common.InvocationEntity;
import org.apache.airavata.workflow.tracking.common.WorkflowTrackingContext;
import org.apache.airavata.workflow.tracking.impl.state.InvocationContextImpl;
import org.apache.airavata.workflow.tracking.types.*;
import org.apache.log4j.Logger;

/**
 * DOES NOT SUPPORT MULTI_THREADING -- PUBLISHER QUEUE, DATA CONSUMED/PRODUCED BATCHING
 * 
 * The constructor of this class uses the following properties from CONSTS: BROKER_URL, TOPIC, WORKFLOW_ID, NODE_ID,
 * TIMESTEP, SERVICE_ID, ASYNC_PUB_MODE
 */
public class GenericNotifierImpl extends AbstractNotifier implements GenericNotifier {

    // private AnnotationProps globalAnnotations;

    protected static final org.apache.log4j.Logger logger = Logger.getLogger(GenericNotifierImpl.class);

    public GenericNotifierImpl() throws WorkflowTrackingException {
        super();
    }

    public InvocationContext createInitialContext(WorkflowTrackingContext context) {
        if (context.getMyself() == null) {
            throw new RuntimeException("Local entity passed to createInitialContext was NULL");
        }
        return new InvocationContextImpl(context.getMyself(), null);
    }

    public InvocationContext createInvocationContext(WorkflowTrackingContext context, InvocationEntity remoteEntity) {

        if (context.getMyself() == null) {
            throw new RuntimeException("Local entity passed to createInitialContext was NULL");
        }

        if (remoteEntity == null) {
            throw new RuntimeException("Remote entity passed to createInitialContext was NULL");
        }

        return new InvocationContextImpl(context.getMyself(), remoteEntity);
    }

    public void debug(WorkflowTrackingContext context, String... descriptionAndAnnotation) {
        LogDebugDocument logMsg = LogDebugDocument.Factory.newInstance();
        BaseNotificationType log = logMsg.addNewLogDebug();
        // add timestamp and notification source; add description, and
        // annotation if present
        sendNotification(context, logMsg, descriptionAndAnnotation, null);
    }

    public void exception(WorkflowTrackingContext context, String... descriptionAndAnnotation) {
        LogExceptionDocument logMsg = LogExceptionDocument.Factory.newInstance();
        BaseNotificationType log = logMsg.addNewLogException();
        sendNotification(context, logMsg, descriptionAndAnnotation, null);
    }

    public void info(WorkflowTrackingContext context, String... descriptionAndAnnotation) {
        LogInfoDocument logMsg = LogInfoDocument.Factory.newInstance();
        BaseNotificationType log = logMsg.addNewLogInfo();
        // add timestamp and notification source; add description, and
        // annotation if present
        // publish activity
        sendNotification(context, logMsg, descriptionAndAnnotation, null);
    }

    public void publishURL(WorkflowTrackingContext context, String title, String url,
            String... descriptionAndAnnotation) {
        PublishURLDocument pubMsg = PublishURLDocument.Factory.newInstance();
        PublishURLDocument.PublishURL pub = pubMsg.addNewPublishURL();
        pub.setTitle(title);
        pub.setLocation(url);
        sendNotification(context, pubMsg, descriptionAndAnnotation, null);
    }

    public void warning(WorkflowTrackingContext context, String... descriptionAndAnnotation) {
        LogWarningDocument logMsg = LogWarningDocument.Factory.newInstance();
        BaseNotificationType log = logMsg.addNewLogWarning();
        sendNotification(context, logMsg, descriptionAndAnnotation, null);
    }

}
