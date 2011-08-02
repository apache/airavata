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

package org.apache.airavata.xbaya.jython.lib.invoker;

import java.util.Iterator;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.jython.lib.ServiceNotificationSender;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.xmlpull.v1.builder.XmlElement;

import xsul.invoker.puretls.PuretlsInvoker;
import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif.spi.WSIFProviderManager;
import xsul.wsif_xsul_soap_gsi.Provider;
import xsul.xwsif_runtime.WSIFClient;
import xsul5.MLogger;

public class SecureInvoker implements Invoker {

    protected WSIFClient client;

    private WsdlDefinitions definitions;

    private WSIFOperation operation;

    private WSIFMessage inputMessage;

    private WSIFMessage outputMessage;

    private WSIFMessage faultMessage;

    private PuretlsInvoker secureInvoker;

    private LeadContextHeader leadContextHeader;

    private String operationName;

    private ServiceNotificationSender notifier;

    private WSIFPort port;

    private static final MLogger logger = MLogger.getLogger();

    static {
        WSIFProviderManager.getInstance().addProvider(new xsul.wsif_xsul_soap_http.Provider());
    }

    /**
     * Constructs a SimpleInvoker.
     * 
     * @param definitions
     * @param leadContextHeader
     * @param serviceNotificationSender
     */
    public SecureInvoker(WsdlDefinitions definitions, String operationName, PuretlsInvoker secureInvoker,
            LeadContextHeader leadContextHeader, ServiceNotificationSender serviceNotificationSender) {
        this.operationName = operationName;
        this.definitions = definitions;
        this.secureInvoker = secureInvoker;
        this.leadContextHeader = leadContextHeader;
        this.notifier = serviceNotificationSender;
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#setup()
     */
    public WsdlDefinitions setup() throws XBayaException {

        // WsdlResolver wsdlResolver = WsdlResolver.getInstance();
        // wsdlResolver.setSecureInvoker(secureInvoker);
        // WsdlDefinitions definitions = wsdlResolver.loadWsdl(
        // wsdlURI);
        WSIFService service = WSIFServiceFactory.newInstance().getService(definitions);
        if (this.secureInvoker != null) {
            Provider secureProvider = new xsul.wsif_xsul_soap_gsi.Provider(this.secureInvoker);
            service.addLocalProvider(secureProvider);
        }
        this.port = service.getPort();
        this.operation = port.createOperation(this.operationName);
        this.inputMessage = this.operation.createInputMessage();
        this.outputMessage = this.operation.createOutputMessage();
        this.faultMessage = this.operation.createFaultMessage();
        return definitions;
        // WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
        // WSIFService service = factory.getService(this.definitions);
        //
        // if (this.secureInvoker != null) {
        // Provider secureProvider = new xsul.wsif_xsul_soap_gsi.Provider(
        // this.secureInvoker);
        // service.addLocalProvider(secureProvider);
        // }
        //
        // this.client = WSIFRuntime.getDefault().newClientFor(service, null);
        // // null selects the first port in the first service.
        //
        //
        //
        //
        // if (WSDLUtil.isAsynchronousSupported(WSDLUtil
        // .wsdlDefinitions3ToWsdlDefintions5(definitions))) {
        // // Async
        // int clientPort = 0;
        // WSIFAsyncResponsesCorrelator correlator;
        // logger.info("starting response correlator using local port "
        // + clientPort);
        // XsulSoapHttpWsaResponsesCorrelator wasCorrelator = new XsulSoapHttpWsaResponsesCorrelator(
        // clientPort);
        // String serverLoc = (wasCorrelator).getServerLocation();
        // logger.finest("client is waiting at " + serverLoc);
        // correlator = wasCorrelator;
        // this.client.useAsyncMessaging(correlator);
        // }
        //
        //
        // StickySoapHeaderHandler handler = new StickySoapHeaderHandler(
        // "use-lead-header", this.leadContextHeader);
        // this.client.addHandler(handler);
        //
        // // This handler has to be end to get the entire soap message.
        // NotificationHandler notificationHandler = new NotificationHandler(
        // this.leadContextHeader);
        // this.client.addHandler(notificationHandler);
        //
        // WSIFPort port = this.client.getPort();
        // logger.finest("operationName: " + this.operation);
        // this.operation = port.createOperation(this.operationName);
        // this.inputMessage = this.operation.createInputMessage();
        // this.outputMessage = this.operation.createOutputMessage();
        // this.faultMessage = this.operation.createFaultMessage();
        //
        // return this.definitions;

    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getClient()
     */
    public WSIFClient getClient() {
        return this.client;
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#setOperation(java.lang.String)
     */
    public void setOperation(String operationName) throws XBayaException {
        try {
            this.operation = port.createOperation(operationName);
            this.inputMessage = this.operation.createInputMessage();
            this.outputMessage = this.operation.createOutputMessage();
            this.faultMessage = this.operation.createFaultMessage();
        } catch (RuntimeException e) {
            String message = "The WSDL does not conform to the invoking service.";
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#setInput(java.lang.String, java.lang.Object)
     */
    public void setInput(String name, Object value) throws XBayaException {
        try {
            if (value instanceof XmlElement) {
                // If the value is a complex type, change the name of the
                // element to the correct one.
                XmlElement valueElement = (XmlElement) value;
                valueElement.setName(name);
                // value = XmlConstants.BUILDER.serializeToString(valueElement);
            } else if (value instanceof org.xmlpull.infoset.XmlElement) {
                org.xmlpull.infoset.XmlElement valueElement = (org.xmlpull.infoset.XmlElement) value;
                valueElement.setName(name);
                value = WSDLUtil.xmlElement5ToXmlElementv1(valueElement);
            } else if (value instanceof String) {

                // Simple case.
            } else {
                // convert int, doule to string.
                value = "" + value;
            }
            this.inputMessage.setObjectPart(name, value);
        } catch (RuntimeException e) {
            String message = "Error in setting an input. name: " + name + " value: " + value;
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getInputs()
     */
    public WSIFMessage getInputs() {
        return this.inputMessage;
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#invoke()
     */
    public boolean invoke() throws XBayaException {

        new Thread() {
            public void run() {
                try {
                    SecureInvoker.this.operation.executeRequestResponseOperation(SecureInvoker.this.inputMessage,
                            SecureInvoker.this.outputMessage, SecureInvoker.this.faultMessage);
                } catch (Throwable e) {
                    SecureInvoker.this.notifier.invocationFailed("Failed Invoking service", e);
                }
            }
        }.start();

        return true;

    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getOutputs()
     */
    public WSIFMessage getOutputs() {
        return this.outputMessage;
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getOutput(java.lang.String)
     */
    public Object getOutput(String name) throws XBayaException {
        try {
            // This code doesn't work when the output is a complex type.
            // Object output = this.outputMessage.getObjectPart(name);
            // return output;

            XmlElement outputElement = (XmlElement) this.outputMessage;
            XmlElement valueElement = outputElement.element(null, name);
            Iterator childIt = valueElement.children();
            int numberOfChildren = 0;
            while (childIt.hasNext()) {
                childIt.next();
                numberOfChildren++;
            }
            if (numberOfChildren == 1) {
                Object child = valueElement.children().next();
                if (child instanceof String) {
                    // Value is a simple type. Return the string.
                    String value = (String) child;
                    return value;
                }
            }
            // Value is a complex type. Return the whole XmlElement so that we
            // can set it to the next service as it is.
            return valueElement;
        } catch (RuntimeException e) {
            String message = "Error in getting output. name: " + name;
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getFault()
     */
    public WSIFMessage getFault() {
        return this.faultMessage;
    }

}