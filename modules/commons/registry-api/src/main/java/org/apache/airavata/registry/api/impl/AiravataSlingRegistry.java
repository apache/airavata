package org.apache.airavata.registry.api.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Pattern;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.registry.api.impl.SlingRegistry;
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
import org.apache.airavata.registry.api.workflow.*;
import org.apache.airavata.registry.api.workflow.WorkflowInstanceStatus.ExecutionStatus;
import org.apache.airavata.schemas.gfac.MethodType;
import org.apache.airavata.schemas.gfac.PortTypeType;
import org.apache.airavata.schemas.gfac.ServiceType;
import org.apache.airavata.schemas.gfac.ServiceType.ServiceName;
import org.json.JSONArray;
import org.json.JSONObject;

public class AiravataSlingRegistry extends SlingRegistry implements
		Axis2Registry, DataRegistry {

	private static final String HOST_NODE_NAME = "GFAC_HOST";
	private static final String SERVICE_NODE_NAME = "SERVICE_HOST";
	private static final String DEPLOY_NODE_NAME = "APP_HOST";
	private static final String XML_PROPERTY_NAME = "XML";

	private static final String AIRAVATA_CONFIG_NODE = "AIRAVATA_CONFIGURATION_DATA";
	private static final String OUTPUT_NODE_NAME = "OUTPUTS";
	private static final String GFAC_INSTANCE_DATA = "GFAC_INSTANCE_DATA";
	private static final String WORKFLOW_INTERPRETER_INSTANCE_DATA = "WORKFLOW_INTERPRETER_INSTANCE_DATA";
	private static final String MESSAGE_BOX_INSTANCE_DATA = "MESSAGE_BOX_INSTANCE_DATA";
	private static final String EVENTING_INSTANCE_DATA = "EVENTING_INSTANCE_DATA";
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
	public static final String WORKFLOW_USER_PROPERTY = "User";
	public static final String NOTIFICATION_STORE = "User";
    public static final String WORKFLOW_INSTANCE_NAME_PROPERTY = "Worflow_instace_name";
    
	private AiravataProvenanceRegistry provenanceRegistry;	
    private static final String REPOSITORY_PROPERTIES = "repository.properties";

	public AiravataSlingRegistry(URI uri, String username, String password)
			throws RepositoryException {
		super(uri, username, password);
		setupProvenanceRegistry();
	}

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
	
	private AiravataProvenanceRegistry getProvenanceRegistry() {
		return provenanceRegistry;
	}


	@Override
	public String saveServiceDescription(ServiceDescription service)
			throws RegistryException {
		String result = null;
		try {
			String data = XML_PROPERTY_NAME + "=" + service.toXML();
			result = createOrUpdateNode(SERVICE_NODE_NAME + "/"
					+ service.getType().getName(), data);
		} catch (Exception e) {
			System.out.println(e);
			throw new RegistryException(
					"Error while saving service description!", e);
		}
		return result;
	}
	
	@Override
	public ServiceDescription getServiceDescription(String serviceId)
			throws ServiceDescriptionRetrieveException {

		ServiceDescription result = null;
		try {
			JSONObject json = getNodeAsJson(SERVICE_NODE_NAME + "/" + serviceId);
			String xml = json.get(XML_PROPERTY_NAME).toString();
			result = ServiceDescription.fromXML(xml);
		} catch (FileNotFoundException ex) {
			try {
				createOrUpdateNode(SERVICE_NODE_NAME, null);
			} catch (Exception e) {
				// do nothing
			}
			return null;
		} catch (Exception ex) {
			throw new ServiceDescriptionRetrieveException(ex);
		}
		return result;
	}

	@Override
	public List<ServiceDescription> searchServiceDescription(String nameRegEx)
			throws RegistryException {
		ArrayList<ServiceDescription> result = new ArrayList<ServiceDescription>();
		try {
			JSONObject json = getChildNodesAsJson(SERVICE_NODE_NAME, "1");
			String[] fields = JSONObject.getNames(json);
			JSONObject jsonService;
			String prop;
			for (String service : fields) {
				if (nameRegEx.equals("") || service.matches(nameRegEx)) {
					jsonService = json.optJSONObject(service);
					if (jsonService == null) {
						continue;
					}
					prop = jsonService.optString(XML_PROPERTY_NAME);
					if (prop.isEmpty()) {
						continue;
					}
					result.add(ServiceDescription.fromXML(prop));
				}
			}
		} catch (FileNotFoundException e) {
			try {
				createOrUpdateNode(SERVICE_NODE_NAME, null);
			} catch (Exception ex) {
				throw new RegistryException(
						"Error while searching service description!", ex);
			}

		} catch (Exception e) {
			throw new RegistryException(
					"Error while searching service description!", e);
		}
		return result;
	}

	@Override
	public void deleteServiceDescription(String serviceId)
			throws RegistryException {
		try {
			deleteNode(SERVICE_NODE_NAME + "/" + serviceId);
			deleteNode(DEPLOY_NODE_NAME + "/" + serviceId);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while deleting service description!", e);
		}
	}

	@Override
	public String saveHostDescription(HostDescription host)
			throws RegistryException {
		String result = null;
		try {
			String data = XML_PROPERTY_NAME + "=" + host.toXML();
			result = createOrUpdateNode(HOST_NODE_NAME + "/"
					+ host.getType().getHostName(), data);
		} catch (Exception e) {
			System.out.println(e);
			throw new RegistryException("Error while saving host description!",
					e);
		}
		return result;
	}

	@Override
	public HostDescription getHostDescription(String hostId)
			throws RegistryException {
		HostDescription result = null;
		try {
			JSONObject json = getNodeAsJson(HOST_NODE_NAME + "/" + hostId);
			String xml = json.get(XML_PROPERTY_NAME).toString();
			result = HostDescription.fromXML(xml);
		} catch (FileNotFoundException ex) {
			try {
				createOrUpdateNode(HOST_NODE_NAME, null);
			} catch (Exception e) {
				// Do nothing
			}
			return null;
		} catch (Exception ex) {
			throw new HostDescriptionRetrieveException(ex);
		}
		return result;
	}

	@Override
	public List<HostDescription> searchHostDescription(String nameRegEx)
			throws RegistryException {
		ArrayList<HostDescription> result = new ArrayList<HostDescription>();
		try {
			JSONObject json = getChildNodesAsJson(HOST_NODE_NAME, "1");
			String[] fields = JSONObject.getNames(json);
			JSONObject jsonHost;
			String prop;
			for (String host : fields) {
				if (host.matches(nameRegEx)) {
					jsonHost = json.optJSONObject(host);
					if (jsonHost == null) {
						continue;
					}
					prop = jsonHost.optString(XML_PROPERTY_NAME);
					if (prop.isEmpty()) {
						continue;
					}
					result.add(HostDescription.fromXML(prop));
				}
			}
		} catch (FileNotFoundException e) {
			try {
				createOrUpdateNode(HOST_NODE_NAME, null);
			} catch (Exception ex) {
				throw new RegistryException(
						"Error while searching host description!", ex);
			}
		} catch (Exception e) {
			throw new RegistryException(
					"Error while searching host description!", e);
		}
		return result;
	}

	@Override
	public void deleteHostDescription(String hostId) throws RegistryException {
		try {
			deleteNode(HOST_NODE_NAME + "/" + hostId);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while deleting host description!", e);
		}
	}
	
	@Override
	public String saveDeploymentDescription(String serviceId, String hostId,
			ApplicationDeploymentDescription app) throws RegistryException {
		String result = null;
		try {
			String data = XML_PROPERTY_NAME + "=" + app.toXML();
			result = createOrUpdateNode(DEPLOY_NODE_NAME + "/" + serviceId
					+ "/" + hostId + "/"
					+ app.getType().getApplicationName().getStringValue(), data);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while saving application delployment description!",
					e);
		}
		return result;
	}

	@Override
	public ApplicationDeploymentDescription getDeploymentDescription(
			String serviceId, String hostId) throws RegistryException {
		ApplicationDeploymentDescription result = null;
		try {
			JSONObject json = getChildNodesAsJson(DEPLOY_NODE_NAME + "/"
					+ serviceId + "/" + hostId, "1");
			String[] fields = JSONObject.getNames(json);
			JSONObject jsonApp = null;
			String xml = null;
			for (String app : fields) {
				jsonApp = json.optJSONObject(app);
				if (jsonApp == null) {
					continue;
				}
				xml = jsonApp.optString(XML_PROPERTY_NAME);
				if (xml.isEmpty()) {
					continue;
				}
				result = ApplicationDeploymentDescription.fromXML(xml);
				break;
			}
		} catch (FileNotFoundException ex) {
			try {
				createOrUpdateNode(DEPLOY_NODE_NAME, null);
			} catch (Exception e) {
				// Do nothing
			}
			return null;
		} catch (Exception ex) {
			throw new DeploymentDescriptionRetrieveException(ex);
		}
		return result;
	}

	@Override
	public List<ApplicationDeploymentDescription> searchDeploymentDescription(
			String serviceName, String hostName) throws RegistryException {
		ArrayList<ApplicationDeploymentDescription> result = new ArrayList<ApplicationDeploymentDescription>();
		if (serviceName == null || hostName == null) {
			return result;
		}
		try {
			JSONObject jsonApps = getChildNodesAsJson(DEPLOY_NODE_NAME + "/"
					+ serviceName + "/" + hostName, "1");
			String[] fields = JSONObject.getNames(jsonApps);
			JSONObject jsonApp = null;
			String xml;
			for (String appName : fields) {
				jsonApp = jsonApps.optJSONObject(appName);
				if (jsonApp == null) {
					continue;
				}
				xml = jsonApp.optString(XML_PROPERTY_NAME);
				if (xml.isEmpty()) {
					continue;
				}
				result.add(ApplicationDeploymentDescription.fromXML(xml));
			}
		} catch (FileNotFoundException e) {
			try {
				createOrUpdateNode(DEPLOY_NODE_NAME, null);
				return result;
			} catch (Exception ex) {
				throw new RegistryException(
						"Error while searching application deployment description!",
						ex);
			}
		} catch (Exception ex) {
			throw new RegistryException(
					"Error while searching application deployment description!",
					ex);
		}
		return result;
	}

	@Override
	public Map<HostDescription, List<ApplicationDeploymentDescription>> searchDeploymentDescription(
			String serviceName) throws RegistryException {
		Map<HostDescription, List<ApplicationDeploymentDescription>> map = new HashMap<HostDescription, List<ApplicationDeploymentDescription>>();
		try {
			JSONObject hostNodes = getChildNodesAsJson(DEPLOY_NODE_NAME + "/"
					+ serviceName, "2");
			String[] hostNames = JSONObject.getNames(hostNodes);
			List<ApplicationDeploymentDescription> result = null;
			HostDescription hostDesc = null;
			for (String hostName : hostNames) {
				try {
					hostDesc = getHostDescription(hostName);
					result = searchDeploymentDescription(serviceName, hostName);
					map.put(hostDesc, result);
				} catch (Exception ex) {
					continue;
				}
			}
		} catch (FileNotFoundException e) {
			try {
				createOrUpdateNode(DEPLOY_NODE_NAME, null);
			} catch (Exception ex) {
				throw new RegistryException(
						"Error while searching application deployment description!",
						ex);
			}
		} catch (Exception ex) {
			throw new RegistryException(
					"Error while searching application deployment description!",
					ex);
		}
		return map;
	}

	@Override
	public List<ApplicationDeploymentDescription> searchDeploymentDescription(
			String serviceName, String hostName, String applicationName)
			throws RegistryException {
		ArrayList<ApplicationDeploymentDescription> result = new ArrayList<ApplicationDeploymentDescription>();
		if (serviceName == null || hostName == null) {
			return result;
		}
		try {
			JSONObject jsonApps = getChildNodesAsJson(DEPLOY_NODE_NAME + "/"
					+ serviceName + "/" + hostName, "1");
			String[] fields = JSONObject.getNames(jsonApps);
			JSONObject jsonApp = null;
			String xml = null;
			for (String appName : fields) {
				if (appName.equalsIgnoreCase(applicationName)) {
					jsonApp = jsonApps.optJSONObject(appName);
					if (jsonApp == null) {
						continue;
					}
					xml = jsonApp.optString(XML_PROPERTY_NAME);
					if (xml.isEmpty()) {
						continue;
					}
					result.add(ApplicationDeploymentDescription.fromXML(xml));
				}
			}
		} catch (FileNotFoundException e) {
			try {
				createOrUpdateNode(DEPLOY_NODE_NAME, null);
				return result;
			} catch (Exception ex) {
				throw new RegistryException(
						"Error while searching application deployment description!",
						ex);
			}
		} catch (Exception ex) {
			throw new RegistryException(
					"Error while searching application deployment description!",
					ex);
		}
		return result;
	}

	@Override
	public Map<ApplicationDeploymentDescription, String> searchDeploymentDescription()
			throws RegistryException {
		Map<ApplicationDeploymentDescription, String> map = new HashMap<ApplicationDeploymentDescription, String>();
		try {
			JSONObject serviceNodes = getChildNodesAsJson(DEPLOY_NODE_NAME, "3");
			String[] serviceNames = JSONObject.getNames(serviceNodes);

			JSONObject jsonService = null;
			String[] hostNames = null;
			for (String serviceName : serviceNames) {
				try {
					jsonService = new JSONObject(
							serviceNodes.getString(serviceName));
					hostNames = JSONObject.getNames(jsonService);

					List<ApplicationDeploymentDescription> appList = null;
					String value = null;
					for (String hostName : hostNames) {
						try {
							value = serviceName + "$" + hostName;
							appList = searchDeploymentDescription(serviceName,
									hostName);
							for (ApplicationDeploymentDescription app : appList) {
								map.put(app, value);
							}
						} catch (Exception ex) {
							continue;
						}
					}
				} catch (Exception ex) {
					continue;
				}
			}
		} catch (FileNotFoundException e) {
			try {
				createOrUpdateNode(DEPLOY_NODE_NAME, null);
			} catch (Exception ex) {
				throw new RegistryException(
						"Error while searching application deployment description!",
						ex);
			}
		} catch (Exception ex) {
			throw new RegistryException(
					"Error while searching application deployment description!",
					ex);
		}
		return map;
	}

	@Override
	public void deleteDeploymentDescription(String serviceName,
			String hostName, String applicationName) throws RegistryException {
		try {
			deleteNode(DEPLOY_NODE_NAME + "/" + serviceName + "/" + hostName
					+ "/" + applicationName);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while deleting application deployment description!",
					e);
		}
	}

	@Override
	public boolean deployServiceOnHost(String serviceName, String hostName)
			throws RegistryException {
		try {
			String hostUuid = null;
			if (!isPropertyAvailable(SERVICE_NODE_NAME + "/" + serviceName,
					LINK_NAME)) {
				hostUuid = getUuidOfNode(HOST_NODE_NAME + "/" + hostName);
				String data = LINK_NAME + "=" + hostUuid + "&" + LINK_NAME
						+ "@TypeHint=String[]";
				writePropertiesToNode(SERVICE_NODE_NAME + "/" + serviceName,
						data);
			} else {
				JSONObject node = getNodeAsJson(SERVICE_NODE_NAME + "/"
						+ serviceName);
				JSONArray values = node.getJSONArray(LINK_NAME);
				String value = null;
				ArrayList<String> list = new ArrayList<String>();
				for (int i = 0; i < values.length(); i++) {
					value = (String) values.get(i);
					if (value.equalsIgnoreCase(hostUuid)) {
						return false;
					}
					list.add(value);
				}

				list.add(hostUuid);
				String[] ss = list.toArray(new String[list.size()]);
				String val = "";
				for (String s : ss) {
					val = val + LINK_NAME + "=" + s + "&";
				}
				val = val + LINK_NAME + "@TypeHint=String[]";
				writePropertiesToNode(SERVICE_NODE_NAME + "/" + serviceName,
						val);
			}
			return true;
		} catch (Exception e) {
			throw new RegistryException("Error while saving service on host!",
					e);
		}
	}

	@Override
	public boolean saveGFacDescriptor(String gfacURL) throws RegistryException {
		java.util.Date today = Calendar
				.getInstance(TimeZone.getTimeZone("GMT")).getTime();
		Timestamp timestamp = new Timestamp(today.getTime());
		try {
			URI uri = new URI(gfacURL);
			createOrUpdateNode(getMetadataNodeURL(), null);
			String propertyName = uri.getHost() + "-" + uri.getPort();
			String propertyValue = gfacURL + ";" + timestamp.getTime();
			String data = propertyName + "=" + propertyValue;
			createOrUpdateNode(getMetadataNodeURL() + "/" + GFAC_INSTANCE_DATA,
					data);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while saving GFac Descriptor to the registry!", e);
		}
		return true;
	}

	@Override
	public boolean deleteGFacDescriptor(String gfacURL)
			throws RegistryException {
		try {
			URI uri = new URI(gfacURL);
			String propertyName = uri.getHost() + "-" + uri.getPort();
			createOrUpdateNode(getMetadataNodeURL(), null);
			createOrUpdateNode(getMetadataNodeURL() + "/" + GFAC_INSTANCE_DATA,
					null);
			removePropertyFromNode(getMetadataNodeURL() + "/"
					+ GFAC_INSTANCE_DATA, propertyName);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while deleting GFac Description from registry!", e);
		}
		return true;
	}

	private String getMetadataNodeURL() throws Exception {
		createOrUpdateNode(AIRAVATA_CONFIG_NODE, null);
		return AIRAVATA_CONFIG_NODE;
	}

	@Override
	public List<String> getGFacDescriptorList() throws RegistryException {
		List<String> urlList = new ArrayList<String>();
		java.util.Date today = Calendar
				.getInstance(TimeZone.getTimeZone("GMT")).getTime();
		Timestamp timestamp = new Timestamp(today.getTime());
		try {
			createOrUpdateNode(getMetadataNodeURL(), null);
			createOrUpdateNode(getMetadataNodeURL() + "/" + GFAC_INSTANCE_DATA,
					null);

			JSONObject jsonProps = getNodeAsJson(getMetadataNodeURL() + "/"
					+ GFAC_INSTANCE_DATA);
			String[] fields = JSONObject.getNames(jsonProps);
			String propertyValue = null;
			Timestamp setTime = null;
			for (String field : fields) {
				propertyValue = jsonProps.getString(field);
				if (propertyValue.contains(";")) {
					setTime = new Timestamp(new Long(
							propertyValue.split(";")[1]));
					//if (GFAC_URL_UPDATE_INTERVAL > (timestamp.getTime() - setTime.getTime())) {
						urlList.add(propertyValue.split(";")[0]);
					//}
				}
			}
		} catch (Exception e) {
			throw new RegistryException(
					"Error while retrieving GFac Descriptor list!", e);
		}
		Collections.reverse(urlList);
		return urlList;
	}

	@Override
	public boolean saveWorkflow(QName ResourceID, String workflowName,
			String resourceDesc, String workflowAsaString, String owner,
			boolean isMakePublic) throws RegistryException {
		try {
			createOrUpdateNode(WORKFLOWS, null);
			String data = "workflow=" + workflowAsaString + "&Prefix="
					+ ResourceID.getPrefix() + "&LocalPart="
					+ ResourceID.getLocalPart() + "&NamespaceURI="
					+ ResourceID.getNamespaceURI() + "&public=" + isMakePublic
					+ "&public@TypeHint=Boolean" + "&Description="
					+ resourceDesc + "&Type=" + REGISTRY_TYPE_WORKFLOW;
			data = data.replace("&lt;", "<");
			System.out.println(data);
			if (isMakePublic) {
				createOrUpdateNode(WORKFLOWS + "/" + PUBLIC + "/"
						+ workflowName, data);
			} else {
				createOrUpdateNode(
						WORKFLOWS + "/" + owner + "/" + workflowName, "a=b&lt;h j=3&d=5");
			}
		} catch (Exception e) {
			throw new RegistryException(
					"Error while saving workflow to the registry!", e);
		}
		return true;
	}

	@Override
	public Map<QName, Node> getWorkflows(String userName)
			throws RegistryException {
		Map<QName, Node> workflowList = new HashMap<QName, Node>();
		/*
		 * try { createNode(WORKFLOWS + "/" + PUBLIC, null); JSONObject
		 * workflows = getChildNodesAsJson(WORKFLOWS + "/" + PUBLIC, "1");
		 * String[] fields = JSONObject.getNames(workflows); String
		 * propertyValue = null; for (String field : fields) { propertyValue =
		 * workflows.getString(field); workflowList.put(new QName(field), new
		 * Node()); } for (Node nextNode:childNodes) { workflowList.put(new
		 * QName(nextNode.getName()), nextNode); } workflowListNode =
		 * getOrAddNode(getOrAddNode(getRootNode(session), WORKFLOWS),
		 * userName); childNodes = getChildNodes(workflowListNode); for (Node
		 * nextNode:childNodes) { workflowList.put(new
		 * QName(nextNode.getName()), nextNode); }
		 * 
		 * } catch (Exception e) { throw new
		 * RegistryException("Error while retrieving workflows from registry!!!"
		 * ,e); }
		 */
		return workflowList;
	}

	@Override
	public Node getWorkflow(QName templateID, String userName)
			throws RegistryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean deleteWorkflow(QName resourceID, String userName)
			throws RegistryException {
		try {
			deleteNode(WORKFLOWS + "/" + userName + "/"
					+ resourceID.getLocalPart());
		} catch (Exception e) {
			throw new RegistryException(
					"Error while deleting workflow from registry!!!", e);
		}
		return false;
	}

	@Override
	public boolean saveWorkflowExecutionServiceInput(
			WorkflowServiceIOData workflowInputData) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionServiceInput(workflowInputData);
    	}
		return saveWorkflowIO(workflowInputData, INPUT);
	}

	@Override
	public boolean saveWorkflowExecutionServiceOutput(
			WorkflowServiceIOData workflowOutputData) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionServiceOutput(workflowOutputData);
    	}
		return saveWorkflowIO(workflowOutputData, OUTPUT);
	}

	private boolean saveWorkflowIO(WorkflowServiceIOData workflowOutputData,
			String type) throws RegistryException {
		try {
			String experimentId = workflowOutputData.getExperimentId();
			String wfName = PROPERTY_WORKFLOW_NAME + "="
					+ workflowOutputData.getWorkflowName();
			createOrUpdateNode(getWorkflowDataNodeURL() + "/" + experimentId
					+ "/" + experimentId, wfName);
			String ioContent = PROPERTY_WORKFLOW_IO_CONTENT + "="
					+ workflowOutputData.getValue();
			createOrUpdateNode(getWorkflowDataNodeURL() + "/" + experimentId
					+ "/" + experimentId + "/" + workflowOutputData.getNodeId()
					+ "/" + type, ioContent);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while saving workflow execution service data!", e);
		}
		return true;
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceInput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().searchWorkflowExecutionServiceInput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
    	}
		return searchWorkflowIO(experimentIdRegEx, workflowNameRegEx,
				nodeNameRegEx, INPUT);
	}

	@Override
	public List<WorkflowServiceIOData> searchWorkflowExecutionServiceOutput(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().searchWorkflowExecutionServiceOutput(experimentIdRegEx, workflowNameRegEx, nodeNameRegEx);
    	}
		return searchWorkflowIO(experimentIdRegEx, workflowNameRegEx,
				nodeNameRegEx, OUTPUT);
	}

	private List<WorkflowServiceIOData> searchWorkflowIO(
			String experimentIdRegEx, String workflowNameRegEx,
			String nodeNameRegEx, String type) throws RegistryException {
		List<WorkflowServiceIOData> workflowIODataList = new ArrayList<WorkflowServiceIOData>();
		try {
			JSONObject expData = getChildNodesAsJson(getWorkflowDataNodeURL(),
					"1");
			String[] expFields = JSONObject.getNames(expData);
			JSONObject jsonExp = null;
			JSONObject wfData = null;
			String[] wfFields = null;
			JSONObject jsonWf = null;
			String propWorkflowName = null;
			JSONObject serviceData = null;
			String[] serviceFields = null;
			JSONObject jsonService = null;
			JSONObject jsonType = null;
			String ioContent = null;
			for (String expName : expFields) {
				if(expName.startsWith("jcr:")){
					continue;
				}
				if (experimentIdRegEx != null
						&& !expName.matches(experimentIdRegEx)) {
					continue;
				}
				jsonExp = expData.optJSONObject(expName);
				if (jsonExp == null) {
					continue;
				}

				wfData = getChildNodesAsJson(getWorkflowDataNodeURL() + "/"
						+ expName, "1");
				wfFields = JSONObject.getNames(wfData);
				for (String wfName : wfFields) {
					if(wfName.startsWith("jcr:")){
						continue;
					}
					jsonWf = wfData.optJSONObject(wfName);
					if (jsonWf == null) {
						continue;
					}
					propWorkflowName = jsonWf.optString(PROPERTY_WORKFLOW_NAME);
					if (!propWorkflowName.isEmpty()) {
						if (workflowNameRegEx != null
								&& !propWorkflowName.matches(workflowNameRegEx)) {
							continue;
						}
					}

					serviceData = getChildNodesAsJson(getWorkflowDataNodeURL()
							+ "/" + expName + "/" + wfName, "1");
					serviceFields = JSONObject.getNames(serviceData);
					WorkflowServiceIOData workflowIOData = null;
					for (String serviceName : serviceFields) {
						if(serviceName.startsWith("jcr:")){
							continue;
						}
						if (nodeNameRegEx != null
								&& !serviceName.matches(nodeNameRegEx)) {
							continue;
						}
						jsonService = serviceData.optJSONObject(serviceName);
						if (jsonService == null) {
							continue;
						}
						createOrUpdateNode(getWorkflowDataNodeURL() + "/"
								+ expName + "/" + wfName + "/" + serviceName
								+ "/" + type, null);
						jsonType = getNodeAsJson(getWorkflowDataNodeURL() + "/"
								+ expName + "/" + wfName + "/" + serviceName
								+ "/" + type);
						ioContent = jsonType
								.optString(PROPERTY_WORKFLOW_IO_CONTENT);
						if (!ioContent.isEmpty()) {
							workflowIOData = new WorkflowServiceIOData();
							workflowIOData.setExperimentId(expName);
							workflowIOData.setWorkflowId(wfName);
							workflowIOData.setWorkflowName(propWorkflowName);
							workflowIOData.setNodeId(serviceName);
							workflowIOData.setValue(ioContent);
							workflowIODataList.add(workflowIOData);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RegistryException(
					"Error while searching workflow execution service data!", e);
		}
		return workflowIODataList;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String experimentId,
			String outputNodeName, String output) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionOutput(experimentId, outputNodeName, output);
    	}
		try {
			String path = getWorkflowExperimentResultNodeURL(experimentId);
			createOrUpdateNode(path, outputNodeName + "=" + output);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}
		return true;
	}

	private String getWorkflowExperimentResultNodeURL(String experimentId) throws Exception {
		createOrUpdateNode(getWorkflowExperimentDataNodeURL(experimentId) + "/" + RESULT, null);
		return getWorkflowExperimentDataNodeURL(experimentId) + "/" + RESULT;
	}

	private String getWorkflowExperimentDataNodeURL(String experimentId) throws Exception {
		createOrUpdateNode(getWorkflowDataNodeURL() + "/" + experimentId + "/"
				+ experimentId, null);
		return getWorkflowDataNodeURL() + "/" + experimentId + "/"
				+ experimentId;
	}

	private String getWorkflowDataNodeURL() throws Exception {
		createOrUpdateNode(WORKFLOW_DATA, null);
		return WORKFLOW_DATA;
	}

	@Override
	public boolean saveWorkflowExecutionOutput(String experimentId,
			WorkflowIOData data) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionOutput(experimentId, data);
    	}
		return saveWorkflowExecutionOutput(experimentId, data.getNodeId(),
				data.getValue());
	}

	@Override
	public WorkflowIOData getWorkflowExecutionOutput(String experimentId,
			String outputNodeName) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionOutput(experimentId, outputNodeName);
    	}
		try {
			String path = getWorkflowExperimentResultNodeURL(experimentId);
			JSONObject resultNode = getNodeAsJson(path);
			String outputProperty = resultNode.optString(outputNodeName);
			if (outputProperty.isEmpty()) {
				return null;
			}
			return new WorkflowIOData(outputNodeName, outputProperty);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}
	}

	@Override
	public List<WorkflowIOData> getWorkflowExecutionOutput(String experimentId)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionOutput(experimentId);
    	}
		List<WorkflowIOData> result = new ArrayList<WorkflowIOData>();
		String[] workflowExecutionOutputNames = getWorkflowExecutionOutputNames(experimentId);
		for (String workflowExecutionOutputName : workflowExecutionOutputNames) {
			result.add(getWorkflowExecutionOutput(experimentId,
					workflowExecutionOutputName));
		}
		return result;
	}

	@Override
	public String[] getWorkflowExecutionOutputNames(String experimentId)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionOutputNames(experimentId);
    	}
		List<String> outputNames = new ArrayList<String>();
		try {
			String path = getWorkflowExperimentResultNodeURL(experimentId);
			JSONObject resultNode = getNodeAsJson(path);
			String[] fields = JSONObject.getNames(resultNode);
			String property = null;
			for (String name : fields) {
				if(name.startsWith("jcr:")){
					continue;
				}
				outputNames.add(name);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new RegistryException(e);
		}
		return outputNames.toArray(new String[] {});
	}

	@Override
	public boolean saveWorkflowExecutionUser(String experimentId, String user)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionUser(experimentId, user);
    	}
		try {
			String workflowDataNodePath = getWorkflowExperimentDataNodeURL(experimentId);
			createOrUpdateNode(workflowDataNodePath, WORKFLOW_USER_PROPERTY
					+ "=" + user);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getWorkflowExecutionUser(String experimentId)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionUser(experimentId);
    	}
		String property = null;
		try {
			String workflowDataNodeURL = getWorkflowExperimentDataNodeURL(experimentId);
			createOrUpdateNode(workflowDataNodeURL, null);
			JSONObject node = getNodeAsJson(workflowDataNodeURL);
			property = node.optString(WORKFLOW_USER_PROPERTY);
			if(property.isEmpty()){
				property = null;
			}
		} catch (Exception e) {
			throw new RegistryException(e);
		}
		return property;
	}

	@Override
	public List<String> getWorkflowExecutionIdByUser(String user)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionIdByUser(user);
    	}
		List<String> ids = new ArrayList<String>();
		try {
			List<String> matchingExperimentIds = getMatchingExperimentIds(".*");
			Pattern compile = Pattern.compile(user == null ? ".*" : user);
			for (String id : matchingExperimentIds) {
				String instanceUser = getWorkflowExecutionUser(id);
				if (user == null
						|| (instanceUser != null && compile.matcher(
								instanceUser).find())) {
					ids.add(id);
				}
			}
		} catch (RepositoryException e) {
			throw new RegistryException(
					"Error in retrieving Execution Ids for the user '" + user
							+ "'", e);
		}
		return ids;
	}

	private List<String> getMatchingExperimentIds(String regex)
			throws RepositoryException {
		List<String> matchList = new ArrayList<String>();
		Pattern compile = Pattern.compile(regex);
		try {
			String path = getWorkflowDataNodeURL();
			JSONObject childNodes = getChildNodesAsJson(path, "1");
			String[] fields = JSONObject.getNames(childNodes);
			for (String name : fields) {
				if(name.startsWith("jcr:")){
					continue;
				}
				if (compile.matcher(name).find()) {
					matchList.add(name);
				}
			}
		} catch (Exception ex) {
			throw new RepositoryException(ex);
		}
		return matchList;
	}

	@Override
	public String getWorkflowExecutionMetadata(String experimentId)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionMetadata(experimentId);
    	}
		String property = null;
		try {
			String path = getWorkflowExperimentDataNodeURL(experimentId);
			property = getNodeAsJson(path)
					.optString(WORKFLOW_METADATA_PROPERTY);
			if(property.isEmpty()){
				property = null;
			}
		} catch (Exception e) {
			throw new RegistryException(
					"Error while retrieving workflow metadata!", e);
		}
		return property;
	}

	@Override
	public boolean saveWorkflowExecutionMetadata(String experimentId,
			String metadata) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionMetadata(experimentId, metadata);
    	}
		try {
			String workflowDataNodePath = getWorkflowExperimentDataNodeURL(experimentId);
			createOrUpdateNode(workflowDataNodePath, WORKFLOW_METADATA_PROPERTY
					+ "=" + metadata);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String saveOutput(String workflowId, List<ActualParameter> parameters)
			throws RegistryException {
		String result = null;
		try {
			String data = "";
			for (int i = 0; i < parameters.size(); i++) {
				data = data + "&" + String.valueOf(i) + "="
						+ parameters.get(i).toXML();
			}
			result = createOrUpdateNode(OUTPUT_NODE_NAME + "/" + workflowId,
					data.substring(1));
		} catch (Exception e) {
			System.out.println(e);
			throw new RegistryException(
					"Error while saving workflow output to the registry!", e);
		}
		return result;
	}

	@Override
	public List<ActualParameter> loadOutput(String workflowId)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().loadOutput(workflowId);
    	}
		ArrayList<ActualParameter> result = new ArrayList<ActualParameter>();
		try {
			createOrUpdateNode(OUTPUT_NODE_NAME, null);
			JSONObject node = getNodeAsJson(OUTPUT_NODE_NAME + "/" + workflowId);
			String[] fields = JSONObject.getNames(node);
			String prop = null;
			for (String name : fields) {
				if(name.startsWith("jcr:")){
					continue;
				}
				prop = node.optString(name);
				if (!prop.isEmpty()) {
					result.add(ActualParameter.fromXML(prop));
				}
			}
		} catch (Exception e) {
			System.out.println(e);
			throw new RegistryException(
					"Error while loading workflow output from registry!", e);
		}
		return result;
	}

	@Override
	public String getWSDL(String serviceName) throws Exception {
		ServiceDescription serviceDescription = getServiceDescription(serviceName);
		if (serviceDescription != null) {
			return getWSDL(serviceDescription);
		}
		throw new ServiceDescriptionRetrieveException(new Exception(
				"No service description from the name " + serviceName));
	}

	@Override
	public String getWSDL(ServiceDescription service) throws Exception {
		try {
			ServiceType type = service.getType().addNewService();
			ServiceName name = type.addNewServiceName();
			name.setStringValue(service.getType().getName());
			name.setTargetNamespace("http://schemas.airavata.apache.org/gfac/type");
			if (service.getType().getPortType() == null) {
				PortTypeType portType = service.getType().addNewPortType();
				MethodType methodType = portType.addNewMethod();
				methodType.setMethodName("invoke");
			} else {
				MethodType method = service.getType().getPortType().getMethod();
				if (method == null) {
					MethodType methodType = service.getType().getPortType()
							.addNewMethod();
					methodType.setMethodName("invoke");
				} else {
					service.getType().getPortType().getMethod()
							.setMethodName("invoke");
				}
			}
			WSDLGenerator generator = new WSDLGenerator();
			Hashtable table = generator.generateWSDL(null, null, null,
					service.getType(), true);
			return (String) table.get(WSDLConstants.AWSDL);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<URI> getInterpreterServiceURLList() throws RegistryException {
		String nodeName = WORKFLOW_INTERPRETER_INSTANCE_DATA;
		try {
			return getServiceURLList(nodeName);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while retrieving Workflow Interpreter Service url list!",
					e);
		}
	}

	private List<URI> getServiceURLList(String nodeName) throws Exception {
		List<URI> urlList = new ArrayList<URI>();
		java.util.Date today = Calendar
				.getInstance(TimeZone.getTimeZone("GMT")).getTime();
		Timestamp timestamp = new Timestamp(today.getTime());
		createOrUpdateNode(getMetadataNodeURL() + "/" + nodeName, null);
		JSONObject node = getNodeAsJson(getMetadataNodeURL() + "/" + nodeName);
		String[] fields = JSONObject.getNames(node);
		String property = null;
		Timestamp setTime = null;
		for (String name : fields) {
			if(name.startsWith("jcr:")){
				continue;
			}
			property = node.optString(name);
			if (property.contains(";")) {
				setTime = new Timestamp(new Long(property.split(";")[1]));
				//if (GFAC_URL_UPDATE_INTERVAL > (timestamp.getTime() - setTime.getTime())) {
					try {
						urlList.add(new URI(property.split(";")[0]));
					} catch (URISyntaxException e) {
						e.printStackTrace();
					}
				//}
			}
		}
		Collections.reverse(urlList);
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
			throw new RegistryException(
					"Error while saving Interoreter Service URL to the registry!",
					e);
		}
		return true;
	}

	private void saveServiceURL(URI gfacURL, String nodeName) throws Exception {
		java.util.Date today = Calendar
				.getInstance(TimeZone.getTimeZone("GMT")).getTime();
		Timestamp timestamp = new Timestamp(today.getTime());
		String propertyName = gfacURL.getHost() + "-" + gfacURL.getPort();
		String data = propertyName + "=" + gfacURL + ";" + timestamp.getTime();
		createOrUpdateNode(getMetadataNodeURL() + "/" + nodeName, data);
	}

	@Override
	public boolean deleteInterpreterServiceURL(URI gfacURL)
			throws RegistryException {
		String nodeName = WORKFLOW_INTERPRETER_INSTANCE_DATA;
		try {
			deleteServiceURL(gfacURL, nodeName);
		} catch (Exception e) {
			System.out.println(e);
			throw new RegistryException(
					"Error while deleting Workflow Interpreter Service URL from registry!",
					e);
		}
		return true;
	}

	private void deleteServiceURL(URI uri, String nodeName) throws Exception {
		String propertyName = uri.getHost() + "-" + uri.getPort();
		createOrUpdateNode(getMetadataNodeURL() + "/" + nodeName, null);
		removePropertyFromNode(getMetadataNodeURL() + "/" + nodeName,
				propertyName);
	}

	@Override
	public List<URI> getMessageBoxServiceURLList() throws RegistryException {
		String nodeName = MESSAGE_BOX_INSTANCE_DATA;
		try {
			return getServiceURLList(nodeName);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while retrieving Message box Service url list!", e);
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
			throw new RegistryException(
					"Error while saving Message box Service URL to the registry!",
					e);
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
			throw new RegistryException(
					"Error while deleting Message box Service URL from registry!",
					e);
		}
		return true;
	}

	@Override
	public List<URI> getEventingServiceURLList() throws RegistryException {
		String nodeName = EVENTING_INSTANCE_DATA;
		try {
			return getServiceURLList(nodeName);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while retrieving Eventing Service url list!", e);
		}
	}

	@Override
	public boolean saveEventingServiceURL(URI gfacURL) throws RegistryException {
		String nodeName = EVENTING_INSTANCE_DATA;
		try {
			saveServiceURL(gfacURL, nodeName);
		} catch (Exception e) {
			System.out.println(e);
			throw new RegistryException(
					"Error while saving Eventing Service URL to the registry!",
					e);
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
			throw new RegistryException(
					"Error while deleting Eventing Service URL from registry!",
					e);
		}
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionStatus(String experimentId,
			WorkflowInstanceStatus status) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionStatus(experimentId, status.getExecutionStatus());
    	}
		try {
			String path = getWorkflowExperimentDataNodeURL(experimentId);
			Date time = status.getStatusUpdateTime();
			if (time == null) {
				time = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
						.getTime();
			}
			String data = WORKFLOW_STATUS_PROPERTY + "="
					+ status.getExecutionStatus().name() + "&"
					+ WORKFLOW_STATUS_TIME_PROPERTY + "=" + time.getTime();
			createOrUpdateNode(path, data);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	@Override
	public boolean saveWorkflowExecutionStatus(String experimentId,
			ExecutionStatus status) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionStatus(experimentId, status);
    	}
		return saveWorkflowExecutionStatus(experimentId,
				new WorkflowInstanceStatus(new WorkflowInstance(experimentId,
						experimentId), status));
	}

	@Override
	public WorkflowInstanceStatus getWorkflowExecutionStatus(String experimentId)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionStatus(experimentId);
    	}
		WorkflowInstanceStatus property = null;
		try {
			String workflowDataNodePath = getWorkflowExperimentDataNodeURL(experimentId);
			createOrUpdateNode(workflowDataNodePath, null);
			JSONObject node = getNodeAsJson(workflowDataNodePath);
			String statusVal = node.optString(WORKFLOW_STATUS_PROPERTY);
			ExecutionStatus status = null;
			if (!statusVal.isEmpty()) {
				status = ExecutionStatus.valueOf(statusVal);
			}
			long dateVal = node.optLong(WORKFLOW_STATUS_TIME_PROPERTY);
			Date date = null;
			if (dateVal != 0) { // 0=no value
				Long dateMiliseconds = dateVal;
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(dateMiliseconds);
				date = cal.getTime();
			}
			property = new WorkflowInstanceStatus(new WorkflowInstance(
					experimentId, experimentId), status, date);
		} catch (Exception e) {
			throw new RegistryException(
					"Error while retrieving workflow execution status!", e);
		}
		return property;
	}

	@Override
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

	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String user)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionByUser(user);
    	}
		return getWorkflowExecution(user,-1,-1);
	}
	
	@Override
	public List<WorkflowExecution> getWorkflowExecutionByUser(String user,
			int pageSize, int pageNo) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionByUser(user, pageSize, pageNo);
    	}
		return getWorkflowExecutionByUser(user,pageSize*pageNo,pageSize*(pageNo+1));
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

	@Override
	public boolean saveWorkflowExecutionName(String experimentId,
			String workflowIntanceName) throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowExecutionName(experimentId, workflowIntanceName);
    	}
        try {
            String workflowDataNodePath = getWorkflowExperimentDataNodeURL(experimentId);
            createOrUpdateNode(workflowDataNodePath, WORKFLOW_INSTANCE_NAME_PROPERTY + "=" + workflowIntanceName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
	}

	@Override
	public String getWorkflowExecutionName(String experimentId)
			throws RegistryException {
		if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().getWorkflowExecutionName(experimentId);
    	}
    	String property = null;
        try {
            String workflowDataNodePath = getWorkflowExperimentDataNodeURL(experimentId);
            createOrUpdateNode(workflowDataNodePath, null);
            property = getNodeAsJson(workflowDataNodePath).optString(WORKFLOW_INSTANCE_NAME_PROPERTY);
            if(property.isEmpty()){
				property = null;
			}
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving workflow execution name!", e);
        }
        return property;
	}

	@Override
	public String getWorkflowExecutionTemplateName(String experimentId)
			throws RegistryException {
    	String templateName = null;
        try {
            String workflowDataNodePath = getWorkflowExperimentDataNodeURL(experimentId);
            createOrUpdateNode(workflowDataNodePath, null);
            templateName = getNodeAsJson(workflowDataNodePath).optString(PROPERTY_WORKFLOW_NAME);
            if(templateName.isEmpty()){
				templateName = null;
			}
        } catch (Exception e) {
            throw new RegistryException("Error while retrieving workflow execution template name!", e);
        }
        return templateName;
	}

    @Override
    public boolean saveWorkflowData(WorkflowRunTimeData workflowData)throws RegistryException{
        if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowData(workflowData);
    	}
        return false;
    }

    @Override
    public  boolean saveWorkflowLastUpdateTime(String experimentId,Timestamp timestamp)throws RegistryException{
        if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowLastUpdateTime(experimentId, timestamp);
    	}
        return false;
    }

    @Override
    public boolean saveWorkflowNodeStatus(String workflowInstanceID,String workflowNodeID,ExecutionStatus status)throws RegistryException{
       if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowNodeStatus(workflowInstanceID,workflowNodeID, status);
    	}
        return false;
    }

    @Override
    public boolean saveWorkflowNodeLastUpdateTime(String workflowInstanceID,String workflowNodeID,Timestamp lastUpdateTime)throws RegistryException{
        if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowNodeLastUpdateTime(workflowInstanceID,workflowNodeID, lastUpdateTime);
    	}
        return false;
    }

    @Override
    public boolean saveWorkflowNodeGramData(WorkflowNodeGramData workflowNodeGramData)throws RegistryException{
        if (getProvenanceRegistry()!=null){
    		return getProvenanceRegistry().saveWorkflowNodeGramData(workflowNodeGramData);
    	}
        return false;
    }

    @Override
    public boolean saveWorkflowNodeGramLocalJobID(String workflowInstanceID, String workflowNodeID, String localJobID) throws RegistryException {
        if (getProvenanceRegistry() != null) {
            return getProvenanceRegistry().saveWorkflowNodeGramLocalJobID(workflowInstanceID, workflowNodeID, localJobID);
        }
        return false;
    }

}