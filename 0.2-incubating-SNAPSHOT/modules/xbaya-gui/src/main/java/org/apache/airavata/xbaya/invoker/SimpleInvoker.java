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

import java.util.Iterator;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.jackrabbit.core.cache.ConcurrentCache;
import org.xmlpull.v1.builder.XmlElement;

import xsul.wsdl.WsdlDefinitions;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.wsif.WSIFPort;
import xsul.wsif.WSIFService;
import xsul.wsif.WSIFServiceFactory;
import xsul.wsif.spi.WSIFProviderManager;
import xsul.xwsif_runtime.WSIFClient;
import xsul.xwsif_runtime.WSIFRuntime;

public class SimpleInvoker implements Invoker {

    protected WSIFClient client;

    private WsdlDefinitions definitions;

    private WSIFOperation operation;

    private WSIFMessage inputMessage;

    private volatile WSIFMessage outputMessage;

    private WSIFMessage faultMessage;

    private boolean lock = false;

    static {
        WSIFProviderManager.getInstance().addProvider(new xsul.wsif_xsul_soap_http.Provider());
    }

    /**
     * Constructs a SimpleInvoker.
     * 
     * @param definitions
     */
    public SimpleInvoker(WsdlDefinitions definitions) {
        this.definitions = definitions;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#setup()
     */
    public void setup() throws XBayaException {
        try {
            WSIFService service = WSIFServiceFactory.newInstance().getService(this.definitions);
            WSIFPort port = service.getPort();
            this.client = WSIFRuntime.getDefault().newClientFor(port);
            this.client.setAsyncResponseTimeoutInMs(999999999);
        } catch (RuntimeException e) {
            String message = "The WSDL is in the wrong format";
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getClient()
     */
    public WSIFClient getClient() {
        return this.client;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#setOperation(java.lang.String)
     */
    public void setOperation(String operationName) throws XBayaException {
        try {
            WSIFPort port = this.client.getPort();
            this.operation = port.createOperation(operationName);
            this.inputMessage = this.operation.createInputMessage();
            this.outputMessage = this.operation.createOutputMessage();
            this.faultMessage = this.operation.createFaultMessage();
        } catch (RuntimeException e) {
            String message = "The WSDL does not conform to the invoking service.";
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#setInput(java.lang.String, java.lang.Object)
     */
    public void setInput(String name, Object value) throws XBayaException {
        try {
            if (value instanceof XmlElement) {
                // If the value is a complex type, change the name of the
                // element to the correct one.
                XmlElement valueElement = (XmlElement) value;
                valueElement.setName(name);
            } else if (value instanceof String) {
                    if(XMLUtil.isXML((String)value)){
                     XmlElement valueElement = XMLUtil.stringToXmlElement3((String) value);
                     valueElement.setName(name);
                        value = valueElement;
                }
                // Simple case.
            } else {
                // convert int, doule to string.
                value = "" + value;
            }
            this.inputMessage.setObjectPart(name, value);
        } catch (RuntimeException e) {
            String message = "Error in setting an input. name: " + name + " value: " + value;
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getInputs()
     */
    public WSIFMessage getInputs() {
        return this.inputMessage;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#invoke()
     */
    public boolean invoke() throws XBayaException {
        try {
            boolean success = this.operation.executeRequestResponseOperation(this.inputMessage, this.outputMessage,
                    this.faultMessage);
            while(this.outputMessage == null){

            }
            return success;
        } catch (RuntimeException e) {
            String message = "Error in invoking a service.";
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getOutputs()
     */
    public WSIFMessage getOutputs() {
         if (lock) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return this.outputMessage;
    }


    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getOutput(java.lang.String)
     */
    public Object getOutput(String name) throws XBayaException {
        try {
            // This code doesn't work when the output is a complex type.
            // Object output = this.outputMessage.getObjectPart(name);
            // return output;

            XmlElement outputElement = (XmlElement) this.outputMessage;
            XmlElement valueElement = outputElement.element(null, name);
            Iterator childIt = valueElement.children();
            int numberOfChildren = 0;
            while (childIt.hasNext()) {
                childIt.next();
                numberOfChildren++;
            }
            if (numberOfChildren == 1) {
                Object child = valueElement.children().next();
                if (child instanceof String) {
                    // Value is a simple type. Return the string.
                    String value = (String) child;
                    return value;
                }
            }
            // Value is a complex type. Return the whole XmlElement so that we
            // can set it to the next service as it is.
            return valueElement;
        } catch (RuntimeException e) {
            String message = "Error in getting output. name: " + name;
            throw new XBayaException(message, e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.Invoker#getFault()
     */
    public WSIFMessage getFault() {
        return this.faultMessage;
    }

    public WsdlDefinitions getDefinitions() {
        return definitions;
    }

    public WSIFOperation getOperation() {
        return operation;
    }

    public WSIFMessage getInputMessage() {
        return inputMessage;
    }

    public synchronized WSIFMessage getOutputMessage() {
        return outputMessage;
    }

    public WSIFMessage getFaultMessage() {
        return faultMessage;
    }

    public synchronized void setOutputMessage(WSIFMessage outputMessage) {
        System.out.println("Setting output message");
        this.outputMessage = outputMessage;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isLock() {
        return lock;
    }
}