package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.server.model.experiment.Experiment;
import org.apache.airavata.k8s.api.server.resources.experiment.ExperimentResource;
import org.apache.airavata.k8s.api.server.service.ExperimentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * TODO: Class level comments please
 *
 * @author dimuthu
 * @since 1.0.0-SNAPSHOT
 */
@RestController
@RequestMapping(path="/experiment")
public class ExperimentController {

    @Resource
    private ExperimentService experimentService;

    @PostMapping( path = "", consumes = MediaType.APPLICATION_JSON_VALUE)
    public String createExperiment(@RequestBody Experiment experiment) {

        return "Experiment success ";
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExperimentResource findExperimentById(@PathVariable("id") long id) {
        return this.experimentService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("Experiment with id " + id + " not found"));
    }
}
