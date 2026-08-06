package org.apache.airavata.application.model.template;

import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;

@Entity
public class ApplicationTemplateOutputEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String outputId;

    @ManyToOne
    @JoinColumn(name = "template_id", foreignKey = @ForeignKey(name = "fk_output_template"))
    private ApplicationTemplateEntity applicationTemplate;

    private ApplicationTemplateOutputType outputType;

    private String outputName;
    private String displayName;
    private String outputDescription;

    public String getOutputId() {
        return outputId;
    }

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }

    public ApplicationTemplateEntity getApplicationTemplate() {
        return applicationTemplate;
    }

    public void setApplicationTemplate(ApplicationTemplateEntity applicationTemplate) {
        this.applicationTemplate = applicationTemplate;
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
