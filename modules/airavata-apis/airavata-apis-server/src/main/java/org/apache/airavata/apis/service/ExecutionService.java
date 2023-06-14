package org.apache.airavata.apis.service;

import org.apache.airavata.api.execution.stubs.Experiment;

/**
 * Transactional service layer for CRUD operations on database.
 */
public interface ExecutionService {

    Experiment createExperiment(Experiment experiment);

    Experiment updateExperiment(Experiment experiment);
}
