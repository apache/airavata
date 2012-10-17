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

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.registry.api.AiravataRegistry2;
import org.apache.airavata.xbaya.model.registrybrowser.ApplicationDeploymentDescriptionWrap;
import org.apache.airavata.xbaya.model.registrybrowser.ApplicationDeploymentDescriptions;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.AddAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.DeleteAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.RefreshAction;
import org.apache.airavata.xbaya.ui.dialogs.descriptors.ApplicationDescriptionDialog;

public class ApplicationDeploymentDescriptionsNode extends AbstractAiravataTreeNode {
    private ApplicationDeploymentDescriptions applicationDeploymentDescriptions;

    public ApplicationDeploymentDescriptionsNode(ApplicationDeploymentDescriptions applicationDeploymentDescriptions,
            TreeNode parent) {
        super(parent);
        setApplicationDeploymentDescriptions(applicationDeploymentDescriptions);
    }

    @Override
    protected List<TreeNode> getChildren() {
        try {
            return getTreeNodeList(getApplicationDeploymentDescriptions().getDescriptions().toArray(), this);
        } catch (RegistryException e) {
            e.printStackTrace();
            return emptyList();
        }
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return "Deployments";
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.APPLICATIONS_ICON;
    }

    public ApplicationDeploymentDescriptions getApplicationDeploymentDescriptions() {
        return applicationDeploymentDescriptions;
    }

    public void setApplicationDeploymentDescriptions(ApplicationDeploymentDescriptions applicationDeploymentDescriptions) {
        this.applicationDeploymentDescriptions = applicationDeploymentDescriptions;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(RefreshAction.ID, DeleteAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
            deleteApplicationDescription(tree);
            return true;
        } else if (action.equals(AddAction.ID)) {
            ApplicationDescriptionDialog applicationDescriptionDialog = new ApplicationDescriptionDialog(getXBayaEngine());
            applicationDescriptionDialog.open();
            if (applicationDescriptionDialog.isApplicationDescCreated()) {
                refresh();
                reloadTreeNode(tree, this);
            }
            return true;
        }
        return super.triggerAction(tree, action);
    }

    private void deleteApplicationDescription(JTree tree) throws Exception {
        if (askQuestion("Application descriptions",
                "Are you sure that you want to remove all application descriptions in this registry?")) {
            AiravataRegistry2 registry = getRegistry();
            List<ApplicationDeploymentDescriptionWrap> descriptions = getApplicationDeploymentDescriptions()
                    .getDescriptions();
            for (ApplicationDeploymentDescriptionWrap descriptionWrap : descriptions) {
                registry.removeApplicationDescriptor(descriptionWrap.getService(), descriptionWrap.getHost(),
                        descriptionWrap.getDescription().getType().getApplicationName().getStringValue());
            }
            refresh();
            reloadTreeNode(tree, this);
        }
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        if (action.getID().equals(DeleteAction.ID)) {
            return "Remove all applications";
        } else if (action.getID().equals(AddAction.ID)) {
            return "New application...";
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
