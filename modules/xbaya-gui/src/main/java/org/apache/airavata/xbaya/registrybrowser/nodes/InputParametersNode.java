package org.apache.airavata.xbaya.registrybrowser.nodes;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.model.InputParameters;

public class InputParametersNode extends ParametersNode {

	public InputParametersNode(InputParameters parameters, TreeNode parent) {
		super(parameters, parent);
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "Input";
	}
	
	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("input_para.png");
	}
}
