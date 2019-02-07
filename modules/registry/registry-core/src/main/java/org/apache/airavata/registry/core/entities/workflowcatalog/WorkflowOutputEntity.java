/*
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
*/
package org.apache.airavata.registry.core.entities.workflowcatalog;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the workflow_output database table.
 * 
 */
@Entity
@Table(name="WORKFLOW_OUTPUT")
@IdClass(WorkflowOutputPK.class)
public class WorkflowOutputEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "TEMPLATE_ID")
	private String templateId;

	@Id
	@Column(name = "OUTPUT_KEY")
	private String name;

	@Column(name="APP_ARGUMENT")
	private String applicationArgument;

	@Column(name="DATA_MOVEMENT")
	private short dataMovement;

	@Column(name="DATA_NAME_LOCATION")
	private String location;

	@Column(name="DATA_TYPE")
	private String type;

	@Column(name="IS_REQUIRED")
	private short isRequired;

	@Column(name="OUTPUT_STREAMING")
	private short outputStreaming;

	@Column(name="OUTPUT_VALUE")
	private String value;

	@Column(name="REQUIRED_TO_COMMANDLINE")
	private short requiredToAddedToCommandLine;

	@Column(name="SEARCH_QUERY")
	private String searchQuery;

	@ManyToOne(targetEntity = WorkflowEntity.class, cascade = CascadeType.MERGE)
	@JoinColumn(name = "TEMPLATE_ID")
	private WorkflowEntity workflow;

	public WorkflowOutputEntity() {
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getApplicationArgument() {
		return applicationArgument;
	}

	public void setApplicationArgument(String applicationArgument) {
		this.applicationArgument = applicationArgument;
	}

	public short getDataMovement() {
		return dataMovement;
	}

	public void setDataMovement(short dataMovement) {
		this.dataMovement = dataMovement;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public short getRequiredToAddedToCommandLine() {
		return requiredToAddedToCommandLine;
	}

	public void setRequiredToAddedToCommandLine(short requiredToAddedToCommandLine) {
		this.requiredToAddedToCommandLine = requiredToAddedToCommandLine;
	}

	public String getSearchQuery() {
		return searchQuery;
	}

	public void setSearchQuery(String searchQuery) {
		this.searchQuery = searchQuery;
	}

	public WorkflowEntity getWorkflow() {
		return workflow;
	}

	public void setWorkflow(WorkflowEntity workflow) {
		this.workflow = workflow;
	}
}