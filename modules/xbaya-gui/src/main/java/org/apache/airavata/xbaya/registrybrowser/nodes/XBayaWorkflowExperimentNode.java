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

import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.workflow.ExperimentName;
import org.apache.airavata.registry.api.workflow.WorkflowExecutionStatus;
import org.apache.airavata.xbaya.model.registrybrowser.XBayaWorkflowExperiment;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.CopyAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.DeleteAction;

public class XBayaWorkflowExperimentNode extends AbstractAiravataTreeNode {
	private XBayaWorkflowExperiment experiment;
	private String workflowExecutionName;
	
    public XBayaWorkflowExperimentNode(XBayaWorkflowExperiment experiment, TreeNode parent) {
        super(parent);
        setExperiment(experiment);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return getTreeNodeList(getExperiment().getWorkflows().toArray(), this);
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
    	if (workflowExecutionName==null) {
			try {
				workflowExecutionName = getRegistry().getProvenanceManager().getExperimentName(getExperiment().getExperimentId()).getInstanceName();
			} catch (AiravataAPIInvocationException e) {
				e.printStackTrace();
			}
			if (workflowExecutionName==null){
				workflowExecutionName="["+getExperiment().getExperimentId()+"]";
			}
		}
    	String caption=workflowExecutionName;
    	try {
			WorkflowExecutionStatus workflowExecutionStatus = getRegistry().getProvenanceManager().getWorkflowInstanceStatus(getExperiment().getExperimentId(), getExperiment().getExperimentId());
			if (workflowExecutionStatus!=null && workflowExecutionStatus.getExecutionStatus()!=null){
				caption += " - <i>" + workflowExecutionStatus.getExecutionStatus().toString()+"</i>";
				if (workflowExecutionStatus.getStatusUpdateTime()!=null) {
						caption += "<i> as of " + workflowExecutionStatus.getStatusUpdateTime().toString() + "</i>";
				}
			}
		} catch (AiravataAPIInvocationException e) {
			e.printStackTrace();
		}
		return wrapAsHtml(caption);
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.WORKFLOW_EXPERIMENT_ICON;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(CopyAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(CopyAction.ID)) {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getExperimentInfo()), null);
        }
        return super.triggerAction(tree, action);
    }

    private String getExperimentName (){
        String experimentId = getExperiment().getExperimentId();
        try {
            ExperimentName experimentName = getExperiment().getAiravataAPI().getProvenanceManager().getExperimentName(experimentId);
            return experimentName.getInstanceName();
        } catch (AiravataAPIInvocationException e) {
            return null;
        }
    }

    private String getExperimentInfo (){
        String experimetName = getExperimentName();
        String experimetID = getExperiment().getExperimentId();
        return "[Experiment Name = " + experimetName + ", Experiment ID = " + experimetID + "]";
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        if (action.getID().equals(CopyAction.ID)) {
            return "Copy Experiment Info to clipboard";
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

	public XBayaWorkflowExperiment getExperiment() {
		return experiment;
	}

	public void setExperiment(XBayaWorkflowExperiment experiment) {
		this.experiment = experiment;
	}
}
