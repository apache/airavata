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

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.jython.lib.NotificationSender;
import org.apache.airavata.xbaya.jython.lib.SecureGFacCreator;
import org.apache.airavata.xbaya.jython.lib.ServiceNotificationSender;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.workflow.WorkflowInvoker;
import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlElement;

import xsul.XmlConstants;
import xsul.invoker.puretls.PuretlsInvoker;
import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlPortType;
import xsul.wsif.WSIFMessage;
import xsul5.MLogger;

public class SecureGFacInvoker implements WorkflowInvoker {

    private static final MLogger logger = MLogger.getLogger();

    private String nodeID;

    private QName portTypeQName;

    private WsdlDefinitions abstractDefinitions;

    private String serviceInformation;

    private String gfacURL;

    private Invoker invoker;

    private Future<Boolean> result;

    private ServiceNotificationSender notifier;

    /**
     * used for notification
     */
    private List<Object> inputValues = new ArrayList<Object>();

    /**
     * used for notification
     */
    private List<String> inputNames = new ArrayList<String>();

    boolean failerSent;

    private String gfacRegistryURL;

    private PuretlsInvoker secureInvoker;

    private SecureGFacCreator creator;

    private LeadContextHeader leadContextHeader;

    private WsdlDefinitions invokableWSDL;

    private String operationName;

    /**
     * 
     * Constructs a SecureGFacInvoker.
     * 
     * @param portTypeQName
     * @param abstractDefinitions
     * @param nodeID
     * @param messageBoxURL
     * @param gfacRegistryURL
     * @param gfacURL
     * @param notifier
     * @param secureInvoker
     * @param leadCtxHeader
     */
    public SecureGFacInvoker(QName portTypeQName, WsdlDefinitions abstractDefinitions, String nodeID, String gfacURL,
            NotificationSender notifier, PuretlsInvoker secureInvoker, LeadContextHeader leadCtxHeader,
            String operationName) {
        logger.entering(new Object[] { portTypeQName, abstractDefinitions, nodeID, notifier });
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.abstractDefinitions = abstractDefinitions;
        this.serviceInformation = XmlConstants.BUILDER.serializeToString(abstractDefinitions);
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);
        this.secureInvoker = secureInvoker;
        this.failerSent = false;
        this.leadContextHeader = leadCtxHeader;
        this.operationName = operationName;
    }

    /**
     * 
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#setup()
     */
    public void setup() throws XBayaException {
        String wsdlLoc = null;
        if (this.gfacURL == null) {
            XmlElement factoryServicesElement = this.abstractDefinitions.element(null, "factoryServices");
            if (factoryServicesElement != null) {
                XmlElement factoryService = factoryServicesElement.element(null, "factoryService");
                if (factoryService != null) {
                    String location = factoryService.getAttributeValue(null, "location");
                    String portType = factoryService.getAttributeValue(null, "portType");
                    logger.finest("location: " + location);
                    logger.finest("portType: " + portType);
                    wsdlLoc = WSDLUtil.appendWSDLQuary(location);
                }
            }
        } else {
            wsdlLoc = WSDLUtil.appendWSDLQuary(this.gfacURL);
        }

        if (wsdlLoc == null) {
            String message = "The location of the Generic Factory is not specified.";
            throw new XBayaException(message);
        }
        if (!wsdlLoc.startsWith("https")) {
            throw new XBayaException("The GFac url is expected to support https but got:" + wsdlLoc);
        }

        try {
            this.creator = new SecureGFacCreator(wsdlLoc, this.secureInvoker);
        } catch (URISyntaxException e) {
            String message = "The location of the Generic Factory is in a wrong format";
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
        if (this.creator == null) {
            String message = "Cannot find the location of the Generic Factory in the WSDL";
            throw new XBayaException(message);
        }

        if (this.portTypeQName == null) {
            try {
                Iterable portTypes = this.abstractDefinitions.getPortTypes();
                WsdlPortType portType = (WsdlPortType) portTypes.iterator().next();
                String portTypeName = portType.getPortTypeName();
                String targetNamespace = this.abstractDefinitions.getTargetNamespace();
                this.portTypeQName = new QName(targetNamespace, portTypeName);
            } catch (RuntimeException e) {
                String message = "Error in finding QName of the service in the WSDL";
                this.notifier.invocationFailed(message, e);
                throw new XBayaException(message, e);
            }
        }
        invokableWSDL = this.creator.createService(this.portTypeQName);

        // this.invoker = InvokerFactory.createInvoker(portTypeQName, invokableWSDL, null,
        // XBayaConstants.DEFAULT_MESSAGE_BOX_URL.toString());
        this.invoker = new SecureInvoker(invokableWSDL, this.operationName, this.secureInvoker, this.leadContextHeader,
                this.notifier);
        this.invoker.setup();

        System.out.println(XmlConstants.BUILDER.serializeToString(invokableWSDL));

    }

    public WsdlDefinitions getInvokableWSDL() {
        return this.invokableWSDL;
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#getOutput(java.lang.String)
     */
    public Object getOutput(String name) throws XBayaException {
        logger.entering(new Object[] { name });
        try {
            waitToFinish();
            Object output = this.invoker.getOutput(name);
            if (output instanceof XmlElement) {
                logger.finest("output: " + XMLUtil.xmlElementToString((XmlElement) output));
            }
            logger.exiting(output);
            return output;
        } catch (XBayaException e) {
            logger.caught(e);
            // An appropriate message has been set in the exception.
            if (!this.failerSent) {
                this.notifier.invocationFailed(e.getMessage(), e);
            }
            throw e;
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "Error while waiting for a output: " + name;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#getOutputs()
     */
    public WSIFMessage getOutputs() throws XBayaException {
        return this.invoker.getOutputs();
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#invoke()
     */
    public void invoke() throws XBayaException {
        logger.entering();

        try {
            WSIFMessage inputMessage = this.invoker.getInputs();
            logger.finest("inputMessage: " + XMLUtil.xmlElementToString((XmlElement) inputMessage));
            this.notifier.invokingService(inputMessage);
            boolean success = this.invoker.invoke();
            if (success) {
                // Send notification
                WSIFMessage outputMessage = SecureGFacInvoker.this.invoker.getOutputs();
                // An implementation of WSIFMessage,
                // WSIFMessageElement, implements toString(), which
                // serialize the message XML.
                logger.finest("outputMessage: " + outputMessage);
                // SecureGFacInvoker.this.notifier
                // .serviceFinished(outputMessage);
            } else {
                WSIFMessage faultMessage = SecureGFacInvoker.this.invoker.getFault();
                // An implementation of WSIFMessage,
                // WSIFMessageElement, implements toString(), which
                // serialize the message XML.
                logger.finest("received fault: " + faultMessage);
                SecureGFacInvoker.this.notifier.receivedFault(faultMessage);
                SecureGFacInvoker.this.failerSent = true;
            }

            logger.exiting();
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "Error in invoking a service: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#waitToFinish()
     */
    @SuppressWarnings("boxing")
    public synchronized void waitToFinish() throws XBayaException {
        logger.entering();
        try {

            while (this.invoker.getOutputs() == null) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            logger.exiting();
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "Error while waiting for a service to finish: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#setInput(java.lang.String, java.lang.Object)
     */
    public void setInput(String name, Object value) throws XBayaException {
        logger.entering(new Object[] { name, value });
        try {
            if (value instanceof XmlElement) {
                logger.finest("value: " + XMLUtil.xmlElementToString((XmlElement) value));
            }
            this.inputNames.add(name);
            this.inputValues.add(value);
            this.invoker.setInput(name, value);
            logger.exiting();
        } catch (XBayaException e) {
            logger.caught(e);
            // An appropriate message has been set in the exception.
            this.notifier.invocationFailed(e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "Error in setting an input. name: " + name + " value: " + value;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }

    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#setOperation(java.lang.String)
     */
    public void setOperation(String operationName) throws XBayaException {
        logger.entering(new Object[] { operationName });

        try {
            this.invoker.setOperation(operationName);
            logger.exiting();
        } catch (XBayaException e) {
            logger.caught(e);
            // An appropriate message has been set in the exception.
            this.notifier.invocationFailed(e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "The WSDL does not conform to the invoking service: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }

    }

}