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
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.DeleteAction;
import org.apache.airavata.xbaya.registrybrowser.menu.EditAction;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptionWrap;

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
        return SwingUtil.createImageIcon("application.png");
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

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
            deleteApplicationDescription(tree);
            return true;
        } else if (action.equals(EditAction.ID)) {
            JOptionPane.showMessageDialog(null, "TODO");
            return true;
        }
        return super.triggerAction(tree, action);
    }

    private void deleteApplicationDescription(JTree tree) throws RegistryException {
        if (askQuestion("Application description",
                "Are you sure that you want to remove the application description \""
                        + getApplicationDeploymentDescriptionWrap().getDescription().getType().getApplicationName().getStringValue() + "\"?")) {
            getRegistry().deleteDeploymentDescription(getApplicationDeploymentDescriptionWrap().getService(),
                    getApplicationDeploymentDescriptionWrap().getHost(),
                    getApplicationDeploymentDescriptionWrap().getDescription().getType().getApplicationName().getStringValue());
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
