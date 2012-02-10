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

package org.apache.airavata.xbaya.test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.common.utils.WSDLUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.invoker.GenericInvoker;
import org.apache.airavata.xbaya.invoker.Invoker;
import org.apache.airavata.xbaya.jython.lib.GFacServiceCreator;
import org.apache.airavata.xbaya.jython.lib.NotificationSender;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.wf.Workflow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GFacServiceCreaterTestCase extends XBayaTestCase {

    private static final String TEST_SERVICE_QNAME = "{http://www.extreme.indiana.edu/lead}TestCMD_Simple";

    private static final String TEST_AWSDL = XBayaPathConstants.WSDL_DIRECTORY + File.separator
            + WorkflowCreator.GFAC_TEST_AWSDL;

    private static final Logger logger = LoggerFactory.getLogger(GFacServiceCreaterTestCase.class);

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
        Invoker invoker = new GenericInvoker(QName.valueOf(TEST_SERVICE_QNAME), TEST_AWSDL, "test-node", null,
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