package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.model.GFacURL;

public class GFacURLNode extends AbstractAiravataTreeNode {
	private GFacURL gfacURL;
	
	public GFacURLNode(GFacURL gfacURL, TreeNode parent) {
		super(parent);
		setGfacURL(gfacURL);
	}

	@Override
	protected List<TreeNode> getChildren() {
		return emptyList();
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return getGfacURL().getGfacURL().toString();
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("registry.png");
	}

	public GFacURL getGfacURL() {
		return gfacURL;
	}

	public void setGfacURL(GFacURL gfacURL) {
		this.gfacURL = gfacURL;
	}

}
