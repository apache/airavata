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

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.xbaya.model.registrybrowser.ApplicationDeploymentDescriptionWrap;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.DeleteAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.EditAction;
import org.apache.airavata.xbaya.ui.dialogs.descriptors.ApplicationDescriptionDialog;

public class ApplicationDeploymentDescriptionNode extends AbstractAiravataTreeNode {
    private ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap;

    public ApplicationDeploymentDescriptionNode(
            ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap, TreeNode parent) {
        super(parent);
        setApplicationDeploymentDescriptionWrap(applicationDeploymentDescriptionWrap);
    }

    @Override
    protected List<TreeNode> getChildren() {
        return emptyList();
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getApplicationDeploymentDescriptionWrap().getDescription().getType().getApplicationName().getStringValue();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.APPLICATION_ICON;
    }

    public ApplicationDeploymentDescriptionWrap getApplicationDeploymentDescriptionWrap() {
        return applicationDeploymentDescriptionWrap;
    }

    public void setApplicationDeploymentDescriptionWrap(
            ApplicationDeploymentDescriptionWrap applicationDeploymentDescriptionWrap) {
        this.applicationDeploymentDescriptionWrap = applicationDeploymentDescriptionWrap;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(EditAction.ID, DeleteAction.ID);
    }

    @Override
    public String getDefaultAction() {
    	return EditAction.ID;
    }
    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
        	return deleteApplicationDescription(tree);
        } else if (action.equals(EditAction.ID)) {
        	return editDescriptor(tree);
        }
        return super.triggerAction(tree, action);
    }

	private boolean editDescriptor(JTree tree) {
		ApplicationDescriptionDialog applicationDescriptionDialog = new ApplicationDescriptionDialog(getXBayaEngine(),false,getApplicationDeploymentDescriptionWrap().getDescription(),getApplicationDeploymentDescriptionWrap().getHost(),getApplicationDeploymentDescriptionWrap().getService());
		applicationDescriptionDialog.open();
		if (applicationDescriptionDialog.isApplicationDescCreated()) {
		    refresh();
		    reloadTreeNode(tree, this);
		}
		return true;
	}

    private boolean deleteApplicationDescription(JTree tree) throws RegistryException {
        if (askQuestion("Application description",
                "Are you sure that you want to remove the application description \""
                        + getApplicationDeploymentDescriptionWrap().getDescription().getType().getApplicationName().getStringValue() + "\"?")) {
            getRegistry().deleteDeploymentDescription(getApplicationDeploymentDescriptionWrap().getService(),
                    getApplicationDeploymentDescriptionWrap().getHost(),
                    getApplicationDeploymentDescriptionWrap().getDescription().getType().getApplicationName().getStringValue());
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
