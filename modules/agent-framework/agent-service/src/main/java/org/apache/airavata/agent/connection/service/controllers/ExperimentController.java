package org.apache.airavata.agent.connection.service.controllers;

import org.apache.airavata.agent.connection.service.handlers.AgentManagementHandler;
import org.apache.airavata.agent.connection.service.models.LaunchAgentRequest;
import org.apache.airavata.agent.connection.service.models.LaunchAgentResponse;
import org.apache.airavata.agent.connection.service.models.TerminateAgentResponse;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.model.process.ProcessModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Valid;

class ExperimentController {

};

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
    public ResponseEntity<LaunchAgentResponse> createAndLaunchExperiment(@Valid @RequestBody LaunchAgentRequest request) {
        LaunchAgentResponse agentResponse = agentManagementHandler.createAndLaunchExperiment(request);
        return ResponseEntity.ok(agentResponse);
    }

    @GetMapping(value = "/terminate/{expId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TerminateAgentResponse> terminateExperiment(@PathVariable("expId") String expId) {
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
