package org.apache.airavata.xbaya.registrybrowser;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.airavata.xbaya.XBayaEngine;

public class JCRBrowserDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2866874255829295553L;
	private JPanel contentPanel = new JPanel();
	private XBayaEngine engine;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			JCRBrowserDialog dialog = new JCRBrowserDialog(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public JCRBrowserDialog(XBayaEngine engine) {
		setEngine(engine);
		initGUI();
	}

	private void initGUI() {
		setModal(true);
		setLocationRelativeTo(null);
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel=new JCRBrowserPanel(getEngine());
		getContentPane().add(contentPanel, BorderLayout.CENTER);
//		contentPanel.setLayout(new BorderLayout(0, 0));
//		{
//			JScrollPane scrollPane = new JScrollPane();
//			contentPanel.add(scrollPane, BorderLayout.CENTER);
//			{
//				JTree tree = new JTree(AiravataTreeNodeFactory.getTreeNode(getJCRRegistry(),null));
//				tree.setCellRenderer(new RegistryTreeCellRenderer());
//				scrollPane.setViewportView(tree);
//			}
//		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Close");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						close();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

	public void close() {
		setVisible(false);
	}

	public void open() {
		setVisible(true);
	}
	
	public XBayaEngine getEngine() {
		return engine;
	}

	public void setEngine(XBayaEngine engine) {
		this.engine = engine;
	}

}
