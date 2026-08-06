package org.apache.airavata.application.model.template;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_template_input_name", columnNames = { "template_id",
        "input_name" }))
public class ApplicationTemplateInputEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String inputId;

    @ManyToOne
    @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "fk_input_template"))
    private ApplicationTemplateEntity applicationTemplate;

    private String inputName;
    private String displayName;
    private String inputDescription;
    private ApplicationTemplateInputType inputType;
    private boolean isRequired;

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

    public ApplicationTemplateEntity getApplicationTemplate() {
        return applicationTemplate;
    }

    public void setApplicationTemplate(ApplicationTemplateEntity applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
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
        return isRequired;
    }

    public void setRequired(boolean required) {
        isRequired = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
