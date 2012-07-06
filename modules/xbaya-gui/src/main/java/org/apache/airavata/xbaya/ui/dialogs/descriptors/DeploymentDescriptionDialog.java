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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.PathNotFoundException;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.xml.namespace.QName;

import org.apache.airavata.common.registry.api.exception.RegistryException;
import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.HostDescriptionType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.airavata.xbaya.ui.dialogs.descriptors.HostDeploymentDialog.HostDeployment;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.xmlbeans.XmlCursor;

public class DeploymentDescriptionDialog extends JDialog {

    private static final long serialVersionUID = 2705760838264284423L;
    private final GridPanel contentPanel = new GridPanel();
    private XBayaLabel lblServiceName;
    private XBayaTextField txtApplicationServiceName;
    private JTable tblParameters;
    private boolean serviceCreated = false;
    private JLabel lblError;
    private ServiceDescription serviceDescription;
    private ServiceDescription orginalServiceDescription;
    private JButton okButton;
    private JButton btnDeleteParameter;
    private DefaultTableModel defaultTableModel;
    private AiravataRegistry registry;
    private boolean newDescription;
    private boolean ignoreTableChanges=false;
	private JCheckBox chkForceFileStagingToWorkDir;
	private String suggestedNamePrefix;
	private String titlePrefix;
	private Map<String,HostDeployment> deployments;
	private JTable tblHosts;
	private DefaultTableModel tblModelHosts;
	
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            DeploymentDescriptionDialog dialog = new DeploymentDescriptionDialog(null,true,null);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public DeploymentDescriptionDialog(AiravataRegistry registry) {
    	this(registry,true,null);
    }
    
    /**
     * Create the dialog.
     */
    public DeploymentDescriptionDialog(AiravataRegistry registry, boolean newDescription, ServiceDescription serviceDescription) {
    	setNewDescription(newDescription);
    	this.setOrginalServiceDescription(serviceDescription);
    	setSuggestedNamePrefix(suggestedNamePrefix);
    	if (isNewDescription()) {
			setTitlePrefix("Register Application");
		}else{
			setTitlePrefix("Update Application: "+getOrginalServiceDescription().getType().getName());
		}
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent arg0) {
//                if (isNewDescription()) {
//					String baseName = "Application";
//					int i;
//					String defaultName;
//					i = 1;
//					defaultName = baseName+i;
//					try {
//						while (getRegistry().getServiceDescription(defaultName) != null) {
//							defaultName = baseName + (++i);
//						}
//					} catch (Exception e) {
//					}
//					txtApplicationServiceName.setText(defaultName);
//					setServiceName(txtApplicationServiceName.getText());
//				}
            }
        });
        setRegistry(registry);
        initGUI();

    }

    public void open() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    protected DeploymentDescriptionDialog getDialog() {
        return this;
    }

    private void initGUI() {
    	setTitle(getTitlePrefix());
		setBounds(100, 100, 463, 459);
        setModal(true);
        setLocationRelativeTo(null);
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(5);
        borderLayout.setHgap(5);
        getContentPane().setLayout(borderLayout);

        txtApplicationServiceName = new XBayaTextField();
        txtApplicationServiceName.getSwingComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setServiceName(txtApplicationServiceName.getText());
            }
        });
        txtApplicationServiceName.setColumns(10);
        lblServiceName = new XBayaLabel("Application name",txtApplicationServiceName);
        JLabel lblInputParameters = new JLabel("Application Parameters");
        lblInputParameters.setFont(new Font("Tahoma", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane();
        tblParameters=createParameterTableControls();
        scrollPane.setViewportView(tblParameters);
        
		chkForceFileStagingToWorkDir=new JCheckBox("Advanced: Force input file staging to working directory");
        chkForceFileStagingToWorkDir.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				setForceFileStagingToWorkDir(chkForceFileStagingToWorkDir.isSelected());
			}
        	
        });
        GridPanel buttonPane = new GridPanel();
        {
            GridBagLayout gbl_buttonPane = new GridBagLayout();
            gbl_buttonPane.columnWidths = new int[] { 307, 136, 0 };
            gbl_buttonPane.rowHeights = new int[] { 33, 0 };
            gbl_buttonPane.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
            gbl_buttonPane.rowWeights = new double[] { 0.0, Double.MIN_VALUE };
            

            lblError = new JLabel("");
            lblError.setForeground(Color.RED);
            GridBagConstraints gbc_lblError = new GridBagConstraints();
            gbc_lblError.insets = new Insets(0, 0, 0, 5);
            gbc_lblError.gridx = 0;
            gbc_lblError.gridy = 0;
            buttonPane.add(lblError);
            JPanel panel = new JPanel();
            GridBagConstraints gbc_panel = new GridBagConstraints();
            gbc_panel.anchor = GridBagConstraints.NORTHWEST;
            gbc_panel.gridx = 1;
            gbc_panel.gridy = 0;
            buttonPane.add(panel);
            {
            	JButton resetButton = new JButton("Reset");
                resetButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                    	loadData();
                    }
                });
                panel.add(resetButton);
            }
            {
                okButton = new JButton("Register");
                if (!isNewDescription()){
                	okButton.setText("Update");
                }
                okButton.setEnabled(false);
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        try {
							saveServiceDescription();
							close();
						} catch (RegistryException e1) {
							e1.printStackTrace();
						}
                    }
                });
                panel.add(okButton);
                okButton.setActionCommand("OK");
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        setServiceCreated(false);
                        close();
                    }
                });
                panel.add(cancelButton);
                cancelButton.setActionCommand("Cancel");
            }
        }
        
        contentPanel.add(lblServiceName);
        contentPanel.add(txtApplicationServiceName);
        GridPanel pnlTables=new GridPanel();
        
        GridPanel parameterPanel=new GridPanel();
		parameterPanel.add(lblInputParameters);
        parameterPanel.add(scrollPane);
    	parameterPanel.add(btnDeleteParameter);
    	parameterPanel.add(chkForceFileStagingToWorkDir);
    	
        SwingUtil.layoutToGrid(contentPanel.getSwingComponent(), 1, 2, SwingUtil.WEIGHT_NONE, 1);
    	SwingUtil.layoutToGrid(parameterPanel.getSwingComponent(), 4, 1, 1, 0);
    	
    	pnlTables.add(parameterPanel);
    	pnlTables.add(createHostDeploymentTable());
    	
    	pnlTables.layout(2, 1, SwingUtil.WEIGHT_EQUALLY, 0);
    	
        GridPanel infoPanel = new GridPanel();
		infoPanel.add(contentPanel);
		infoPanel.add(pnlTables);
        infoPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
		infoPanel.layout(2, 1, 1, 0);
		getContentPane().add(infoPanel.getSwingComponent());
        getContentPane().add(buttonPane.getSwingComponent());
        buttonPane.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        SwingUtil.layoutToGrid(getContentPane(), 2, 1, 0, 0);
        setResizable(true);
        getRootPane().setDefaultButton(okButton);
        if (!isNewDescription()){
        	loadData();
        }
    }
    
    private GridPanel createHostDeploymentTable() {
    	tblHosts = new JTable();
    	tblHosts.setTableHeader(null);
        tblHosts.setFillsViewportHeight(true);
        tblModelHosts = new DefaultTableModel(new Object[][] {}, new String[] { "Host"}){
			private static final long serialVersionUID = -5973463590447809117L;
			@Override
            public boolean isCellEditable(int row, int column) {
               return false;
            }
        };
        tblHosts.setModel(tblModelHosts);
       
        ListSelectionModel selectionModel = tblHosts.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        JButton btnNewDeployment = new JButton("New deployment");
        btnNewDeployment.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				HostDeploymentDialog hostDeploymentDialog = new HostDeploymentDialog(getRegistry(),true,null,null,Arrays.asList(getDeployments().keySet().toArray(new String[]{})));
				try {
					HostDeployment deployDesc = hostDeploymentDialog.execute();
					if (deployDesc!=null){
						ApplicationDeploymentDescriptionType appType = deployDesc.getApplicationDescription().getType();
						if (appType.getApplicationName()==null){
							appType.addNewApplicationName();
				    	}
						HostDescriptionType hostType = deployDesc.getHostDescription().getType();
						appType.getApplicationName().setStringValue(hostType.getHostName()+"_application");
						getDeployments().put(hostType.getHostName(), deployDesc);
						updateDeploymentTable();
					}
				} catch (RegistryException e1) {
					setError(e1.getLocalizedMessage());
					e1.printStackTrace();
				}
			}
		});
        
        final JButton btnEditDeployment = new JButton("Edit deployment");
        btnEditDeployment.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String hostName = tblModelHosts.getValueAt(tblHosts.getSelectedRow(),0).toString();
				HostDeploymentDialog hostDeploymentDialog = new HostDeploymentDialog(getRegistry(),false,getDeployments().get(hostName).getApplicationDescription(),hostName,Arrays.asList(getDeployments().keySet().toArray(new String[]{})));
				try {
					HostDeployment deployDesc = hostDeploymentDialog.execute();
					if (deployDesc!=null){
						getDeployments().put(deployDesc.getHostDescription().getType().getHostName(), deployDesc);
						updateDeploymentTable();
					}
				} catch (RegistryException e1) {
					setError(e1.getLocalizedMessage());
					e1.printStackTrace();
				}
			}
		});
        
        final JButton btnDeleteDeployment = new JButton("Delete deployment");
        btnDeleteDeployment.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String hostName = tblModelHosts.getValueAt(tblHosts.getSelectedRow(),0).toString();
				int result = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove the host deployment '"+hostName+"'?", "Remove Host Deployment",
                        JOptionPane.YES_NO_OPTION);
				if (result==JOptionPane.YES_OPTION){
					tblModelHosts.removeRow(tblHosts.getSelectedRow());
					getDeployments().remove(hostName);
				}
			}
		});
        
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
            	btnEditDeployment.setEnabled(tblHosts.getSelectedRows().length > 0);
            	btnDeleteDeployment.setEnabled(tblHosts.getSelectedRows().length > 0);
            }

        });
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(tblHosts);
        
        GridPanel pnlTableButtons = new GridPanel();
        pnlTableButtons.add(btnNewDeployment);
        pnlTableButtons.add(btnEditDeployment);
        pnlTableButtons.add(btnDeleteDeployment);
        pnlTableButtons.layout(1, 3,SwingUtil.WEIGHT_NONE,SwingUtil.WEIGHT_EQUALLY);
        
        GridPanel pnlMainPanel = new GridPanel();
        pnlMainPanel.add(scrollPane);
        
        pnlMainPanel.add(pnlTableButtons);
        pnlMainPanel.layout(2, 1, 0, 0);
        btnEditDeployment.setEnabled(false);
    	btnDeleteDeployment.setEnabled(false);
        return pnlMainPanel;
	}

    private void updateDeploymentTable(){
    	List<String> hosts=new ArrayList<String>();
    	for (int i = 0; i < tblModelHosts.getRowCount(); i++) {
    		hosts.add((String) tblModelHosts.getValueAt(i, 0));
        }
    	for (String hostName : getDeployments().keySet()) {
			if (!hosts.contains(hostName)){
				tblModelHosts.addRow(new Object[] { hostName });
			}
		}
    }
    
	private JTable createParameterTableControls() {
		final JTable tblParameters = new JTable();
        tblParameters.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        tblParameters.setFillsViewportHeight(true);
        defaultTableModel = new DefaultTableModel(new Object[][] { { null, null, null, null }, }, new String[] { "I/O",
                "Parameter Name", "Type", "Description" });
        tblParameters.setModel(defaultTableModel);
        defaultTableModel.addTableModelListener(new TableModelListener() {

            @Override
            public void tableChanged(TableModelEvent arg0) {
                if (!ignoreTableChanges) {
					int selectedRow = tblParameters.getSelectedRow();
					if (selectedRow != -1
							&& defaultTableModel.getRowCount() > 0) {
						Object parameterIOType = defaultTableModel.getValueAt(
								selectedRow, 0);
						Object parameterDataType = defaultTableModel
								.getValueAt(selectedRow, 2);
						if (parameterIOType == null
								|| parameterIOType.equals("")) {
							defaultTableModel.setValueAt(getIOStringList()[0],
									selectedRow, 0);
						}
						if (parameterDataType == null
								|| parameterDataType.equals("")) {
							defaultTableModel.setValueAt(getDataTypes()[0],
									selectedRow, 2);
						}
					}
					addNewRowIfLastIsNotEmpty();
				}
            }

        });
        TableColumn ioColumn = tblParameters.getColumnModel().getColumn(0);
        String[] ioStringList = getIOStringList();
        ioColumn.setCellEditor(new StringArrayComboBoxEditor(ioStringList));

        TableColumn datatypeColumn = tblParameters.getColumnModel().getColumn(2);
        String[] dataTypeStringList = getDataTypes();
        datatypeColumn.setCellEditor(new StringArrayComboBoxEditor(dataTypeStringList));

        TableColumn parameterNameCol = tblParameters.getColumnModel().getColumn(1);
        parameterNameCol.setPreferredWidth(190);
        ListSelectionModel selectionModel = tblParameters.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                btnDeleteParameter.setEnabled(tblParameters.getSelectedRows().length > 0);
            }

        });
        
        btnDeleteParameter = new JButton("Delete parameter");
        btnDeleteParameter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                deleteSelectedRows();
            }
        });
        btnDeleteParameter.setEnabled(false);
        return tblParameters;
	}

    private void loadData() {
    	ServiceDescriptionType descType = getOrginalServiceDescription().getType();
		txtApplicationServiceName.setText(descType.getName());
		setServiceName(txtApplicationServiceName.getText());

		txtApplicationServiceName.setEditable(isNewDescription());
    	ignoreTableChanges=true;
    	updateIODataTable(descType);
    	try {
    		getDeployments().clear();
			Map<HostDescription, List<ApplicationDeploymentDescription>> descs = getRegistry().searchDeploymentDescription(descType.getName());
			for (HostDescription hostDesc : descs.keySet()) {
				getDeployments().put(hostDesc.getType().getHostName(),new HostDeployment(hostDesc, descs.get(hostDesc).get(0)));
			}
		} catch (RegistryException e) {
			e.printStackTrace();
		}
    	updateDeploymentTable();
    	Boolean selected = false;
    	if (descType.getPortType()!=null && descType.getPortType().getMethod()!=null) {
			XmlCursor cursor = descType.getPortType().getMethod().newCursor();
			String value = cursor.getAttributeText(new QName("forceFileStagingToWorkDir"));
			cursor.dispose();
			selected = false;
			if (value != null) {
				selected = Boolean.parseBoolean(value);
			}
		}
		chkForceFileStagingToWorkDir.setSelected(selected);
    	setForceFileStagingToWorkDir(selected);
    	ignoreTableChanges=false;
	}

	private void updateIODataTable(ServiceDescriptionType descType) {
		while(defaultTableModel.getRowCount()>0){
    		defaultTableModel.removeRow(0);
    	}
    	InputParameterType[] iparameters = descType.getInputParametersArray();
    	for (InputParameterType parameter : iparameters) {
    		defaultTableModel.addRow(new Object[] { getIOStringList()[0], parameter.getParameterName(),parameter.getParameterType().getName(),parameter.getParameterDescription()});	
		}
    	OutputParameterType[] oparameters = descType.getOutputParametersArray();
    	for (OutputParameterType parameter : oparameters) {
    		defaultTableModel.addRow(new Object[] { getIOStringList()[1], parameter.getParameterName(), parameter.getParameterType().getName(),parameter.getParameterDescription()});	
		}
    	addNewRowIfLastIsNotEmpty();
	}

    private String[] getIOStringList() {
        String[] ioStringList = new String[] { "Input", "Output" };
        return ioStringList;
    }

    private String[] getDataTypes() {
        String[] type = new String[DataType.Enum.table.lastInt()];
        for (int i = 1; i <= DataType.Enum.table.lastInt(); i++) {
            type[i - 1] = DataType.Enum.forInt(i).toString();
        }
        return type;
    }

    public boolean isServiceCreated() {
        return serviceCreated;
    }

    public void setServiceCreated(boolean serviceCreated) {
        this.serviceCreated = serviceCreated;
    }

    public ServiceDescription getServiceDescription() {
        if (serviceDescription == null) {
            serviceDescription = new ServiceDescription();
        }
        return serviceDescription;
    }

    public ServiceDescriptionType getServiceDescriptionType() {
        return getServiceDescription().getType();
    }

    public String getServiceName() {
        return getServiceDescription().getType().getName();
    }

    public void setServiceName(String serviceName) {
        getServiceDescription().getType().setName(serviceName);
        updateDialogStatus();
    }

    private void setupMethod(){
    	if (getServiceDescriptionType().getPortType()==null){
    		getServiceDescriptionType().setPortType(getServiceDescriptionType().addNewPortType());
    	}
    	if (getServiceDescriptionType().getPortType().getMethod()==null){
    		getServiceDescriptionType().getPortType().setMethod(getServiceDescriptionType().getPortType().addNewMethod());
    	}
    }
    public void setForceFileStagingToWorkDir(Boolean force){
    	setupMethod();
    	XmlCursor cursor = getServiceDescriptionType().getPortType().getMethod().newCursor();
    	cursor.toNextToken();
		if (!cursor.setAttributeText(new QName("http://schemas.airavata.apache.org/gfac/type","forceFileStagingToWorkDir"),force.toString())){
			cursor.insertAttributeWithValue("forceFileStagingToWorkDir",force.toString());
		}
		cursor.dispose();
    }
    
    public Boolean getForceFileStagingToWorkDir(){
    	setupMethod();
    	XmlCursor cursor = getServiceDescriptionType().getPortType().getMethod().newCursor();    	
    	cursor.toNextToken();
		String value = cursor.getAttributeText(new QName("forceFileStagingToWorkDir"));
		cursor.dispose();
		if (value==null){
			return false;
		}else{
			return Boolean.parseBoolean(value);
		}
		
    }
    
    private void updateDialogStatus() {
        String message = null;
        try {
            validateDialog();
        } catch (Exception e) {
            message = e.getLocalizedMessage();
        }
        okButton.setEnabled(message == null);
        setError(message);
    }

    private void validateDialog() throws Exception {
        if (getServiceName() == null || getServiceName().trim().equals("")) {
            throw new Exception("Name of the application cannot be empty!!!");
        }

        ServiceDescription serviceDescription2 = null;
        try {
            serviceDescription2 = getRegistry().getServiceDescription(getServiceName());
        } catch (RegistryException e) {
            if (e.getCause() instanceof PathNotFoundException) {
                // non-existant name. just want we want
            } else {
                throw e;
            }
        }
        if (isNewDescription() && serviceDescription2 != null) {
            throw new Exception("Service descriptor with the given name already exists!!!");
        }
    }

    public void saveServiceDescription() throws RegistryException {
        List<InputParameterType> inputParameters = new ArrayList<InputParameterType>();
        List<OutputParameterType> outputParameters = new ArrayList<OutputParameterType>();

        for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
            String parameterName = (String) defaultTableModel.getValueAt(i, 1);
            String paramType = (String) defaultTableModel.getValueAt(i, 2);
            String parameterDescription = (String) defaultTableModel.getValueAt(i, 3);
            if (parameterName != null && !parameterName.trim().equals("")) {
                // todo how to handle Enum
                if (getIOStringList()[0].equals(defaultTableModel.getValueAt(i, 0))) {
                    InputParameterType parameter = InputParameterType.Factory.newInstance();
                    parameter.setParameterName(parameterName);
                    parameter.setParameterDescription(parameterDescription);
                    ParameterType parameterType = parameter.addNewParameterType();
                    parameterType.setType(DataType.Enum.forString(paramType));
                    parameterType.setName(paramType);
                    inputParameters.add(parameter);

                } else {
                    OutputParameterType parameter = OutputParameterType.Factory.newInstance();
                    parameter.setParameterName(parameterName);
                    parameter.setParameterDescription(parameterDescription);
                    ParameterType parameterType = parameter.addNewParameterType();
                    parameterType.setType(DataType.Enum.forString(paramType));
                    parameterType.setName(paramType);
                    outputParameters.add(parameter);
                }
            }
        }
        getServiceDescriptionType().setInputParametersArray(inputParameters.toArray(new InputParameterType[] {}));
        getServiceDescriptionType().setOutputParametersArray(outputParameters.toArray(new OutputParameterType[] {}));

        try {
			getRegistry().saveServiceDescription(getServiceDescription());
			if (!isNewDescription()){
				Map<HostDescription, List<ApplicationDeploymentDescription>> descs = getRegistry().searchDeploymentDescription(getServiceName());
				for (HostDescription hostDesc : descs.keySet()) {
					for (ApplicationDeploymentDescription app : descs.get(hostDesc)) {
						getRegistry().deleteDeploymentDescription(getServiceName(), hostDesc.getType().getHostName(), app.getType().getApplicationName().getStringValue());	
					}
				}
			}
			for (String hostName : getDeployments().keySet()) {
				getRegistry().saveDeploymentDescription(getServiceName(), hostName, getDeployments().get(hostName).getApplicationDescription());
			}
	        setServiceCreated(true);
	        JOptionPane.showMessageDialog(this,"Application '"+getServiceName()+"' is registered Successfully !");
		} catch (RegistryException e) {
			setError(e.getMessage());
			throw e;
		}
    }

    public void close() {
        getDialog().setVisible(false);
    }

    private void setError(String errorMessage) {
        if (errorMessage == null || errorMessage.trim().equals("")) {
            lblError.setText("");
        } else {
            lblError.setText(errorMessage.trim());
        }
    }

    private void deleteSelectedRows() {
        // TODO confirm deletion of selected rows
        int selectedRow = tblParameters.getSelectedRow();
        while (selectedRow >= 0 && tblParameters.getRowCount()>0) {
            defaultTableModel.removeRow(selectedRow);
            selectedRow = tblParameters.getSelectedRow();
        }
        addNewRowIfLastIsNotEmpty();
    }

    private void addNewRowIfLastIsNotEmpty() {
    	
        if (defaultTableModel.getRowCount()>0) {
			Object parameterName = defaultTableModel.getValueAt(
					defaultTableModel.getRowCount() - 1, 1);
			if (parameterName != null && !parameterName.equals("")) {
				defaultTableModel
						.addRow(new Object[] { null, null, null, null });
			}
		}else{
			if (tblParameters.getSelectedRow()==-1){
				defaultTableModel.addRow(new Object[] { null, null, null, null });
			}
			
		}
    }

    public AiravataRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry registry) {
        this.registry = registry;
    }

    public boolean isNewDescription() {
		return newDescription;
	}

	public void setNewDescription(boolean newDescription) {
		this.newDescription = newDescription;
	}

	public ServiceDescription getOrginalServiceDescription() {
		return orginalServiceDescription;
	}

	public void setOrginalServiceDescription(ServiceDescription orginalServiceDescription) {
		this.orginalServiceDescription = orginalServiceDescription;
	}

	public String getSuggestedNamePrefix() {
		return suggestedNamePrefix;
	}

	public void setSuggestedNamePrefix(String suggestedNamePrefix) {
		this.suggestedNamePrefix = suggestedNamePrefix;
	}

	public String getTitlePrefix() {
		return titlePrefix;
	}

	public void setTitlePrefix(String titlePrefix) {
		this.titlePrefix = titlePrefix;
	}

	public Map<String,HostDeployment> getDeployments() {
		if (deployments==null){
			deployments=new HashMap<String, HostDeployment>();
		}
		return deployments;
	}

	private class StringArrayComboBoxEditor extends DefaultCellEditor {
        private static final long serialVersionUID = -304464739219209395L;

        public StringArrayComboBoxEditor(Object[] items) {
            super(new JComboBox(items));
        }
    }
}
