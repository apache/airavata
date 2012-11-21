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

import org.apache.airavata.client.api.AiravataAPIInvocationException;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.DeleteAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.EditAction;
import org.apache.airavata.xbaya.ui.dialogs.descriptors.HostDescriptionDialog;

public class HostDescriptionNode extends AbstractAiravataTreeNode {
    private HostDescription hostDescription;

    public HostDescriptionNode(HostDescription hostDescription, TreeNode parent) {
        super(parent);
        setHostDescription(hostDescription);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return emptyList();
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getHostDescription().getType().getHostName();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.HOST_ICON;
    }

    public HostDescription getHostDescription() {
        return hostDescription;
    }

    public void setHostDescription(HostDescription hostDescription) {
        this.hostDescription = hostDescription;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(EditAction.ID, DeleteAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
        	return deleteHostDescription(tree);
        } else if (action.equals(EditAction.ID)) {
            return editHostDescription(tree);
        }
        return super.triggerAction(tree, action);
    }
    
    @Override
    public String getDefaultAction() {
    	return EditAction.ID;
    }
    
	private boolean editHostDescription(JTree tree) {
		HostDescriptionDialog hostDescriptionDialog = new HostDescriptionDialog(getXBayaEngine().getConfiguration().getJcrComponentRegistry().getAiravataAPI(),false,getHostDescription(), null);
		hostDescriptionDialog.open();
		if (hostDescriptionDialog.isHostCreated()) {
		    refresh();
		    reloadTreeNode(tree, this);
		}
		return true;
	}

    private boolean deleteHostDescription(JTree tree) throws AiravataAPIInvocationException {
        if (askQuestion("Host description", "Are you sure that you want to remove the host description \""
                + getHostDescription().getType().getHostName() + "\"?")) {
            getRegistry().getApplicationManager().deleteHostDescription(getHostDescription().getType().getHostName());
            ((AbstractAiravataTreeNode) getParent()).refresh();
            reloadTreeNode(tree, getParent());
        }
        return true;
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        if (action.getID().equals(DeleteAction.ID)) {
            return "Remove";
        } else if (action.getID().equals(EditAction.ID)) {
            return "View/Edit";
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
