package org.apache.airavata.allocation.manager.db.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.airavata.allocation.manager.db.entities.ProjectReviewerEntity;
import org.apache.airavata.allocation.manager.db.entities.ReviewerAllocationDetailEntity;
import org.apache.airavata.allocation.manager.db.utils.DBConstants;
import org.apache.airavata.allocation.manager.models.AllocationManagerException;
import org.apache.airavata.allocation.manager.models.ProjectReviewer;
import org.apache.airavata.allocation.manager.models.ReviewerAllocationDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectReviewerRepository
		extends AbstractRepository<ProjectReviewer, ProjectReviewerEntity, String> {
	// private final static Logger logger =
	// LoggerFactory.getLogger(DomainRepository.class);

	public ProjectReviewerRepository() {
		super(ProjectReviewer.class, ProjectReviewerEntity.class);
	}

	/* Method for getting a list of project assigned to a reviewer */

	public List<ProjectReviewer> getProjectForReviewer(String reviewerUserName) throws Exception {
		Map<String, Object> queryParameters = new HashMap<>();
		String query = "SELECT DISTINCT p from " + ProjectReviewerEntity.class.getSimpleName() + " as p";
		query += " WHERE ";
		query += "p." + DBConstants.ProjectReviewerTable.REVIEWER + " = " + "'" + reviewerUserName + "'";
		//queryParameters.put(DBConstants.ProjectReviewerTable.REVIEWER, reviewerUserName);
		return select(query, queryParameters, 0, -1);
	}
	
	public boolean isProjectExists(String projectId,String reviewerId) throws Exception {
		Map<String, Object> queryParameters = new HashMap<>();
		 String query = "SELECT DISTINCT p from " + ProjectReviewerEntity.class.getSimpleName() + " as p";
		query += " WHERE ";
		query += "p." + DBConstants.UserAllocationDetailTable.PROJECTID + " = '" + projectId + "'" + "AND ";
		query += "p." + DBConstants.ProjectReviewerTable.REVIEWER + " = '" + reviewerId + "'"; 
		List<ProjectReviewer> projectReviewer = select(query, queryParameters, 0, -1);
		if(projectReviewer.size()>=1)
			return true;
		else
			return false;
	}
}