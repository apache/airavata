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

package org.apache.airavata.xbaya.test.jython;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.jython.runner.JythonOneTimeRunnerImpl;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.test.XBayaTestCase;
import org.apache.airavata.xbaya.test.service.adder.AdderService;
import org.apache.airavata.xbaya.test.service.multiplier.MultiplierService;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.wf.Workflow;

public class JythonOneTimeRunnerTestCase extends XBayaTestCase {

    /**
     * @throws XBayaException
     */
    public void testRun() throws XBayaException {

        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow = creator.createComplexMathWorkflow();
        JythonScript script = new JythonScript(workflow, this.configuration);
        script.create();
        String jythonString = script.getJythonString();

        AdderService adder = new AdderService();
        adder.run();
        String adderWSDLLoc = adder.getServiceWsdlLocation();

        MultiplierService multiplier = new MultiplierService();
        multiplier.run();
        String multiplierWSDLLoc = multiplier.getServiceWsdlLocation();

        String[] arguments = new String[] { "-topic", "complex-math", "-Adder_wsdl", adderWSDLLoc, "-Adder_2_wsdl",
                adderWSDLLoc, "-Multiplier_wsdl", multiplierWSDLLoc };

        JythonOneTimeRunnerImpl runner = new JythonOneTimeRunnerImpl();
        runner.run(jythonString, arguments);

        try {
            runner.run(jythonString, arguments);
            fail();
        } catch (Exception e) {
            // It succeeds only once.
        }

        adder.shutdownServer();
        multiplier.shutdownServer();
    }
}