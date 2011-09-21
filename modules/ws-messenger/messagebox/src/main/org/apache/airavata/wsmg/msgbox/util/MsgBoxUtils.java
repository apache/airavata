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

package org.apache.airavata.wsmg.msgbox.util;

import java.io.Reader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

public class MsgBoxUtils {

    public static SOAPEnvelope reader2SOAPEnvilope(Reader reader) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(reader);

        StAXSOAPModelBuilder builder = new StAXSOAPModelBuilder(inflow);
        SOAPEnvelope omEnvelope = builder.getSOAPEnvelope();
        return omEnvelope;
    }

    public static OMElement reader2OMElement(Reader reader) throws XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        XMLStreamReader inflow = inputFactory.createXMLStreamReader(reader);

        StAXOMBuilder builder = new StAXOMBuilder(inflow);
        OMElement omElement = builder.getDocumentElement();
        inflow.close();
        return omElement;
    }

    public static String formatMessageBoxUrl(String msgBoxServiceUrl, String msgboxId) {
        return msgBoxServiceUrl.endsWith("/") ? msgBoxServiceUrl + "clientid/" + msgboxId : msgBoxServiceUrl
                + "/clientid/" + msgboxId;
    }

    public static String formatURLString(String url) {

        if (url == null) {
            throw new IllegalArgumentException("url can't be null");
        }

        if (url.indexOf("//") < 0) {
            url = "http://" + url; // use default http
        }
        return url;
    }
}
