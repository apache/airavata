package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the workflow_input database table.
 */
@Entity
@Table(name = "workflow_input")
public class WorkflowInputEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    private WorkflowInputPK id;

    @Column(name = "APP_ARGUMENT")
    private String appArgument;

    @Column(name = "DATA_STAGED")
    private short dataStaged;

    @Column(name = "DATA_TYPE")
    private String dataType;

    @Column(name = "INPUT_ORDER")
    private int inputOrder;

    @Column(name = "INPUT_VALUE")
    private String inputValue;

    @Column(name = "IS_REQUIRED")
    private short isRequired;

    @Column(name = "METADATA")
    private String metadata;

    @Column(name = "REQUIRED_TO_COMMANDLINE")
    private short requiredToCommandline;

    @Column(name = "STANDARD_INPUT")
    private short standardInput;

    @Column(name = "USER_FRIENDLY_DESC")
    private String userFriendlyDesc;


    @Column(name = "TEMPLATE_ID")
    private String templateId;

    public WorkflowInputEntity() {
    }

    public WorkflowInputPK getId() {
        return id;
    }

    public void setId(WorkflowInputPK id) {
        this.id = id;
    }

    public String getAppArgument() {
        return appArgument;
    }

    public void setAppArgument(String appArgument) {
        this.appArgument = appArgument;
    }

    public short getDataStaged() {
        return dataStaged;
    }

    public void setDataStaged(short dataStaged) {
        this.dataStaged = dataStaged;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public int getInputOrder() {
        return inputOrder;
    }

    public void setInputOrder(int inputOrder) {
        this.inputOrder = inputOrder;
    }

    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    public short getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(short isRequired) {
        this.isRequired = isRequired;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public short getRequiredToCommandline() {
        return requiredToCommandline;
    }

    public void setRequiredToCommandline(short requiredToCommandline) {
        this.requiredToCommandline = requiredToCommandline;
    }

    public short getStandardInput() {
        return standardInput;
    }

    public void setStandardInput(short standardInput) {
        this.standardInput = standardInput;
    }

    public String getUserFriendlyDesc() {
        return userFriendlyDesc;
    }

    public void setUserFriendlyDesc(String userFriendlyDesc) {
        this.userFriendlyDesc = userFriendlyDesc;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }
}