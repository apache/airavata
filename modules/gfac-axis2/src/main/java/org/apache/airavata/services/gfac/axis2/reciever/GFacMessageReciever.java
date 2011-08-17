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

import com.sun.tools.doclets.internal.toolkit.Configuration;
import org.apache.airavata.core.gfac.api.impl.JCRRegistry;
import org.apache.airavata.core.gfac.context.InvocationContext;
import org.apache.airavata.core.gfac.context.SecurityContext;
import org.apache.airavata.core.gfac.context.impl.ExecutionContextImpl;
import org.apache.airavata.core.gfac.context.impl.ParameterContextImpl;
import org.apache.airavata.core.gfac.factory.PropertyServiceFactory;
import org.apache.airavata.core.gfac.notification.DummyNotification;
import org.apache.airavata.core.gfac.registry.RegistryService;
import org.apache.airavata.core.gfac.services.GenericService;
import org.apache.airavata.core.gfac.type.parameter.StringParameter;
import org.apache.airavata.services.gfac.axis2.utils.GFacServiceOperations;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.MessageContextBuilder;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jcr.*;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;

public class GFacMessageReciever implements MessageReceiver {

    private static final Log log = LogFactory.getLog(GFacMessageReciever.class);
    public static final String SECURITY_CONTEXT = "security_context";
    GenericService service = null;

    public void receive(org.apache.axis2.context.MessageContext axisRequestMsgCtx)
            throws AxisFault {
        GFacServiceOperations operation = GFacServiceOperations.valueFrom(axisRequestMsgCtx.getOperationContext().getOperationName());
        switch (operation) {
            case INVOKE: {
                processInvokeOperation(axisRequestMsgCtx);
                log.info("Invoke operation invoked !!");
            }
            break;
            case GETWSDL: {
                try {
                    processgetWSDLOperation(axisRequestMsgCtx);
                } catch (Exception e) {
                    throw new AxisFault("Error retrieving the WSDL");
                }
                log.info("getWSDL operation invoked !!");
            }
        }
    }

    public void processInvokeOperation(MessageContext messageContext) {
        MessageContext response = null;
        String serviceName = getOriginalServiceName(messageContext);
        try {        
        ConfigurationContext context = messageContext.getConfigurationContext();
        OMElement input = messageContext.getEnvelope().getBody().getFirstChildWithName(new QName("input"));
        OMElement output = invokeApplication(serviceName, input,context);
        SOAPFactory sf = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope responseEnv = sf.createSOAPEnvelope();
        sf.createSOAPBody(responseEnv);
        responseEnv.getBody().addChild(output);
            response = MessageContextBuilder.createOutMessageContext(messageContext);
            response.setEnvelope(responseEnv);
            response.getOperationContext().addMessageContext(response);
            AxisEngine.send(response);
        } catch (AxisFault fault) {
            log.error("Error creating response");
        }
    }

    private OMElement invokeApplication(String serviceName, OMElement input,ConfigurationContext context) {
        OMElement output = null;
        try {
            InvocationContext ct = new InvocationContext();
            Repository repository = (Repository)context.getProperty("repository");
            Credentials credentials = (Credentials)context.getProperty("credentials");
            ct.setExecutionContext(new ExecutionContextImpl());
            ct.setServiceName(serviceName);
            ct.getExecutionContext().setRegistryService(new JCRRegistry(repository,credentials));
            ParameterContextImpl x = new ParameterContextImpl();

            // TODO define real parameter passing in SOAP body
            //handle parameter
            for (Iterator iterator = input.getChildren(); iterator.hasNext(); ) {
                OMElement element = (OMElement) iterator.next();
                String name = element.getQName().getLocalPart();
                StringParameter value = new StringParameter();
                value.parseStringVal(element.getText());
                x.addParameter(name, value);
            }
            ct.addMessageContext("input", x);
            if (service == null) {
                service = new PropertyServiceFactory().createService();
            }

            //invoke service
            service.execute(ct);


            //TODO also define how output too
            /*
             * Process Output
             */
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://ws.apache.org/axis2/xsd", "ns1");
            output = fac.createOMElement("output", omNs);

            ParameterContextImpl paramContext = (ParameterContextImpl) ct.getMessageContext("output");
            for (Iterator<String> iterator = paramContext.getParameterNames(); iterator.hasNext(); ) {
                String name = iterator.next();
                OMElement ele = fac.createOMElement(name, omNs);
                ele.addAttribute("type", paramContext.getParameterValue(name).getType().toString(), omNs);
                ele.setText(paramContext.getParameterValue(name).toString());
                output.addChild(ele);
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return output;
    }

    public void processgetWSDLOperation(MessageContext messageContext)throws Exception {
        MessageContext response = null;
        String serviceName = getOriginalServiceName(messageContext);
        ConfigurationContext context = messageContext.getConfigurationContext();
        //todo this logic has to change based on the logic we are storing data into repository
        try {
            Credentials credentials = (Credentials) context.getProperty("credentials");
            Session session = ((Repository)context.getProperty("repository")).login(credentials);
            Node node = session.getRootNode().getNode("wsdls").getNode(serviceName);
            Property propertyContent = node.getProperty("content");
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader
                    (new StringReader(propertyContent.getString()));
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement wsdlElement = builder.getDocumentElement();
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

    private String getOriginalServiceName(MessageContext messageContext) {
        String toAddress = messageContext.getTo().getAddress();
        String[] values = Utils.parseRequestURLForServiceAndOperation(toAddress,
                messageContext
                        .getConfigurationContext().getServiceContextPath());
        return values[0];
    }

}
