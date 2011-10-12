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

package org.apache.airavata.services.gfac.axis2.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;
import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;

public class WSDLUtil {

    private static final Logger logger = LoggerFactory.getLogger(WSDLUtil.class);
    
    private WSDLUtil(){        
    }
    
    /**
     * Generate Concrete WSDL (including <Binding> and <Service>) from an Abstract WSDL using WSDL4J.
     * 
     * @param Abstract WSDL
     * @param Service end point
     * @return Concrete WSDL
     * @throws WSDLException
     */
    public static String createCWSDL(String abstractWSDL, String epr) throws WSDLException{
        
        /*
         * Read to Definition
         */
        WSDLFactory factory = WSDLFactory.newInstance();
        WSDLReader wsdlreader = factory.newWSDLReader();
        InputSource source = new InputSource(new StringReader(abstractWSDL));
        Definition wsdlDefinition = wsdlreader.readWSDL(null, source);                        

        Map portTypes = wsdlDefinition.getPortTypes();
        Iterator portIt = portTypes.keySet().iterator();
        while (portIt.hasNext()) {
            QName key = (QName)portIt.next();
            PortType portType = (PortType) portTypes.get(key);
            String portTypeName = key.getLocalPart();
            List operations = portType.getOperations();
            Iterator opIt = operations.iterator();
            String namespace = portType.getQName().getNamespaceURI();
            

            /*
             * Create WSDL Binding
             */
            Binding binding = wsdlDefinition.createBinding();
            binding.setQName(new QName(namespace, portTypeName + "SoapBinding"));
            binding.setPortType(portType);
            binding.setUndefined(false);
            
            /*
             * Create SOAPBinding for WSDL Binding
             */
            SOAPBinding soapbinding = new SOAPBindingImpl();
            soapbinding.setTransportURI("http://schemas.xmlsoap.org/soap/http");
            soapbinding.setStyle("document");
            binding.addExtensibilityElement(soapbinding);

            while (opIt.hasNext()) {
                Operation operation = (Operation) opIt.next();
                
                /*
                 * For each operation in portType, Create BindingOperation
                 */
                BindingOperation boperation = wsdlDefinition.createBindingOperation();
                boperation.setName(operation.getName());
                boperation.setOperation(operation);

                BindingInput input = wsdlDefinition.createBindingInput();
                BindingOutput outpout = wsdlDefinition.createBindingOutput();

                SOAPBodyImpl soapBodyIn = new SOAPBodyImpl();
                soapBodyIn.setUse("literal");
                
                SOAPBodyImpl soapBodyOut = new SOAPBodyImpl();
                soapBodyOut.setUse("literal");

                input.addExtensibilityElement(soapBodyIn);
                outpout.addExtensibilityElement(soapBodyOut);

                /*
                 * Create SOAP Operation for BindingOperation
                 */
                SOAPOperation soapoperation = new SOAPOperationImpl();
                soapoperation.setSoapActionURI(namespace + "invoke");
                soapoperation.setStyle("document");
                
                boperation.setBindingInput(input);
                boperation.setBindingOutput(outpout);
                boperation.addExtensibilityElement(soapoperation);
                
                /*
                 * Add BindingOperation to Binding
                 */
                binding.addBindingOperation(boperation);
            }
            
            /*
             * Add Binding to WSDL
             */
            wsdlDefinition.addBinding(binding);

            /*
             * Create Service for each Binding
             */
            Service service = wsdlDefinition.createService();
            service.setQName(new QName(namespace, portTypeName + "Service"));

            /*
             * Create Port for service
             */
            Port soapport = wsdlDefinition.createPort();
            soapport.setName(portTypeName + "SoapPort");
            soapport.setBinding(binding);
            /*
             * Set Address for SOAP Port
             */
            SOAPAddress address = new SOAPAddressImpl();
            address.setLocationURI(epr);
            
            soapport.addExtensibilityElement(address);

            //add Port
            service.addPort(soapport);

            /*
             * Add Service to WSDL
             */
            wsdlDefinition.addService(service);
        }            

        /*
         * Write to String
         */
        StringWriter out = new StringWriter();
        WSDLWriter writer = WSDLFactory.newInstance().newWSDLWriter();
        writer.writeWSDL(wsdlDefinition, out);
        
        return out.toString();
    }

}