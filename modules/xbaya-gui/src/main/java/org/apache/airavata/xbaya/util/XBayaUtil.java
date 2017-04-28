/**
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
 */
package org.apache.airavata.xbaya.util;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.airavata.api.Airavata;
import org.apache.airavata.api.Airavata.Client;
import org.apache.airavata.api.client.AiravataClientFactory;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.model.error.AiravataClientConnectException;
import org.apache.airavata.workflow.model.exceptions.WorkflowRuntimeException;
import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.airavata.workflow.model.graph.Node;
import org.apache.airavata.workflow.model.graph.system.EndForEachNode;
import org.apache.airavata.workflow.model.graph.system.ForEachNode;
import org.apache.airavata.xbaya.ThriftClientData;
import org.apache.airavata.xbaya.ThriftServiceType;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.ui.dialogs.registry.RegistryWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XBayaUtil {

    private static final Logger logger = LoggerFactory.getLogger(XBayaUtil.class);
    public static final String JCR_USER = "jcr.username";
    public static final String JCR_PASS = "jcr.password";
    public static final String JCR_URL = "jcr.url";


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
            logger.error(e.getMessage(), e);
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
		return AiravataClientFactory.createAiravataClient(data.getServerAddress(),data.getServerPort());
	}
	

	
//    public static AiravataRegistry2 getExperimentCatalog(URL url) throws IOException, RepositoryException, URISyntaxException {
//        Properties properties = new Properties();
//        properties.load(url.openStream());
//        JCRComponentRegistry jcrComponentRegistry = new JCRComponentRegistry(new URI((String) properties.get(JCR_URL)),
//                (String) properties.get(JCR_USER),(String) properties.get(JCR_PASS));
//        return jcrComponentRegistry.getExperimentCatalog();
//    }

}