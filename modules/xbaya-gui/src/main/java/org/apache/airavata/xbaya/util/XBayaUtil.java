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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.gui.JCRRegistryWindow;
import org.apache.airavata.xbaya.graph.DataPort;
import org.apache.airavata.xbaya.graph.Node;
import org.apache.airavata.xbaya.graph.amazon.InstanceNode;
import org.apache.airavata.xbaya.graph.system.ConstantNode;
import org.apache.airavata.xbaya.graph.system.EndForEachNode;
import org.apache.airavata.xbaya.graph.system.EndifNode;
import org.apache.airavata.xbaya.graph.system.ForEachNode;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.interpretor.NameValue;
import org.apache.airavata.xbaya.interpretor.SystemComponentInvoker;
import org.apache.airavata.xbaya.interpretor.WorkFlowInterpreterException;
import org.apache.airavata.xbaya.invoker.GenericInvoker;
import org.apache.airavata.xbaya.invoker.Invoker;
import org.apache.airavata.xbaya.invoker.WorkflowInvokerWrapperForGFacInvoker;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.monitor.MonitorConfiguration;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.axis2.util.XMLUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.infoset.XmlElement;

import xsul.lead.LeadContextHeader;
import xsul.lead.LeadResourceMapping;
import xsul5.XmlConstants;

public class XBayaUtil {

    private static final Logger logger = LoggerFactory.getLogger(XBayaUtil.class);

    public static LeadContextHeader buildLeadContextHeader(final XBayaEngine engine,
            MonitorConfiguration monitorConfiguration, String nodeId, LeadResourceMapping resourceMapping)
            throws URISyntaxException {

        XBayaConfiguration configuration = engine.getConfiguration();
        Workflow workflow = engine.getWorkflow();

        LeadContextHeader leadContext = buildLeadContextHeader(workflow, configuration, monitorConfiguration, nodeId,
                resourceMapping);

        return leadContext;

    }

    /**
     * 
     * @param workflow
     * @param configuration
     * @param monitorConfiguration
     * @param nodeId
     * @param resourceMapping
     * @return
     * @throws URISyntaxException
     */
    public static LeadContextHeader buildLeadContextHeader(Workflow workflow, XBayaConfiguration configuration,
            MonitorConfiguration monitorConfiguration, String nodeId, LeadResourceMapping resourceMapping)
            throws URISyntaxException {
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        leadContextHelper.setXBayaConfiguration(configuration);

        leadContextHelper.setWorkflowInstanceID(workflow.getGPELInstanceID());
        leadContextHelper.setWorkflowTemplateID(workflow.getUniqueWorkflowName());

        leadContextHelper.setMonitorConfiguration(monitorConfiguration);

        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        leadContext.setNodeId(nodeId);

        leadContext.setTimeStep("1");
        // leadContext.setXRegistryUrl(new URI(configuration.getXRegistryURL().toString() + "?wsdl"));

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

    public static boolean acquireJCRRegistry(XBayaEngine engine) {
        XBayaConfiguration configuration = engine.getConfiguration();
        if (configuration.getJcrComponentRegistry() == null) {
            JCRRegistryWindow window = new JCRRegistryWindow(engine);
            window.show();
        }
        return engine.getConfiguration().getJcrComponentRegistry() != null;
    }
    public static Object getInputsForForEachNode(final ForEachNode forEachNode,
			final LinkedList<String> listOfValues, Map<Node, Invoker> invokerMap) throws XBayaException {
		Node forEachInputNode = forEachNode.getInputPort(0).getFromNode();
		// if input node for for-each is WSNode
		Object returnValForProvenance = null;
		if (forEachInputNode instanceof InputNode) {
			for (DataPort dataPort : forEachNode.getInputPorts()) {
				returnValForProvenance = XBayaUtil
						.findInputFromPort(dataPort, invokerMap);
				if (null == returnValForProvenance) {
					throw new WorkFlowInterpreterException(
							"Unable to find input for the node:"
									+ forEachNode.getID());
				}
				String[] vals = returnValForProvenance.toString().split(",");
				listOfValues.addAll(Arrays.asList(vals));
			}
		} else {
			Invoker workflowInvoker = invokerMap
					.get(forEachInputNode);
			if (workflowInvoker != null) {
				if (workflowInvoker instanceof GenericInvoker) {

					returnValForProvenance = ((GenericInvoker) workflowInvoker)
							.getOutputs();
					String message = returnValForProvenance.toString();

					XmlElement msgElmt = XmlConstants.BUILDER
							.parseFragmentFromString(message);
					Iterator children = msgElmt.children().iterator();
					while (children.hasNext()) {
						Object object = children.next();
						// foreachWSNode.getInputPort(0).getType()
						if (object instanceof XmlElement) {
							listOfValues.add(XmlConstants.BUILDER
									.serializeToString(object));
							// TODO fix for simple type - Done
						}
					}
				} else if (workflowInvoker instanceof WorkflowInvokerWrapperForGFacInvoker) {
					String outputName = forEachInputNode.getOutputPort(0)
							.getName();
					returnValForProvenance = workflowInvoker
							.getOutput(outputName);
					org.xmlpull.v1.builder.XmlElement msgElmt = (org.xmlpull.v1.builder.XmlElement) returnValForProvenance;
					Iterator children = msgElmt.children();
					while (children.hasNext()) {
						Object object = children.next();
						if (object instanceof org.xmlpull.v1.builder.XmlElement) {
							org.xmlpull.v1.builder.XmlElement child = (org.xmlpull.v1.builder.XmlElement) object;
							Iterator valItr = child.children();
							if (valItr.hasNext()) {
								Object object2 = valItr.next();
								if (object2 instanceof String) {
									listOfValues.add(object2.toString());
								}
							}
						}
					}
				} else if (workflowInvoker instanceof SystemComponentInvoker) {
					String outputName = forEachInputNode.getOutputPort(0)
							.getName();
					returnValForProvenance = workflowInvoker
							.getOutput(outputName);
					XmlElement msgElmt = XmlConstants.BUILDER
							.parseFragmentFromString("<temp>"
									+ returnValForProvenance + "</temp>");
					Iterator valItr = msgElmt.children().iterator();
					while (valItr.hasNext()) {
						Object object2 = valItr.next();
						if (object2 instanceof XmlElement) {
							listOfValues.add(((XmlElement) object2).children()
									.iterator().next().toString());
						}
					}
				}
			} else {
				throw new WorkFlowInterpreterException(
						"Did not find inputs from WS to foreach");
			}
		}
		return returnValForProvenance;
	}

    /**
     *
     * @param inputPort
     * @param invokerMap
     * @return
     * @throws XBayaException
     */
	public static Object findInputFromPort(DataPort inputPort, Map<Node, Invoker>  invokerMap) throws XBayaException {
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
							+ ","
							+ ((XmlElement) object2).children().iterator()
									.next().toString();
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
			throw new XBayaRuntimeException("ForEach output does not contain single out-edge");
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
		throw new XBayaRuntimeException("EndForEachNode not found");
	}
	
	
	public static List<NameValue> getIOParameterData(String xml) throws ParserConfigurationException, SAXException, IOException{
		List<NameValue> parameters=new ArrayList<NameValue>();
		Document parameterDocument = XMLUtils.newDocument(new ByteArrayInputStream(xml.getBytes()));
		org.w3c.dom.NodeList childNodes = parameterDocument.getDocumentElement().getChildNodes();
		for(int i=0;i<childNodes.getLength();i++){
			org.w3c.dom.Node parameterNode = childNodes.item(i);
			NameValue pair = new NameValue();
			pair.setName(parameterNode.getLocalName());
			pair.setValue(parameterNode.getTextContent());
			parameters.add(pair);
		}
		return parameters;
	}

}