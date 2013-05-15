package org.apache.airavata.rest.mappings.resourcemappings;

import org.apache.airavata.registry.api.workflow.ExperimentExecutionError;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class ExperimentErrorsList {
    private List<ExperimentExecutionError> experimentExecutionErrorList = new ArrayList<ExperimentExecutionError>();

    public List<ExperimentExecutionError> getExperimentExecutionErrorList() {
        return experimentExecutionErrorList;
    }

    public void setExperimentExecutionErrorList(List<ExperimentExecutionError> experimentExecutionErrorList) {
        this.experimentExecutionErrorList = experimentExecutionErrorList;
    }
}
