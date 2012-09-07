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

package org.apache.airavata.xbaya.registry;

import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.xml.namespace.QName;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.workflow.model.component.ComponentException;
import org.apache.airavata.workflow.model.component.registry.JCRComponentRegistry;
import org.apache.airavata.workflow.model.graph.GraphException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.ui.experiment.RegistryWorkflowPublisherWindow;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;

public class RegistryAccesser {

    /**
     * PUBLIC_ACTOR
     */
    public static final String PUBLIC_ACTOR = "public";

    private XBayaEngine engine;

    private GSSCredential gssCredential;

    /**
     * Constructs a RegistryAccesser.
     * 
     * @param engine
     */
    public RegistryAccesser(XBayaEngine engine) {
        this.engine = engine;
    }

    private AiravataRegistry2 connectToRegistry() {
        JCRComponentRegistry jcrComponentRegistry = this.engine.getConfiguration().getJcrComponentRegistry();
        return jcrComponentRegistry.getRegistry();
    }

    /**
     * 
     * @return
     * @throws RepositoryException
     */
    public Map<String, String> getOGCEWorkflowTemplateList() throws RegistryException {
        AiravataRegistry2 registry = connectToRegistry();
        return registry.getWorkflows();
    }

    /**
     * 
     * @param workflowTemplateId
     * @return
     * @throws RepositoryException
     * @throws GraphException
     * @throws ComponentException
     * @throws Exception
     */
    public Workflow getOGCEWorkflow(QName workflowTemplateId) throws RepositoryException, GraphException,
            ComponentException, Exception {
        AiravataRegistry registry = connectToRegistry();
        Node node = registry.getWorkflow(workflowTemplateId, this.engine.getConfiguration().getRegistryUserName());
        XmlElement xwf = XMLUtil.stringToXmlElement(node.getProperty("workflow").getString());
        Workflow workflow = new Workflow(xwf);
        return workflow;
    }

    /**
     * Save workflow in to Registry
     */
    public boolean saveWorkflow() {
        if (XBayaUtil.acquireJCRRegistry(this.engine)) {
            try {

                Workflow workflow = this.engine.getGUI().getWorkflow();
                JythonScript script = new JythonScript(workflow, this.engine.getConfiguration());

                // Check if there is any errors in the workflow first.
                ArrayList<String> warnings = new ArrayList<String>();
                if (!script.validate(warnings)) {
                    StringBuilder buf = new StringBuilder();
                    for (String warning : warnings) {
                        buf.append("- ");
                        buf.append(warning);
                        buf.append("\n");
                    }
                    this.engine.getGUI().getErrorWindow().warning(buf.toString());
                    return false;
                }
                RegistryWorkflowPublisherWindow registryPublishingWindow = new RegistryWorkflowPublisherWindow(
                        this.engine);
                registryPublishingWindow.show();

                String workflowId = workflow.getName();

                workflowId = StringUtil.convertToJavaIdentifier(workflowId);

                QName workflowQName = new QName(XBayaConstants.OGCE_WORKFLOW_NS, workflowId);

                String workflowAsString = XMLUtil.xmlElementToString(workflow.toXML());
                String owner = this.engine.getConfiguration().getRegistryUserName();

                AiravataRegistry registry = this.connectToRegistry();
                boolean result = registry.saveWorkflow(workflowQName, workflow.getName(), workflow.getDescription(), workflowAsString,
                        owner, registryPublishingWindow.isMakePublic());
                registryPublishingWindow.hide();
                return result;
            } catch (Exception e) {
                this.engine.getGUI().getErrorWindow().error(e.getMessage(), e);
            }
        }
		return false;
    }

    /**
     * 
     * @param workflowTemplateId
     * @throws RepositoryException
     */
    public void deleteOGCEWorkflow(QName workflowTemplateId) throws RegistryException {
        if (XBayaUtil.acquireJCRRegistry(this.engine)) {
            AiravataRegistry registry = connectToRegistry();
            registry.deleteWorkflow(workflowTemplateId, this.engine.getConfiguration().getRegistryUserName());
        }
    }

    /**
     * 
     * @param qname
     * @return
     */
    public Workflow getWorkflow(QName qname) throws RegistryException {
        AiravataRegistry registry = connectToRegistry();
        Node node = registry.getWorkflow(qname, this.engine.getConfiguration().getRegistryUserName());
        Workflow workflow = null;
        try {
            XmlElement xwf = XMLUtil.stringToXmlElement(node.getProperty("workflow").getString());
            workflow = new Workflow(xwf);
        } catch (GraphException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        } catch (ComponentException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        } catch (RepositoryException e) {
            e.printStackTrace(); // To change body of catch statement use File | Settings | File Templates.
        }
        return workflow;
    }

    /**
     * 
     * @param name
     * @return
     * @throws RegistryException 
     */
    public Workflow getWorkflow(String name) throws RegistryException {
        return getWorkflow(new QName(XBayaConstants.LEAD_NS, name));
    }

    public void main() {

        XBayaConfiguration config = new XBayaConfiguration();
        config.setMyProxyServer("myproxy.teragrid.org");
        config.setMyProxyUsername("USER");
        config.setMyProxyPassphrase("PASSWORD");

        new XBayaEngine(config);
    }
}