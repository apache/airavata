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

package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.model.registrybrowser.AiravataConfigurations;
import org.apache.airavata.xbaya.model.registrybrowser.ApplicationDeploymentDescriptions;
import org.apache.airavata.xbaya.model.registrybrowser.GFacURLs;
import org.apache.airavata.xbaya.model.registrybrowser.HostDescriptions;
import org.apache.airavata.xbaya.model.registrybrowser.ServiceDescriptions;
import org.apache.airavata.xbaya.model.registrybrowser.XBayaWorkflowExperiments;
import org.apache.airavata.xbaya.model.registrybrowser.XBayaWorkflowTemplates;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.RefreshAction;

public class RegistryNode extends AbstractAiravataTreeNode {
    private AiravataRegistry registry;
    private XBayaEngine engine;

    public RegistryNode(XBayaEngine engine, TreeNode parent) {
        super(parent);
        setRegistry(engine.getConfiguration().getJcrComponentRegistry().getRegistry());
        this.engine=engine;
    }

    protected List<TreeNode> getChildren() {
        List<Object> children = new ArrayList<Object>();
        AiravataConfigurations airavataConfigurations = new AiravataConfigurations(getRegistry());
        children.add(airavataConfigurations);
        HostDescriptions hostDescriptions = new HostDescriptions(getRegistry());
        children.add(hostDescriptions);
        ServiceDescriptions serviceDescriptions = new ServiceDescriptions(getRegistry());
        children.add(serviceDescriptions);
//        ApplicationDeploymentDescriptions applicationDeploymentDescriptions = new ApplicationDeploymentDescriptions(
//                getRegistry());
//        children.add(applicationDeploymentDescriptions);
        XBayaWorkflowTemplates xBayaWorkflows = new XBayaWorkflowTemplates(getRegistry());
        children.add(xBayaWorkflows);
        XBayaWorkflowExperiments xBayaWorkflowExperiments = new XBayaWorkflowExperiments(getRegistry());
        children.add(xBayaWorkflowExperiments);
        return getTreeNodeList(children.toArray(), this);
    }

    public AiravataRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getRegistry().getName() + " - " + getRegistry().getUsername() + "@"
                + getRegistry().getRepositoryURI().toString();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.REGISTRY_ICON;
    }

    // @Override
    // public void refresh() {
    // List<TreeNode> children = getChildren();
    // for (TreeNode node : children) {
    // if (node instanceof AbstractAiravataTreeNode){
    // ((AbstractAiravataTreeNode)node).refresh();
    // }
    // }
    // }
    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(RefreshAction.ID);
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        return action.getDefaultCaption();
    }

    @Override
    public Icon getActionIcon(AbstractBrowserActionItem action) {
        return null;
    }

    @Override
    public String getActionDescription(AbstractBrowserActionItem action) {
        return null;
    }

    public XBayaEngine getEngine() {
        return engine;
    }

    public void setEngine(XBayaEngine engine) {
        this.engine = engine;
    }
}
