package org.apache.airavata.agent.connection.service.controllers;

import javax.validation.Valid;

import org.apache.airavata.agent.connection.service.handlers.AgentManagementHandler;
import org.apache.airavata.agent.connection.service.models.AgentLaunchRequest;
import org.apache.airavata.agent.connection.service.models.AgentLaunchResponse;
import org.apache.airavata.agent.connection.service.models.AgentTerminateResponse;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exp")
public class ExperimentController {

    private final AgentManagementHandler agentManagementHandler;

    public ExperimentController(AgentManagementHandler agentManagementHandler) {
        this.agentManagementHandler = agentManagementHandler;
    }

    @GetMapping(value = "/{expId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentModel> getExperiment(@PathVariable("expId") String expId) {
        return ResponseEntity.ok(agentManagementHandler.getExperiment(expId));
    }

    @PostMapping(value = "/launch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgentLaunchResponse> createAndLaunchExperiment(@Valid @RequestBody AgentLaunchRequest request) {
        AgentLaunchResponse agentResponse = agentManagementHandler.createAndLaunchExperiment(request);
        return ResponseEntity.ok(agentResponse);
    }

    @GetMapping(value = "/terminate/{expId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgentTerminateResponse> terminateExperiment(@PathVariable("expId") String expId) {
        return ResponseEntity.ok(agentManagementHandler.terminateExperiment(expId));
    }

    @GetMapping(value = "/{expId}/process", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcessModel> getEnvProcessModel(@PathVariable("expId") String expId) {
        ProcessModel processModel = agentManagementHandler.getEnvProcessModel(expId);
        if (processModel == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(processModel);
        }
    }
}
