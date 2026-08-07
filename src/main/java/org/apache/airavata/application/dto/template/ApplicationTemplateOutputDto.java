package org.apache.airavata.application.dto.template;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.airavata.application.model.template.ApplicationTemplateOutputType;

public class ApplicationTemplateOutputDto {

    /** Server assigned; ignored on create/update requests. */
    private String outputId;

    @NotBlank(message = "Output name cannot be blank")
    private String outputName;

    private String displayName;
    private String outputDescription;

    @NotNull(message = "Output type cannot be null")
    private ApplicationTemplateOutputType outputType;

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }

    public String getOutputName() {
        return outputName;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getOutputDescription() {
        return outputDescription;
    }

    public void setOutputDescription(String outputDescription) {
        this.outputDescription = outputDescription;
    }

    public ApplicationTemplateOutputType getOutputType() {
        return outputType;
    }

    public void setOutputType(ApplicationTemplateOutputType outputType) {
        this.outputType = outputType;
    }
}
