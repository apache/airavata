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

package org.apache.airavata.xbaya.jython.lib.invoker;

import java.net.URISyntaxException;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.jython.lib.GFacServiceCreator;
import org.apache.airavata.xbaya.lead.NotificationHandler;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.xmlpull.v1.builder.Iterable;
import org.xmlpull.v1.builder.XmlElement;

import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlPortType;
import xsul.wsif.WSIFMessage;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;
import xsul5.MLogger;

public class GFacInvoker implements Invoker {

    private final static MLogger logger = MLogger.getLogger();

    private String gfacURL;

    private String messageBoxURL;

    private WsdlDefinitions abstractDefinitions;

    private QName portTypeQName;

    private GFacServiceCreator creator;

    private Invoker invoker;

    private LeadContextHeader leadContext;

    private boolean noShutDown = false;

    // FIXME: Should not invoke gfac without a context header, deprecating this method
    // /**
    // * Constructs a GFacInvoker.
    // *
    // * @param definitions
    // * @param messageBoxURL
    // */
    // public GFacInvoker(WsdlDefinitions definitions, String messageBoxURL) {
    // this(definitions, null, null, messageBoxURL);
    // }

    /**
     * Constructs a GFacInvoker.
     * 
     * @param portTypeQName
     * @param gfacURL
     * @param messageBoxURL
     * @param context
     */
    public GFacInvoker(QName portTypeQName, String gfacURL, String messageBoxURL, LeadContextHeader context) {
        this(portTypeQName, gfacURL, messageBoxURL, context, false);
    }

    /**
     * Constructs a GFacInvoker.
     * 
     * @param portTypeQName
     * @param gfacURL
     * @param messageBoxURL
     * @param context
     * @param noShutDown
     */
    public GFacInvoker(QName portTypeQName, String gfacURL, String messageBoxURL, LeadContextHeader context,
            boolean noShutDown) {
        this(null, portTypeQName, gfacURL, messageBoxURL);
        this.leadContext = context;
        this.noShutDown = noShutDown;
    }

    /**
     * Constructs a GFacInvoker.
     * 
     * @param definitions
     * @param portTypeQName
     * @param gfacURL
     * @param messageBoxURL
     */
    public GFacInvoker(WsdlDefinitions definitions, QName portTypeQName, String gfacURL, String messageBoxURL) {
        this.abstractDefinitions = definitions;
        this.portTypeQName = portTypeQName;
        this.gfacURL = gfacURL;
        this.messageBoxURL = messageBoxURL;
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#setup()
     */
    public WsdlDefinitions setup() throws XBayaException {

        String wsdlLoc = null;
        if (this.gfacURL == null) {
            XmlElement factoryServicesElement = this.abstractDefinitions.element(null, "factoryServices");
            if (factoryServicesElement != null) {
                XmlElement factoryService = factoryServicesElement.element(null, "factoryService");
                if (factoryService != null) {
                    String location = factoryService.getAttributeValue(null, "location");
                    String portType = factoryService.getAttributeValue(null, "portType");
                    logger.finest("location: " + location);
                    logger.finest("portType: " + portType);
                    wsdlLoc = WSDLUtil.appendWSDLQuary(location);
                }
            }
        } else {
            wsdlLoc = WSDLUtil.appendWSDLQuary(this.gfacURL);
        }

        if (wsdlLoc == null) {
            String message = "The location of the Generic Factory is not specified.";
            throw new XBayaException(message);
        }

        try {
            this.creator = new GFacServiceCreator(wsdlLoc);
        } catch (URISyntaxException e) {
            String message = "The location of the Generic Factory is in a wrong format";
            throw new XBayaException(message, e);
        }
        if (this.creator == null) {
            String message = "Cannot find the location of the Generic Factory in the WSDL";
            throw new XBayaException(message);
        }

        if (this.portTypeQName == null) {
            try {
                Iterable portTypes = this.abstractDefinitions.getPortTypes();
                WsdlPortType portType = (WsdlPortType) portTypes.iterator().next();
                String portTypeName = portType.getPortTypeName();
                String targetNamespace = this.abstractDefinitions.getTargetNamespace();
                this.portTypeQName = new QName(targetNamespace, portTypeName);
            } catch (RuntimeException e) {
                String message = "Error in finding QName of the service in the WSDL";
                throw new XBayaException(message, e);
            }
        }

        WsdlDefinitions definitions = this.creator.createService(this.portTypeQName);

        // FIXME: Should pass the last argument of leadcontextheader
        this.invoker = InvokerFactory.createInvoker(this.portTypeQName, definitions, null, this.messageBoxURL, null);

        return this.invoker.setup();
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getClient()
     */
    public WSIFClient getClient() {
        return this.invoker.getClient();
    }

    /**
     * @throws XBayaException
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#setOperation(java.lang.String)
     */
    public void setOperation(String operationName) throws XBayaException {
        this.invoker.setOperation(operationName);
    }

    /**
     * @throws XBayaException
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#setInput(java.lang.String, java.lang.Object)
     */
    public void setInput(String name, Object value) throws XBayaException {
        this.invoker.setInput(name, value);
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getInputs()
     */
    public WSIFMessage getInputs() throws XBayaException {
        return this.invoker.getInputs();
    }

    /**
     * @throws XBayaException
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#invoke()
     */
    public boolean invoke() throws XBayaException {

        WSIFClient client = invoker.getClient();
        // FIXME: Temporary fix
        if (this.leadContext == null) {
            LeadContextHeader lh = new LeadContextHeader(UUID.randomUUID().toString(), "XBaya-User");
            this.leadContext = lh;
        }
        StickySoapHeaderHandler handler = new StickySoapHeaderHandler("use-lead-header", this.leadContext);
        client.addHandler(handler);

        // This handler has to be end to get the entire soap message.
        NotificationHandler notificationHandler = new NotificationHandler(this.leadContext);
        client.addHandler(notificationHandler);
        boolean success = this.invoker.invoke();

        // Try to shutdown the service

        if (!noShutDown) {
            new Thread() {
                @Override
                public void run() {
                    try {
                        GFacInvoker.this.creator.shutdownService();
                    } catch (XBayaException e) {
                        // Ignore the error.
                        logger.caught(e);
                    }
                }
            }.start();
        }

        return success;
    }

    /**
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getOutputs()
     */
    public WSIFMessage getOutputs() throws XBayaException {
        return this.invoker.getOutputs();
    }

    /**
     * @throws XBayaException
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getOutput(java.lang.String)
     */
    public Object getOutput(String name) throws XBayaException {
        return this.invoker.getOutput(name);
    }

    /**
     * @throws XBayaException
     * @see org.apache.airavata.xbaya.jython.lib.invoker.Invoker#getFault()
     */
    public WSIFMessage getFault() throws XBayaException {
        return this.invoker.getFault();
    }

}