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

import java.net.URI;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
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
import org.apache.airavata.registry.api.Axis2Registry;
import org.apache.airavata.registry.api.DataRegistry;
import org.apache.airavata.registry.api.exception.DeploymentDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;
import org.apache.airavata.registry.api.workflow.WorkflowIOData;
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
    private static final String DEPLOY_NODE_NAME = "APP_HOST";
    private static final String HOST_NODE_NAME = "GFAC_HOST";
    private static final String XML_PROPERTY_NAME = "XML";
    private static final String WSDL_PROPERTY_NAME = "WSDL";
    private static final String GFAC_URL_PROPERTY_NAME = "GFAC_URL_LIST";
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

    private static Logger log = LoggerFactory.getLogger(AiravataJCRRegistry.class);

    public AiravataJCRRegistry(URI repositoryURI, String className,
			String user, String pass, Map<String, String> map)
			throws RepositoryException {
		super(repositoryURI, className, user, pass, map);
	}
    
    private Node getServiceNode(Session session) throws RepositoryException {
        return getOrAddNode(session.getRootNode(), SERVICE_NODE_NAME);
    }

    private Node getDeploymentNode(Session session) throws RepositoryException {
        return getOrAddNode(session.getRootNode(), DEPLOY_NODE_NAME);
    }

    private Node getHostNode(Session session) throws RepositoryException {
        return getOrAddNode(session.getRootNode(), HOST_NODE_NAME);
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
                session.save();
                triggerObservers(this);
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
            NodeIterator nodes = hostNode.getNodes();
            for (; nodes.hasNext();) {
                Node app = nodes.nextNode();
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
                triggerObservers(this);
            }
        } catch (Exception e) {
            throw new HostDescriptionRetrieveException(e);
        } finally {
            closeSession(session);
        }
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
            triggerObservers(this);
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
            triggerObservers(this);
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
            triggerObservers(this);
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
            NodeIterator nodes = node.getNodes();
            for (; nodes.hasNext();) {
                Node service = nodes.nextNode();
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
            NodeIterator nodes = node.getNodes();
            for (; nodes.hasNext();) {
                Node host = nodes.nextNode();
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
            NodeIterator serviceNodes = deploymentNode.getNodes();

            for (; serviceNodes.hasNext();) {
                Node serviceNode = serviceNodes.nextNode();
                NodeIterator hostNodes = serviceNode.getNodes();

                for (; hostNodes.hasNext();) {
                    Node hostNode = hostNodes.nextNode();
                    NodeIterator nodes = hostNode.getNodes();
                    for (; nodes.hasNext();) {
                        Node app = nodes.nextNode();
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
            NodeIterator nodes = hostNode.getNodes();
            boolean found = false;
            for (; nodes.hasNext();) {
                Node app = nodes.nextNode();
                Property prop = app.getProperty(XML_PROPERTY_NAME);
                ApplicationDeploymentDescription appDesc = ApplicationDeploymentDescription.fromXML(prop.getString());
                if (appDesc.getType().getApplicationName().getStringValue().matches(applicationName)) {
                    app.remove();
                    found = true;
                }
            }
            if (found) {
                session.save();
                triggerObservers(this);
            }
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
        try {
            session = getSession();
            Node deploymentNode = getDeploymentNode(session);
            Node serviceNode = deploymentNode.getNode(serviceName);
            Node hostNode = serviceNode.getNode(hostName);
            NodeIterator nodes = hostNode.getNodes();
            for (; nodes.hasNext();) {
                Node app = nodes.nextNode();
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
			NodeIterator hostNodes = serviceNode.getNodes();
			for(;hostNodes.hasNext();){
				Node hostNode = hostNodes.nextNode();
				HostDescription hostDescriptor = getHostDescription(hostNode.getName());
				result.put(hostDescriptor, new ArrayList<ApplicationDeploymentDescription>());
				NodeIterator nodes = hostNode.getNodes();
				for (; nodes.hasNext();) {
					Node app = nodes.nextNode();
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
        try {
            session = getSession();
            Node deploymentNode = getDeploymentNode(session);
            Node serviceNode = deploymentNode.getNode(serviceName);
            Node hostNode = serviceNode.getNode(hostName);
            NodeIterator nodes = hostNode.getNodes();
            for (; nodes.hasNext();) {
                Node app = nodes.nextNode();
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
            
            PortTypeType portType = service.getType().addNewPortType();
            MethodType methodType = portType.addNewMethod();
            
            methodType.setMethodName("invoke");
            
            WSDLGenerator generator = new WSDLGenerator();
            Hashtable table = generator.generateWSDL(null, null, null, service.getType(), true);            
            return (String) table.get(WSDLConstants.AWSDL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean saveGFacDescriptor(String gfacURL) throws RegistryException{
        java.util.Date today = Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
        Timestamp timestamp = new Timestamp(today.getTime());
        Session session = null;
        try {
            URI uri = new URI(gfacURL);
            String propertyName = uri.getHost() + "-" + uri.getPort();
            session = getSession();
            Node gfacDataNode = getOrAddNode(session.getRootNode(), GFAC_INSTANCE_DATA);
            try {
                Property prop = gfacDataNode.getProperty(propertyName);
                prop.setValue(gfacURL + ";" + timestamp.getTime());
                session.save();
            } catch (PathNotFoundException e) {
                gfacDataNode.setProperty(propertyName, gfacURL + ";" + timestamp.getTime());
                session.save();
            }
            triggerObservers(this);
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
            Node gfacDataNode = getOrAddNode(session.getRootNode(), GFAC_INSTANCE_DATA);
            Property prop = gfacDataNode.getProperty(propertyName);
            if (prop != null) {
                prop.setValue((String) null);
                session.save();
                triggerObservers(this);
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
            Node gfacNode = getOrAddNode(session.getRootNode(), GFAC_INSTANCE_DATA);
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
        Session session = null;
        String result = null;
        try {
            session = getSession();
            Node outputNode = getOrAddNode(session.getRootNode(), OUTPUT_NODE_NAME);
            Node node = getOrAddNode(outputNode, workflowId);
            for (int i = 0; i < parameters.size(); i++) {
                node.setProperty(String.valueOf(i), parameters.get(i).toXML());
            }

            session.save();

            result = node.getIdentifier();
            triggerObservers(this);
        } catch (Exception e) {
            System.out.println(e);
            throw new RegistryException("Error while saving workflow output to the registry!!!", e);
        } finally {
            closeSession(session);
        }
        return result;
    }

    public List<ActualParameter> loadOutput(String workflowId) throws RegistryException{
        Session session = null;
        ArrayList<ActualParameter> result = new ArrayList<ActualParameter>();
        try {
            session = getSession();
            Node outputNode = getOrAddNode(session.getRootNode(), OUTPUT_NODE_NAME);
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
            Node workflowListNode = getOrAddNode(getOrAddNode(session.getRootNode(), WORKFLOWS), PUBLIC);
            NodeIterator iterator = workflowListNode.getNodes();
            while (iterator.hasNext()) {
                Node nextNode = iterator.nextNode();
                workflowList.put(new QName(nextNode.getName()), nextNode);
            }
            workflowListNode = getOrAddNode(getOrAddNode(session.getRootNode(), WORKFLOWS), userName);
            iterator = workflowListNode.getNodes();
            while (iterator.hasNext()) {
                Node nextNode = iterator.nextNode();
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
            Node workflowListNode = getOrAddNode(getOrAddNode(session.getRootNode(), WORKFLOWS), userName);
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
            Node workflowListNode = getOrAddNode(session.getRootNode(), WORKFLOWS);
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
            triggerObservers(this);
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
            Node workflowListNode = getOrAddNode(getOrAddNode(session.getRootNode(), WORKFLOWS), userName);
            Node result = getOrAddNode(workflowListNode, resourceID.getLocalPart());
            if (result != null) {
                result.remove();
                session.save();
                triggerObservers(this);
            }
        } catch (Exception e) {
            throw new RegistryException("Error while deleting workflow from registry!!!", e);
        } finally {
            closeSession(session);
        }
        return false;
    }

    public boolean saveWorkflowExecutionServiceInput(WorkflowIOData workflowInputData) throws RegistryException{
        return saveWorkflowIO(workflowInputData, INPUT);
    }

    public boolean saveWorkflowExecutionServiceOutput(WorkflowIOData workflowOutputData) throws RegistryException{
        return saveWorkflowIO(workflowOutputData, OUTPUT);
    }


    private boolean saveWorkflowIO(WorkflowIOData workflowOutputData, String type) throws RegistryException{
        Session session = null;
        boolean isSaved = true;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(workflowOutputData.getExperimentId(),session);
            workflowDataNode.setProperty(PROPERTY_WORKFLOW_NAME, workflowOutputData.getWorkflowName());
            workflowDataNode = getOrAddNode(getOrAddNode(workflowDataNode, workflowOutputData.getNodeId()), type);
            workflowDataNode.setProperty(PROPERTY_WORKFLOW_IO_CONTENT, workflowOutputData.getData());
            session.save();
        } catch (Exception e) {
            isSaved = false;
            throw new RegistryException("Error while saving workflow execution service data!!!", e);
        } finally {
            closeSession(session);
        }
        return isSaved;
    }

    public List<WorkflowIOData> searchWorkflowExecutionServiceInput(String experimentIdRegEx, String workflowNameRegEx,
            String nodeNameRegEx) throws RegistryException{
        return searchWorkflowIO(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx, INPUT);
    }

    public List<WorkflowIOData> searchWorkflowExecutionServiceOutput(String experimentIdRegEx, String workflowNameRegEx,
            String nodeNameRegEx) throws RegistryException{
        return searchWorkflowIO(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx, OUTPUT);
    }

    private List<WorkflowIOData> searchWorkflowIO(String experimentIdRegEx, String workflowNameRegEx,
            String nodeNameRegEx, String type) throws RegistryException{
        List<WorkflowIOData> workflowIODataList = new ArrayList<WorkflowIOData>();
        Session session = null;
        try {
            session = getSession();
            Node experimentsNode = getOrAddNode(session.getRootNode(), WORKFLOW_DATA);
            NodeIterator experimentNodes = experimentsNode.getNodes();
            for (; experimentNodes.hasNext();) {
                Node experimentNode = experimentNodes.nextNode();
                if (experimentIdRegEx != null && !experimentNode.getName().matches(experimentIdRegEx)) {
                    continue;
                }
                NodeIterator workflowNodes = experimentNode.getNodes();
                for (; workflowNodes.hasNext();) {
                    Node workflowNode = workflowNodes.nextNode();
                    String workflowName = workflowNode.getProperty(PROPERTY_WORKFLOW_NAME).getString();
                    if (workflowNameRegEx != null && !workflowName.matches(workflowNameRegEx)) {
                        continue;
                    }
                    NodeIterator serviceNodes = workflowNode.getNodes();
                    for (; serviceNodes.hasNext();) {
                        Node serviceNode = serviceNodes.nextNode();
                        if (nodeNameRegEx != null && !serviceNode.getName().matches(nodeNameRegEx)) {
                            continue;
                        }
                        Node ioNode = getOrAddNode(serviceNode, type);
                        WorkflowIOData workflowIOData = new WorkflowIOData();
                        workflowIOData.setExperimentId(experimentNode.getName());
                        workflowIOData.setWorkflowId(workflowNode.getName());
                        workflowIOData.setWorkflowName(workflowName);
                        workflowIOData.setNodeId(serviceNode.getName());
                        workflowIOData.setData(ioNode.getProperty(PROPERTY_WORKFLOW_IO_CONTENT).getString());
                        workflowIODataList.add(workflowIOData);
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

    public boolean saveWorkflowExecutionStatus(String experimentId,String status)throws RegistryException{
        Session session = null;
        boolean isSaved = true;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            workflowDataNode.setProperty(WORKFLOW_STATUS_PROPERTY,status);
            session.save();
        } catch (Exception e) {
            isSaved = false;
            e.printStackTrace();
        } finally {
            closeSession(session);
        }
        return isSaved;
    }

    public String getWorkflowExecutionStatus(String experimentId)throws RegistryException{
       Session session = null;
        String property = null;
        try {
            session = getSession();
            Node workflowDataNode = getWorkflowExperimentDataNode(experimentId, session);
            property = workflowDataNode.getProperty(WORKFLOW_STATUS_PROPERTY).getString();
            session.save();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeSession(session);
        }
        return property;
    }

	private Node getWorkflowExperimentDataNode(String experimentId,
			Session session) throws RepositoryException {
		return getOrAddNode(getOrAddNode(getOrAddNode(session.getRootNode(), WORKFLOW_DATA),
		                experimentId),experimentId);
	}
    
	public boolean saveWorkflowExecutionOutput(String experimentId,String outputNodeName,String output) throws RegistryException{
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

    public String getWorkflowExecutionOutput(String experimentId,String outputNodeName) throws RegistryException{
		Session session=null;
		try {
			session = getSession();
			Node resultNode = getWorkflowExperimentResultNode(experimentId,
					session);
			Property outputProperty = resultNode.getProperty(outputNodeName);
			if (outputProperty==null){
				return null;
			}
			return outputProperty.getString();
		} catch (RepositoryException e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}finally{
			closeSession(session);
		}
    }

    public String[] getWorkflowExecutionOutputNames(String experimentId) throws RegistryException{
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
        Node orAddNode = getOrAddNode(session.getRootNode(), WORKFLOW_DATA);
        List<String> matchList = new ArrayList<String>();
        NodeIterator nodes = orAddNode.getNodes();
        Pattern compile = Pattern.compile(regex);
        while(nodes.hasNext()){
            Node node = nodes.nextNode();
            String name = node.getName();
            if(compile.matcher(name).find()){
                matchList.add(name);
            }
        }
        return matchList;
    }
    public Map<String, String> getWorkflowExecutionStatusWithRegex(String regex) throws RegistryException {
        Session session=null;
        Map<String,String> workflowStatusMap = new HashMap<String, String>();
        try {
            session = getSession();
            List<String> matchingExperimentIds = getMatchingExperimentIds(regex, session);
            for(String experimentId:matchingExperimentIds){
                String workflowStatus = getWorkflowExecutionStatus(experimentId);
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

    public Map<String, String> getWorkflowExecutionOutputWithRegex(String regex, String outputName) throws RegistryException {
        Session session=null;
        Map<String,String> workflowStatusMap = new HashMap<String, String>();
        try {
            session = getSession();
            List<String> matchingExperimentIds = getMatchingExperimentIds(regex, session);
            for(String experimentId:matchingExperimentIds){
                String workflowOutputData = getWorkflowExecutionOutput(experimentId,outputName);
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
		return false;
	}
}
