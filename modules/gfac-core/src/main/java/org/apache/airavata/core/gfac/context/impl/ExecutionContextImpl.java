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

package org.apache.airavata.core.gfac.context.impl;

import org.apache.airavata.core.gfac.api.Registry;
import org.apache.airavata.core.gfac.context.ExecutionContext;
import org.apache.airavata.core.gfac.context.SecurityContext;
import org.apache.airavata.core.gfac.context.ServiceContext;
import org.apache.airavata.core.gfac.model.ExecutionModel;
import org.apache.airavata.core.gfac.notification.NotificationService;
import org.ogce.namespaces.x2010.x08.x30.workflowContextHeader.WorkflowContextHeaderDocument.WorkflowContextHeader;

public class ExecutionContextImpl implements ExecutionContext {

    private ExecutionModel executionModel;
    private WorkflowContextHeader workflowContextHeader;
    private NotificationService notificationService;
    private Registry registryService;
    private SecurityContext securityContext;

    public ExecutionModel getExecutionModel() {
        return executionModel;
    }

    public void setExectionModel(ExecutionModel model) {
        this.executionModel = model;
    }

    public WorkflowContextHeader getWorkflowHeader() {
        return workflowContextHeader;
    }

    public void setWorkflowHeader(WorkflowContextHeader header) {
        this.workflowContextHeader = header;
    }

    public NotificationService getNotificationService() {
        return this.notificationService;
    }

    public void setNotificationService(NotificationService service) {
        this.notificationService = service;

    }

    public SecurityContext getSecurityContext() {
        return this.securityContext;
    }

    public void setSecurityContext(SecurityContext context) {
        this.securityContext = context;

    }

    public ServiceContext getServiceContext() {
        // TODO Auto-generated method stub
        return null;
    }

    public void setServiceContext(ServiceContext context) {
        // TODO Auto-generated method stub

    }

    public Registry getRegistryService() {
        return this.registryService;
    }

    public void setRegistryService(Registry registryService) {
        this.registryService = registryService;
    }

}
