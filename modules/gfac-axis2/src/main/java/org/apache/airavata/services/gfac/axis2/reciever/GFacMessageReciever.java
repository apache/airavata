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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.airavata.registry.api.Axis2Registry;
import org.apache.airavata.commons.gfac.type.Parameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.commons.gfac.type.parameter.AbstractParameter;
import org.apache.airavata.commons.gfac.type.util.SchemaUtil;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultExecutionContext;
import org.apache.airavata.core.gfac.context.invocation.impl.DefaultInvocationContext;
import org.apache.airavata.core.gfac.context.message.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.factory.PropertyServiceFactory;
import org.apache.airavata.core.gfac.notification.impl.LoggingNotification;
import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.airavata.services.gfac.axis2.GFacService;
import org.apache.airavata.services.gfac.axis2.util.GFacServiceOperations;
import org.apache.airavata.services.gfac.axis2.util.WSDLUtil;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xmlpull.v1.builder.XmlDocument;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.wsdl.WsdlDefinitions;

public class GFacMessageReciever implements MessageReceiver {

    private static final Log log = LogFactory.getLog(GFacMessageReciever.class);
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
                log.info("Invoke operation invoked !!");
            } catch (Exception e) {
                throw new AxisFault("Error Invoking the service", e);
            }
            break;
        case GETWSDL:
            try {
                log.debug("invoking getAbstractWSDL operation");
                processgetWSDLOperation(axisRequestMsgCtx);
                log.info("getWSDL operation invoked !!");
            } catch (Exception e) {
                throw new AxisFault("Error retrieving the WSDL", e);
            }
            break;
        }
    }

    private void processInvokeOperation(MessageContext messageContext) throws Exception {
        MessageContext response = null;
        String serviceName = getOriginalServiceName(messageContext);
        try {
            ConfigurationContext context = messageContext.getConfigurationContext();

            /*
             * We assume that input likes <invoke>
             * <input_param_name1>value</input_param_name1>
             * <input_param_name2>value</input_param_name2>
             * <input_param_name3>value</input_param_name3> </invoke>
             */
            OMElement invoke = messageContext.getEnvelope().getBody().getFirstElement();

            /*
             * We assume that output likes <invokeResponse>
             * <output_param_name1>value</output_param_name1>
             * <output_param_name2>value</output_param_name2>
             * <output_param_name3>value</output_param_name3> </invokeResponse>
             */
            OMElement output = invokeApplication(serviceName, invoke, context);

            SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
            sf.createSOAPBody(responseEnv);
            responseEnv.getBody().addChild(output);
            response = MessageContextBuilder.createOutMessageContext(messageContext);
            response.setEnvelope(responseEnv);
            response.getOperationContext().addMessageContext(response);
            AxisEngine.send(response);
        } catch (Exception e) {
            throw e;
        }
    }

    private OMElement invokeApplication(String serviceName, OMElement input, ConfigurationContext context)
            throws Exception {
        OMElement outputElement = null;
        try {
            LoggingNotification notification = new LoggingNotification();

            DefaultInvocationContext ct = new DefaultInvocationContext();
            ct.setExecutionContext(new DefaultExecutionContext());
            ct.setServiceName(serviceName);
            ct.getExecutionContext().setRegistryService(getRegistry(context));
            ct.getExecutionContext().addNotifiable(notification);

            /*
             * read from registry and set the correct parameters
             */
            ServiceDescription serviceDescription = getRegistry(context).getServiceDescription(serviceName);

            /*
             * Input
             */
            ParameterContextImpl inputParam = new ParameterContextImpl();
            List<Parameter> inputs = serviceDescription.getInputParameters();
            for (Parameter parameter : inputs) {
                OMElement element = input.getFirstChildWithName(new QName(parameter.getName()));

                if (element == null) {
                    throw new Exception("Parameter is not found in the message");
                }

                AbstractParameter param = SchemaUtil.mapFromType(parameter.getType());
                param.parseStringVal(element.getText());
                inputParam.add(parameter.getName(), param);
            }

            /*
             * Output
             */
            ParameterContextImpl outputParam = new ParameterContextImpl();
            List<Parameter> outputs = serviceDescription.getOutputParameters();
            for (Parameter parameter : outputs) {
                outputParam.add(parameter.getName(), SchemaUtil.mapFromType(parameter.getType()));
            }

            ct.setInput(inputParam);
            ct.setOutput(outputParam);

            if (service == null) {
                service = new PropertyServiceFactory(GFacService.REPOSITORY_PROPERTIES).createService();
            }
            // invoke service
            service.execute(ct);

            /*
             * Process Output
             */
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2/xsd", "ns1");
            outputElement = fac.createOMElement("invokeResponse", omNs);

            ParameterContextImpl paramContext = (ParameterContextImpl) ct
                    .<AbstractParameter> getMessageContext("output");
            for (Iterator<String> iterator = paramContext.getNames(); iterator.hasNext();) {
                String name = iterator.next();
                OMElement ele = fac.createOMElement(name, omNs);
                ele.addAttribute("type", paramContext.getValue(name).getType().toString(), omNs);
                ele.setText(paramContext.getValue(name).toStringVal());
                outputElement.addChild(ele);
            }

        } catch (Exception e) {
            log.error("Error in invoking service", e);
            throw e;
        }
        return outputElement;
    }

    private void processgetWSDLOperation(MessageContext messageContext) throws Exception {
        MessageContext response = null;
        EndpointReference gfacUrl = messageContext.getConfigurationContext().getListenerManager()
                .getEPRforService("GFacService", "invoke", "http");
        String serviceName = getOriginalServiceName(messageContext);
        String serviceEpr = gfacUrl.getAddress().split("GFacService")[0] + serviceName;
        ConfigurationContext context = messageContext.getConfigurationContext();
        // TODO this logic has to change based on the logic we are storing data
        // into repository
        try {
            OMElement wsdlElement = getWSDL(context, serviceName);
            XmlInfosetBuilder xmlInfosetBuilder = xsul.XmlConstants.BUILDER;
            XmlDocument document = xmlInfosetBuilder.parseInputStream(new ByteArrayInputStream(wsdlElement.toString()
                    .getBytes()));
            WsdlDefinitions definitions = new WsdlDefinitions(document.getDocumentElement());
            xsul5.wsdl.WsdlDefinitions definition5 = WSDLUtil.wsdlDefinitions3ToWsdlDefintions5(definitions);
            definition5 = xsul5.wsdl.WsdlUtil.createCWSDL(definition5, new URI(serviceEpr));
            definitions = WSDLUtil.wsdlDefinitions5ToWsdlDefintions3(definition5);
            // WSDLReader wsdlReader =
            // WSDLFactory.newInstance().newWSDLReader();
            // ByteArrayInputStream byteArrayInputStream = new
            // ByteArrayInputStream(wsdlElement.toString().getBytes());
            // InputSource source = new InputSource(byteArrayInputStream);
            // Definition wsdlDefinition = wsdlReader.readWSDL(null, source);
            //
            //
            // Map portTypes = wsdlDefinition.getPortTypes();
            // Iterator portIt = portTypes.keySet().iterator();
            // while(portIt.hasNext()){
            // PortType portType = (PortType)portTypes.get(portIt.next());
            // List operations = portType.getOperations();
            // Iterator opIt = operations.iterator();
            // String namespace = portType.getQName().getNamespaceURI();
            //
            // Binding soap11binding = wsdlDefinition.createBinding();
            // soap11binding.setQName(new QName(namespace,serviceName +
            // "Soap11Binding"));
            // soap11binding.setPortType(portType);
            // while(opIt.hasNext()){
            // Operation operation = (Operation)opIt.next();
            // BindingOperation boperation =
            // wsdlDefinition.createBindingOperation();
            // boperation.setName(operation.getName());
            // boperation.setOperation(operation);
            // soap11binding.addBindingOperation(boperation);
            // }
            //
            // opIt = operations.iterator();
            // Binding soap12binding = wsdlDefinition.createBinding();
            // soap12binding.setQName(new QName(namespace,serviceName +
            // "Soap12Binding"));
            // soap12binding.setPortType(portType);
            // while(opIt.hasNext()){
            // Operation operation = (Operation)opIt.next();
            // BindingOperation boperation =
            // wsdlDefinition.createBindingOperation();
            // boperation.setOperation(operation);
            // BindingInput input = wsdlDefinition.createBindingInput();
            // BindingOutput outpout = wsdlDefinition.createBindingOutput();
            // ExtensibilityElement element = new UnknownExtensibilityElement();
            // element.setElementType(new
            // QName("http://schemas.xmlsoap.org/wsdl/soap12/","body"));
            // SOAP12BodyImpl body = new SOAP12BodyImpl()
            // input.addExtensibilityElement();
            // boperation.setBindingInput();
            // boperation.setName(operation.getName());
            // soap12binding.addBindingOperation(boperation);
            // }
            //
            // opIt = operations.iterator();
            // Binding httpBinding = wsdlDefinition.create;
            // httpBinding.setQName(new QName(namespace, serviceName +
            // "httpBinding"));
            // httpBinding.setPortType(portType);
            // while(opIt.hasNext()){
            // Operation operation = (Operation)opIt.next();
            // BindingOperation boperation =
            // wsdlDefinition.createBindingOperation();
            // boperation.setOperation(operation);
            // boperation.setName(operation.getName());
            // httpBinding.addBindingOperation(boperation);
            // }
            // wsdlDefinition.addBinding(soap11binding);
            // wsdlDefinition.addBinding(soap12binding);
            // wsdlDefinition.addBinding(httpBinding);
            //
            // Port soap11port = wsdlDefinition.createPort();
            // Port soap12port = wsdlDefinition.createPort();
            // Port httpPort = wsdlDefinition.createPort();
            //
            //
            // soap11port.setName(serviceName + "HttpSoap11Endpoint");
            // soap12port.setName(serviceName + "HttpSoap12Endpoint");
            // httpPort.setName(serviceName + "HttpEndpoint");
            //
            // soap11port.setBinding(soap11binding);
            // soap12port.setBinding(soap12binding);
            // httpPort.setBinding(httpBinding);
            //
            // Service service = wsdlDefinition.createService();
            // service.setQName(new QName(namespace,serviceName));
            // service.addPort(soap11port);
            // service.addPort(soap12port);
            // service.addPort(httpPort);
            //
            // wsdlDefinition.addService(service);
            // break;
            // }
            //
            // ByteArrayOutputStream out = new ByteArrayOutputStream();
            // WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
            // writer.writeWSDL(wsdlDefinition,out);
            // out.toString();
            // reader = XMLInputFactory.newInstance().createXMLStreamReader(
            // new StringReader(out.toString()));
            // builder = new StAXOMBuilder(reader);
            // wsdlElement = builder.getDocumentElement();
            // TODO based on the abstact wsdl content fill up the required
            // information using wsdl4j api
            SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
            sf.createSOAPBody(responseEnv);

            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(definitions.toString()));
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
    private OMElement getWSDL(ConfigurationContext context, String serviceName) throws XMLStreamException{
        String WSDL = getRegistry(context).getWSDL(serviceName);
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

}
