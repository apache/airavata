package org.apache.ariavata.simple.workflow.engine.dag.edge;

import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;
import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;

/**
 * Edge is a link to one node to another, basically edge should have outPort of a workflow node ,
 * which is starting point and inPort of a workflow node, which is end point of the edge.
 */

public interface Edge {

    public InPort getToPort();

    public void setToPort(InPort inPort);

    public OutPort getFromPort();

    public void setFromPort(OutPort outPort);


}
