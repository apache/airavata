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

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.invoker.factory.InvokerFactory;
import org.apache.airavata.xbaya.jython.lib.ServiceNotifiable;
import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;

import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlException;
import xsul.wsdl.WsdlResolver;
import xsul.wsif.WSIFMessage;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;

public class GenericInvoker implements Invoker {

    private static final Logger logger = LoggerFactory.getLogger(GenericInvoker.class);

    private String nodeID;

    private QName portTypeQName;

    private String wsdlLocation;

    private String serviceInformation;

    private String messageBoxURL;

    private String gfacURL;

    private Invoker invoker;

    private Future<Boolean> result;

    private ServiceNotifiable notifier;

    private ContextHeaderDocument.ContextHeader contextHeader;

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
     * @param notifier
     *            The notification sender
     */
    public GenericInvoker(QName portTypeQName, String wsdlLocation, String nodeID, WorkflowNotifiable notifier) {
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
            WorkflowNotifiable notifier) {
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
            String gfacURL, WorkflowNotifiable notifier) {
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.wsdlLocation = wsdlLocation;
        this.serviceInformation = wsdlLocation;
        this.messageBoxURL = messageBoxURL;
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);
        this.failerSent = false;
        this.contextHeader = WorkflowContextHeaderBuilder.getCurrentContextHeader();
    }

    /**
     *
     * @param portTypeQName
     * @param wsdl
     * @param nodeID
     * @param messageBoxURL
     * @param gfacURL
     * @param notifier
     */
    public GenericInvoker(QName portTypeQName, WsdlDefinitions wsdl, String nodeID, String messageBoxURL,
            String gfacURL, WorkflowNotifiable notifier) {
        final String wsdlStr = xsul.XmlConstants.BUILDER.serializeToString(wsdl);
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.wsdlDefinitionObject = wsdl;
        this.messageBoxURL = messageBoxURL;
        this.serviceInformation = wsdlStr;
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);
        this.failerSent = false;
        this.contextHeader = WorkflowContextHeaderBuilder.getCurrentContextHeader();
    }

    /**
     *
     * @throws XBayaException
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

        } catch (XBayaException e) {
            logger.error(e.getMessage(), e);
            // An appropriate message has been set in the exception.
            this.notifier.invocationFailed(e.getMessage(), e);
            throw e;
        } catch (URISyntaxException e) {
            logger.error(e.getMessage(), e);
            String message = "The location of the WSDL has to be a valid URL or file path: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (WsdlException e) {
            logger.error(e.getMessage(), e);
            String message = "Error in processing the WSDL: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error in processing the WSDL: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    private void setup(WsdlDefinitions definitions) throws XBayaException {

        // Set LEAD context header.
        WorkflowContextHeaderBuilder builder;
        if(contextHeader == null){
            builder = new WorkflowContextHeaderBuilder(this.notifier.getEventSink()
                .getAddress(), this.gfacURL, null, this.notifier.getWorkflowID().toASCIIString(),
                "xbaya-experiment", this.messageBoxURL);
        }else{
             builder = new WorkflowContextHeaderBuilder(contextHeader);
        }
        if(builder.getWorkflowMonitoringContext() == null){
            builder.addWorkflowMonitoringContext(this.notifier.getEventSink().getAddress(),
                    this.notifier.getWorkflowID().toASCIIString(),this.nodeID,this.messageBoxURL);
        } else {
            builder.getWorkflowMonitoringContext().setWorkflowInstanceId(this.notifier.getWorkflowID().toASCIIString());
        }
        builder.getWorkflowMonitoringContext().setWorkflowNodeId(this.nodeID);
        builder.getWorkflowMonitoringContext().setServiceInstanceId(this.nodeID);
        builder.getWorkflowMonitoringContext().setWorkflowTimeStep(1);
        builder.setUserIdentifier("xbaya-user");
        //todo write a UI component to collect this information and pass it through Header
//        builder.setGridMyProxyRepository("myproxy.nersc.gov","fangliu","Jdas7wph",14000);
        StickySoapHeaderHandler handler = new StickySoapHeaderHandler("use-workflowcontext-header", builder.getXml());
        // Create Invoker
        this.invoker = InvokerFactory.createInvoker(this.portTypeQName, definitions, this.gfacURL, this.messageBoxURL,
                builder, true);
        this.invoker.setup();

        WSIFClient client = this.invoker.getClient();
        client.addHandler(handler);

        WsdlResolver resolver = WsdlResolver.getInstance();
        // Get the concrete WSDL from invoker.setup() and set it to the
        // notifier.

        this.notifier.setServiceID(this.nodeID);
        // if (this.wsdlLocation != null) {
        // this.notifier.setServiceID(this.nodeID);
        // } else {
        // String name = this.portTypeQName.getLocalPart();
        // this.notifier.setServiceID(name);
        // }
    }

    /**
     *
     * @param operationName
     *            The name of the operation
     * @throws XBayaException
     */
    public void setOperation(String operationName) throws XBayaException {
        try {
            this.invoker.setOperation(operationName);
        } catch (XBayaException e) {
            logger.error(e.getMessage(), e);
            // An appropriate message has been set in the exception.
            this.notifier.invocationFailed(e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "The WSDL does not conform to the invoking service: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    /**
     *
     * @param name
     *            The name of the input parameter
     * @param value
     *            The value of the input parameter
     * @throws XBayaException
     */
    public void setInput(String name, Object value) throws XBayaException {
        try {
            if (value instanceof XmlElement) {
                logger.info("value: " + XMLUtil.xmlElementToString((XmlElement) value));
            }
            this.inputNames.add(name);
            this.inputValues.add(value);
            this.invoker.setInput(name, value);
        } catch (XBayaException e) {
            logger.error(e.getMessage(), e);
            // An appropriate message has been set in the exception.
            this.notifier.invocationFailed(e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error in setting an input. name: " + name + " value: " + value;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    /**
     *
     * @return
     * @throws XBayaException
     */
    public synchronized boolean invoke() throws XBayaException {
        try {
            WSIFMessage inputMessage = this.invoker.getInputs();
            logger.info("inputMessage: " + XMLUtil.xmlElementToString((XmlElement) inputMessage));
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
                            logger.info("outputMessage: " + outputMessage);
                            GenericInvoker.this.notifier.serviceFinished(outputMessage);
                        } else {
                            WSIFMessage faultMessage = GenericInvoker.this.invoker.getFault();
                            // An implementation of WSIFMessage,
                            // WSIFMessageElement, implements toString(), which
                            // serialize the message XML.
                            logger.info("received fault: " + faultMessage);
                            GenericInvoker.this.notifier.receivedFault(faultMessage);
                            GenericInvoker.this.failerSent = true;
                        }
                        return success;
                    } catch (XBayaException e) {
                        logger.error(e.getMessage(), e);
                        // An appropriate message has been set in the exception.
                        GenericInvoker.this.notifier.invocationFailed(e.getMessage(), e);
                        GenericInvoker.this.failerSent = true;
                        throw new XBayaRuntimeException(e);
                    } catch (RuntimeException e) {
                        logger.error(e.getMessage(), e);
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
                logger.error(e.getMessage(), e);
            } catch (TimeoutException e) {
                // The job is probably running fine.
                // The normal case.
                return true;
            } catch (ExecutionException e) {
                // The service-failed notification should have been sent
                // already.
                logger.error(e.getMessage(), e);
                String message = "Error in invoking a service: " + this.serviceInformation;
                throw new XBayaException(message, e);
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error in invoking a service: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
        return true;
    }

    /**
     *
     * @throws XBayaException
     */
    @SuppressWarnings("boxing")
    public synchronized void waitToFinish() throws XBayaException {
        try {
            while (this.result == null) {
                // The job is not submitted yet.
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
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
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } catch (ExecutionException e) {
            // The service-failed notification should have been sent already.
            logger.error(e.getMessage(), e);
            String message = "Error in invoking a service: " + this.serviceInformation;
            throw new XBayaException(message, e);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error while waiting for a service to finish: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    /**
     *
     * @param name
     *            The name of the output parameter
     * @return
     * @throws XBayaException
     */
    public Object getOutput(String name) throws XBayaException {
        try {
            waitToFinish();
            Object output = this.invoker.getOutput(name);
            if (output instanceof XmlElement) {
                logger.info("output: " + XMLUtil.xmlElementToString((XmlElement) output));
            }
            return output;
        } catch (XBayaException e) {
            logger.error(e.getMessage(), e);
            // An appropriate message has been set in the exception.
            if (!this.failerSent) {
                this.notifier.invocationFailed(e.getMessage(), e);
            }
            throw e;
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error while waiting for a output: " + name;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    /**
     *
     * @return
     * @throws XBayaException
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