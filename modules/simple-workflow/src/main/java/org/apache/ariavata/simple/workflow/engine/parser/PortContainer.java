package org.apache.ariavata.simple.workflow.engine.parser;

import org.apache.airavata.workflow.model.graph.DataPort;
import org.apache.ariavata.simple.workflow.engine.dag.port.InPort;


public class PortContainer {
    private DataPort dataPort;
    private InPort inPort;


    public PortContainer(DataPort dataPort, InPort inPort) {
        this.dataPort = dataPort;
        this.inPort = inPort;
    }

    public DataPort getDataPort() {
        return dataPort;
    }

    public void setDataPort(DataPort dataPort) {
        this.dataPort = dataPort;
    }

    public InPort getInPort() {
        return inPort;
    }

    public void setInPort(InPort inPort) {
        this.inPort = inPort;
    }
}
