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
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.model.ServiceParameter;

public class ParameterNode extends AbstractAiravataTreeNode {
	private ServiceParameter parameter;
	
	public ParameterNode(ServiceParameter parameter, TreeNode parent) {
		super(parent);
		setParameter(parameter);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return emptyList();
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		if (getParameter().getValue()!=null){
			return getParameter().getName()+":"+getParameter().getValue().toString();
		}else{
			return getParameter().getName();
		}
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("parameter.png");
	}

	@Override
	public List<String> getSupportedActions() {
		return Arrays.asList();
	}

	public boolean triggerAction(JTree tree,String action) throws Exception{
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

	public ServiceParameter getParameter() {
		return parameter;
	}

	public void setParameter(ServiceParameter parameter) {
		this.parameter = parameter;
	}
}
