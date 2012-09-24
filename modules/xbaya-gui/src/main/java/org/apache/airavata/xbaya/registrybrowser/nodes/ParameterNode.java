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

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.xbaya.model.registrybrowser.NodeParameter;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.CopyAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.ViewAction;
import org.apache.airavata.xbaya.ui.dialogs.TextWindow;

public class ParameterNode extends AbstractAiravataTreeNode {
	private NodeParameter parameter;
	
	public ParameterNode(NodeParameter parameter, TreeNode parent) {
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
			String parameterValue = getParameter().getValue().toString();
			if (parameterValue.length()>200){
				parameterValue=parameterValue.substring(0, 200)+"...";
			}
			return wrapAsHtml("<b>"+getParameter().getName()+"</b>",": ",""+parameterValue+"");
		}else{
			return getParameter().getName();
		}
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return JCRBrowserIcons.PARAMETER_ICON;
	}

	@Override
	public String getDefaultAction() {
		return ViewAction.ID;
	}
	
    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(ViewAction.ID,CopyAction.ID);
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
    	if (action.getID().equals(ViewAction.ID)) {
    		return "View";
    	} else if (action.getID().equals(CopyAction.ID)) {
            return "Copy to clipboard";
        }
    	return null;
    }
    
	public boolean triggerAction(JTree tree,String action) throws Exception{
		if (action.equals(ViewAction.ID)) {
			TextWindow textWindow = new TextWindow(getXBayaEngine(), getParameter().getName(), getParameter().getValue().toString(),"Parameter Content");
			textWindow.show();
		} else if (action.equals(CopyAction.ID)) {
        	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getParameter().getValue().toString()), null);
        }
		return super.triggerAction(tree, action);
	}

	@Override
	public Icon getActionIcon(AbstractBrowserActionItem action) {
		return null;
	}

	@Override
	public String getActionDescription(AbstractBrowserActionItem action) {
		return null;
	}

	public NodeParameter getParameter() {
		return parameter;
	}

	public void setParameter(NodeParameter parameter) {
		this.parameter = parameter;
	}
}
