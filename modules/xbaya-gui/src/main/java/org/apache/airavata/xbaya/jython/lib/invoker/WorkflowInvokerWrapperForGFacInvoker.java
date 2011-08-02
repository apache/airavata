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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.jython.lib.ServiceNotificationSender;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.workflow.WorkflowInvoker;
import org.xmlpull.v1.builder.XmlElement;

import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul5.MLogger;

public class WorkflowInvokerWrapperForGFacInvoker implements WorkflowInvoker {

    private static final MLogger logger = MLogger.getLogger();

    private GFacInvoker invoker;
    private ServiceNotificationSender notifier;

    private String serviceInformation;

    private Future<Boolean> result;

    protected boolean failerSent = false;

    public WorkflowInvokerWrapperForGFacInvoker(QName portTypeQName, String gfacURL, String messageBoxURL,
            LeadContextHeader leadcontext, ServiceNotificationSender serviceNotificationSender) {
        this.invoker = new GFacInvoker(portTypeQName, gfacURL, messageBoxURL, leadcontext, true);
        this.notifier = serviceNotificationSender;
        this.serviceInformation = portTypeQName.toString();
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#setup()
     */
    public void setup() throws XBayaException {

        this.invoker.setup();
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#setOperation(java.lang.String)
     */
    public void setOperation(String operationName) throws XBayaException {
        this.invoker.setOperation(operationName);
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#setInput(java.lang.String, java.lang.Object)
     */
    public void setInput(String name, Object value) throws XBayaException {

        this.invoker.setInput(name, value);
    }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#invoke()
     */
    public synchronized void invoke() throws XBayaException {

        try {
            WSIFMessage inputMessage = this.invoker.getInputs();
            logger.finest("inputMessage: " + XMLUtil.xmlElementToString((XmlElement) inputMessage));
            this.notifier.invokingService(inputMessage);

            ExecutorService executor = Executors.newSingleThreadExecutor();
            this.result = executor.submit(new Callable<Boolean>() {
                @SuppressWarnings("boxing")
                public Boolean call() {
                    try {
                        boolean success = WorkflowInvokerWrapperForGFacInvoker.this.invoker.invoke();
                        if (success) {
                            // Send notification
                            WSIFMessage outputMessage = WorkflowInvokerWrapperForGFacInvoker.this.invoker.getOutputs();
                            // An implementation of WSIFMessage,
                            // WSIFMessageElement, implements toString(), which
                            // serialize the message XML.
                            logger.finest("outputMessage: " + outputMessage);
                            WorkflowInvokerWrapperForGFacInvoker.this.notifier.serviceFinished(outputMessage);
                        } else {
                            WSIFMessage faultMessage = WorkflowInvokerWrapperForGFacInvoker.this.invoker.getFault();
                            // An implementation of WSIFMessage,
                            // WSIFMessageElement, implements toString(), which
                            // serialize the message XML.
                            logger.finest("received fault: " + faultMessage);
                            WorkflowInvokerWrapperForGFacInvoker.this.notifier.receivedFault(faultMessage);
                            WorkflowInvokerWrapperForGFacInvoker.this.failerSent = true;
                        }
                        return success;
                    } catch (XBayaException e) {
                        logger.caught(e);
                        // An appropriate message has been set in the exception.
                        WorkflowInvokerWrapperForGFacInvoker.this.notifier.invocationFailed(e.getMessage(), e);
                        WorkflowInvokerWrapperForGFacInvoker.this.failerSent = true;
                        throw new XBayaRuntimeException(e);
                    } catch (RuntimeException e) {
                        logger.caught(e);
                        String message = "Error in invoking a service: "
                                + WorkflowInvokerWrapperForGFacInvoker.this.serviceInformation;
                        WorkflowInvokerWrapperForGFacInvoker.this.notifier.invocationFailed(message, e);
                        WorkflowInvokerWrapperForGFacInvoker.this.failerSent = true;
                        throw e;
                    }
                }
            });

            // Kill the thread inside of executor. This is necessary for Jython
            // script to finish.
            executor.shutdown();

            // Let other threads know that job has been submitted.
            notifyAll();

            // Check if the invocation itself fails. This happens immediately.
            try {
                this.result.get(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.caught(e);
            } catch (TimeoutException e) {
                // The job is probably running fine.
                // The normal case.
                return;
            } catch (ExecutionException e) {
                // The service-failed notification should have been sent
                // already.
                logger.caught(e);
                String message = "Error in invoking a service: " + this.serviceInformation;
                throw new XBayaException(message, e);
            }

            logger.exiting();
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "Error in invoking a service: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }

        boolean success = this.invoker.invoke();
        if (!success) {
            try {
                throw new Exception("Failed invoking GFac");
            } catch (Exception e) {
                notifier.invocationFailed(this.invoker.getFault().toString(), e);
            }

        } else {
            notifier.serviceFinished(this.invoker.getOutputs());
        }
    }

    public synchronized void waitToFinish() throws XBayaException {
        logger.entering();
        try {
            while (this.result == null) {
                // The job is not submitted yet.
                try {
                    wait();
                } catch (InterruptedException e) {
                    logger.caught(e);
                }
            }
            // Wait for the job to finish.
            Boolean success = this.result.get();
            if (success == false) {
                WSIFMessage faultMessage = this.invoker.getFault();
                String message = "Error in a service: ";
                // An implementation of WSIFMessage,
                // WSIFMessageElement, implements toString(), which
                // serialize the message XML.
                message += faultMessage.toString();
                throw new XBayaException(message);
            }
            logger.exiting();
        } catch (InterruptedException e) {
            logger.caught(e);
        } catch (ExecutionException e) {
            // The service-failed notification should have been sent already.
            logger.caught(e);
            String message = "Error in invoking a service: " + this.serviceInformation;
            throw new XBayaException(message, e);
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "Error while waiting for a service to finish: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    public Object getOutput(String name) throws XBayaException {
        logger.entering(new Object[] { name });
        try {
            waitToFinish();
            Object output = this.invoker.getOutput(name);
            if (output instanceof XmlElement) {
                logger.finest("output: " + XMLUtil.xmlElementToString((XmlElement) output));
            }
            logger.exiting(output);
            return output;
        } catch (XBayaException e) {
            logger.caught(e);
            // An appropriate message has been set in the exception.
            if (!this.failerSent) {
                this.notifier.invocationFailed(e.getMessage(), e);
            }
            throw e;
        } catch (RuntimeException e) {
            logger.caught(e);
            String message = "Error while waiting for a output: " + name;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        } catch (Error e) {
            logger.caught(e);
            String message = "Unexpected error: " + this.serviceInformation;
            this.notifier.invocationFailed(message, e);
            throw new XBayaException(message, e);
        }
    }

    // /**
    // * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#getOutput(java.lang.String)
    // */
    // public Object getOutput(String name) throws XBayaException {
    // try {
    // // This code doesn't work when the output is a complex type.
    // // Object output = this.outputMessage.getObjectPart(name);
    // // return output;
    //
    // XmlElement outputElement = (XmlElement) this.getOutputs();
    // XmlElement valueElement = outputElement.element(null, name);
    // Iterator childIt = valueElement.children();
    // int numberOfChildren = 0;
    // while (childIt.hasNext()) {
    // childIt.next();
    // numberOfChildren++;
    // }
    // if (numberOfChildren == 1) {
    // Object child = valueElement.children().next();
    // if (child instanceof String) {
    // // Value is a simple type. Return the string.
    // String value = (String) child;
    // return value;
    // }
    // }
    // // Value is a complex type. Return the whole XmlElement so that we
    // // can set it to the next service as it is.
    // return valueElement;
    // } catch (RuntimeException e) {
    // String message = "Error in getting output. name: " + name;
    // throw new XBayaException(message, e);
    // }
    // }

    /**
     * @see org.apache.airavata.xbaya.workflow.WorkflowInvoker#getOutputs()
     */
    public WSIFMessage getOutputs() throws XBayaException {
        return this.invoker.getOutputs();
    }

}