package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.registry.core.entities.expcatalog.UserConfigurationDataEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConfigurationDataRepository extends ExpCatAbstractRepository<UserConfigurationDataModel, UserConfigurationDataEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(UserConfigurationDataRepository.class);

    public UserConfigurationDataRepository() { super(UserConfigurationDataModel.class, UserConfigurationDataEntity.class); }
}