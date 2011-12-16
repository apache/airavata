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

package org.apache.airavata.xbaya.appwrapper;

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

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.schemas.gfac.GramApplicationDeploymentType;
import org.apache.airavata.schemas.gfac.JobTypeType;
import org.apache.airavata.schemas.gfac.JobTypeType.Enum;
import org.apache.airavata.schemas.gfac.ProjectAccountType;
import org.apache.airavata.schemas.gfac.QueueType;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaComboBox;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;

public class ApplicationDescriptionHostAdvancedOptionDialog extends JDialog {
    private static final long serialVersionUID = 3920479739097405014L;
    private XBayaComboBox cmbJobType;
    private XBayaTextField txtProjectAccountNumber;
    private XBayaTextField txtProjectAccountDescription;
    private XBayaTextField txtQueueType;
    private XBayaTextField txtQueueName;
    private XBayaTextField txtRslParameterName;
    private XBayaTextField txtRslParameterValue;
    private JButton okButton;
    private Registry registry;
    private ApplicationDeploymentDescription descriptor;

    /**
     * Create the dialog.
     */
    public ApplicationDescriptionHostAdvancedOptionDialog(Registry registry, ApplicationDeploymentDescription descriptor) {
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
        setTitle("Host Description Advance Options");
        setModal(true);
        setBounds(100, 100, 400, 250);
        setLocationRelativeTo(null);
        GridPanel buttonPane = new GridPanel();
        okButton = new JButton("Update");
        okButton.setActionCommand("OK");
        okButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveApplicationDescriptionAdvancedOptions();
                close();
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
		
		txtQueueName = new XBayaTextField();
		
		txtQueueType = new XBayaTextField();
		
		txtRslParameterName = new XBayaTextField();
		txtRslParameterValue = new XBayaTextField();
		
		cmbJobType = new XBayaComboBox(new DefaultComboBoxModel(getJobTypesAsStrings()));
		cmbJobType.setEditable(false);
		
		XBayaLabel lbljobType = new XBayaLabel("Job Type",cmbJobType);
		XBayaLabel lblProjectAccountNumber = new XBayaLabel("Project Account Number",txtProjectAccountNumber);
		XBayaLabel lblProjectAccountDescription = new XBayaLabel("Project Account Description",txtProjectAccountDescription);
		XBayaLabel lblQueueType = new XBayaLabel("Queue Type",txtQueueType);
		XBayaLabel lblQueueName = new XBayaLabel("Queue Name",txtQueueName);
		XBayaLabel lblRSLParameterName = new XBayaLabel("RSL Parameter Name",txtRslParameterName);
		XBayaLabel lslRSLParameterValue = new XBayaLabel("RSL Parameter Value",txtRslParameterValue);
    	
		panel.add(lbljobType);
		panel.add(cmbJobType);
		panel.add(lblProjectAccountNumber);
		panel.add(txtProjectAccountNumber);
		panel.add(lblProjectAccountDescription);
		panel.add(txtProjectAccountDescription);
		panel.add(lblQueueType);
		panel.add(txtQueueType);
		panel.add(lblQueueName);
		panel.add(txtQueueName);
//		panel.add(lblRSLParameterName);
//		panel.add(lslRSLParameterValue);
		panel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        buttonPane.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());

        SwingUtil.layoutToGrid(panel.getSwingComponent(), 5, 2, SwingUtil.WEIGHT_NONE, 1);
        
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        
        getContentPane().add(panel.getSwingComponent());
        getContentPane().add(buttonPane.getSwingComponent());
        SwingUtil.layoutToGrid(getContentPane(), 2, 1, 0, 0);
        setResizable(false);
        getRootPane().setDefaultButton(okButton);
    }
    
    private static List<JobTypeType.Enum> jobTypes;
    
	private List<JobTypeType.Enum> getJobTypes() {
		if (jobTypes==null){
			jobTypes = new ArrayList<Enum>();
			jobTypes.add(JobTypeType.OPEN_MP);
			jobTypes.add(JobTypeType.MPI);
			jobTypes.add(JobTypeType.SERIAL);
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
	
    public ApplicationDeploymentDescription getApplicationDescription() {
        return descriptor;
    }

    public GramApplicationDeploymentType getGramApplicationDescriptionType() {
        return (GramApplicationDeploymentType)descriptor.getType();
    }
    
    public void setShellApplicationDescription(ApplicationDeploymentDescription shellApplicationDescription) {
        this.descriptor = shellApplicationDescription;
    }

    private void saveApplicationDescriptionAdvancedOptions() {
		getGramApplicationDescriptionType().setJobType(getJobTypeEnum(cmbJobType.getText()));
		ProjectAccountType projectAccount = getProjectAccountType();
		projectAccount.setProjectAccountNumber(txtProjectAccountNumber.getText());
		projectAccount.setProjectAccountDescription(txtProjectAccountDescription.getText());
		
		projectAccount.setProjectAccountNumber(txtProjectAccountNumber.getText());
		projectAccount.setProjectAccountNumber(txtProjectAccountNumber.getText());
		
		QueueType queueName = getQueueName();
		queueName.setQueueName(txtQueueType.getText());
		queueName.setQueueDescription(txtQueueName.getText());
    }

	private QueueType getQueueName() {
		if (getGramApplicationDescriptionType().getQueue()==null){
			getGramApplicationDescriptionType().addNewQueue();
		}
		return getGramApplicationDescriptionType().getQueue();
	}

	private ProjectAccountType getProjectAccountType() {
		if (getGramApplicationDescriptionType().getProjectAccount()==null){
			getGramApplicationDescriptionType().addNewProjectAccount();
		}
		return getGramApplicationDescriptionType().getProjectAccount();
	}
    

    private void loadApplicationDescriptionAdvancedOptions() {
    	if (getGramApplicationDescriptionType().getJobType()!=null) {
			cmbJobType.setSelectedItem(getGramApplicationDescriptionType()
					.getJobType().toString());
		}
		ProjectAccountType projectAccount = getProjectAccountType();

		txtProjectAccountNumber.setText(projectAccount.getProjectAccountNumber()==null? "":projectAccount.getProjectAccountNumber());
		txtProjectAccountDescription.setText(projectAccount.getProjectAccountDescription()==null? "":projectAccount.getProjectAccountDescription());
		
//        txtWorkingDir.setText(getGramApplicationDescriptionType().getScratchWorkingDirectory());
//        txtInputDir.setText(getGramApplicationDescriptionType().getInputDataDirectory());
//        txtOutputDir.setText(getGramApplicationDescriptionType().getOutputDataDirectory());
//        txtSTDIN.setText(getGramApplicationDescriptionType().getStandardInput());
//        txtSTDOUT.setText(getGramApplicationDescriptionType().getStandardOutput());
//        txtSTDERR.setText(getGramApplicationDescriptionType().getStandardError());
//        tableModelChanging = true;
//      todo handle other parameters previous sent in the Entry Now they are defined
//        ShellApplicationDeploymentType.Env.Entry[] entry = getShellApplicationDescriptionType().getEnv().getEntryArray();
//
//        Map<String, String> env = null;
//        for (int i = 0; i < entry.length; i++) {
//            String key = getShellApplicationDescriptionType().getEnv().getEntryArray(i).getKey();
//            String value = getShellApplicationDescriptionType().getEnv().getEntryArray(i).getValue();
//            env.put(key, value);
//        }

//        while (defaultTableModel.getRowCount() > 0) {
//            defaultTableModel.removeRow(0);
//        }
//        if (env != null) {
//            for (String varName : env.keySet()) {
//                defaultTableModel.addRow(new String[] { varName, env.get(varName) });
//            }
//        }
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
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
    // deploymentDescriptions = getJCRComponentRegistry().getRegistry().searchDeploymentDescription(getServiceName(),
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
