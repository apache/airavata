package org.apache.airavata.service.experiment;

import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.experiment.ExperimentStatistics;
import org.apache.airavata.model.status.ExperimentState;
import org.apache.airavata.model.status.ExperimentStatus;
import org.apache.airavata.model.status.JobStatus;
import org.apache.airavata.model.application.io.OutputDataObjectType;
import org.apache.airavata.registry.api.service.handler.RegistryServerHandler;
import org.apache.airavata.service.context.RequestContext;
import org.apache.airavata.service.exception.ServiceAuthorizationException;
import org.apache.airavata.service.exception.ServiceException;
import org.apache.airavata.service.messaging.EventPublisher;
import org.apache.airavata.sharing.registry.server.SharingRegistryServerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
class ExperimentServiceTest {

    @Mock RegistryServerHandler registryHandler;
    @Mock SharingRegistryServerHandler sharingHandler;
    @Mock EventPublisher eventPublisher;

    ExperimentService experimentService;
    RequestContext ctx;

    @BeforeEach
    void setUp() {
        experimentService = new ExperimentService(registryHandler, sharingHandler, eventPublisher);
        ctx = new RequestContext("testUser", "testGateway", "token123",
                Map.of("userName", "testUser", "gatewayId", "testGateway"));
    }

    @Test
    void createExperiment_returnsExperimentId() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setExperimentName("test-exp");
        experiment.setGatewayId("testGateway");
        experiment.setUserName("testUser");
        experiment.setProjectId("proj-1");

        when(registryHandler.createExperiment("testGateway", experiment)).thenReturn("exp-123");

        String result = experimentService.createExperiment(ctx, experiment);

        assertEquals("exp-123", result);
        verify(registryHandler).createExperiment("testGateway", experiment);
    }

    @Test
    void getExperiment_ownerGetsAccess() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");

        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);

        ExperimentModel result = experimentService.getExperiment(ctx, "exp-123");

        assertNotNull(result);
        assertEquals("testUser", result.getUserName());
    }

    @Test
    void deleteExperiment_onlyDeletesCreatedExperiments() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");
        ExperimentStatus status = new ExperimentStatus();
        status.setState(ExperimentState.CREATED);
        experiment.addToExperimentStatus(status);
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
        when(registryHandler.deleteExperiment("exp-123")).thenReturn(true);
        boolean result = experimentService.deleteExperiment(ctx, "exp-123");
        assertTrue(result);
        verify(registryHandler).deleteExperiment("exp-123");
    }

    @Test
    void deleteExperiment_rejectsNonCreatedExperiment() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("testUser");
        experiment.setGatewayId("testGateway");
        ExperimentStatus status = new ExperimentStatus();
        status.setState(ExperimentState.EXECUTING);
        experiment.addToExperimentStatus(status);
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
        assertThrows(ServiceException.class, () -> experimentService.deleteExperiment(ctx, "exp-123"));
    }

    @Test
    void getExperimentByAdmin_allowsSameGateway() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("otherUser");
        experiment.setGatewayId("testGateway");
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
        ExperimentModel result = experimentService.getExperimentByAdmin(ctx, "exp-123");
        assertNotNull(result);
    }

    @Test
    void getExperimentByAdmin_rejectsDifferentGateway() throws Exception {
        ExperimentModel experiment = new ExperimentModel();
        experiment.setUserName("otherUser");
        experiment.setGatewayId("otherGateway");
        when(registryHandler.getExperiment("exp-123")).thenReturn(experiment);
        assertThrows(ServiceAuthorizationException.class, () -> experimentService.getExperimentByAdmin(ctx, "exp-123"));
    }

    @Test
    void getExperimentStatus_delegatesToRegistry() throws Exception {
        ExperimentStatus status = new ExperimentStatus();
        status.setState(ExperimentState.COMPLETED);
        when(registryHandler.getExperimentStatus("exp-123")).thenReturn(status);
        ExperimentStatus result = experimentService.getExperimentStatus(ctx, "exp-123");
        assertEquals(ExperimentState.COMPLETED, result.getState());
    }

    @Test
    void getExperimentOutputs_delegatesToRegistry() throws Exception {
        List<OutputDataObjectType> outputs = List.of(new OutputDataObjectType());
        when(registryHandler.getExperimentOutputs("exp-123")).thenReturn(outputs);
        List<OutputDataObjectType> result = experimentService.getExperimentOutputs(ctx, "exp-123");
        assertEquals(1, result.size());
    }

    @Test
    void getExperimentStatistics_delegatesToRegistry() throws Exception {
        ExperimentStatistics stats = new ExperimentStatistics();
        stats.setAllExperimentCount(5);
        when(registryHandler.getExperimentStatistics(
                "testGateway", 1000L, 2000L, null, null, null, null, 10, 0))
                .thenReturn(stats);
        ExperimentStatistics result = experimentService.getExperimentStatistics(
                ctx, "testGateway", 1000L, 2000L, null, null, null, 10, 0);
        assertEquals(5, result.getAllExperimentCount());
    }

    @Test
    void updateExperiment_ownerCanUpdate() throws Exception {
        ExperimentModel existing = new ExperimentModel();
        existing.setUserName("testUser");
        existing.setGatewayId("testGateway");
        ExperimentModel updated = new ExperimentModel();
        updated.setExperimentName("new-name");
        updated.setProjectId("proj-1");

        when(registryHandler.getExperiment("exp-123")).thenReturn(existing);
        doNothing().when(registryHandler).updateExperiment("exp-123", updated);

        // Should not throw — owner has implicit WRITE
        assertDoesNotThrow(() -> experimentService.updateExperiment(ctx, "exp-123", updated));
        verify(registryHandler).updateExperiment("exp-123", updated);
    }

    @Test
    void getJobStatuses_delegatesToRegistry() throws Exception {
        Map<String, JobStatus> statuses = Map.of("job-1", new JobStatus());
        when(registryHandler.getJobStatuses("exp-123")).thenReturn(statuses);
        Map<String, JobStatus> result = experimentService.getJobStatuses(ctx, "exp-123");
        assertEquals(1, result.size());
    }

    @Test
    void validateExperiment_returnsTrue() throws Exception {
        boolean result = experimentService.validateExperiment(ctx, "exp-123");
        assertTrue(result);
    }
}
