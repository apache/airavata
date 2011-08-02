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

package org.apache.airavata.xbaya.gpel.component;

import java.net.URI;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.ComponentException;
import org.apache.airavata.xbaya.component.gui.ComponentTreeNode;
import org.apache.airavata.xbaya.component.registry.ComponentRegistry;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryException;
import org.apache.airavata.xbaya.component.ws.WorkflowComponent;
import org.apache.airavata.xbaya.graph.GraphException;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.myproxy.MyProxyClient;
import org.apache.airavata.xbaya.myproxy.gui.MyProxyChecker;
import org.apache.airavata.xbaya.security.UserX509Credential;
import org.apache.airavata.xbaya.security.XBayaSecurity;
import org.apache.airavata.xbaya.wf.Workflow;
import org.apache.airavata.xbaya.workflow.WorkflowClient;
import org.apache.airavata.xbaya.workflow.WorkflowClient.WorkflowType;
import org.apache.airavata.xbaya.workflow.WorkflowEngineException;
import org.gpel.client.GcSearchList;
import org.gpel.client.GcSearchResult;
import org.ietf.jgss.GSSCredential;

public class GPELRegistry extends ComponentRegistry {

    // private static final MLogger logger = MLogger.getLogger();

    private WorkflowClient workflowClient;

    private WorkflowType type;

    private int max;
    private XBayaEngine xbayaEngine;

    /**
     * Constructs a GPELRegistry.
     * 
     * @param engine
     * @param type
     * @param max
     */
    public GPELRegistry(XBayaEngine engine, WorkflowClient.WorkflowType type, int max) {
        this.xbayaEngine = engine;
        this.workflowClient = engine.getWorkflowClient();
        this.type = type;
        this.max = max;
    }

    /**
     * @see org.apache.airavata.xbaya.component.registry.ComponentRegistry#getComponentTree()
     */
    @Override
    public ComponentTreeNode getComponentTree() throws ComponentRegistryException {
        try {
            ComponentTreeNode tree = new ComponentTreeNode(this);

            if (workflowClient.isSecure()) {
                // Check if the proxy is loaded.
                boolean loaded = new MyProxyChecker(this.xbayaEngine).loadIfNecessary();
                if (!loaded) {
                    return null;
                }
                // Creates a secure channel in gpel.
                MyProxyClient myProxyClient = this.xbayaEngine.getMyProxyClient();
                GSSCredential proxy = myProxyClient.getProxy();
                UserX509Credential credential = new UserX509Credential(proxy, XBayaSecurity.getTrustedCertificates());
                try {
                    workflowClient.setUserX509Credential(credential);
                } catch (WorkflowEngineException e) {
                    this.xbayaEngine.getErrorWindow().error(ErrorMessages.GPEL_ERROR, e);
                    return null;
                }
            }

            GcSearchList resultList = this.workflowClient.list(this.max, this.type);
            for (GcSearchResult result : resultList.results()) {
                GPELComponentReference componentRef = new GPELComponentReference(this, result);
                ComponentTreeNode treeLeaf = new ComponentTreeNode(componentRef);
                tree.add(treeLeaf);

            }
            return tree;
        } catch (WorkflowEngineException e) {
            throw new ComponentRegistryException(e);
        } catch (RuntimeException e) {
            throw new ComponentRegistryException(e);
        }
    }

    /**
     * @see org.apache.airavata.xbaya.component.registry.ComponentRegistry#getName()
     */
    @Override
    public String getName() {
        return this.workflowClient.getEngineURL().toString();
    }

    /**
     * @param workflowID
     * @return The Component
     * @throws ComponentRegistryException
     * @throws ComponentException
     */
    public WorkflowComponent getComponent(URI workflowID) throws ComponentRegistryException, ComponentException {

        try {
            Workflow workflow = this.workflowClient.load(workflowID, this.type);
            return new WorkflowComponent(workflow);
        } catch (WorkflowEngineException e) {
            throw new ComponentRegistryException(e);
        } catch (GraphException e) {
            throw new ComponentException(e);
        } catch (RuntimeException e) {
            throw new ComponentRegistryException(e);
        }
    }
}