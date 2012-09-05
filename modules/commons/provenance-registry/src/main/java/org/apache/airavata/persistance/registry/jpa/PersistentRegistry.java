package org.apache.airavata.persistance.registry.jpa;

import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.*;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class PersistentRegistry extends AiravataRegistry2{


    @Override
    protected void initialize() {

    }

    public Object getConfiguration(String key) {
        return null;
    }

    public List<Object> getConfigurationList(String key) {
        return null;
    }

    public void setConfiguration(String key, String value, Date expire) {

    }

    public void addConfiguration(String key, String value, Date expire) {

    }

    public void removeAllConfiguration(String key) {

    }

    public void removeConfiguration(String key, String value) {

    }

    public List<URI> getGFacURIs() {
        return null;
    }

    public List<URI> getWorkflowInterpreterURIs() {
        return null;
    }

    public URI getEventingServiceURI() {
        return null;
    }

    public URI getMessageBoxURI() {
        return null;
    }

    public void addGFacURI(URI uri) {

    }

    public void addWorkflowInterpreterURI(URI uri) {

    }

    public void setEventingURI(URI uri) {

    }

    public void setMessageBoxURI(URI uri) {

    }

    public void addGFacURI(URI uri, Date expire) {

    }

    public void addWorkflowInterpreterURI(URI uri, Date expire) {

    }

    public void setEventingURI(URI uri, Date expire) {

    }

    public void setMessageBoxURI(URI uri, Date expire) {

    }

    public void removeGFacURI(URI uri) {

    }

    public void removeWorkflowInterpreterURI() {

    }

    public void removeAllGFacURI(URI uri) {

    }

    public void removeAllWorkflowInterpreterURI() {

    }

    public void unsetEventingURI() {

    }

    public void unsetMessageBoxURI() {

    }

    public void addHostDescriptor(HostDescription descriptor) {

    }

    public void updateHostDescriptor(HostDescription descriptor) {

    }

    public HostDescription getHostDescriptor(String hostName) {
        return null;
    }

    public void removeHostDescriptor(String hostName) {

    }

    public ResourceMetadata getHostDescriptorMetadata(String hostName) {
        return null;
    }

    public void addServiceDescriptor(ServiceDescription descriptor) {

    }

    public void updateServiceDescriptor(ServiceDescription descriptor) {

    }

    public ServiceDescription getServiceDescriptor(String serviceName) {
        return null;
    }

    public void removeServiceDescriptor(String serviceName) {

    }

    public ResourceMetadata getServiceDescriptorMetadata(String serviceName) {
        return null;
    }

    public void addApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) {

    }

    public void addApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) {

    }

    public void udpateApplicationDescriptor(ServiceDescription serviceDescription, HostDescription hostDescriptor, ApplicationDeploymentDescription descriptor) {

    }

    public void updateApplicationDescriptor(String serviceName, String hostName, ApplicationDeploymentDescription descriptor) {

    }

    public ApplicationDeploymentDescription getApplicationDescriptors(String serviceName, String hostname) {
        return null;
    }

    public Map<String, ApplicationDeploymentDescription> getApplicationDescriptors(String serviceName) {
        return null;
    }

    public void removeApplicationDescriptor(String serviceName, String hostName, String applicationName) {

    }

    public ResourceMetadata getApplicationDescriptorMetadata(String serviceName, String hostName, String applicationName) {
        return null;
    }

    public void addWorkspaceProject(WorkspaceProject project) {

    }

    public void updateWorkspaceProject(WorkspaceProject project) {

    }

    public void deleteWorkspaceProject(String projectName) {

    }

    public WorkspaceProject getWorkspaceProject(String projectName) {
        return null;
    }

    public void createExperiment(String projectName, AiravataExperiment experiment) {

    }

    public void removeExperiment(String experimentId) {

    }

    public List<AiravataExperiment> getExperiments() {
        return null;
    }

    public List<AiravataExperiment> getExperiments(String projectName) {
        return null;
    }

    public List<AiravataExperiment> getExperiments(Date from, Date to) {
        return null;
    }

    public List<AiravataExperiment> getExperiments(String projectName, Date from, Date to) {
        return null;
    }

    public void publishWorkflow(String workflowName, String workflowGraphXml) {

    }

    public void publishWorkflow(String workflowName) {

    }

    public String getPublishedWorkflowGraphXML(String workflowName) {
        return null;
    }

    public ResourceMetadata getPublishedWorkflowMetadata(String workflowName) {
        return null;
    }

    public void removePublishedWorkflow(String workflowName) {

    }

    public String getPublishedWorkflowUser(String workflowName) {
        return null;
    }

    public Date getWorkflowPublishedTime(String workflowName) {
        return null;
    }

    public void addWorkflow(String workflowName, String workflowGraphXml) {

    }

    public void updateWorkflow(String workflowName, String workflowGraphXml) {

    }

    public String getWorkflowGraphXML(String workflowName) {
        return null;
    }

    public ResourceMetadata getWorkflowMetadata(String workflowName) {
        return null;
    }

    public Date getWorkflowLastUpdatedTime(String workflowName) {
        return null;
    }

    public void removeWorkflow(String workflowName) {

    }

    public void setAiravataRegistry(AiravataRegistry2 registry) {

    }

    public void setAiravataUser(AiravataUser user) {

    }
}
