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

package org.apache.airavata.registry.api.util;

import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.commons.gfac.wsdl.WSDLConstants;
import org.apache.airavata.commons.gfac.wsdl.WSDLGenerator;
import org.apache.airavata.schemas.gfac.*;

import java.util.Hashtable;

public class WebServiceUtil {



    public static String generateWSDL(ServiceDescription service) {
        StringBuilder builder = new StringBuilder();
        builder.append("<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:ns1=\"http://org.apache.axis2/xsd\" xmlns:ns=\"http://www.wso2.org/types\" xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\" targetNamespace=\"http://www.wso2.org/types\">");
        builder.append("<wsdl:documentation>");
        builder.append(service.getType().getName());
        builder.append("</wsdl:documentation>");
        builder.append("<wsdl:types>");
        builder.append("<xs:schema attributeFormDefault=\"qualified\" elementFormDefault=\"unqualified\" targetNamespace=\"http://www.wso2.org/types\">");

        boolean isInputParametersPresent = service.getType().getInputParametersArray() != null
                && service.getType().getInputParametersArray().length > 0;
        if (isInputParametersPresent) {
            builder.append("<xs:element name=\"invoke\">");
            builder.append("<xs:complexType>");
            builder.append("<xs:sequence>");

            ServiceDescriptionType p = service.getType();

            for (int i = 0; i < p.getInputParametersArray().length; i++) {
                generateElementFromInputType(p.getInputParametersArray(i), builder);
            }

            builder.append("</xs:sequence>");
            builder.append("</xs:complexType>");
            builder.append("</xs:element>");
        }

        boolean isOutputParametersPresent = service.getType().getOutputParametersArray() != null
                && service.getType().getOutputParametersArray().length > 0;
        if (isOutputParametersPresent) {
            builder.append("<xs:element name=\"invokeResponse\">");
            builder.append("<xs:complexType>");
            builder.append("<xs:sequence>");

            ServiceDescriptionType p = service.getType();

            for (int i = 0; i < p.getOutputParametersArray().length; i++) {
                generateElementFromOutputType(p.getOutputParametersArray(i), builder);
            }

            builder.append("</xs:sequence>");
            builder.append("</xs:complexType>");
            builder.append("</xs:element>");
        }

        builder.append("</xs:schema>");
        builder.append("</wsdl:types>");

        builder.append("<wsdl:message name=\"invokeRequest\">");
        if (isInputParametersPresent) {
            builder.append("<wsdl:part name=\"parameters\" element=\"ns:invoke\"/>");
        }
        builder.append("</wsdl:message>");
        if (isOutputParametersPresent) {
            builder.append("<wsdl:message name=\"invokeResponse\">");
            builder.append("<wsdl:part name=\"parameters\" element=\"ns:invokeResponse\"/>");
            builder.append("</wsdl:message>");
        }

        builder.append("<wsdl:portType name=\"");
        builder.append(service.getType().getName());
        builder.append("\">");
        builder.append("<wsdl:operation name=\"invoke\">");
        builder.append("<wsdl:input message=\"ns:invokeRequest\" wsaw:Action=\"urn:invoke\"/>");
        if (isOutputParametersPresent) {
            builder.append("<wsdl:output message=\"ns:invokeResponse\" wsaw:Action=\"urn:invokeResponse\"/>");
        }
        builder.append("</wsdl:operation>");
        builder.append("</wsdl:portType>");

        builder.append("</wsdl:definitions>");

        return builder.toString();
    }

    private static void generateElementFromInputType(InputParameterType parameter, StringBuilder builder) {

        String type = parameter.getParameterType().getName();
        if (type.equals("String")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" nillable=\"true\" type=\"xs:string\"/>");
        } else if (type.equals("Integer")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" type=\"xs:int\"/>");
        } else if (type.equals("Boolean")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" type=\"xs:boolean\"/>");
        } else if (type.equals("Double")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" type=\"xs:double\"/>");
        } else if (type.equals("Float")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" type=\"xs:float\"/>");
        } else if (type.equals("File")) {
            // TODO adding this means adding a new complex type for File type
            // builder.append("<xs:element minOccurs=\"0\" name=\"");
            // builder.append(parameter.getName());
            // builder.append("\"  nillable=\"true\" type=\"ax22:File\"/>");
        }

    }

        private static void generateElementFromOutputType(OutputParameterType parameter, StringBuilder builder) {

        String type = parameter.getParameterType().getName();
        if (type.equals("String")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" nillable=\"true\" type=\"xs:string\"/>");
        } else if (type.equals("Integer")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" type=\"xs:int\"/>");
        } else if (type.equals("Boolean")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" type=\"xs:boolean\"/>");
        } else if (type.equals("Double")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" type=\"xs:double\"/>");
        } else if (type.equals("Float")) {
            builder.append("<xs:element minOccurs=\"0\" name=\"");
            builder.append(parameter.getParameterName());
            builder.append("\" type=\"xs:float\"/>");
        } else if (type.equals("File")) {
            // TODO adding this means adding a new complex type for File type
            // builder.append("<xs:element minOccurs=\"0\" name=\"");
            // builder.append(parameter.getName());
            // builder.append("\"  nillable=\"true\" type=\"ax22:File\"/>");
        }

    }

    public static String getWSDL(ServiceDescription service) throws Exception{
        try {

            ServiceType type = service.getType().addNewService();
            ServiceType.ServiceName name = type.addNewServiceName();
            name.setStringValue(service.getType().getName());
            name.setTargetNamespace("http://airavata.apache.org/schemas/gfac/2012/12");
            if(service.getType().getPortType() == null){
                PortTypeType portType = service.getType().addNewPortType();
                MethodType methodType = portType.addNewMethod();
                methodType.setMethodName("invoke");
            }else{
                MethodType method = service.getType().getPortType().getMethod();
                if (method == null) {
                    MethodType methodType = service.getType().getPortType().addNewMethod();
                    methodType.setMethodName("invoke");
                } else {
                    service.getType().getPortType().getMethod().setMethodName("invoke");
                }
            }
            WSDLGenerator generator = new WSDLGenerator();
            Hashtable table = generator.generateWSDL(null, null, null, service.getType(), true);
            return (String) table.get(WSDLConstants.AWSDL);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
