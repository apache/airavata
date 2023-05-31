package org.apache.airavata.apis.mapper;

import com.github.dozermapper.core.Mapper;
import org.apache.airavata.api.execution.stubs.Experiment;
import org.apache.airavata.apis.db.entity.ExperimentEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExperimentMapper implements ObjectMapper<ExperimentEntity, Experiment> {

    @Autowired
    Mapper dozerMapper;

    @Override
    public Experiment mapEntityToModel(ExperimentEntity entity) {

        return dozerMapper.map(entity, Experiment.class);
    }

    @Override
    public ExperimentEntity mapModelToEntity(Experiment model) {
        ExperimentEntity entity = new ExperimentEntity();
        dozerMapper.map(model, entity);
        return entity;
    }

}
