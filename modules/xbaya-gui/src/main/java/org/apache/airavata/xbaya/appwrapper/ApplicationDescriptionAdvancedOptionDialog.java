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

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.xml.namespace.QName;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.commons.gfac.type.ApplicationDeploymentDescription;
import org.apache.airavata.registry.api.AiravataRegistry;
import org.apache.airavata.schemas.gfac.ApplicationDeploymentDescriptionType;
import org.apache.airavata.schemas.gfac.DataType;
import org.apache.airavata.schemas.gfac.InputParameterType;
import org.apache.airavata.schemas.gfac.NameValuePairType;
import org.apache.airavata.schemas.gfac.OutputParameterType;
import org.apache.airavata.schemas.gfac.ParameterType;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaTextField;
import org.apache.xmlbeans.XmlCursor;

public class ApplicationDescriptionAdvancedOptionDialog extends JDialog {
    private static final long serialVersionUID = 3920479739097405014L;
    private XBayaTextField txtInputDir;
    private XBayaTextField txtOutputDir;
    private XBayaTextField txtSTDIN;
    private XBayaTextField txtSTDOUT;
    private XBayaTextField txtSTDERR;
    private JTable tblEnv;
    private ApplicationDeploymentDescription shellApplicationDescription;
    private DefaultTableModel defaultTableModel;
    private boolean tableModelChanging = false;
    private JButton btnDeleteVariable;
    private JButton okButton;
    private AiravataRegistry registry;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            ApplicationDescriptionAdvancedOptionDialog dialog = new ApplicationDescriptionAdvancedOptionDialog(null,
                    null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public ApplicationDescriptionAdvancedOptionDialog(AiravataRegistry registry, ApplicationDeploymentDescription descriptor) {
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

    protected ApplicationDescriptionAdvancedOptionDialog getDialog() {
        return this;
    }

    public void close() {
        getDialog().setVisible(false);
    }

    @SuppressWarnings("serial")
	private void initGUI() {
        setTitle("Application Description Advance Options");
        setModal(true);
        setBounds(100, 100, 600, 400);
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
        
        txtInputDir = new XBayaTextField();
        
        XBayaLabel lblInputDirectory = new XBayaLabel("Input directory",txtInputDir);

        JLabel lblLocations = new JLabel("Locations");
        lblLocations.setFont(new Font("Tahoma", Font.BOLD, 11));

        txtOutputDir = new XBayaTextField();

        XBayaLabel lblOutputDirectory = new XBayaLabel("Output directory",txtOutputDir);

        JLabel lblProgramData = new JLabel("Program data");
        lblProgramData.setFont(new Font("Tahoma", Font.BOLD, 11));


        txtSTDIN = new XBayaTextField();
        XBayaLabel lblStdin = new XBayaLabel("STDIN",txtSTDIN);


        txtSTDOUT = new XBayaTextField();
        XBayaLabel lblStdout = new XBayaLabel("STDOUT",txtSTDOUT);


        txtSTDERR = new XBayaTextField();
        XBayaLabel lblStderr = new XBayaLabel("STDERR",txtSTDERR);

        JLabel other = new JLabel("Other");
        other.setFont(new Font("Tahoma", Font.BOLD, 11));

        JSeparator separator_1 = new JSeparator();
        separator_1.setOrientation(SwingConstants.VERTICAL);

        JLabel lblEnvironmentalVariables = new JLabel("Environmental Variables");
        lblEnvironmentalVariables.setFont(new Font("Tahoma", Font.BOLD, 11));

        JScrollPane scrollPane = new JScrollPane();

        btnDeleteVariable = new JButton("Delete variable");
        btnDeleteVariable.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedRows();
            }
        });
        btnDeleteVariable.setEnabled(false);
        tblEnv = new JTable();
        tblEnv.setFillsViewportHeight(true);
        scrollPane.setViewportView(tblEnv);
        tblEnv.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        defaultTableModel = new DefaultTableModel(new Object[][] { { null, null }, }, new String[] { "Name",
                "Value" }) {
            @SuppressWarnings("rawtypes")
			Class[] columnTypes = new Class[] { String.class, String.class };

            @SuppressWarnings({ "rawtypes", "unchecked" })
			public Class getColumnClass(int columnIndex) {
                return columnTypes[columnIndex];
            }
        };
        tblEnv.setModel(defaultTableModel);
        defaultTableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent arg0) {
                if (!tableModelChanging) {
                    addNewRowIfLastIsNotEmpty();
                }
            }

        });
        tblEnv.getColumnModel().getColumn(0).setPreferredWidth(67);
        tblEnv.getColumnModel().getColumn(1).setPreferredWidth(158);
        ListSelectionModel selectionModel = tblEnv.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                btnDeleteVariable.setEnabled(tblEnv.getSelectedRows().length > 0);
            }

        });
        
        GridPanel leftPanel = new GridPanel();
        leftPanel.add(lblLocations);
        leftPanel.add(new JLabel());
        leftPanel.add(lblInputDirectory);
        leftPanel.add(txtInputDir);
        leftPanel.add(lblOutputDirectory);
        leftPanel.add(txtOutputDir);
        leftPanel.add(lblProgramData);
        leftPanel.add(new JLabel());
        leftPanel.add(lblStdin);
        leftPanel.add(txtSTDIN);
        leftPanel.add(lblStdout);
        leftPanel.add(txtSTDOUT);
        leftPanel.add(lblStderr);
        leftPanel.add(txtSTDERR);
        
        SwingUtil.layoutToGrid(leftPanel.getSwingComponent(), 7, 2, SwingUtil.WEIGHT_NONE, 1);
        
        GridPanel rightPanel = new GridPanel();
        rightPanel.add(lblEnvironmentalVariables);
        rightPanel.add(scrollPane);
        rightPanel.add(btnDeleteVariable);
        rightPanel.getSwingComponent().setSize(150, -1);
        leftPanel.getSwingComponent().setSize(150, -1);
        SwingUtil.layoutToGrid(rightPanel.getSwingComponent(), 3, 1, 1, 0);
        
        GridPanel p=new GridPanel();
        p.add(leftPanel);
        p.add(new JSeparator(JSeparator.VERTICAL));
        p.layout(1,2, 0,0);
        panel.add(p);
        panel.add(rightPanel);
        panel.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());

        SwingUtil.layoutToGrid(panel.getSwingComponent(), 1, 2, SwingUtil.WEIGHT_NONE, SwingUtil.WEIGHT_EQUALLY);
        
        buttonPane.add(okButton);
        buttonPane.add(cancelButton);
        buttonPane.getSwingComponent().setBorder(BorderFactory.createEtchedBorder());

        getContentPane().add(panel.getSwingComponent());
        getContentPane().add(buttonPane.getSwingComponent());
        SwingUtil.layoutToGrid(getContentPane(), 2, 1, 0, 0);
        setResizable(true);
        getRootPane().setDefaultButton(okButton);
    }

    private void deleteSelectedRows() {
        // TODO confirm deletion of selected rows
        int selectedRow = tblEnv.getSelectedRow();
        while (selectedRow >= 0) {
            defaultTableModel.removeRow(selectedRow);
            selectedRow = tblEnv.getSelectedRow();
        }
        addNewRowIfLastIsNotEmpty();
    }

    public ApplicationDeploymentDescription getApplicationDescription() {
        return shellApplicationDescription;
    }

    public ApplicationDeploymentDescriptionType getShellApplicationDescriptionType() {
        return (ApplicationDeploymentDescriptionType)shellApplicationDescription.getType();
    }
    
    public void setShellApplicationDescription(ApplicationDeploymentDescription shellApplicationDescription) {
        this.shellApplicationDescription = shellApplicationDescription;
    }

    private void addNewRowIfLastIsNotEmpty() {
        Object varName = null;
        if (defaultTableModel.getRowCount() > 0) {
            varName = defaultTableModel.getValueAt(defaultTableModel.getRowCount() - 1, 0);
        }
        if (defaultTableModel.getRowCount() == 0 || (varName != null && !varName.equals(""))) {
            defaultTableModel.addRow(new Object[] { null, null });
        }
    }

    private void saveApplicationDescriptionAdvancedOptions() {
    	getShellApplicationDescriptionType().setInputDataDirectory(txtInputDir.getText());
    	getShellApplicationDescriptionType().setOutputDataDirectory(txtOutputDir.getText());
    	getShellApplicationDescriptionType().setStandardInput(txtSTDIN.getText());
    	getShellApplicationDescriptionType().setStandardOutput(txtSTDOUT.getText());
    	getShellApplicationDescriptionType().setStandardError(txtSTDERR.getText());
    	
    	while(getShellApplicationDescriptionType().getApplicationEnvironmentArray().length>0){
    		getShellApplicationDescriptionType().removeApplicationEnvironment(0);
    	}
    	for (int i = 0; i < defaultTableModel.getRowCount(); i++) {
            String parameterName = (String) defaultTableModel.getValueAt(i, 0);
            String paramValue = (String) defaultTableModel.getValueAt(i, 1);
            if (parameterName != null && !parameterName.trim().equals("")) {
            	NameValuePairType envType = getShellApplicationDescriptionType().addNewApplicationEnvironment();
        		envType.setName(parameterName);
                envType.setValue(paramValue);
            }
        }
    	int a=10;
    }

    private void loadApplicationDescriptionAdvancedOptions() {
        txtInputDir.setText(getShellApplicationDescriptionType().getInputDataDirectory());
        txtOutputDir.setText(getShellApplicationDescriptionType().getOutputDataDirectory());
        txtSTDIN.setText(getShellApplicationDescriptionType().getStandardInput());
        txtSTDOUT.setText(getShellApplicationDescriptionType().getStandardOutput());
        txtSTDERR.setText(getShellApplicationDescriptionType().getStandardError());
        tableModelChanging = true;
        while(defaultTableModel.getRowCount()>0){
    		defaultTableModel.removeRow(0);
    	}
        NameValuePairType[] envParams = getShellApplicationDescriptionType().getApplicationEnvironmentArray();
    	for (NameValuePairType envParam : envParams) {
    		defaultTableModel.addRow(new Object[] { envParam.getName(),envParam.getName()});	
		}
    	addNewRowIfLastIsNotEmpty();
        tableModelChanging = false;
    }



    public AiravataRegistry getRegistry() {
        return registry;
    }

    public void setRegistry(AiravataRegistry registry) {
        this.registry = registry;
    }


}
