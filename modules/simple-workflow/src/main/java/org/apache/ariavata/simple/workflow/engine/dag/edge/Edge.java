package org.apache.ariavata.simple.workflow.engine.dag.edge;

import org.apache.airavata.model.appcatalog.appinterface.InputDataObjectType;
import org.apache.airavata.model.appcatalog.appinterface.OutputDataObjectType;
import org.apache.ariavata.simple.workflow.engine.dag.nodes.WorkflowNode;
import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;

/**
 * Created by shameera on 1/29/15.
 */
public interface Edge {

//    public WorkflowNode fromNode();

//    public WorkflowNode toNode();

/*    public InputDataObjectType getInputObject();

    public void setInputObject();

    public OutputDataObjectType getOutputObject();

    public void setOutputObject();*/

    public InPort getInPort();

    public void setInPort(InPort inPort);

    public OutPort getOutPort();

    public void setOutPort(OutPort outPort);


}
