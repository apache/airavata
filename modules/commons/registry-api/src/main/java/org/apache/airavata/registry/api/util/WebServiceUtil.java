package org.apache.airavata.registry.api.util;

import org.apache.airavata.commons.gfac.type.Parameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;

public class WebServiceUtil {

    public static String generateWSDL(ServiceDescription service) {
        StringBuilder builder = new StringBuilder();
        builder.append("<wsdl:definitions xmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\" xmlns:ns1=\"http://org.apache.axis2/xsd\" xmlns:ns=\"http://www.wso2.org/types\" xmlns:wsaw=\"http://www.w3.org/2006/05/addressing/wsdl\" xmlns:http=\"http://schemas.xmlsoap.org/wsdl/http/\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:soap=\"http://schemas.xmlsoap.org/wsdl/soap/\" xmlns:mime=\"http://schemas.xmlsoap.org/wsdl/mime/\" xmlns:soap12=\"http://schemas.xmlsoap.org/wsdl/soap12/\" targetNamespace=\"http://www.wso2.org/types\">");
        builder.append("<wsdl:documentation>");
        builder.append(service.getId());
        builder.append("</wsdl:documentation>");
        builder.append("<wsdl:types>");
        builder.append("<xs:schema attributeFormDefault=\"qualified\" elementFormDefault=\"unqualified\" targetNamespace=\"http://www.wso2.org/types\">");

        boolean isInputParametersPresent = service.getInputParameters() != null && service.getInputParameters().size() > 0;
		if (isInputParametersPresent) {
            builder.append("<xs:element name=\"invoke\">");
            builder.append("<xs:complexType>");
            builder.append("<xs:sequence>");

            for (Parameter parameter : service.getInputParameters()) {
                generateElementFromType(parameter, builder);
            }

            builder.append("</xs:sequence>");
            builder.append("</xs:complexType>");
            builder.append("</xs:element>");
        }

		boolean isOutputParametersPresent = service.getOutputParameters() != null && service.getOutputParameters().size() > 0;
		if (isOutputParametersPresent) {
            builder.append("<xs:element name=\"invokeResponse\">");
            builder.append("<xs:complexType>");
            builder.append("<xs:sequence>");

            for (Parameter parameter : service.getOutputParameters()) {
                generateElementFromType(parameter, builder);
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
        builder.append(service.getId());
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

    private static void generateElementFromType(Parameter parameter, StringBuilder builder) {
        builder.append("<xs:element minOccurs=\"0\" maxOccurs=\"1\" name=\"");
        builder.append(parameter.getName());
        builder.append("\" nillable=\"true\" type=\"xs:string\"/>");        
    }
}
