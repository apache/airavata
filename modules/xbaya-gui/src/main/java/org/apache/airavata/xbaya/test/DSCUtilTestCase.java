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

import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.file.XBayaPathConstants;
import org.apache.airavata.xbaya.gpel.DSCUtil;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.wf.Workflow;

import xsul5.MLogger;
import xsul5.wsdl.WsdlDefinitions;
import xsul5.wsdl.WsdlException;
import xsul5.wsdl.WsdlResolver;

public class DSCUtilTestCase extends XBayaTestCase {

    private static final String SAMPLE_AWSDL = XBayaPathConstants.WSDL_DIRECTORY + "/test/adder-awsdl.xml";

    private static final String WSDL_WITH_MULTIPLE_PORT_TYPES = XBayaPathConstants.BPEL_SCRIPT_DIRECTORY
            + File.separator + "receive-test-wsdl.xml";

    private static final MLogger logger = MLogger.getLogger();

    /**
     * @throws WsdlException
     */
    public void testConvertToCWSDL() throws WsdlException {
        WsdlDefinitions definitions = WsdlResolver.getInstance().loadWsdl(new File(".").toURI(),
                new File(SAMPLE_AWSDL).toURI());
        DSCUtil.convertToCWSDL(definitions, this.configuration.getDSCURL());
        logger.info(definitions.xmlStringPretty());
    }

    /**
     * 
     */
    public void testMultiplePortTypes() {
        WsdlDefinitions definitions = WsdlResolver.getInstance().loadWsdl(new File(".").toURI(),
                new File(WSDL_WITH_MULTIPLE_PORT_TYPES).toURI());
        DSCUtil.convertToCWSDL(definitions, this.configuration.getDSCURL());
        logger.info(definitions.xmlStringPretty());
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws ComponentRegistryException
     */
    public void testConvertToCWSDLs() throws ComponentException, GraphException, ComponentRegistryException {
        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow = creator.createComplexMathWorkflow();
        DSCUtil.createCWSDLs(workflow, this.configuration.getDSCURL());
    }
}