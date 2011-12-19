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

import org.apache.airavata.common.utils.StringUtil;
import org.apache.airavata.common.utils.XMLUtil;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.xbaya.XBayaConfiguration;
import org.apache.airavata.xbaya.XBayaConstants;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.experiment.gui.RegistryWorkflowPublisherWindow;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.jython.script.JythonScript;
import org.apache.airavata.xbaya.util.XBayaUtil;
import org.apache.airavata.xbaya.wf.Workflow;
import org.ietf.jgss.GSSCredential;
import org.xmlpull.infoset.XmlElement;

public class RegistryAccesser {

    /**
     * PUBLIC_ACTOR
     */
    public static final String PUBLIC_ACTOR = "public";

    private XBayaEngine engine;

    private GSSCredential gssCredential;

    private URI xregistryURL;

    /**
     * Constructs a XRegistryAccesser.
     * 
     * @param engine
     */
    public RegistryAccesser(XBayaEngine engine) {
        this.engine = engine;
    }

    private Registry connectToRegistry() {
        JCRComponentRegistry jcrComponentRegistry = this.engine.getConfiguration().getJcrComponentRegistry();
        return jcrComponentRegistry.getRegistry();
    }

    /**
     * 
     * @return
     * @throws RepositoryException
     */
    public Map<QName, Node> getOGCEWorkflowTemplateList() throws RepositoryException {
        Registry registry = connectToRegistry();
        return registry.getWorkflows(this.engine.getConfiguration().getRegigstryUserName());
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
        Registry registry = connectToRegistry();
        Node node = registry.getWorkflow(workflowTemplateId, this.engine.getConfiguration().getRegigstryUserName());
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

                Workflow workflow = this.engine.getWorkflow();
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
                    this.engine.getErrorWindow().warning(buf.toString());
                    return false;
                }
                RegistryWorkflowPublisherWindow registryPublishingWindow = new RegistryWorkflowPublisherWindow(
                        this.engine);
                registryPublishingWindow.show();

                String workflowId = workflow.getName();

                workflowId = StringUtil.convertToJavaIdentifier(workflowId);

                // FIXME::Commenting the workflow UUID. It is debatable if the
                // workflow template id should be unique or not.
                // workflowId = workflowId + UUID.randomUUID();

                QName workflowQName = new QName(XBayaConstants.OGCE_WORKFLOW_NS, workflowId);

                // first find whether this resource is already in xregistry
                // TODO: Add the check back
                // DocData[] resource =
                // client.findOGCEResource(workflowQName.toString(), "Workflow",
                // null);
                // if (resource != null && !"".equals(resource)) {
                // // if already there then remove
                //
                // int result =
                // JOptionPane.showConfirmDialog(this.engine.getGUI().getGraphCanvas().getSwingComponent(),
                // "Workflow Already Exist in Xregistry. Do you want to overwrite",
                // "Workflow already exist", JOptionPane.YES_NO_OPTION);
                // if(result != JOptionPane.YES_OPTION){
                // return;
                // }
                // client.removeResource(workflowQName);
                // }
                String workflowAsString = XMLUtil.xmlElementToString(workflow.toXML());
                String owner = this.engine.getConfiguration().getRegigstryUserName();

                Registry registry = this.connectToRegistry();
                boolean result = registry.saveWorkflow(workflowQName, workflow.getName(), workflow.getDescription(), workflowAsString,
                        owner, registryPublishingWindow.isMakePublic());
                registryPublishingWindow.hide();
                return result;
            } catch (Exception e) {
                this.engine.getErrorWindow().error(e.getMessage(), e);
            }
        }
		return false;
    }

    /**
     * 
     * @param workflowTemplateId
     * @throws RepositoryException
     */
    public void deleteOGCEWorkflow(QName workflowTemplateId) throws RepositoryException {
        if (XBayaUtil.acquireJCRRegistry(this.engine)) {
            Registry registry = connectToRegistry();
            registry.deleteWorkflow(workflowTemplateId, this.engine.getConfiguration().getRegigstryUserName());
        }
    }

    /**
     * 
     * @param qname
     * @return
     */
    public Workflow getWorkflow(QName qname) {
        Registry registry = connectToRegistry();
        Node node = registry.getWorkflow(qname, this.engine.getConfiguration().getRegigstryUserName());
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
     */
    public Workflow getWorkflow(String name) {
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