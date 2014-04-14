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

import java.io.IOException;

import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.workflow.model.exceptions.WorkflowException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.jython.runner.JythonRunner;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.test.XBayaTestCase;
import org.apache.airavata.xbaya.test.service.adder.AdderService;
import org.apache.airavata.xbaya.test.service.arrayadder.ArrayAdderService;
import org.apache.airavata.xbaya.test.service.arraygen.ArrayGeneratorService;
import org.apache.airavata.xbaya.test.service.multiplier.MultiplierService;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;

public class JythonScriptTestCase extends XBayaTestCase {

    private WorkflowCreator workflowCreator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.workflowCreator = new WorkflowCreator();
    }

    /**
     * @throws IOException
     * @throws WorkflowException
     */
    public void testSimpleMath() throws IOException, WorkflowException {
        Workflow workflow = this.workflowCreator.createSimpleMathWorkflow();
        JythonScript script = new JythonScript(workflow, this.configuration);
        script.create();
        String jythonString = script.getJythonString();
        String filename = "tmp/simple-math.py";
        IOUtil.writeToFile(jythonString, filename);

        AdderService service = new AdderService();
        service.run();
        String adderWSDLLoc = service.getServiceWsdlLocation();

        String[] arguments = new String[] { "-Adder_add_wsdl", adderWSDLLoc };
        JythonRunner runner = new JythonRunner();
        runner.run(jythonString, arguments);

        service.shutdownServer();
    }

    /**
     * @throws IOException
     * @throws WorkflowException
     */
    public void testComplexMath() throws IOException, WorkflowException {
        Workflow workflow = this.workflowCreator.createComplexMathWorkflow();
        JythonScript script = new JythonScript(workflow, this.configuration);
        script.create();
        String jythonString = script.getJythonString();
        String filename = "tmp/complex-math.py";
        IOUtil.writeToFile(jythonString, filename);

        AdderService adder = new AdderService();
        adder.run();
        String adderWSDLLoc = adder.getServiceWsdlLocation();

        MultiplierService multiplier = new MultiplierService();
        multiplier.run();
        String multiplierWSDLLoc = multiplier.getServiceWsdlLocation();

        String[] arguments = new String[] { "-topic", "complex-math", "-Adder_add_wsdl", adderWSDLLoc,
                "-Adder_add_2_wsdl", adderWSDLLoc, "-Multiplier_multiply_wsdl", multiplierWSDLLoc };

        JythonRunner runner = new JythonRunner();
        runner.run(jythonString, arguments);

        adder.shutdownServer();
        multiplier.shutdownServer();
    }

    /**
     * @throws IOException
     * @throws WorkflowException
     */
    public void testArray() throws IOException, WorkflowException {
        Workflow workflow = this.workflowCreator.createArrayWorkflow();
        JythonScript script = new JythonScript(workflow, this.configuration);
        script.create();
        String jythonString = script.getJythonString();
        String filename = "tmp/array-test.py";
        IOUtil.writeToFile(jythonString, filename);

        ArrayGeneratorService arrayGenerator = new ArrayGeneratorService();
        arrayGenerator.run();
        String arrayGeneratorWSDLLoc = arrayGenerator.getServiceWsdlLocation();

        ArrayAdderService arrayAdder = new ArrayAdderService();
        arrayAdder.run();
        String arrayAdderWSDLLoc = arrayAdder.getServiceWsdlLocation();

        String[] arguments = new String[] { "-topic", "array-test", "-ArrayAdder_add_wsdl", arrayAdderWSDLLoc,
                "-ArrayGenerator_generate_wsdl", arrayGeneratorWSDLLoc };

        JythonRunner runner = new JythonRunner();
        runner.run(jythonString, arguments);

        arrayGenerator.shutdownServer();
        arrayAdder.shutdownServer();
    }

}