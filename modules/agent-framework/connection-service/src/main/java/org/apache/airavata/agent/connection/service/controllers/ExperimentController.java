package org.apache.airavata.agent.connection.service.controllers;

import org.apache.airavata.agent.connection.service.handlers.ExperimentHandler;
import org.apache.airavata.model.experiment.ExperimentModel;
import org.apache.airavata.tools.load.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@RequestMapping("/api/v1/exp")
public class ExperimentController {

    private final ExperimentHandler experimentHandler;

    public ExperimentController(ExperimentHandler experimentHandler) {
        this.experimentHandler = experimentHandler;
    }

    @GetMapping(value = "/{expId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExperimentModel> getExperiment(@PathVariable("expId") String expId) {
        return ResponseEntity.ok(experimentHandler.getExperiment(expId));
    }

    @PostMapping(value = "/launch", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createAndLaunchExperiment(@Valid @RequestBody Configuration experimentConfig) {
        String experimentId = experimentHandler.createAndLaunchExperiment(experimentConfig);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(experimentId)
                .encode()
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
