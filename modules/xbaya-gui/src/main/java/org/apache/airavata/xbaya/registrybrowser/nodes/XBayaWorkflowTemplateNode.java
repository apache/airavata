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

import javax.jcr.PathNotFoundException;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.DeleteAction;
import org.apache.airavata.xbaya.registrybrowser.menu.EditAction;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowTemplate;

public class XBayaWorkflowTemplateNode extends AbstractAiravataTreeNode {
    private XBayaWorkflowTemplate xbayaWorkflow;

    public XBayaWorkflowTemplateNode(XBayaWorkflowTemplate xbayaWorkflow, TreeNode parent) {
        super(parent);
        setXbayaWorkflow(xbayaWorkflow);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return emptyList();
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getXbayaWorkflow().getWorkflowName();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return SwingUtil.createImageIcon("workflow.png");
    }

    public XBayaWorkflowTemplate getXbayaWorkflow() {
        return xbayaWorkflow;
    }

    public void setXbayaWorkflow(XBayaWorkflowTemplate xbayaWorkflow) {
        this.xbayaWorkflow = xbayaWorkflow;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(DeleteAction.ID);
    }

    @Override
    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
            deleteHostDescription(tree);
            return true;
        } else if (action.equals(EditAction.ID)) {
            JOptionPane.showMessageDialog(null, "TODO");
            // TODO
            return true;
        }
        return super.triggerAction(tree, action);
    }

    private void deleteHostDescription(JTree tree) throws PathNotFoundException, ServiceDescriptionRetrieveException {
        if (askQuestion("XBaya Workflow", "Are you sure that you want to remove the workflow \""
                + getXbayaWorkflow().getWorkflowName() + "\"?")) {
            getRegistry().deleteWorkflow(new QName(getXbayaWorkflow().getWorkflowName()), getRegistry().getUsername());
            ((AbstractAiravataTreeNode) getParent()).refresh();
            reloadTreeNode(tree, getParent());
        }
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        if (action.getID().equals(DeleteAction.ID)) {
            return "Remove";
        } else if (action.getID().equals(EditAction.ID)) {
            return "Edit";
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
