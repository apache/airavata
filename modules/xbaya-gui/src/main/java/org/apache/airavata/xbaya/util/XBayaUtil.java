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

package org.apache.airavata.xbaya.util;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.api.workflow.Workflow.Client;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.amazon.InstanceNode;
import org.apache.airavata.workflow.model.graph.system.ConstantNode;
import org.apache.airavata.workflow.model.graph.system.EndForEachNode;
import org.apache.airavata.workflow.model.graph.system.EndifNode;
import org.apache.airavata.workflow.model.graph.system.ForEachNode;
import org.apache.airavata.workflow.model.graph.system.InputNode;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.ThriftClientData;
import org.apache.airavata.xbaya.ThriftServiceType;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.invoker.Invoker;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.ui.dialogs.registry.RegistryWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.infoset.XmlElement;

import xsul.lead.LeadContextHeader;
import xsul.lead.LeadResourceMapping;
import xsul5.XmlConstants;

public class XBayaUtil {

    private static final Logger logger = LoggerFactory.getLogger(XBayaUtil.class);
    public static final String JCR_USER = "jcr.username";
    public static final String JCR_PASS = "jcr.password";
    public static final String JCR_URL = "jcr.url";

    public static LeadContextHeader buildLeadContextHeader(final XBayaEngine engine,
           String nodeId, LeadResourceMapping resourceMapping)
            throws URISyntaxException {

        XBayaConfiguration configuration = engine.getConfiguration();
        Workflow workflow = engine.getGUI().getWorkflow();

        LeadContextHeader leadContext = buildLeadContextHeader(workflow, configuration, nodeId,
                resourceMapping);

        return leadContext;

    }

    /**
     * 
     * @param workflow
     * @param configuration
     * @param nodeId
     * @param resourceMapping
     * @return
     * @throws URISyntaxException
     */
    public static LeadContextHeader buildLeadContextHeader(Workflow workflow, XBayaConfiguration configuration,
            String nodeId, LeadResourceMapping resourceMapping)
            throws URISyntaxException {
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        leadContextHelper.setXBayaConfiguration(configuration);

        leadContextHelper.setWorkflowInstanceID(workflow.getGPELInstanceID());
        leadContextHelper.setWorkflowTemplateID(workflow.getUniqueWorkflowName());

//        leadContextHelper.setMonitorConfiguration(monitorConfiguration);

        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        leadContext.setNodeId(nodeId);

        leadContext.setTimeStep("1");

        if (resourceMapping != null) {
            leadContext.setResourceMapping(resourceMapping);
        }
        return leadContext;

    }

    public static boolean isURLExists(String URLName) {
        try {
            if (!URLName.toUpperCase().contains("HTTP"))
                URLName = "http://" + URLName;
            URL url = new URL(URLName);
            System.setProperty("java.net.useSystemProxies", "true");
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setConnectTimeout(9000);
            urlConn.setReadTimeout(9000);
            urlConn.connect();
            if (HttpURLConnection.HTTP_OK == urlConn.getResponseCode())
                return true;
            else
                return false;
        } catch (SocketTimeoutException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

//    public static boolean acquireJCRRegistry(XBayaEngine engine) {
//        XBayaConfiguration configuration = engine.getConfiguration();
//        if (configuration.getAiravataAPI() == null) {
//        	updateJCRRegistryInfo(engine);
//        }
//        return engine.getConfiguration().getAiravataAPI() != null;
//    }
    
    public static void updateJCRRegistryInfo(XBayaEngine xbayaEngine) {
    	RegistryWindow window = new RegistryWindow(xbayaEngine, ThriftServiceType.API_SERVICE);
        window.show();
	}
   
    /**
     *
     * @param inputPort
     * @param invokerMap
     * @return
     * @throws WorkflowException
     */
	public static Object findInputFromPort(DataPort inputPort, Map<Node, Invoker>  invokerMap) throws WorkflowException {
		Object outputVal = null;
		Node fromNode = inputPort.getFromNode();
		if (fromNode instanceof InputNode) {
			outputVal = ((InputNode) fromNode).getDefaultValue();
		} else if (fromNode instanceof ConstantNode) {
			outputVal = ((ConstantNode) fromNode).getValue();
		} else if (fromNode instanceof EndifNode) {
			Invoker fromInvoker = invokerMap.get(fromNode);
			outputVal = fromInvoker.getOutput(inputPort.getFromPort().getID());
		} else if (fromNode instanceof InstanceNode) {
			return ((InstanceNode) fromNode).getOutputInstanceId();
		} else if (fromNode instanceof EndForEachNode) {
			outputVal = "";
			Invoker workflowInvoker = invokerMap.get(fromNode);
			String outputName = fromNode.getOutputPort(0).getName();
			XmlElement msgElmt = XmlConstants.BUILDER
					.parseFragmentFromString("<temp>"
							+ workflowInvoker.getOutput(outputName) + "</temp>");
			Iterator valItr = msgElmt.children().iterator();
			while (valItr.hasNext()) {
				Object object2 = valItr.next();
				if (object2 instanceof XmlElement) {
					outputVal = outputVal
							+ StringUtil.DELIMETER 
							+ StringUtil.quoteString(((XmlElement) object2).children().iterator()
									.next().toString());
				}
			}
			outputVal = ((String) outputVal).substring(1,
					((String) outputVal).length());
		} else {
			Invoker fromInvoker = invokerMap.get(fromNode);
			try {
				if (fromInvoker != null)
					outputVal = fromInvoker.getOutput(inputPort.getFromPort()
							.getName());

			} catch (Exception e) {
				// if the value is still null look it up from the inputport name
				// because the value is set to the input port name at some point
				// there is no harm in doing this
				if (null == outputVal) {
					outputVal = fromInvoker.getOutput(inputPort.getName());
				}
			}

		}
		return outputVal;

	}

	/**
	 * @param node
	 * @return
	 */
	public static Node findEndForEachFor(ForEachNode node) {

		Collection<Node> toNodes = node.getOutputPort(0).getToNodes();
		if(toNodes.size() != 1){
			throw new WorkflowRuntimeException("ForEach output does not contain single out-edge");
		}
		Node middleNode = toNodes.iterator().next();
		List<DataPort> outputPorts = middleNode.getOutputPorts();
		for (DataPort dataPort : outputPorts) {
			if(dataPort.getToNodes().size() == 1){
				Node possibleEndForEachNode = dataPort.getToNodes().get(0);
				if(possibleEndForEachNode instanceof EndForEachNode){
					return possibleEndForEachNode;
				}
			}
		}
		throw new WorkflowRuntimeException("EndForEachNode not found");
	}
	
	
//	public static List<NameValue> getIOParameterData(String xml) throws ParserConfigurationException, SAXException, IOException{
//		List<NameValue> parameters=new ArrayList<NameValue>();
//		Document parameterDocument = XMLUtils.newDocument(new ByteArrayInputStream(xml.getBytes()));
//		org.w3c.dom.NodeList childNodes = parameterDocument.getDocumentElement().getChildNodes();
//		for(int i=0;i<childNodes.getLength();i++){
//			org.w3c.dom.Node parameterNode = childNodes.item(i);
//			NameValue pair = new NameValue();
//			pair.setName(parameterNode.getLocalName());
//			pair.setValue(parameterNode.getTextContent());
//			parameters.add(pair);
//		}
//		return parameters;
//	}

	public static Airavata.Client getAiravataClient(ThriftClientData data) throws AiravataClientConnectException{
		return AiravataClientFactory.createAiravataClient(data.getServerAddress(),data.getServerPort());
	}
	
	public static Client getWorkflowClient(ThriftClientData data) throws AiravataClientConnectException{
		return AiravataClientFactory.createWorkflowClient(data.getServerAddress(),data.getServerPort());
	}
	

	
//    public static AiravataRegistry2 getRegistry(URL url) throws IOException, RepositoryException, URISyntaxException {
//        Properties properties = new Properties();
//        properties.load(url.openStream());
//        JCRComponentRegistry jcrComponentRegistry = new JCRComponentRegistry(new URI((String) properties.get(JCR_URL)),
//                (String) properties.get(JCR_USER),(String) properties.get(JCR_PASS));
//        return jcrComponentRegistry.getRegistry();
//    }

}