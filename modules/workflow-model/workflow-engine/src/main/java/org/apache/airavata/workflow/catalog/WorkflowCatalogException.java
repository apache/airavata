package org.apache.airavata.workflow.catalog;

public class WorkflowCatalogException extends Exception{
    private static final long serialVersionUID = -2849422320139467602L;

    public WorkflowCatalogException(Throwable e) {
        super(e);
    }

    public WorkflowCatalogException(String message) {
        super(message, null);
    }

    public WorkflowCatalogException(String message, Throwable e) {
        super(message, e);
    }
}
