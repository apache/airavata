package org.apache.airavata.application.dto.template;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.apache.airavata.application.model.template.ApplicationTemplateInputType;

public class ApplicationTemplateInputDto {

    /** Server assigned; ignored on create/update requests. */
    private String inputId;

    @NotBlank(message = "Input name cannot be blank")
    private String inputName;

    private String displayName;
    private String inputDescription;

    @NotNull(message = "Input type cannot be null")
    private ApplicationTemplateInputType inputType;

    private boolean required;

    /**
     * Default input as JSON string. For example:
     * - For a single value: {"value": "defaultValue"}
     * - For a list of values: {"values": ["defaultValue1", "defaultValue2"]}
     */
    private String defaultValue;

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getInputDescription() {
        return inputDescription;
    }

    public void setInputDescription(String inputDescription) {
        this.inputDescription = inputDescription;
    }

    public ApplicationTemplateInputType getInputType() {
        return inputType;
    }

    public void setInputType(ApplicationTemplateInputType inputType) {
        this.inputType = inputType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
