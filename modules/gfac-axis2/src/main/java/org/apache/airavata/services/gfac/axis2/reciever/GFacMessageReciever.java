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
import java.net.URI;
import java.util.Iterator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.commons.gfac.type.ActualParameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultInvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.context.message.impl.WorkflowContextImpl;
import org.apache.airavata.core.gfac.context.security.impl.GSISecurityContext;
import org.apache.airavata.core.gfac.factory.PropertyServiceFactory;
import org.apache.airavata.core.gfac.notification.impl.LoggingNotification;
import org.apache.airavata.core.gfac.notification.impl.WorkflowTrackingNotification;
import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.airavata.registry.api.Axis2Registry;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.schemas.gfac.BooleanArrayType;
import org.apache.airavata.schemas.gfac.BooleanParameterType;
import org.apache.airavata.schemas.gfac.DoubleArrayType;
import org.apache.airavata.schemas.gfac.DoubleParameterType;
import org.apache.airavata.schemas.gfac.FileArrayType;
import org.apache.airavata.schemas.gfac.FileParameterType;
import org.apache.airavata.schemas.gfac.FloatArrayType;
import org.apache.airavata.schemas.gfac.FloatParameterType;
import org.apache.airavata.schemas.gfac.IntegerArrayType;
import org.apache.airavata.schemas.gfac.IntegerParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.Parameter;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.airavata.schemas.gfac.StringArrayType;
import org.apache.airavata.schemas.gfac.StringParameterType;
import org.apache.airavata.schemas.gfac.URIArrayType;
import org.apache.airavata.schemas.gfac.URIParameterType;
import org.apache.airavata.schemas.wec.ContextHeaderDocument;
import org.apache.airavata.schemas.wec.SecurityContextDocument;
import org.apache.airavata.services.gfac.axis2.GFacService;
import org.apache.airavata.services.gfac.axis2.util.GFacServiceOperations;
import org.apache.airavata.services.gfac.axis2.util.WSConstants;
import org.apache.airavata.services.gfac.axis2.util.WSDLUtil;
import org.apache.airavata.wsmg.client.WseMsgBrokerClient;
import org.apache.airavata.wsmg.client.WsntMsgBrokerClient;
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

    private static final Logger log = LoggerFactory.getLogger(GFacMessageReciever.class);
    public static final String TRUSTED_CERT_LOCATION = "trusted.cert.location";
    public static final String MYPROXY_SERVER = "myproxy.server";
    public static final String MYPROXY_USER = "myproxy.user";
    public static final String MYPROXY_PASS = "myproxy.pass";
    public static final String MYPROXY_LIFE = "myproxy.life";
    
    private GenericService service;
    private Axis2Registry registry;

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
                log.debug("invoking Invoke operation");
                processInvokeOperation(axisRequestMsgCtx);
                log.info(axisRequestMsgCtx.getEnvelope().getBody().getFirstElement().toStringWithConsume());
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
                        invokeApplication(replyTo,serviceName, invoke, finalMessageContext);
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

    private OMElement invokeApplication(EndpointReference msgBoxAddr,String serviceName, OMElement input, MessageContext messageContext)
            throws Exception {
        ConfigurationContext context = messageContext.getConfigurationContext();
        String brokerURL = getEventBrokerURL(messageContext);
        String topic = getTopic(messageContext);
        OMElement outputElement = null;
        try {
            /*
             * Add notifiable object
             */
            WorkflowTrackingNotification workflowNotification = new WorkflowTrackingNotification(brokerURL, topic);
            LoggingNotification loggingNotification = new LoggingNotification();
            DefaultInvocationContext invocationContext = new DefaultInvocationContext();
            invocationContext.setExecutionContext(new DefaultExecutionContext());
            invocationContext.setServiceName(serviceName);
            invocationContext.getExecutionContext().setRegistryService(getRegistry(context));
            invocationContext.getExecutionContext().setSecurityContextHeader(getHeader(messageContext));
            invocationContext.getExecutionContext().addNotifiable(workflowNotification);
            invocationContext.getExecutionContext().addNotifiable(loggingNotification);

            GSISecurityContext gssContext = new GSISecurityContext();
            SecurityContextDocument parse =
                    SecurityContextDocument.Factory.parse(getHeader(messageContext).getFirstChildWithName
                            (new QName("http://schemas.airavata.apache.org/workflow-execution-context", "security-context")).toStringWithConsume());
            SecurityContextDocument.SecurityContext.GridMyproxyRepository gridMyproxyRepository = parse.getSecurityContext().getGridMyproxyRepository();
            if (gridMyproxyRepository==null){
            	gssContext.setMyproxyPasswd((String)messageContext.getConfigurationContext().getProperty(MYPROXY_PASS));
                gssContext.setMyproxyUserName((String)messageContext.getConfigurationContext().getProperty(MYPROXY_USER));
                gssContext.setMyproxyLifetime(Integer.parseInt((String)messageContext.getConfigurationContext().getProperty(MYPROXY_LIFE)));
                gssContext.setMyproxyServer((String)messageContext.getConfigurationContext().getProperty(MYPROXY_SERVER));	
            }else{
	            gssContext.setMyproxyPasswd(gridMyproxyRepository.getPassword());
	            gssContext.setMyproxyUserName(gridMyproxyRepository.getUsername());
	            gssContext.setMyproxyLifetime(gridMyproxyRepository.getLifeTimeInhours());
	            gssContext.setMyproxyServer(gridMyproxyRepository.getMyproxyServer());
            }
            gssContext.setTrustedCertLoc((String)messageContext.getConfigurationContext().getProperty(TRUSTED_CERT_LOCATION));
            
            invocationContext.addSecurityContext("myproxy",gssContext);

            /*
             * Add workflow context
             */
            WorkflowContextImpl workflowContext = new WorkflowContextImpl();
            workflowContext.setValue(WorkflowContextImpl.WORKFLOW_ID, URI.create(topic).toString());
            invocationContext.addMessageContext(WorkflowContextImpl.WORKFLOW_CONTEXT_NAME, workflowContext);

            /*
             * read from registry and set the correct parameters
             */
            ServiceDescription serviceDescription = getRegistry(context).getServiceDescription(serviceName);

            /*
             * Input
             */
            ParameterContextImpl inputParam = new ParameterContextImpl();
            ServiceDescriptionType serviceDescriptionType = serviceDescription.getType();

            for (Parameter parameter : serviceDescriptionType.getInputParametersArray()) {
                OMElement element = input.getFirstChildWithName(new QName(null,parameter.getParameterName()));
                if (element == null) {
                    throw new Exception("Parameter is not found in the message");
                }
                //todo this implementation doesn't work when there are n number of nodes connecting .. need to fix

//                String xmlContent = "";
//                if(!element.getChildElements().hasNext()){
//                    xmlContent = "<type:GFacParameter xsi:type=\"type:" + MappingFactory.getActualParameterType(parameter.getParameterType().getType())
//                        +"\" xmlns:type=\"http://schemas.airavata.apache.org/gfac/type\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + element.getText() + "</type:GFacParameter>";
//                }else{
//                    xmlContent = "<type:GFacParameter xsi:type=\"type:" + MappingFactory.getActualParameterType(parameter.getParameterType().getType())
//                            +"\" xmlns:type=\"http://schemas.airavata.apache.org/gfac/type\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" + element.toStringWithConsume() + "</type:GFacParameter>";
//                }
//                System.out.println(xmlContent);
//                inputParam.add(parameter.getParameterName(),ActualParameter.fromXML(xmlContent));

                ActualParameter actualParameter = getInputActualParameter(parameter, element);
                inputParam.add(parameter.getParameterName(),actualParameter);
            }

            /*
             * Output
             */
            ParameterContextImpl outputParam = new ParameterContextImpl();


            // List<Parameter> outputs = serviceDescription.getOutputParameters();
            for (OutputParameterType parameter : serviceDescriptionType.getOutputParametersArray()) {
                ActualParameter actualParameter = new ActualParameter();
                if("String".equals(parameter.getParameterType().getName())){
                   actualParameter.getType().changeType(StringParameterType.type);
                }else if("Double".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(DoubleParameterType.type);
                }else if("Integer".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(IntegerParameterType.type);
                }else if("Float".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(FloatParameterType.type);
                }else if("Boolean".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(BooleanParameterType.type);
                }else if("File".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(FileParameterType.type);
                }else if("URI".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(URIParameterType.type);
                }else if("StringArray".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(StringArrayType.type);
                }else if("DoubleArray".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(DoubleArrayType.type);
                }else if("IntegerArray".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(IntegerArrayType.type);
                }else if("FloatArray".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(FloatArrayType.type);
                }else if("BooleanArray".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(BooleanArrayType.type);
                }else if("FileArray".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(FileArrayType.type);
                }else if("URIArray".equals(parameter.getParameterType().getName())){
                    actualParameter.getType().changeType(URIArrayType.type);
                }
                outputParam.add(parameter.getParameterName(), new ActualParameter());
            }

            invocationContext.setInput(inputParam);
            invocationContext.setOutput(outputParam);

            if (service == null) {
                service = new PropertyServiceFactory(GFacService.REPOSITORY_PROPERTIES).createService();
            }
            // invoke service
            service.execute(invocationContext);

            /*
             * Process Output
             */
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2/xsd", "ns1");
            outputElement = fac.createOMElement("invokeResponse", omNs);

            ParameterContextImpl paramContext = (ParameterContextImpl) invocationContext
                    .<ActualParameter> getMessageContext("output");
            for (Iterator<String> iterator = paramContext.getNames(); iterator.hasNext();) {
                String name = iterator.next();
                String outputString = paramContext.getValue(name).toXML().replaceAll("GFacParameter", name);
                XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(outputString));
                StAXOMBuilder builder = new StAXOMBuilder(reader);
                outputElement.addChild(builder.getDocumentElement());
            }

        } catch (Exception e) {
            log.error("Error in invoking service", e);
            throw e;
        }

        SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
        sf.createSOAPBody(responseEnv);
        responseEnv.getBody().addChild(outputElement);
        MessageContext outMessageContext = MessageContextBuilder.createOutMessageContext(messageContext);
        outMessageContext.setEnvelope(responseEnv);
        AxisEngine.send(outMessageContext);
        return outputElement;
    }

    private ActualParameter getInputActualParameter(Parameter parameter, OMElement element) {
        OMElement innerelement = null;
        ActualParameter actualParameter = new ActualParameter();
        if("String".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(StringParameterType.type);
            innerelement = (OMElement)element.getChildrenWithLocalName("value").next();
            ((StringParameterType)actualParameter.getType()).setValue(innerelement.getText());
        }else if("Double".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(DoubleParameterType.type);
            innerelement = (OMElement)element.getChildrenWithLocalName("value").next();
            ((DoubleParameterType)actualParameter.getType()).setValue(new Double(innerelement.getText()));
        }else if("Integer".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(IntegerParameterType.type);
            innerelement = (OMElement)element.getChildrenWithLocalName("value").next();
            ((IntegerParameterType)actualParameter.getType()).setValue(new Integer(innerelement.getText()));
        }else if("Float".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(FloatParameterType.type);
            innerelement = (OMElement)element.getChildrenWithLocalName("value").next();
            ((FloatParameterType)actualParameter.getType()).setValue(new Float(innerelement.getText()));
        }else if("Boolean".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(BooleanParameterType.type);
            innerelement = (OMElement)element.getChildrenWithLocalName("value").next();
            ((BooleanParameterType)actualParameter.getType()).setValue(new Boolean(innerelement.getText()));
        }else if("File".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(FileParameterType.type);
            innerelement = (OMElement)element.getChildrenWithLocalName("value").next();
            ((FileParameterType)actualParameter.getType()).setValue(innerelement.getText());
        }else if("URI".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(URIParameterType.type);
            innerelement = (OMElement)element.getChildrenWithLocalName("value").next();
            System.out.println(actualParameter.getType().toString());
            log.debug(actualParameter.getType().toString());
            ((URIParameterType)actualParameter.getType()).setValue(innerelement.getText());
        }else if("StringArray".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(StringArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i =0;
            while(value.hasNext()){
                innerelement = (OMElement)value.next();
                ((StringArrayType)actualParameter.getType()).insertValue(i++, innerelement.getText());
            }
        }else if("DoubleArray".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(DoubleArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
            int i =0;
            while(value.hasNext()){
                innerelement = (OMElement)value.next();
                ((DoubleArrayType)actualParameter.getType()).insertValue(i++,new Double(innerelement.getText()));
            }
        }else if("IntegerArray".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(IntegerArrayType.type);
              Iterator value = element.getChildrenWithLocalName("value");
            int i =0;
            while(value.hasNext()){
                innerelement = (OMElement)value.next();
                ((IntegerArrayType)actualParameter.getType()).insertValue(i++,new Integer(innerelement.getText()));
            }
        }else if("FloatArray".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(FloatArrayType.type);
              Iterator value = element.getChildrenWithLocalName("value");
            int i =0;
            while(value.hasNext()){
                innerelement = (OMElement)value.next();
                ((FloatArrayType)actualParameter.getType()).insertValue(i++,new Float(innerelement.getText()));
            }
        }else if("BooleanArray".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(BooleanArrayType.type);
              Iterator value = element.getChildrenWithLocalName("value");
            int i =0;
            while(value.hasNext()){
                innerelement = (OMElement)value.next();
                ((BooleanArrayType)actualParameter.getType()).insertValue(i++,new Boolean(innerelement.getText()));
            }
        }else if("FileArray".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(FileArrayType.type);
              Iterator value = element.getChildrenWithLocalName("value");
            int i =0;
            while(value.hasNext()){
                innerelement = (OMElement)value.next();
                ((FileArrayType)actualParameter.getType()).insertValue(i++,innerelement.getText());
            }
        }else if("URIArray".equals(parameter.getParameterType().getName())){
            actualParameter = new ActualParameter(URIArrayType.type);
            Iterator value = element.getChildrenWithLocalName("value");
          int i =0;
          while(value.hasNext()){
              innerelement = (OMElement)value.next();
              ((URIArrayType)actualParameter.getType()).insertValue(i++,innerelement.getText());
          }
        }
        return actualParameter;
    }

    private void processgetWSDLOperation(MessageContext messageContext) throws Exception {
        MessageContext response = null;
        EndpointReference gfacUrl = messageContext
                .getConfigurationContext()
                .getListenerManager()
                .getEPRforService(WSConstants.GFAC_SERVICE_NAME, WSConstants.GFAC_INVOKE_METHOD,
                        WSConstants.GFAC_TRANSPORT);
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
 			WSDL = getRegistry(context).getWSDL(serviceName);
		} catch (RegistryException e) {
			//TODO this scenario occur if the service is not present in the registry.
			//someone should handle this 
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
    private Axis2Registry getRegistry(ConfigurationContext context) {
        if (this.registry == null) {
            this.registry = (Axis2Registry) context.getProperty(GFacService.CONFIGURATION_CONTEXT_REGISTRY);
        }
        return registry;
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
                "http://schemas.airavata.apache.org/workflow-execution-context", "context-header"));
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
        SOAPHeader header = context.getEnvelope().getHeader();
        OMElement contextHeader = header.getFirstChildWithName(new QName(
                "http://schemas.airavata.apache.org/workflow-execution-context", "context-header"));
        String topic = null;
        try {
            ContextHeaderDocument document = ContextHeaderDocument.Factory.parse(contextHeader.toStringWithConsume());
            topic = document.getContextHeader().getWorkflowMonitoringContext().getWorkflowInstanceId();
        } catch (XmlException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        } catch (XMLStreamException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
        topic = topic.substring(1);
        return topic.replaceAll("_", "-");
    }

    private OMElement getHeader(MessageContext context) {
        SOAPHeader header = context.getEnvelope().getHeader();
        OMElement contextHeader = header.getFirstChildWithName(new QName(
                "http://schemas.airavata.apache.org/workflow-execution-context", "context-header"));
        return contextHeader;
    }

}
