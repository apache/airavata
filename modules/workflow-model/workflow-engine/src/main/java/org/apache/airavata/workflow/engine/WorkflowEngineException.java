package org.apache.airavata.workflow.engine;

public class WorkflowEngineException extends Exception{
    private static final long serialVersionUID = -2849422320139467602L;

    public WorkflowEngineException(Throwable e) {
        super(e);
    }

    public WorkflowEngineException(String message) {
        super(message, null);
    }

    public WorkflowEngineException(String message, Throwable e) {
        super(message, e);
    }
}
