package org.apache.airavata.application.dto.template;

import java.util.List;

/**
 * Read view of an application template. Deployments are not inlined — they are a
 * separate aggregate, reachable via {@code GET /api/v1/slurm-deployments?templateId=...}.
 */
public class ApplicationTemplateResponseDto {

    private String templateId;
    private String templateName;
    private String templateDescription;
    private List<ApplicationTemplateInputDto> inputs;
    private List<ApplicationTemplateOutputDto> outputs;

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

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
