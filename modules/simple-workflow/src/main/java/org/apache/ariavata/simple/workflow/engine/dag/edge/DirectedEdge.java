package org.apache.ariavata.simple.workflow.engine.dag.edge;

import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.OutPort;


public class DirectedEdge implements Edge {

    private InPort inPort;
    private OutPort outPort;

    @Override
    public InPort getToPort() {
        return inPort;
    }

    @Override
    public void setToPort(InPort inPort) {
        this.inPort = inPort;
    }

    @Override
    public OutPort getFromPort() {
        return outPort;
    }

    @Override
    public void setFromPort(OutPort outPort) {
        this.outPort = outPort;
    }
}
