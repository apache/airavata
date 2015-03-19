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
package org.apache.airavata.gfac.core.utils;

import org.airavata.appcatalog.cpi.AppCatalog;
import org.airavata.appcatalog.cpi.AppCatalogException;
import org.apache.aiaravata.application.catalog.data.impl.AppCatalogFactory;
import org.apache.airavata.common.exception.ApplicationSettingsException;
import org.apache.airavata.common.utils.AiravataZKUtils;
import org.apache.airavata.common.utils.DBUtil;
import org.apache.airavata.common.utils.ServerSettings;
import org.apache.airavata.credential.store.store.CredentialReader;
import org.apache.airavata.credential.store.store.impl.CredentialReaderImpl;
import org.apache.airavata.gfac.Constants;
import org.apache.airavata.gfac.ExecutionMode;
import org.apache.airavata.gfac.GFacConfiguration;
import org.apache.airavata.gfac.GFacException;
import org.apache.airavata.gfac.core.context.JobExecutionContext;
import org.apache.airavata.gfac.core.handler.GFacHandlerException;
import org.apache.airavata.gfac.core.states.GfacExperimentState;
import org.apache.airavata.gfac.core.states.GfacPluginState;
import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.airavata.model.appcatalog.computeresource.*;
import org.apache.airavata.model.workspace.experiment.*;
import org.apache.airavata.persistance.registry.jpa.impl.RegistryFactory;
import org.apache.airavata.registry.cpi.ChildDataType;
import org.apache.airavata.registry.cpi.CompositeIdentifier;
import org.apache.airavata.registry.cpi.Registry;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.*;

import java.io.*;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.*;

//import org.apache.airavata.commons.gfac.type.ActualParameter;

public class GFacUtils {
	private final static Logger log = LoggerFactory.getLogger(GFacUtils.class);
	public static final String DELIVERY_TAG_POSTFIX = "-deliveryTag";

	private GFacUtils() {
	}

	/**
	 * Read data from inputStream and convert it to String.
	 * 
	 * @param in
	 * @return String read from inputStream
	 * @throws java.io.IOException
	 */
	public static String readFromStream(InputStream in) throws IOException {
		try {
			StringBuffer wsdlStr = new StringBuffer();

			int read;

			byte[] buf = new byte[1024];
			while ((read = in.read(buf)) > 0) {
				wsdlStr.append(new String(buf, 0, read));
			}
			return wsdlStr.toString();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					log.warn("Cannot close InputStream: "
							+ in.getClass().getName(), e);
				}
			}
		}
	}

	/**
	 * this can be used to do framework opertaions specific to different modes
	 * 
	 * @param jobExecutionContext
	 * @return
	 */
	public static boolean isSynchronousMode(
			JobExecutionContext jobExecutionContext) {
		GFacConfiguration gFacConfiguration = jobExecutionContext
				.getGFacConfiguration();
		if (ExecutionMode.ASYNCHRONOUS.equals(gFacConfiguration
				.getExecutionMode())) {
			return false;
		}
		return true;
	}

	public static String readFileToString(String file)
			throws FileNotFoundException, IOException {
		BufferedReader instream = null;
		try {

			instream = new BufferedReader(new FileReader(file));
			StringBuffer buff = new StringBuffer();
			String temp = null;
			while ((temp = instream.readLine()) != null) {
				buff.append(temp);
				buff.append(Constants.NEWLINE);
			}
			return buff.toString();
		} finally {
			if (instream != null) {
				try {
					instream.close();
				} catch (IOException e) {
					log.warn("Cannot close FileinputStream", e);
				}
			}
		}
	}

	public static boolean isLocalHost(String appHost)
			throws UnknownHostException {
		String localHost = InetAddress.getLocalHost().getCanonicalHostName();
		return (localHost.equals(appHost)
				|| Constants.LOCALHOST.equals(appHost) || Constants._127_0_0_1
					.equals(appHost));
	}

	public static String createUniqueNameWithDate(String name) {
		String date = new Date().toString();
		date = date.replaceAll(" ", "_");
		date = date.replaceAll(":", "_");
		return name + "_" + date;
	}

    public static List<Element> getElementList(Document doc, String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression expr = xPath.compile(expression);
        NodeList nodeList = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        List<Element> elementList = new ArrayList<Element>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if (item instanceof Element) {
                elementList.add((Element) item);
            }
        }
        return elementList;
    }

	public static String createGsiftpURIAsString(String host, String localPath)
			throws URISyntaxException {
		StringBuffer buf = new StringBuffer();
		if (!host.startsWith("gsiftp://"))
			buf.append("gsiftp://");
		buf.append(host);
		if (!host.endsWith("/"))
			buf.append("/");
		buf.append(localPath);
		return buf.toString();
	}

//	public static ActualParameter getInputActualParameter(Parameter parameter,
//			DataObjectType element) {
//		ActualParameter actualParameter = new ActualParameter();
//		if ("String".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(StringParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((StringParameterType) actualParameter.getType())
//						.setValue(element.getValue());
//			} else {
//				((StringParameterType) actualParameter.getType()).setValue("");
//			}
//		} else if ("Double".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(DoubleParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((DoubleParameterType) actualParameter.getType())
//						.setValue(new Double(element.getValue()));
//			}
//		} else if ("Integer".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(IntegerParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((IntegerParameterType) actualParameter.getType())
//						.setValue(new Integer(element.getValue()));
//			}
//		} else if ("Float".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FloatParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((FloatParameterType) actualParameter.getType())
//						.setValue(new Float(element.getValue()));
//			}
//		} else if ("Boolean".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(BooleanParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((BooleanParameterType) actualParameter.getType())
//						.setValue(new Boolean(element.getValue()));
//			}
//		} else if ("File".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FileParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((FileParameterType) actualParameter.getType())
//						.setValue(element.getValue());
//			}
//		} else if ("URI".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(URIParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((URIParameterType) actualParameter.getType()).setValue(element
//						.getValue());
//			} else {
//				((URIParameterType) actualParameter.getType()).setValue("");
//			}
//
//		} else if ("StdOut".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(StdOutParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((StdOutParameterType) actualParameter.getType())
//						.setValue(element.getValue());
//			} else {
//				((StdOutParameterType) actualParameter.getType()).setValue("");
//			}
//
//		} else if ("StdErr".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(StdErrParameterType.type);
//			if (!"".equals(element.getValue())) {
//				((StdErrParameterType) actualParameter.getType())
//						.setValue(element.getValue());
//			} else {
//				((StdErrParameterType) actualParameter.getType()).setValue("");
//			}
//
//		}
//		return actualParameter;
//	}

//	public static ActualParameter getInputActualParameter(Parameter parameter,
//			OMElement element) {
//		OMElement innerelement = null;
//		ActualParameter actualParameter = new ActualParameter();
//		if ("String".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(StringParameterType.type);
//			if (!"".equals(element.getText())) {
//				((StringParameterType) actualParameter.getType())
//						.setValue(element.getText());
//			} else if (element.getChildrenWithLocalName("value").hasNext()) {
//				innerelement = (OMElement) element.getChildrenWithLocalName(
//						"value").next();
//				((StringParameterType) actualParameter.getType())
//						.setValue(innerelement.getText());
//			} else {
//				((StringParameterType) actualParameter.getType()).setValue("");
//			}
//		} else if ("Double".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(DoubleParameterType.type);
//			if (!"".equals(element.getText())) {
//				((DoubleParameterType) actualParameter.getType())
//						.setValue(new Double(innerelement.getText()));
//			} else {
//				innerelement = (OMElement) element.getChildrenWithLocalName(
//						"value").next();
//				((DoubleParameterType) actualParameter.getType())
//						.setValue(new Double(innerelement.getText()));
//			}
//		} else if ("Integer".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(IntegerParameterType.type);
//			if (!"".equals(element.getText())) {
//				((IntegerParameterType) actualParameter.getType())
//						.setValue(new Integer(element.getText()));
//			} else {
//				innerelement = (OMElement) element.getChildrenWithLocalName(
//						"value").next();
//				((IntegerParameterType) actualParameter.getType())
//						.setValue(new Integer(innerelement.getText()));
//			}
//		} else if ("Float".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FloatParameterType.type);
//			if (!"".equals(element.getText())) {
//				((FloatParameterType) actualParameter.getType())
//						.setValue(new Float(element.getText()));
//			} else {
//				innerelement = (OMElement) element.getChildrenWithLocalName(
//						"value").next();
//				((FloatParameterType) actualParameter.getType())
//						.setValue(new Float(innerelement.getText()));
//			}
//		} else if ("Boolean".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(BooleanParameterType.type);
//			if (!"".equals(element.getText())) {
//				((BooleanParameterType) actualParameter.getType())
//						.setValue(new Boolean(element.getText()));
//			} else {
//				innerelement = (OMElement) element.getChildrenWithLocalName(
//						"value").next();
//				((BooleanParameterType) actualParameter.getType())
//						.setValue(Boolean.parseBoolean(innerelement.getText()));
//			}
//		} else if ("File".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FileParameterType.type);
//			if (!"".equals(element.getText())) {
//				((FileParameterType) actualParameter.getType())
//						.setValue(element.getText());
//			} else {
//				innerelement = (OMElement) element.getChildrenWithLocalName(
//						"value").next();
//				((FileParameterType) actualParameter.getType())
//						.setValue(innerelement.getText());
//			}
//		} else if ("URI".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(URIParameterType.type);
//			if (!"".equals(element.getText())) {
//				((URIParameterType) actualParameter.getType()).setValue(element
//						.getText());
//			} else if (element.getChildrenWithLocalName("value").hasNext()) {
//				innerelement = (OMElement) element.getChildrenWithLocalName(
//						"value").next();
//				System.out.println(actualParameter.getType().toString());
//				log.debug(actualParameter.getType().toString());
//				((URIParameterType) actualParameter.getType())
//						.setValue(innerelement.getText());
//			} else {
//				((URIParameterType) actualParameter.getType()).setValue("");
//			}
//		} else if ("StringArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(StringArrayType.type);
//			Iterator value = element.getChildrenWithLocalName("value");
//			int i = 0;
//			if (!"".equals(element.getText())) {
//				String[] list = StringUtil.getElementsFromString(element
//						.getText());
//				for (String arrayValue : list) {
//					((StringArrayType) actualParameter.getType()).insertValue(
//							i++, arrayValue);
//				}
//			} else {
//				while (value.hasNext()) {
//					innerelement = (OMElement) value.next();
//					((StringArrayType) actualParameter.getType()).insertValue(
//							i++, innerelement.getText());
//				}
//			}
//		} else if ("DoubleArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(DoubleArrayType.type);
//			Iterator value = element.getChildrenWithLocalName("value");
//			int i = 0;
//			if (!"".equals(element.getText())) {
//				String[] list = StringUtil.getElementsFromString(element
//						.getText());
//				for (String arrayValue : list) {
//					((DoubleArrayType) actualParameter.getType()).insertValue(
//							i++, new Double(arrayValue));
//				}
//			} else {
//				while (value.hasNext()) {
//					innerelement = (OMElement) value.next();
//					((DoubleArrayType) actualParameter.getType()).insertValue(
//							i++, new Double(innerelement.getText()));
//				}
//			}
//
//		} else if ("IntegerArray"
//				.equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(IntegerArrayType.type);
//			Iterator value = element.getChildrenWithLocalName("value");
//			int i = 0;
//			if (!"".equals(element.getText())) {
//				String[] list = StringUtil.getElementsFromString(element
//						.getText());
//				for (String arrayValue : list) {
//					((IntegerArrayType) actualParameter.getType()).insertValue(
//							i++, new Integer(arrayValue));
//				}
//			} else {
//				while (value.hasNext()) {
//					innerelement = (OMElement) value.next();
//					((IntegerArrayType) actualParameter.getType()).insertValue(
//							i++, new Integer(innerelement.getText()));
//				}
//			}
//		} else if ("FloatArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FloatArrayType.type);
//			Iterator value = element.getChildrenWithLocalName("value");
//			int i = 0;
//			if (!"".equals(element.getText())) {
//				String[] list = StringUtil.getElementsFromString(element
//						.getText());
//				for (String arrayValue : list) {
//					((FloatArrayType) actualParameter.getType()).insertValue(
//							i++, new Float(arrayValue));
//				}
//			} else {
//
//				while (value.hasNext()) {
//					innerelement = (OMElement) value.next();
//					((FloatArrayType) actualParameter.getType()).insertValue(
//							i++, new Float(innerelement.getText()));
//				}
//			}
//		} else if ("BooleanArray"
//				.equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(BooleanArrayType.type);
//			Iterator value = element.getChildrenWithLocalName("value");
//			int i = 0;
//			if (!"".equals(element.getText())) {
//				String[] list = StringUtil.getElementsFromString(element
//						.getText());
//				for (String arrayValue : list) {
//					((BooleanArrayType) actualParameter.getType()).insertValue(
//							i++, new Boolean(arrayValue));
//				}
//			} else {
//
//				while (value.hasNext()) {
//					innerelement = (OMElement) value.next();
//					((BooleanArrayType) actualParameter.getType()).insertValue(
//							i++, new Boolean(innerelement.getText()));
//				}
//			}
//		} else if ("FileArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FileArrayType.type);
//			Iterator value = element.getChildrenWithLocalName("value");
//			int i = 0;
//			if (!"".equals(element.getText())) {
//				String[] list = StringUtil.getElementsFromString(element
//						.getText());
//				for (String arrayValue : list) {
//					((FileArrayType) actualParameter.getType()).insertValue(
//							i++, arrayValue);
//				}
//			} else {
//
//				while (value.hasNext()) {
//					innerelement = (OMElement) value.next();
//					((FileArrayType) actualParameter.getType()).insertValue(
//							i++, innerelement.getText());
//				}
//			}
//		} else if ("URIArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(URIArrayType.type);
//			Iterator value = element.getChildrenWithLocalName("value");
//			int i = 0;
//			if (!"".equals(element.getText())) {
//				String[] list = StringUtil.getElementsFromString(element
//						.getText());
//				for (String arrayValue : list) {
//					((URIArrayType) actualParameter.getType()).insertValue(i++,
//							arrayValue);
//				}
//			} else {
//
//				while (value.hasNext()) {
//					innerelement = (OMElement) value.next();
//					((URIArrayType) actualParameter.getType()).insertValue(i++,
//							innerelement.getText());
//				}
//			}
//		}
//		return actualParameter;
//	}

//	public static ActualParameter getInputActualParameter(Parameter parameter,
//			String inputVal) throws GFacException {
//		OMElement innerelement = null;
//		ActualParameter actualParameter = new ActualParameter();
//		if ("String".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(StringParameterType.type);
//			((StringParameterType) actualParameter.getType())
//					.setValue(inputVal);
//		} else if ("Double".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(DoubleParameterType.type);
//			((DoubleParameterType) actualParameter.getType())
//					.setValue(new Double(inputVal));
//		} else if ("Integer".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(IntegerParameterType.type);
//			((IntegerParameterType) actualParameter.getType())
//					.setValue(new Integer(inputVal));
//		} else if ("Float".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FloatParameterType.type);
//			((FloatParameterType) actualParameter.getType())
//					.setValue(new Float(inputVal));
//		} else if ("Boolean".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(BooleanParameterType.type);
//			((BooleanParameterType) actualParameter.getType())
//					.setValue(new Boolean(inputVal));
//		} else if ("File".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FileParameterType.type);
//			((FileParameterType) actualParameter.getType()).setValue(inputVal);
//		} else if ("URI".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(URIParameterType.type);
//			((URIParameterType) actualParameter.getType()).setValue(inputVal);
//		} else if ("StringArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(StringArrayType.type);
//			Iterator iterator = Arrays.asList(
//					StringUtil.getElementsFromString(inputVal)).iterator();
//			int i = 0;
//			while (iterator.hasNext()) {
//				innerelement = (OMElement) iterator.next();
//				((StringArrayType) actualParameter.getType()).insertValue(i++,
//						innerelement.getText());
//			}
//		} else if ("DoubleArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(DoubleArrayType.type);
//			Iterator value = Arrays.asList(
//					StringUtil.getElementsFromString(inputVal)).iterator();
//			int i = 0;
//			while (value.hasNext()) {
//				innerelement = (OMElement) value.next();
//				((DoubleArrayType) actualParameter.getType()).insertValue(i++,
//						new Double(innerelement.getText()));
//			}
//		} else if ("IntegerArray"
//				.equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(IntegerArrayType.type);
//			Iterator value = Arrays.asList(
//					StringUtil.getElementsFromString(inputVal)).iterator();
//			int i = 0;
//			while (value.hasNext()) {
//				innerelement = (OMElement) value.next();
//				((IntegerArrayType) actualParameter.getType()).insertValue(i++,
//						new Integer(innerelement.getText()));
//			}
//		} else if ("FloatArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FloatArrayType.type);
//			Iterator value = Arrays.asList(
//					StringUtil.getElementsFromString(inputVal)).iterator();
//			int i = 0;
//			while (value.hasNext()) {
//				innerelement = (OMElement) value.next();
//				((FloatArrayType) actualParameter.getType()).insertValue(i++,
//						new Float(innerelement.getText()));
//			}
//		} else if ("BooleanArray"
//				.equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(BooleanArrayType.type);
//			Iterator value = Arrays.asList(
//					StringUtil.getElementsFromString(inputVal)).iterator();
//			int i = 0;
//			while (value.hasNext()) {
//				innerelement = (OMElement) value.next();
//				((BooleanArrayType) actualParameter.getType()).insertValue(i++,
//						new Boolean(innerelement.getText()));
//			}
//		} else if ("FileArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(FileArrayType.type);
//			Iterator value = Arrays.asList(
//					StringUtil.getElementsFromString(inputVal)).iterator();
//			int i = 0;
//			while (value.hasNext()) {
//				innerelement = (OMElement) value.next();
//				((FileArrayType) actualParameter.getType()).insertValue(i++,
//						innerelement.getText());
//			}
//		} else if ("URIArray".equals(parameter.getParameterType().getName())) {
//			actualParameter = new ActualParameter(URIArrayType.type);
//			Iterator value = Arrays.asList(
//					StringUtil.getElementsFromString(inputVal)).iterator();
//			int i = 0;
//			while (value.hasNext()) {
//				innerelement = (OMElement) value.next();
//				((URIArrayType) actualParameter.getType()).insertValue(i++,
//						innerelement.getText());
//			}
//		} else {
//			throw new GFacException(
//					"Input parameters are not configured properly ");
//		}
//		return actualParameter;
//	}

//	public static ApplicationJob createApplicationJob(
//			JobExecutionContext jobExecutionContext) {
//		ApplicationJob appJob = new ApplicationJob();
//		appJob.setExperimentId((String) jobExecutionContext
//				.getProperty(Constants.PROP_TOPIC));
//		appJob.setWorkflowExecutionId(appJob.getExperimentId());
//		appJob.setNodeId((String) jobExecutionContext
//				.getProperty(Constants.PROP_WORKFLOW_NODE_ID));
//		appJob.setServiceDescriptionId(jobExecutionContext
//				.getApplicationContext().getServiceDescription().getType()
//				.getName());
//		appJob.setHostDescriptionId(jobExecutionContext.getApplicationContext()
//				.getHostDescription().getType().getHostName());
//		appJob.setApplicationDescriptionId(jobExecutionContext
//				.getApplicationContext().getApplicationDeploymentDescription()
//				.getType().getApplicationName().getStringValue());
//		return appJob;
//	}

//	public static void updateApplicationJobStatusUpdateTime(
//			JobExecutionContext context, String jobId, Date statusUpdateTime) {
//		AiravataAPI airavataAPI = context.getGFacConfiguration()
//				.getAiravataAPI();
//		try {
//			airavataAPI.getProvenanceManager()
//					.updateApplicationJobStatusUpdateTime(jobId,
//							statusUpdateTime);
//		} catch (AiravataAPIInvocationException e) {
//			log.error("Error in updating application job status time "
//					+ statusUpdateTime.toString() + " for job Id " + jobId
//					+ "!!!", e);
//		}
//	}
//
//	public static void updateApplicationJobStatus(JobExecutionContext context,
//			String jobId, ApplicationJobStatus status, Date statusUpdateTime) {
//		AiravataAPI airavataAPI = context.getGFacConfiguration()
//				.getAiravataAPI();
//		try {
//			airavataAPI.getProvenanceManager().updateApplicationJobStatus(
//					jobId, status, statusUpdateTime);
//		} catch (AiravataAPIInvocationException e) {
//			log.error(
//					"Error in updating application job status "
//							+ status.toString() + " for job Id " + jobId
//							+ "!!!", e);
//		}
//	}

//	/**
//	 * Gets the job ids given experiment id.
//	 *
//	 * @param context
//	 *            The job execution context.
//	 * @param experimentId
//	 *            The experiment id.
//	 * @return List of job ids relevant to given experiment id.
//	 */
//	public static List<ApplicationJob> getJobIds(JobExecutionContext context,
//			String experimentId) {
//
//		AiravataAPI airavataAPI = context.getGFacConfiguration()
//				.getAiravataAPI();
//		try {
//			return airavataAPI.getProvenanceManager().getApplicationJobs(
//					experimentId, null, null);
//		} catch (AiravataAPIInvocationException e) {
//			log.error("Error retrieving application jobs for experiment id "
//					+ experimentId, e);
//		}
//
//		return new ArrayList<ApplicationJob>(0);
//	}

//	/**
//	 * Gets the job ids given experiment id and workflow id.
//	 *
//	 * @param context
//	 *            The job execution context.
//	 * @param experimentId
//	 *            The experiment id.
//	 * @param workflowId
//	 *            The workflow id
//	 * @return List of job ids relevant to given experiment id and workflow id.
//	 */
//	public static List<ApplicationJob> getJobIds(JobExecutionContext context,
//			String experimentId, String workflowId) {
//
//		AiravataAPI airavataAPI = context.getGFacConfiguration()
//				.getAiravataAPI();
//		try {
//			return airavataAPI.getProvenanceManager().getApplicationJobs(
//					experimentId, workflowId, null);
//		} catch (AiravataAPIInvocationException e) {
//			log.error("Error retrieving application jobs for experiment id "
//					+ experimentId, " workflow id " + workflowId, e);
//		}
//
//		return new ArrayList<ApplicationJob>(0);
//	}

//	/**
//	 * Gets the job ids given experiment id and workflow id.
//	 *
//	 * @param context
//	 *            The job execution context.
//	 * @param experimentId
//	 *            The experiment id.
//	 * @param workflowId
//	 *            The workflow id
//	 * @return List of job ids relevant to given experiment id and workflow id.
//	 */
//	public static List<ApplicationJob> getJobIds(JobExecutionContext context,
//			String experimentId, String workflowId, String nodeId) {
//
//		AiravataAPI airavataAPI = context.getGFacConfiguration()
//				.getAiravataAPI();
//		try {
//			return airavataAPI.getProvenanceManager().getApplicationJobs(
//					experimentId, workflowId, nodeId);
//		} catch (AiravataAPIInvocationException e) {
//			log.error("Error retrieving application jobs for experiment id "
//					+ experimentId, " workflow id " + workflowId, e);
//		}
//
//		return new ArrayList<ApplicationJob>(0);
//	}

	/*
	 * public static RequestData getRequestData(Properties
	 * configurationProperties) {
	 * 
	 * RequestData requestData = new RequestData();
	 * 
	 * requestData.setMyProxyServerUrl(configurationProperties.getProperty(Constants
	 * .MYPROXY_SERVER));
	 * requestData.setMyProxyUserName(configurationProperties.
	 * getProperty(Constants.MYPROXY_USER));
	 * requestData.setMyProxyPassword(configurationProperties
	 * .getProperty(Constants.MYPROXY_PASS));
	 * 
	 * int lifeTime; String sLife =
	 * configurationProperties.getProperty(Constants.MYPROXY_LIFE); if (sLife !=
	 * null) { lifeTime = Integer.parseInt(sLife);
	 * requestData.setMyProxyLifeTime(lifeTime); } else {
	 * log.info("The configuration does not specify a default life time"); }
	 * 
	 * 
	 * 
	 * }
	 */

//	public static void recordApplicationJob(JobExecutionContext context,
//			ApplicationJob job) {
//		AiravataAPI airavataAPI = context.getGFacConfiguration()
//				.getAiravataAPI();
//		try {
//			airavataAPI.getProvenanceManager().addApplicationJob(job);
//		} catch (AiravataAPIInvocationException e) {
//			log.error(
//					"Error in persisting application job data for application job "
//							+ job.getJobId() + "!!!", e);
//		}
//	}

	public static void saveJobStatus(JobExecutionContext jobExecutionContext,
			JobDetails details, JobState state) throws GFacException {
		try {
			Registry registry = jobExecutionContext.getRegistry();
			JobStatus status = new JobStatus();
			status.setJobState(state);
			details.setJobStatus(status);
			registry.add(ChildDataType.JOB_DETAIL, details,
					new CompositeIdentifier(jobExecutionContext.getTaskData()
							.getTaskID(), details.getJobID()));
		} catch (Exception e) {
			throw new GFacException("Error persisting job status"
					+ e.getLocalizedMessage(), e);
		}
	}

	public static void updateJobStatus(JobExecutionContext jobExecutionContext,
			JobDetails details, JobState state) throws GFacException {
		try {
			Registry registry = jobExecutionContext.getRegistry();
			JobStatus status = new JobStatus();
			status.setJobState(state);
			status.setTimeOfStateChange(Calendar.getInstance()
					.getTimeInMillis());
			details.setJobStatus(status);
			registry.update(
					org.apache.airavata.registry.cpi.RegistryModelType.JOB_DETAIL,
					details, details.getJobID());
		} catch (Exception e) {
			throw new GFacException("Error persisting job status"
					+ e.getLocalizedMessage(), e);
		}
	}

	public static void saveErrorDetails(
			JobExecutionContext jobExecutionContext, String errorMessage,
			CorrectiveAction action, ErrorCategory errorCatogory)
			throws GFacException {
		try {
			Registry registry = jobExecutionContext.getRegistry();
			ErrorDetails details = new ErrorDetails();
			details.setActualErrorMessage(errorMessage);
			details.setCorrectiveAction(action);
			details.setActionableGroup(ActionableGroup.GATEWAYS_ADMINS);
			details.setCreationTime(Calendar.getInstance().getTimeInMillis());
			details.setErrorCategory(errorCatogory);
			registry.add(ChildDataType.ERROR_DETAIL, details,
					jobExecutionContext.getTaskData().getTaskID());
		} catch (Exception e) {
			throw new GFacException("Error persisting job status"
					+ e.getLocalizedMessage(), e);
		}
	}

//	public static Map<String, Object> getInMessageContext(
//			List<DataObjectType> experimentData, Parameter[] parameters)
//			throws GFacException {
//		HashMap<String, Object> stringObjectHashMap = new HashMap<String, Object>();
//		Map<String, DataObjectType> map = new HashMap<String, DataObjectType>();
//		for (DataObjectType objectType : experimentData) {
//			map.put(objectType.getKey(), objectType);
//		}
//		for (int i = 0; i < parameters.length; i++) {
//			DataObjectType input = map.get(parameters[i].getParameterName());
//			if (input != null) {
//				DataType t = DataType.STRING;
//				String type = parameters[i].getParameterType().getType().toString().toUpperCase();
//				if (type.equals("STRING")){
//					t=DataType.STRING;
//				}else if (type.equals("INTEGER")){
//					t=DataType.INTEGER;
//				}else if (type.equals("FLOAT")){
//					//FIXME
//					t=DataType.INTEGER;
//				}else if (type.equals("URI")){
//					t=DataType.URI;
//				}
//				input.setType(t);
//				stringObjectHashMap
//						.put(parameters[i].getParameterName(), GFacUtils
//								.getInputActualParameter(parameters[i], input));
//			} else {
//				throw new GFacException(
//						"Error finding the parameter: parameter Name"
//								+ parameters[i].getParameterName());
//			}
//		}
//		return stringObjectHashMap;
//	}

    public static Map<String, Object> getInputParamMap(List<InputDataObjectType> experimentData) throws GFacException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (InputDataObjectType objectType : experimentData) {
            map.put(objectType.getName(), objectType);
        }
        return map;
    }
//
//	public static Map<String, Object> getOutMessageContext(
//			List<DataObjectType> experimentData, Parameter[] parameters)
//			throws GFacException {
//		HashMap<String, Object> stringObjectHashMap = new HashMap<String, Object>();
//		Map<String, DataObjectType> map = new HashMap<String, DataObjectType>();
//		for (DataObjectType objectType : experimentData) {
//			map.put(objectType.getKey(), objectType);
//		}
//		for (int i = 0; i < parameters.length; i++) {
//			DataObjectType output = map.get(parameters[i].getParameterName());
//			if (output==null){
//				output=new DataObjectType();
//				output.setKey(parameters[i].getParameterName());
//				output.setValue("");
//				String type = parameters[i].getParameterType().getType().toString().toUpperCase();
//				DataType t = DataType.STRING;
//				if (type.equals("STRING")){
//					t=DataType.STRING;
//				}else if (type.equals("INTEGER")){
//					t=DataType.INTEGER;
//				}else if (type.equals("FLOAT")){
//					//FIXME
//					t=DataType.INTEGER;
//				}else if (type.equals("URI")){
//					t=DataType.URI;
//				}
//				output.setType(t);
//			}
//			stringObjectHashMap
//					.put(parameters[i].getParameterName(), GFacUtils
//							.getInputActualParameter(parameters[i], output));
//		}
//		return stringObjectHashMap;
//	}

    public static Map<String, Object> getOuputParamMap(List<OutputDataObjectType> experimentData) throws GFacException {
        Map<String, Object> map = new HashMap<String, Object>();
        for (OutputDataObjectType objectType : experimentData) {
            map.put(objectType.getName(), objectType);
        }
        return map;
    }

	public static GfacExperimentState getZKExperimentState(ZooKeeper zk,
			JobExecutionContext jobExecutionContext)
			throws ApplicationSettingsException, KeeperException,
			InterruptedException {
		String expState = AiravataZKUtils.getExpState(zk, jobExecutionContext
				.getExperimentID(), jobExecutionContext.getTaskData()
				.getTaskID());
		return GfacExperimentState.findByValue(Integer.parseInt(expState));
	}

	public static int getZKExperimentStateValue(ZooKeeper zk,
			JobExecutionContext jobExecutionContext)
			throws ApplicationSettingsException, KeeperException,
			InterruptedException {
		String expState = AiravataZKUtils.getExpState(zk, jobExecutionContext
				.getExperimentID(), jobExecutionContext.getTaskData()
				.getTaskID());
		if (expState == null) {
			return -1;
		}
		return Integer.parseInt(expState);
	}

    public static int getZKExperimentStateValue(ZooKeeper zk,String fullPath)throws ApplicationSettingsException,
            KeeperException, InterruptedException {
        Stat exists = zk.exists(fullPath+File.separator+"state", false);
        if (exists != null) {
            return Integer.parseInt(new String(zk.getData(fullPath+File.separator+"state", false, exists)));
        }
        return -1;
    }

	public static boolean createPluginZnode(ZooKeeper zk,
			JobExecutionContext jobExecutionContext, String className)
			throws ApplicationSettingsException, KeeperException,
			InterruptedException {
		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
				jobExecutionContext.getExperimentID(), jobExecutionContext
						.getTaskData().getTaskID(), className);
		Stat exists = zk.exists(expState, false);
		if (exists == null) {
			zk.create(expState, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);

			zk.create(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0],
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} else {
			exists = zk.exists(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
			if (exists == null) {
				zk.create(expState + File.separator
						+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
						new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
		}

		exists = zk.exists(expState + File.separator
				+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
		if (exists != null) {
			zk.setData(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
					String.valueOf(GfacPluginState.INVOKING.getValue())
							.getBytes(), exists.getVersion());
		}
		return true;
	}

	public static boolean createPluginZnode(ZooKeeper zk,
			JobExecutionContext jobExecutionContext, String className,
			GfacPluginState state) throws ApplicationSettingsException,
			KeeperException, InterruptedException {
		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
				jobExecutionContext.getExperimentID(), jobExecutionContext
						.getTaskData().getTaskID(), className);
		Stat exists = zk.exists(expState, false);
		if (exists == null) {
			zk.create(expState, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
					CreateMode.PERSISTENT);

			zk.create(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, new byte[0],
					ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} else {
			exists = zk.exists(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
			if (exists == null) {
				zk.create(expState + File.separator
						+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
						new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
			}
		}

		exists = zk.exists(expState + File.separator
				+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
		if (exists != null) {
			zk.setData(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
					String.valueOf(state.getValue()).getBytes(),
					exists.getVersion());
		}
		return true;
	}

	public static boolean updatePluginState(ZooKeeper zk,
			JobExecutionContext jobExecutionContext, String className,
			GfacPluginState state) throws ApplicationSettingsException,
			KeeperException, InterruptedException {
		String expState = AiravataZKUtils.getExpZnodeHandlerPath(
				jobExecutionContext.getExperimentID(), jobExecutionContext
						.getTaskData().getTaskID(), className);

		Stat exists = zk.exists(expState + File.separator
				+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
		if (exists != null) {
			zk.setData(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE,
					String.valueOf(state.getValue()).getBytes(),
					exists.getVersion());
		} else {
			createPluginZnode(zk, jobExecutionContext, className, state);
		}
		return true;
	}

	public static String getPluginState(ZooKeeper zk,
			JobExecutionContext jobExecutionContext, String className) {
		try {
			String expState = AiravataZKUtils.getExpZnodeHandlerPath(
					jobExecutionContext.getExperimentID(), jobExecutionContext
							.getTaskData().getTaskID(), className);

			Stat exists = zk.exists(expState + File.separator
					+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false);
			if (exists != null) {
				return new String(zk.getData(expState + File.separator
						+ AiravataZKUtils.ZK_EXPERIMENT_STATE_NODE, false,
						exists));
			}
			return null; // if the node doesn't exist or any other error we
							// return false
		} catch (Exception e) {
			log.error("Error occured while getting zk node status", e);
			return null;
		}
	}

	// This method is dangerous because of moving the experiment data
	public static boolean createExperimentEntryForRPC(String experimentID,
													  String taskID, ZooKeeper zk, String experimentNode,
													  String pickedChild, String tokenId) throws KeeperException,
			InterruptedException {
		String experimentPath = experimentNode + File.separator + pickedChild;
		String newExpNode = experimentPath + File.separator + experimentID
				+ "+" + taskID;
        Stat exists1 = zk.exists(newExpNode, false);
        String experimentEntry = GFacUtils.findExperimentEntry(experimentID, taskID, zk);
        String foundExperimentPath = null;
		if (exists1 == null && experimentEntry == null) {  // this means this is a very new experiment
			List<String> runningGfacNodeNames = AiravataZKUtils
					.getAllGfacNodeNames(zk); // here we take old gfac servers
												// too
			for (String gfacServerNode : runningGfacNodeNames) {
				if (!gfacServerNode.equals(pickedChild)) {
					foundExperimentPath = experimentNode + File.separator
							+ gfacServerNode + File.separator + experimentID
							+ "+" + taskID;
					exists1 = zk.exists(foundExperimentPath, false);
					if (exists1 != null) { // when the experiment is found we
											// break the loop
						break;
					}
				}
			}
			if (exists1 == null) { // OK this is a pretty new experiment so we
									// are going to create a new node
				log.info("This is a new Job, so creating all the experiment docs from the scratch");
				zk.create(newExpNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);

				Stat expParent = zk.exists(newExpNode, false);
				if (tokenId != null && expParent != null) {
					zk.setData(newExpNode, tokenId.getBytes(),
							expParent.getVersion());
				}
				zk.create(newExpNode + File.separator + "state", String
						.valueOf(GfacExperimentState.LAUNCHED.getValue())
						.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
                zk.create(newExpNode + File.separator + "operation","submit".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);

			} else {
				// ohhh this node exists in some other failed gfac folder, we
				// have to move it to this gfac experiment list,safely
				log.info("This is an old Job, so copying data from old experiment location");
				zk.create(newExpNode,
						zk.getData(foundExperimentPath, false, exists1),
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

				List<String> children = zk.getChildren(foundExperimentPath,
						false);
				for (String childNode1 : children) {
					String level1 = foundExperimentPath + File.separator
							+ childNode1;
					Stat exists2 = zk.exists(level1, false); // no need to check
																// exists
					String newLeve1 = newExpNode + File.separator + childNode1;
					log.info("Creating new znode: " + newLeve1); // these has to
																	// be info
																	// logs
					zk.create(newLeve1, zk.getData(level1, false, exists2),
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					for (String childNode2 : zk.getChildren(level1, false)) {
						String level2 = level1 + File.separator + childNode2;
						Stat exists3 = zk.exists(level2, false); // no need to
																	// check
																	// exists
						String newLeve2 = newLeve1 + File.separator
								+ childNode2;
						log.info("Creating new znode: " + newLeve2);
						zk.create(newLeve2, zk.getData(level2, false, exists3),
								ZooDefs.Ids.OPEN_ACL_UNSAFE,
								CreateMode.PERSISTENT);
					}
				}
				// After all the files are successfully transfered we delete the
				// old experiment,otherwise we do
				// not delete a single file
				log.info("After a successful copying of experiment data for an old experiment we delete the old data");
				log.info("Deleting experiment data: " + foundExperimentPath);
				ZKUtil.deleteRecursive(zk, foundExperimentPath);
			}
		}else if(experimentEntry != null && GFacUtils.isCancelled(experimentID,taskID,zk) ){
            // this happens when a cancel request comes to a differnt gfac node, in this case we do not move gfac experiment
            // node to gfac node specific location, because original request execution will fail with errors
            log.error("This experiment is already cancelled and its already executing the cancel operation so cannot submit again !");
            return false;
        } else {
            log.error("ExperimentID: " + experimentID + " taskID: " + taskID
                    + " is already running by this Gfac instance");
            List<String> runningGfacNodeNames = AiravataZKUtils
                    .getAllGfacNodeNames(zk); // here we take old gfac servers
            // too
            for (String gfacServerNode : runningGfacNodeNames) {
                if (!gfacServerNode.equals(pickedChild)) {
                    foundExperimentPath = experimentNode + File.separator
                            + gfacServerNode + File.separator + experimentID
                            + "+" + taskID;
                    break;
                }
            }
            ZKUtil.deleteRecursive(zk, foundExperimentPath);
        }
        return true;
	}

	// This method is dangerous because of moving the experiment data
	public static boolean createExperimentEntryForPassive(String experimentID,
													  String taskID, ZooKeeper zk, String experimentNode,
													  String pickedChild, String tokenId,long deliveryTag) throws KeeperException,
			InterruptedException, ApplicationSettingsException {
		String experimentPath = experimentNode + File.separator + pickedChild;
		String newExpNode = experimentPath + File.separator + experimentID
				+ "+" + taskID;
		Stat exists1 = zk.exists(newExpNode, false);
		String experimentEntry = GFacUtils.findExperimentEntry(experimentID, taskID, zk);
		if (exists1 == null && experimentEntry == null) {  // this means this is a very new experiment
				// are going to create a new node
				log.info("This is a new Job, so creating all the experiment docs from the scratch");
				Stat expParent = zk.exists(newExpNode, false);
				zk.create(newExpNode, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);

				if (tokenId != null && expParent != null) {
					zk.setData(newExpNode, tokenId.getBytes(),
							expParent.getVersion());
				}
				String s = zk.create(newExpNode + File.separator + "state", String
								.valueOf(GfacExperimentState.LAUNCHED.getValue())
								.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				String s1 = zk.create(newExpNode + File.separator + "operation", "submit".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
						CreateMode.PERSISTENT);
				zk.exists(s1, true);// we want to know when this node get deleted
				String s2 = zk.create(newExpNode + DELIVERY_TAG_POSTFIX, longToBytes(deliveryTag), ZooDefs.Ids.OPEN_ACL_UNSAFE,  // here we store the value of delivery message
						CreateMode.PERSISTENT);
		}else if(experimentEntry != null && GFacUtils.isCancelled(experimentID,taskID,zk) ){
			// this happens when a cancel request comes to a differnt gfac node, in this case we do not move gfac experiment
			// node to gfac node specific location, because original request execution will fail with errors
			log.error("This experiment is already cancelled and its already executing the cancel operation so cannot submit again !");
			return false;
		} else if(experimentEntry != null && !GFacUtils.isCancelled(experimentID,taskID,zk)){
			if(ServerSettings.isGFacPassiveMode()){
				log.error("ExperimentID: " + experimentID + " taskID: " + taskID
						+ " was running by some Gfac instance,but it failed");
				log.info("This is an old Job, so copying data from old experiment location");
				zk.create(newExpNode,
						zk.getData(experimentEntry, false, exists1),
						ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

				List<String> children = zk.getChildren(experimentEntry,
						false);
				for (String childNode1 : children) {
					String level1 = experimentEntry + File.separator
							+ childNode1;
					Stat exists2 = zk.exists(level1, false); // no need to check exists
					String newLeve1 = newExpNode + File.separator + childNode1;
					log.info("Creating new znode: " + newLeve1); // these has to be info logs
					zk.create(newLeve1, zk.getData(level1, false, exists2),
							ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					for (String childNode2 : zk.getChildren(level1, false)) {
						String level2 = level1 + File.separator + childNode2;
						Stat exists3 = zk.exists(level2, false); // no need to check exists
						String newLeve2 = newLeve1 + File.separator
								+ childNode2;
						log.info("Creating new znode: " + newLeve2);
						zk.create(newLeve2, zk.getData(level2, false, exists3),
								ZooDefs.Ids.OPEN_ACL_UNSAFE,
								CreateMode.PERSISTENT);
					}
				}
				// After all the files are successfully transfered we delete the
				// old experiment,otherwise we do
				// not delete a single file
				log.info("After a successful copying of experiment data for an old experiment we delete the old data");
				log.info("Deleting experiment data: " + experimentEntry);
				ZKUtil.deleteRecursive(zk, experimentEntry);
			}else {
				log.error("ExperimentID: " + experimentID + " taskID: " + taskID
						+ " is already running by this Gfac instance");
				List<String> runningGfacNodeNames = AiravataZKUtils
						.getAllGfacNodeNames(zk); // here we take old gfac servers
				// too
				for (String gfacServerNode : runningGfacNodeNames) {
					if (!gfacServerNode.equals(pickedChild)) {
						experimentEntry = experimentNode + File.separator
								+ gfacServerNode + File.separator + experimentID
								+ "+" + taskID;
						break;
					}
				}
				if(experimentEntry!=null) {
					ZKUtil.deleteRecursive(zk, experimentEntry);
				}
			}

		}
		return true;
	}

	/**
	 * This will return a value if the server is down because we iterate through exisiting experiment nodes, not
	 * through gfac-server nodes
	 * @param experimentID
	 * @param taskID
	 * @param zk
	 * @return
	 * @throws KeeperException
	 * @throws InterruptedException
	 */
    public static String findExperimentEntry(String experimentID,
                                                String taskID, ZooKeeper zk
                                                ) throws KeeperException,
            InterruptedException {
        String experimentNode = ServerSettings.getSetting(org.apache.airavata.common.utils.Constants.ZOOKEEPER_GFAC_EXPERIMENT_NODE, "/gfac-experiments");
        List<String> children = zk.getChildren(experimentNode, false);
        for(String pickedChild:children) {
            String experimentPath = experimentNode + File.separator + pickedChild;
            String newExpNode = experimentPath + File.separator + experimentID
                    + "+" + taskID;
            Stat exists = zk.exists(newExpNode, false);
            if(exists == null){
                continue;
            }else{
                return newExpNode;
            }
        }
        return null;
    }

    public static void setExperimentCancel(String experimentId,String taskId,ZooKeeper zk)throws KeeperException,
            InterruptedException {
        String experimentEntry = GFacUtils.findExperimentEntry(experimentId, taskId, zk);
        if(experimentEntry == null){
            log.error("Cannot find the experiment Entry, so cancel operation cannot be performed !!!");
        }else {
            Stat operation = zk.exists(experimentEntry + File.separator + "operation", false);
            if (operation == null) { // if there is no entry, this will come when a user immediately cancel a job
                zk.create(experimentEntry + File.separator + "operation", "cancel".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            } else { // if user submit the job to gfac then cancel during execution
                zk.setData(experimentEntry + File.separator + "operation", "cancel".getBytes(), operation.getVersion());
            }
        }

    }
    public static boolean isCancelled(String experimentID,
                                             String taskID, ZooKeeper zk
    ) throws KeeperException,
            InterruptedException {
        String experimentEntry = GFacUtils.findExperimentEntry(experimentID, taskID, zk);
        if(experimentEntry == null){
            return false;
        }else {
            Stat exists = zk.exists(experimentEntry, false);
            if (exists != null) {
                String operation = new String(zk.getData(experimentEntry+File.separator+"operation", false, exists));
                if ("cancel".equals(operation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void savePluginData(JobExecutionContext jobExecutionContext,
			StringBuffer data, String className) throws GFacHandlerException {
		try {
			ZooKeeper zk = jobExecutionContext.getZk();
			if (zk != null) {
				String expZnodeHandlerPath = AiravataZKUtils
						.getExpZnodeHandlerPath(
								jobExecutionContext.getExperimentID(),
								jobExecutionContext.getTaskData().getTaskID(),
								className);
				Stat exists = zk.exists(expZnodeHandlerPath, false);
				zk.setData(expZnodeHandlerPath, data.toString().getBytes(),
						exists.getVersion());
			}
		} catch (Exception e) {
			throw new GFacHandlerException(e);
		}
	}

	public static long getDeliveryTag(String experimentID,
									  String taskID, ZooKeeper zk, String experimentNode,
									  String pickedChild) throws KeeperException, InterruptedException,GFacException {
		String experimentPath = experimentNode + File.separator + pickedChild;
		String deliveryTagPath = experimentPath + File.separator + experimentID
				+ "+" + taskID + DELIVERY_TAG_POSTFIX;
		Stat exists = zk.exists(deliveryTagPath, false);
		if(exists==null) {
			throw new GFacException("Cannot find delivery Tag for this experiment");
		}
		return bytesToLong(zk.getData(deliveryTagPath, false, exists));
	}
	public static String getPluginData(JobExecutionContext jobExecutionContext,
			String className) throws ApplicationSettingsException,
			KeeperException, InterruptedException {
		ZooKeeper zk = jobExecutionContext.getZk();
		if (zk != null) {
			String expZnodeHandlerPath = AiravataZKUtils
					.getExpZnodeHandlerPath(
							jobExecutionContext.getExperimentID(),
							jobExecutionContext.getTaskData().getTaskID(),
							className);
			Stat exists = zk.exists(expZnodeHandlerPath, false);
			return new String(jobExecutionContext.getZk().getData(
					expZnodeHandlerPath, false, exists));
		}
		return null;
	}

	public static CredentialReader getCredentialReader()
			throws ApplicationSettingsException, IllegalAccessException,
			InstantiationException {
		try{
		String jdbcUrl = ServerSettings.getCredentialStoreDBURL();
		String jdbcUsr = ServerSettings.getCredentialStoreDBUser();
		String jdbcPass = ServerSettings.getCredentialStoreDBPassword();
		String driver = ServerSettings.getCredentialStoreDBDriver();
		return new CredentialReaderImpl(new DBUtil(jdbcUrl, jdbcUsr, jdbcPass,
				driver));
		}catch(ClassNotFoundException e){
			log.error("Not able to find driver: " + e.getLocalizedMessage());
			return null;	
		}
	}

    public static LOCALSubmission getLocalJobSubmission (String submissionId) throws AppCatalogException{
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getLocalJobSubmission(submissionId);
        }catch (Exception e){
            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static UnicoreJobSubmission getUnicoreJobSubmission (String submissionId) throws AppCatalogException{
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getUNICOREJobSubmission(submissionId);
        }catch (Exception e){
            String errorMsg = "Error while retrieving UNICORE job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static GlobusJobSubmission getGlobusJobSubmission (String submissionId) throws AppCatalogException{
        return null;
//        try {
//            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
//            return appCatalog.getComputeResource().getGlobus(submissionId);
//        }catch (Exception e){
//            String errorMsg = "Error while retrieving local job submission with submission id : " + submissionId;
//            log.error(errorMsg, e);
//            throw new AppCatalogException(errorMsg, e);
//        }
    }

    public static SSHJobSubmission getSSHJobSubmission (String submissionId) throws AppCatalogException{
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getSSHJobSubmission(submissionId);
        }catch (Exception e){
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    public static CloudJobSubmission getCloudJobSubmission (String submissionId) throws AppCatalogException{
        try {
            AppCatalog appCatalog = AppCatalogFactory.getAppCatalog();
            return appCatalog.getComputeResource().getCloudJobSubmission(submissionId);
        }catch (Exception e){
            String errorMsg = "Error while retrieving SSH job submission with submission id : " + submissionId;
            log.error(errorMsg, e);
            throw new AppCatalogException(errorMsg, e);
        }
    }

    /**
     * To convert list to separated value
     * @param listOfStrings
     * @param separator
     * @return
     */
    public static  String listToCsv(List<String> listOfStrings, char separator) {
        StringBuilder sb = new StringBuilder();

        // all but last
        for(int i = 0; i < listOfStrings.size() - 1 ; i++) {
            sb.append(listOfStrings.get(i));
            sb.append(separator);
        }

        // last string, no separator
        if(listOfStrings.size() > 0){
            sb.append(listOfStrings.get(listOfStrings.size()-1));
        }

        return sb.toString();
    }

	public static byte[] longToBytes(long x) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(x);
		return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.put(bytes);
		buffer.flip();//need flip
		return buffer.getLong();
	}
}
