package org.apache.airavata.registry.core.repositories.expcatalog;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.UserConfigurationDataModel;
import org.apache.airavata.registry.core.entities.expcatalog.UserConfigurationDataEntity;
import org.apache.airavata.registry.cpi.RegistryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserConfigurationDataRepository extends ExpCatAbstractRepository<UserConfigurationDataModel, UserConfigurationDataEntity, String> {
    private final static Logger logger = LoggerFactory.getLogger(UserConfigurationDataRepository.class);
    private ExperimentRepository experimentRepository = new ExperimentRepository();

    public UserConfigurationDataRepository() { super(UserConfigurationDataModel.class, UserConfigurationDataEntity.class); }

    public String addUserConfigurationData(UserConfigurationDataModel userConfigurationDataModel, String experimentId) throws RegistryException {
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        experimentModel.setUserConfigurationData(userConfigurationDataModel);
        experimentRepository.updateExperiment(experimentModel, experimentId);
        return experimentId;
    }

    public String updateUserConfigurationData(UserConfigurationDataModel updatedUserConfigurationDataModel, String experimentId) throws RegistryException {
        return addUserConfigurationData(updatedUserConfigurationDataModel, experimentId);
    }

    public UserConfigurationDataModel getUserConfigurationData(String experimentId) throws RegistryException {
        ExperimentModel experimentModel = experimentRepository.getExperiment(experimentId);
        return experimentModel.getUserConfigurationData();
    }

    public boolean isUserConfigurationDataExist(String experimentId) throws RegistryException {
        return isExists(experimentId);
    }

    public void removeUserConfigurationData(String experimentId) throws RegistryException {
        delete(experimentId);
    }

}