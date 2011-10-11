package org.apache.airavata.xbaya.appwrapper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.regex.Pattern;

import javax.jcr.PathNotFoundException;
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

import org.apache.airavata.commons.gfac.type.HostDescription;
import org.apache.airavata.registry.api.exception.HostDescriptionRetrieveException;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;

public class HostDescriptionDialog extends JDialog {

	private static final long serialVersionUID = 1423293834766468324L;
	private JTextField txtHostLocation;
	private JTextField txtHostName;
	private XBayaEngine engine;
	private HostDescription hostDescription;
	
	private String hostName;
	private JButton okButton;
	private boolean hostCreated=false;
	private JLabel lblError;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			HostDescriptionDialog dialog = new HostDescriptionDialog(null);
			dialog.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void open(){
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setVisible(true);
	}
	
	protected HostDescriptionDialog getDialog(){
		return this;
	}
	/**
	 * Create the dialog.
	 */
	public HostDescriptionDialog(XBayaEngine engine) {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowOpened(WindowEvent arg0) {
				String baseName="Host";
				int i=1;
				String defaultName=baseName+i;
				try {
					while(getJCRComponentRegistry().getHostDescription(defaultName)!=null){
						defaultName=baseName+(++i);
					}
				} catch (HostDescriptionRetrieveException e) {
				} catch (PathNotFoundException e) {
				}
				txtHostName.setText(defaultName);
				setHostName(txtHostName.getText());
			}
		});
		setEngine(engine);
		initGUI();
	}

	private void initGUI() {
		setTitle("New Host Description");
		setBounds(100, 100, 448, 129);
		setModal(true);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Save");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						saveHostDescription();
						close();
					}
				});
				
				lblError = new JLabel("");
				lblError.setForeground(Color.RED);
				buttonPane.add(lblError);
				okButton.setEnabled(false);
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setHostCreated(false);
						close();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			JLabel label = new JLabel("Registry");
			label.setVisible(false);
			JComboBox comboBox = new JComboBox();
			comboBox.setVisible(false);
			JLabel lblHostName = new JLabel("Host name");
			JLabel lblHostLocationip = new JLabel("Host location/ip");
			txtHostLocation = new JTextField();
			txtHostLocation.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					setHostLocation(txtHostLocation.getText());
				}
			});
			txtHostLocation.setColumns(10);
			txtHostName = new JTextField();
			txtHostName.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(KeyEvent e) {
					setHostName(txtHostName.getText());
				}
			});
			txtHostName.setColumns(10);
			JSeparator separator = new JSeparator();
			separator.setVisible(false);
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
									.addComponent(lblHostName)
									.addComponent(lblHostLocationip))
								.addGap(18)
								.addGroup(gl_panel.createParallelGroup(Alignment.LEADING, false)
									.addComponent(txtHostLocation)
									.addComponent(txtHostName, GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE))
								.addGap(19)))
						.addGap(18))
					.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
						.addContainerGap()
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, 425, GroupLayout.PREFERRED_SIZE)
						.addContainerGap(36, Short.MAX_VALUE))
			);
			gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
					.addGap(0, 180, Short.MAX_VALUE)
					.addGroup(gl_panel.createSequentialGroup()
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(label)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addGap(8)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(txtHostName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblHostName))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
							.addComponent(txtHostLocation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addComponent(lblHostLocationip))
						.addGap(176))
			);
			gl_panel.setAutoCreateGaps(true);
			gl_panel.setAutoCreateContainerGaps(true);
			panel.setLayout(gl_panel);
		}
	}

	public XBayaEngine getEngine() {
		return engine;
	}

	public void setEngine(XBayaEngine engine) {
		this.engine = engine;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
		updateDialogStatus();
	}

	public String getHostLocation() {
		return getHostDescription().getName();
	}

	public void setHostLocation(String hostLocation) {
		getHostDescription().setName(hostLocation);
		updateDialogStatus();
	}

	private void validateDialog() throws Exception{
		if (getHostName()==null || getHostName().trim().equals("")){
			throw new Exception("Name of the host cannot be empty!!!");
		}
		
		HostDescription hostDescription2=null;
		try {
			hostDescription2 = getJCRComponentRegistry().getHostDescription(Pattern.quote(getHostName()));
		} catch (PathNotFoundException e) {
			//what we want
		} catch (Exception e){
			throw e;
		}
		if (hostDescription2!=null){
			throw new Exception("Host descriptor with the given name already exists!!!");
		}
		
		if (getHostLocation()==null || getHostLocation().trim().equals("")){
			throw new Exception("Host location/ip cannot be empty!!!");
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

	public boolean isHostCreated() {
		return hostCreated;
	}

	public void setHostCreated(boolean hostCreated) {
		this.hostCreated = hostCreated;
	}

	public HostDescription getHostDescription() {
		if (hostDescription==null){
			hostDescription=new HostDescription();
		}
		return hostDescription;
	}

	public void saveHostDescription() {
		getJCRComponentRegistry().saveHostDescription(getHostName(), getHostDescription());
		setHostCreated(true);
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
}
