package org.apache.airavata.xbaya.registrybrowser.nodes;

import javax.swing.Icon;
import javax.swing.tree.TreeNode;

import org.apache.airavata.common.utils.SwingUtil;
import org.apache.airavata.xbaya.registrybrowser.model.OutputParameters;

public class OutputParametersNode extends ParametersNode {

	public OutputParametersNode(OutputParameters parameters, TreeNode parent) {
		super(parameters, parent);
	}

	@Override
	public String getCaption(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return "Output";
	}
	
	@Override
	public Icon getIcon(boolean selected, boolean expanded, boolean leaf,
			boolean hasFocus) {
		return SwingUtil.createImageIcon("output_para.png");
	}
}
