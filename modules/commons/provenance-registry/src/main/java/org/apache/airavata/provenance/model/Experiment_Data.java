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
