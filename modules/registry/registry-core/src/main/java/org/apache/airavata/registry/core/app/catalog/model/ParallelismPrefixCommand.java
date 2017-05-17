/**
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
 */
package org.apache.airavata.registry.core.app.catalog.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "PARALLELISM_COMMAND")
@IdClass(ParallelismPrefixCommand_PK.class)
public class ParallelismPrefixCommand implements Serializable {
	
	@Id
	@Column(name = "RESOURCE_JOB_MANAGER_ID")
	private String resourceJobManagerId;
	
	@ManyToOne(cascade= CascadeType.MERGE)
	@JoinColumn(name = "RESOURCE_JOB_MANAGER_ID")
	private ResourceJobManager resourceJobManager;
	
	@Id
	@Column(name = "COMMAND_TYPE")
	private String commandType;
	
	@Column(name = "COMMAND")
	private String command;
	
	public String getResourceJobManagerId() {
		return resourceJobManagerId;
	}
	
	public ResourceJobManager getResourceJobManager() {
		return resourceJobManager;
	}
	
	public String getCommandType() {
		return commandType;
	}
	
	public String getCommand() {
		return command;
	}
	
	public void setResourceJobManagerId(String resourceJobManagerId) {
		this.resourceJobManagerId=resourceJobManagerId;
	}
	
	public void setResourceJobManager(ResourceJobManager resourceJobManager) {
		this.resourceJobManager=resourceJobManager;
	}
	
	public void setCommandType(String commandType) {
		this.commandType=commandType;
	}
	
	public void setCommand(String command) {
		this.command=command;
	}
}
