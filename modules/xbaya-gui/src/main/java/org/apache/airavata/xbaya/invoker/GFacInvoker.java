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

package org.apache.airavata.xbaya.invoker;

import java.net.URI;
import java.util.UUID;

import javax.xml.namespace.QName;

import org.apache.airavata.common.workflow.execution.context.WorkflowContextHeaderBuilder;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.xbaya.invoker.factory.InvokerFactory;
import org.apache.airavata.xbaya.lead.NotificationHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import xsul.lead.LeadContextHeader;
import xsul.wsdl.WsdlDefinitions;
import xsul.wsdl.WsdlResolver;
import xsul.wsif.WSIFMessage;
import xsul.xhandler_soap_sticky_header.StickySoapHeaderHandler;
import xsul.xwsif_runtime.WSIFClient;

public class GFacInvoker implements Invoker {

    private static final Log logger = LogFactory.getLog(GFacInvoker.class);

    private String gfacURL;

    private String messageBoxURL;

    private QName portTypeQName;

    private Invoker invoker;

    private LeadContextHeader leadContext;

    private WorkflowContextHeaderBuilder builder;

    /**
     * Constructs a GFacInvoker.
     * 
     * @param portTypeQName
     * @param gfacURL
     * @param messageBoxURL
     * @param context
     */
    public GFacInvoker(QName portTypeQName, String gfacURL, String messageBoxURL, LeadContextHeader context) {
        this.portTypeQName = portTypeQName;
        this.gfacURL = gfacURL;
        this.messageBoxURL = messageBoxURL;
        this.leadContext = context;
    }

    public GFacInvoker(QName portTypeQName, String gfacURL, String messageBoxURL, WorkflowContextHeaderBuilder context) {
        this.portTypeQName = portTypeQName;
        this.gfacURL = gfacURL;
        this.messageBoxURL = messageBoxURL;
        this.builder = context;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#setup()
     */
    public void setup() throws WorkflowException {

        if (this.gfacURL == null) {
            String message = "The location of the Generic Factory is not specified.";
            throw new WorkflowException(message);
        }

        if (this.portTypeQName == null) {
            String message = "Error in finding the service name";
            throw new WorkflowException(message);
        }

        try {

            URI uri = new URI(this.gfacURL);

            /*
             * Substring to remove GfacService
             */
            String gfacPath = uri.getPath();
            if (gfacPath != null && gfacPath.contains("/")) {
                gfacPath = gfacPath.substring(0, gfacPath.lastIndexOf('/') + 1) + portTypeQName.getLocalPart();
            }
            URI getWsdlURI = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), gfacPath
                    + "/getWSDL", uri.getQuery(), uri.getFragment());

            logger.info("getWSDL service:" + getWsdlURI.toString());

            WsdlDefinitions concreteWSDL = WsdlResolver.getInstance().loadWsdl(getWsdlURI);

            this.invoker = InvokerFactory.createInvoker(this.portTypeQName, concreteWSDL, null, this.messageBoxURL,
                    null, true);
            this.invoker.setup();
        } catch (WorkflowException xe) {
            throw xe;
        } catch (Exception e) {
            throw new WorkflowException(e.getMessage(), e);
        }

    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getClient()
     */
    public WSIFClient getClient() {
        return this.invoker.getClient();
    }

    /**
     * @throws WorkflowException
     * @see org.apache.airavata.xbaya.invoker.Invoker#setOperation(java.lang.String)
     */
    public void setOperation(String operationName) throws WorkflowException {
        this.invoker.setOperation(operationName);
    }

    /**
     * @throws WorkflowException
     * @see org.apache.airavata.xbaya.invoker.Invoker#setInput(java.lang.String, java.lang.Object)
     */
    public void setInput(String name, Object value) throws WorkflowException {
        this.invoker.setInput(name, value);
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getInputs()
     */
    public WSIFMessage getInputs() throws WorkflowException {
        return this.invoker.getInputs();
    }

    /**
     * @throws WorkflowException
     * @see org.apache.airavata.xbaya.invoker.Invoker#invoke()
     */
    public boolean invoke() throws WorkflowException {

        WSIFClient client = invoker.getClient();
        // FIXME: Temporary fix
        // if (this.leadContext == null) {
        // LeadContextHeader lh = new LeadContextHeader(UUID.randomUUID().toString(), "XBaya-User");
        // this.leadContext = lh;
        // }
        // StickySoapHeaderHandler handler = new StickySoapHeaderHandler("use-lead-header", this.leadContext);
        // client.addHandler(handler);

        // This handler has to be end to get the entire soap message.
        NotificationHandler notificationHandler = new NotificationHandler(this.builder);
        client.addHandler(notificationHandler);
        return this.invoker.invoke();
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getOutputs()
     */
    public WSIFMessage getOutputs() throws WorkflowException {
        return this.invoker.getOutputs();
    }

    /**
     * @throws WorkflowException
     * @see org.apache.airavata.xbaya.invoker.Invoker#getOutput(java.lang.String)
     */
    public Object getOutput(String name) throws WorkflowException {
        return this.invoker.getOutput(name);
    }

    /**
     * @throws WorkflowException
     * @see org.apache.airavata.xbaya.invoker.Invoker#getFault()
     */
    public WSIFMessage getFault() throws WorkflowException {
        return this.invoker.getFault();
    }

}