package org.apache.airavata.rest.mappings.resourcemappings;

import org.apache.airavata.registry.api.workflow.GFacJobExecutionError;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class GFacErrorsList {
    private List<GFacJobExecutionError> gFacJobExecutionErrorList = new ArrayList<GFacJobExecutionError>();

    public List<GFacJobExecutionError> getgFacJobExecutionErrorList() {
        return gFacJobExecutionErrorList;
    }

    public void setgFacJobExecutionErrorList(List<GFacJobExecutionError> gFacJobExecutionErrorList) {
        this.gFacJobExecutionErrorList = gFacJobExecutionErrorList;
    }
}
