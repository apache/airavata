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
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import javax.swing.JScrollPane;

public class ApplicationDescriptionAdvancedOptionDialog extends JDialog {
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private JTextField textField_5;
	private JTable table;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ApplicationDescriptionAdvancedOptionDialog dialog = new ApplicationDescriptionAdvancedOptionDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ApplicationDescriptionAdvancedOptionDialog() {
		setTitle("Application Description Advance Options");
		setBounds(100, 100, 601, 284);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
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
			JLabel lblWorkingDirectory = new JLabel("Working Directory");
			JLabel lblInputDirectory = new JLabel("Input directory");
			textField = new JTextField();
			textField.setColumns(10);
			textField_1 = new JTextField();
			textField_1.setColumns(10);
			JLabel lblLocations = new JLabel("Locations");
			lblLocations.setFont(new Font("Tahoma", Font.BOLD, 11));
			
			textField_2 = new JTextField();
			textField_2.setColumns(10);
			
			JLabel lblOutputDirectory = new JLabel("Output directory");
			
			JSeparator separator = new JSeparator();
			
			JLabel label = new JLabel("Program data");
			label.setFont(new Font("Tahoma", Font.BOLD, 11));
			
			JLabel lblStdin = new JLabel("STDIN");
			lblStdin.setHorizontalAlignment(SwingConstants.TRAILING);
			
			textField_3 = new JTextField();
			textField_3.setColumns(10);
			
			JLabel lblStdout = new JLabel("STDOUT");
			lblStdout.setHorizontalAlignment(SwingConstants.TRAILING);
			
			textField_4 = new JTextField();
			textField_4.setColumns(10);
			
			JLabel lblStderr = new JLabel("STDERR");
			lblStderr.setHorizontalAlignment(SwingConstants.TRAILING);
			
			textField_5 = new JTextField();
			textField_5.setColumns(10);
			
			JSeparator separator_1 = new JSeparator();
			separator_1.setOrientation(SwingConstants.VERTICAL);
			
			JLabel lblEnvironmentalVariables = new JLabel("Environmental Variables");
			lblEnvironmentalVariables.setFont(new Font("Tahoma", Font.BOLD, 11));
			
			JScrollPane scrollPane = new JScrollPane();
			GroupLayout gl_panel = new GroupLayout(panel);
			gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
							.addGroup(gl_panel.createSequentialGroup()
								.addComponent(lblLocations, GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE)
								.addGap(135))
							.addGroup(gl_panel.createSequentialGroup()
								.addComponent(label, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
								.addGap(215))
							.addGroup(gl_panel.createSequentialGroup()
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
									.addGroup(gl_panel.createSequentialGroup()
										.addComponent(lblStderr, GroupLayout.PREFERRED_SIZE, 80, GroupLayout.PREFERRED_SIZE)
										.addGap(18)
										.addComponent(textField_5, GroupLayout.PREFERRED_SIZE, 181, GroupLayout.PREFERRED_SIZE))
									.addGroup(gl_panel.createSequentialGroup()
										.addComponent(lblStdout, GroupLayout.PREFERRED_SIZE, 72, GroupLayout.PREFERRED_SIZE)
										.addGap(18)
										.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, 181, GroupLayout.PREFERRED_SIZE))
									.addGroup(gl_panel.createSequentialGroup()
										.addComponent(lblStdin, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
										.addGap(18)
										.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, 181, GroupLayout.PREFERRED_SIZE)))
								.addPreferredGap(ComponentPlacement.UNRELATED))
							.addComponent(separator, GroupLayout.PREFERRED_SIZE, 293, GroupLayout.PREFERRED_SIZE)
							.addGroup(gl_panel.createSequentialGroup()
								.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
									.addGroup(gl_panel.createSequentialGroup()
										.addComponent(lblWorkingDirectory)
										.addGap(18))
									.addGroup(gl_panel.createSequentialGroup()
										.addComponent(lblInputDirectory)
										.addGap(17))
									.addGroup(gl_panel.createSequentialGroup()
										.addComponent(lblOutputDirectory)
										.addGap(18)))
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
									.addComponent(textField_2)
									.addComponent(textField)
									.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE))))
						.addGap(2)
						.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
							.addComponent(lblEnvironmentalVariables)
							.addGroup(gl_panel.createSequentialGroup()
								.addGap(10)
								.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 258, GroupLayout.PREFERRED_SIZE)))
						.addGap(411))
			);
			gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
							.addComponent(separator_1, GroupLayout.PREFERRED_SIZE, 212, GroupLayout.PREFERRED_SIZE)
							.addGroup(gl_panel.createSequentialGroup()
								.addComponent(lblLocations)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblWorkingDirectory)
									.addComponent(textField_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblInputDirectory)
									.addComponent(textField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(textField_2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
									.addComponent(lblOutputDirectory))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(separator, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(label)
								.addGap(3)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblStdin)
									.addComponent(textField_3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(9)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblStdout)
									.addComponent(textField_4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addGap(9)
								.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
									.addComponent(lblStderr)
									.addComponent(textField_5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
							.addGroup(gl_panel.createSequentialGroup()
								.addComponent(lblEnvironmentalVariables)
								.addPreferredGap(ComponentPlacement.RELATED)
								.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 179, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(29, Short.MAX_VALUE))
			);
			
			table = new JTable();
			table.setFillsViewportHeight(true);
			scrollPane.setViewportView(table);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.setCellSelectionEnabled(true);
			table.setColumnSelectionAllowed(true);
			table.setModel(new DefaultTableModel(
				new Object[][] {
					{null, null},
				},
				new String[] {
					"Name", "Value"
				}
			) {
				Class[] columnTypes = new Class[] {
					String.class, String.class
				};
				public Class getColumnClass(int columnIndex) {
					return columnTypes[columnIndex];
				}
			});
			table.getColumnModel().getColumn(0).setPreferredWidth(67);
			table.getColumnModel().getColumn(1).setPreferredWidth(158);
			gl_panel.setAutoCreateGaps(true);
			gl_panel.setAutoCreateContainerGaps(true);
			panel.setLayout(gl_panel);
		}
	}
}
