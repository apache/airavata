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

package org.apache.airavata.xbaya.streaming;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.ode.ODEClient;
import org.apache.airavata.xbaya.security.SecurityUtil;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.xregistry.XRegistryAccesser;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import xsul.invoker.gsi.GsiInvoker;
import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlException;
import xsul5.wsdl.WsdlResolver;

public class StreamClient {

    public void invoke(String operationName, WsdlDefinitions wsdl, String name, String topic) {
        ODEClient odeClient = new ODEClient();
        GSSCredential credential = SecurityUtil.getGSSCredential("chathura", "changeme",
                XBayaConstants.DEFAULT_MYPROXY_SERVER);
        XRegistryAccesser xregistryAccesser = new XRegistryAccesser(credential, XBayaConstants.DEFAULT_XREGISTRY_URL);
        Workflow workflow = xregistryAccesser.getWorkflow(name);

        GsiInvoker secureInvoker = null;
        secureInvoker = new GsiInvoker(credential, XBayaSecurity.getTrustedCertificates());

        try {
            LeadContextHeader defaultLeadHeader = odeClient.getDefaultLeadHeader(workflow, topic, credential.getName()
                    .toString(), new URI(topic), null, null);
            defaultLeadHeader.setExperimentId(topic);
            LEADWorkflowInvoker invoker = new LEADWorkflowInvoker(wsdl, defaultLeadHeader, null, secureInvoker,
                    operationName);

            WSIFMessage inputMessage = invoker.getInputMessage();
            // inputMessage.setObjectPart("input",
            // XmlConstants.BUILDER.newFragment("<n1:Run xmlns:n1='http://www.extreme.indiana.edu/xbaya/demok/xsd/'><file1>fds</file1><file2>fsfas</file2></n1:Run>"));
            inputMessage.setObjectPart("input", "tttt");
            // inputMessage.setObjectPart("file2", "tttt");
            invoker.invoke();
        } catch (ComponentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GSSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XBayaException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param args
     * @throws URISyntaxException
     * @throws WsdlException
     */
    public static void main(String[] args) throws WsdlException, URISyntaxException {

        StreamClient streamTestClient = new StreamClient();
        WsdlDefinitions wsdl = WsdlResolver.getInstance().loadWsdl(
                new URI("http://pagodatree.cs.indiana.edu:17080/ode/processes/Control_Stream_Echo?wsdl"));
        streamTestClient.invoke("Run", wsdl, "Control_Stream_Echo", "9e15d8ae-0f36-4c2e-ae8a-930557cdcdd2");
    }

}