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
package org.apache.airavata.registry.service;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.registry.api.impl.JCRRegistry;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.registry.api.WorkflowExecution;
import org.apache.airavata.registry.api.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.impl.AiravataJCRRegistry;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.ServiceLifeCycle;
import org.apache.xmlbeans.XmlException;
import org.xmlpull.infoset.XmlElement;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

public class RegistryService implements ServiceLifeCycle{
    public static final String JCR_PASSWORD = "jcr.password";
    public static final String JCR_USERNAME = "jcr.username";
    public static final String JRC_URL = "jcr.url";
    public static final String JCR_CLASS = "jcr.class";
    public static final String HOST_SPLITTER = "*%host^!";
    public static final String APP_SPLITTER = "*%app^!";
    AiravataRegistry registry;
    @Override
    public void startUp(ConfigurationContext configurationContext, AxisService axisService) {
        //To change body of implemented methods use File | Settings | File Templates.
        URL url = this.getClass().getClassLoader().getResource("jcr.properties");
        Properties properties = new Properties();
        try {
            properties.load(url.openStream());
            configurationContext.setProperty(JCR_PASSWORD, properties.get(JCR_PASSWORD));
            configurationContext.setProperty(JCR_USERNAME, properties.get(JCR_USERNAME));
            String jcrUserName = (String)properties.get(JCR_PASSWORD);
            String jcrPassword = (String) properties.get(JCR_USERNAME);
            String jcrURL = (String) properties.get(JRC_URL);
            String className = (String)properties.get(JCR_CLASS);
            registry = new AiravataJCRRegistry(new URI(jcrURL),className,jcrUserName,jcrPassword,new HashMap<String,String>());
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (URISyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Override
    public void shutDown(ConfigurationContext configurationContext, AxisService axisService) {
        //To change body of implemented methods use File | Settings | File Templates.
        ((JCRRegistry)registry).closeConnection();
    }

    public boolean saveWorkflowExecutionServiceInput(WorkflowServiceIOData data)throws RegistryException{
        return registry.saveWorkflowExecutionServiceInput(data);
    }

    public boolean saveWorkflowExecutionServiceOutput(WorkflowServiceIOData workflowOutputData) throws RegistryException{
        return registry.saveWorkflowExecutionServiceOutput(workflowOutputData);
    }

    public List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(String experimentIdRegEx, String workflowNameRegEx,
                                                                           String nodeNameRegEx) throws RegistryException{
        return registry.searchWorkflowExecutionServiceInput(experimentIdRegEx,workflowNameRegEx,nodeNameRegEx);
    }

    public List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(String experimentIdRegEx, String workflowNameRegEx, String nodeNameRegEx)throws RegistryException{
        return registry.searchWorkflowExecutionServiceOutput(experimentIdRegEx,workflowNameRegEx,nodeNameRegEx);
    }

    public boolean saveWorkflowExecutionStatus(String experimentId,WorkflowInstanceStatus status)throws RegistryException{
        return registry.saveWorkflowExecutionStatus(experimentId,status);
    }

    public WorkflowInstanceStatus getWorkflowExecutionStatus(String experimentId)throws RegistryException{
        return registry.getWorkflowExecutionStatus(experimentId);
    }

    public boolean saveWorkflowExecutionOutput(String experimentId,String outputNodeName,String output) throws RegistryException{
        return registry.saveWorkflowExecutionOutput(experimentId,outputNodeName,output);
    }


    public boolean saveWorkflowExecutionOutput(String experimentId, WorkflowIOData data) throws RegistryException{
        return registry.saveWorkflowExecutionOutput(experimentId,data);
    }


    public WorkflowIOData getWorkflowExecutionOutput(String experimentId,String outputNodeName) throws RegistryException{
        return registry.getWorkflowExecutionOutput(experimentId,outputNodeName);
    }


    public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId) throws RegistryException{
        return registry.getWorkflowExecutionOutput(experimentId);
    }


    public String[] getWorkflowExecutionOutputNames(String exeperimentId) throws RegistryException{
        return registry.getWorkflowExecutionOutputNames(exeperimentId);
    }


    public boolean saveWorkflowExecutionUser(String experimentId, String user) throws RegistryException{
        return registry.saveWorkflowExecutionUser(experimentId,user);
    }


    public String getWorkflowExecutionUser(String experimentId) throws RegistryException{
        return registry.getWorkflowExecutionUser(experimentId);
    }


    public WorkflowExecution getWorkflowExecution(String experimentId) throws RegistryException{
        return registry.getWorkflowExecution(experimentId);
    }


    public List<String> getWorkflowExecutionIdByUser(String user) throws RegistryException{
        return registry.getWorkflowExecutionIdByUser(user);
    }


    public List<WorkflowExecution> getWorkflowExecutionByUser(String user) throws RegistryException{
        return registry.getWorkflowExecutionByUser(user);
    }


    public List<WorkflowExecution> getWorkflowExecutionByUserLimited(String user, int pageSize, int pageNo) throws RegistryException{
        return registry.getWorkflowExecutionByUser(user,pageSize,pageNo);
    }


    public String getWorkflowExecutionMetadata(String experimentId) throws RegistryException{
        return registry.getWorkflowExecutionMetadata(experimentId);
    }

    /* Document related methods */

    public boolean saveWorkflowExecutionMetadata(String experimentId, String metadata) throws RegistryException{
        return registry.saveWorkflowExecutionMetadata(experimentId,metadata);
    }

    public String getServiceDescription(String serviceId) throws RegistryException{
        ServiceDescription serviceDescription = registry.getServiceDescription(serviceId);
        return serviceDescription.toXML();
    }

    public String getDeploymentDescription(String serviceId, String hostId)
            throws RegistryException{
        ApplicationDeploymentDescription deploymentDescription = registry.getDeploymentDescription(serviceId, hostId);
        return  deploymentDescription.toXML();
    }

    public String getHostDescription(String hostId) throws RegistryException{
        HostDescription hostDescription = registry.getHostDescription(hostId);
        return hostDescription.toXML();
    }

    public String saveHostDescription(HostDescription host)throws RegistryException{
        return registry.saveHostDescription(host);
    }

    public String saveServiceDescription(ServiceDescription service)throws RegistryException{
        return registry.saveServiceDescription(service);
    }

    public String saveDeploymentDescription(String serviceId, String hostId, String app)throws RegistryException{
        try {
            return registry.saveDeploymentDescription(serviceId, hostId, ApplicationDeploymentDescription.fromXML(app));
        } catch (XmlException e) {
            throw new RegistryException("Error saving ApplicationDescription Creation/Saving" , e);  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    public boolean deployServiceOnHost(String serviceName, String hostName)throws RegistryException{
        return registry.deployServiceOnHost(serviceName,hostName);
    }

    public List<String> searchHostDescription(String name) throws RegistryException{
        List<HostDescription> hostDescriptions = registry.searchHostDescription(name);
        List<String> hostStringList = new ArrayList<String>();
        for(HostDescription hostDescription:hostDescriptions){
           hostStringList.add(hostDescription.toXML());
        }
        return hostStringList;
    }


    public List<String> searchDeploymentDescriptionWithSerivceHost(String serviceName, String hostName)
            throws RegistryException{
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptions = registry.searchDeploymentDescription(serviceName, hostName);
        List<String> hostStringList = new ArrayList<String>();
        for(ApplicationDeploymentDescription hostDescription:applicationDeploymentDescriptions){
           hostStringList.add(hostDescription.toXML());
        }
        return hostStringList;
    }

    public List<String> searchDeploymentDescriptionWithService(String serviceName)
            throws RegistryException{
        List<String>  list = new ArrayList<String>();
        Map<HostDescription, List<ApplicationDeploymentDescription>> hostDescriptionListMap = registry.searchDeploymentDescription(serviceName);
        for(HostDescription host:hostDescriptionListMap.keySet()){
            StringBuffer eachLine =  new StringBuffer(host.toXML() + HOST_SPLITTER);
            List<ApplicationDeploymentDescription> applicationDeploymentDescriptions = hostDescriptionListMap.get(host);
            for(ApplicationDeploymentDescription description:applicationDeploymentDescriptions){
                eachLine.append(description.toXML());
                eachLine.append(APP_SPLITTER);
            }
           list.add(eachLine.toString());
        }
        return list;
    }

    public List<String> searchDeploymentDescriptionWithServiceHostApp(String serviceName, String hostName,
            String applicationName) throws RegistryException{
        List<ApplicationDeploymentDescription> applicationDeploymentDescriptions = registry.searchDeploymentDescription(serviceName, hostName, applicationName);
         List<String> hostStringList = new ArrayList<String>();
        for(ApplicationDeploymentDescription hostDescription:applicationDeploymentDescriptions){
           hostStringList.add(hostDescription.toXML());
        }
        return hostStringList;
    }

    public List<String> searchDeploymentDescription() throws RegistryException{
       List<String>  list = new ArrayList<String>();
        Map<ApplicationDeploymentDescription,String> hostDescriptionListMap = registry.searchDeploymentDescription();
        for(ApplicationDeploymentDescription host:hostDescriptionListMap.keySet()){
            StringBuffer eachLine =  new StringBuffer(host.toXML() + APP_SPLITTER);
            eachLine.append(hostDescriptionListMap.get(host));
            list.add(eachLine.toString());
        }
        return list;
    }

    public boolean saveGFacDescriptor(String gfacURL)throws RegistryException{
        return registry.saveGFacDescriptor(gfacURL);
    }

    public boolean deleteGFacDescriptor(String gfacURL) throws RegistryException{
        return registry.deleteGFacDescriptor(gfacURL);
    }

     public List<String> getGFacDescriptorList() throws RegistryException{
         return registry.getGFacDescriptorList();
     }

    public boolean saveWorkflow(QName ResourceID, String workflowName, String resourceDesc, String workflowAsaString,
            String owner, boolean isMakePublic) throws RegistryException{
        return  registry.saveWorkflow(ResourceID, workflowName,resourceDesc, workflowAsaString,
            owner, isMakePublic);
    }

    public boolean deleteWorkflow(QName resourceID, String userName) throws RegistryException{
        return registry.deleteWorkflow(resourceID,userName);
    }

    public void deleteServiceDescription(String serviceId) throws RegistryException{
        registry.deleteServiceDescription(serviceId);
    }

    public void deleteDeploymentDescription(String serviceName, String hostName, String applicationName)
            throws RegistryException{
        registry.deleteDeploymentDescription(serviceName,hostName,applicationName);
    }

    public void deleteHostDescription(String hostId) throws RegistryException{
        registry.deleteHostDescription(hostId);
    }

    public List<String> getWorkflows(String userName) throws RegistryException{
        Map<QName, Node> workflows = registry.getWorkflows(userName);
        List<String>  list = new ArrayList<String>();
        XmlElement xwf = null;
        for(QName name:workflows.keySet()){
            StringBuffer eachLine = new StringBuffer("");
            eachLine.append(name.getPrefix());
            eachLine.append(",");
            eachLine.append(name.getLocalPart());
            eachLine.append(",");
            eachLine.append(name.getNamespaceURI());
            Node workflow = workflows.get(name);
            try {
                xwf = XMLUtil.stringToXmlElement(workflow.getProperty("workflow").getString());
            } catch (RepositoryException e) {
                throw new RegistryException("Error while parsing workflow Content",e);  //To change body of catch statement use File | Settings | File Templates.
            }
            eachLine.append(XMLUtil.xmlElementToString(xwf));
            list.add(eachLine.toString());
        }
        return list;
    }

    public String getWorkflow(QName templateID, String userName) throws RegistryException{
        Node workflow = registry.getWorkflow(templateID, userName);
        XmlElement xwf = null;
        try {
            xwf = XMLUtil.stringToXmlElement(workflow.getProperty("workflow").getString());
        } catch (RepositoryException e) {
            throw new RegistryException("Error while parsing workflow Content",e);  //To change body of catch statement use File | Settings | File Templates.
        }
        return XMLUtil.xmlElementToString(xwf);
    }

}
