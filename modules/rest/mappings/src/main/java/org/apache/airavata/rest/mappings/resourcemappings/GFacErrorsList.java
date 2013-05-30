package org.apache.airavata.rest.mappings.resourcemappings;

import org.apache.airavata.registry.api.workflow.ApplicationJobExecutionError;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class GFacErrorsList {
    private List<ApplicationJobExecutionError> gFacJobExecutionErrorList = new ArrayList<ApplicationJobExecutionError>();

    public List<ApplicationJobExecutionError> getgFacJobExecutionErrorList() {
        return gFacJobExecutionErrorList;
    }

    public void setgFacJobExecutionErrorList(List<ApplicationJobExecutionError> gFacJobExecutionErrorList) {
        this.gFacJobExecutionErrorList = gFacJobExecutionErrorList;
    }
}
