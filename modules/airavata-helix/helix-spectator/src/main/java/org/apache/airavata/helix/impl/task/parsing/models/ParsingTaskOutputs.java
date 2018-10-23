package org.apache.airavata.helix.impl.task.parsing.models;

import java.util.ArrayList;
import java.util.List;

public class ParsingTaskOutputs {
    private List<ParsingTaskOutput> outputs = new ArrayList<>();

    public List<ParsingTaskOutput> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ParsingTaskOutput> outputs) {
        this.outputs = outputs;
    }

    public void addOutput(ParsingTaskOutput output) {
        outputs.add(output);
    }
}
