package org.apache.airavata.allocation.manager.db.repositories;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.airavata.allocation.manager.db.utils.DBConstants;
import org.apache.airavata.allocation.manager.db.entities.ProjectReviewerEntity;
import org.apache.airavata.allocation.manager.db.entities.UserSpecificResourceDetailEntity;
import org.apache.airavata.allocation.manager.models.UserSpecificResourceDetail;

public class UserSpecificResourceDetailRespository
		extends AbstractRepository<UserSpecificResourceDetail, UserSpecificResourceDetailEntity, String> {
	public UserSpecificResourceDetailRespository() {
		super(UserSpecificResourceDetail.class, UserSpecificResourceDetailEntity.class);
	}

	public List<UserSpecificResourceDetail> getList(long projectId) throws Exception {
		Map<String, Object> queryParameters = new HashMap<>();
		String query = "SELECT DISTINCT p from " + UserSpecificResourceDetailEntity.class.getSimpleName() + " as p";
		query += " WHERE ";
		query += "p." + "projectId" + " = " + projectId;
		return select(query, queryParameters, 0, -1);
	}

	public UserSpecificResourceDetail getSpecificResource(long projectId, String specificResourceName)
			throws Exception {
	Map<String, Object> queryParameters = new HashMap<>();
		String query = "SELECT DISTINCT p from " + UserSpecificResourceDetailEntity.class.getSimpleName() + " as p";
		query += " WHERE ";
		query += "p." + DBConstants.UserAllocationDetailTable.PROJECTID + " = " + projectId + " AND ";
		query += "p.specificResource" + " = '" + specificResourceName  + "'";
		return select(query, queryParameters, 0, -1).get(0);
	}
	
}
