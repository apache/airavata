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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.PathNotFoundException;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.schemas.gfac.ServiceDescriptionType;
import org.apache.airavata.xbaya.ui.dialogs.descriptors.DescriptorListDialog.DescriptorType;
import org.apache.airavata.xbaya.ui.widgets.GridPanel;
import org.apache.airavata.xbaya.ui.widgets.XBayaLabel;
import org.apache.airavata.xbaya.ui.widgets.XBayaTextField;
import org.apache.xmlbeans.XmlCursor;

public class ServiceDescriptionDialog extends JDialog {

    private static final long serialVersionUID = 2705760838264284423L;
    private final GridPanel contentPanel = new GridPanel();
    private XBayaLabel lblServiceName;
    private XBayaTextField txtServiceName;
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
	private boolean serviceDescriptionMode;
	private String suggestedNamePrefix;
	private String titlePrefix;
	
    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            ServiceDescriptionDialog dialog = new ServiceDescriptionDialog(null,true,null);
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ServiceDescriptionDialog(AiravataRegistry registry) {
    	this(registry,true,null);
    }
    
    public ServiceDescriptionDialog(AiravataRegistry registry, boolean newDescription, ServiceDescription serviceDescription) {
    	this(registry, newDescription, serviceDescription, true, null);
    }
    
    /**
     * Create the dialog.
     */
    public ServiceDescriptionDialog(AiravataRegistry registry, boolean newDescription, ServiceDescription serviceDescription, boolean serviceDescriptionMode, String suggestedNamePrefix) {
    	setNewDescription(newDescription);
    	this.setOrginalServiceDescription(serviceDescription);
    	setServiceDescriptionMode(serviceDescriptionMode);
    	setSuggestedNamePrefix(suggestedNamePrefix);
    	if (isNewDescription()) {
			setTitlePrefix(isServiceDescriptionMode()? "New Service Description":"Input/Output parameters for "+getSuggestedNamePrefix());
		}else{
			setTitlePrefix(isServiceDescriptionMode()? "Update Service Description: "+getOrginalServiceDescription().getType().getName():"Update Input/Output parameters for "+getSuggestedNamePrefix());
		}
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent arg0) {
                if (isNewDescription() && !isServiceDescriptionMode()) {
					String baseName = isServiceDescriptionMode()? "Service":getSuggestedNamePrefix()+"_Service";
					int i;
					String defaultName;
					if (isServiceDescriptionMode()) {
						i = 1;
						defaultName = baseName+i;
					}else{
						i = 0;
						defaultName = baseName;
					}
					try {
						while (getRegistry().getServiceDescription(defaultName) != null) {
							defaultName = baseName + (++i);
						}
					} catch (Exception e) {
					}
					txtServiceName.setText(defaultName);
					setServiceName(txtServiceName.getText());
				}
            }
        });
        setRegistry(registry);
        initGUI();

    }

    public void open() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setVisible(true);
    }

    protected ServiceDescriptionDialog getDialog() {
        return this;
    }

    private void initGUI() {
    	setTitle(getTitlePrefix());
//        if (isNewDescription()) {
//			setTitle("New Service Description");
//		}else{
//			setTitle("Update Service Description: "+getOrginalServiceDescription().getType().getName());
//		}
		setBounds(100, 100, 463, 459);
        setModal(true);
        setLocationRelativeTo(null);
        BorderLayout borderLayout = new BorderLayout();
        borderLayout.setVgap(5);
        borderLayout.setHgap(5);
        getContentPane().setLayout(borderLayout);

        txtServiceName = new XBayaTextField();
        txtServiceName.getSwingComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setServiceName(txtServiceName.getText());
            }
        });
        txtServiceName.setColumns(10);
        lblServiceName = new XBayaLabel(isServiceDescriptionMode()? "Service name":"Bind parameters to service",txtServiceName);
        if (!isServiceDescriptionMode()){
        	lblServiceName.getSwingComponent().setFont(new Font("Tahoma", Font.ITALIC, 11));
        }
        JLabel lblInputParameters = new JLabel(isServiceDescriptionMode()? "Service Parameters":"Input/Output Parameters");
        lblInputParameters.setFont(new Font("Tahoma", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane();

        btnDeleteParameter = new JButton("Delete parameter");
        btnDeleteParameter.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                deleteSelectedRows();
            }
        });
        btnDeleteParameter.setEnabled(false);

        tblParameters = new JTable();
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
        scrollPane.setViewportView(tblParameters);
        ListSelectionModel selectionModel = tblParameters.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                btnDeleteParameter.setEnabled(tblParameters.getSelectedRows().length > 0);
            }

        });
        JLabel lblTableParameterNote=null;
        if (!isServiceDescriptionMode()) {
			final JPopupMenu popup = new JPopupMenu();
			JMenuItem menuItem = new JMenuItem(
					"Bind to parameters in existing service...");
			menuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					bindExistingServiceDescriptionAsParameters();
				}

			});
			popup.add(menuItem);
			tblParameters.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					showPopup(e);
				}

				public void mousePressed(MouseEvent e) {
					showPopup(e);
				}

				public void mouseReleased(MouseEvent e) {
					showPopup(e);
				}

				private void showPopup(MouseEvent e) {
					if (e.isPopupTrigger()) {
						popup.show(tblParameters, e.getX(), e.getY());
					}
				}
			});
			lblTableParameterNote = new JLabel("*Note: Right click on the table to bind an existing service");
			lblTableParameterNote.setFont(new Font("Tahoma", Font.ITALIC, 11));
		}
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
                okButton = new JButton("Save");
                if (!isNewDescription()){
                	okButton.setText("Update");
                }
                okButton.setEnabled(false);
                okButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        saveServiceDescription();
                        close();
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
        contentPanel.add(txtServiceName);
        GridPanel parameterPanel=new GridPanel();
        if (isServiceDescriptionMode()) {
			parameterPanel.add(lblInputParameters);
		}
		if (lblTableParameterNote!=null) {
			parameterPanel.add(lblTableParameterNote);
		}
        parameterPanel.add(scrollPane);
			//        if (isServiceDescriptionMode()){
    	parameterPanel.add(btnDeleteParameter);
//        }else{
//            GridPanel parameterOptionPanel=new GridPanel();
//        	JButton btnLoadExistingServiceData = new JButton("Bind to parameters in existing service description...");
//        	btnLoadExistingServiceData.addActionListener(new ActionListener(){
//				@Override
//				public void actionPerformed(ActionEvent arg0) {
//					bindExistingServiceDescriptionAsParameters();
//				}
//        	});
//        	parameterOptionPanel.add(btnLoadExistingServiceData);
//        	parameterOptionPanel.add(btnDeleteParameter);
//        	parameterPanel.add(parameterOptionPanel);
//        }
        
        SwingUtil.layoutToGrid(contentPanel.getSwingComponent(), 1, 2, SwingUtil.WEIGHT_NONE, 1);
//        if (lblTableParameterNote==null){
        	SwingUtil.layoutToGrid(parameterPanel.getSwingComponent(), 3, 1, 1, 0);
//        }else{
//        	SwingUtil.layoutToGrid(parameterPanel.getSwingComponent(), 4, 1, 2, 0);
//        }
        GridPanel infoPanel = new GridPanel();
        if (isServiceDescriptionMode()) {
			infoPanel.add(contentPanel);
		}
		infoPanel.add(parameterPanel);
        infoPanel.add(chkForceFileStagingToWorkDir);
        if (!isServiceDescriptionMode()) {
			infoPanel.add(contentPanel);
		}
        infoPanel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());
        if (isServiceDescriptionMode()) {
			infoPanel.layout(3, 1, 1, 0);
		}else{
			infoPanel.layout(3, 1, 0, 0);
		}
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

    private void loadData() {
    	ServiceDescriptionType descType = getOrginalServiceDescription().getType();
		txtServiceName.setText(descType.getName());
		setServiceName(txtServiceName.getText());

		txtServiceName.setEditable(!isServiceDescriptionMode() || isNewDescription());
    	ignoreTableChanges=true;
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
    	Boolean selected = false;
    	if (descType.getPortType()!=null && descType.getPortType().getMethod()!=null) {
			XmlCursor cursor = descType.getPortType().getMethod().newCursor();
//			cursor.toNextToken();
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
            throw new Exception("Name of the service cannot be empty!!!");
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

    public void saveServiceDescription() {
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
	        setServiceCreated(true);
		} catch (RegistryException e) {
			e.printStackTrace();
			setError(e.getMessage());
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

	public boolean isServiceDescriptionMode() {
		return serviceDescriptionMode;
	}

	public void setServiceDescriptionMode(boolean serviceDescriptionMode) {
		this.serviceDescriptionMode = serviceDescriptionMode;
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

	private void bindExistingServiceDescriptionAsParameters() {
		DescriptorListDialog descriptorListDialog = new DescriptorListDialog(getRegistry(),DescriptorType.SERVICE);
		descriptorListDialog.open();
		if (descriptorListDialog.isServiceSelected()){
			setOrginalServiceDescription((ServiceDescription) descriptorListDialog.getSelected());
			setNewDescription(false);
			loadData();
		}
	}

	private class StringArrayComboBoxEditor extends DefaultCellEditor {
        private static final long serialVersionUID = -304464739219209395L;

        public StringArrayComboBoxEditor(Object[] items) {
            super(new JComboBox(items));
        }
    }
}
