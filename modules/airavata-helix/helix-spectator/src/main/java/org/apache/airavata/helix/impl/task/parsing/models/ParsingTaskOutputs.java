package org.apache.airavata.helix.impl.task.parsing.models;

import com.google.gson.Gson;
import org.apache.airavata.helix.task.api.TaskParamType;

import java.util.ArrayList;
import java.util.List;

public class ParsingTaskOutputs implements TaskParamType {
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

    @Override
    public String serialize() {
        return new Gson().toJson(this);
    }

    @Override
    public void deserialize(String content) {
        ParsingTaskOutputs parsingTaskOutputs = new Gson().fromJson(content, ParsingTaskOutputs.class);
        this.outputs = parsingTaskOutputs.getOutputs();
    }
}
