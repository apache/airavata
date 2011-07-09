/*
 * Copyright (c) 2006-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: GFacServiceCreaterTestCase.java,v 1.17 2008/09/03 18:44:54 cherath Exp $
 */
package org.apache.airavata.xbaya.test;

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.jython.lib.GFacServiceCreator;
import org.apache.airavata.xbaya.jython.lib.GenericInvoker;
import org.apache.airavata.xbaya.jython.lib.NotificationSender;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.util.IOUtil;
import org.apache.airavata.xbaya.util.WSDLUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowInvoker;

import xsul5.MLogger;

/**
 * @author Satoshi Shirasuna
 */
public class GFacServiceCreaterTestCase extends XBayaTestCase {

    private static final String TEST_SERVICE_QNAME = "{http://www.extreme.indiana.edu/lead}TestCMD_Simple";

    private static final String TEST_AWSDL = XBayaPathConstants.WSDL_DIRECTORY + File.separator
            + WorkflowCreator.GFAC_TEST_AWSDL;

    private static final MLogger logger = MLogger.getLogger();

    /**
     * @throws XBayaException
     */
    public void testCreate() throws XBayaException {
        URI gFacURL = this.configuration.getGFacURL();
        URI gFacWSDLURL = WSDLUtil.appendWSDLQuary(gFacURL);
        GFacServiceCreator creator = new GFacServiceCreator(gFacWSDLURL);
        creator.createService(TEST_SERVICE_QNAME);
        creator.shutdownService();
    }

    /**
     * @throws XBayaException
     */
    public void testService() throws XBayaException {
        NotificationSender notifier = new NotificationSender(this.configuration.getBrokerURL(), "test-topic2");
        WorkflowInvoker invoker = new GenericInvoker(QName.valueOf(TEST_SERVICE_QNAME), TEST_AWSDL, "test-node", null,
                this.configuration.getGFacURL().toString(), notifier);
        invoker.setup();
        invoker.setOperation("Run");
        invoker.setInput("inparam1", "test");
        invoker.invoke();
        Object output = invoker.getOutput("outparam1");
        logger.info("output: " + output);
    }

    /**
     * @throws ComponentException
     * @throws IOException
     * @throws GraphException
     * @throws InterruptedException
     * @throws ComponentRegistryException
     */
    public void testWorkflow() throws ComponentException, IOException, GraphException, InterruptedException,
            ComponentRegistryException {
        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow = creator.createGFacWorkflow();

        File workflowFile = new File("tmp/gfac-test.xwf");
        XMLUtil.saveXML(workflow.toXML(), workflowFile);

        JythonScript script = new JythonScript(workflow, this.configuration);
        script.create();
        String jythonString = script.getJythonString();
        String filename = "tmp/gfac-test.py";
        IOUtil.writeToFile(jythonString, filename);

        // String[] argv = new String[] { filename, "-TestCMD_Simple_wsdl",
        // GFAC_TEST_WSDL };
        // jython.main(argv);

        String[] commands = new String[] { "./jython.sh", filename, "-TestCMD_Simple_wsdl", TEST_AWSDL };
        Process process = Runtime.getRuntime().exec(commands);
        int exitValue = process.waitFor();
        logger.info("Exit value: " + exitValue);
        InputStream inputStream = process.getInputStream();
        String output = IOUtil.readToString(inputStream);
        logger.info("output: " + output);
        InputStream errorStream = process.getErrorStream();
        String error = IOUtil.readToString(errorStream);
        logger.info("error: " + error);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2006-2007 The Trustees of Indiana University. All rights reserved.
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
