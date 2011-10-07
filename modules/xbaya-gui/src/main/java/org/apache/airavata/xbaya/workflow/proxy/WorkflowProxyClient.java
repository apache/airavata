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

package org.apache.airavata.xbaya.workflow.proxy;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.event.Event;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.apache.airavata.xbaya.xregistry.XRegistryAccesser;
import org.apache.airavata.xbaya.xsd.GFacSimpleTypesXSD;
import org.apache.airavata.xbaya.xsd.LeadContextHeaderXSD;
import org.apache.airavata.xbaya.xsd.LeadCrosscutXSD;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.gpel.client.GcInstance;
import org.gpel.client.GcSearchList;
import org.gpel.client.GcSearchResult;
import org.gpel.model.GpelProcess;
import org.ietf.jgss.GSSCredential;
import org.ogce.xregistry.client.XRegistryClient;
import org.ogce.xregistry.utils.XRegistryClientException;
import org.xmlpull.infoset.XmlBuilderException;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xregistry.generated.ResourceData;
import xsul.XsulException;
import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.xbeans_util.XBeansUtil;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul5.XmlConstants;
import xsul5.wsdl.WsdlDefinitions;
import edu.indiana.extreme.weps.DeploymentDocumentsType;
import edu.indiana.extreme.weps.DeploymentInformationDocument;
import edu.indiana.extreme.weps.ProcessNameDocument;
import edu.indiana.extreme.weps.XMLFile;

public class WorkflowProxyClient extends WorkflowClient {

	private GSSCredential gssCredential;

	private XRegistryClient xregistryCient;
	private String xregistryURL;
	private WsdlDefinitions wsdlDefinitions;
	private XBayaEngine xbayaEngine;

	/**
	 * Constructs a WorkflowProxyClient.
	 */
	public WorkflowProxyClient() {
		// Nothing
		sendSafeEvent(new Event(Event.Type.GPEL_ENGINE_CONNECTED));
	}

	/**
	 * Constructs a WorkflowProxyClient.
	 * 
	 * @param engineURL
	 * @param xregistryURL
	 * @param credential
	 * @throws WorkflowProxyException
	 */
	public WorkflowProxyClient(URI engineURL, String xregistryURL,
			UserX509Credential credential) throws WorkflowEngineException {
		// TODO : Xregistry URL can be null. Make sure we check that and set a
		// default location to that.
		this();
		this.engineURL = engineURL;
		this.xregistryURL = xregistryURL;
		this.gssCredential = credential.getGssCredential();
	}

	public void setXRegistryUrl(URI xRegistryURL) {
		if (xRegistryURL != null) {
			this.xregistryURL = xRegistryURL.toString();
		}
	}

	/**
	 * @param engineURL
	 * @throws WorkflowProxyException
	 */
	public void setEngineURL(URI engineURL) throws WorkflowEngineException {
		super.setEngineURL(engineURL);
		sendSafeEvent(new Event(Event.Type.GPEL_ENGINE_CONNECTED));
		// connect();
	}

	/**
	 * @param gssCredential
	 * @throws WorkflowProxyException
	 */
	public void setSecurityInformation(GSSCredential gssCredential)
			throws WorkflowProxyException {
		this.gssCredential = gssCredential;
		// connect();
	}

	/**
	 * @param workflow
	 * @param redeploy
	 * @return The workflow template ID.
	 * @throws WorkflowProxyException
	 */
	public synchronized URI deploy(Workflow workflow, boolean redeploy)
			throws WorkflowProxyException {

		try {
			// count++;

			// adding workflow in to Xregistry

			WSIFAsyncResponsesCorrelator correlator;
			correlator = null;

			// pass some headers
			LeadContextHeader leadContext = getLeadContextHeader();
			// URI uri = new File("/u/cherath/Desktop/WEPSService.xml").toURI();
			// WSIFClient wclient = WSIFRuntime.newClient(uri.toString())
			// .addHandler(
			// new StickySoapHeaderHandler("use-lead-header",
			// leadContext)).useAsyncMessaging(correlator)
			// .setAsyncResponseTimeoutInMs(33000L); // to simplify testing

			xsul5.wsdl.WsdlResolver.getInstance().loadWsdl(engineURL);

			// WsdlService proxyService =
			// WSDLUtil.getfirst(proxyWSDL.services());
			// WsdlPort proxyPort = WSDLUtil.getfirst(proxyService.ports());
			// org.xmlpull.infoset.XmlElement address =
			// proxyPort.xml().element("address");
			// XmlAttribute location = address.attribute("location");
			// address.removeAttribute(location);
			// address.setAttributeValue("location",
			// engineURL.toString().substring(0,
			// engineURL.toString().indexOf("?wsdl")));

			// WSIFService service =
			// WSIFServiceFactory.newInstance().getService(
			// WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(proxyWSDL) );
			// WSIFPort port = service.getPort();
			// WSIFClient wclient = WSIFRuntime.getDefault().newClientFor(port);

			WSIFClient wclient = WSIFRuntime
					.newClient(engineURL.toString())
					.addHandler(
							new StickySoapHeaderHandler("use-lead-header",
									leadContext)).useAsyncMessaging(correlator)
					.setAsyncResponseTimeoutInMs(33000L); // to simplify testing
			// set to just few
			// seconds

			WSIFPort port = wclient.getPort();
			WSIFOperation operation = port.createOperation("deploy");

			WSIFMessage outputMessage = operation.createOutputMessage();
			WSIFMessage faultMessage = operation.createFaultMessage();
			XmlElement inputMessageElement = xmlObjectToEl(getDeploymentPayload(
					workflow, this.xbayaEngine.getConfiguration().getDSCURL()));
			WSIFMessage inputMessage = new WSIFMessageElement(
					inputMessageElement);

			boolean success = operation.executeRequestResponseOperation(
					inputMessage, outputMessage, faultMessage);

			if (success) {
			} else {
				throw new XsulException(faultMessage.toString());

			}

		} catch (IOException e) {
			throw new WorkflowProxyException(e);

		} catch (XmlBuilderException e) {
			throw new WorkflowProxyException(e);
		} catch (GraphException e) {
			throw new WorkflowProxyException(e);
		} catch (ComponentException e) {
			throw new WorkflowProxyException(e);
		}
		XBayaConfiguration configuration = this.xbayaEngine.getConfiguration();
		WsdlDefinitions workflowWSDL;
		try {
			workflowWSDL = workflow.getOdeWorkflowWSDL(
					configuration.getDSCURL(), configuration.getODEURL());
		} catch (Exception e) {
			// shouldnt happen cos we have already called this once
			throw new XBayaRuntimeException(e);
		}
		org.xmlpull.infoset.XmlElement service = workflowWSDL.xml().element(
				null, "service");
		org.xmlpull.infoset.XmlElement port = service.element(null, "port");
		org.xmlpull.infoset.XmlElement address = port.element(null, "address");
		String location = address.attributeValue("location");

		URI ret = null;
		try {
			ret = new URI(location + "?wsdl");
		} catch (URISyntaxException e) {
			throw new XBayaRuntimeException(e);
		}
		return ret;

	}

	private XmlElement xmlObjectToEl(XmlObject outgoingXmlObj)
			throws IOException {
		String outgoingXmlAsString = outgoingXmlObj.xmlText();
		return XmlInfosetBuilder.newInstance().parseFragmentFromReader(
				new StringReader(outgoingXmlAsString));
	}

	private DeploymentInformationDocument getDeploymentPayload(
			Workflow workflow, URI dscURI) throws WorkflowProxyException,
			XmlBuilderException, GraphException, ComponentException {
		try {
			String workflowName = workflow.getName();
			URI templateId = workflow.getUniqueWorkflowName();
			XBayaConfiguration configuration = this.xbayaEngine
					.getConfiguration();
			GpelProcess gpelProcess = null;
			String processString = null;

			gpelProcess = workflow.getOdeProcess(dscURI,
					configuration.getODEURL());
			processString = gpelProcess.xmlStringPretty();

			workflow.getImage();
			WsdlDefinitions workflowWSDL = workflow.getOdeWorkflowWSDL(dscURI,
					configuration.getODEURL());

			// do some error checking here
			if (workflowName == null || "".equals(workflowName)) {
				throw new IllegalStateException("No workflow name given");
			}

			DeploymentInformationDocument document = DeploymentInformationDocument.Factory
					.newInstance();
			DeploymentInformationDocument.DeploymentInformation deploymentInformation = DeploymentInformationDocument.DeploymentInformation.Factory
					.newInstance();
			DeploymentDocumentsType documentsType = DeploymentDocumentsType.Factory
					.newInstance();

			// setting the process name
			deploymentInformation.setProcessName(StringUtil
					.convertToJavaIdentifier(workflowName));

			// setting workflow template id
			if (templateId != null) {
				deploymentInformation.setTemplateId(templateId.toString());
			}

			// setting process GPEL

			XmlObject gpelProcessXmlObject = XBeansUtil
					.xmlElementToXmlObject(processString);
			documentsType.setBPEL(gpelProcessXmlObject);

			// setting deployment descriptors
			documentsType.setDeploymentDescriptor(XmlObject.Factory
					.parse(XmlConstants.BUILDER
							.serializeToStringPretty(workflow
									.getODEDeploymentDescriptor(dscURI,
											configuration.getODEURL()))));

			XMLFile processWSDL = XMLFile.Factory.newInstance();
			processWSDL.setFileName(workflowWSDL.xml().attributeValue("name")
					+ ".wsdl");
			processWSDL.setContent(XBeansUtil
					.xmlElementToXmlObject(workflowWSDL.xmlStringPretty()));
			documentsType.setProcessWSDL(processWSDL);

			XMLFile serviceWSDL;
			Map<String, WsdlDefinitions> wsdlMap = workflow.getOdeServiceWSDLs(
					dscURI, configuration.getODEURL());
			XMLFile[] serviceWSDLs = new XMLFile[wsdlMap.size() + 3];
			int index = 0;
			for (String id : wsdlMap.keySet()) {
				WsdlDefinitions wsdl = wsdlMap.get(id);
				serviceWSDL = XMLFile.Factory.newInstance();
				serviceWSDL.setFileName(wsdl.xml().attributeValue("name")
						+ ".wsdl");
				serviceWSDL.setContent(XBeansUtil.xmlElementToXmlObject(wsdl
						.xmlStringPretty()));
				serviceWSDLs[index++] = serviceWSDL;
			}

			// add the xsds
			// crosscutt
			XMLFile crossCutXsd = XMLFile.Factory.newInstance();
			crossCutXsd.setFileName("lead-crosscut-parameters.xsd");
			crossCutXsd.setContent(XBeansUtil
					.xmlElementToXmlObject(LeadCrosscutXSD.getXml()));
			serviceWSDLs[index++] = crossCutXsd;

			// gfac-simple-types.xsd

			XMLFile gfacXsd = XMLFile.Factory.newInstance();
			gfacXsd.setFileName("gfac-simple-types.xsd");
			gfacXsd.setContent(XBeansUtil
					.xmlElementToXmlObject(GFacSimpleTypesXSD.getXml()));
			serviceWSDLs[index++] = gfacXsd;

			// fileidtype
			XMLFile lch = XMLFile.Factory.newInstance();
			lch.setFileName("lead-context.xsd");
			lch.setContent(XBeansUtil
					.xmlElementToXmlObject(LeadContextHeaderXSD.getXml()));
			serviceWSDLs[index++] = lch;

			documentsType.setServiceWSDLsArray(serviceWSDLs);

			workflow.getGraph();

			deploymentInformation.setDeploymentDocuments(documentsType);
			document.setDeploymentInformation(deploymentInformation);

			return document;
		} catch (XmlException e) {
			throw new WorkflowProxyException(e);
		}

	}

	/**
	 * @param workflow
	 *            The workflow to instantiate.
	 * @param dscURL
	 *            The URL of the DSC.
	 * @param name
	 *            The name that becomes a part of the workflow instance name.
	 * @return The wsdl of the workflow
	 * @throws WorkflowProxyException
	 * @throws ComponentException
	 * @throws GraphException
	 */
	public synchronized GcInstance instantiate(Workflow workflow, URI dscURL,
			String name) throws WorkflowEngineException, ComponentException,
			GraphException {

		URI templateID = workflow.getGPELTemplateID();
		if (templateID == null) {
			throw new IllegalStateException(
					"The workflow has not been deployed.");
		}

		try {

			WSIFAsyncResponsesCorrelator correlator;
			correlator = null;

			// pass some headers
			LeadContextHeader leadContext = getLeadContextHeader();

			WSIFClient wclient = WSIFRuntime
					.newClient(engineURL.toString())
					.addHandler(
							new StickySoapHeaderHandler("use-lead-header",
									leadContext)).useAsyncMessaging(correlator)
					.setAsyncResponseTimeoutInMs(33000L); // to simplify testing
			// set to just few
			// seconds

			WSIFPort port = wclient.getPort();
			WSIFOperation operation = port.createOperation("createInstance");

			WSIFMessage outputMessage = operation.createOutputMessage();
			WSIFMessage faultMessage = operation.createFaultMessage();

			ProcessNameDocument processNameDocument = ProcessNameDocument.Factory
					.newInstance();
			// TODO : do we use template id or process name here?
			processNameDocument.setProcessName(workflow.getName());

			XmlElement inputMessageElement = xmlObjectToEl(processNameDocument);

			WSIFMessage inputMessage = new WSIFMessageElement(
					inputMessageElement);

			boolean success = operation.executeRequestResponseOperation(
					inputMessage, outputMessage, faultMessage);

			XmlElement result;
			if (success) {
				result = (XmlElement) outputMessage;
			} else {
				result = (XmlElement) faultMessage;
			}

			wsdlDefinitions = new WsdlDefinitions(
					XMLUtil.xmlElementToString(result));

			// this is tricky here. This method requires us to return a
			// GcInstance and will call
			// start method with GcInstance as the argument to get
			// wsdlDefinitions.
			// With workflow proxy client, we do get wsdldefinitions from the
			// first invocation itself and we do not need
			// to have a GcInstance. So let's create our own GcInstance extended
			// from GcInstance, store the wsdldefinitions
			// inside that. When we get "start" call next time, let's retrieve
			// these wsdldefinitions from that GcInstance
			// object and return it to the client
			return new ProxyGcInstance(wsdlDefinitions);
		} catch (IOException e) {
			e.printStackTrace();

		}

		return null;
	}

	@Deprecated
	public GcInstance instantiate(Workflow workflow,
			Map<String, WsdlDefinitions> wsdlMap)
			throws WorkflowEngineException {
		return null; // To change body of implemented methods use File |
		// Settings | File Templates.
	}

	private LeadContextHeader getLeadContextHeader() {
		LeadContextHeaderHelper helper = new LeadContextHeaderHelper();
		helper.setXBayaConfiguration(new XBayaConfiguration());
		LeadContextHeader leadContext = helper.getLeadContextHeader();
		leadContext.setWorkflowId(URI
				.create("http://host/2005/11/09/workflowinstace"));
		leadContext.setNodeId("decoder1");
		leadContext.setTimeStep("5");
		leadContext.setServiceId("decoder-instance-10");
		return leadContext;
	}

	/**
	 * Loads a workflow with s specified workflow template ID.
	 * 
	 * @param templateID
	 *            The workflow template ID.
	 * @return The workflow loaded
	 * @throws GraphException
	 * @throws WorkflowProxyException
	 * @throws org.apache.airavata.xbaya.component.ComponentException
	 * 
	 */
	public Workflow load(URI templateID, WorkflowType workflowType)
			throws GraphException, WorkflowProxyException, ComponentException {

		Workflow workflow = null;
		String templateIDString = templateID.toString();

		// workflow template id is of the format
		// urn:uuid:<workflow_name>:date_as_long. Trying to extract
		// the workflow name from the template id now

		QName workflowQName;
		if (templateIDString.indexOf("urn:uuid") > -1) {
			int secondColonLocation = templateIDString.indexOf(":",
					templateIDString.indexOf(":") + 1);
			int thirdColonLocation = templateIDString.indexOf(":",
					secondColonLocation + 1);

			workflowQName = new QName(templateIDString,
					templateIDString.substring(secondColonLocation + 1,
							thirdColonLocation));
		} else {
			workflowQName = new QName(templateIDString);
		}

		XRegistryAccesser xregistryAccesser = new XRegistryAccesser(
				this.xbayaEngine);
		String templateAsString = xregistryAccesser.getWorkflow(workflowQName)
				.toString();

		workflow = new Workflow(XMLUtil.stringToXmlElement(templateAsString));

		return workflow;
	}

	public void setUserX509Credential(UserX509Credential userX509Credential)
			throws WorkflowEngineException {
		this.gssCredential = userX509Credential.getGssCredential();
	}

	/**
	 * Returns the List of GcSearchResult.
	 * 
	 * @param maxNum
	 *            The maximum number of results
	 * @return The List of GcSearchResult.
	 * @throws WorkflowProxyException
	 */
	@SuppressWarnings("boxing")
	public synchronized GcSearchList list(int maxNum, WorkflowType type)
			throws WorkflowProxyException {

		try {
			// TODO return the ones only related to the user.
			GcSearchListImpl results = new GcSearchListImpl(maxNum);
			// retrieve the workflow from xregistry
			checkAndLoadCredentials();

//			URI xregistryURL = this.xbayaEngine.getConfiguration()
//					.getXRegistryURL();
//			if (xregistryURL == null) {
//				xregistryURL = XBayaConstants.DEFAULT_XREGISTRY_URL;
//			}
			XRegistryClient xregistryClient = new XRegistryClient(
					gssCredential, XBayaSecurity.getTrustedCertificates(),
					xregistryURL.toString());
			ResourceData[] datas = xregistryClient.findResource("");

			int index = 0;
			while (maxNum > 0 && datas.length > index) {
				ResourceData data = datas[index];
				QName qName = data.getName();
				results.addResult(new SearchResult(new URI(qName
						.getNamespaceURI()), qName.getLocalPart(), index));
				index++;
				maxNum--;
			}

			return results;
		} catch (RuntimeException e) {
			throw new WorkflowProxyException(e);
		} catch (XRegistryClientException e) {
			throw new WorkflowProxyException(e);
		} catch (URISyntaxException e) {
			throw new WorkflowProxyException(e);
		}
	}

	public WsdlDefinitions start(GcInstance instance) {
		if (instance instanceof ProxyGcInstance) {
			((ProxyGcInstance) instance).getWsdlDefinitions();
		}

		return null;
	}

	public void connect() throws WorkflowEngineException {
		// we don't have to explicitly connect to our workflow engine. So
		// discarding that
	}

	public void setXBayaEngine(XBayaEngine xBayaEngine) {
		this.xbayaEngine = xBayaEngine;
	}

	class ProxyGcInstance extends GcInstance {
		private WsdlDefinitions wsdlDefinitions;

		public ProxyGcInstance(WsdlDefinitions wsdlDefinitions) {
			super(null);
			this.wsdlDefinitions = wsdlDefinitions;
		}

		public WsdlDefinitions getWsdlDefinitions() {
			return wsdlDefinitions;
		}

		public void setWsdlDefinitions(WsdlDefinitions wsdlDefinitions) {
			this.wsdlDefinitions = wsdlDefinitions;
		}
	}

	class SearchResult implements GcSearchResult {

		private URI id;
		private String title;
		private int lastModfied;

		public SearchResult(URI id, String title, int lastModfied) {
			this.title = title;
			this.id = id;
			this.lastModfied = lastModfied;
		}

		public String getTitle() {
			return title;
		}

		public URI getId() {
			return id;
		}

		public long getLastModfied() {
			return lastModfied;
		}
	}

	class GcSearchListImpl implements GcSearchList {

		private List<GcSearchResult> list;

		GcSearchListImpl(int size) {
			list = new ArrayList<GcSearchResult>(size);
		}

		public void addResult(org.gpel.client.GcSearchResult gcSearchResult) {
			this.list.add(gcSearchResult);
		}

		public Iterable<GcSearchResult> results() {
			return list;
		}

		public int size() {
			return list.size();
		}
	}

	private void checkAndLoadCredentials() {
		if (isSecure()) {
			// Check if the proxy is loaded.
			boolean loaded = new MyProxyChecker(this.xbayaEngine)
					.loadIfNecessary();
			if (!loaded) {
				return;
			}
			// Creates a secure channel in gpel.
			MyProxyClient myProxyClient = this.xbayaEngine.getMyProxyClient();
			GSSCredential proxy = myProxyClient.getProxy();
			UserX509Credential credential = new UserX509Credential(proxy,
					XBayaSecurity.getTrustedCertificates());
			try {
				this.setUserX509Credential(credential);
			} catch (WorkflowEngineException e) {
				this.xbayaEngine.getErrorWindow().error(
						ErrorMessages.GPEL_ERROR, e);
				return;
			}
		}
	}

}