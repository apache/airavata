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

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.GfacAPI;
import org.apache.airavata.core.gfac.context.GFacConfiguration;
import org.apache.airavata.core.gfac.context.JobContext;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.schemas.gfac.Parameter;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.jython.lib.ServiceNotifiable;
import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsif.WSIFMessage;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.xwsif_runtime.WSIFClient;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.*;

public class EmbeddedGFacInvoker implements Invoker{

      private static final Logger logger = LoggerFactory.getLogger(EmbeddedGFacInvoker.class);

    private String nodeID;

    private QName portTypeQName;

    private String wsdlLocation;

    private String serviceInformation;

    private String messageBoxURL;

    private String gfacURL;

    private Invoker invoker;

    private XBayaConfiguration configuration;


    private Future<Boolean> result;

    private ServiceNotifiable notifier;

    private AiravataRegistry registry;

    private String topic;

    private String serviceName;
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

    Map<Parameter,ActualParameter> actualParameters = new HashMap<Parameter,ActualParameter>();

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
    public EmbeddedGFacInvoker(QName portTypeQName, String wsdlLocation, String nodeID, WorkflowNotifiable notifier) {
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
    public EmbeddedGFacInvoker(QName portTypeQName, String wsdlLocation, String nodeID, String gfacURL,
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
    public EmbeddedGFacInvoker(QName portTypeQName, String wsdlLocation, String nodeID, String messageBoxURL,
            String gfacURL, WorkflowNotifiable notifier) {
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
     *
     * @param portTypeQName
     * @param wsdl
     * @param nodeID
     * @param messageBoxURL
     * @param gfacURL
     * @param notifier
     */
    public EmbeddedGFacInvoker(QName portTypeQName, WsdlDefinitions wsdl, String nodeID, String messageBoxURL,
            String gfacURL, WorkflowNotifiable notifier,String topic,AiravataRegistry registry,String serviceName,XBayaConfiguration config) {
        final String wsdlStr = xsul.XmlConstants.BUILDER.serializeToString(wsdl);
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.wsdlDefinitionObject = wsdl;
        this.messageBoxURL = messageBoxURL;
        this.serviceInformation = wsdlStr;
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);
        this.registry = registry;
        this.topic = topic;
        this.serviceName = serviceName;
        this.failerSent = false;
        this.configuration = config;
    }

    /**
     *
     * @throws XBayaException
     */
    public void setup() throws XBayaException {
        this.notifier.setServiceID(this.nodeID);
    }

    private void setup(WsdlDefinitions definitions) throws XBayaException {
    }

    /**
     *
     * @param operationName
     *            The name of the operation
     * @throws XBayaException
     */
    public void setOperation(String operationName) throws XBayaException {
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
            ServiceDescription serviceDescription = registry.getServiceDescription(this.serviceName);
            ServiceDescriptionType serviceDescriptionType = serviceDescription.getType();
            for (Parameter parameter : serviceDescriptionType.getInputParametersArray()) {
                //todo this implementation doesn't work when there are n number of nodes connecting .. need to fix
                actualParameters.put(parameter, GfacUtils.getInputActualParameter(parameter, XMLUtil.xmlElementToString((XmlElement)value)));
            }
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
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     *
     * @return
     * @throws XBayaException
     */
    public synchronized boolean invoke() throws XBayaException {
        try {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            this.result = executor.submit(new Callable<Boolean>() {
                @SuppressWarnings("boxing")
                public Boolean call() {
                    try {
                        JobContext jobContext = new JobContext(actualParameters,EmbeddedGFacInvoker.this.topic,
                                EmbeddedGFacInvoker.this.serviceName,EmbeddedGFacInvoker.this.gfacURL);
                        GFacConfiguration gFacConfiguration = new GFacConfiguration(EmbeddedGFacInvoker.this.configuration.getMyProxyServer(),
                                EmbeddedGFacInvoker.this.configuration.getMyProxyUsername(),
                            EmbeddedGFacInvoker.this.configuration.getMyProxyPassphrase(),EmbeddedGFacInvoker.this.configuration.getMyProxyLifetime(),
                                EmbeddedGFacInvoker.this.registry, EmbeddedGFacInvoker.this.configuration.getTrustedCertLocation());

                        GfacAPI gfacAPI1 = new GfacAPI();
                        InvocationContext defaultInvocationContext = gfacAPI1.gridJobSubmit(jobContext, gFacConfiguration);
                        ParameterContextImpl outputParamContext = (ParameterContextImpl) defaultInvocationContext
                                .<ActualParameter>getMessageContext("output");
                        if (outputParamContext.getNames().hasNext()) {
                            /*
                            * Process Output
                            */
                            OMFactory fac = OMAbstractFactory.getOMFactory();
                            OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2/xsd", "ns1");
                            OMElement outputElement = fac.createOMElement("invokeResponse", omNs);

                            for (Iterator<String> iterator = outputParamContext.getNames(); iterator.hasNext(); ) {
                                String name = iterator.next();
                                String outputString = outputParamContext.getValue(name).toXML().replaceAll("GFacParameter", name);
                                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(outputString));
                                StAXOMBuilder builder = new StAXOMBuilder(reader);
                                outputElement.addChild(builder.getDocumentElement());
                            }
                            // Send notification
                            logger.info("outputMessage: " + outputElement.toString());
                            EmbeddedGFacInvoker.this.notifier.serviceFinished(new WSIFMessageElement(XMLUtil.stringToXmlElement3(outputElement.toStringWithConsume())));
                        } else {
                            // An implementation of WSIFMessage,
                            // WSIFMessageElement, implements toString(), which
                            // serialize the message XML.
                            EmbeddedGFacInvoker.this.notifier.receivedFault(new WSIFMessageElement(XMLUtil.stringToXmlElement3("<Message>Invocation Failed</Message>")));
                            EmbeddedGFacInvoker.this.failerSent = true;
                        }
                        return true;
                    } catch (XBayaException e) {
                        logger.error(e.getMessage(), e);
                        // An appropriate message has been set in the exception.
                        EmbeddedGFacInvoker.this.notifier.invocationFailed(e.getMessage(), e);
                        EmbeddedGFacInvoker.this.failerSent = true;
                        throw new XBayaRuntimeException(e);
                    } catch (RuntimeException e) {
                        logger.error(e.getMessage(), e);
                        String message = "Error in invoking a service: " + EmbeddedGFacInvoker.this.serviceInformation;
                        EmbeddedGFacInvoker.this.notifier.invocationFailed(message, e);
                        EmbeddedGFacInvoker.this.failerSent = true;
                        throw e;
                    } catch (XMLStreamException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    } catch (Exception e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                    return false;
                }
            });
            // Kill the thread inside of executor. This is necessary for Jython
            // script to finish.
//            executor.shutdown();

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
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
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
