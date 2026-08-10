package org.apache.airavata.application.model.template;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
import org.apache.airavata.application.model.deployment.BatchApplicationDeploymentEntity;

@Entity
public class ApplicationTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String templateId;
    private String templateName;
    private String templateDescription;

    // Inputs and outputs are owned by the template: they are created, replaced and
    // deleted with it. Deployments are not — they outlive individual edits and are
    // managed through their own endpoints.
    @OneToMany(mappedBy = "applicationTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationTemplateInputEntity> inputs = new ArrayList<>();

    @OneToMany(mappedBy = "applicationTemplate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationTemplateOutputEntity> outputs = new ArrayList<>();

    @OneToMany(mappedBy = "applicationTemplate")
    private List<BatchApplicationDeploymentEntity> deployments;

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

    public List<ApplicationTemplateInputEntity> getInputs() {
        return inputs;
    }

    public void setInputs(List<ApplicationTemplateInputEntity> inputs) {
        this.inputs = inputs;
    }

    public List<ApplicationTemplateOutputEntity> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<ApplicationTemplateOutputEntity> outputs) {
        this.outputs = outputs;
    }

    public List<BatchApplicationDeploymentEntity> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<BatchApplicationDeploymentEntity> deployments) {
        this.deployments = deployments;
    }
}
