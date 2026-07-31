package org.apache.airavata.application.service;

import org.apache.airavata.application.mapper.ApplicationMapper;
import org.apache.airavata.application.model.ApplicationInterfaceEntity;
import org.apache.airavata.models.appcatalog.appinterface.ApplicationInterfaceDescription;
import org.springframework.stereotype.Component;

@Component
public class ApplicationInterfaceService {

    protected ApplicationInterfaceDescription toModel(ApplicationInterfaceEntity entity) {
        return ApplicationMapper.INSTANCE.appInterfaceToModel(entity);
    }

    protected ApplicationInterfaceEntity toEntity(ApplicationInterfaceDescription model) {
        return ApplicationMapper.INSTANCE.appInterfaceToEntity(model);
    }
}
