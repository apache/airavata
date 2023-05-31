package org.apache.airavata.apis.handlers;

import org.apache.airavata.api.execution.stubs.Experiment;
import org.apache.airavata.api.gateway.ExperimentRegisterRequest;
import org.apache.airavata.api.gateway.ExperimentRegisterResponse;
import org.apache.airavata.apis.db.entity.ExperimentEntity;
import org.apache.airavata.apis.db.repository.ExperimentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@DataJpaTest
public class ExecutionHandlerTest {

    @Autowired
    ExecutionHandler executionHandler;

    @Autowired
    ExperimentRepository experimentRepository;

    @Test
    void testExperimentMapping() {

        Experiment experiment = Experiment.newBuilder()
                .setCreationTime(System.currentTimeMillis())
                .setDescription("Sample Exp")
                .setExperimentName("Exp Name")
                .build();

        ExperimentRegisterRequest experimentRegisterRequest = ExperimentRegisterRequest.newBuilder()
                .setExperiment(experiment).build();

        TestStreamObserver<ExperimentRegisterResponse> responseObserver = new TestStreamObserver<>();
        executionHandler.registerExperiment(experimentRegisterRequest, responseObserver);

        assertTrue(responseObserver.isCompleted());
        String experimentId = responseObserver.getNext().getExperimentId();
        ExperimentEntity experimentEntity = experimentRepository.findById(experimentId).get();

        assertEquals(experiment.getCreationTime(), experimentEntity.getCreationTime());
        assertEquals(experiment.getDescription(), experimentEntity.getDescription());
        assertEquals(experiment.getExperimentName(), experimentEntity.getExperimentName());
    }

}
