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

package org.apache.airavata.xbaya.ui.dialogs.descriptors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.airavata.client.api.AiravataAPI;
import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.ApplicationDescription;
import org.apache.airavata.schemas.gfac.HpcApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.JobTypeType.Enum;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.QueueType;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaComboBox;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
//import org.apache.airavata.registry.api.AiravataRegistry2;

public class ApplicationDescriptionHostAdvancedOptionDialog extends JDialog {
    private static final long serialVersionUID = 3920479739097405014L;
    private XBayaComboBox cmbJobType;
    private XBayaTextField txtProjectAccountNumber;
    private XBayaTextField txtProjectAccountDescription;
    private XBayaTextField txtQueueType;
    private XBayaTextField txtMaxWallTime = new XBayaTextField();
    private XBayaTextField txtMinMemory = new XBayaTextField();
    private XBayaTextField txtMaxMemory = new XBayaTextField();
    private XBayaTextField txtCpuCount = new XBayaTextField();
    private XBayaTextField txtNodeCount = new XBayaTextField();
    private XBayaTextField txtProcessorsPerNode = new XBayaTextField();
    private JButton okButton;
    private AiravataAPI registry;
    private ApplicationDescription descriptor;
	private XBayaLabel lblCpuCount;
	private XBayaLabel lblProcessorPerNode;
    private XBayaLabel lbNodeCount;
	private XBayaTextField txtjobSubmitterCommand;
	private XBayaTextField txtinstalledParentPath;

    /**
     * Create the dialog.
     */
    public ApplicationDescriptionHostAdvancedOptionDialog(AiravataAPI registry, ApplicationDescription descriptor) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent arg0) {
                loadApplicationDescriptionAdvancedOptions();
            }
        });
        setRegistry(registry);
        setShellApplicationDescription(descriptor);
        initGUI();
    }

    public void open() {
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    protected ApplicationDescriptionHostAdvancedOptionDialog getDialog() {
        return this;
    }

    public void close() {
        getDialog().setVisible(false);
    }

    private void initGUI() {
        setTitle("HPC Configuration Options");
        setModal(true);
        setBounds(100, 100, 500, 500);
        setLocationRelativeTo(null);
        GridPanel buttonPane = new GridPanel();
        okButton = new JButton("Update");
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (saveApplicationDescriptionAdvancedOptions()){
                	close();
                }
            }
        });
        getRootPane().setDefaultButton(okButton);
    
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setActionCommand("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });
            
        
    	GridPanel panel = new GridPanel();
		txtProjectAccountNumber = new XBayaTextField();
		txtProjectAccountDescription = new XBayaTextField();

        txtQueueType = new XBayaTextField();
        txtMaxWallTime = new XBayaTextField();
        txtMinMemory = new XBayaTextField();
        txtMaxMemory = new XBayaTextField();
        txtCpuCount = new XBayaTextField();
        txtProcessorsPerNode = new XBayaTextField();
        txtNodeCount = new XBayaTextField();
        txtjobSubmitterCommand = new XBayaTextField();
        txtinstalledParentPath = new XBayaTextField();
        

        DefaultComboBoxModel cmbModelJobType = new DefaultComboBoxModel(getJobTypesAsStrings());
		cmbJobType = new XBayaComboBox(cmbModelJobType);
        cmbJobType.setEditable(false);
        //FIXME:: Machines like trestles are mandating to have cpu and node types set. So better to have these enabled.
//        cmbJobType.getSwingComponent().addActionListener(new ActionListener(){
//			@Override
//			public void actionPerformed(ActionEvent arg0) {
//				boolean disabled=cmbJobType.getText().equalsIgnoreCase(JobTypeType.SERIAL.toString()) || cmbJobType.getText().equalsIgnoreCase(JobTypeType.SINGLE.toString());
//				txtCpuCount.setEnabled(!disabled);
//				txtProcessorsPerNode.setEnabled(!disabled);
//				lblCpuCount.getSwingComponent().setEnabled(!disabled);
//				lblProcessorPerNode.getSwingComponent().setEnabled(!disabled);
//			}
//        });
        
		XBayaLabel lbljobType = new XBayaLabel("Job Type",cmbJobType);
		XBayaLabel lblProjectAccountNumber = new XBayaLabel("Project Account Number *",txtProjectAccountNumber);
		XBayaLabel lblProjectAccountDescription = new XBayaLabel("Project Account Description",txtProjectAccountDescription);
        XBayaLabel lblQueueType = new XBayaLabel("Queue Type *",txtQueueType);
		XBayaLabel lblMaxWallTime = new XBayaLabel("Max Wall Time",txtMaxWallTime);
		lblCpuCount = new XBayaLabel("CPU Count",txtCpuCount);
		lblProcessorPerNode = new XBayaLabel("Processor Per Node", txtProcessorsPerNode);
		XBayaLabel lblMinMemory = new XBayaLabel("Min Memory",txtMinMemory);
		XBayaLabel lblMaxMemory = new XBayaLabel("Max Memory",txtMaxMemory);
        lbNodeCount = new XBayaLabel("Node Count", txtNodeCount);
        XBayaLabel lbljobSubmitterCommand = new XBayaLabel("Job Submitter Command",txtjobSubmitterCommand);
        XBayaLabel lblinstalledParentPath = new XBayaLabel("Installed Parent Path",txtinstalledParentPath);
        
		panel.add(lbljobType);
		panel.add(cmbJobType);
		panel.add(lblProjectAccountNumber);
		panel.add(txtProjectAccountNumber);
		panel.add(lblProjectAccountDescription);
		panel.add(txtProjectAccountDescription);
		panel.add(lblQueueType);
		panel.add(txtQueueType);
        panel.add(lblMaxWallTime);
		panel.add(txtMaxWallTime);
        panel.add(lblCpuCount);
		panel.add(txtCpuCount);
        panel.add(lbNodeCount);
        panel.add(txtNodeCount);
        panel.add(lblProcessorPerNode);
		panel.add(txtProcessorsPerNode);
        panel.add(lblMinMemory);
		panel.add(txtMinMemory);
        panel.add(lblMaxMemory);
        panel.add(txtMaxMemory);
        panel.add(lbljobSubmitterCommand);
        panel.add(txtjobSubmitterCommand);
        panel.add(lblinstalledParentPath);
        panel.add(txtinstalledParentPath);
		panel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        buttonPane.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());

        SwingUtil.layoutToGrid(panel.getSwingComponent(), 12, 2, SwingUtil.WEIGHT_NONE, 1);
        
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        
        getContentPane().add(panel.getSwingComponent());
        getContentPane().add(buttonPane.getSwingComponent());
        SwingUtil.layoutToGrid(getContentPane(), 2, 1, 0, 0);
        setResizable(true);
        getRootPane().setDefaultButton(okButton);
    }
    
    private static List<JobTypeType.Enum> jobTypes;
    
	private List<JobTypeType.Enum> getJobTypes() {
		if (jobTypes==null){
			jobTypes = new ArrayList<Enum>();
			jobTypes.add(JobTypeType.OPEN_MP);
			jobTypes.add(JobTypeType.MPI);
			jobTypes.add(JobTypeType.SERIAL);
//            jobTypes.add(JobTypeType.SINGLE);
		}
		return jobTypes;
	}

	private String[] getJobTypesAsStrings() {
		List<String> typeList=new ArrayList<String>();
		for (Enum jtype : getJobTypes()) {
			typeList.add(jtype.toString());
		}
		return typeList.toArray(new String[]{});
	}

	private Enum getJobTypeEnum(String jobTypeString){
		for (Enum jtype : getJobTypes()) {
			if (jtype.toString().equalsIgnoreCase(jobTypeString)){
				return jtype;
			}
		}
		return null;
	}
	
    public ApplicationDescription getApplicationDescription() {
        return descriptor;
    }

    public HpcApplicationDeploymentType getHPCApplicationDescriptionType() {
        return (HpcApplicationDeploymentType)descriptor.getType();
    }
    
    public void setShellApplicationDescription(ApplicationDescription shellApplicationDescription) {
        this.descriptor = shellApplicationDescription;
    }

    private boolean isValueNotEmpty(String s){
    	return !s.trim().isEmpty();
    }
    
    private void showError(String message, String title){
    	JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }
    private boolean saveApplicationDescriptionAdvancedOptions() {
		if (isValueNotEmpty(cmbJobType.getText())) {
		    getHPCApplicationDescriptionType().setJobType(
					getJobTypeEnum(cmbJobType.getText()));
		}
		try {
			if (isValueNotEmpty(txtMaxWallTime.getText())) {
			    getHPCApplicationDescriptionType().setMaxWallTime(
						Integer.parseInt(txtMaxWallTime.getText()));
			}
		} catch (NumberFormatException e) {
			showError("Max wall time must be a number", "Invalid value");
			return false;
		}
		try {
			if (isValueNotEmpty(txtCpuCount.getText())) {
			    getHPCApplicationDescriptionType().setCpuCount(
						Integer.parseInt(txtCpuCount.getText()));
			}
		} catch (NumberFormatException e) {
			showError("CPU count must be a number", "Invalid value");
			return false;
		}
		try {
			if (isValueNotEmpty(txtProcessorsPerNode.getText())) {
			    getHPCApplicationDescriptionType().setProcessorsPerNode(
						Integer.parseInt(txtProcessorsPerNode.getText()));
			}
		} catch (NumberFormatException e) {
			showError("Processors per node must be a number", "Invalid value");
			return false;
		}
        try {
			if (isValueNotEmpty(txtNodeCount.getText())) {
			    getHPCApplicationDescriptionType().setNodeCount(
						Integer.parseInt(txtNodeCount.getText()));
			}
		} catch (NumberFormatException e) {
			showError("Node count must be a number", "Invalid value");
			return false;
		}
		try {
			if (isValueNotEmpty(txtMinMemory.getText())) {
			    getHPCApplicationDescriptionType().setMinMemory(
						Integer.parseInt(txtMinMemory.getText()));
			}
		} catch (NumberFormatException e) {
			showError("Minimum memory must be a number", "Invalid value");
			return false;
		}
	    try {
			if (isValueNotEmpty(txtMaxMemory.getText())) {
			    getHPCApplicationDescriptionType().setMaxMemory(
		                Integer.parseInt(txtMaxMemory.getText()));
		    }
		} catch (NumberFormatException e) {
			showError("Maximum memory must be a number", "Invalid value");
			return false;
		}
	    try {
			if (isValueNotEmpty(txtjobSubmitterCommand.getText())) {
			    getHPCApplicationDescriptionType().setJobSubmitterCommand(
		                txtjobSubmitterCommand.getText());
		    }else{
		    	getHPCApplicationDescriptionType().setJobSubmitterCommand(null);
		    }
		} catch (NumberFormatException e) {
			showError("Maximum memory must be a number", "Invalid value");
			return false;
		}
	    try {
			if (isValueNotEmpty(txtinstalledParentPath.getText())) {
			    getHPCApplicationDescriptionType().setInstalledParentPath(
			    		txtinstalledParentPath.getText());
		    }else{
		    	getHPCApplicationDescriptionType().setInstalledParentPath(null);
		    }
		} catch (NumberFormatException e) {
			showError("Maximum memory must be a number", "Invalid value");
			return false;
		}	    
		ProjectAccountType projectAccount = getProjectAccountType();
		if (isValueNotEmpty(txtProjectAccountNumber.getText())) {
			projectAccount.setProjectAccountNumber(txtProjectAccountNumber
					.getText());
		}
		if (isValueNotEmpty(txtProjectAccountDescription.getText())) {
			projectAccount
					.setProjectAccountDescription(txtProjectAccountDescription
							.getText());
		}
		if (isValueNotEmpty(txtQueueType.getText())) {
			QueueType queueName = getQueueName();
			queueName.setQueueName(txtQueueType.getText());
		}
		return true;
    }

	private QueueType getQueueName() {
		if (getHPCApplicationDescriptionType().getQueue()==null){
		    getHPCApplicationDescriptionType().addNewQueue();
		}
		return getHPCApplicationDescriptionType().getQueue();
	}

	private ProjectAccountType getProjectAccountType() {
		if (getHPCApplicationDescriptionType().getProjectAccount()==null){
		    getHPCApplicationDescriptionType().addNewProjectAccount();
		}
		return getHPCApplicationDescriptionType().getProjectAccount();
	}
    
	private String getPropValue(int num){
		if (num==0){
			return "";
		}else{
			return String.valueOf(num);
		}
	}
	
    private void loadApplicationDescriptionAdvancedOptions() {
        HpcApplicationDeploymentType hpcAppType = getHPCApplicationDescriptionType();
		if (hpcAppType.getJobType()!=null) {
			cmbJobType.setSelectedItem(hpcAppType
					.getJobType().toString());
		}
    	txtMaxWallTime.setText(getPropValue(hpcAppType.getMaxWallTime()));
        txtCpuCount.setText(getPropValue(hpcAppType.getCpuCount()));
        txtNodeCount.setText(getPropValue(hpcAppType.getNodeCount()));
        txtProcessorsPerNode.setText(getPropValue(hpcAppType.getProcessorsPerNode()));
        txtMinMemory.setText(getPropValue(hpcAppType.getMinMemory()));
        txtMaxMemory.setText(getPropValue(hpcAppType.getMaxMemory()));
        txtNodeCount.setText(getPropValue(hpcAppType.getNodeCount()));
        txtjobSubmitterCommand.setText(hpcAppType.getJobSubmitterCommand()==null?"":hpcAppType.getJobSubmitterCommand());
        txtinstalledParentPath.setText(hpcAppType.getInstalledParentPath()==null?"":hpcAppType.getInstalledParentPath());
		ProjectAccountType projectAccount = getProjectAccountType();

		txtProjectAccountNumber.setText(projectAccount.getProjectAccountNumber()==null? "":projectAccount.getProjectAccountNumber());
		txtProjectAccountDescription.setText(projectAccount.getProjectAccountDescription()==null? "":projectAccount.getProjectAccountDescription());

		QueueType queueName = getQueueName();
		txtQueueType.setText(queueName.getQueueName()==null?"":queueName.getQueueName());
    }

    public AiravataAPI getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataAPI registry) {
        this.registry = registry;
    }

    // private void updateDialogStatus(){
    // String message=null;
    // try {
    // validateDialog();
    // } catch (Exception e) {
    // message=e.getLocalizedMessage();
    // }
    // okButton.setEnabled(message==null);
    // setError(message);
    // }
    //
    // private void validateDialog() throws Exception{
    // if (getApplicationName()==null || getApplicationName().trim().equals("")){
    // throw new Exception("Name of the application cannot be empty!!!");
    // }
    //
    // List<ApplicationDeploymentDescription> deploymentDescriptions=null;
    // try {
    // deploymentDescriptions = getJCRComponentRegistry().getRegistry().searchApplicationDescription(getServiceName(),
    // getHostName(), Pattern.quote(getApplicationName()));
    // } catch (PathNotFoundException e) {
    // //what we want
    // } catch (Exception e){
    // throw e;
    // }
    // if (deploymentDescriptions.size()>0){
    // throw new Exception("Application descriptor with the given name already exists!!!");
    // }
    //
    // if (getExecutablePath()==null || getExecutablePath().trim().equals("")){
    // throw new Exception("Executable path cannot be empty!!!");
    // }
    //
    // if (getTempDir()==null || getTempDir().trim().equals("")){
    // throw new Exception("Temporary directory location cannot be empty!!!");
    // }
    //
    // if (getServiceName()==null || getServiceName().trim().equals("")){
    // throw new Exception("Please select/create service to bind to this deployment description");
    // }
    //
    // if (getHostName()==null || getHostName().trim().equals("")){
    // throw new Exception("Please select/create host to bind to this deployment description");
    // }
    //
    // }

}
