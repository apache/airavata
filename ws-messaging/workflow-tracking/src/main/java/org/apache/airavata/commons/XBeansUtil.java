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

package org.apache.airavata.commons;

import java.io.StringReader;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

public class XBeansUtil {

    org.apache.log4j.Logger logger = Logger.getLogger(XBeansUtil.class);

    public XBeansUtil() {
    }

    public static OMElement xmlObjectToOMElement(XmlObject responseXmlObj) throws XMLStreamException {
        String responseXml;
        XmlOptions opts = new XmlOptions();
        opts.setSaveOuter();
        responseXml = responseXmlObj.xmlText(opts);
        OMElement outgoingMsg = org.apache.airavata.commons.WorkFlowUtils
                .reader2OMElement(new StringReader(responseXml));
        return outgoingMsg;
    }
}
