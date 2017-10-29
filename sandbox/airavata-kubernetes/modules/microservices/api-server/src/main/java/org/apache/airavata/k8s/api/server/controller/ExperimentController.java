package org.apache.airavata.k8s.api.server.controller;

import org.apache.airavata.k8s.api.server.ServerRuntimeException;
import org.apache.airavata.k8s.api.resources.experiment.ExperimentResource;
import org.apache.airavata.k8s.api.server.service.ExperimentService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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
    public long createExperiment(@RequestBody ExperimentResource resource) {
        return experimentService.create(resource);
    }

    @GetMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ExperimentResource> getAllExperiments() {
        return this.experimentService.getAll();
    }

    @GetMapping(path = "{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ExperimentResource findExperimentById(@PathVariable("id") long id) {
        return this.experimentService.findById(id)
                .orElseThrow(() -> new ServerRuntimeException("Experiment with id " + id + " not found"));
    }

    @GetMapping(path = "{id}/launch", produces = MediaType.APPLICATION_JSON_VALUE)
    public long launchExperiment(@PathVariable("id") long id) {
        return this.experimentService.launchExperiment(id);
    }
}
