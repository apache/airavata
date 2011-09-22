package org.apache.airavata.xbaya.component.gui;

import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.ComponentRegistryLoader;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.gui.ErrorMessages;
import org.apache.airavata.xbaya.gui.GridPanel;
import org.apache.airavata.xbaya.gui.XBayaDialog;
import org.apache.airavata.xbaya.gui.XBayaLabel;
import org.apache.airavata.xbaya.gui.XBayaLinkButton;
import org.apache.airavata.xbaya.gui.XBayaTextField;

public class NewJCRRegistryUserDialog {

    private XBayaEngine engine;

    private ComponentRegistryLoader loader;
    
    private XBayaDialog dialog;

    private XBayaTextField urlTextField;
    
    private XBayaTextField usernameTextField;
    
    private JPasswordField passwordTextField;

	private XBayaLinkButton newUserButton;
	
	private String username;
	
	private URL url;
	
    public NewJCRRegistryUserDialog(XBayaEngine engine) {
    	this(engine,null);
    }
    
    public NewJCRRegistryUserDialog(XBayaEngine engine, URL url) {
    	this(engine,url,null);
    }
	
    /**
     * @param engine
     */
    public NewJCRRegistryUserDialog(XBayaEngine engine, URL url, String username) {
        this.engine = engine;
        this.loader = new ComponentRegistryLoader(engine);
        setUrl(url);
        setUsername(username);
        initGUI();
    }

    /**
     * Displays the dialog.
     */
    public void show() {
        this.dialog.show();
    }

    private void hide() {
        this.dialog.hide();
    }

    private void ok() {
        String urlString = this.urlTextField.getText();
        String username = this.usernameTextField.getText();
        String password = new String(this.passwordTextField.getPassword());

        if (urlString.length() == 0) {
            this.engine.getErrorWindow().error(ErrorMessages.URL_EMPTY);
            return;
        }
        URI url;
        try {
            url = new URI(urlString);
        } catch (URISyntaxException e) {
            this.engine.getErrorWindow().error(ErrorMessages.URL_WRONG, e);
            return;
        }

        JCRComponentRegistry registry = new JCRComponentRegistry(url, username, password);
        this.engine.getConfiguration().setJcrComponentRegistry(registry);
        hide();

        this.loader.load(registry);
    }

    private void createNewUser(){
    	
    }
    
    /**
     * Initializes the GUI.
     */
    private void initGUI() {
        this.urlTextField = new XBayaTextField();
        this.usernameTextField = new XBayaTextField();
        this.passwordTextField = new JPasswordField();
        XBayaLabel urlLabel = new XBayaLabel("URL", this.urlTextField);
        XBayaLabel nameLabel = new XBayaLabel("Username", this.usernameTextField);
        XBayaLabel passLabel = new XBayaLabel("Password", this.usernameTextField);
        
        this.newUserButton = new XBayaLinkButton("Create new user...");
        newUserButton.setHorizontalAlignment(XBayaLinkButton.RIGHT);
        JLabel emptyLabel = new JLabel("");
        
        newUserButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
            	createNewUser();
            }
        });
        
        GridPanel infoPanel = new GridPanel();
        infoPanel.add(urlLabel);
        infoPanel.add(this.urlTextField);
//		GridBagConstraints c = new GridBagConstraints();
//		c.fill = GridBagConstraints.HORIZONTAL;
//		c.gridwidth = 2;
//		c.gridx = 0;
//		c.gridy = 1;
//		infoPanel.getContentPanel().add(new JSeparator(SwingConstants.HORIZONTAL),c);
//		infoPanel.add(new JSeparator(SwingConstants.HORIZONTAL));
        infoPanel.add(nameLabel);
        infoPanel.add(this.usernameTextField);
        infoPanel.add(passLabel);
        infoPanel.add(this.passwordTextField);
        infoPanel.add(emptyLabel);
        infoPanel.add(this.newUserButton);
        infoPanel.layout(4, 2, GridPanel.WEIGHT_NONE, 1);
        
        JButton okButton = new JButton("OK");
        okButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hide();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        this.dialog = new XBayaDialog(this.engine, "JCR Registry", infoPanel, buttonPanel);
        this.dialog.setDefaultButton(okButton);
    }

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public URL getUrl() {
		return url;
	}

	public void setUrl(URL url) {
		this.url = url;
	}
}
