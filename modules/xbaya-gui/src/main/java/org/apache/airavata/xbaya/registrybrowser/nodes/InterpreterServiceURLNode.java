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

import org.apache.airavata.xbaya.model.registrybrowser.InterpreterServiceURL;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.CopyAction;

public class InterpreterServiceURLNode extends AbstractAiravataTreeNode {
    private InterpreterServiceURL interpreterServiceURL;

    public InterpreterServiceURLNode(InterpreterServiceURL interpreterServiceURL, TreeNode parent) {
        super(parent);
        setInterpreterServiceURL(interpreterServiceURL);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return emptyList();
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getInterpreterServiceURL().getInterpreterServiceURL().toString();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.GFAC_URL_ICON;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(CopyAction.ID);
    }

    @Override
    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(CopyAction.ID)) {
        	Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(getInterpreterServiceURL().getInterpreterServiceURL().toString()), null);
        }
        return super.triggerAction(tree, action);
    }
    
    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
    	if (action.getID().equals(CopyAction.ID)) {
            return "Copy to clipboard";
        }
    	return null;
	}

    @Override
    public Icon getActionIcon(AbstractBrowserActionItem action) {
        return null;
    }

    @Override
    public String getActionDescription(AbstractBrowserActionItem action) {
        return null;
    }

	public InterpreterServiceURL getInterpreterServiceURL() {
		return interpreterServiceURL;
	}

	public void setInterpreterServiceURL(InterpreterServiceURL interpreterServiceURL) {
		this.interpreterServiceURL = interpreterServiceURL;
	}
}
