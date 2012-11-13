package org.apache.airavata.services.registry.rest.resourcemappings;



import org.apache.airavata.registry.api.workflow.WorkflowNodeIOData;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class WorkflowNodeIODataList {
    WorkflowNodeIOData[] workflowNodeIOData = new WorkflowNodeIOData[]{};

    public WorkflowNodeIOData[] getWorkflowNodeIOData() {
        return workflowNodeIOData;
    }

    public void setWorkflowNodeIOData(WorkflowNodeIOData[] workflowNodeIOData) {
        this.workflowNodeIOData = workflowNodeIOData;
    }
}
