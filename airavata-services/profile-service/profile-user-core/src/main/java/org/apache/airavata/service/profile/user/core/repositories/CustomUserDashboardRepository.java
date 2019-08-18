package org.apache.airavata.service.profile.user.core.repositories;

import org.apache.airavata.model.user.CustomDashboard;
import org.apache.airavata.model.user.UserProfile;
import org.apache.airavata.service.profile.commons.repositories.AbstractRepository;
import org.apache.airavata.service.profile.commons.user.entities.CustomizedDashboardEntity;
import org.apache.airavata.service.profile.commons.user.entities.UserProfileEntity;
import org.apache.airavata.service.profile.commons.utils.JPAUtils;
import org.apache.airavata.service.profile.commons.utils.ObjectMapperSingleton;
import org.apache.airavata.service.profile.commons.utils.QueryConstants;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomUserDashboardRepository extends AbstractRepository<CustomDashboard, CustomizedDashboardEntity, String> {

    private final static Logger logger = LoggerFactory.getLogger(CustomUserDashboardRepository.class);

    public CustomUserDashboardRepository() {super(CustomDashboard.class, CustomizedDashboardEntity.class);}

    public CustomDashboard getDashboardDetailsUsingAiravataInternalUserId(String airavataInternalUserId)   {
        CustomDashboard customDashboard = null;

        Map<String, Object> queryParam = new HashMap<String, Object>();
        queryParam.put(CustomDashboard._Fields.AIRAVATA_INTERNAL_USER_ID.getFieldName(), airavataInternalUserId);
        List<CustomDashboard> resultList = select(QueryConstants.FIND_CUSTOS_DASHBOARD_BY_AIRAVATA_ID, 1, 0, queryParam);

        if (resultList != null && resultList.size() > 0)
            customDashboard =  resultList.get(0);

        return customDashboard;
    }

    public CustomDashboard updateCustosDashboard(CustomDashboard customDashboard, Runnable postUpdateAction) {

        Mapper mapper = ObjectMapperSingleton.getInstance();
        CustomizedDashboardEntity entity = mapper.map(customDashboard, CustomizedDashboardEntity.class);
        CustomizedDashboardEntity persistedCopy = JPAUtils.execute(entityManager -> {
            CustomizedDashboardEntity result = entityManager.merge(entity);
            if (postUpdateAction != null) {
                postUpdateAction.run();
            }
            return result;
        });
        return mapper.map(persistedCopy, CustomDashboard.class);
    }
}
