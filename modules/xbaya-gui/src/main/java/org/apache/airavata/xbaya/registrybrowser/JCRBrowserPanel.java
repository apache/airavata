package org.apache.airavata.xbaya.registrybrowser;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultTreeModel;

import org.apache.airavata.registry.api.Registry;
import org.apache.airavata.xbaya.XBayaEngine;
import org.apache.airavata.xbaya.component.registry.JCRComponentRegistry;
import org.apache.airavata.xbaya.registrybrowser.nodes.AbstractAiravataTreeNode;
import org.apache.airavata.xbaya.registrybrowser.nodes.AiravataTreeNodeFactory;
import org.apache.airavata.xbaya.registrybrowser.nodes.RegistryTreeCellRenderer;

public class JCRBrowserPanel extends JPanel implements Observer{

	private XBayaEngine engine;
	private JTree tree;
	private JPopupMenu popupMenu;
	
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
//				tree.addMouseListener(new MouseAdapter() {
//					@Override
//					public void mousePressed(MouseEvent e) {
//						int selRow = tree.getRowForLocation(e.getX(), e.getY());
//						
//				         if(selRow != -1 && e.isPopupTrigger()) {
//				        	 tree.setSelectionRow(selRow);
//				        	 popupMenu.show((Component)e.getSource(),e.getX(), e.getY());
//				         }
//					}
//				});
				tree.setCellRenderer(new RegistryTreeCellRenderer());
				scrollPane.setViewportView(tree);
				
				popupMenu = new JPopupMenu();
				popupMenu.setLabel("");
				addPopup(tree, popupMenu);
				
				JMenuItem mntmRefresh = new JMenuItem("Refresh");
				mntmRefresh.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						Object o = tree.getLastSelectedPathComponent();
						if (o instanceof AbstractAiravataTreeNode){
							AbstractAiravataTreeNode node=((AbstractAiravataTreeNode)o);
							node.refresh();
//							((DefaultTreeModel)tree.getModel()).nodeChanged(node);
							((DefaultTreeModel)tree.getModel()).reload(node);
						}
					}
				});
				popupMenu.add(mntmRefresh);
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
	
	private void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				int selRow = tree.getRowForLocation(e.getX(), e.getY());
				if(selRow != -1 && e.isPopupTrigger()) {
					tree.setSelectionRow(selRow);
					popup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
	}
}
