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

package org.apache.airavata.xbaya.interpretor;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.XBayaRuntimeException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.graph.system.InputNode;
import org.apache.airavata.xbaya.interpretor.WorkflowInterpretorStub.NameValue;
import org.apache.airavata.xbaya.monitor.MonitorException;
import org.apache.airavata.xbaya.ode.ODEClient;
import org.apache.airavata.xbaya.wf.Workflow;

/**
 * WorkflowInterpretorSkeleton java skeleton for the axisService
 */
public class WorkflowInterpretorSkeleton {

    public static final String XREGISTRY = "xregistry";
    public static final String PROXYSERVER = "proxyserver";
    public static final String MSGBOX = "msgbox";
    public static final String GFAC = "gfac";
    public static final String DSC = "dsc";
    public static final String BROKER = "broker";

    /**
     * Auto generated method signature
     * 
     * @param workflowAsString
     * @param topic
     * @param password
     * @param username
     * @param inputs
     * @param configurations
     */

    public java.lang.String launchWorkflow(java.lang.String workflowAsString, java.lang.String topic,
            java.lang.String password, java.lang.String username, NameValue[] inputs, NameValue[] configurations) {
        System.err.println("Launch is called for topi:");

        Workflow workflow = null;
        try {
            workflow = new Workflow(workflowAsString);
            System.err.println("Workflow Object created");

        } catch (GraphException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (ComponentException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        System.err.println("Setting Input values");
        List<InputNode> inputNodes = new ODEClient().getInputNodes(workflow);
        for (InputNode inputNode : inputNodes) {
            for (NameValue input : inputs) {
                if (inputNode.getName().equals(input.getName())) {
                    inputNode.setDefaultValue(input.getValue());
                    break;
                }
            }
            if (inputNode.getDefaultValue() == null) {
                throw new XBayaRuntimeException("Could not find a input value for component with name :"
                        + inputNode.getName());
            }

        }
        System.err.println("Input all set");

        XBayaConfiguration conf = null;
        try {
            conf = getConfiguration(configurations);
        } catch (URISyntaxException e1) {
            throw new XBayaRuntimeException(e1);
        }
        WorkflowInterpretorEventListener listener = new WorkflowInterpretorEventListener(workflow, conf);
        try {
            listener.start();
        } catch (MonitorException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        WorkflowInterpreter interpreter = new WorkflowInterpreter(conf, topic, workflow, username, password);
        System.err.println("Created the interpreter");
        try {
            interpreter.scheduleDynamically();
            System.err.println("Called the interpreter");

        } catch (XBayaException e) {
            throw new XBayaRuntimeException(e);
        }
        try {
            listener.stop();
        } catch (MonitorException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return topic;
    }

    private XBayaConfiguration getConfiguration(NameValue[] vals) throws URISyntaxException {
        XBayaConfiguration configuration = new XBayaConfiguration();
        configuration.setBrokerURL(new URI(findValue(vals, BROKER, XBayaConstants.DEFAULT_BROKER_URL.toString())));
        configuration.setDSCURL(new URI(findValue(vals, DSC, XBayaConstants.DEFAULT_DSC_URL.toString())));
        configuration.setGFacURL(new URI(findValue(vals, GFAC, XBayaConstants.DEFAULT_GFAC_URL.toString())));
        configuration.setMessageBoxURL(new URI(findValue(vals, MSGBOX,
                XBayaConstants.DEFAULT_MESSAGE_BOX_URL.toString())));
        configuration.setMyProxyLifetime(XBayaConstants.DEFAULT_MYPROXY_LIFTTIME);
        configuration.setMyProxyPort(XBayaConstants.DEFAULT_MYPROXY_PORT);
        configuration.setMyProxyServer(findValue(vals, PROXYSERVER, XBayaConstants.DEFAULT_MYPROXY_SERVER));
        configuration.setXRegistryURL(new URI(findValue(vals, XREGISTRY,
                XBayaConstants.DEFAULT_XREGISTRY_URL.toString())));
        return configuration;
    }

    public String findValue(NameValue[] vals, String key, String defaultVal) {
        for (int i = 0; i < vals.length; i++) {
            if (key.equals(vals[i].getName()) && !"".equals(vals[i].getValue())) {
                return vals[i].getValue();
            }
        }

        return defaultVal;
    }

}
