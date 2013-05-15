package org.apache.airavata.rest.mappings.resourcemappings;

import org.apache.airavata.registry.api.workflow.WorkflowExecutionError;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class WorkflowErrorsList {
    private List<WorkflowExecutionError> workflowExecutionErrorList = new ArrayList<WorkflowExecutionError>();

    public List<WorkflowExecutionError> getWorkflowExecutionErrorList() {
        return workflowExecutionErrorList;
    }

    public void setWorkflowExecutionErrorList(List<WorkflowExecutionError> workflowExecutionErrorList) {
        this.workflowExecutionErrorList = workflowExecutionErrorList;
    }
}
