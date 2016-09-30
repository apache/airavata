package org.apache.airavata.registry.core.entities.workflowcatalog;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the workflow_output database table.
 * 
 */
@Entity
@Table(name="workflow_output")
public class WorkflowOutputEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private WorkflowOutputPK id;

	@Column(name="APP_ARGUMENT")
	private String appArgument;

	@Column(name="DATA_MOVEMENT")
	private short dataMovement;

	@Column(name="DATA_NAME_LOCATION")
	private String dataNameLocation;

	@Column(name="DATA_TYPE")
	private String dataType;

	@Column(name="IS_REQUIRED")
	private short isRequired;

	@Column(name="OUTPUT_STREAMING")
	private short outputStreaming;

	@Lob
	@Column(name="OUTPUT_VALUE")
	private String outputValue;

	@Column(name="REQUIRED_TO_COMMANDLINE")
	private short requiredToCommandline;

	@Column(name="SEARCH_QUERY")
	private String searchQuery;

	@Column(name="TEMPLATE_ID")
	private String templateId;

	public WorkflowOutputEntity() {
	}

	public WorkflowOutputPK getId() {
		return id;
	}

	public void setId(WorkflowOutputPK id) {
		this.id = id;
	}

	public String getAppArgument() {
		return appArgument;
	}

	public void setAppArgument(String appArgument) {
		this.appArgument = appArgument;
	}

	public short getDataMovement() {
		return dataMovement;
	}

	public void setDataMovement(short dataMovement) {
		this.dataMovement = dataMovement;
	}

	public String getDataNameLocation() {
		return dataNameLocation;
	}

	public void setDataNameLocation(String dataNameLocation) {
		this.dataNameLocation = dataNameLocation;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public short getIsRequired() {
		return isRequired;
	}

	public void setIsRequired(short isRequired) {
		this.isRequired = isRequired;
	}

	public short getOutputStreaming() {
		return outputStreaming;
	}

	public void setOutputStreaming(short outputStreaming) {
		this.outputStreaming = outputStreaming;
	}

	public String getOutputValue() {
		return outputValue;
	}

	public void setOutputValue(String outputValue) {
		this.outputValue = outputValue;
	}

	public short getRequiredToCommandline() {
		return requiredToCommandline;
	}

	public void setRequiredToCommandline(short requiredToCommandline) {
		this.requiredToCommandline = requiredToCommandline;
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}
}