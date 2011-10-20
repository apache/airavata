package org.apache.airavata.xbaya.lead;/*
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

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.schemas.wec.*;
import org.xmlpull.v1.builder.XmlElement;

public class WorkflowContextHeaderBuilder {

    protected XmlElement header;

    private WorkflowMonitoringContextDocument.WorkflowMonitoringContext workflowMonitoringContext;

    private String gfacUrl;

    private String registryUrl;

    private SecurityContextDocument.SecurityContext securityContext;

    private SchedulingContextDocument.SchedulingContext schedulingContext;

    private String resourceSchedulerUrl;

    private String dataRegistryUrl;

    private String userName;

    private String outputDataDirectory;

    private WorkflowSchedulingDocument.WorkflowScheduling workflowScheduling;

    private ContextDocument.Context context;

    public WorkflowContextHeaderBuilder(String brokerUrl,String gfacUrl, String registryUrl) {
        this.context = ContextDocument.Context.Factory.newInstance();
        workflowMonitoringContext = WorkflowMonitoringContextDocument.WorkflowMonitoringContext.Factory.newInstance();
        workflowMonitoringContext.setEventPublishEpr(brokerUrl);
        this.context.setWorkflowMonitoringContext(workflowMonitoringContext);
        this.gfacUrl = gfacUrl;
        this.context.setGfacUrl(gfacUrl);
        this.registryUrl = registryUrl;
        this.context.setRegistryUrl(registryUrl);
    }

    public WorkflowContextHeaderBuilder setWorkflowMonitoringContext(WorkflowMonitoringContextDocument.WorkflowMonitoringContext workflowMonitoringContext) {
        this.workflowMonitoringContext = workflowMonitoringContext;
        return this;
    }

    public WorkflowContextHeaderBuilder setGfacUrl(String gfacUrl) {
        this.gfacUrl = gfacUrl;
        return this;
    }

    public WorkflowContextHeaderBuilder setWorkflowScheduling(WorkflowSchedulingDocument.WorkflowScheduling workflowScheduling) {
        this.workflowScheduling = workflowScheduling;
        return this;
    }

    public WorkflowContextHeaderBuilder setRegistryUrl(String registryUrl) {
        this.registryUrl = registryUrl;
        return this;
    }

    public WorkflowContextHeaderBuilder setSecurityContext(SecurityContextDocument.SecurityContext securityContext) {
        this.securityContext = securityContext;
        return this;
    }

    public WorkflowContextHeaderBuilder setSchedulingContext(SchedulingContextDocument.SchedulingContext schedulingContext) {
        this.schedulingContext = schedulingContext;
        return this;
    }

    public WorkflowContextHeaderBuilder setResourceSchedulerUrl(String resourceSchedulerUrl) {
        this.resourceSchedulerUrl = resourceSchedulerUrl;
        return this;
    }

    public WorkflowContextHeaderBuilder setDataRegistryUrl(String dataRegistryUrl) {
        this.dataRegistryUrl = dataRegistryUrl;
        return this;
    }

    public WorkflowContextHeaderBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public WorkflowContextHeaderBuilder setOutputDataDirectory(String outputDataDirectory) {
        this.outputDataDirectory = outputDataDirectory;
        return this;
    }

    public String getGfacUrl() {
        return gfacUrl;
    }

    public String getRegistryUrl() {
        return registryUrl;
    }

    public SecurityContextDocument.SecurityContext getSecurityContext() {
        return securityContext;
    }

    public SchedulingContextDocument.SchedulingContext getSchedulingContext() {
        return schedulingContext;
    }

    public String getResourceSchedulerUrl() {
        return resourceSchedulerUrl;
    }

    public String getDataRegistryUrl() {
        return dataRegistryUrl;
    }

    public String getUserName() {
        return userName;
    }

    public String getOutputDataDirectory() {
        return outputDataDirectory;
    }

    public WorkflowSchedulingDocument.WorkflowScheduling getWorkflowScheduling() {
        return workflowScheduling;
    }

    public ContextDocument.Context getContext() {
        return context;
    }

    public WorkflowMonitoringContextDocument.WorkflowMonitoringContext getWorkflowMonitoringContext() {
        return workflowMonitoringContext;
    }

    public XmlElement getXml(){
        return XMLUtil.stringToXmlElement3(this.context.toString());
    }
}
