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
package org.apache.airavata.workflow.engine.invoker;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import xsul.wsdl.WsdlDefinitions;
//import xsul.wsif.WSIFMessage;
//import xsul.wsif.WSIFOperation;
//import xsul.xwsif_runtime_async.WSIFAsyncResponsesCorrelator;
//import xsul.xwsif_runtime_async_http.XsulSoapHttpWsaResponsesCorrelator;

public class AsynchronousInvoker extends SimpleInvoker {

    private static final Logger logger = LoggerFactory.getLogger(AsynchronousInvoker.class);

    private String messageBoxURL;

    /**
     * Constructs an AsynchronousInvoker.
     * 
     * @param definitions
     */
//    public AsynchronousInvoker(WsdlDefinitions definitions) {
//        this(definitions, null);
//    }

    /**
     * Constructs an AsynchronousInvoker.
     * 
     * @param definitions
     * @param messageBoxURL
     */
//    public AsynchronousInvoker(WsdlDefinitions definitions, String messageBoxURL) {
//        super(definitions);
//        this.messageBoxURL = messageBoxURL;
//    }

    /**
     * @see org.apache.airavata.workflow.engine.invoker.SimpleInvoker#setup()
     */
    @Override
    public void setup() throws WorkflowException {
        super.setup();
        /* Set the output message to null to set teh output from async Listener */
//        WSIFAsyncResponsesCorrelator correlator;
//        if (this.messageBoxURL == null || this.messageBoxURL.length() == 0) {
//            correlator = new XsulSoapHttpWsaResponsesCorrelator();
//            String serverLoc = ((XsulSoapHttpWsaResponsesCorrelator) correlator).getServerLocation();
//            logger.debug("using async correlator at " + serverLoc);
//            this.client.useAsyncMessaging(correlator);
//        }
//        else {
//            correlator = new MsgBoxWsaResponsesCorrelator(this.messageBoxURL,this);
//            logger.debug("using message box at " + this.messageBoxURL);
//        }
    }

     public boolean invoke() throws WorkflowException {
////         final WSIFOperation  operation = this.getOperation();
////         final WSIFMessage inputMessage = this.getInputMessage();
////         this.setOutputMessage(null);
        try {
//              new Thread() {
//                @Override
//                public void run() {
//                    try {
//                        operation.executeInputOnlyOperation(inputMessage);
//                    } catch (Exception e) {
//                        // Ignore the error.
//                        logger.error("Error invoking GFac Service",e);
//                    }
//                }
//            }.start();
//
//            while(this.getOutputMessage() == null){
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    logger.error("Error Waiting for the response from backend");
//                }
//            }
//            // Gfac operation failed, so xbaya side throws this exception
//            if("ErrorResponse".equals(XMLUtil.stringToXmlElement3(this.getOutputMessage().toString()).getName())){
//                // Here we do not throw an exception, because if we throw an exception Interpreter will catch it and do the unsubscription,
//                // which is not needed because if there's an gfac side error gfac will send a failure and unsubscription will be done in monitoring
//                // so if we send an exception we are attempting to do two unsubscriptions which will cause a one unsubscription to fail.
//                return false;
//            }
//
            return true;
        } catch (RuntimeException e) {
            String message = "Error in invoking a service.";
            throw new WorkflowException(message, e);
        }
    }
}