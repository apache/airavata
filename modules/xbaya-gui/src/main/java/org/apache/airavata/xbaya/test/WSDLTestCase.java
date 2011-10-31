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

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.common.utils.IOUtil;
import org.apache.airavata.xbaya.wf.Workflow;

public class WSDLTestCase extends XBayaTestCase {

    // private static final Logger logger = LoggerFactory.getLogger();

    private WorkflowCreator graphCreater;

    /*
     * @see TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        File tmpDir = new File("tmp");
        tmpDir.mkdir();

        this.graphCreater = new WorkflowCreator();
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testSimpleMath() throws ComponentException, GraphException, IOException, ComponentRegistryException {

        Workflow workflow = this.graphCreater.createSimpleMathWorkflow();

        File workflowFile = new File("tmp/simple-math.xwf");
        XMLUtil.saveXML(workflow.toXML(), workflowFile);

        // Creates a Jython script
        File jythonFile = new File("tmp/simple-math.py");
        JythonScript script = new JythonScript(workflow, this.configuration);
        script.create();
        IOUtil.writeToFile(script.getJythonString(), jythonFile);
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws IOException
     * @throws ComponentRegistryException
     */
    public void testComplexMath() throws ComponentException, GraphException, IOException, ComponentRegistryException {
        Workflow workflow = this.graphCreater.createComplexMathWorkflow();
        File graphFile = new File("tmp/complex-math.xwf");
        XMLUtil.saveXML(workflow.toXML(), graphFile);

        // Creates a Jython script
        File jythonFile = new File("tmp/complex-math.py");
        JythonScript script = new JythonScript(workflow, this.configuration);
        script.create();
        IOUtil.writeToFile(script.getJythonString(), jythonFile);
    }
}