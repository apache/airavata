package org.apache.airavata.allocation.manager.db.repositories;

import java.util.List;
import java.util.Map;
import java.math.BigInteger;
import java.util.HashMap;
import org.apache.airavata.allocation.manager.db.entities.UserAllocationDetailEntity;
import org.apache.airavata.allocation.manager.db.utils.DBConstants;
import org.apache.airavata.allocation.manager.models.UserAllocationDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAllocationDetailRepository extends AbstractRepository<UserAllocationDetail, UserAllocationDetailEntity, String> {
    public UserAllocationDetailRepository(){
        super(UserAllocationDetail.class, UserAllocationDetailEntity.class);
    }
     
    public List<UserAllocationDetail> getAllUserRequests(String userName, String userRole) throws Exception{
       Map<String,Object> queryParameters = new HashMap<>();
        String query = "SELECT DISTINCT p from " + UserAllocationDetailEntity.class.getSimpleName() + " as p";
        if(!userRole.toUpperCase().equals(DBConstants.UserType.ADMIN)) {
        	query += " WHERE ";
    		 query += "p.username" + " = '" + userName + "'";
        }
        return select(query, queryParameters, 0, -1);

    }
    
    public long getProjectId(String userName) throws Exception{
        Map<String,Object> queryParameters = new HashMap<>();
         String query = "SELECT DISTINCT p from " + UserAllocationDetailEntity.class.getSimpleName() + " as p";
         query += " WHERE ";
 		 query += "p.username" + " = '" + userName + "'";
         return select(query, queryParameters, 0, -1).get(0).getProjectId();
     }
}