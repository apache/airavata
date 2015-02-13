package org.apache.ariavata.simple.workflow.engine.dag.edge;

import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;


public class DirectedEdge implements Edge {

    private InPort inPort;
    private OutPort outPort;

    @Override
    public InPort getToPort() {
        return null; // TODO: Auto generated method body.
    }

    @Override
    public void setToPort(InPort inPort) {
        // TODO: Auto generated method body.
    }

    @Override
    public OutPort getFromPort() {
        return null; // TODO: Auto generated method body.
    }

    @Override
    public void setFromPort(OutPort outPort) {
        // TODO: Auto generated method body.
    }
}
