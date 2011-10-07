package org.apache.airavata.xbaya.appwrapper;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;

import org.apache.airavata.xbaya.gui.XBayaLinkButton;

public class ServiceDescriptionDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JLabel lblRegistry;
	private JLabel lblServiceName;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ServiceDescriptionDialog dialog = new ServiceDescriptionDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ServiceDescriptionDialog() {
		setTitle("New Service Description");
		setBounds(100, 100, 457, 354);
		BorderLayout borderLayout = new BorderLayout();
		borderLayout.setVgap(5);
		borderLayout.setHgap(5);
		getContentPane().setLayout(borderLayout);
		contentPanel.setBorder(null);
		getContentPane().add(contentPanel, BorderLayout.EAST);
		{
			lblServiceName = new JLabel("Service name");
		}
		{
			lblRegistry = new JLabel("Registry");
		}
		
		JComboBox comboBox = new JComboBox();
		
		JSeparator lblSeperator = new JSeparator();
		
		textField = new JTextField();
		textField.setColumns(10);
		
		JLabel lblNewLabel = new JLabel("Method name");
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		
		JButton btnConfigureInputs = new JButton("Configure Inputs...");
		
		JButton btnConfigureOutputs = new JButton("Configure Outputs...");
		
		JSeparator separator = new JSeparator();
		
		JLabel lblApplicationName = new JLabel("Application");
		
		JComboBox comboBox_1 = new JComboBox();
		
		XBayaLinkButton btnNewButton = new XBayaLinkButton("New button");
		btnNewButton.setHorizontalAlignment(SwingConstants.TRAILING);
		btnNewButton.setText("Create new application...");
		
		JSeparator separator_1 = new JSeparator();
		
		JLabel lblHost = new JLabel("Host");
		
		JComboBox comboBox_2 = new JComboBox();
		
		XBayaLinkButton blnkbtnCreateNewHost = new XBayaLinkButton("New button");
		blnkbtnCreateNewHost.setText("Create new host...");
		blnkbtnCreateNewHost.setHorizontalAlignment(SwingConstants.TRAILING);
		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
			gl_contentPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(22)
							.addComponent(lblRegistry, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(comboBox, 0, 317, Short.MAX_VALUE)
							.addGap(15))
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(19)
							.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_contentPanel.createSequentialGroup()
									.addComponent(btnConfigureInputs)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(btnConfigureOutputs))
								.addGroup(gl_contentPanel.createSequentialGroup()
									.addGroup(gl_contentPanel.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_contentPanel.createSequentialGroup()
											.addComponent(lblServiceName)
											.addGap(18))
										.addGroup(gl_contentPanel.createSequentialGroup()
											.addComponent(lblNewLabel)
											.addGap(17)))
									.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING, false)
										.addComponent(textField_1)
										.addComponent(textField, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))))
							.addGap(19)))
					.addGap(18))
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap(12, Short.MAX_VALUE)
					.addComponent(lblSeperator, GroupLayout.PREFERRED_SIZE, 425, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap(12, Short.MAX_VALUE)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(10)
							.addComponent(lblApplicationName)
							.addGap(26)
							.addComponent(comboBox_1, GroupLayout.PREFERRED_SIZE, 311, GroupLayout.PREFERRED_SIZE))
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, 425, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap(269, Short.MAX_VALUE)
					.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(24))
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap(14, Short.MAX_VALUE)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPanel.createSequentialGroup()
							.addGap(10)
							.addComponent(lblHost)
							.addGap(56)
							.addComponent(comboBox_2, GroupLayout.PREFERRED_SIZE, 310, GroupLayout.PREFERRED_SIZE))
						.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, 425, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addContainerGap(271, Short.MAX_VALUE)
					.addComponent(blnkbtnCreateNewHost, GroupLayout.PREFERRED_SIZE, 156, GroupLayout.PREFERRED_SIZE)
					.addGap(22))
		);
		gl_contentPanel.setVerticalGroup(
			gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblRegistry)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblSeperator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(8)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblServiceName)
						.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel)
						.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnConfigureOutputs)
						.addComponent(btnConfigureInputs))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(comboBox_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblApplicationName))
					.addGap(1)
					.addComponent(btnNewButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblHost)
						.addComponent(comboBox_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(blnkbtnCreateNewHost, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE)
					.addGap(13))
		);
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
			JPanel panel = new JPanel();
			GridBagConstraints gbc_panel = new GridBagConstraints();
			gbc_panel.anchor = GridBagConstraints.NORTHWEST;
			gbc_panel.gridx = 1;
			gbc_panel.gridy = 0;
			buttonPane.add(panel, gbc_panel);
			{
				JButton okButton = new JButton("Save");
				panel.add(okButton);
				okButton.setActionCommand("OK");
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				panel.add(cancelButton);
				cancelButton.setActionCommand("Cancel");
			}
		}
		
		
	}
}
