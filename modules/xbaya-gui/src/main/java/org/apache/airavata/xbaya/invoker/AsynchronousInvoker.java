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

import org.apache.airavata.xbaya.XBayaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xsul.wsdl.WsdlDefinitions;
import xsul.wsif.WSIFMessage;
import xsul.wsif.WSIFOperation;
import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
import xsul.xwsif_runtime_async_http.XsulSoapHttpWsaResponsesCorrelator;

public class AsynchronousInvoker extends SimpleInvoker {

    private static final Logger logger = LoggerFactory.getLogger(AsynchronousInvoker.class);

    private String messageBoxURL;

    /**
     * Constructs an AsynchronousInvoker.
     * 
     * @param definitions
     */
    public AsynchronousInvoker(WsdlDefinitions definitions) {
        this(definitions, null);
    }

    /**
     * Constructs an AsynchronousInvoker.
     * 
     * @param definitions
     * @param messageBoxURL
     */
    public AsynchronousInvoker(WsdlDefinitions definitions, String messageBoxURL) {
        super(definitions);
        this.messageBoxURL = messageBoxURL;
    }

    /**
     * @see org.apache.airavata.xbaya.invoker.SimpleInvoker#setup()
     */
    @Override
    public void setup() throws XBayaException {
        super.setup();
        /* Set the output message to null to set teh output from async Listener */
        WSIFAsyncResponsesCorrelator correlator;
        if (this.messageBoxURL == null || this.messageBoxURL.length() == 0) {
            correlator = new XsulSoapHttpWsaResponsesCorrelator();
            String serverLoc = ((XsulSoapHttpWsaResponsesCorrelator) correlator).getServerLocation();
            logger.info("using async correlator at " + serverLoc);
        } else {
            correlator = new MsgBoxWsaResponsesCorrelator(this.messageBoxURL,this);
            logger.info("using message box at " + this.messageBoxURL);
        }
        this.client.useAsyncMessaging(correlator);
    }

     public boolean invoke() throws XBayaException {
         final WSIFOperation  operation = this.getOperation();
         final WSIFMessage inputMessage = this.getInputMessage();
         this.setOutputMessage(null);
        try {
              new Thread() {
                @Override
                public void run() {
                    try {
                        operation.executeInputOnlyOperation(inputMessage);
                    } catch (Exception e) {
                        // Ignore the error.
                        logger.error("Error invoking GFac Service",e);
                    }
                }
            }.start();

            while(this.getOutputMessage() == null){
            }
            return true;
        } catch (RuntimeException e) {
            String message = "Error in invoking a service.";
            throw new XBayaException(message, e);
        }
    }
}