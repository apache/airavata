package org.apache.airavata.allocation.manager.db.repositories;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import org.apache.airavata.allocation.manager.db.entities.ReviewerAllocationDetailEntity;
import org.apache.airavata.allocation.manager.db.utils.DBConstants;
import org.apache.airavata.allocation.manager.models.ReviewerAllocationDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewerAllocationDetailRepository extends
		AbstractRepository<ReviewerAllocationDetail, ReviewerAllocationDetailEntity, String> {
	// private final static Logger logger =
	// LoggerFactory.getLogger(DomainRepository.class);

	public ReviewerAllocationDetailRepository() {
		super(ReviewerAllocationDetail.class, ReviewerAllocationDetailEntity.class);
	}

	public List<ReviewerAllocationDetail> getAllReviewsForARequest(String projectId) throws Exception {
		Map<String, Object> queryParameters = new HashMap<>();
		 String query = "SELECT DISTINCT p from " + ReviewerAllocationDetailEntity.class.getSimpleName() + " as p";
		query += " WHERE ";
		 query += "p." + DBConstants.UserAllocationDetailTable.PROJECTID + " = '" + projectId + " ' "; 
		return select(query, queryParameters, 0, -1);
	}
	
	public ReviewerAllocationDetail isProjectExists(String projectId,String reviewerId) throws Exception {
		Map<String, Object> queryParameters = new HashMap<>();
		 String query = "SELECT DISTINCT p from " + ReviewerAllocationDetailEntity.class.getSimpleName() + " as p";
		query += " WHERE ";
		query += "p." + DBConstants.UserAllocationDetailTable.PROJECTID + " = '" + projectId + "'" + "AND ";
		query += "p." + DBConstants.UserAllocationDetailTable.USERNAME + " = '" + reviewerId + "'"; 
		List<ReviewerAllocationDetail> reviewerAllocationDetail = select(query, queryParameters, 0, -1);
		if(reviewerAllocationDetail.size()>=1)
			return reviewerAllocationDetail.get(0);
		else
			return null;
	}
}