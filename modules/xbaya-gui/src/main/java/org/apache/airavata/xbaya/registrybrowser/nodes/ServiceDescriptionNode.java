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

import org.apache.airavata.registry.api.exception.RegistryException;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.xbaya.model.registrybrowser.ApplicationDeploymentDescriptions;
import org.apache.airavata.xbaya.model.registrybrowser.InputParameters;
import org.apache.airavata.xbaya.model.registrybrowser.OutputParameters;
import org.apache.airavata.xbaya.ui.actions.AbstractBrowserActionItem;
import org.apache.airavata.xbaya.ui.actions.registry.browser.DeleteAction;
import org.apache.airavata.xbaya.ui.actions.registry.browser.EditAction;
import org.apache.airavata.xbaya.ui.dialogs.descriptors.DeploymentDescriptionDialog;

public class ServiceDescriptionNode extends AbstractAiravataTreeNode {
	private ServiceDescription serviceDescription;

	public ServiceDescriptionNode(ServiceDescription serviceDescription, TreeNode parent) {
		super(parent);
		setServiceDescription(serviceDescription);
	}

	@Override
	protected List<TreeNode> getChildren() {
		List<Object> parameterTypeList=new ArrayList<Object>();
		if (getServiceDescription().getType().getInputParametersArray().length>0){
			parameterTypeList.add(new InputParameters(getServiceDescription().getType().getInputParametersArray()));
		}
		if (getServiceDescription().getType().getOutputParametersArray().length>0){
			parameterTypeList.add(new OutputParameters(getServiceDescription().getType().getOutputParametersArray()));
		}
		parameterTypeList.add(new ApplicationDeploymentDescriptions(getRegistry(),getServiceDescription().getType().getName()));
		return getTreeNodeList(parameterTypeList.toArray(), this);
	}

    @Override
    public String getCaption(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return getServiceDescription().getType().getName();
    }

    @Override
    public Icon getIcon(boolean selected, boolean expanded, boolean leaf, boolean hasFocus) {
        return JCRBrowserIcons.SERVICE_ICON;
    }

    public ServiceDescription getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(ServiceDescription serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    @Override
    public List<String> getSupportedActions() {
        return Arrays.asList(EditAction.ID, DeleteAction.ID);
    }

    public boolean triggerAction(JTree tree, String action) throws Exception {
        if (action.equals(DeleteAction.ID)) {
        	return deleteServiceDescription(tree);
        } else if (action.equals(EditAction.ID)) {
        	return editServiceDescription(tree);
        }
        return super.triggerAction(tree, action);
    }

	private boolean editServiceDescription(JTree tree) {
		DeploymentDescriptionDialog serviceDescriptionDialog = new DeploymentDescriptionDialog(getRegistry(),false,getServiceDescription(), null);
    	serviceDescriptionDialog.open();
//		ServiceDescriptionDialog serviceDescriptionDialog = new ServiceDescriptionDialog(getRegistry(),false,getServiceDescription());
//		serviceDescriptionDialog.open();
		if (serviceDescriptionDialog.isServiceCreated()) {
		    refresh();
		    reloadTreeNode(tree, this);
		}
		return true;
	}

    private boolean deleteServiceDescription(JTree tree) throws RegistryException {
        if (askQuestion("Application", "Are you sure that you want to remove the applications associated with \""
                + getServiceDescription().getType().getName() + "\"?")) {
            getRegistry().removeServiceDescriptor(getServiceDescription().getType().getName());
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

	@Override
	public String getDefaultAction() {
		return EditAction.ID;
	}
}
