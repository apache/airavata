package org.apache.airavata.allocation.manager.db.repositories;

import java.util.List;
import java.util.Map;
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
     
    public List<UserAllocationDetail> getAllUserRequests() throws Exception{
       Map<String,Object> queryParameters = new HashMap<>();
        String query = "SELECT DISTINCT p from " + UserAllocationDetailEntity.class.getSimpleName() + " as p";
        return select(query, queryParameters, 0, -1);

    }
}