package org.apache.airavata.commons.gfac.wsdl;

import java.util.Hashtable;

import org.apache.airavata.schemas.gfac.ServiceDescriptionDocument;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.xmlbeans.XmlException;

public class TestWSDLGeneration {

    public static String createAwsdl4ServiceMap(String serviceDescAsStr) throws GFacWSDLException {
        try {
            ServiceDescriptionType serviceDesc = ServiceDescriptionDocument.Factory.parse(serviceDescAsStr)
                    .getServiceDescription();
            WSDLGenerator wsdlGenerator = new WSDLGenerator();
            Hashtable serviceTable = wsdlGenerator.generateWSDL(null, null, null, serviceDesc, true);
            String wsdl = (String) serviceTable.get(WSDLConstants.AWSDL);
            System.out.println("The generated AWSDL is " + wsdl);
            return wsdl;
        } catch (XmlException e) {
            throw new GFacWSDLException(e);
        }
    }

    public static String createCwsdl4ServiceMap(String serviceDescAsStr) throws GFacWSDLException {
        try {
            ServiceDescriptionType serviceDesc = ServiceDescriptionDocument.Factory.parse(serviceDescAsStr)
                    .getServiceDescription();
            WSDLGenerator wsdlGenerator = new WSDLGenerator();
            String security = WSDLConstants.TRANSPORT_LEVEL;
            String serviceLocation = "http://localhost:8080/axis2/services/test?wsdl";
            
            Hashtable serviceTable = wsdlGenerator.generateWSDL(serviceLocation, null, security,
                    serviceDesc, false);
            
            String wsdl = (String) serviceTable.get(WSDLConstants.WSDL);
            
            System.out.println("The generated CWSDL is " + wsdl);
            return wsdl;

        } catch (XmlException e) {
            throw new GFacWSDLException(e);
        }
    }
    
}
