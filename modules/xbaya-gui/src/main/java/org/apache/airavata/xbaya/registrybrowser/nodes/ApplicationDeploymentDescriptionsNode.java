package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.xbaya.appwrapper.ApplicationDescriptionDialog;
import org.apache.airavata.xbaya.registrybrowser.menu.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.registrybrowser.menu.AddAction;
import org.apache.airavata.xbaya.registrybrowser.menu.DeleteAction;
import org.apache.airavata.xbaya.registrybrowser.menu.RefreshAction;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptionWrap;
import org.apache.airavata.xbaya.registrybrowser.model.ApplicationDeploymentDescriptions;

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
        return "Applications";
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return SwingUtil.createImageIcon("applications.png");
    }

    public ApplicationDeploymentDescriptions getApplicationDeploymentDescriptions() {
        return applicationDeploymentDescriptions;
    }

    public void setApplicationDeploymentDescriptions(ApplicationDeploymentDescriptions applicationDeploymentDescriptions) {
        this.applicationDeploymentDescriptions = applicationDeploymentDescriptions;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(AddAction.ID, RefreshAction.ID, DeleteAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
            deleteApplicationDescription(tree);
            return true;
        } else if (action.equals(AddAction.ID)) {
            ApplicationDescriptionDialog applicationDescriptionDialog = new ApplicationDescriptionDialog(getRegistry());
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
            Registry registry = getRegistry();
            List<ApplicationDeploymentDescriptionWrap> descriptions = getApplicationDeploymentDescriptions()
                    .getDescriptions();
            for (ApplicationDeploymentDescriptionWrap descriptionWrap : descriptions) {
                registry.deleteDeploymentDescription(descriptionWrap.getService(), descriptionWrap.getHost(),
                        descriptionWrap.getDescription().getId());
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
