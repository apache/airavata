package org.apache.airavata.services.gfac.axis2.reciever;

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

import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.commons.gfac.wsdl.GFacSchemaConstants;
import org.apache.airavata.commons.gfac.wsdl.WSDLConstants;
import org.apache.airavata.core.gfac.GfacAPI;
import org.apache.airavata.core.gfac.context.GFacConfiguration;
import org.apache.airavata.core.gfac.context.JobContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultInvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.airavata.core.gfac.utils.GfacUtils;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.util.RegistryUtils;
import org.apache.airavata.registry.api.util.WebServiceUtil;
import org.apache.airavata.schemas.gfac.Parameter;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.apache.airavata.services.gfac.axis2.GFacService;
import org.apache.airavata.services.gfac.axis2.util.GFacServiceOperations;
import org.apache.airavata.services.gfac.axis2.util.WSConstants;
import org.apache.airavata.services.gfac.axis2.util.WSDLUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.Utils;
import org.apache.xmlbeans.XmlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacMessageReciever implements MessageReceiver {

    private static Logger log = LoggerFactory.getLogger(GFacMessageReciever.class);
    public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
    public static final String MYPROXY_SERVER = "myproxy.server";
    public static final String MYPROXY_USER = "myproxy.user";
    public static final String MYPROXY_PASS = "myproxy.pass";
    public static final String MYPROXY_LIFE = "myproxy.life";
    public static final String GFAC_URL = "GFacURL";
    public static final String REPOSITORY_PROPERTIES = "airavata-server.properties";
    private GFacConfiguration gfacContext;
    private GenericService service;
//    private AiravataRegistry2 registry;
    private  GfacAPI gfacAPI;

    public void receive(org.apache.axis2.context.MessageContext axisRequestMsgCtx) throws AxisFault {
        GFacServiceOperations operation = GFacServiceOperations.valueFrom(axisRequestMsgCtx.getOperationContext()
                .getOperationName());
        switch (operation) {
        case GETABSTRACTWSDL:
            try {
                log.debug("invoking getAbstractWSDL operation");
                processgetAbstractWSDLOperation(axisRequestMsgCtx);
                log.debug("getAbstractWSDL operation invoked");
            } catch (Exception e) {
                throw new AxisFault("Error retrieving the WSDL", e);
            }

            break;
        case INVOKE:
            try {
                ContextHeaderDocument document  = ContextHeaderDocument.Factory.parse(getHeader(axisRequestMsgCtx).toStringWithConsume());
                log = LoggerFactory.getLogger(GFacMessageReciever.class + "." + document.getContextHeader().getWorkflowMonitoringContext().getExperimentId());
                log.debug("invoking Invoke operation");
                processInvokeOperation(axisRequestMsgCtx);
                log.info(axisRequestMsgCtx.getEnvelope().getBody().getFirstElement().toString());
                log.info("Invoke operation invoked !!");
            } catch (Exception e) {
                throw new AxisFault("Error Invoking the service", e);
            }
            break;
            case GETWSDL:
            try {
                log.debug("invoking getWSDL operation");
                processgetWSDLOperation(axisRequestMsgCtx);
                log.info("getWSDL operation invoked !!");
            } catch (Exception e) {
                throw new AxisFault("Error retrieving the WSDL", e);
            }
            break;
        }
    }

    private void processInvokeOperation(MessageContext messageContext) throws Exception {
        final MessageContext finalMessageContext = messageContext;
        MessageContext response = null;
        final String serviceName = getOriginalServiceName(messageContext);
        final EndpointReference replyTo = messageContext.getReplyTo();
        try {
            /*
             * We assume that input likes <invoke> <input_param_name1>value</input_param_name1>
             * <input_param_name2>value</input_param_name2> <input_param_name3>value</input_param_name3> </invoke>
             */
            final OMElement invoke = messageContext.getEnvelope().getBody().getFirstElement();
            /*
             * We assume that output likes <invokeResponse> <output_param_name1>value</output_param_name1>
             * <output_param_name2>value</output_param_name2> <output_param_name3>value</output_param_name3>
             * </invokeResponse>
             */
            new Thread() {
                @Override
                public void run() {
                    try {
                        invokeApplication(serviceName, invoke, finalMessageContext);
                    } catch (Exception e) {
                        // Ignore the error.
                        log.error("Error invoking GFac Service",e);
                    }
                }
            }.start();
        } catch (Exception e) {
            throw e;
        }
    }

    private OMElement invokeApplication(String serviceName, OMElement input, MessageContext messageContext)
            throws Exception {
        ConfigurationContext context = messageContext.getConfigurationContext();
        String brokerURL = getEventBrokerURL(messageContext);
        String topic = getTopic(messageContext);
        OMElement outputElement = null;
        OMElement header = getHeader(messageContext);
        ContextHeaderDocument document = null;
        try {
            document = ContextHeaderDocument.Factory.parse(header.toStringWithConsume());
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (XmlException e) {
            e.printStackTrace();
        }
        //Set the WorkflowContext Header to the ThreadLocal of the Gfac Service, so that this can be accessed easilly
        WorkflowContextHeaderBuilder.setCurrentContextHeader(document.getContextHeader());
        Map<Parameter,ActualParameter> actualParameters = new LinkedHashMap<Parameter,ActualParameter>();
        ServiceDescription serviceDescription = getRegistry(context).getServiceDescriptor(serviceName);
        if(serviceDescription==null){
        	throw new RegistryException(new Exception("Service Description not found in registry."));
        }
        ServiceDescriptionType serviceDescriptionType = serviceDescription.getType();
        for (Parameter parameter : serviceDescriptionType.getInputParametersArray()) {
            OMElement element = input.getFirstChildWithName(new QName(null,parameter.getParameterName().replaceAll(WSDLConstants.HYPHEN, WSDLConstants.HYPHEN_REPLACEMENT)));
            if(element == null){
                element = input.getFirstChildWithName(new QName(GFacSchemaConstants.GFAC_NAMESPACE,parameter.getParameterName().replaceAll(WSDLConstants.HYPHEN, WSDLConstants.HYPHEN_REPLACEMENT)));
            }
            if (element == null) {
                throw new Exception(parameter.getParameterName() + " Parameter is not found in the message or Parameter have wrong charactor in it");
            }
            //todo this implementation doesn't work when there are n number of nodes connecting .. need to fix
            actualParameters.put(parameter, GfacUtils.getInputActualParameter(parameter, element));
        }
        DefaultInvocationContext invocationContext = null;
        JobContext jobContext = new JobContext(actualParameters,topic,serviceName,brokerURL);
        //TODO: send security context header from Xbaya and handle it
//        if(document.getContextHeader().getSecurityContext().getAmazonWebservices() != null){
////            invocationContext.getExecutionContext().setSecurityContextHeader(header);
//            //todo if there's amazoneWebServices context we need to set that value, this will refer in EC2Provider
//        }else {
        try {

            gfacAPI = new GfacAPI();
            String workflowNodeId = WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowMonitoringContext().getWorkflowNodeId();
            String workflowInstanceId = WorkflowContextHeaderBuilder.getCurrentContextHeader().getWorkflowMonitoringContext().getWorkflowInstanceId();
            invocationContext = gfacAPI.gridJobSubmit(jobContext, (GFacConfiguration) context.getProperty(GFacService.GFAC_CONFIGURATION),workflowNodeId,workflowInstanceId);
            /*
             * Add notifiable object
             */

            ParameterContextImpl outputParamContext = (ParameterContextImpl) invocationContext
                    .<ActualParameter> getMessageContext("output");
            /*
             * Process Output
             */
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2/xsd", "ns1");
            outputElement = fac.createOMElement("invokeResponse", omNs);

            for (Iterator<String> iterator = outputParamContext.getNames(); iterator.hasNext();) {
                String name = iterator.next();
                String outputString = outputParamContext.getValue(name).toXML().replaceAll("GFacParameter", name);
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(outputString));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                outputElement.addChild(builder.getDocumentElement());
            }

        } catch (Exception e) {
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2/xsd", "ns1");
            outputElement = fac.createOMElement("ErrorResponse", omNs);
            outputElement.setText("Invocation failed" + e.getMessage());
            log.error("Error in invoking service", e);
            SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
            sf.createSOAPBody(responseEnv);
            responseEnv.getBody().addChild(outputElement);
            MessageContext outMessageContext = MessageContextBuilder.createOutMessageContext(messageContext);
            outMessageContext.setEnvelope(responseEnv);
            AxisEngine.send(outMessageContext);
            throw e;
        }
//        }

        SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
        sf.createSOAPBody(responseEnv);
        responseEnv.getBody().addChild(outputElement);
        MessageContext outMessageContext = MessageContextBuilder.createOutMessageContext(messageContext);
        outMessageContext.setEnvelope(responseEnv);
        AxisEngine.send(outMessageContext);
        return outputElement;
    }

    private void processgetWSDLOperation(MessageContext messageContext) throws Exception {
        MessageContext response = null;
        EndpointReference gfacUrl = new EndpointReference((String)messageContext.getConfigurationContext().getProperty(GFAC_URL));
        String serviceName = getOriginalServiceName(messageContext);
        String serviceEpr = gfacUrl.getAddress().split(WSConstants.GFAC_SERVICE_NAME)[0] + serviceName;
        ConfigurationContext context = messageContext.getConfigurationContext();

        try {
            OMElement wsdlElement = getWSDL(context, serviceName);

            // create Concrete WSDL
            String cWSDL = WSDLUtil.createCWSDL(wsdlElement.toString(), serviceEpr);

            SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
            sf.createSOAPBody(responseEnv);

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(
                    new StringReader(cWSDL.toString()));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            responseEnv.getBody().addChild(builder.getDocumentElement());
            response = MessageContextBuilder.createOutMessageContext(messageContext);
            response.setEnvelope(responseEnv);
            response.getOperationContext().addMessageContext(response);
            AxisEngine.send(response);
        } catch (Exception fault) {
            log.error("Error creating response");
            throw fault;
        }
    }

    private void processgetAbstractWSDLOperation(MessageContext messageContext) throws Exception {
        MessageContext response = null;
        String serviceName = getOriginalServiceName(messageContext);
        ConfigurationContext context = messageContext.getConfigurationContext();
        // TODO this logic has to change based on the logic we are storing data
        // into repository
        try {
            OMElement wsdlElement = getWSDL(context, serviceName);
            SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
            sf.createSOAPBody(responseEnv);
            responseEnv.getBody().addChild(wsdlElement);
            response = MessageContextBuilder.createOutMessageContext(messageContext);
            response.setEnvelope(responseEnv);
            response.getOperationContext().addMessageContext(response);
            AxisEngine.send(response);
        } catch (Exception fault) {
            log.error("Error creating response");
            throw fault;
        }
    }

    /**
     * Get Abstract WSDL and build it as OMElement
     *
     * @param context
     * @param serviceName
     * @return
     * @throws XMLStreamException
     */
    private OMElement getWSDL(ConfigurationContext context, String serviceName) throws XMLStreamException {
        String WSDL = null;
		try {
 			WSDL = WebServiceUtil.getWSDL(getRegistry(context).getServiceDescriptor(serviceName));
		} catch (RegistryException e) {
			//TODO this scenario occur if the service is not present in the registry.
			//someone should handle this
		} catch (Exception e) {
			//TODO this scenario occur if something something something.
			//someone should handle this
			e.printStackTrace();
		}
        XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(WSDL));
        StAXOMBuilder builder = new StAXOMBuilder(reader);
        OMElement wsdlElement = builder.getDocumentElement();
        return wsdlElement;
    }

    /**
     * Get Registry Object in the configuration context
     *
     * @param context
     * @return
     *
     */
    private AiravataRegistry2 getRegistry(ConfigurationContext context) {
        return RegistryUtils.getRegistryFromServerSettings();
    }

    private String getOriginalServiceName(MessageContext messageContext) {
        String toAddress = messageContext.getTo().getAddress();
        String[] values = Utils.parseRequestURLForServiceAndOperation(toAddress, messageContext
                .getConfigurationContext().getServiceContextPath());
        return values[0];
    }

    private String getEventBrokerURL(MessageContext context) {
        SOAPHeader header = context.getEnvelope().getHeader();
        OMElement contextHeader = header.getFirstChildWithName(new QName(
                "http://airavata.apache.org/schemas/wec/2012/05", "context-header"));
        String address = null;
        try {
            ContextHeaderDocument document = ContextHeaderDocument.Factory.parse(contextHeader.toStringWithConsume());
            address = document.getContextHeader().getWorkflowMonitoringContext().getEventPublishEpr();
        } catch (XmlException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        } catch (XMLStreamException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
        return address;
    }

    private String getTopic(MessageContext context) {
        OMElement contextHeader = getHeader(context);
        String topic = null;
        try {
            ContextHeaderDocument document = ContextHeaderDocument.Factory.parse(contextHeader.toStringWithConsume());
            topic = document.getContextHeader().getWorkflowMonitoringContext().getExperimentId();
        } catch (XmlException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        return topic;
    }

    private OMElement getHeader(MessageContext context) {
        SOAPHeader header = context.getEnvelope().getHeader();
        OMElement contextHeader = header.getFirstChildWithName(new QName(
                "http://airavata.apache.org/schemas/wec/2012/05", "context-header"));
        return contextHeader;
    }
}
