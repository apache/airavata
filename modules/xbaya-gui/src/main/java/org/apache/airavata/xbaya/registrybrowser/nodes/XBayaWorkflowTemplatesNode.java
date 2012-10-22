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

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.xbaya.model.registrybrowser.XBayaWorkflowTemplates;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.AddAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.RefreshAction;

public class XBayaWorkflowTemplatesNode extends AbstractAiravataTreeNode {
    private XBayaWorkflowTemplates xbayaWorkflows;

    public XBayaWorkflowTemplatesNode(XBayaWorkflowTemplates xbayaWorkflows, TreeNode parent) {
        super(parent);
        setXbayaWorkflows(xbayaWorkflows);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return getTreeNodeList(getXbayaWorkflows().getWorkflows().toArray(), this);
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return "Workflow Templates";
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.WORKFLOW_TEMPLATES_ICON;
    }

    public XBayaWorkflowTemplates getXbayaWorkflows() {
        return xbayaWorkflows;
    }

    public void setXbayaWorkflows(XBayaWorkflowTemplates xbayaWorkflows) {
        this.xbayaWorkflows = xbayaWorkflows;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(RefreshAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(AddAction.ID)) {
            JOptionPane.showMessageDialog(null, "TODO");
            // TODO
            return true;
        }
        return super.triggerAction(tree, action);
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        if (action.getID().equals(AddAction.ID)) {
            return "New workflow...";
        }
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
}
