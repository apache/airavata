/*
 * Copyright (c) 2005-2007 Extreme! Lab, Indiana University. All rights reserved.
 *
 * This software is open source. See the bottom of this file for the license.
 *
 * $Id: BPELScriptTestCase.java,v 1.26 2008/04/01 21:44:24 echintha Exp $
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

import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.gpel.script.BPELScript;
import org.apache.airavata.xbaya.gpel.script.BPELScriptType;
import org.apache.airavata.xbaya.gpel.script.WorkflowWSDL;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.util.IOUtil;
import org.apache.airavata.xbaya.util.XMLUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.gpel.model.GpelProcess;
import org.xmlpull.infoset.XmlElement;

import xsul5.wsdl.WsdlDefinitions;

/**
 * @author Satoshi Shirasuna
 */
public class BPELScriptTestCase extends XBayaTestCase {

    // private static final MLogger logger = MLogger.getLogger();

    private WorkflowCreator workflowCreator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        this.workflowCreator = new WorkflowCreator();
    }

    /**
     * @throws ComponentException
     * @throws IOException
     * @throws GraphException
     * @throws ComponentRegistryException
     */
    public void testSimpleMath() throws ComponentException, IOException, GraphException, ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createSimpleMathWorkflow();
        testWrokflow(workflow, "simple-math");
    }

    /**
     * @throws ComponentException
     * @throws IOException
     * @throws GraphException
     * @throws ComponentRegistryException
     */
    public void testMath() throws ComponentException, IOException, GraphException, ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createMathWorkflow();
        testWrokflow(workflow, "math");
    }

    /**
     * @throws ComponentException
     * @throws IOException
     * @throws GraphException
     * @throws ComponentRegistryException
     */
    public void testComplexMath() throws ComponentException, IOException, GraphException, ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createComplexMathWorkflow();
        testWrokflow(workflow, "complex-math");
    }

    /**
     * @throws ComponentException
     * @throws IOException
     * @throws GraphException
     * @throws ComponentRegistryException
     */
    public void testMathWithConstant() throws ComponentException, IOException, GraphException,
            ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createMathWithConstWorkflow();
        testWrokflow(workflow, "constant-test");
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testArray() throws ComponentException, GraphException, IOException, ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createArrayWorkflow();
        testWrokflow(workflow, "array-test");
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testForEach() throws ComponentException, GraphException, IOException, ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createForEachWorkflow();
        testWrokflow(workflow, "foreach-test");
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testIf() throws ComponentException, GraphException, IOException, ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createIfWorkflow();
        testWrokflow(workflow, "if-test");
    }

    /**
     * @throws GraphException
     * @throws ComponentException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testReceive() throws GraphException, ComponentException, IOException, ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createReceiveWorkflow();
        testWrokflow(workflow, "receive-test");
    }

    /**
     * @throws GraphException
     * @throws ComponentException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testLoan() throws GraphException, ComponentException, IOException, ComponentRegistryException {
        Workflow workflow = this.workflowCreator.createLoanWorkflow();
        testWrokflow(workflow, "loan");
    }

    private void testWrokflow(Workflow workflow, String filename) throws GraphException, IOException,
            ComponentException {

        // This one is for debuggin in case something below fails.
        XMLUtil.saveXML(workflow.toXML(), new File(this.temporalDirectory, filename + "-0.xwf"));

        // Create BPEL
        BPELScript bpel = new BPELScript(workflow);
        bpel.create(BPELScriptType.GPEL);
        GpelProcess gpelProcess = bpel.getGpelProcess();
        WorkflowWSDL workflowWSDL = bpel.getWorkflowWSDL();
        WsdlDefinitions definitions = workflowWSDL.getWsdlDefinitions();

        File bpelFile = new File(this.temporalDirectory, filename + XBayaConstants.BPEL_SUFFIX);
        File wsdlFile = new File(this.temporalDirectory, filename + XBayaConstants.WSDL_SUFFIX);
        XMLUtil.saveXML(gpelProcess.xml(), bpelFile);
        XMLUtil.saveXML(definitions.xml(), wsdlFile);

        // Save the workflow
        File workflowFile = new File(this.temporalDirectory, filename + XBayaConstants.WORKFLOW_FILE_SUFFIX);
        XMLUtil.saveXML(workflow.toXML(), workflowFile);

        // Read the workflow
        XmlElement workflowElement = XMLUtil.loadXML(workflowFile);
        workflow = new Workflow(workflowElement);

        // Create BPEL again
        bpel = new BPELScript(workflow);
        bpel.create(BPELScriptType.GPEL);
        gpelProcess = bpel.getGpelProcess();
        workflowWSDL = bpel.getWorkflowWSDL();
        definitions = workflowWSDL.getWsdlDefinitions();

        File bpelFile2 = new File(this.temporalDirectory, filename + "-2" + XBayaConstants.BPEL_SUFFIX);
        File wsdlFile2 = new File(this.temporalDirectory, filename + "-2" + XBayaConstants.WSDL_SUFFIX);
        XMLUtil.saveXML(gpelProcess.xml(), bpelFile2);
        XMLUtil.saveXML(definitions.xml(), wsdlFile2);

        File workflowFile2 = new File(this.temporalDirectory, filename + "-2" + XBayaConstants.WORKFLOW_FILE_SUFFIX);
        XMLUtil.saveXML(workflow.toXML(), workflowFile2);

        // Compare
        String workflowString = IOUtil.readFileToString(workflowFile);
        String workflowString2 = IOUtil.readFileToString(workflowFile2);
        assertEquals(workflowString, workflowString2);

        String bpelString = IOUtil.readFileToString(bpelFile);
        String bpelString2 = IOUtil.readFileToString(bpelFile2);
        assertEquals(bpelString, bpelString2);

        String wsdlString = IOUtil.readFileToString(wsdlFile);
        String wsdlString2 = IOUtil.readFileToString(wsdlFile2);
        assertEquals(wsdlString, wsdlString2);
    }
}

/*
 * Indiana University Extreme! Lab Software License, Version 1.2
 * 
 * Copyright (c) 2005-2007 The Trustees of Indiana University. All rights reserved.
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
