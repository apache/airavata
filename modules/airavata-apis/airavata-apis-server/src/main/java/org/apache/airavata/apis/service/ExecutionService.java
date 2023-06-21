package org.apache.airavata.apis.service;

import org.apache.airavata.api.execution.stubs.Experiment;

import java.util.Optional;

/**
 * Transactional service layer for CRUD operations on database.
 */
public interface ExecutionService {

    Experiment createExperiment(Experiment experiment);

    Experiment updateExperiment(Experiment experiment);

    Optional<Experiment> getExperiment(String experimentId);
}
