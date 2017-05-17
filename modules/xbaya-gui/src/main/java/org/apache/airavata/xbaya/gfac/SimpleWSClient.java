/**
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
 */
package org.apache.airavata.xbaya.gfac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.apache.airavata.workflow.model.component.ComponentRegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;
import org.xmlpull.v1.builder.XmlInfosetBuilder;

import xsul.XmlConstants;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.impl.WSIFMessageElement;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;

/**
 * This is a Simple Web Service client for easy SOAP Messages creation
 * 
 */
public class SimpleWSClient {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWSClient.class);

    private static final XmlInfosetBuilder builder = XmlConstants.BUILDER;

    private String requestNS = GFacRegistryClient.GFAC_NAMESPACE;

    /**
     * @param url
     * @param args
     * @param opName
     * @return The output
     * @throws ComponentRegistryException
     */
    public WSIFMessage sendSOAPMessage(String url, Object[][] args, String opName) throws ComponentRegistryException {
        WSIFClient wclient = WSIFRuntime.newClient(url);
        return sendSOAPMessage(wclient, args, opName);
    }

    /**
     * @param wclient
     * @param args
     * @param opName
     * @return The output
     * @throws ComponentRegistryException
     */
    public WSIFMessage sendSOAPMessage(WSIFClient wclient, Object[][] args, String opName)
            throws ComponentRegistryException {

        WSIFPort port = wclient.getPort();

        WSIFOperation operation = port.createOperation(opName);
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();
        String messageName = operation.createInputMessage().getName();
        XmlElement inputMsgElem = builder.newFragment(this.requestNS, messageName);

        for (int i = 0; i < args.length; i++) {
            createMessage((String) args[i][0], args[i][1], inputMsgElem);
        }

        WSIFMessageElement inputMessage = new WSIFMessageElement(inputMsgElem);

        boolean success = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);
        if (success) {
            logger.debug("" + outputMessage);
            return outputMessage;
        } else {
            throw new ComponentRegistryException("Excpetion at server " + faultMessage);
        }
    }

    private void createMessage(String paramName, Object value, XmlElement inputMsgElem)
            throws ComponentRegistryException {
        XmlElement paramsElem = builder.newFragment(this.requestNS, paramName);
        if (value instanceof String) {
            paramsElem.addChild(value);
        } else if (value instanceof Collection) {
            Collection list = (Collection) value;
            Iterator arrayValues = list.iterator();
            while (arrayValues.hasNext()) {
                XmlElement item = builder.newFragment("value");
                item.addChild(arrayValues.next());
                paramsElem.addChild(item);
            }
        } else if (value instanceof ArrayList) {
            Collection list = (Collection) value;
            Iterator arrayValues = list.iterator();
            while (arrayValues.hasNext()) {
                XmlElement item = builder.newFragment("value");
                item.addChild(arrayValues.next());
                paramsElem.addChild(item);
            }
        } else if (value instanceof String[]) {
            String[] list = (String[]) value;
            for (int i = 0; i < list.length; i++) {
                XmlElement item = builder.newFragment("value");
                item.addChild(list[i]);
                paramsElem.addChild(item);
            }
        } else {
            throw new ComponentRegistryException("Simple WS Client can not handle the value of type " + value);
        }

        inputMsgElem.addElement(paramsElem);
    }

}
