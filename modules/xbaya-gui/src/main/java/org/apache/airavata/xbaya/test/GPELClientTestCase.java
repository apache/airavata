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

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import junit.framework.TestSuite;

import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaException;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.ws.WSComponentPort;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.lead.LEADWorkflowInvoker;
import org.apache.airavata.xbaya.lead.LeadContextHeaderHelper;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.test.util.WorkflowCreator;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowClient.WorkflowType;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.apache.airavata.xbaya.workflow.WorkflowEngineManager;
import org.globus.gsi.CertUtil;
import org.gpel.client.GcInstance;
import org.gpel.client.GcSearchList;
import org.gpel.client.GcSearchResult;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;

import xsul.lead.LeadContextHeader;
import xsul.wsif.WSIFMessage;
import xsul5.MLogger;
import xsul5.wsdl.WsdlDefinitions;

public class GPELClientTestCase extends XBayaTestCase {

    private static final MLogger logger = MLogger.getLogger();

    /**
     * @param args
     */
    public static void main(String[] args) {
        junit.textui.TestRunner.run(new TestSuite(GPELClientTestCase.class));
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws WorkflowEngineException
     * @throws ComponentRegistryException
     */
    public void XtestDeploy() throws ComponentException, GraphException, WorkflowEngineException,
            ComponentRegistryException {
        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow = creator.createComplexMathWorkflow();
        workflow.setName("this is test name");
        workflow.setDescription("this is test description");
        WorkflowClient client = WorkflowEngineManager.getWorkflowClient();
        client.setEngineURL(XBayaConstants.DEFAULT_GPEL_ENGINE_URL);

        client.createScriptAndDeploy(workflow, false);

        GcSearchList list = client.list();
        for (GcSearchResult result : list.results()) {
            logger.info(result.getTitle());
        }

        client.load(workflow.getGPELTemplateID());
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws WorkflowEngineException
     * @throws ComponentRegistryException
     */
    public void XtestRedeploy() throws ComponentException, GraphException, WorkflowEngineException,
            ComponentRegistryException {
        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow = creator.createComplexMathWorkflow();
        WorkflowClient client = WorkflowEngineManager.getWorkflowClient();
        client.setEngineURL(XBayaConstants.DEFAULT_GPEL_ENGINE_URL);

        client.createScriptAndDeploy(workflow, false);

        client.createScriptAndDeploy(workflow, true);
    }

    /**
     * @throws WorkflowEngineException
     */
    public void XtestListWorkflowInstances() throws WorkflowEngineException {
        WorkflowClient client = WorkflowEngineManager.getWorkflowClient();
        client.setEngineURL(XBayaConstants.DEFAULT_GPEL_ENGINE_URL);
        GcSearchList searchList = client.list(100, WorkflowType.INSTANCE);
        for (GcSearchResult result : searchList.results()) {
            URI id = result.getId();
            logger.info("id: " + id);
        }
    }

    /**
     * @throws WorkflowEngineException
     */
    public void XtestList() throws WorkflowEngineException {

        // The URL of the GPEL Engine.
        URI gpelEngineURL = XBayaConstants.DEFAULT_GPEL_ENGINE_URL;

        // Create a client.
        WorkflowClient client = WorkflowEngineManager.getWorkflowClient();
        client.setEngineURL(gpelEngineURL);

        // XXX This is supposed to return the workflow templates related to the
        // user only. But users are not implemented in GPEL yet.
        GcSearchList results = client.list();

        for (GcSearchResult result : results.results()) {

            // The title to show to the user.
            String title = result.getTitle();
            logger.info("title: " + title);

            // ID to load the workflow from the GPEL engine.
            URI templateID = result.getId();
            logger.info("templateID: " + templateID);

            // XXX Not implemented yet? It always returns -1.
            Date lastModified = new Date(result.getLastModfied());
            logger.info("lastModified: " + lastModified);
        }
    }

    /**
     * @throws ComponentException
     * @throws GraphException
     * @throws WorkflowEngineException
     * @throws ComponentRegistryException
     */
    public void XtestStart() throws ComponentException, GraphException, WorkflowEngineException,
            ComponentRegistryException {
        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow = creator.createSimpleMathWorkflow();

        WorkflowClient client = WorkflowEngineManager.getWorkflowClient();
        client.setEngineURL(XBayaConstants.DEFAULT_GPEL_ENGINE_URL);

        client.createScriptAndDeploy(workflow, false);

        // Instantiate the workflow template.
        GcInstance instance = client.instantiate(workflow, this.configuration.getDSCURL());

        // Start the workflow instance.
        WsdlDefinitions wsdl = client.start(instance);

        logger.info(wsdl.xmlStringPretty());
    }

    /**
     * @throws XBayaException
     */
    public void testLoadInstance() throws XBayaException {
        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow0 = creator.createComplexMathWorkflow();

        WorkflowClient client = WorkflowEngineManager.getWorkflowClient();
        client.setEngineURL(XBayaConstants.DEFAULT_GPEL_ENGINE_URL);

        // Deploy
        URI templateID = client.createScriptAndDeploy(workflow0, false);

        // Load a workflow template from the GPEL Engine.
        Workflow workflowTemplate = client.load(templateID);

        // Instantiate the workflow template.
        GcInstance instance = client.instantiate(workflowTemplate, this.configuration.getDSCURL());
        // ID to retrieve the workflow instance
        URI instanceID = instance.getInstanceId();
        logger.info("instanceID: " + instanceID);

        // Start the workflow instance.
        // WsdlDefinitions wsdl = client.start(instance);

        Workflow workflowInstance = client.load(instanceID, WorkflowType.INSTANCE);
        assertNotNull(workflowInstance);
    }

    /**
     * @throws XBayaException
     */
    public void XtestInvoke() throws XBayaException {

        WorkflowCreator creator = new WorkflowCreator();
        Workflow workflow0 = creator.createGFacWorkflow();

        WorkflowClient client = WorkflowEngineManager.getWorkflowClient();
        client.setEngineURL(XBayaConstants.DEFAULT_GPEL_ENGINE_URL);

        URI templateID = client.createScriptAndDeploy(workflow0, false);

        // Load a workflow template from the GPEL Engine.
        Workflow workflow = client.load(templateID);

        // Get the metadata for input.
        XmlElement inputAppinfo = workflow.getInputMetadata();
        logger.info("inputAppinfo: " + XMLUtil.xmlElementToString(inputAppinfo));

        // Get the input information
        List<WSComponentPort> inputs = workflow.getInputs();

        for (WSComponentPort input : inputs) {
            // Show the information of each input.

            // Name
            String name = input.getName();
            logger.info("name: " + name);

            // Type
            QName type = input.getType();
            logger.info("type: " + type);

            // Metadata as XML
            XmlElement appinfo = input.getAppinfo();
            logger.info("appinfo: " + XMLUtil.xmlElementToString(appinfo));
            if (appinfo != null) {
                // Parse the simple case.
                for (XmlElement element : appinfo.requiredElementContent()) {
                    String tag = element.getName();
                    String value = element.requiredText();
                    logger.info(tag + " = " + value);
                }
            }

            // Set a value to each input.
            input.setValue("200");
        }

        // Instantiate the workflow template.
        GcInstance instance = client.instantiate(workflow, this.configuration.getDSCURL());
        // ID to retrieve the workflow instance
        URI instanceID = instance.getInstanceId();
        logger.info("instanceID: " + instanceID);

        // Start the workflow instance.
        WsdlDefinitions wsdl = client.start(instance);

        // Create lead context.
        LeadContextHeaderHelper leadContextHelper = new LeadContextHeaderHelper();
        leadContextHelper.setXBayaConfiguration(this.configuration);
        LeadContextHeader leadContext = leadContextHelper.getLeadContextHeader();

        URI messageBoxURL = null;
        if (this.configuration.isPullMode()) {
            messageBoxURL = this.configuration.getMessageBoxURL();
        }

        // Create an invoker to invoke the workflow instance.
        LEADWorkflowInvoker invoker = new LEADWorkflowInvoker(wsdl, leadContext, messageBoxURL);

        // Set the input values to the invoker.
        invoker.setInputs(inputs);

        // Invoke the workflow. This will block, so you may want to do it in a
        // thread.
        boolean success = invoker.invoke();
        logger.info("success: " + success);

        // We don't need to wait for the outputs.

        if (success) {
            List<WSComponentPort> outputs = invoker.getOutputs();
            for (WSComponentPort output : outputs) {
                String name = output.getName();
                logger.info("name: " + name);
                Object value = output.getValue();
                logger.info("value: " + value);
            }
        } else {
            WSIFMessage fault = invoker.getFault();
            logger.info("fault: " + fault);
        }

    }

    /**
     * @throws WorkflowEngineException
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public void XtestSecurity() throws WorkflowEngineException, IOException, GeneralSecurityException {
        boolean userCred = true;

        URI engineURL = XBayaConstants.DEFAULT_GPEL_ENGINE_URL;
        GSSCredential proxy = null;

        String trustedcerts = System.getProperty("trustedcerts");
        String certskey = System.getProperty("certskey");

        UserX509Credential credential;
        if (userCred) {
            // Using user credential
            credential = new UserX509Credential(proxy, CertUtil.loadCertificates(trustedcerts));
        } else {
            // Using service certificate.
            credential = new UserX509Credential(certskey, trustedcerts);
        }
        WorkflowClient client = WorkflowEngineManager.getWorkflowClient(engineURL, credential);
        assertNotNull(client);
    }
}