package org.airavata.appcatalog.cpi;

import java.util.List;

public interface WorkflowCatalog {

    public List<String> getAllWorkflows() throws AppCatalogException;

    public org.apache.airavata.model.Workflow getWorkflow(String workflowTemplateId) throws AppCatalogException;

    public void deleteWorkflow(String workflowTemplateId) throws AppCatalogException;

    public String registerWorkflow(org.apache.airavata.model.Workflow workflow) throws AppCatalogException;

    public void updateWorkflow(String workflowTemplateId, org.apache.airavata.model.Workflow workflow) throws AppCatalogException;

    public String getWorkflowTemplateId(String workflowName) throws AppCatalogException;

    public boolean isWorkflowExistWithName(String workflowName) throws AppCatalogException;
}
