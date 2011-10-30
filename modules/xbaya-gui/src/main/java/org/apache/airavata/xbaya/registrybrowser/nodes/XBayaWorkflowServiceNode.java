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
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceParameters;
import org.apache.airavata.xbaya.registrybrowser.model.XBayaWorkflowService;

public class XBayaWorkflowServiceNode extends AbstractAiravataTreeNode {
    private XBayaWorkflowService xbayaWorkflowService;

    public XBayaWorkflowServiceNode(XBayaWorkflowService xbayaWorkflowService, TreeNode parent) {
        super(parent);
        setXbayaWorkflowService(xbayaWorkflowService);
    }

    @Override
    protected List<TreeNode> getChildren() {
		List<ServiceParameters> parameterTypeList=new ArrayList<ServiceParameters>();
		if (getXbayaWorkflowService().getInputParameters()!=null && getXbayaWorkflowService().getInputParameters().getParameters().size()>0){
			parameterTypeList.add(getXbayaWorkflowService().getInputParameters());
		}
		if (getXbayaWorkflowService().getOutputParameters()!=null && getXbayaWorkflowService().getOutputParameters().getParameters().size()>0){
			parameterTypeList.add(getXbayaWorkflowService().getOutputParameters());
		}
		return getTreeNodeList(parameterTypeList.toArray(), this);
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        String type = "";
//        if (selected) {
//			type = " <font color=\"#D3D3D3\">Service call</font>";
//		}
		return wrapAsHtml(getXbayaWorkflowService().getServiceNodeId(),type);
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return SwingUtil.createImageIcon("workflow.png");
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList();
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        return super.triggerAction(tree, action);
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

	public XBayaWorkflowService getXbayaWorkflowService() {
		return xbayaWorkflowService;
	}

	public void setXbayaWorkflowService(XBayaWorkflowService xbayaWorkflowService) {
		this.xbayaWorkflowService = xbayaWorkflowService;
	}
}
