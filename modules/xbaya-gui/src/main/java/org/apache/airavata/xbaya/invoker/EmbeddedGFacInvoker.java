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

import java.io.StringReader;
import java.util.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.core.gfac.exception.ProviderException;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.GfacAPI;
import org.apache.airavata.core.gfac.context.GFacConfiguration;
import org.apache.airavata.core.gfac.context.JobContext;
import org.apache.airavata.core.gfac.context.invocation.InvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.Parameter;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.jython.lib.ServiceNotifiable;
import org.apache.airavata.xbaya.jython.lib.WorkflowNotifiable;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;

import xsul.wsdl.WsdlDefinitions;
import xsul.wsif.WSIFMessage;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.xwsif_runtime.WSIFClient;

public class EmbeddedGFacInvoker implements Invoker {

    private static final Logger logger = LoggerFactory.getLogger(EmbeddedGFacInvoker.class);

    private String nodeID;

    private QName portTypeQName;

    private String wsdlLocation;

    private String serviceInformation;

    private String messageBoxURL;

    private String gfacURL;

    private Invoker invoker;

    private XBayaConfiguration configuration;


    private Boolean result;

    private ServiceNotifiable notifier;

//    private AiravataRegistry2 registry;

    private String topic;

    private String serviceName;

    private AiravataAPI airavataAPI;
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

    private Object outPut;

    Map<Parameter, ActualParameter> actualParameters = new LinkedHashMap<Parameter, ActualParameter>();

    /**
     * Creates an InvokerWithNotification.
     *
     * @param portTypeQName
     * @param wsdlLocation  The URL of WSDL of the service to invoke
     * @param nodeID        The ID of the service
     * @param notifier      The notification sender
     */
    public EmbeddedGFacInvoker(QName portTypeQName, String wsdlLocation, String nodeID, WorkflowNotifiable notifier) {
        this(portTypeQName, wsdlLocation, nodeID, null, notifier);
    }

    /**
     * Creates an InvokerWithNotification.
     *
     * @param portTypeQName
     * @param wsdlLocation  The URL of WSDL of the service to invoke
     * @param nodeID        The ID of the service
     * @param gfacURL       The URL of GFac service.
     * @param notifier      The notification sender
     */
    public EmbeddedGFacInvoker(QName portTypeQName, String wsdlLocation, String nodeID, String gfacURL,
                               WorkflowNotifiable notifier) {
        this(portTypeQName, wsdlLocation, nodeID, null, gfacURL, notifier);
    }

    /**
     * Creates an InvokerWithNotification.
     *
     * @param portTypeQName
     * @param wsdlLocation  The URL of WSDL of the service to invoke
     * @param nodeID        The ID of the service
     * @param messageBoxURL
     * @param gfacURL       The URL of GFac service.
     * @param notifier      The notification sender
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
     * @param portTypeQName
     * @param wsdl
     * @param nodeID
     * @param messageBoxURL
     * @param gfacURL
     * @param notifier
     */
    public EmbeddedGFacInvoker(QName portTypeQName,
                               WsdlDefinitions wsdl,
                               String nodeID,
                               String messageBoxURL,
                               String gfacURL,
                               WorkflowNotifiable notifier,
                               String topic,
                               AiravataAPI airavataAPI,
                               String serviceName,
                               XBayaConfiguration config) {
        final String wsdlStr = xsul.XmlConstants.BUILDER.serializeToString(wsdl);
        this.nodeID = nodeID;
        this.portTypeQName = portTypeQName;
        this.wsdlDefinitionObject = wsdl;
        this.messageBoxURL = messageBoxURL;
        this.serviceInformation = wsdlStr;
        this.gfacURL = gfacURL;
        this.notifier = notifier.createServiceNotificationSender(nodeID);
        this.airavataAPI = airavataAPI;
        this.topic = topic;
        this.serviceName = serviceName;
        this.failerSent = false;
        this.configuration = config;
    }

    /**
     * @throws WorkflowException
     */
    public void setup() throws WorkflowException {
        this.notifier.setServiceID(this.nodeID);
    }

    private void setup(WsdlDefinitions definitions) throws WorkflowException {
    }

    /**
     * @param operationName The name of the operation
     * @throws WorkflowException
     */
    public void setOperation(String operationName) throws WorkflowException {
    }

    /**
     * @param name  The name of the input parameter
     * @param value The value of the input parameter
     * @throws WorkflowException
     */
    public void setInput(String name, Object value) throws WorkflowException {
        try {
            if (value instanceof XmlElement) {
                logger.debug("value: " + XMLUtil.xmlElementToString((XmlElement) value));
            }
            this.inputNames.add(name);
            this.inputValues.add(value);
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error in setting an input. name: " + name + " value: " + value;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * @return
     * @throws WorkflowException
     */
    public synchronized boolean invoke() throws WorkflowException {
        try {
            OMElement inputMessage = createActualParameters();
            Object wsifMessageElement = new WSIFMessageElement(XMLUtil.stringToXmlElement3(inputMessage.toStringWithConsume()));
            this.notifier.invokingService(new WSIFMessageElement((XmlElement)wsifMessageElement));
            JobContext jobContext = new JobContext(actualParameters, EmbeddedGFacInvoker.this.topic,
                    EmbeddedGFacInvoker.this.serviceName, EmbeddedGFacInvoker.this.gfacURL);
            GFacConfiguration gFacConfiguration = new GFacConfiguration(EmbeddedGFacInvoker.this.configuration.getMyProxyServer(),
                    EmbeddedGFacInvoker.this.configuration.getMyProxyUsername(),
                    EmbeddedGFacInvoker.this.configuration.getMyProxyPassphrase(), EmbeddedGFacInvoker.this.configuration.getMyProxyLifetime(),
                    EmbeddedGFacInvoker.this.airavataAPI, EmbeddedGFacInvoker.this.configuration.getTrustedCertLocation());

            GfacAPI gfacAPI1 = new GfacAPI();
            InvocationContext defaultInvocationContext = gfacAPI1.gridJobSubmit(jobContext,
                    gFacConfiguration, this.nodeID, this.notifier.getWorkflowID().toASCIIString());
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
                logger.debug("outputMessage: " + outputElement.toString());
                outPut = new WSIFMessageElement(XMLUtil.stringToXmlElement3(outputElement.toStringWithConsume()));
                this.result = true;
                EmbeddedGFacInvoker.this.notifier.serviceFinished(new WSIFMessageElement((XmlElement) outPut));
            } else {
                // An implementation of WSIFMessage,
                // WSIFMessageElement, implements toString(), which
                // serialize the message XML.
                EmbeddedGFacInvoker.this.notifier.receivedFault(new WSIFMessageElement(XMLUtil.stringToXmlElement3("<Message>Invocation Failed</Message>")));
                EmbeddedGFacInvoker.this.failerSent = true;
            }


            // Check if the invocation itself fails. This happens immediately.
//            try {
//                this.result.get(100, TimeUnit.MILLISECONDS);
//            } catch (InterruptedException e) {
//                logger.error(e.getMessage(), e);
//            } catch (TimeoutException e) {
//                // The job is probably running fine.
//                // The normal case.
//                return true;
//            } catch (ExecutionException e) {
//                // The service-failed notification should have been sent
//                // already.
//                logger.error(e.getMessage(), e);
//                String message = "Error in invoking a service: " + this.serviceInformation;
//                throw new WorkflowException(message, e);
//            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error in invoking a service: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch(ProviderException e){
            if (e.getAditionalInfo().length > 0) {
                this.notifier.invocationFailed(e.getMessage() + "\n" + e.getAditionalInfo()[0], e);
            } else {
                this.notifier.invocationFailed(e.getMessage(), e);
            }
            throw new WorkflowException(e.getMessage(), e);
        } catch (Exception e) {
            this.notifier.invocationFailed(e.getMessage(), e);
            throw new WorkflowException(e.getMessage(), e);
        }
        return true;
    }

    /**
     * @throws WorkflowException
     */
    @SuppressWarnings("boxing")
    public synchronized void waitToFinish() throws WorkflowException {
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
            Boolean success = this.result;
            if (success == false) {
                WSIFMessage faultMessage = this.invoker.getFault();
                String message = "Error in a service: ";
                // An implementation of WSIFMessage,
                // WSIFMessageElement, implements toString(), which
                // serialize the message XML.
                message += faultMessage.toString();
                throw new WorkflowException(message);
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            String message = "Error while waiting for a service to finish: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        }
    }

    /**
     * @param name The name of the output parameter
     * @return
     * @throws WorkflowException
     */
    public Object getOutput(String name) throws WorkflowException {
        try {
            waitToFinish();
            if (outPut instanceof XmlElement) {
                    return ((XmlElement)((XmlElement)((XmlElement) outPut).children().next()).children().next()).children().next();
            } else  {
                return outPut;
            }
        } catch (WorkflowException e) {
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
            throw new WorkflowException(message, e);
        } catch (Error e) {
            logger.error(e.getMessage(), e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new WorkflowException(message, e);
        }
    }

    /**
     * @return
     * @throws WorkflowException
     */
    public WSIFMessage getOutputs() throws WorkflowException {
        return this.invoker.getOutputs();
    }

    @Override
    public WSIFClient getClient() {
        return null;
    }

    @Override
    public WSIFMessage getInputs() throws WorkflowException {
        return null;
    }

    @Override
    public WSIFMessage getFault() throws WorkflowException {
        return null;
    }

    private OMElement createActualParameters() throws AiravataAPIInvocationException, RegistryException, XMLStreamException {
        OMFactory omFactory = OMAbstractFactory.getOMFactory();
        OMElement invoke_inputParams = omFactory.createOMElement(new QName("invoke_InputParams"));
        ServiceDescription serviceDescription = airavataAPI.getApplicationManager().getServiceDescription(this.serviceName);
        if (serviceDescription == null) {
            throw new RegistryException(new Exception("Service Description not found in registry."));
        }
        ServiceDescriptionType serviceDescriptionType = serviceDescription.getType();
        for (String inputName : this.inputNames) {
            OMElement omElement = omFactory.createOMElement(new QName(inputName));
            int index = this.inputNames.indexOf(inputName);
            Object value = this.inputValues.get(index);
            InputParameterType parameter = serviceDescriptionType.getInputParametersArray(index);
            if (value instanceof XmlElement) {
                    omElement.setText((String)((XmlElement)((XmlElement)((XmlElement) value).children().next()).children().next()).children().next());
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(XMLUtil.xmlElementToString((XmlElement) value)));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                OMElement input = builder.getDocumentElement();
                actualParameters.put(parameter, GfacUtils.getInputActualParameter(parameter, input));
            } else if (value instanceof String) {
                omElement.setText((String)value);
                actualParameters.put(parameter, GfacUtils.getInputActualParameter(parameter, AXIOMUtil.stringToOM("<value>" + value + "</value>")));
            }
            invoke_inputParams.addChild(omElement);
        }
        return invoke_inputParams;
    }
}
