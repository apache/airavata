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
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.xbaya.appwrapper.HostDescriptionDialog;
import org.apache.airavata.xbaya.model.registrybrowser.HostDescriptions;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.AddAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.DeleteAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.RefreshAction;

public class HostDescriptionsNode extends AbstractAiravataTreeNode {
    private HostDescriptions hostDescriptions;

    public HostDescriptionsNode(HostDescriptions hostDescriptions, TreeNode parent) {
        super(parent);
        setHostDescriptions(hostDescriptions);
    }

    @Override
    protected List<TreeNode> getChildren() {
        try {
            return getTreeNodeList(getHostDescriptions().getDescriptions().toArray(), this);
        } catch (RegistryException e) {
            e.printStackTrace();
            return emptyList();
        }
    }

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return "Hosts";
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.HOSTS_ICON;
    }

    public HostDescriptions getHostDescriptions() {
        return hostDescriptions;
    }

    public void setHostDescriptions(HostDescriptions hostDescriptions) {
        this.hostDescriptions = hostDescriptions;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(AddAction.ID, RefreshAction.ID, DeleteAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
            deleteHostDescription(tree);
            return true;
        } else if (action.equals(AddAction.ID)) {
            HostDescriptionDialog hostDescriptionDialog = new HostDescriptionDialog(getXBayaEngine());
            hostDescriptionDialog.open();
            if (hostDescriptionDialog.isHostCreated()) {
                refresh();
                reloadTreeNode(tree, this);
            }
            return true;
        }
        return super.triggerAction(tree, action);
    }

    private void deleteHostDescription(JTree tree) throws Exception {
        if (askQuestion("Host descriptions",
                "Are you sure that you want to remove all host descriptions in this registry?")) {
            AiravataRegistry registry = getRegistry();
            List<HostDescription> descriptions = getHostDescriptions().getDescriptions();
            for (HostDescription descriptionWrap : descriptions) {
                registry.deleteHostDescription(descriptionWrap.getType().getHostName());
            }
            refresh();
            reloadTreeNode(tree, this);
        }
    }

    @Override
    public String getActionCaption(AbstractBrowserActionItem action) {
        if (action.getID().equals(DeleteAction.ID)) {
            return "Remove all hosts";
        } else if (action.getID().equals(AddAction.ID)) {
            return "New host...";
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
