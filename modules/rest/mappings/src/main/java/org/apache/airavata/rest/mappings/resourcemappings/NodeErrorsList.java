package org.apache.airavata.rest.mappings.resourcemappings;

import org.apache.airavata.registry.api.workflow.NodeExecutionError;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class NodeErrorsList {
    private List<NodeExecutionError> nodeExecutionErrorList = new ArrayList<NodeExecutionError>();

    public List<NodeExecutionError> getNodeExecutionErrorList() {
        return nodeExecutionErrorList;
    }

    public void setNodeExecutionErrorList(List<NodeExecutionError> nodeExecutionErrorList) {
        this.nodeExecutionErrorList = nodeExecutionErrorList;
    }
}
