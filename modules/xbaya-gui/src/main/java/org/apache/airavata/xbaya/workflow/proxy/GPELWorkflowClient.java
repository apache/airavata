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

package org.apache.airavata.xbaya.workflow.proxy;

import java.net.URI;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.gpel.GPELClient;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gpel.script.BPELScriptType;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.lead.LEADTypes;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.ode.ODEClient;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.util.Pair;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.gpel.client.GcInstance;

import xsul.XmlConstants;
import xsul.invoker.gsi.GsiInvoker;
import xsul5.wsdl.WsdlDefinitions;
import edu.indiana.extreme.util.mini_logger.MLogger;

public class GPELWorkflowClient implements WorkflowClient {

    private static final MLogger logger = MLogger.getLogger();

    private WorkflowContext context;
    private Workflow workflow;
    private GcInstance instance;
    private WsdlDefinitions wsdl;

    public GPELWorkflowClient(WorkflowContext context, Workflow workflow) {
        this.context = context;
        this.workflow = workflow;
    }

    public void init() {

        try {
            UserX509Credential credential = new UserX509Credential(context.getCredentials(),
                    XBayaSecurity.getTrustedCertificates());

            BPELScript bpel = new BPELScript(workflow);
            bpel.create(BPELScriptType.GPEL);
            workflow.getGpelProcess();
            workflow.setGpelProcess(bpel.getGpelProcess());
            workflow.setWorkflowWSDL(bpel.getWorkflowWSDL().getWsdlDefinitions());

            GPELClient client = new GPELClient(XBayaConstants.DEFAULT_GPEL_ENGINE_URL, credential);// changed from
                                                                                                   // constants
            client.deploy(workflow, false);
            this.instance = client.instantiate(workflow, XBayaConstants.DEFAULT_DSC_URL, context.getTopic());
            this.wsdl = client.start(instance);
        } catch (WorkflowEngineException e) {
            throw new RuntimeException(e);
        } catch (ComponentException e) {
            throw new RuntimeException(e);
        } catch (GraphException e) {
            throw new RuntimeException(e);
        }

    }

    public String getInstanceID() {
        return this.instance.getInstanceId().toString();
    }

    public Pair<String, String> invoke(Pair<String, String>[] inputs) {

        List<WSComponentPort> wfInputs = new ODEClient().getInputs(this.workflow);

        for (Iterator<WSComponentPort> iterator = wfInputs.iterator(); iterator.hasNext();) {
            WSComponentPort componentPort = iterator.next();

            String value = getWorkflowInputValue(inputs, componentPort.getName());

            if (value == null) {
                logger.severe("EXCEPTION: Value for input component is not set");
                throw new RuntimeException("Value for input component is not set" + componentPort.getName());
            } else {
                componentPort.setDefaultValue(value);
            }

            logger.info("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$" + componentPort.getName());
            logger.info(componentPort.getDefaultValue());
        }

        for (Iterator<WSComponentPort> iterator = wfInputs.iterator(); iterator.hasNext();) {
            WSComponentPort componentPort = iterator.next();
            Object value = parseValue(componentPort, componentPort.getDefaultValue());
            if (value == null) {
                return null;
            }
            componentPort.setValue(value);
        }

        LEADWorkflowInvoker invoker = null;
        try {

            URI messageBoxURL = XBayaConstants.DEFAULT_MESSAGE_BOX_URL;

            // create an invoker with LEAD Context
            GsiInvoker secureInvoker = new GsiInvoker(this.context.getCredentials(),
                    XBayaSecurity.getTrustedCertificates());

            invoker = new LEADWorkflowInvoker(this.wsdl, context.getHeader(), messageBoxURL, secureInvoker);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        invoker.setInputs(wfInputs);

        final LEADWorkflowInvoker workflowInvoker = invoker;
        logger.info("Invoking .....");

        new Thread() {
            @Override
            public synchronized void run() {
                try {
                    boolean success = workflowInvoker.invoke();
                    String result = null;
                    if (success) {
                        result = XmlConstants.BUILDER.serializeToString(workflowInvoker.getOutputMessage());
                    } else {
                        result = XmlConstants.BUILDER.serializeToString(workflowInvoker.getFaultMessage());
                    }
                    logger.info("Result from workflow invocation => " + result);
                } catch (XBayaException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }.start();

        return new Pair<String, String>(this.context.getTopic(), this.getInstanceID());

    }

    private static Object parseValue(WSComponentPort input, String valueString) {
        String name = input.getName();
        if (false) {
            // Some user wants to pass empty strings, so this check is disabled.
            if (valueString.length() == 0) {
                throw new RuntimeException("Input parameter, " + name + ", cannot be empty");
            }
        }
        QName type = input.getType();
        Object value;
        if (LEADTypes.isKnownType(type)) {
            // TODO check the type.
            value = valueString;
        } else {
            try {
                value = XMLUtil.stringToXmlElement3(valueString);
            } catch (RuntimeException e) {
                throw e;
            }
        }
        return value;
    }

    private static String getWorkflowInputValue(Pair<String, String>[] vals, String key) {
        for (Pair<String, String> input : vals) {
            if (input.getLeft().equalsIgnoreCase(key) == true) {
                return input.getRight();
            }
        }
        return null;
    }
}
