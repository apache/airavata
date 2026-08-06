package org.apache.airavata.application.model.template;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToMany;
import java.util.List;
import org.apache.airavata.application.model.deployment.SlurmApplicationDeploymentEntity;

@Entity
public class ApplicationTemplateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String templateId;
    private String templateName;
    private String templateDescription;

    @OneToMany(mappedBy = "applicationTemplate")
    private List<ApplicationTemplateInputEntity> inputs;

    @OneToMany(mappedBy = "applicationTemplate")
    private List<ApplicationTemplateOutputEntity> outputs;

    @OneToMany(mappedBy = "applicationTemplate")
    private List<SlurmApplicationDeploymentEntity> deployments;

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

    public List<SlurmApplicationDeploymentEntity> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<SlurmApplicationDeploymentEntity> deployments) {
        this.deployments = deployments;
    }
}
