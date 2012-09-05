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
package org.apache.airavata.persistance.registry.jpa.impl;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.*;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class AiravataJPARegistry extends AiravataRegistry2{
    @Override
    protected void initialize() {

    }

    public Object getConfiguration(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<Object> getConfigurationList(String key) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setConfiguration(String key, String value, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addConfiguration(String key, String value, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAllConfiguration(String key) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeConfiguration(String key, String value) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<URI> getGFacURIs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<URI> getWorkflowInterpreterURIs() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URI getEventingServiceURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public URI getMessageBoxURI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkflowInterpreterURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setEventingURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMessageBoxURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addGFacURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkflowInterpreterURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setEventingURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setMessageBoxURI(URI uri, Date expire) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeWorkflowInterpreterURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAllGFacURI(URI uri) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeAllWorkflowInterpreterURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unsetEventingURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unsetMessageBoxURI() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addHostDescriptor(HostDescription descriptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateHostDescriptor(HostDescription descriptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public HostDescription getHostDescriptor(String hostName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeHostDescriptor(String hostName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceMetadata getHostDescriptorMetadata(String hostName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addServiceDescriptor(ServiceDescription descriptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateServiceDescriptor(ServiceDescription descriptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ServiceDescription getServiceDescriptor(String serviceName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeServiceDescriptor(String serviceName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceMetadata getServiceDescriptorMetadata(String serviceName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName, String hostname) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkspaceProject(WorkspaceProject project) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateWorkspaceProject(WorkspaceProject project) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void deleteWorkspaceProject(String projectName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public WorkspaceProject getWorkspaceProject(String projectName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void createExperiment(String projectName, AiravataExperiment experiment) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeExperiment(String experimentId) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments(String projectName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments(Date from, Date to) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void publishWorkflow(String workflowName, String publishWorkflowName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void publishWorkflow(String workflowName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getPublishedWorkflowGraphXML(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removePublishedWorkflow(String workflowName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void addWorkflow(String workflowName, String workflowGraphXml) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void updateWorkflow(String workflowName, String workflowGraphXml) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getWorkflowGraphXML(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public ResourceMetadata getWorkflowMetadata(String workflowName) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void removeWorkflow(String workflowName) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAiravataRegistry(AiravataRegistry2 registry) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setAiravataUser(AiravataUser user) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
