package org.apache.airavata.xbaya.appwrapper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.jcr.PathNotFoundException;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.apache.airavata.commons.gfac.type.DataType;
import org.apache.airavata.commons.gfac.type.Parameter;
import org.apache.airavata.commons.gfac.type.ServiceDescription;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ServiceDescriptionDialog extends JDialog {

	private static final long serialVersionUID = 2705760838264284423L;
	private final JPanel contentPanel = new JPanel();
	private JLabel lblServiceName;
	private JTextField txtServiceName;
	private JTable tblParameters;
	private boolean serviceCreated=false;
	private JLabel lblError;
	private XBayaEngine engine;
	private ServiceDescription serviceDescription;
	private JButton okButton;
	private JButton btnDeleteParameter;
	private DefaultTableModel defaultTableModel;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ServiceDescriptionDialog dialog = new ServiceDescriptionDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ServiceDescriptionDialog(XBayaEngine engine) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				String baseName="Service";
				int i=1;
				String defaultName=baseName+i;
				try {
					while(getJCRComponentRegistry().getServiceDescription(defaultName)!=null){
						defaultName=baseName+(++i);
					}
				} catch (Exception e) {
				}
				txtServiceName.setText(defaultName);
				setServiceName(txtServiceName.getText());
			}
		});		
		setEngine(engine);
		initGUI();
		
		
	}
	
	public void open(){
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	protected ServiceDescriptionDialog getDialog(){
		return this;
	}
	
	private void initGUI() {
		setTitle("New Service Description");
		setBounds(100, 100, 463, 369);
		setModal(true);
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap(5);
		borderLayout.setHgap(5);
		getContentPane().setLayout(borderLayout);
		contentPanel.setBorder(null);
		getContentPane().add(contentPanel, BorderLayout.EAST);
		{
			lblServiceName = new JLabel("Service name");
		}
		
		txtServiceName = new JTextField();
		txtServiceName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				setServiceName(txtServiceName.getText());
			}
		});
		txtServiceName.setColumns(10);
		
		JSeparator separator = new JSeparator();
		
		JLabel lblInputParameters = new JLabel("Service Parameters");
		lblInputParameters.setFont(new Font("Tahoma", Font.BOLD, 11));
		
		JScrollPane scrollPane = new JScrollPane();
		
		btnDeleteParameter = new JButton("Delete parameter");
		btnDeleteParameter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteSelectedRows();
			}
		});
		btnDeleteParameter.setEnabled(false);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGap(177)
					.addComponent(lblInputParameters)
					.addContainerGap(313, Short.MAX_VALUE))
				.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
					.addContainerGap(171, Short.MAX_VALUE)
					.addComponent(lblServiceName)
					.addGap(18)
					.addComponent(txtServiceName, GroupLayout.PREFERRED_SIZE, 309, GroupLayout.PREFERRED_SIZE)
					.addGap(30))
				.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
					.addGap(181)
					.addComponent(separator)
					.addContainerGap())
				.addGroup(Alignment.TRAILING, gl_contentPanel.createSequentialGroup()
					.addContainerGap(195, Short.MAX_VALUE)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(btnDeleteParameter)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 380, GroupLayout.PREFERRED_SIZE))
					.addGap(27))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblServiceName)
						.addComponent(txtServiceName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblInputParameters)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 182, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnDeleteParameter)
					.addGap(74))
		);
		
		tblParameters = new JTable();
		tblParameters.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				int selectedRow = tblParameters.getSelectedRow();
				Object parameterIOType = defaultTableModel.getValueAt(selectedRow, 0);
				Object parameterDataType = defaultTableModel.getValueAt(selectedRow, 2);
				if (parameterIOType==null || parameterIOType.equals("")){
					defaultTableModel.setValueAt(getIOStringList()[0],selectedRow, 0);
				}
				if (parameterDataType==null || parameterDataType.equals("")){
					defaultTableModel.setValueAt(getDataTypes()[0],selectedRow, 2);
				}
				addNewRowIfLastIsNotEmpty();
			}
		});
		tblParameters.setFillsViewportHeight(true);
		defaultTableModel = new DefaultTableModel(
			new Object[][] {
				{null, null, null, null},
			},
			new String[] {
				"I/O", "Parameter Name", "Type", "Description"
			}
		);
		tblParameters.setModel(defaultTableModel);
		TableColumn ioColumn = tblParameters.getColumnModel().getColumn(0);
		String[] ioStringList = getIOStringList();
		ioColumn.setCellEditor(new StringArrayComboBoxEditor(ioStringList));
//		ioColumn.setCellRenderer(new StringArrayComboBoxRenderer(ioStringList));
		
		TableColumn datatypeColumn = tblParameters.getColumnModel().getColumn(2);
		DataType[] dataTypeStringList = getDataTypes();
		datatypeColumn.setCellEditor(new StringArrayComboBoxEditor(dataTypeStringList));
//		datatypeColumn.setCellRenderer(new StringArrayComboBoxRenderer(dataTypeStringList));
		
		tblParameters.getColumnModel().getColumn(1).setPreferredWidth(190);
		scrollPane.setViewportView(tblParameters);
		
		ListSelectionModel selectionModel = tblParameters.getSelectionModel();
		selectionModel.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		selectionModel.addListSelectionListener(new ListSelectionListener() {
	      public void valueChanged(ListSelectionEvent e) {
	    	  btnDeleteParameter.setEnabled(tblParameters.getSelectedRows().length>0);
	      }

	    });
	    
		gl_contentPanel.setAutoCreateContainerGaps(true);
		gl_contentPanel.setAutoCreateGaps(true);
		contentPanel.setLayout(gl_contentPanel);
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			GridBagLayout gbl_buttonPane = new GridBagLayout();
			gbl_buttonPane.columnWidths = new int[]{307, 136, 0};
			gbl_buttonPane.rowHeights = new int[]{33, 0};
			gbl_buttonPane.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
			gbl_buttonPane.rowWeights = new double[]{0.0, Double.MIN_VALUE};
			buttonPane.setLayout(gbl_buttonPane);
			
			lblError = new JLabel("");
			lblError.setForeground(Color.RED);
			GridBagConstraints gbc_lblError = new GridBagConstraints();
			gbc_lblError.insets = new Insets(0, 0, 0, 5);
			gbc_lblError.gridx = 0;
			gbc_lblError.gridy = 0;
			buttonPane.add(lblError, gbc_lblError);
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.anchor = GridBagConstraints.NORTHWEST;
			gbc_panel.gridx = 1;
			gbc_panel.gridy = 0;
			buttonPane.add(panel, gbc_panel);
			{
				okButton = new JButton("Save");
				okButton.setEnabled(false);
				okButton.addActionListener(new ActionListener() {
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
					public void actionPerformed(ActionEvent e) {
						setServiceCreated(false);
						close();
					}
				});
				panel.add(cancelButton);
				cancelButton.setActionCommand("Cancel");
			}
		}
	}

	private String[] getIOStringList() {
		String[] ioStringList = new String[]{"Input","Output"};
		return ioStringList;
	}

	private DataType[] getDataTypes() {
		return DataType.values();
	}

	public boolean isServiceCreated() {
		return serviceCreated;
	}

	public void setServiceCreated(boolean serviceCreated) {
		this.serviceCreated = serviceCreated;
	}

	public ServiceDescription getServiceDescription() {
		if (serviceDescription==null){
			serviceDescription=new ServiceDescription();
		}
		return serviceDescription;
	}

	public XBayaEngine getEngine() {
		return engine;
	}

	public void setEngine(XBayaEngine engine) {
		this.engine = engine;
	}

	public String getServiceName() {
		return getServiceDescription().getName();
	}

	public void setServiceName(String serviceName) {
		getServiceDescription().setName(serviceName);
		updateDialogStatus();
	}
	
	private void validateDialog() throws Exception{
		if (getServiceName()==null || getServiceName().trim().equals("")){
			throw new Exception("Name of the service cannot be empty!!!");
		}
		
		ServiceDescription serviceDescription2=null;
		try {
			serviceDescription2 = getJCRComponentRegistry().getServiceDescription(Pattern.quote(getServiceName()));
		} catch (PathNotFoundException e) {
			//what we want
		} catch (Exception e){
			throw e;
		}
		if (serviceDescription2!=null){
			throw new Exception("Service descriptor with the given name already exists!!!");
		}

		
	}
	private void updateDialogStatus(){
		String message=null;
		try {
			validateDialog();
		} catch (Exception e) {
			message=e.getLocalizedMessage();
		}
		okButton.setEnabled(message==null);
		setError(message);
	}

	public void close() {
		getDialog().setVisible(false);
	}

	public void saveServiceDescription() {
		getServiceDescription().setInputParameters(new ArrayList<Parameter>());
		getServiceDescription().setOutputParameters(new ArrayList<Parameter>());
		for(int i=0;i<defaultTableModel.getRowCount();i++){
			Parameter parameter = new Parameter();
			String parameterName = (String)defaultTableModel.getValueAt(i, 1);
			DataType parameterDataType = (DataType)defaultTableModel.getValueAt(i, 2);
			String parameterDescription = (String)defaultTableModel.getValueAt(i, 3);
			parameter.setName(parameterName);
			parameter.setDescription(parameterDescription);
			parameter.setType(parameterDataType);
			if (getIOStringList()[0].equals(defaultTableModel.getValueAt(i, 0))){
				getServiceDescription().getInputParameters().add(parameter);
			}else{
				getServiceDescription().getOutputParameters().add(parameter);
			}
		}
		
		getJCRComponentRegistry().saveServiceDescription(getServiceName(), getServiceDescription());
		setServiceCreated(true);
	}

	private JCRComponentRegistry getJCRComponentRegistry() {
		return getEngine().getConfiguration().getJcrComponentRegistry();
	}
	
	private void setError(String errorMessage){
		if (errorMessage==null || errorMessage.trim().equals("")){
			lblError.setText("");
		}else{
			lblError.setText(errorMessage.trim());
		}
	}
	
	private void deleteSelectedRows() {
		//TODO confirm deletion of selected rows
		int selectedRow = tblParameters.getSelectedRow();
		while(selectedRow>=0){
			defaultTableModel.removeRow(selectedRow);
			selectedRow = tblParameters.getSelectedRow();
		}
		addNewRowIfLastIsNotEmpty();
	}

	private void addNewRowIfLastIsNotEmpty() {
		Object parameterName = defaultTableModel.getValueAt(defaultTableModel.getRowCount()-1, 1);
		if (parameterName!=null && !parameterName.equals("")){
			defaultTableModel.addRow(new Object[]{null,null,null,null});
		}
	}

	private class StringArrayComboBoxRenderer extends JComboBox implements TableCellRenderer {
		private static final long serialVersionUID = 8634257755770934231L;

		public StringArrayComboBoxRenderer(String[] items) {
	        super(items);
	    }

	    public Component getTableCellRendererComponent(JTable table, Object value,
	            boolean isSelected, boolean hasFocus, int row, int column) {
	        if (isSelected) {
	            setForeground(table.getSelectionForeground());
	            super.setBackground(table.getSelectionBackground());
	        } else {
	            setForeground(table.getForeground());
	            setBackground(table.getBackground());
	        }

	        // Select the current value
	        setSelectedItem(value);
	        return this;
	    }
	}

	private class StringArrayComboBoxEditor extends DefaultCellEditor {
		private static final long serialVersionUID = -304464739219209395L;

		public StringArrayComboBoxEditor(Object[] items) {
	        super(new JComboBox(items));
	    }
	}
}
