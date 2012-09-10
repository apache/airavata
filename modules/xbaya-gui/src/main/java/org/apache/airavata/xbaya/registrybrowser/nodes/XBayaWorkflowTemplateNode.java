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
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.exception.ServiceDescriptionRetrieveException;
import org.apache.airavata.workflow.model.wf.Workflow;
import org.apache.airavata.xbaya.model.registrybrowser.XBayaWorkflowTemplate;
import org.apache.airavata.xbaya.registry.RegistryAccesser;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.DeleteAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.ImportAction;
import org.apache.airavata.xbaya.ui.graph.GraphCanvas;

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
        return JCRBrowserIcons.WORKFLOW_TEMPLATE_ICON;
    }

    public XBayaWorkflowTemplate getXbayaWorkflow() {
        return xbayaWorkflow;
    }

    public void setXbayaWorkflow(XBayaWorkflowTemplate xbayaWorkflow) {
        this.xbayaWorkflow = xbayaWorkflow;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(ImportAction.ID,DeleteAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
            deleteHostDescription(tree);
            return true;
        } else if (action.equals(ImportAction.ID)) {
        	Workflow workflow = new RegistryAccesser(getXBayaEngine()).getWorkflow(getXbayaWorkflow().getWorkflowName());
            GraphCanvas newGraphCanvas = getXBayaEngine().getGUI().newGraphCanvas(true);
            newGraphCanvas.setWorkflow(workflow);
            getXBayaEngine().getGUI().getGraphCanvas().setWorkflowFile(null);
            return true;
        }
        return super.triggerAction(tree, action);
    }

    private void deleteHostDescription(JTree tree) throws PathNotFoundException, ServiceDescriptionRetrieveException {
        if (askQuestion("XBaya Workflow", "Are you sure that you want to remove the workflow \""
                + getXbayaWorkflow().getWorkflowName() + "\"?")) {
            try {
				getRegistry().removeWorkflow(getXbayaWorkflow().getWorkflowName());
				((AbstractAiravataTreeNode) getParent()).refresh();
				reloadTreeNode(tree, getParent());
			} catch (RegistryException e) {
				e.printStackTrace();
			}
        }
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        if (action.getID().equals(DeleteAction.ID)) {
            return "Remove";
        } else if (action.getID().equals(ImportAction.ID)) {
            return "Import";
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
    
    @Override
    public String getDefaultAction() {
    	return ImportAction.ID;
    }
}
