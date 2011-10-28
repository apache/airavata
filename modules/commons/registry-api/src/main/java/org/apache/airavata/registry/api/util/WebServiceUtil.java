package org.apache.airavata.registry.api.util;

import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.Parameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;

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

}
