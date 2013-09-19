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

package org.apache.airavata.json;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLOutputFactory;
import de.odysseus.staxon.json.stream.jackson.JacksonStreamFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMOutputFormat;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.MessageFormatter;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.*;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import java.io.OutputStream;
import java.net.URL;

public class StaxonJSONFormatter implements MessageFormatter {
    @Override
    public byte[] getBytes(MessageContext messageContext, OMOutputFormat omOutputFormat) throws AxisFault {
        return new byte[0];
    }

    @Override
    public void writeTo(MessageContext messageContext, OMOutputFormat omOutputFormat, OutputStream outputStream,
                        boolean preserve) throws AxisFault {
        String charSetEncoding = (String) messageContext.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
        JsonXMLConfig config = new JsonXMLConfigBuilder()
                .autoArray(true)
                .autoPrimitive(true)
                .prettyPrint(true)
                .build();



        try {
            OMElement outMessage = messageContext.getEnvelope().getBody().getFirstElement();
            XMLStreamReader reader = null;
            if (preserve) {
                // do not consume the OM
                reader = outMessage.getXMLStreamReader();
            }else{
                // consume the OM
                reader = outMessage.getXMLStreamReaderWithoutCaching();
            }
            Source source = new StAXSource(reader);

            XMLStreamWriter writer = new JsonXMLOutputFactory(config, new JacksonStreamFactory())
                    .createXMLStreamWriter(outputStream, charSetEncoding);
            Result result = new StAXResult(writer);
            String backupProp = System.getProperty("javax.xml.transform.TransformerFactory");
            System.setProperty("javax.xml.transform.TransformerFactory",
                    "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
            TransformerFactory.newInstance().newTransformer().transform(source, result);
            System.setProperty("javax.xml.transform.TransformerFactory", backupProp);
        } catch (XMLStreamException e) {
            throw new AxisFault("Error while writing OMElement to output writer ", e);
        } catch (TransformerConfigurationException e) {
            throw new AxisFault("StaxonJSONFormatter threw an error while writing to output ",e);
        } catch (TransformerException e) {
            throw new AxisFault("StaxonJSONFormatter threw an error while writing to output",e);
        }

    }

    @Override
    public String getContentType(MessageContext messageContext, OMOutputFormat omOutputFormat, String s) {
        return (String) messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
    }

    @Override
    public URL getTargetAddress(MessageContext messageContext, OMOutputFormat omOutputFormat, URL url) throws AxisFault {
        return null;
    }

    @Override
    public String formatSOAPAction(MessageContext messageContext, OMOutputFormat omOutputFormat, String s) {
        return null;
    }
}
