package org.apache.airavata.allocation.manager.db.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.allocation.manager.db.entities.ReviewerSpecificResourceDetailEntity;
import org.apache.airavata.allocation.manager.db.utils.DBConstants;
import org.apache.airavata.allocation.manager.models.ReviewerSpecificResourceDetail;

public class ReviewerSpecificResourceDetailRepository extends AbstractRepository<ReviewerSpecificResourceDetail, ReviewerSpecificResourceDetailEntity, String> {
    public ReviewerSpecificResourceDetailRepository(){
        super(ReviewerSpecificResourceDetail.class, ReviewerSpecificResourceDetailEntity.class);
    }
    
    public List<ReviewerSpecificResourceDetail> getList(long projectId) throws Exception{
    	Map<String, Object> queryParameters = new HashMap<>();
		String query = "SELECT DISTINCT p from " + ReviewerSpecificResourceDetailEntity.class.getSimpleName() + " as p";
		query += " WHERE ";
		query += "p." + "projectId" + " = " + projectId;
		return select(query, queryParameters, 0, -1);
     }
    
	public ReviewerSpecificResourceDetail getSpecificResource(long projectId, String specificResourceName,String reviewerName)
			throws Exception {
	Map<String, Object> queryParameters = new HashMap<>();
		String query = "SELECT DISTINCT p from " + ReviewerSpecificResourceDetailEntity.class.getSimpleName() + " as p";
		query += " WHERE ";
		query += "p." + DBConstants.UserAllocationDetailTable.PROJECTID + " = " + projectId + " AND ";
		query += "p.specificResource" + " = '" + specificResourceName  + "' AND ";
		query += "p.username" + " = '" + reviewerName  + "'";
		if(select(query, queryParameters, 0, -1).size() > 0) return select(query, queryParameters, 0, -1).get(0);
		else return null;
	}
}

