package org.apache.airavata.workflow.core;

import org.apache.airavata.workflow.core.dag.nodes.InputNode;

import java.util.List;

/**
 * Created by syodage on 2/1/16.
 */
public interface WorkflowBuilder {

    public List<InputNode> build() throws Exception;

    public List<InputNode> build(String workflow) throws Exception;
}
