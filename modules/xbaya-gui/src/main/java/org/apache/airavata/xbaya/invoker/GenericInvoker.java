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

package org.apache.airavata.xbaya.invoker;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.invoker.factory.InvokerFactory;
import org.apache.airavata.xbaya.jython.lib.NotificationSender;
import org.apache.airavata.xbaya.jython.lib.ServiceNotificationSender;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.xmlpull.v1.builder.XmlElement;

import xsul.lead.LeadContextHeader;
import xsul.ws_addressing.WsaEndpointReference;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlException;
import xsul.wsdl.WsdlResolver;
import xsul.wsif.WSIFMessage;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul5.MLogger;

public class GenericInvoker implements Invoker {

    private static final MLogger logger = MLogger.getLogger();

    private String nodeID;

    private QName portTypeQName;

    private String wsdlLocation;

    private String serviceInformation;

    private String messageBoxURL;

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

    private WsdlDefinitions wsdlDefinitionObject;

    /**
     * Creates an InvokerWithNotification.
     * 
     * @param portTypeQName
     * 
     * @param wsdlLocation
     *            The URL of WSDL of the service to invoke
     * @param nodeID
     *            The ID of the service
     * @param gfacURL
     *            The URL of GFac service.
     * @param notifier
     *            The notification sender
     */
    public GenericInvoker(QName portTypeQName, String wsdlLocation, String nodeID, NotificationSender notifier) {
        this(portTypeQName, wsdlLocation, nodeID, null, notifier);
    }

    /**
     * Creates an InvokerWithNotification.
     * 
     * @param portTypeQName
     * 
     * @param wsdlLocation
     *            The URL of WSDL of the service to invoke
     * @param nodeID
     *            The ID of the service
     * @param gfacURL
     *            The URL of GFac service.
     * @param notifier
     *            The notification sender
     */
    public GenericInvoker(QName portTypeQName, String wsdlLocation, String nodeID, String gfacURL,
            NotificationSender notifier) {
        this(portTypeQName, wsdlLocation, nodeID, null, gfacURL, notifier);
    }

    /**
     * Creates an InvokerWithNotification.
     * 
     * @param portTypeQName
     * 
     * @param wsdlLocation
     *            The URL of WSDL of the service to invoke
     * @param nodeID
     *            The ID of the service
     * @param messageBoxURL
     * @param gfacURL
     *            The URL of GFac service.
     * @param notifier
     *            The notification sender
     */
    public GenericInvoker(QName portTypeQName, String wsdlLocation, String nodeID, String messageBoxURL,
            String gfacURL, NotificationSender notifier) {
        logger.entering(new Object[] { portTypeQName, wsdlLocation, nodeID, notifier });
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.wsdlLocation = wsdlLocation;
        this.serviceInformation = wsdlLocation;
        this.messageBoxURL = messageBoxURL;
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);

        this.failerSent = false;
    }

    /**
     * Constructs a GenericInvoker.
     * 
     * @param portTypeQName2
     * @param wsdlDefinitions5ToWsdlDefintions3
     * @param id
     * @param string
     * @param object
     * @param object2
     * @param object3
     * @param notifier2
     */
    public GenericInvoker(QName portTypeQName, WsdlDefinitions wsdl, String nodeID, String messageBoxURL,
            String gfacURL, NotificationSender notifier) {
        final String wsdlStr = xsul.XmlConstants.BUILDER.serializeToString(wsdl);
        logger.entering(new Object[] { portTypeQName, wsdlStr, nodeID, notifier });
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.wsdlDefinitionObject = wsdl;
        this.messageBoxURL = messageBoxURL;
        this.serviceInformation = wsdlStr;
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);

        this.failerSent = false;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#setup()
     */
    public void setup() throws XBayaException {
        try {
            WsdlDefinitions definitions = null;
            if (this.wsdlLocation != null && !this.wsdlLocation.equals("")) {
                WsdlResolver resolver = WsdlResolver.getInstance();
                definitions = resolver.loadWsdl(new File(".").toURI(), new URI(this.wsdlLocation));
            } else {
                definitions = this.wsdlDefinitionObject;
            }

            setup(definitions);            
            
            logger.exiting();
            
        } catch (XBayaException e) {
            logger.caught(e);
            // An appropriate message has been set in the exception.
            this.notifier.invocationFailed(e.getMessage(), e);
            throw e;
        } catch (URISyntaxException e) {
            logger.caught(e);
            String message = "The location of the WSDL has to be a valid URL or file path: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (WsdlException e) {
            logger.caught(e);
            String message = "Error in processing the WSDL: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "Error in processing the WSDL: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    private void setup(WsdlDefinitions definitions) throws XBayaException {

        // Set LEAD context header.
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();
        try {
            leadContext.setEventSink(new WsaEndpointReference(new URI(this.notifier.getEventSink().getAddress())));
            leadContext.setServiceId(this.nodeID);
            leadContext.setNodeId(this.nodeID);
            leadContext.setWorkflowId(this.notifier.getWorkflowID());
            leadContext.setTimeStep("1");
        } catch (URISyntaxException e) {

        }
        StickySoapHeaderHandler handler = new StickySoapHeaderHandler("use-lead-header", leadContext);

        // Create Invoker
        this.invoker = InvokerFactory.createInvoker(this.portTypeQName, definitions, this.gfacURL, this.messageBoxURL,
                leadContext);
        this.invoker.setup();
        WSIFClient client = this.invoker.getClient();
        client.addHandler(handler);

        WsdlResolver resolver = WsdlResolver.getInstance();
        // Get the concrete WSDL from invoker.setup() and set it to the
        // notifier.
        if (this.wsdlLocation != null) {
            this.notifier.setServiceID(this.wsdlLocation);
        } else {
            String name = this.portTypeQName.getLocalPart();
            this.notifier.setServiceID(name);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#setOperation(java.lang.String)
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

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#setInput(java.lang.String, java.lang.Object)
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
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#invoke()
     */
    public synchronized boolean invoke() throws XBayaException {

        logger.entering();

        try {
                WSIFMessage inputMessage = this.invoker.getInputs();
            logger.finest("inputMessage: " + XMLUtil.xmlElementToString((XmlElement) inputMessage));
            this.notifier.invokingService(inputMessage);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            this.result = executor.submit(new Callable<Boolean>() {
                @SuppressWarnings("boxing")
                public Boolean call() {
                    try {
                        boolean success = GenericInvoker.this.invoker.invoke();
                        if (success) {
                            // Send notification
                            WSIFMessage outputMessage = GenericInvoker.this.invoker.getOutputs();
                            // An implementation of WSIFMessage,
                            // WSIFMessageElement, implements toString(), which
                            // serialize the message XML.
                            logger.finest("outputMessage: " + outputMessage);
                            GenericInvoker.this.notifier.serviceFinished(outputMessage);
                        } else {
                            WSIFMessage faultMessage = GenericInvoker.this.invoker.getFault();
                            // An implementation of WSIFMessage,
                            // WSIFMessageElement, implements toString(), which
                            // serialize the message XML.
                            logger.finest("received fault: " + faultMessage);
                            GenericInvoker.this.notifier.receivedFault(faultMessage);
                            GenericInvoker.this.failerSent = true;
                        }
                        return success;
                    } catch (XBayaException e) {
                        logger.caught(e);
                        // An appropriate message has been set in the exception.
                        GenericInvoker.this.notifier.invocationFailed(e.getMessage(), e);
                        GenericInvoker.this.failerSent = true;
                        throw new XBayaRuntimeException(e);
                    } catch (RuntimeException e) {
                        logger.caught(e);
                        String message = "Error in invoking a service: " + GenericInvoker.this.serviceInformation;
                        GenericInvoker.this.notifier.invocationFailed(message, e);
                        GenericInvoker.this.failerSent = true;
                        throw e;
                    }
                }
            });

            // Kill the thread inside of executor. This is necessary for Jython
            // script to finish.
            executor.shutdown();

            // Let other threads know that job has been submitted.
            notifyAll();

            // Check if the invocation itself fails. This happens immediately.
            try {
                this.result.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.caught(e);
            } catch (TimeoutException e) {
                // The job is probably running fine.
                // The normal case.
                return true;
            } catch (ExecutionException e) {
                // The service-failed notification should have been sent
                // already.
                logger.caught(e);
                String message = "Error in invoking a service: " + this.serviceInformation;
                throw new XBayaException(message, e);
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
        return true;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#waitToFinish()
     */
    @SuppressWarnings("boxing")
    public synchronized void waitToFinish() throws XBayaException {
        logger.entering();
        try {
            while (this.result == null) {
                // The job is not submitted yet.
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger.caught(e);
                }
            }
            // Wait for the job to finish.
            Boolean success = this.result.get();
            if (success == false) {
                WSIFMessage faultMessage = this.invoker.getFault();
                String message = "Error in a service: ";
                // An implementation of WSIFMessage,
                // WSIFMessageElement, implements toString(), which
                // serialize the message XML.
                message += faultMessage.toString();
                throw new XBayaException(message);
            }
            logger.exiting();
        } catch (InterruptedException e) {
            logger.caught(e);
        } catch (ExecutionException e) {
            // The service-failed notification should have been sent already.
            logger.caught(e);
            String message = "Error in invoking a service: " + this.serviceInformation;
            throw new XBayaException(message, e);
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
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#getOutput(java.lang.String)
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
     * @see org.apache.airavata.xbaya.invoker.WorkflowInvoker#getOutputs()
     */
    public WSIFMessage getOutputs() throws XBayaException {
        return this.invoker.getOutputs();
    }

    @Override
    public WSIFClient getClient() {
        return null;
    }

    @Override
    public WSIFMessage getInputs() throws XBayaException {
        return null;
    }

    @Override
    public WSIFMessage getFault() throws XBayaException {
        return null;
    }

}