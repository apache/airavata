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
package org.apache.airavata.xbaya.jython.lib;

import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmlpull.v1.builder.XmlElement;

import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlResolver;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif.spi.WSIFProviderManager;

public class GFacServiceCreator {

    private static final String CREATE_SERVICE_OPERATION = "CreateService";

    private static final String SHUTDOWN_OPERATION = "Shutdown";

    private static final String SERVICE_QNAME_PART = "serviceQName";

    private static final String SECURITY_PART = "security";

    private static final String WSDL_PART = "WSDL";

    private static final String SECURITY_NONE = "None";

    private static final Logger logger = LoggerFactory.getLogger(GFacServiceCreator.class);

    private WSIFOperation gFacOperation;

    private WsdlDefinitions serviceDefinitions;

    static {
        WSIFProviderManager.getInstance().addProvider(new xsul.wsif_xsul_soap_http.Provider());
    }

    /**
     * Constructs a GFacServiceCreater.
     * 
     * @param wsdlURL
     *            The URL of the GFac service
     * @throws URISyntaxException
     * @throws WorkflowException
     */
    public GFacServiceCreator(String wsdlURL) throws URISyntaxException, WorkflowException {
        this(new URI(wsdlURL));
    }

    /**
     * Constructs a GFacServiceCreater.
     * 
     * @param wsdlURI
     *            The URI of the GFac service
     * @throws WorkflowException
     */
    public GFacServiceCreator(URI wsdlURI) throws WorkflowException {
        try {
            WsdlDefinitions definitions = WsdlResolver.getInstance().loadWsdl(wsdlURI);
            WSIFService service = WSIFServiceFactory.newInstance().getService(definitions);
            WSIFPort port = service.getPort();
            this.gFacOperation = port.createOperation(CREATE_SERVICE_OPERATION);
        } catch (RuntimeException e) {
            String message = "Failed to connect to the Generic Factory: " + wsdlURI;
            throw new WorkflowException(message, e);
        }
    }

    /**
     * @param serviceQName
     * @return The WSDL definitions of the service created.
     * @throws WorkflowException
     */
    public WsdlDefinitions createService(QName serviceQName) throws WorkflowException {
        return createService(serviceQName.toString());
    }

    /**
     * @param serviceQName
     * @return The WSDL definitions of the service created.
     * @throws WorkflowException
     */
    public WsdlDefinitions createService(String serviceQName) throws WorkflowException {
        logger.debug(serviceQName);
        try {
            WSIFMessage inputMessage = this.gFacOperation.createInputMessage();
            WSIFMessage outputMessage = this.gFacOperation.createOutputMessage();
            WSIFMessage faultMessage = this.gFacOperation.createFaultMessage();

            inputMessage.setObjectPart(SERVICE_QNAME_PART, serviceQName);
            inputMessage.setObjectPart(SECURITY_PART, SECURITY_NONE);

            logger.debug("inputMessage: " + inputMessage);
            boolean success = this.gFacOperation.executeRequestResponseOperation(inputMessage, outputMessage,
                    faultMessage);
            if (!success) {
                // An implementation of WSIFMessage, WSIFMessageElement,
                // implements toString(), which serialize the message XML.
                String message = "Failed to create a service: " + faultMessage.toString();
                throw new WorkflowException(message);
            }

            String wsdl = (String) outputMessage.getObjectPart(WSDL_PART);
            logger.debug("WSDL: " + wsdl);

            XmlElement definitionsElement = XMLUtil.stringToXmlElement3(wsdl);

            this.serviceDefinitions = new WsdlDefinitions(definitionsElement);
            return this.serviceDefinitions;
        } catch (RuntimeException e) {
            String message = "Failed to create a service";
            throw new WorkflowException(message, e);
        }
    }

    /**
     * Shutdowns the service created.
     * 
     * @throws WorkflowException
     */
    public void shutdownService() throws WorkflowException {
        WSIFService service = WSIFServiceFactory.newInstance().getService(this.serviceDefinitions);
        WSIFPort port = service.getPort();
        WSIFOperation operation = port.createOperation(SHUTDOWN_OPERATION);

        WSIFMessage inputMessage = operation.createInputMessage();
        WSIFMessage outputMessage = operation.createOutputMessage();
        WSIFMessage faultMessage = operation.createFaultMessage();

        boolean success = operation.executeRequestResponseOperation(inputMessage, outputMessage, faultMessage);
        if (!success) {
            // An implementation of WSIFMessage, WSIFMessageElement,
            // implements toString(), which serialize the message XML.
            String message = "Failed to shutdown the service: " + faultMessage.toString();
            throw new WorkflowException(message);
        }
    }
}