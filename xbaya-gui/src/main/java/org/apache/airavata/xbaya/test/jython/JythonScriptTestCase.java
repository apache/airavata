/*
 * Copyright (c) 2005-2006 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: JythonScriptTestCase.java,v 1.10 2008/04/01 21:44:34 echintha Exp $
 */
package org.apache.airavata.xbaya.test.jython;

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

import java.io.IOException;

import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.jython.runner.JythonRunner;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.test.XBayaTestCase;
import org.apache.airavata.xbaya.test.service.adder.AdderService;
import org.apache.airavata.xbaya.test.service.arrayadder.ArrayAdderService;
import org.apache.airavata.xbaya.test.service.arraygen.ArrayGeneratorService;
import org.apache.airavata.xbaya.test.service.multiplier.MultiplierService;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.util.IOUtil;
import org.apache.airavata.xbaya.wf.Workflow;

/**
 * @author Satoshi Shirasuna
 */
public class JythonScriptTestCase extends XBayaTestCase {

    private WorkflowCreator workflowCreator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.workflowCreator = new WorkflowCreator();
    }

    /**
     * @throws IOException
     * @throws XBayaException
     */
    public void testSimpleMath() throws IOException, XBayaException {
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
     * @throws XBayaException
     */
    public void testComplexMath() throws IOException, XBayaException {
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
     * @throws XBayaException
     */
    public void testArray() throws IOException, XBayaException {
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

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005-2006 The Trustees of Indiana University. All rights reserved.
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
