package org.apache.airavata.xbaya.appwrapper;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import org.apache.airavata.xbaya.gui.XBayaLinkButton;
import javax.swing.SwingConstants;
import javax.swing.LayoutStyle.ComponentPlacement;

public class ApplicationDescriptionDialog extends JDialog {
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ApplicationDescriptionDialog dialog = new ApplicationDescriptionDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ApplicationDescriptionDialog() {
		setTitle("New Application Description");
		setBounds(100, 100, 441, 240);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Save");
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			JLabel label = new JLabel("Registry");
			JComboBox comboBox = new JComboBox();
			JLabel lblApplicationName = new JLabel("Application name");
			JLabel lblExecutablePatyh = new JLabel("Executable path");
			textField = new JTextField();
			textField.setColumns(10);
			textField_1 = new JTextField();
			textField_1.setColumns(10);
			JSeparator separator = new JSeparator();
			JSeparator separator_1 = new JSeparator();
			JLabel lblTemporaryDirectory = new JLabel("Temporary directory");
			textField_2 = new JTextField();
			textField_2.setColumns(10);
			JButton btnAdvance = new JButton("Advanced options...");
			GroupLayout gl_panel = new GroupLayout(panel);
			gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.TRAILING)
					.addGroup(gl_panel.createSequentialGroup()
						.addGap(22)
						.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
							.addGroup(gl_panel.createSequentialGroup()
								.addComponent(label, GroupLayout.PREFERRED_SIZE, 65, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(comboBox, 0, 341, Short.MAX_VALUE)
								.addGap(15))
							.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
									.addComponent(lblApplicationName)
									.addComponent(lblExecutablePatyh))
								.addGap(18)
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
									.addComponent(textField)
									.addComponent(textField_1, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))
								.addGap(19)))
						.addGap(18))
					.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
						.addContainerGap()
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, 425, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(36, Short.MAX_VALUE))
					.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
							.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, 425, GroupLayout.PREFERRED_SIZE)
							.addGroup(gl_panel.createSequentialGroup()
								.addGap(10)
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
									.addComponent(btnAdvance)
									.addGroup(gl_panel.createSequentialGroup()
										.addComponent(lblTemporaryDirectory)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, 305, GroupLayout.PREFERRED_SIZE)))))
						.addGap(36))
			);
			gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(label)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(8)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblApplicationName))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblExecutablePatyh))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(lblTemporaryDirectory)
							.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnAdvance)
						.addGap(109))
			);
			gl_panel.setAutoCreateGaps(true);
			gl_panel.setAutoCreateContainerGaps(true);
			panel.setLayout(gl_panel);
		}
	}

}
