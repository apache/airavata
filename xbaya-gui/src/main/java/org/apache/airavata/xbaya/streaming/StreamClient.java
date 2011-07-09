/*
 * Copyright (c) 2009 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: $
 */
package org.apache.airavata.xbaya.streaming;

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

/**
 * @author Chathura Herath
 */
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

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2009 The Trustees of Indiana University. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1) All redistributions of source code must retain the above copyright notice, the list of authors in the original
 * source code, this list of conditions and the disclaimer listed in this license;
 * 
 * 2) All redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * disclaimer listed in this license in the documentation and/or other materials provided with the distribution;
 * 
 * 3) Any documentation included with all redistributions must include the following acknowledgement:
 * 
 * "This product includes software developed by the Indiana University Extreme! Lab. For further information please
 * visit http://www.extreme.indiana.edu/"
 * 
 * Alternatively, this acknowledgment may appear in the software itself, and wherever such third-party acknowledgments
 * normally appear.
 * 
 * 4) The name "Indiana University" or "Indiana University Extreme! Lab" shall not be used to endorse or promote
 * products derived from this software without prior written permission from Indiana University. For written permission,
 * please contact http://www.extreme.indiana.edu/.
 * 
 * 5) Products derived from this software may not use "Indiana University" name nor may "Indiana University" appear in
 * their name, without prior written permission of the Indiana University.
 * 
 * Indiana University provides no reassurances that the source code provided does not infringe the patent or any other
 * intellectual property rights of any other entity. Indiana University disclaims any liability to any recipient for
 * claims brought by any other entity based on infringement of intellectual property rights or otherwise.
 * 
 * LICENSEE UNDERSTANDS THAT SOFTWARE IS PROVIDED "AS IS" FOR WHICH NO WARRANTIES AS TO CAPABILITIES OR ACCURACY ARE
 * MADE. INDIANA UNIVERSITY GIVES NO WARRANTIES AND MAKES NO REPRESENTATION THAT SOFTWARE IS FREE OF INFRINGEMENT OF
 * THIRD PARTY PATENT, COPYRIGHT, OR OTHER PROPRIETARY RIGHTS. INDIANA UNIVERSITY MAKES NO WARRANTIES THAT SOFTWARE IS
 * FREE FROM "BUGS", "VIRUSES", "TROJAN HORSES", "TRAP DOORS", "WORMS", OR OTHER HARMFUL CODE. LICENSEE ASSUMES THE
 * ENTIRE RISK AS TO THE PERFORMANCE OF SOFTWARE AND/OR ASSOCIATED MATERIALS, AND TO THE PERFORMANCE AND VALIDITY OF
 * INFORMATION GENERATED USING SOFTWARE.
 */
