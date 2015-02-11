package org.apache.ariavata.simple.workflow.engine;

/**
 * All classes implement this WorkflowFactory interface, should be abstract or singleton.
 */
public interface WorkflowFactory {

    public WorkflowParser getWorkflowParser();

}
