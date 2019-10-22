package org.apache.airavata.helix.impl.task.parsing.models;

import com.google.gson.Gson;
import org.apache.airavata.helix.task.api.TaskParamType;

import java.util.ArrayList;
import java.util.List;

public class ParsingTaskInputs implements TaskParamType {

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

    @Override
    public String serialize() {
        return new Gson().toJson(this);
    }

    @Override
    public void deserialize(String content) {
        ParsingTaskInputs parsingTaskInputs = new Gson().fromJson(content, ParsingTaskInputs.class);
        this.inputs = parsingTaskInputs.getInputs();
    }
}
