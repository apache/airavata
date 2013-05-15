package org.apache.airavata.rest.mappings.resourcemappings;

import org.apache.airavata.registry.api.workflow.ExecutionError;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ExecutionErrorsList {
    private List<ExecutionError> executionErrors = new ArrayList<ExecutionError>();

    public List<ExecutionError> getExecutionErrors() {
        return executionErrors;
    }

    public void setExecutionErrors(List<ExecutionError> executionErrors) {
        this.executionErrors = executionErrors;
    }
}
