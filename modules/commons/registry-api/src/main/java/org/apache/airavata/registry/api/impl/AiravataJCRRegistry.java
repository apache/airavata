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

package org.apache.airavata.registry.api.impl;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.xml.namespace.QName;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.registry.api.impl.JCRRegistry;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.commons.gfac.wsdl.WSDLConstants;
import org.apache.airavata.commons.gfac.wsdl.WSDLGenerator;
import org.apache.airavata.registry.api.AiravataProvenanceRegistry;
import org.apache.airavata.registry.api.Axis2Registry;
import org.apache.airavata.registry.api.DataRegistry;
import org.apache.airavata.registry.api.exception.DeploymentDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;
import org.apache.airavata.registry.api.workflow.WorkflowExecution;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
import org.apache.airavata.registry.api.workflow.WorkflowInstance;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.registry.api.workflow.WorkflowServiceIOData;
import org.apache.airavata.schemas.gfac.MethodType;
import org.apache.airavata.schemas.gfac.PortTypeType;
import org.apache.airavata.schemas.gfac.ServiceType;
import org.apache.airavata.schemas.gfac.ServiceType.ServiceName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AiravataJCRRegistry extends JCRRegistry implements Axis2Registry, DataRegistry {

	private static final String OUTPUT_NODE_NAME = "OUTPUTS";
    private static final String SERVICE_NODE_NAME = "SERVICE_HOST";
    private static final String GFAC_INSTANCE_DATA = "GFAC_INSTANCE_DATA";
    private static final String WORKFLOW_INTERPRETER_INSTANCE_DATA = "WORKFLOW_INTERPRETER_INSTANCE_DATA";
    private static final String MESSAGE_BOX_INSTANCE_DATA = "MESSAGE_BOX_INSTANCE_DATA";
    private static final String EVENTING_INSTANCE_DATA = "EVENTING_INSTANCE_DATA";
    private static final String AIRAVATA_CONFIG_NODE = "AIRAVATA_CONFIGURATION_DATA";
    private static final String DEPLOY_NODE_NAME = "APP_HOST";
    private static final String HOST_NODE_NAME = "GFAC_HOST";
    private static final String XML_PROPERTY_NAME = "XML";
    private static final String LINK_NAME = "LINK";
    private static final String PROPERTY_WORKFLOW_NAME = "workflowName";
    private static final String PROPERTY_WORKFLOW_IO_CONTENT = "content";

    public static final String WORKFLOWS = "WORKFLOWS";
    public static final String PUBLIC = "PUBLIC";
    public static final String REGISTRY_TYPE_WORKFLOW = "workflow";
    public static final int GFAC_URL_UPDATE_INTERVAL = 1000 * 60 * 60 * 3;
    public static final String WORKFLOW_DATA = "experiments";
    public static final String INPUT = "Input";
    public static final String OUTPUT = "Output";
    public static final String RESULT = "Result";
    public static final String WORKFLOW_STATUS_PROPERTY = "Status";
    public static final String WORKFLOW_STATUS_TIME_PROPERTY = "Status_Time";
    public static final String WORKFLOW_METADATA_PROPERTY = "Metadata";
    public static final String WORKFLOW_INSTANCE_NAME_PROPERTY = "Worflow_instace_name";
    public static final String WORKFLOW_USER_PROPERTY = "User";
    public static final String NOTIFICATION_STORE = "User";
    
    private AiravataProvenanceRegistry provenanceRegistry;
    private static Logger log = LoggerFactory.getLogger(AiravataJCRRegistry.class);

    public AiravataJCRRegistry(URI repositoryURI, String className,
			String user, String pass, Map<String, String> map)
			throws RepositoryException {
		super(repositoryURI, className, user, pass, map);
        setupProvenanceRegistry();
	}
    
    private Node getServiceNode(Session session) throws RepositoryException {
        return getOrAddNode(getRootNode(session), SERVICE_NODE_NAME);
    }

    private Node getDeploymentNode(Session session) throws RepositoryException {
        return getOrAddNode(getRootNode(session), DEPLOY_NODE_NAME);
    }

    private Node getHostNode(Session session) throws RepositoryException {
        return getOrAddNode(getRootNode(session), HOST_NODE_NAME);
    }

//    public List<HostDescription> getServiceLocation(String serviceId) {
//        Session session = null;
//        ArrayList<HostDescription> result = new ArrayList<HostDescription>();
//        try {
//            session = getSession();
//            Node node = getServiceNode(session);
//            Node serviceNode = node.getNode(serviceId);
//            if (serviceNode.hasProperty(LINK_NAME)) {
//                Property prop = serviceNode.getProperty(LINK_NAME);
//                Value[] vals = prop.getValues();
//                for (Value val : vals) {
//                    Node host = session.getNodeByIdentifier(val.getString());
//                    Property hostProp = host.getProperty(XML_PROPERTY_NAME);
//                    result.add(HostDescription.fromXML(hostProp.getString()));
//                }
//            }
//        } catch (Exception e) {
//            System.out.println(e);
//            e.printStackTrace();
//            // TODO propagate
//        } finally {
//            closeSession(session);
//        }
//        return result;
//    }

    public void deleteServiceDescription(String serviceId) throws ServiceDescriptionRetrieveException {
        Session session = null;
        try {
            session = getSession();
            Node serviceNode = getServiceNode(session);
            Node node = serviceNode.getNode(serviceId);
            if (node != null) {
                node.remove();
                Node deploymentNode = getDeploymentNode(session);
                if (deploymentNode!=null && deploymentNode.hasNode(serviceId)) {
					serviceNode = deploymentNode.getNode(serviceId);
					serviceNode.remove();
				}
				session.save();
//                triggerObservers(this);
            }
        } catch (Exception e) {
            throw new ServiceDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
    }

    public ServiceDescription getServiceDescription(String serviceId) throws ServiceDescriptionRetrieveException {
        Session session = null;
        ServiceDescription result = null;
        try {
            session = getSession();
            Node serviceNode = getServiceNode(session);
            Node node = serviceNode.getNode(serviceId);
            Property prop = node.getProperty(XML_PROPERTY_NAME);
            result = ServiceDescription.fromXML(prop.getString());
            } catch (Exception e) {
            throw new ServiceDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public ApplicationDeploymentDescription getDeploymentDescription(String serviceId, String hostId)
            throws RegistryException {
        Session session = null;
        ApplicationDeploymentDescription result = null;
        try {
            session = getSession();
            Node deploymentNode = getDeploymentNode(session);
            Node serviceNode = deploymentNode.getNode(serviceId);
            Node hostNode = serviceNode.getNode(hostId);
            List<Node> childNodes = getChildNodes(hostNode);
            for (Node app:childNodes) {
                Property prop = app.getProperty(XML_PROPERTY_NAME);
                result = ApplicationDeploymentDescription.fromXML(prop.getString());
                break;
            }
        } catch (PathNotFoundException e) {
            return null;
        } catch (Exception e) {
            log.error("Cannot get Deployment Description", e);
            throw new DeploymentDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public void deleteHostDescription(String hostId) throws RegistryException {
        Session session = null;
        try {
            session = getSession();
            Node hostNode = getHostNode(session);
            Node node = hostNode.getNode(hostId);
            if (node != null) {
                node.remove();
                session.save();
//                triggerObservers(this);
            }
        } catch (Exception e) {
            throw new HostDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
    }

    public ServiceDescription getServiceDesc(String serviceId) throws ServiceDescriptionRetrieveException {
        Session session = null;
        ServiceDescription result = null;
        try {
            session = getSession();
            Node serviceNode = getServiceNode(session);
            Node node = serviceNode.getNode(serviceId);
            Property prop = node.getProperty(XML_PROPERTY_NAME);
            result = ServiceDescription.fromXML(prop.getString());
        } catch (PathNotFoundException e) {
            return null;
        } catch (Exception e) {
            throw new ServiceDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public HostDescription getHostDescription(String hostId) throws RegistryException {
        Session session = null;
        HostDescription result = null;
        try {
            session = getSession();
            Node hostNode = getHostNode(session);
            Node node = hostNode.getNode(hostId);
            if (node != null) {
                result = getHostDescriptor(node);
            }
        } catch (PathNotFoundException e) {
            return null;
        } catch (Exception e) {
            log.debug(e.getMessage());
            e.printStackTrace();
            throw new HostDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    private HostDescription getHostDescriptor(Node node) throws RegistryException {
        HostDescription result;
        try {
            Property prop = node.getProperty(XML_PROPERTY_NAME);
            result = HostDescription.fromXML(prop.getString());
        } catch (Exception e) {
            throw new HostDescriptionRetrieveException(e);
        }
        return result;
    }

    public String saveHostDescription(HostDescription host) throws RegistryException{
        Session session = null;
        String result = null;
        try {
            session = getSession();
            Node hostNode = getHostNode(session);
            Node node = getOrAddNode(hostNode, host.getType().getHostName());
            node.setProperty(XML_PROPERTY_NAME, host.toXML());
            session.save();

            result = node.getIdentifier();
//            triggerObservers(this);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving host description!!!", e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public String saveServiceDescription(ServiceDescription service) throws RegistryException{
        Session session = null;
        String result = null;
        try {
            session = getSession();
            Node serviceNode = getServiceNode(session);
            Node node = getOrAddNode(serviceNode, service.getType().getName());
            node.setProperty(XML_PROPERTY_NAME, service.toXML());
            session.save();

            result = node.getIdentifier();
//            triggerObservers(this);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving service description!!!", e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public String saveDeploymentDescription(String serviceId, String hostId, ApplicationDeploymentDescription app) throws RegistryException {
        Session session = null;
        String result = null;
        try {
            session = getSession();
            Node deployNode = getDeploymentNode(session);
            Node serviceNode = getOrAddNode(deployNode, serviceId);
            Node hostNode = getOrAddNode(serviceNode, hostId);
            Node appName = getOrAddNode(hostNode, app.getType().getApplicationName().getStringValue());
            appName.setProperty(XML_PROPERTY_NAME, app.toXML());
            session.save();
            result = appName.getIdentifier();
//            triggerObservers(this);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving deployment description!!!", e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public boolean deployServiceOnHost(String serviceId, String hostId)throws RegistryException {
        Session session = null;
        try {
            session = getSession();
            Node serviceRoot = getServiceNode(session);
            Node hostRoot = getHostNode(session);

            Node serviceNode = serviceRoot.getNode(serviceId);
            Node hostNode = hostRoot.getNode(hostId);

            if (!serviceNode.hasProperty(LINK_NAME)) {
                serviceNode.setProperty(LINK_NAME, new String[] { hostNode.getIdentifier() });
            } else {
                Property prop = serviceNode.getProperty(LINK_NAME);
                Value[] vals = prop.getValues();
                ArrayList<String> s = new ArrayList<String>();
                for (Value val : vals) {
                    s.add(val.getString());
                }

                if (s.contains(hostNode.getIdentifier())) {
                    return false;
                }

                s.add(hostNode.getIdentifier());
                serviceNode.setProperty(LINK_NAME, s.toArray(new String[0]));
            }

            session.save();
            return true;
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving service on host!!!", e);
        } finally {
            closeSession(session);
        }
    }

    public List<ServiceDescription> searchServiceDescription(String nameRegEx) throws RegistryException {
        Session session = null;
        ArrayList<ServiceDescription> result = new ArrayList<ServiceDescription>();
        try {
            session = getSession();
            Node node = getServiceNode(session);
            List<Node> childNodes = getChildNodes(node);
            for (Node service:childNodes) {
                if (nameRegEx.equals("") || service.getName().matches(nameRegEx)) {
                    Property prop = service.getProperty(XML_PROPERTY_NAME);
                    result.add(ServiceDescription.fromXML(prop.getString()));
                }
            }
        } catch (Exception e) {
            throw new ServiceDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public List<HostDescription> searchHostDescription(String nameRegEx) throws RegistryException {
        Session session = null;
        List<HostDescription> result = new ArrayList<HostDescription>();
        try {
            session = getSession();
            Node node = getHostNode(session);
            List<Node> childNodes = getChildNodes(node);
            for (Node host:childNodes) {
                if (host != null && host.getName().matches(nameRegEx)) {
                    HostDescription hostDescriptor = getHostDescriptor(host);
                    result.add(hostDescriptor);
                }
            }
        } catch (Exception e) {
            throw new HostDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public Map<ApplicationDeploymentDescription, String> searchDeploymentDescription() throws RegistryException {
        Session session = null;
        Map<ApplicationDeploymentDescription, String> result = new HashMap<ApplicationDeploymentDescription, String>();
        try {
            session = getSession();
            Node deploymentNode = getDeploymentNode(session);
            List<Node> childNodes = getChildNodes(deploymentNode);
            for (Node serviceNode:childNodes) {
                List<Node> childNodes2 = getChildNodes(serviceNode);
                for (Node hostNode:childNodes2) {
                    List<Node> childNodes3 = getChildNodes(hostNode);
                    for (Node app:childNodes3) {
                        Property prop = app.getProperty(XML_PROPERTY_NAME);
                        result.put(ApplicationDeploymentDescription.fromXML(prop.getString()), serviceNode.getName()
                                + "$" + hostNode.getName());
                    }
                }
            }
        } catch (Exception e) {
            throw new DeploymentDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public void deleteDeploymentDescription(String serviceName, String hostName, String applicationName)
            throws RegistryException {
        Session session = null;
        try {
            session = getSession();
            Node deploymentNode = getDeploymentNode(session);
            Node serviceNode = deploymentNode.getNode(serviceName);
            Node hostNode = serviceNode.getNode(hostName);
            hostNode.remove();
            session.save();
        } catch (Exception e) {
            throw new DeploymentDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
    }

    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName,
            String applicationName) throws RegistryException {
        Session session = null;
        List<ApplicationDeploymentDescription> result = new ArrayList<ApplicationDeploymentDescription>();
        if (serviceName==null || hostName==null){
        	return result;
        }
        try {
            session = getSession();
            Node deploymentNode = getDeploymentNode(session);
            if (!deploymentNode.hasNode(serviceName)){
            	return result;
            }
            Node serviceNode = deploymentNode.getNode(serviceName);
            if (!serviceNode.hasNode(hostName)){
            	return result;
            }
            Node hostNode = serviceNode.getNode(hostName);
            List<Node> childNodes = getChildNodes(hostNode);
            for (Node app:childNodes) {
                Property prop = app.getProperty(XML_PROPERTY_NAME);
                ApplicationDeploymentDescription appDesc = ApplicationDeploymentDescription.fromXML(prop.getString());
                if (appDesc.getType().getApplicationName().getStringValue().matches(applicationName)) {
                    result.add(appDesc);
                }
            }
        } catch (PathNotFoundException e) {
            return result;
        } catch (Exception e) {
            throw new DeploymentDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }
	
	public Map<HostDescription,List<ApplicationDeploymentDescription>> searchDeploymentDescription(String serviceName)
            throws RegistryException{
		Session session = null;
		Map<HostDescription,List<ApplicationDeploymentDescription>> result = new HashMap<HostDescription,List<ApplicationDeploymentDescription>>();
		try {
			session = getSession();
			Node deploymentNode = getDeploymentNode(session);
			Node serviceNode = deploymentNode.getNode(serviceName);
            List<Node> childNodes = getChildNodes(serviceNode);
            for (Node hostNode:childNodes) {
				HostDescription hostDescriptor = getHostDescription(hostNode.getName());
	            List<Node> childNodes2 = getChildNodes(hostNode);
                for (Node app:childNodes2) {
                    result.put(hostDescriptor, new ArrayList<ApplicationDeploymentDescription>());
                    Property prop = app.getProperty(XML_PROPERTY_NAME);
					result.get(hostDescriptor).add(ApplicationDeploymentDescription.fromXML(prop.getString()));
				}
			}
		}catch (PathNotFoundException e){
            return result;
        } catch (Exception e) {
			throw new DeploymentDescriptionRetrieveException(e);
		} finally {
			closeSession(session);
		}
		return result;
	}
	
    public List<ApplicationDeploymentDescription> searchDeploymentDescription(String serviceName, String hostName)
            throws RegistryException {
        Session session = null;
        List<ApplicationDeploymentDescription> result = new ArrayList<ApplicationDeploymentDescription>();
        if (serviceName==null || hostName==null){
        	return result;
        }
        try {
            session = getSession();
            Node deploymentNode = getDeploymentNode(session);
            if (!deploymentNode.hasNode(serviceName)){
            	return result;
            }
            Node serviceNode = deploymentNode.getNode(serviceName);
            if (!serviceNode.hasNode(hostName)){
            	return result;
            }
            Node hostNode = serviceNode.getNode(hostName);
            List<Node> childNodes = getChildNodes(hostNode);
            for (Node app:childNodes) {
                Property prop = app.getProperty(XML_PROPERTY_NAME);
                result.add(ApplicationDeploymentDescription.fromXML(prop.getString()));
            }
        } catch (PathNotFoundException e) {
            return result;
        } catch (Exception e) {
            throw new DeploymentDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    // public String saveWSDL(ServiceDescription service, String WSDL) {
    // Session session = null;
    // String result = null;
    // try {
    // session = getSession();
    // Node serviceNode = getServiceNode(session);
    // Node node = getOrAddNode(serviceNode, service.getId());
    // node.setProperty(WSDL_PROPERTY_NAME, WSDL);
    // session.save();
    //
    // result = node.getIdentifier();
    // triggerObservers(this);
    // } catch (Exception e) {
    // System.out.println(e);
    // e.printStackTrace();
    // // TODO propagate
    // } finally {
    // closeSession(session);
    // }
    // return result;
    // }
    //
    // public String saveWSDL(ServiceDescription service) {
    // return saveWSDL(service, WebServiceUtil.generateWSDL(service));
    // }

    public String getWSDL(String serviceName) throws Exception {
        ServiceDescription serviceDescription = getServiceDescription(serviceName);
        if (serviceDescription != null) {
            return getWSDL(serviceDescription);
        }
        throw new ServiceDescriptionRetrieveException(new Exception("No service description from the name "
                + serviceName));
    }

    public String getWSDL(ServiceDescription service) throws Exception{
        try {
            
            ServiceType type = service.getType().addNewService();
            ServiceName name = type.addNewServiceName();
            name.setStringValue(service.getType().getName());
            name.setTargetNamespace("http://schemas.airavata.apache.org/gfac/type");
            if(service.getType().getPortType() == null){
                PortTypeType portType = service.getType().addNewPortType();
                MethodType methodType = portType.addNewMethod();
                methodType.setMethodName("invoke");
            }else{
                MethodType method = service.getType().getPortType().getMethod();
                if (method == null) {
                    MethodType methodType = service.getType().getPortType().addNewMethod();
                    methodType.setMethodName("invoke");
                } else {
                    service.getType().getPortType().getMethod().setMethodName("invoke");
                }
            }
            WSDLGenerator generator = new WSDLGenerator();
            Hashtable table = generator.generateWSDL(null, null, null, service.getType(), true);            
            return (String) table.get(WSDLConstants.AWSDL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Node getMetadataNode(Session session) throws RepositoryException{
        return getOrAddNode(getRootNode(session), AIRAVATA_CONFIG_NODE);
    }
    
    public boolean saveGFacDescriptor(String gfacURL) throws RegistryException{
        java.util.Date today = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        Timestamp timestamp = new Timestamp(today.getTime());
        Session session = null;
        try {
            URI uri = new URI(gfacURL);
            String propertyName = uri.getHost() + "-" + uri.getPort();
            session = getSession();
            Node gfacDataNode = getOrAddNode(getMetadataNode(session), GFAC_INSTANCE_DATA);
            try {
                Property prop = gfacDataNode.getProperty(propertyName);
                prop.setValue(gfacURL + ";" + timestamp.getTime());
                session.save();
            } catch (PathNotFoundException e) {
                gfacDataNode.setProperty(propertyName, gfacURL + ";" + timestamp.getTime());
                session.save();
            }
//            triggerObservers(this);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving GFac Descriptor to the registry!!!", e);
        } finally {
            closeSession(session);
        }
        return true;
    }

    public boolean deleteGFacDescriptor(String gfacURL) throws RegistryException{
        Session session = null;
        try {
            URI uri = new URI(gfacURL);
            String propertyName = uri.getHost() + "-" + uri.getPort();
            session = getSession();
            Node gfacDataNode = getOrAddNode(getMetadataNode(session), GFAC_INSTANCE_DATA);
            Property prop = gfacDataNode.getProperty(propertyName);
            if (prop != null) {
                prop.setValue((String) null);
                session.save();
//                triggerObservers(this);
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while deleting GFac Descriptions from registry!!!",e); 
        } finally {
            closeSession(session);
        }
        return true;
    }

    public List<String> getGFacDescriptorList() throws RegistryException{
        Session session = null;
        List<String> urlList = new ArrayList<String>();
        java.util.Date today = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        Timestamp timestamp = new Timestamp(today.getTime());
        try {
            session = getSession();
            Node gfacNode = getOrAddNode(getMetadataNode(session), GFAC_INSTANCE_DATA);
            PropertyIterator propertyIterator = gfacNode.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                if (!"nt:unstructured".equals(property.getString())) {
                    String x = property.getString();
                    Timestamp setTime = new Timestamp(new Long(property.getString().split(";")[1]));
                    if (GFAC_URL_UPDATE_INTERVAL > (timestamp.getTime() - setTime.getTime())) {
                        urlList.add(property.getString().split(";")[0]);
                    }
                }
            }
        } catch (RepositoryException e) {
            throw new RegistryException("Error while retrieving GFac Descriptor list!!!", e);
        }
        return urlList;
    }

    public String saveOutput(String workflowId, List<ActualParameter> parameters) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveOutput(workflowId, parameters);
    	}
    	Session session = null;
        String result = null;
        try {
            session = getSession();
            Node outputNode = getOrAddNode(getRootNode(session), OUTPUT_NODE_NAME);
            Node node = getOrAddNode(outputNode, workflowId);
            for (int i = 0; i < parameters.size(); i++) {
                node.setProperty(String.valueOf(i), parameters.get(i).toXML());
            }

            session.save();

            result = node.getIdentifier();
//            triggerObservers(this);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving workflow output to the registry!!!", e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public List<ActualParameter> loadOutput(String workflowId) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().loadOutput(workflowId);
    	}
        Session session = null;
        ArrayList<ActualParameter> result = new ArrayList<ActualParameter>();
        try {
            session = getSession();
            Node outputNode = getOrAddNode(getRootNode(session), OUTPUT_NODE_NAME);
            Node node = outputNode.getNode(workflowId);

            PropertyIterator it = node.getProperties();
            while (it.hasNext()) {
                Property prop = (Property) it.next();
                result.add(ActualParameter.fromXML(prop.getString()));
            }
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while loading workflow output from registry!!!", e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public Map<QName, Node> getWorkflows(String userName) throws RegistryException{
        Session session = null;
        Map<QName, Node> workflowList = new HashMap<QName, Node>();
        try {
            session = getSession();
            Node workflowListNode = getOrAddNode(getOrAddNode(getRootNode(session), WORKFLOWS), PUBLIC);
            List<Node> childNodes = getChildNodes(workflowListNode);
            for (Node nextNode:childNodes) {
                workflowList.put(new QName(nextNode.getName()), nextNode);
            }
            workflowListNode = getOrAddNode(getOrAddNode(getRootNode(session), WORKFLOWS), userName);
            childNodes = getChildNodes(workflowListNode);
            for (Node nextNode:childNodes) {
                workflowList.put(new QName(nextNode.getName()), nextNode);
            }

        } catch (Exception e) {
            throw new RegistryException("Error while retrieving workflows from registry!!!",e);
        }
        return workflowList;
    }

    public Node getWorkflow(QName templateID, String userName) throws RegistryException{
        Session session = null;
        Node result = null;
        try {
            session = getSession();
            Node workflowListNode = getOrAddNode(getOrAddNode(getRootNode(session), WORKFLOWS), userName);
            result = getOrAddNode(workflowListNode, templateID.getLocalPart());
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving workflow from registry!!!", e);
        }
        return result;
    }

    public boolean saveWorkflow(QName ResourceID, String workflowName, String resourceDesc, String workflowAsaString,
            String owner, boolean isMakePublic) throws RegistryException{
        Session session = null;
        try {
            session = getSession();
            Node workflowListNode = getOrAddNode(getRootNode(session), WORKFLOWS);
            Node workflowNode = null;
            if (isMakePublic) {
                workflowNode = getOrAddNode(getOrAddNode(workflowListNode, PUBLIC), workflowName);
            } else {
                workflowNode = getOrAddNode(getOrAddNode(workflowListNode, owner), workflowName);
            }
            workflowNode.setProperty("workflow", workflowAsaString);
            workflowNode.setProperty("Prefix", ResourceID.getPrefix());
            workflowNode.setProperty("LocalPart", ResourceID.getLocalPart());
            workflowNode.setProperty("NamespaceURI", ResourceID.getNamespaceURI());
            workflowNode.setProperty("public", isMakePublic);
            workflowNode.setProperty("Description", resourceDesc);
            workflowNode.setProperty("Type", REGISTRY_TYPE_WORKFLOW);
            session.save();
//            triggerObservers(this);
        } catch (Exception e) {
            throw new RegistryException("Error while saving workflow to the registry!!!", e);
        } finally {
            closeSession(session);
            return true;
        }
    }

    public boolean deleteWorkflow(QName resourceID, String userName) throws RegistryException{
        Session session = null;
        try {
            session = getSession();
            Node workflowListNode = getOrAddNode(getOrAddNode(getRootNode(session), WORKFLOWS), userName);
            Node result = getOrAddNode(workflowListNode, resourceID.getLocalPart());
            if (result != null) {
                result.remove();
                session.save();
//                triggerObservers(this);
            }
        } catch (Exception e) {
            throw new RegistryException("Error while deleting workflow from registry!!!", e);
        } finally {
            closeSession(session);
        }
        return false;
    }

    public boolean saveWorkflowExecutionServiceInput(WorkflowServiceIOData workflowInputData) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionServiceInput(workflowInputData);
    	}
        return saveWorkflowIO(workflowInputData, INPUT);
    }

    public boolean saveWorkflowExecutionServiceOutput(WorkflowServiceIOData workflowOutputData) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionServiceOutput(workflowOutputData);
    	}
        return saveWorkflowIO(workflowOutputData, OUTPUT);
    }


    private boolean saveWorkflowIO(WorkflowServiceIOData workflowOutputData, String type) throws RegistryException{
        Session session = null;
        boolean isSaved = true;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(workflowOutputData.getExperimentId(),session);
            workflowDataNode.setProperty(PROPERTY_WORKFLOW_NAME, workflowOutputData.getWorkflowName());
            workflowDataNode = getOrAddNode(getOrAddNode(workflowDataNode, workflowOutputData.getNodeId()), type);
            workflowDataNode.setProperty(PROPERTY_WORKFLOW_IO_CONTENT, workflowOutputData.getValue());
            session.save();
        } catch (Exception e) {
            isSaved = false;
            throw new RegistryException("Error while saving workflow execution service data!!!", e);
        } finally {
            closeSession(session);
        }
        return isSaved;
    }

    public List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(String experimentIdRegEx, String workflowNameRegEx,
            String nodeNameRegEx) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().searchWorkflowExecutionServiceInput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
    	}
        return searchWorkflowIO(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx, INPUT);
    }

    public List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(String experimentIdRegEx, String workflowNameRegEx,
            String nodeNameRegEx) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().searchWorkflowExecutionServiceOutput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
    	}
        return searchWorkflowIO(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx, OUTPUT);
    }

    private List<WorkflowServiceIOData> searchWorkflowIO(String experimentIdRegEx, String workflowNameRegEx,
            String nodeNameRegEx, String type) throws RegistryException{
        List<WorkflowServiceIOData> workflowIODataList = new ArrayList<WorkflowServiceIOData>();
        Session session = null;
        try {
            session = getSession();
            Node experimentsNode = getWorkflowDataNode(session);
            List<Node> childNodes = getChildNodes(experimentsNode);
            for (Node experimentNode:childNodes) {
                if (experimentIdRegEx != null && !experimentNode.getName().matches(experimentIdRegEx)) {
                    continue;
                }
                List<Node> childNodes2 = getChildNodes(experimentNode);
                for (Node workflowNode:childNodes2) {
                    String workflowName = null;
                    if (workflowNode.hasProperty(PROPERTY_WORKFLOW_NAME)) {
						workflowName = workflowNode.getProperty(
								PROPERTY_WORKFLOW_NAME).getString();
						if (workflowNameRegEx != null
								&& !workflowName.matches(workflowNameRegEx)) {
							continue;
						}
					}
                    List<Node> childNodes3 = getChildNodes(workflowNode);
                    for (Node serviceNode:childNodes3) {
                        if (nodeNameRegEx != null && !serviceNode.getName().matches(nodeNameRegEx)) {
                            continue;
                        }
                        Node ioNode = getOrAddNode(serviceNode, type);
                        if (ioNode.hasProperty(PROPERTY_WORKFLOW_IO_CONTENT)) {
							WorkflowServiceIOData workflowIOData = new WorkflowServiceIOData();
							workflowIOData.setExperimentId(experimentNode
									.getName());
							workflowIOData
									.setWorkflowId(workflowNode.getName());
							workflowIOData.setWorkflowName(workflowName);
							workflowIOData.setNodeId(serviceNode.getName());
							workflowIOData.setValue(ioNode.getProperty(
									PROPERTY_WORKFLOW_IO_CONTENT).getString());
							workflowIODataList.add(workflowIOData);
						}
                    }
                }
            }
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving workflow execution service data!!!",e);
        } finally {
            closeSession(session);
        }
        return workflowIODataList;
    }

    public boolean saveWorkflowExecutionStatus(String experimentId,WorkflowInstanceStatus status)throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionStatus(experimentId, status);
    	}
        Session session = null;
        boolean isSaved = true;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            workflowDataNode.setProperty(WORKFLOW_STATUS_PROPERTY,status.getExecutionStatus().name());
            Date time = status.getStatusUpdateTime();
            if (time==null){
            	time=Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
            }
            //TODO is saving the datetime following format ok?
			workflowDataNode.setProperty(WORKFLOW_STATUS_TIME_PROPERTY,time.getTime());
            session.save();
        } catch (Exception e) {
            isSaved = false;
            e.printStackTrace();
        } finally {
            closeSession(session);
        }
        return isSaved;
    }

    public WorkflowInstanceStatus getWorkflowExecutionStatus(String experimentId)throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionStatus(experimentId);
    	}
    	Session session = null;
    	WorkflowInstanceStatus property = null;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            ExecutionStatus status = null;
			if (workflowDataNode.hasProperty(WORKFLOW_STATUS_PROPERTY)) {
				status = ExecutionStatus.valueOf(workflowDataNode.getProperty(
						WORKFLOW_STATUS_PROPERTY).getString());
			}
            Date date = null;
			if (workflowDataNode.hasProperty(WORKFLOW_STATUS_TIME_PROPERTY)) {
				Property prop = workflowDataNode
						.getProperty(WORKFLOW_STATUS_TIME_PROPERTY);
				date = null;
				if (prop != null) {
					Long dateMiliseconds = prop.getLong();
					Calendar cal = Calendar.getInstance();
					cal.setTimeInMillis(dateMiliseconds);
					date = cal.getTime();
				}
			}
			property=new WorkflowInstanceStatus(new WorkflowInstance(experimentId,experimentId),status, date);
            session.save();
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving workflow execution status!!!", e);
        } finally {
            closeSession(session);
        }
        return property;
    }

	private Node getWorkflowExperimentDataNode(String experimentId,
			Session session) throws RepositoryException {
		return getOrAddNode(getOrAddNode(getWorkflowDataNode(session),
                experimentId),experimentId);
	}

	private Node getWorkflowDataNode(Session session)
			throws RepositoryException {
		return getOrAddNode(getRootNode(session), WORKFLOW_DATA);
	}
    
	public boolean saveWorkflowExecutionOutput(String experimentId,String outputNodeName,String output) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionOutput(experimentId, outputNodeName, output);
    	}
		Session session=null;
		try {
			session = getSession();
			Node resultNode = getWorkflowExperimentResultNode(experimentId,
					session);
			resultNode.setProperty(outputNodeName, output);
			session.save();
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}finally{
			closeSession(session);
		}
	    return true;
	}

    public WorkflowIOData getWorkflowExecutionOutput(String experimentId,String outputNodeName) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionOutput(experimentId, outputNodeName);
    	}
		Session session=null;
		try {
			session = getSession();
			Node resultNode = getWorkflowExperimentResultNode(experimentId,
					session);
			Property outputProperty = resultNode.getProperty(outputNodeName);
			if (outputProperty==null){
				return null;
			}
			return new WorkflowIOData(outputNodeName,outputProperty.getString());
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}finally{
			closeSession(session);
		}
    }

    public String[] getWorkflowExecutionOutputNames(String experimentId) throws RegistryException{
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionOutputNames(experimentId);
    	}
    	Session session=null;
    	List<String> outputNames=new ArrayList<String>();
		try {
			session = getSession();
			Node resultNode = getWorkflowExperimentResultNode(experimentId,
					session);
			PropertyIterator properties = resultNode.getProperties();
			for (;properties.hasNext();) {
				Property nextProperty = properties.nextProperty();
                if(!"jcr:primaryType".equals(nextProperty.getName())){
				    outputNames.add(nextProperty.getName());
                }
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}finally{
			closeSession(session);
		}
        return outputNames.toArray(new String[]{});
    }

	private Node getWorkflowExperimentResultNode(String experimentId,
			Session session) throws RepositoryException {
		Node workflowExperimentDataNode = getWorkflowExperimentDataNode(experimentId, session);
		Node resultNode = getOrAddNode(workflowExperimentDataNode,RESULT);
		return resultNode;
	}
    private List<String> getMatchingExperimentIds(String regex,Session session)throws RepositoryException{
        Node orAddNode = getWorkflowDataNode(session);
        List<String> matchList = new ArrayList<String>();
        
        Pattern compile = Pattern.compile(regex);
        List<Node> childNodes = getChildNodes(orAddNode);
        for (Node node:childNodes) {
            String name = node.getName();
            if(compile.matcher(name).find()){
                matchList.add(name);
            }
        }
        return matchList;
    }
    public Map<String, WorkflowInstanceStatus> getWorkflowExecutionStatusWithRegex(String regex) throws RegistryException {
        Session session=null;
        Map<String,WorkflowInstanceStatus> workflowStatusMap = new HashMap<String, WorkflowInstanceStatus>();
        try {
            session = getSession();
            List<String> matchingExperimentIds = getMatchingExperimentIds(regex, session);
            for(String experimentId:matchingExperimentIds){
                WorkflowInstanceStatus workflowStatus = getWorkflowExecutionStatus(experimentId);
                workflowStatusMap.put(experimentId,workflowStatus);
            }
		} catch (RepositoryException e) {
            e.printStackTrace();
            throw new RegistryException(e);
        }finally{
            closeSession(session);
        }
        return workflowStatusMap;
    }

    public Map<String, WorkflowIOData> getWorkflowExecutionOutputWithRegex(String regex, String outputName) throws RegistryException {
        Session session=null;
        Map<String,WorkflowIOData> workflowStatusMap = new HashMap<String, WorkflowIOData>();
        try {
            session = getSession();
            List<String> matchingExperimentIds = getMatchingExperimentIds(regex, session);
            for(String experimentId:matchingExperimentIds){
            	WorkflowIOData workflowOutputData = getWorkflowExecutionOutput(experimentId, outputName);
                workflowStatusMap.put(experimentId,workflowOutputData);
            }
		} catch (RepositoryException e) {
            e.printStackTrace();
            throw new RegistryException(e);
        }finally{
            closeSession(session);
        }
        return workflowStatusMap;
    }

    public Map<String, String[]> getWorkflowExecutionOutputNamesWithRegex(String regex) throws RegistryException {
        Session session = null;
      Map<String,String[]> workflowStatusMap = new HashMap<String, String[]>();
        try {
            session = getSession();
            List<String> matchingExperimentIds = getMatchingExperimentIds(regex, session);
            for(String experimentId:matchingExperimentIds){
                String[] workflowOutputData = getWorkflowExecutionOutputNames(experimentId);
                workflowStatusMap.put(experimentId,workflowOutputData);
            }
		} catch (RepositoryException e) {
            e.printStackTrace();
            throw new RegistryException(e);
        }finally{
            closeSession(session);
        }
        return workflowStatusMap;
    }

	public boolean saveWorkflowExecutionUser(String experimentId, String user)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionUser(experimentId, user);
    	}
		Session session = null;
        boolean isSaved = true;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            workflowDataNode.setProperty(WORKFLOW_USER_PROPERTY,user);
            session.save();
        } catch (Exception e) {
            isSaved = false;
            e.printStackTrace();
        } finally {
            closeSession(session);
        }
        return isSaved;
	}

	public boolean saveWorkflowExecutionOutput(String experimentId,WorkflowIOData data)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionOutput(experimentId, data);
    	}
		return saveWorkflowExecutionOutput(experimentId, data.getNodeId(), data.getValue());
	}

	public String getWorkflowExecutionUser(String experimentId)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionUser(experimentId);
    	}
		Session session = null;
        String property = null;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            if (workflowDataNode.hasProperty(WORKFLOW_USER_PROPERTY)) {
				property = workflowDataNode.getProperty(WORKFLOW_USER_PROPERTY)
						.getString();
			}
			session.save();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSession(session);
        }
        return property;
	}

	public WorkflowExecution getWorkflowExecution(String experimentId)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecution(experimentId);
    	}
		WorkflowExecution workflowExecution = new WorkflowExecutionImpl();
		workflowExecution.setExperimentId(experimentId);
		workflowExecution.setExecutionStatus(getWorkflowExecutionStatus(experimentId));
		workflowExecution.setUser(getWorkflowExecutionUser(experimentId));
		workflowExecution.setMetadata(getWorkflowExecutionMetadata(experimentId));
		workflowExecution.setWorkflowInstanceName(getWorkflowExecutionName(experimentId));
		workflowExecution.setOutput(getWorkflowExecutionOutput(experimentId));
		workflowExecution.setServiceInput(searchWorkflowExecutionServiceInput(experimentId,".*",".*"));
		workflowExecution.setServiceOutput(searchWorkflowExecutionServiceOutput(experimentId,".*",".*"));
		return workflowExecution;
	}

	public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionOutput(experimentId);
    	}
		List<WorkflowIOData> result=new ArrayList<WorkflowIOData>();
		String[] workflowExecutionOutputNames = getWorkflowExecutionOutputNames(experimentId);
		for (String workflowExecutionOutputName : workflowExecutionOutputNames) {
			result.add(getWorkflowExecutionOutput(experimentId, workflowExecutionOutputName));
		}
		return result;
	}

	public List<String> getWorkflowExecutionIdByUser(String user)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionIdByUser(user);
    	}
		Session session = null;
		List<String> ids=new ArrayList<String>();
		try {
			session = getSession();
			List<String> matchingExperimentIds = getMatchingExperimentIds(".*", session);
			Pattern compile = Pattern.compile(user==null? ".*":user);
			for (String id : matchingExperimentIds) {
				String instanceUser = getWorkflowExecutionUser(id);
				if (user==null || (instanceUser!=null && compile.matcher(instanceUser).find())){
					ids.add(id);
				}
			}
		} catch (RepositoryException e) {
			throw new RegistryException("Error in retrieving Execution Ids for the user '"+user+"'",e);
		}finally{
			closeSession(session);
		}
		return ids;
	}

	public List<WorkflowExecution> getWorkflowExecutionByUser(String user)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionByUser(user);
    	}
		return getWorkflowExecution(user,-1,-1);
	}

	private List<WorkflowExecution> getWorkflowExecution(String user, int startLimit, int endLimit)
			throws RegistryException {
		List<WorkflowExecution> executions=new ArrayList<WorkflowExecution>();
		List<String> workflowExecutionIdByUser = getWorkflowExecutionIdByUser(user);
		int count=0;
		for (String id : workflowExecutionIdByUser) {
			if ((startLimit==-1 && endLimit==-1) ||
				(startLimit==-1 && count<endLimit) ||
				(startLimit<=count && endLimit==-1) ||
				(startLimit<=count && count<endLimit)){
				executions.add(getWorkflowExecution(id));
			}
			count++;
		}
		return executions;
	}

	public List<WorkflowExecution> getWorkflowExecutionByUser(String user,
			int pageSize, int pageNo) throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionByUser(user, pageSize, pageNo);
    	}
		return getWorkflowExecutionByUser(user,pageSize*pageNo,pageSize*(pageNo+1));
	}

	public String getWorkflowExecutionMetadata(String experimentId)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionMetadata(experimentId);
    	}
		Session session = null;
    	String property = null;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            if (workflowDataNode.hasProperty(WORKFLOW_METADATA_PROPERTY)) {
				property = workflowDataNode.getProperty(
						WORKFLOW_METADATA_PROPERTY).getString();
			}
			session.save();
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving workflow metadata!!!", e);
        } finally {
            closeSession(session);
        }
        return property;
	}

	public boolean saveWorkflowExecutionMetadata(String experimentId,
			String metadata) throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionMetadata(experimentId, metadata);
    	}
		Session session = null;
        boolean isSaved = true;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            workflowDataNode.setProperty(WORKFLOW_METADATA_PROPERTY,metadata);
            session.save();
        } catch (Exception e) {
            isSaved = false;
            e.printStackTrace();
        } finally {
            closeSession(session);
        }
        return isSaved;
		
	}

	public boolean saveWorkflowExecutionStatus(String experimentId,
			ExecutionStatus status) throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionStatus(experimentId, status);
    	}
		return saveWorkflowExecutionStatus(experimentId,new WorkflowInstanceStatus(new WorkflowInstance(experimentId, experimentId), status));
	}
	@Override
	public boolean saveWorkflowExecutionName(String experimentId,
			String workflowIntanceName) throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionName(experimentId, workflowIntanceName);
    	}
		Session session = null;
        boolean isSaved = true;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            workflowDataNode.setProperty(WORKFLOW_INSTANCE_NAME_PROPERTY,workflowIntanceName);
            session.save();
        } catch (Exception e) {
            isSaved = false;
            e.printStackTrace();
        } finally {
            closeSession(session);
        }
        return isSaved;
	}

	@Override
	public String getWorkflowExecutionName(String experimentId)
			throws RegistryException {
    	if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionName(experimentId);
    	}
		Session session = null;
    	String property = null;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            if (workflowDataNode.hasProperty(WORKFLOW_INSTANCE_NAME_PROPERTY)) {
				property = workflowDataNode.getProperty(
						WORKFLOW_INSTANCE_NAME_PROPERTY).getString();
			}
			session.save();
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving workflow metadata!!!", e);
        } finally {
            closeSession(session);
        }
        return property;
	}	

	private void saveServiceURL(URI gfacURL, String nodeName)
			throws Exception {
		java.util.Date today = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        Timestamp timestamp = new Timestamp(today.getTime());
        Session session = null;
        try {
            URI uri = gfacURL;
            String propertyName = uri.getHost() + "-" + uri.getPort();
            session = getSession();
			Node gfacDataNode = getOrAddNode(getMetadataNode(session), nodeName);
            try {
                Property prop = gfacDataNode.getProperty(propertyName);
                prop.setValue(gfacURL + ";" + timestamp.getTime());
                session.save();
            } catch (PathNotFoundException e) {
                gfacDataNode.setProperty(propertyName, gfacURL + ";" + timestamp.getTime());
                session.save();
            }
//	            triggerObservers(this);
        } finally {
            closeSession(session);
        }
	}

	private void deleteServiceURL(URI gfacURL, String nodeName)
			throws Exception {
		Session session = null;
        try {
            URI uri = gfacURL;
            String propertyName = uri.getHost() + "-" + uri.getPort();
            session = getSession();
            Node gfacDataNode = getOrAddNode(getMetadataNode(session), nodeName);
            Property prop = gfacDataNode.getProperty(propertyName);
            if (prop != null) {
                prop.setValue((String) null);
                session.save();
//	                triggerObservers(this);
            }
        } finally {
            closeSession(session);
        }
	}

	private List<URI> getServiceURLList(String nodeName)
			throws RepositoryException {
		Session session = null;
        List<URI> urlList = new ArrayList<URI>();
        java.util.Date today = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        Timestamp timestamp = new Timestamp(today.getTime());
        try {
            session = getSession();
            Node gfacNode = getOrAddNode(getMetadataNode(session), nodeName);
            PropertyIterator propertyIterator = gfacNode.getProperties();
            while (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                if (!"nt:unstructured".equals(property.getString())) {
                    String x = property.getString();
                    Timestamp setTime = new Timestamp(new Long(property.getString().split(";")[1]));
                    if (GFAC_URL_UPDATE_INTERVAL > (timestamp.getTime() - setTime.getTime())) {
                        try {
							urlList.add(new URI(property.getString().split(";")[0]));
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
                    }
                }
            }
        }finally{
        	closeSession(session);
        }
        return urlList;
	}
	
	@Override
	public boolean saveInterpreterServiceURL(URI gfacURL)
			throws RegistryException {
        String nodeName = WORKFLOW_INTERPRETER_INSTANCE_DATA;
		try {
			saveServiceURL(gfacURL, nodeName);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving Interoreter Service URL to the registry!!!", e);
		}
        return true;
    }
	
	@Override
	public boolean deleteInterpreterServiceURL(URI gfacURL) throws RegistryException {
        String nodeName = WORKFLOW_INTERPRETER_INSTANCE_DATA;
		try {
			deleteServiceURL(gfacURL, nodeName);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while deleting Workflow Interpreter Service URL from registry!!!",e); 
		}
        return true;
    }


	@Override
	public List<URI> getInterpreterServiceURLList() throws RegistryException {
        String nodeName = WORKFLOW_INTERPRETER_INSTANCE_DATA;
        try {
			return getServiceURLList(nodeName);
        } catch (RepositoryException e) {
            throw new RegistryException("Error while retrieving Workflow Interpreter Service url list!!!", e);
		}
    }

	@Override
	public List<URI> getMessageBoxServiceURLList() throws RegistryException {
		String nodeName = MESSAGE_BOX_INSTANCE_DATA;
        try {
			return getServiceURLList(nodeName);
        } catch (RepositoryException e) {
            throw new RegistryException("Error while retrieving Message box Service url list!!!", e);
		}
	}

	@Override
	public boolean saveMessageBoxServiceURL(URI gfacURL)
			throws RegistryException {
		String nodeName = MESSAGE_BOX_INSTANCE_DATA;
		try {
			saveServiceURL(gfacURL, nodeName);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving Message box Service URL to the registry!!!", e);
		}
        return true;
	}

	@Override
	public boolean deleteMessageBoxServiceURL(URI gfacURL)
			throws RegistryException {
		String nodeName = MESSAGE_BOX_INSTANCE_DATA;
		try {
			deleteServiceURL(gfacURL, nodeName);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while deleting Message box Service URL from registry!!!",e); 
		}
        return true;
	}

	@Override
	public List<URI> getEventingServiceURLList() throws RegistryException {
		String nodeName = EVENTING_INSTANCE_DATA;
        try {
			return getServiceURLList(nodeName);
        } catch (RepositoryException e) {
            throw new RegistryException("Error while retrieving Eventing Service url list!!!", e);
		}
	}

	@Override
	public boolean saveEventingServiceURL(URI gfacURL) throws RegistryException {
		String nodeName = EVENTING_INSTANCE_DATA;
		try {
			saveServiceURL(gfacURL, nodeName);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving Eventing Service URL to the registry!!!", e);
		}
        return true;
	}

	@Override
	public boolean deleteEventingServiceURL(URI gfacURL)
			throws RegistryException {
		String nodeName = EVENTING_INSTANCE_DATA;
		try {
			deleteServiceURL(gfacURL, nodeName);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while deleting Eventing Service URL from registry!!!",e); 
		}
        return true;
	}

	private AiravataProvenanceRegistry getProvenanceRegistry() {
		return provenanceRegistry;
	}
	
    private static final String REPOSITORY_PROPERTIES = "repository.properties";

	@SuppressWarnings("unchecked")
	private void setupProvenanceRegistry(){
        Properties properties = new Properties();
		URL url = this.getClass().getClassLoader().getResource(REPOSITORY_PROPERTIES);
        if (url!=null) {
            try {
                properties.load(url.openStream());
                String provenanceClass = "class.provenance.registry.accessor";
                String provRegAccessorClass = properties.getProperty(provenanceClass, null);
                if (provRegAccessorClass != null) {
                    Class<AiravataProvenanceRegistry> provenanceRegistryClass = (Class<AiravataProvenanceRegistry>) getClass().getClassLoader().loadClass(provRegAccessorClass);
                    provenanceRegistry = provenanceRegistryClass.getConstructor(String.class).newInstance(getUsername());
                }
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
        
	}
}
