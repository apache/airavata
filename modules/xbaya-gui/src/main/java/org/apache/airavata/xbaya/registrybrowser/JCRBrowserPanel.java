package org.apache.airavata.xbaya.registrybrowser;

import java.awt.BorderLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeModel;

import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.registrybrowser.nodes.AiravataTreeNodeFactory;
import org.apache.airavata.xbaya.registrybrowser.nodes.RegistryTreeCellRenderer;

public class JCRBrowserPanel extends JPanel implements Observer{

	private XBayaEngine engine;
	private JTree tree;
	
	/**
	 * Create the dialog.
	 */
	public JCRBrowserPanel(XBayaEngine engine) {
		setEngine(engine);
		initGUI();
	}

	private void initGUI() {
		setBounds(100, 100, 450, 300);
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		this.setLayout(new BorderLayout(0, 0));
		{
			JScrollPane scrollPane = new JScrollPane();
			this.add(scrollPane, BorderLayout.CENTER);
			{
				tree = new JTree(AiravataTreeNodeFactory.getTreeNode(getJCRRegistry()==null?"No registry specified":getJCRRegistry(),null));
				tree.setCellRenderer(new RegistryTreeCellRenderer());
				scrollPane.setViewportView(tree);
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
		if (this.engine!=null) {
			this.engine.getConfiguration().deleteObserver(this);
		}
		this.engine = engine;
		if (this.engine!=null) {
			this.engine.getConfiguration().addObserver(this);
		}
	}

	private Registry getJCRRegistry(){
		try {
			return getEngine().getConfiguration().getJcrComponentRegistry().getRegistry();
		} catch (Exception e) {
			//JCR registry not specified yet
			return null;
		}
	}

	@Override
	public void update(Observable observable, Object o) {
		if (getEngine().getConfiguration()==observable){
			if (o instanceof JCRComponentRegistry){
				resetModel();
			} else if (o instanceof Registry){
				resetModel();
			}
		}
	}

	private void resetModel() {
		tree.setModel(new DefaultTreeModel(AiravataTreeNodeFactory.getTreeNode(getJCRRegistry()==null?"No registry specified":getJCRRegistry(),null)));
	}
	
}
