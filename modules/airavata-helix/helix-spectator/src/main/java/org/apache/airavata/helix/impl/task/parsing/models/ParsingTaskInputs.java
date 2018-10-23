package org.apache.airavata.helix.impl.task.parsing.models;

import java.util.ArrayList;
import java.util.List;

public class ParsingTaskInputs {

    private List<ParsingTaskInput> inputs = new ArrayList<>();

    public List<ParsingTaskInput> getInputs() {
        return inputs;
    }

    public void setInputs(List<ParsingTaskInput> inputs) {
        this.inputs = inputs;
    }

    public void addInput(ParsingTaskInput input) {
        this.inputs.add(input);
    }
}
