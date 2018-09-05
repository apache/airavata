package org.apache.airavata.allocation.manager.db.entities;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the PROJECT_REVIEWER database table.
 * 
 */
@Entity
@Table(name="PROJECT_REVIEWER")
@NamedQuery(name="ProjectReviewerEntity.findAll", query="SELECT p FROM ProjectReviewerEntity p")
public class ProjectReviewerEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private int id;

	@Column(name="PROJECT_ID")
	private int projectId;

	@Column(name="REVIEWER_USERNAME")
	private String reviewerUsername;

	public ProjectReviewerEntity() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getProjectId() {
		return this.projectId;
	}

	public void setProjectId(int projectId) {
		this.projectId = projectId;
	}

	public String getReviewerUsername() {
		return this.reviewerUsername;
	}

	public void setReviewerUsername(String reviewerUsername) {
		this.reviewerUsername = reviewerUsername;
	}

}