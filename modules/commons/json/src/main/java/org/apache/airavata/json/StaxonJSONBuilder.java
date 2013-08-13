package org.apache.airavata.json;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;


public class StaxonJSONBuilder implements Builder {
    @Override
    public OMElement processDocument(InputStream inputStream, String s, MessageContext messageContext) throws AxisFault {
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope envelope = factory.createSOAPEnvelope();
        // configure JSON to XML conversion property
        JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).prettyPrint(false).build();
        //
        try {
            String charSetEncoding = (String) messageContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            XMLStreamReader reader = new JsonXMLInputFactory(config).createXMLStreamReader(inputStream, charSetEncoding);
            StAXOMBuilder stAXOMBuilder = new StAXOMBuilder(reader);
            OMElement message = stAXOMBuilder.getDocumentElement();
            envelope.getBody().addChild(message);
        } catch (XMLStreamException e) {
            throw new AxisFault("Error while creating XMLStreamReader with JsonXMLInputFactory");
        }
        return envelope;
    }
}
