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
package org.apache.airavata.provenance.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Experiment_Data {
	@Id
	private String experiment_ID;
	private String name;

	/*@OneToMany(cascade=CascadeType.ALL, mappedBy = "Experiment_Data")
	private final List<Workflow_Data> workflows = new ArrayList<Workflow_Data>();*/

	public String getExperiment_ID() {
		return experiment_ID;
	}

	public void setExperiment_ID(String experiment_ID) {
		this.experiment_ID = experiment_ID;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/*public List<Workflow_Data> getWorkflows() {
		return workflows;
	}*/
}
