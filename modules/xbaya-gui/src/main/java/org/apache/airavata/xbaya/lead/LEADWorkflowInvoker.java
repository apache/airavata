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

package org.apache.airavata.xbaya.lead;

import java.net.URI;
import java.util.List;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponent;
import org.apache.airavata.xbaya.component.ws.WSComponentFactory;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.v1.builder.XmlElement;

import xsul.XmlConstants;
import xsul.invoker.puretls.PuretlsInvoker;
import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.wsif.spi.WSIFProviderManager;
import xsul.wsif_xsul_soap_gsi.Provider;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul.xwsif_runtime_async_http.XsulSoapHttpWsaResponsesCorrelator;
import xsul.xwsif_runtime_async_msgbox.XsulMsgBoxWsaResponsesCorrelator;
import xsul5.MLogger;
import xsul5.wsdl.WsdlDefinitions;

public class LEADWorkflowInvoker {

    private static final MLogger logger = MLogger.getLogger();

    private WsdlDefinitions definitions;

    private WSIFClient client;

    private WSIFOperation operation;

    private boolean success;

    private WSIFMessage inputMessage;

    private WSIFMessage outputMessage;

    private WSIFMessage faultMessage;

    private WSComponent component;

    private LeadContextHeader leadContext;

    private URI messageBoxURL;

    private PuretlsInvoker secureInvoker;

    private String operationName;

    static {
        WSIFProviderManager.getInstance().addProvider(new xsul.wsif_xsul_soap_http.Provider());
    }

    /**
     * Constructs a LEADWorkflowInvoker.
     * 
     * @param definitions
     * @param leadContext
     * @throws ComponentException
     */
    public LEADWorkflowInvoker(WsdlDefinitions definitions, LeadContextHeader leadContext) throws ComponentException {
        this(definitions, leadContext, null, (PuretlsInvoker) null);
    }

    /**
     * Constructs a LEADWorkflowInvoker.
     * 
     * @param definitions
     * @param leadContext
     * @param messageBoxURL
     * @throws ComponentException
     */
    public LEADWorkflowInvoker(WsdlDefinitions definitions, LeadContextHeader leadContext, URI messageBoxURL)
            throws ComponentException {
        this(definitions, leadContext, messageBoxURL, (PuretlsInvoker) null);
    }

    /**
     * Constructs a LEADWorkflowInvoker.
     * 
     * @param definitions
     * @param leadContext
     * @param messageBoxURL
     * @param secureInvoker
     * @throws ComponentException
     */
    public LEADWorkflowInvoker(WsdlDefinitions definitions, LeadContextHeader leadContext, URI messageBoxURL,
            PuretlsInvoker secureInvoker) throws ComponentException {
        this(definitions, leadContext, messageBoxURL, secureInvoker, null);
    }

    /**
     * 
     * Constructs a LEADWorkflowInvoker.
     * 
     * @param definitions
     * @param leadContext
     * @param messageBoxURL
     * @param secureInvoker
     * @param operationName
     *            If Wsdl contains more than one operation
     * @throws ComponentException
     */
    public LEADWorkflowInvoker(WsdlDefinitions definitions, LeadContextHeader leadContext, URI messageBoxURL,
            PuretlsInvoker secureInvoker, String operationName) throws ComponentException {
        this.definitions = definitions;
        this.leadContext = leadContext;
        this.messageBoxURL = messageBoxURL;
        this.secureInvoker = secureInvoker;
        this.operationName = operationName;
        init();
    }

    /**
     * @return The lead context
     */
    public LeadContextHeader getLeadContext() {
        return this.leadContext;
    }

    /**
     * @return The inputs
     */
    public List<WSComponentPort> getInputs() {
        return this.component.getInputPorts();
    }

    /**
     * @param inputs
     */
    public void setInputs(List<WSComponentPort> inputs) {
        for (WSComponentPort input : inputs) {
            String name = input.getName();
            Object value = input.getValue();

            // Give a special treatment for "arrays"
            if (this.inputMessage instanceof WSIFMessageElement) {
                QName type = ((WSIFMessageElement) this.inputMessage).getPartType(name);
                if (LEADTypes.isArrayType(type)) {
                    // split string into items using " " as separator
                    Pattern pattern = Pattern.compile("[,\\s]+");
                    String[] result = pattern.split((String) value);
                    XmlElement arrayEl = XmlConstants.BUILDER.newFragment(name);
                    for (int i = 0; i < result.length; i++) {
                        logger.finest("split=" + result[i]);
                        arrayEl.addElement("value").addChild(result[i]);
                    }
                    this.inputMessage.setObjectPart(name, arrayEl);
                    value = null; // no need to set string value below
                }

            }
            if (value != null) {
                this.inputMessage.setObjectPart(name, value);
            }
        }
    }

    /**
     * @return true if it succeeds; false otherwise
     * @throws XBayaException
     */
    public boolean invoke() throws XBayaException {
        try {
            logger.info("leadContext: " + XMLUtil.xmlElementToString(this.leadContext));
            logger.info("inputMessage: " + XMLUtil.xmlElementToString((XmlElement) this.inputMessage));
            // Soap11Util.getInstance().wrapBodyContent(
            // (XmlElement) this.inputMessage);
            this.success = this.operation.executeRequestResponseOperation(this.inputMessage, this.outputMessage,
                    this.faultMessage);
            if (this.success) {
                logger.info("outputMessage: " + XMLUtil.xmlElementToString((XmlElement) this.outputMessage));
            } else {
                logger.info("faultMessage: " + XMLUtil.xmlElementToString((XmlElement) this.faultMessage));
            }
            return this.success;
        } catch (RuntimeException e) {
            String message = "Error in invoking a service.";
            throw new XBayaException(message, e);
        }
    }

    /**
     * @return The outputs
     */
    public List<WSComponentPort> getOutputs() {
        List<WSComponentPort> outputs = this.component.getOutputPorts();
        for (WSComponentPort output : outputs) {
            String name = output.getName();
            String value = this.outputMessage.getObjectPart(name).toString();
            output.setValue(value);
        }
        return outputs;
    }

    /**
     * @return The fault
     */
    public WSIFMessage getFault() {
        return getFaultMessage();
    }

    /**
     * @return true if success; false otherwise.
     */
    public boolean isSuccess() {
        return this.success;
    }

    /**
     * @return the input message.
     */
    public WSIFMessage getInputMessage() {
        return this.inputMessage;
    }

    /**
     * @return the output message.
     */
    public WSIFMessage getOutputMessage() {
        return this.outputMessage;
    }

    /**
     * @return the fault message.
     */
    public WSIFMessage getFaultMessage() {
        return this.faultMessage;
    }

    /**
     * Parses the WSDL and prepares everything.
     * 
     * @throws ComponentException
     */
    private void init() throws ComponentException {
        logger.finest("wsdl: " + this.definitions.xmlStringPretty());
        this.component = WSComponentFactory.createComponent(this.definitions);

        WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        WSIFService service = factory.getService(WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(this.definitions));

        if (this.secureInvoker != null) {
            Provider secureProvider = new xsul.wsif_xsul_soap_gsi.Provider(this.secureInvoker);
            service.addLocalProvider(secureProvider);
        }

        this.client = WSIFRuntime.getDefault().newClientFor(service, null);
        // null selects the first port in the first service.

        // Async
        int clientPort = 0;
        WSIFAsyncResponsesCorrelator correlator;
        if (this.messageBoxURL == null) {
            logger.info("starting response correlator using local port " + clientPort);
            XsulSoapHttpWsaResponsesCorrelator wasCorrelator = new XsulSoapHttpWsaResponsesCorrelator(clientPort);
            String serverLoc = (wasCorrelator).getServerLocation();
            logger.finest("client is waiting at " + serverLoc);
            correlator = wasCorrelator;
        } else {
            logger.info("starting reponse correlator using message box " + this.messageBoxURL);
            correlator = new XsulMsgBoxWsaResponsesCorrelator(this.messageBoxURL.toString());
        }

        this.client.useAsyncMessaging(correlator);

        StickySoapHeaderHandler handler = new StickySoapHeaderHandler("use-lead-header", this.leadContext);
        this.client.addHandler(handler);

        // This handler has to be end to get the entire soap message.
        NotificationHandler notificationHandler = new NotificationHandler(this.leadContext);
        this.client.addHandler(notificationHandler);

        WSIFPort port = this.client.getPort();
        if (this.operationName == null) {
            this.operationName = this.component.getOperationName();
        }
        logger.finest("operationName: " + operationName);
        this.operation = port.createOperation(operationName);
        this.inputMessage = this.operation.createInputMessage();
        this.outputMessage = this.operation.createOutputMessage();
        this.faultMessage = this.operation.createFaultMessage();
    }
}