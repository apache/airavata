package org.apache.airavata.xbaya.registrybrowser.nodes;

import java.util.List;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.model.GFacURLs;

public class GFacURLsNode extends AbstractAiravataTreeNode {
	private GFacURLs gfacURLs;
	public GFacURLsNode(GFacURLs gfacURLs, TreeNode parent) {
		super(parent);
		setGfacURLs(gfacURLs);
	}
	@Override
	protected List<TreeNode> getChildren() {
		return getTreeNodeList(getGfacURLs().getURLS().toArray(),this);
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "GFac Locations";
	}

	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("gfac_urls.png");
	}
	public GFacURLs getGfacURLs() {
		return gfacURLs;
	}
	public void setGfacURLs(GFacURLs gfacURLs) {
		this.gfacURLs = gfacURLs;
	}

}
