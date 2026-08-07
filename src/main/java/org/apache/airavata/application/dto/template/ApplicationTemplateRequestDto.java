package org.apache.airavata.application.dto.template;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Create/update payload for an application template. Inputs and outputs are owned by
 * the template, so they are declared inline here rather than through separate endpoints;
 * an update replaces the previously declared sets.
 */
public class ApplicationTemplateRequestDto {

    @NotBlank(message = "Template name cannot be blank")
    private String templateName;

    private String templateDescription;

    @Valid
    private List<ApplicationTemplateInputDto> inputs;

    @Valid
    private List<ApplicationTemplateOutputDto> outputs;

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateDescription() {
        return templateDescription;
    }

    public void setTemplateDescription(String templateDescription) {
        this.templateDescription = templateDescription;
    }

    public List<ApplicationTemplateInputDto> getInputs() {
        return inputs;
    }

    public void setInputs(List<ApplicationTemplateInputDto> inputs) {
        this.inputs = inputs;
    }

    public List<ApplicationTemplateOutputDto> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ApplicationTemplateOutputDto> outputs) {
        this.outputs = outputs;
    }
}
